package BACnetCore.TransportLayerClasses.TransportCore;
import BACnetCore.Interfaces.*;

/**
 * Created by Phil on 3/27/2016.
 */
public class BacnetIpUdpProtocolTransport : IBacnetTransport, IDisposable
        {
private UdpClient m_shared_conn;
private UdpClient m_exclusive_conn;
private int m_port;

private BVLC bvlc;

public BVLC Bvlc { get { return bvlc; } }

private bool m_exclusive_port = false;
private bool m_dont_fragment;
private int m_max_payload;
private string m_local_endpoint;

public BacnetAddressTypes Type { get { return BacnetAddressTypes.IP; } }
public event MessageRecievedHandler MessageRecieved;
public int SharedPort { get { return m_port; } }
public int ExclusivePort { get { return ((IPEndPoint)m_exclusive_conn.Client.LocalEndPoint).Port; } }

public int HeaderLength { get { return BVLC.BVLC_HEADER_LENGTH; } }
public BacnetMaxAdpu MaxAdpuLength { get { return BVLC.BVLC_MAX_APDU; } }
public byte MaxInfoFrames { get { return 0xff; } set { /* ignore */ } }     //the udp doesn't have max info frames
public int MaxBufferLength { get { return m_max_payload; } }

public BacnetIpUdpProtocolTransport(int port, bool use_exclusive_port = false, bool dont_fragment = false, int max_payload = 1472, string local_endpoint_ip = "")
        {
        m_port = port;
        m_max_payload = max_payload;
        m_exclusive_port = use_exclusive_port;
        m_dont_fragment = dont_fragment;
        m_local_endpoint = local_endpoint_ip;

        }

public override bool Equals(object obj)
        {
        if (!(obj is BacnetIpUdpProtocolTransport)) return false;
        BacnetIpUdpProtocolTransport a = (BacnetIpUdpProtocolTransport)obj;
        return a.m_port == m_port;
        }

public override int GetHashCode()
        {
        return m_port.GetHashCode();
        }

public override string ToString()
        {
        return "Udp:" + m_port;
        }

private void Open()
        {

        if (!m_exclusive_port)
        {
                /* We need a shared broadcast "listen" port. This is the 0xBAC0 port */
                /* This will enable us to have more than 1 client, on the same machine. Perhaps it's not that important though. */
                /* We (might) only recieve the broadcasts on this. Any unicasts to this might be eaten by another local client */
        if (m_shared_conn == null)
        {
        m_shared_conn = new UdpClient();
        m_shared_conn.ExclusiveAddressUse = false;
        m_shared_conn.Client.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.ReuseAddress, true);
        EndPoint ep = new IPEndPoint(IPAddress.Any, m_port);
        if (!string.IsNullOrEmpty(m_local_endpoint)) ep = new IPEndPoint(IPAddress.Parse(m_local_endpoint), m_port);
        m_shared_conn.Client.Bind(ep);
        m_shared_conn.DontFragment = m_dont_fragment;
        }
                /* This is our own exclusive port. We'll recieve everything sent to this. */
                /* So this is how we'll present our selves to the world */
        if (m_exclusive_conn == null)
        {
        EndPoint ep = new IPEndPoint(IPAddress.Any, 0);
        if (!string.IsNullOrEmpty(m_local_endpoint)) ep = new IPEndPoint(IPAddress.Parse(m_local_endpoint), 0);
        m_exclusive_conn = new UdpClient((IPEndPoint)ep);
        m_exclusive_conn.DontFragment = m_dont_fragment;
        }
        }
        else
        {
        EndPoint ep = new IPEndPoint(IPAddress.Any, m_port);
        if (!string.IsNullOrEmpty(m_local_endpoint)) ep = new IPEndPoint(IPAddress.Parse(m_local_endpoint), m_port);
        m_exclusive_conn = new UdpClient();
        m_exclusive_conn.ExclusiveAddressUse = true;
        m_exclusive_conn.Client.Bind((IPEndPoint)ep);
        m_exclusive_conn.DontFragment = m_dont_fragment; m_exclusive_conn.EnableBroadcast = true;
        }

        bvlc = new BVLC(this);
        }

