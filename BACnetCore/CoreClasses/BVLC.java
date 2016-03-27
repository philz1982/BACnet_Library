package BACnetCore.CoreClasses;

/**
 * Created by Phil on 3/26/2016.
 */
public class BVLC

{
    BacnetIpUdpProtocolTransport MyBBMDTransport;
    String BroadcastAdd;

    bool BBMD_FD_ServiceActivated = false;

    public const byte BVLL_TYPE_BACNET_IP = 0x81;
    public const byte BVLC_HEADER_LENGTH = 4;
    public const BacnetMaxAdpu BVLC_MAX_APDU = BacnetMaxAdpu.MAX_APDU1476;

    // Two lists for optional BBMD activity
    List<KeyValuePair<IPEndPoint, DateTime>> ForeignDevices = new List<KeyValuePair<IPEndPoint, DateTime>>();
    List<KeyValuePair<IPEndPoint, IPAddress>> BBMDs = new List<KeyValuePair<IPEndPoint, IPAddress>>();

    // Contains the rules to accept FRD based on the IP adress
    // If empty it's equal to *.*.*.*, everyone allows
    List<Regex> AutorizedFDR = new List<Regex>();

    public BVLC(BacnetIpUdpProtocolTransport Transport)
    {
        MyBBMDTransport = Transport;
        BroadcastAdd = MyBBMDTransport.GetBroadcastAddress().ToString().Split(':')[0];
    }

    public string FDList()
    {
        StringBuilder sb = new StringBuilder();
        lock (ForeignDevices)
        {
            // remove oldest Device entries (Time expiration > TTL + 30s delay)
            ForeignDevices.Remove(ForeignDevices.Find(item => DateTime.Now > item.Value));

            foreach (KeyValuePair<IPEndPoint, DateTime> client in ForeignDevices)
            {
                sb.Append(client.Key.Address);
                sb.Append(":");
                sb.Append(client.Key.Port);
                sb.Append(";");
            }
        }
        return sb.ToString();
    }

    public void AddFDRAutorisationRule(Regex IpRule)
    {
        AutorizedFDR.Add(IpRule);
    }

    // Used to initiate the BBMD & FD behaviour, if BBMD is null it start the FD activity only
    public void AddBBMDPeer(IPEndPoint BBMD, IPAddress Mask)
    {
        BBMD_FD_ServiceActivated = true;

        if (BBMD != null)
            lock (BBMDs)
        BBMDs.Add(new KeyValuePair<IPEndPoint, IPAddress>(BBMD, Mask));
    }

    // Add a FD to the table or renew it
    private void RegisterForeignDevice(IPEndPoint sender, int TTL)
    {
        lock (ForeignDevices)
        {
            // remove it, if any
            ForeignDevices.Remove(ForeignDevices.Find(item => item.Key.Equals(sender)));
            // TTL + 30s grace period
            DateTime Expiration = DateTime.Now.AddSeconds(TTL + 30);
            // add it
            if (AutorizedFDR.Count == 0) // No rules, accept all
            {
                ForeignDevices.Add(new KeyValuePair<IPEndPoint, DateTime>(sender, Expiration));
                return;
            }
            else
                foreach (Regex r in AutorizedFDR)
            {
                if (r.Match(sender.Address.ToString()).Success)
                {
                    ForeignDevices.Add(new KeyValuePair<IPEndPoint, DateTime>(sender, Expiration));
                    return;
                }
            }
            System.Diagnostics.Trace.TraceInformation("Rejected FDR registration, IP : " + sender.Address.ToString());
        }
    }

    // Send a Frame to each registered foreign devices, except the original sender
    private void SendToFDs(byte[] buffer, int msg_length, IPEndPoint EPsender = null)
    {
        lock (ForeignDevices)
        {
            // remove oldest Device entries (Time expiration > TTL + 30s delay)
            ForeignDevices.Remove(ForeignDevices.Find(item => DateTime.Now > item.Value));
            // Send to all others, except the original sender
            foreach (KeyValuePair<IPEndPoint, DateTime> client in ForeignDevices)
            {
                if (!(client.Key.Equals(EPsender)))
                    MyBBMDTransport.Send(buffer, msg_length, client.Key);
            }
        }
    }

    private IPEndPoint BBMDSentAdd(IPEndPoint BBMD, IPAddress Mask)
    {
        byte[] bm = Mask.GetAddressBytes();
        byte[] bip = BBMD.Address.GetAddressBytes();

            /* annotation in Steve Karg bacnet stack :

            The B/IP address to which the Forwarded-NPDU message is
            sent is formed by inverting the broadcast distribution
            mask in the BDT entry and logically ORing it with the
            BBMD address of the same entry. This process
            produces either the directed broadcast address of the remote
            subnet or the unicast address of the BBMD on that subnet
            depending on the contents of the broadcast distribution
            mask.

            remark from me :
               for instance remote BBMD 192.168.0.1 - mask 255.255.255.255
                    messages are forward directly to 192.168.0.1
               remote BBMD 192.168.0.1 - mask 255.255.255.0
                    messages are forward to 192.168.0.255, ie certainly the local broadcast
                    address, but these datagrams are generaly destroy by the final IP router
             */

        for (int i = 0; i < bm.Length; i++)
            bip[i] = (byte)(bip[i] | (~bm[i]));

        return new IPEndPoint(new IPAddress(bip), BBMD.Port);
    }

