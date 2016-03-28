package BACnetCore.TransportLayerClasses.TransportIPV6;

import BACnetCore.TransportLayerClasses.TransportIPV6.BacnetIpV6UdpProtocolTransport;

/**
 * Created by Phil on 3/27/2016.
 */
public class BVLCV6
{
    BacnetIpV6UdpProtocolTransport MyTransport;
    BacnetAddress BroadcastAdd;

    public const byte BVLL_TYPE_BACNET_IPV6 = 0x82;
    public const byte BVLC_HEADER_LENGTH = 10; // Not all the time, could be 7 for bacnet broadcast
    public const BacnetMaxAdpu BVLC_MAX_APDU = BacnetMaxAdpu.MAX_APDU1476;

    public byte[] VMAC = new byte[3];
    public bool RandomVmac = false;

    bool BBMD_FD_ServiceActivated = false;
    // Contains the rules to accept FRD based on the IP adress
    // If empty it's equal to * , everyone allows
    List<Regex> AutorizedFDR = new List<Regex>();
    // Two lists for optional BBMD activity
    List<KeyValuePair<IPEndPoint, DateTime>> ForeignDevices = new List<KeyValuePair<IPEndPoint, DateTime>>();
    List<System.Net.IPEndPoint> BBMDs = new List<IPEndPoint>();

    public BVLCV6(BacnetIpV6UdpProtocolTransport Transport, int VMAC)
    {
        MyTransport = Transport;
        BroadcastAdd = MyTransport.GetBroadcastAddress();

        if (VMAC == -1)
        {
            RandomVmac = true;
            new Random().NextBytes(this.VMAC);
            this.VMAC[0] = (byte)((this.VMAC[0] & 0x7F) | 0x40); // ensure 01xxxxxx on the High byte

            // Open with default interface specified, cannot send it or
            // it will generate an uncheckable continuous local loopback
            if (!MyTransport.LocalEndPoint.ToString().Contains("[::]"))
                SendAddressResolutionRequest(this.VMAC);
            else
                RandomVmac = false; // back to false avoiding loop back
        }
        else // Device Id is the Vmac Id
        {
            this.VMAC[0] = (byte)((VMAC >> 16) & 0x3F); // ensure the 2 high bits are 0 on the High byte
            this.VMAC[1] = (byte)((VMAC >> 8) & 0xFF);
            this.VMAC[2] = (byte)(VMAC & 0xFF);
            // unicity is guaranteed by the end user !
        }
    }

    public void AddFDRAutorisationRule(Regex IpRule)
    {
        AutorizedFDR.Add(IpRule);
    }