private void Close()
        {
        m_exclusive_conn.Close();
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
        BacnetBvlcFunctions function;
        int msg_length;
        if (rx < BVLC.BVLC_HEADER_LENGTH)
        {
        Trace.TraceWarning("Some garbage data got in");
        }
        else
        {
        // Basic Header lenght
        int HEADER_LENGTH = bvlc.Decode(local_buffer, 0, out function, out msg_length, ep);

        if (HEADER_LENGTH == -1)
        {
        Trace.WriteLine("Unknow BVLC Header");
        return;
        }

        // response to BVLC_REGISTER_FOREIGN_DEVICE (could be BVLC_DISTRIBUTE_BROADCAST_TO_NETWORK ... but we are not a BBMD, don't care)
        if (function == BacnetBvlcFunctions.BVLC_RESULT)
        {
        Trace.WriteLine("Receive Register as Foreign Device Response");
        }

        // a BVLC_FORWARDED_NPDU frame by a BBMD, change the remote_address to the original one (stored in the BVLC header)
        // we don't care about the BBMD address
        if (function == BacnetBvlcFunctions.BVLC_FORWARDED_NPDU)
        {
        long ip = ((long)local_buffer[7] << 24) + ((long)local_buffer[6] << 16) + ((long)local_buffer[5] << 8) + (long)local_buffer[4];
        int port = (local_buffer[8] << 8) + local_buffer[9];    // 0xbac0 maybe
        ep = new IPEndPoint(ip, port);

        Convert((IPEndPoint)ep, out remote_address);

        }

        if ((function == BacnetBvlcFunctions.BVLC_ORIGINAL_UNICAST_NPDU) || (function == BacnetBvlcFunctions.BVLC_ORIGINAL_BROADCAST_NPDU) || (function == BacnetBvlcFunctions.BVLC_FORWARDED_NPDU))
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

public bool SendRegisterAsForeignDevice(IPEndPoint BBMD, short TTL)
        {
        if (BBMD.AddressFamily == AddressFamily.InterNetwork)
        {
        bvlc.SendRegisterAsForeignDevice(BBMD, TTL);
        return true;
        }
        return false;
        }
public bool SendRemoteWhois(byte[] buffer, IPEndPoint BBMD, int msg_length)
        {
        if (BBMD.AddressFamily == AddressFamily.InterNetwork)
        {
        bvlc.SendRemoteWhois(buffer, BBMD, msg_length);
        return true;
        }
        return false;
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
        bvlc.Encode(buffer, offset - BVLC.BVLC_HEADER_LENGTH, address.net == 0xFFFF ? BacnetBvlcFunctions.BVLC_ORIGINAL_BROADCAST_NPDU : BacnetBvlcFunctions.BVLC_ORIGINAL_UNICAST_NPDU, full_length);

        //create end point
        IPEndPoint ep;
        Convert(address, out ep);

        try
        {
        //send
        return m_exclusive_conn.Send(buffer, full_length, ep);    //broadcasts are transported from our local unicast socket also
        }
        catch
        {
        return 0;
        }
        }

public static void Convert(IPEndPoint ep, out BacnetAddress addr)
        {
        byte[] tmp1 = ep.Address.GetAddressBytes();
        byte[] tmp2 = BitConverter.GetBytes((ushort)ep.Port);
        Array.Reverse(tmp2);
        Array.Resize<byte>(ref tmp1, tmp1.Length + tmp2.Length);
        Array.Copy(tmp2, 0, tmp1, tmp1.Length - tmp2.Length, tmp2.Length);
        addr = new BacnetAddress(BacnetAddressTypes.IP, 0, tmp1);
        }

public static void Convert(BacnetAddress addr, out IPEndPoint ep)
        {
        long ip_address = BitConverter.ToUInt32(addr.adr, 0);
        ushort port = (ushort)((addr.adr[4] << 8) | (addr.adr[5] << 0));
        ep = new IPEndPoint(ip_address, (int)port);
        }

// A lot of problems on Mono (Raspberry) to get the correct broadcast @
// so this method is overridable (this allows the implementation of operating system specific code)
// Marc solution http://stackoverflow.com/questions/8119414/how-to-query-the-subnet-masks-using-mono-on-linux for instance
//
protected virtual BacnetAddress _GetBroadcastAddress()
        {
        // general broadcast
        IPEndPoint ep = new IPEndPoint(IPAddress.Parse("255.255.255.255"), m_port);
        // restricted local broadcast (directed ... routable)
        foreach (NetworkInterface adapter in NetworkInterface.GetAllNetworkInterfaces())
        foreach (UnicastIPAddressInformation ip in adapter.GetIPProperties().UnicastAddresses)
        if (LocalEndPoint.Address.Equals(ip.Address))
        {
        try
        {
        string[] strCurrentIP = ip.Address.ToString().Split('.');
        string[] strIPNetMask = ip.IPv4Mask.ToString().Split('.');
        StringBuilder BroadcastStr = new StringBuilder();
        for (int i = 0; i < 4; i++)
        {
        BroadcastStr.Append(((byte)(int.Parse(strCurrentIP[i]) | ~int.Parse(strIPNetMask[i]))).ToString());
        if (i != 3) BroadcastStr.Append('.');
        }
        ep = new IPEndPoint(IPAddress.Parse(BroadcastStr.ToString()), m_port);
        }
        catch { }  //On mono IPv4Mask feature not implemented
        }
        BacnetAddress broadcast;
        Convert(ep, out broadcast);
        broadcast.net = 0xFFFF;
        return broadcast;
        }

        BacnetAddress BroadcastAddress = null;
public BacnetAddress GetBroadcastAddress()
        {
        if (BroadcastAddress == null) BroadcastAddress = _GetBroadcastAddress();
        return BroadcastAddress;
        }

// Give 0.0.0.0:xxxx if the socket is open with System.Net.IPAddress.Any
// Today only used by _GetBroadcastAddress method & the bvlc layer class in BBMD mode
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