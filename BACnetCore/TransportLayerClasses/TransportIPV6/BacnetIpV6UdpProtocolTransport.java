package BACnetCore.TransportLayerClasses.TransportIPV6;

/**
 * Created by Phil on 3/27/2016.
 */
public class BacnetIpV6UdpProtocolTransport : IBacnetTransport, IDisposable
        {
private UdpClient m_shared_conn;
private UdpClient m_exclusive_conn;
private int m_port;

private BVLCV6 bvlc;

public BVLCV6 Bvlc { get { return bvlc; } }

private bool m_exclusive_port = false;
private bool m_dont_fragment;
private int m_max_payload;
private string m_local_endpoint;

private int m_VMac;

public BacnetAddressTypes Type { get { return BacnetAddressTypes.IPV6; } }
public event MessageRecievedHandler MessageRecieved;
public int SharedPort { get { return m_port; } }

// Two frames type, unicast with 10 bytes or broadcast with 7 bytes
// Here it's the biggest header, resize will be done after, if needed
public int HeaderLength { get { return BVLCV6.BVLC_HEADER_LENGTH; } }

public BacnetMaxAdpu MaxAdpuLength { get { return BVLCV6.BVLC_MAX_APDU; } }
public byte MaxInfoFrames { get { return 0xff; } set { /* ignore */ } }     //the udp doesn't have max info frames
public int MaxBufferLength { get { return m_max_payload; } }

public BacnetIpV6UdpProtocolTransport(int port, int VMac = -1, bool use_exclusive_port = false, bool dont_fragment = false, int max_payload = 1472, string local_endpoint_ip = "")
        {
        m_port = port;
        m_max_payload = max_payload;
        m_exclusive_port = use_exclusive_port;
        m_dont_fragment = dont_fragment;
        m_local_endpoint = local_endpoint_ip;
        m_VMac = VMac;
        }

public override bool Equals(object obj)
        {
        if (!(obj is BacnetIpV6UdpProtocolTransport)) return false;
        BacnetIpV6UdpProtocolTransport a = (BacnetIpV6UdpProtocolTransport)obj;
        return a.m_port == m_port;
        }

public override int GetHashCode()
        {
        return m_port.GetHashCode();
        }

public override string ToString()
        {
        return "Udp IPv6:" + m_port;
        }

private void Open()
        {

        UdpClient multicastListener = null;

        if (!m_exclusive_port)
        {
                /* We need a shared multicast "listen" port. This is the 0xBAC0 port */
                /* This will enable us to have more than 1 client, on the same machine. Perhaps it's not that important though. */
                /* We (might) only receive the multicast on this. Any unicasts to this might be eaten by another local client */
        if (m_shared_conn == null)
        {
        m_shared_conn = new UdpClient(AddressFamily.InterNetworkV6);
        m_shared_conn.ExclusiveAddressUse = false;
        m_shared_conn.Client.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.ReuseAddress, true);
        EndPoint ep = new IPEndPoint(IPAddress.IPv6Any, m_port);
        if (!string.IsNullOrEmpty(m_local_endpoint)) ep = new IPEndPoint(IPAddress.Parse(m_local_endpoint), m_port);
        m_shared_conn.Client.Bind(ep);

        multicastListener = m_shared_conn;
        }
                /* This is our own exclusive port. We'll recieve everything sent to this. */
                /* So this is how we'll present our selves to the world */
        if (m_exclusive_conn == null)
        {
        EndPoint ep = new IPEndPoint(IPAddress.IPv6Any, 0);
        if (!string.IsNullOrEmpty(m_local_endpoint)) ep = new IPEndPoint(IPAddress.Parse(m_local_endpoint), 0);
        m_exclusive_conn = new UdpClient((IPEndPoint)ep);
        }
        }
        else
        {
        EndPoint ep = new IPEndPoint(IPAddress.IPv6Any, m_port);
        if (!string.IsNullOrEmpty(m_local_endpoint)) ep = new IPEndPoint(IPAddress.Parse(m_local_endpoint), m_port);
        m_exclusive_conn = new UdpClient(AddressFamily.InterNetworkV6);
        m_exclusive_conn.ExclusiveAddressUse = true;
        m_exclusive_conn.Client.Bind((IPEndPoint)ep);

        multicastListener = m_exclusive_conn;
        }

        multicastListener.JoinMulticastGroup(IPAddress.Parse("[FF02::BAC0]"));
        multicastListener.JoinMulticastGroup(IPAddress.Parse("[FF04::BAC0]"));
        multicastListener.JoinMulticastGroup(IPAddress.Parse("[FF05::BAC0]"));
        multicastListener.JoinMulticastGroup(IPAddress.Parse("[FF08::BAC0]"));
        multicastListener.JoinMulticastGroup(IPAddress.Parse("[FF0E::BAC0]"));

        // If this option is enabled Yabe cannot see itself !
        // multicastListener.MulticastLoopback = false;

        bvlc = new BVLCV6(this, m_VMac);
        }