    // Send a Frame to each registered BBMD except the original sender
    private void SendToBBMDs(byte[] buffer, int msg_length)
    {
        lock (BBMDs)
        {
            foreach (KeyValuePair<IPEndPoint, IPAddress> e in BBMDs)
            {
                IPEndPoint endpoint = BBMDSentAdd(e.Key, e.Value);
                MyBBMDTransport.Send(buffer, msg_length, endpoint);
            }
        }
    }

    private void First4BytesHeaderEncode(byte[] b, BacnetBvlcFunctions function, int msg_length)
    {
        b[0] = BVLL_TYPE_BACNET_IP;
        b[1] = (byte)function;
        b[2] = (byte)(((msg_length) & 0xFF00) >> 8);
        b[3] = (byte)(((msg_length) & 0x00FF) >> 0);
    }

    private void Forward_NPDU(byte[] buffer, int msg_length, bool ToGlobalBroadcast, IPEndPoint EPsender)
    {
        // Forms the forwarded NPDU from the original one, and send it to all
        // orignal     - 4 bytes BVLC -  NPDU  - APDU
        // change to   -  10 bytes BVLC  -  NPDU  - APDU

        // copy, 6 bytes shifted
        byte[] b = new byte[msg_length + 6];    // normaly only 'small' frames are present here, so no need to check if it's to big for Udp
        Array.Copy(buffer, 0, b, 6, msg_length);

        // 10 bytes for the BVLC Header, with the embedded 6 bytes IP:Port of the original sender
        First4BytesHeaderEncode(b, BacnetBvlcFunctions.BVLC_FORWARDED_NPDU, msg_length + 6);
        BacnetAddress BacSender;
        BacnetIpUdpProtocolTransport.Convert(EPsender, out BacSender); // to embbed in the forward BVLC header
        for (int i = 0; i < BacSender.adr.Length; i++)
            b[4 + i] = BacSender.adr[i];

        // To BBMD
        SendToBBMDs(b, msg_length + 6);
        // To FD, except the sender
        SendToFDs(b, msg_length + 6, EPsender);
        // Broadcast if required
        if (ToGlobalBroadcast == true)
            MyBBMDTransport.Send(b, msg_length + 6, new IPEndPoint(IPAddress.Parse(BroadcastAdd), MyBBMDTransport.SharedPort));
    }

    // Send ack or nack
    private void SendResult(IPEndPoint sender, BacnetBvlcResults ResultCode)
    {
        byte[] b = new byte[6];
        First4BytesHeaderEncode(b, BacnetBvlcFunctions.BVLC_RESULT, 6);
        b[4] = (byte)(((ushort)ResultCode & 0xFF00) >> 8);
        b[5] = (byte)((ushort)ResultCode & 0xFF);

        MyBBMDTransport.Send(b, 6, sender);
    }

    public void SendRegisterAsForeignDevice(IPEndPoint BBMD, short TTL)
    {
        byte[] b = new byte[6];
        First4BytesHeaderEncode(b, BacnetBvlcFunctions.BVLC_REGISTER_FOREIGN_DEVICE, 6);
        b[4] = (byte)((TTL & 0xFF00) >> 8);
        b[5] = (byte)(TTL & 0xFF);
        MyBBMDTransport.Send(b, 6, BBMD);
    }

    public void SendRemoteWhois(byte[] buffer, IPEndPoint BBMD, int msg_length)
    {
        Encode(buffer, 0, BacnetBvlcFunctions.BVLC_DISTRIBUTE_BROADCAST_TO_NETWORK, msg_length);
        MyBBMDTransport.Send(buffer, msg_length, BBMD);

    }
    // Encode is called by internal services if the BBMD is also an active device
    public int Encode(byte[] buffer, int offset, BacnetBvlcFunctions function, int msg_length)
    {
        // offset always 0, we are the first after udp

        // do the job
        First4BytesHeaderEncode(buffer, function, msg_length);

        // optional BBMD service
        if ((BBMD_FD_ServiceActivated == true) && (function == BacnetBvlcFunctions.BVLC_ORIGINAL_BROADCAST_NPDU))
        {
            IPEndPoint me = MyBBMDTransport.LocalEndPoint;
            // just sometime working, enable to get the local ep, always 0.0.0.0 if the socket is open with
            // System.Net.IPAddress.Any
            // So in this case don't send a bad message
            if ((me.Address.ToString() != "0.0.0.0"))
                Forward_NPDU(buffer, msg_length, false, me);   // send to all BBMDs and FDs
        }
        return 4; // ready to send
    }