    // Used to initiate the BBMD & FD behaviour, if BBMD is null it start the FD activity only
    public void AddBBMDPeer(IPEndPoint BBMD)
    {
        BBMD_FD_ServiceActivated = true;

        if (BBMD != null)
            lock (BBMDs)
        BBMDs.Add(BBMD);
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
                    MyTransport.Send(buffer, msg_length, client.Key);
            }
        }
    }

    // Send a Frame to each registered BBMD
    private void SendToBBMDs(byte[] buffer, int msg_length)
    {
        lock (BBMDs)
        {
            foreach (IPEndPoint ep in BBMDs)
            {
                MyTransport.Send(buffer, msg_length, ep);
            }
        }
    }
    // Never tested
    private void Forward_NPDU(byte[] buffer, int msg_length, bool ToGlobalBroadcast, IPEndPoint EPsender, BacnetAddress BacSender)
    {
        // Forms the forwarded NPDU from the original (broadcast npdu), and send it to all

        // copy, 18 bytes shifted (orignal bvlc header : 7 bytes, new one : 25 bytes)
        byte[] b = new byte[msg_length + 18];    // normaly only 'small' frames are present here, so no need to check if it's to big for Udp
        Array.Copy(buffer, 0, b, 18, msg_length);

        // 7 bytes for the BVLC Header, with the embedded 6 bytes IP:Port of the original sender
        First7BytesHeaderEncode(b, BacnetBvlcV6Functions.BVLC_FORWARDED_NPDU, msg_length + 18);
        // replace my Vmac by the orignal source vmac
        Array.Copy(BacSender.VMac, 0, b, 4, 3);
        // Add IpV6 endpoint
        Array.Copy(BacSender.adr, 0, b, 7, 18);
        // Send To BBMD
        SendToBBMDs(b, msg_length + 18);
        // Send To FD, except the sender
        SendToFDs(b, msg_length + 18, EPsender);
        // Broadcast if required
        if (ToGlobalBroadcast == true)
        {
            IPEndPoint ep;
            BacnetIpV6UdpProtocolTransport.Convert(BroadcastAdd, out ep);
            MyTransport.Send(b, msg_length + 18, ep);
        }
    }

    private void First7BytesHeaderEncode(byte[] b, BacnetBvlcV6Functions function, int msg_length)
    {
        b[0] = BVLL_TYPE_BACNET_IPV6;
        b[1] = (byte)function;
        b[2] = (byte)(((msg_length) & 0xFF00) >> 8);
        b[3] = (byte)(((msg_length) & 0x00FF) >> 0);
        Array.Copy(VMAC, 0, b, 4, 3);
    }

    // Send ack or nack
    private void SendResult(IPEndPoint sender, BacnetBvlcV6Results ResultCode)
    {
        byte[] b = new byte[9];
        First7BytesHeaderEncode(b, BacnetBvlcV6Functions.BVLC_RESULT, 9);
        b[7] = (byte)(((ushort)ResultCode & 0xFF00) >> 8);
        b[8] = (byte)((ushort)ResultCode & 0xFF);
        MyTransport.Send(b, 9, sender);
    }

    public void SendRegisterAsForeignDevice(IPEndPoint BBMD, short TTL)
    {
        byte[] b = new byte[9];
        First7BytesHeaderEncode(b, BacnetBvlcV6Functions.BVLC_REGISTER_FOREIGN_DEVICE, 9);
        b[7] = (byte)((TTL & 0xFF00) >> 8);
        b[8] = (byte)(TTL & 0xFF);
        MyTransport.Send(b, 9, BBMD);
    }

    public void SendRemoteWhois(byte[] buffer, IPEndPoint BBMD, int msg_length)
    {
        // 7 bytes for the BVLC Header
        First7BytesHeaderEncode(buffer, BacnetBvlcV6Functions.BVLC_DISTRIBUTE_BROADCAST_TO_NETWORK, msg_length);
        MyTransport.Send(buffer, msg_length, BBMD);
    }

    // Send ack
    private void SendAddressResolutionAck(IPEndPoint sender, byte[] VMacDest, BacnetBvlcV6Functions function)
    {
        byte[] b = new byte[10];
        First7BytesHeaderEncode(b, function, 10);
        Array.Copy(VMacDest, 0, b, 7, 3);
        MyTransport.Send(b, 10, sender);
    }

    // quite the same frame as the previous one
    private void SendAddressResolutionRequest(byte[] VMacDest)
    {
        IPEndPoint ep;
        BacnetIpV6UdpProtocolTransport.Convert(BroadcastAdd, out ep);

        byte[] b = new byte[10];
        First7BytesHeaderEncode(b, BacnetBvlcV6Functions.BVLC_ADDRESS_RESOLUTION, 10);
        Array.Copy(VMacDest, 0, b, 7, 3);
        MyTransport.Send(b, 10, ep);
    }

    // Encode is called by internal services if the BBMD is also an active device
    public int Encode(byte[] buffer, int offset, BacnetBvlcV6Functions function, int msg_length, BacnetAddress address)
    {
        // offset always 0, we are the first after udp

        First7BytesHeaderEncode(buffer, function, msg_length);

        // BBMD service
        if ((function == BacnetBvlcV6Functions.BVLC_ORIGINAL_BROADCAST_NPDU) && (BBMD_FD_ServiceActivated == true))
        {
            IPEndPoint me = MyTransport.LocalEndPoint;
            BacnetAddress Bacme;
            BacnetIpV6UdpProtocolTransport.Convert(me, out Bacme);
            Array.Copy(VMAC, Bacme.VMac, 3);

            Forward_NPDU(buffer, msg_length, false, me, Bacme);   // send to all BBMDs and FDs

            return 7; // ready to send
        }
        if (function == BacnetBvlcV6Functions.BVLC_ORIGINAL_UNICAST_NPDU)
        {
            buffer[7] = address.VMac[0];
            buffer[8] = address.VMac[1];
            buffer[9] = address.VMac[2];
            return 10; // ready to send
        }

        return 0; // ?
    }

    // Decode is called each time an Udp Frame is received
    public int Decode(byte[] buffer, int offset, out BacnetBvlcV6Functions function, out int msg_length, IPEndPoint sender, BacnetAddress remote_address)
    {

        // offset always 0, we are the first after udp
        // and a previous test by the caller guaranteed at least 4 bytes into the buffer

        function = (BacnetBvlcV6Functions)buffer[1];
        msg_length = (buffer[2] << 8) | (buffer[3] << 0);
        if ((buffer[0] != BVLL_TYPE_BACNET_IPV6) || (buffer.Length != msg_length)) return -1;

        Array.Copy(buffer, 4, remote_address.VMac, 0, 3);

        switch (function)
        {
            case BacnetBvlcV6Functions.BVLC_RESULT:
                return 9;   // only for the upper layers
            case BacnetBvlcV6Functions.BVLC_ORIGINAL_UNICAST_NPDU:
                return 10;   // only for the upper layers
            case BacnetBvlcV6Functions.BVLC_ORIGINAL_BROADCAST_NPDU:
                // Send to FDs & BBMDs, not broadcast or it will be made twice !
                if (BBMD_FD_ServiceActivated == true)
                    Forward_NPDU(buffer, msg_length, false, sender, remote_address);
                return 7;   // also for the upper layers
            case BacnetBvlcV6Functions.BVLC_ADDRESS_RESOLUTION:
                // need to verify that the VMAC is mine
                if ((VMAC[0] == buffer[7]) && (VMAC[1] == buffer[8]) && (VMAC[2] == buffer[9]))
                    // coming from myself ? avoid loopback
                    if (!MyTransport.LocalEndPoint.Equals(sender))
                        SendAddressResolutionAck(sender, remote_address.VMac, BacnetBvlcV6Functions.BVLC_ADDRESS_RESOLUTION_ACK);
                return 0;  // not for the upper layers
            case BacnetBvlcV6Functions.BVLC_FORWARDED_ADDRESS_RESOLUTION:
                // no need to verify the target VMAC, should be OK
                SendAddressResolutionAck(sender, remote_address.VMac, BacnetBvlcV6Functions.BVLC_ADDRESS_RESOLUTION_ACK);
                return 0;  // not for the upper layers
            case BacnetBvlcV6Functions.BVLC_ADDRESS_RESOLUTION_ACK: // adresse conflict
                if ((VMAC[0] == buffer[4]) && (VMAC[1] == buffer[5]) && (VMAC[2] == buffer[6]) && RandomVmac)
                {
                    new Random().NextBytes(this.VMAC);
                    this.VMAC[0] = (byte)((this.VMAC[0] & 0x7F) | 0x40);
                    SendAddressResolutionRequest(VMAC);
                }
                return 0;  // not for the upper layers
            case BacnetBvlcV6Functions.BVLC_VIRTUAL_ADDRESS_RESOLUTION:
                SendAddressResolutionAck(sender, remote_address.VMac, BacnetBvlcV6Functions.BVLC_VIRTUAL_ADDRESS_RESOLUTION_ACK);
                return 0;  // not for the upper layers
            case BacnetBvlcV6Functions.BVLC_VIRTUAL_ADDRESS_RESOLUTION_ACK:
                return 0;  // not for the upper layers
            case BacnetBvlcV6Functions.BVLC_FORWARDED_NPDU:
                if (MyTransport.LocalEndPoint.Equals(sender)) return 0;

                // certainly TODO the same code I've put in the IPV4 implementation
                if ((BBMD_FD_ServiceActivated == true) && (msg_length >= 25))
                {

                    bool ret;
                    lock (BBMDs)
                    ret = BBMDs.Exists(items => items.Equals(sender));    // verify sender presence in the table
                    // avoid also loopback

                    if (ret)    // message from a know BBMD address, sent to all FDs and broadcast
                    {
                        SendToFDs(buffer, msg_length);  // send without modification
                        // Assume all BVLC_FORWARDED_NPDU are directly sent to me in the
                        // unicast mode and not by the way of the multicast address
                        // If not, it's not really a big problem, devices on the local net will
                        // receive two times the message (after all it's just WhoIs, Iam, ...)
                        IPEndPoint ep;
                        BacnetIpV6UdpProtocolTransport.Convert(BroadcastAdd, out ep);
                        MyTransport.Send(buffer, msg_length, ep);
                    }
                }
                return 25;  // for the upper layers
            case BacnetBvlcV6Functions.BVLC_REGISTER_FOREIGN_DEVICE:
                if ((BBMD_FD_ServiceActivated == true) && (msg_length == 9))
                {
                    int TTL = (buffer[7] << 8) + buffer[8]; // unit is second
                    RegisterForeignDevice(sender, TTL);
                    SendResult(sender, BacnetBvlcV6Results.SUCCESSFUL_COMPLETION);  // ack
                }
                return 0;  // not for the upper layers
            case BacnetBvlcV6Functions.BVLC_DELETE_FOREIGN_DEVICE_TABLE_ENTRY:
                return 0;  // not for the upper layers
            case BacnetBvlcV6Functions.BVLC_SECURE_BVLC:
                return 0;  // not for the upper layers
            case BacnetBvlcV6Functions.BVLC_DISTRIBUTE_BROADCAST_TO_NETWORK:  // Sent by a Foreign Device, not a BBMD
                if (BBMD_FD_ServiceActivated == true)
                {
                    // Send to FDs except the sender, BBMDs and broadcast
                    lock (ForeignDevices)
                    {
                        if (ForeignDevices.Exists(item => item.Key.Equals(sender))) // verify previous registration
                        Forward_NPDU(buffer, msg_length, true, sender, remote_address);
                        else
                        SendResult(sender, BacnetBvlcV6Results.DISTRIBUTE_BROADCAST_TO_NETWORK_NAK);
                    }
                }
                return 0;  // not for the upper layers
            // error encoding function or experimental one
            default:
                return -1;
        }
    }
}