private void Close()
        {
        if (m_shared_conn != null)
        m_shared_conn.BeginReceive(OnReceiveData, m_shared_conn);

        if (m_exclusive_conn != null)
        m_exclusive_conn.BeginReceive(OnReceiveData, m_exclusive_conn);
        }

public void Start()
        {
        Open();

        if (m_shared_conn != null)
        m_shared_conn.BeginReceive(OnReceiveData, m_shared_conn);

        if (m_exclusive_conn != null)
        m_exclusive_conn.BeginReceive(OnReceiveData, m_exclusive_conn);

        }

private void OnReceiveData(IAsyncResult asyncResult)
        {
        UdpClient conn = (UdpClient)asyncResult.AsyncState;
        try
        {
        IPEndPoint ep = new IPEndPoint(IPAddress.Any, 0);
        byte[] local_buffer;
        int rx = 0;

        try
        {
        local_buffer = conn.EndReceive(asyncResult, ref ep);
        rx = local_buffer.Length;
        }
        catch (Exception) // ICMP port unreachable
        {
        //restart data receive
        conn.BeginReceive(OnReceiveData, conn);
        return;
        }

        if (rx == 0)    // Empty frame : port scanner maybe
        {
        //restart data receive
        conn.BeginReceive(OnReceiveData, conn);
        return;
        }

        try
        {
        //verify message
        BacnetAddress remote_address;
        Convert((IPEndPoint)ep, out remote_address);
        BacnetBvlcV6Functions function;
        int msg_length;
        if (rx < BVLCV6.BVLC_HEADER_LENGTH - 3)
        {
        Trace.TraceWarning("Some garbage data got in");
        }
        else
        {
        // Basic Header lenght
        int HEADER_LENGTH = bvlc.Decode(local_buffer, 0, out function, out msg_length, ep, remote_address);

        if (HEADER_LENGTH == 0) return;

        if (HEADER_LENGTH == -1)
        {
        Trace.WriteLine("Unknow BVLC Header");
        return;
        }

        // response to BVLC_REGISTER_FOREIGN_DEVICE (could be BVLC_DISTRIBUTE_BROADCAST_TO_NETWORK ... but we are not a BBMD, don't care)
        if (function == BacnetBvlcV6Functions.BVLC_RESULT)
        {
        Trace.WriteLine("Receive Register as Foreign Device Response");
        }

        // a BVLC_FORWARDED_NPDU frame by a BBMD, change the remote_address to the original one (stored in the BVLC header)
        // we don't care about the BBMD address
        if (function == BacnetBvlcV6Functions.BVLC_FORWARDED_NPDU)
        {
        Array.Copy(local_buffer, 7, remote_address.adr, 0, 18);
        }

        if ((function == BacnetBvlcV6Functions.BVLC_ORIGINAL_UNICAST_NPDU) || (function == BacnetBvlcV6Functions.BVLC_ORIGINAL_BROADCAST_NPDU) || (function == BacnetBvlcV6Functions.BVLC_FORWARDED_NPDU))
        //send to upper layers
        if ((MessageRecieved != null) && (rx > HEADER_LENGTH)) MessageRecieved(this, local_buffer, HEADER_LENGTH, rx - HEADER_LENGTH, remote_address);
        }
        }
        catch (Exception ex)
        {
        Trace.TraceError("Exception in udp recieve: " + ex.Message);
        }
        finally
        {
        //restart data receive
        conn.BeginReceive(OnReceiveData, conn);
        }
        }
        catch (Exception ex)
        {
        //restart data receive
        if (conn.Client != null)
        {
        Trace.TraceError("Exception in Ip OnRecieveData: " + ex.Message);
        conn.BeginReceive(OnReceiveData, conn);
        }
        }
        }

public bool WaitForAllTransmits(int timeout)
        {
        //we got no sending queue in udp, so just return true
        return true;
        }

public static string ConvertToHex(byte[] buffer, int length)
        {
        string ret = "";

        for (int i = 0; i < length; i++)
        ret += buffer[i].ToString("X2");

        return ret;
        }