    // Decode is called each time an Udp Frame is received
    public int Decode(byte[] buffer, int offset, out BacnetBvlcFunctions function, out int msg_length, IPEndPoint sender)
    {

        // offset always 0, we are the first after udp
        // and a previous test by the caller guaranteed at least 4 bytes into the buffer

        function = (BacnetBvlcFunctions)buffer[1];
        msg_length = (buffer[2] << 8) | (buffer[3] << 0);
        if ((buffer[0] != BVLL_TYPE_BACNET_IP) || (buffer.Length != msg_length)) return -1;

        switch (function)
        {
            case BacnetBvlcFunctions.BVLC_RESULT:
                return 4;   // only for the upper layers

            case BacnetBvlcFunctions.BVLC_ORIGINAL_UNICAST_NPDU:
                return 4;   // only for the upper layers

            case BacnetBvlcFunctions.BVLC_ORIGINAL_BROADCAST_NPDU: // Normaly received in an IP local or global broadcast packet
                // Send to FDs & BBMDs, not broadcast or it will be made twice !
                if (BBMD_FD_ServiceActivated == true)
                    Forward_NPDU(buffer, msg_length, false, sender);
                return 4;   // also for the upper layers

            case BacnetBvlcFunctions.BVLC_FORWARDED_NPDU:   // Sent only by a BBMD, broadcast on it network, or broadcast demand by one of it's FDs
                if ((BBMD_FD_ServiceActivated == true) && (msg_length >= 10))
                {
                    bool ret;
                    lock (BBMDs)
                    ret = BBMDs.Exists(items => items.Key.Address.Equals(sender.Address));    // verify sender (@ not Port!) presence in the table

                    if (ret)    // message from a know BBMD address, sent to all FDs and broadcast
                    {
                        SendToFDs(buffer, msg_length);  // send without modification

                        // Assume all BVLC_FORWARDED_NPDU are directly sent to me in the
                        // unicast mode and not by the way of the local broadcast address
                        // ie my mask must be 255.255.255.255 in the others BBMD tables
                        // If not, it's not really a big problem, devices on the local net will
                        // receive two times the message (after all it's just WhoIs, Iam, ...)
                        MyBBMDTransport.Send(buffer, msg_length, new IPEndPoint(IPAddress.Parse(BroadcastAdd), MyBBMDTransport.SharedPort));
                    }
                }

                return 10;  // also for the upper layers

            case BacnetBvlcFunctions.BVLC_DISTRIBUTE_BROADCAST_TO_NETWORK:  // Sent by a Foreign Device, not a BBMD
                if (BBMD_FD_ServiceActivated == true)
                {
                    // Send to FDs except the sender, BBMDs and broadcast
                    lock (ForeignDevices)
                    {
                        if (ForeignDevices.Exists(item => item.Key.Equals(sender))) // verify previous registration
                        Forward_NPDU(buffer, msg_length, true, sender);
                        else
                        SendResult(sender, BacnetBvlcResults.BVLC_RESULT_DISTRIBUTE_BROADCAST_TO_NETWORK_NAK);
                    }
                }
                return 4;   // also for the upper layers

            case BacnetBvlcFunctions.BVLC_REGISTER_FOREIGN_DEVICE:
                if ((BBMD_FD_ServiceActivated == true) && (msg_length == 6))
                {
                    int TTL = (buffer[4] << 8) + buffer[5]; // unit is second
                    RegisterForeignDevice(sender, TTL);
                    SendResult(sender, BacnetBvlcResults.BVLC_RESULT_SUCCESSFUL_COMPLETION);  // ack
                }
                return -1;  // not for the upper layers

            // We don't care about Read/Write operation in the BBMD/FDR tables (who realy use it ?)
            case BacnetBvlcFunctions.BVLC_READ_FOREIGN_DEVICE_TABLE:
                //SendResult(sender, BacnetBvlcResults.BVLC_RESULT_READ_FOREIGN_DEVICE_TABLE_NAK);
                return -1;
            case BacnetBvlcFunctions.BVLC_DELETE_FOREIGN_DEVICE_TABLE_ENTRY:
                //SendResult(sender, BacnetBvlcResults.BVLC_RESULT_DELETE_FOREIGN_DEVICE_TABLE_ENTRY_NAK);
                return -1;
            case BacnetBvlcFunctions.BVLC_READ_BROADCAST_DIST_TABLE:
                //SendResult(sender, BacnetBvlcResults.BVLC_RESULT_READ_BROADCAST_DISTRIBUTION_TABLE_NAK);
                return -1;
            case BacnetBvlcFunctions.BVLC_WRITE_BROADCAST_DISTRIBUTION_TABLE:
                //SendResult(sender, BacnetBvlcResults.BVLC_RESULT_WRITE_BROADCAST_DISTRIBUTION_TABLE_NAK);
                return -1;
            // error encoding function or experimental one
            default:
                return -1;
        }
    }
}