// Modif FC : used for BBMD communication
public int Send(byte[] buffer, int data_length, IPEndPoint ep)
        {
        try
        {
        // return m_exclusive_conn.Send(buffer, data_length, ep);
        System.Threading.ThreadPool.QueueUserWorkItem((o) => m_exclusive_conn.Send(buffer, data_length, ep), null);
        return data_length;
        }
        catch
        {
        return 0;
        }
        }

public int Send(byte[] buffer, int offset, int data_length, BacnetAddress address, bool wait_for_transmission, int timeout)
        {
        if (m_exclusive_conn == null) return 0;

        //add header
        int full_length = data_length + HeaderLength;

        if (address.net == 0xFFFF)
        {
        byte[] newBuffer = new byte[full_length - 3];
        Array.Copy(buffer, 3, newBuffer, 0, full_length - 3);
        full_length -= 3;
        buffer = newBuffer;
        bvlc.Encode(buffer, offset - BVLCV6.BVLC_HEADER_LENGTH, BacnetBvlcV6Functions.BVLC_ORIGINAL_BROADCAST_NPDU, full_length, address);
        }
        else
        bvlc.Encode(buffer, offset - BVLCV6.BVLC_HEADER_LENGTH, BacnetBvlcV6Functions.BVLC_ORIGINAL_UNICAST_NPDU, full_length, address);

        // create end point
        IPEndPoint ep;
        Convert(address, out ep);

        try
        {
        //send
        return m_exclusive_conn.Send(buffer, full_length, ep);    //multicast are transported from our local unicast socket also
        }
        catch
        {
        return 0;
        }
        }

public bool SendRegisterAsForeignDevice(IPEndPoint BBMD, short TTL)
        {
        if (BBMD.AddressFamily == AddressFamily.InterNetworkV6)
        {
        bvlc.SendRegisterAsForeignDevice(BBMD, TTL);
        return true;
        }
        return false;
        }
public bool SendRemoteWhois(byte[] buffer, IPEndPoint BBMD, int msg_length)
        {
        if (BBMD.AddressFamily == AddressFamily.InterNetworkV6)
        {
        // This message was build using the default (10) header lenght, but it's smaller (7)
        byte[] newBuffer = new byte[msg_length - 3];
        Array.Copy(buffer, 3, newBuffer, 0, msg_length - 3);
        msg_length -= 3;

        bvlc.SendRemoteWhois(newBuffer, BBMD, msg_length);
        return true;
        }
        return false;
        }

public static void Convert(IPEndPoint ep, out BacnetAddress addr)
        {
        byte[] tmp1 = ep.Address.GetAddressBytes();
        byte[] tmp2 = BitConverter.GetBytes((ushort)ep.Port);
        Array.Reverse(tmp2);
        Array.Resize<byte>(ref tmp1, tmp1.Length + tmp2.Length);
        Array.Copy(tmp2, 0, tmp1, tmp1.Length - tmp2.Length, tmp2.Length);
        addr = new BacnetAddress(BacnetAddressTypes.IPV6, 0, tmp1);
        }

public static void Convert(BacnetAddress addr, out IPEndPoint ep)
        {
        ushort port = (ushort)((addr.adr[16] << 8) | (addr.adr[17] << 0));
        byte[] Ipv6 = new byte[16];
        Array.Copy(addr.adr, Ipv6, 16);
        ep = new IPEndPoint(new IPAddress(Ipv6), (int)port);
        }

public BacnetAddress GetBroadcastAddress()
        {
        BacnetAddress ret;
        // could be FF08, FF05, FF04, FF02
        IPEndPoint ep = new IPEndPoint(IPAddress.Parse("[FF0E::BAC0]"), m_port);
        Convert(ep, out ret);
        ret.net = 0xFFFF;

        return ret;
        }

// Give [::]:xxxx if the socket is open with System.Net.IPAddress.IPv6Any
// Used the bvlc layer class in BBMD mode
// Some more complex solutions could avoid this, that's why this property is virtual
public virtual IPEndPoint LocalEndPoint
        {
        get
        {
        return (IPEndPoint)m_exclusive_conn.Client.LocalEndPoint;
        }
        }

public void Dispose()
        {
        try
        {
        m_exclusive_conn.Close();
        m_exclusive_conn = null;
        m_shared_conn.Close(); // maybe an exception if null
        m_shared_conn = null;
        }
        catch { }
        }
        }