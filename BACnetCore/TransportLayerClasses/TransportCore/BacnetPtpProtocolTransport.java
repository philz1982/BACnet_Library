package BACnetCore.TransportLayerClasses.TransportCore;

/**
 * Created by Phil on 3/27/2016.
 */
public class BacnetPtpProtocolTransport : IBacnetTransport, IDisposable
        {
private IBacnetSerialTransport m_port;
private Thread m_thread;
private bool m_is_server;
private bool m_is_connected = false;
private bool m_sequence_counter = false;
private ManualResetEvent m_may_send = new ManualResetEvent(false);
private string m_password;

public event MessageRecievedHandler MessageRecieved;
public BacnetAddressTypes Type { get { return BacnetAddressTypes.PTP; } }
public int HeaderLength { get { return PTP.PTP_HEADER_LENGTH; } }
public int MaxBufferLength { get { return 502; } }
public BacnetMaxAdpu MaxAdpuLength { get { return PTP.PTP_MAX_APDU; } }
public byte MaxInfoFrames { get { return 0xff; } set { /* ignore */ } }     //the PTP doesn't have max info frames
public string Password { get { return m_password; } set { m_password = value; } }
public bool StateLogging { get; set; }

public const int T_HEARTBEAT = 15000;
public const int T_FRAME_ABORT = 2000;

public BacnetPtpProtocolTransport(IBacnetSerialTransport transport, bool is_server)
        {
        m_port = transport;
        m_is_server = is_server;
        }

public BacnetPtpProtocolTransport(string port_name, int baud_rate, bool is_server)
        : this(new BacnetSerialPortTransport(port_name, baud_rate), is_server)
        {
        }

public override bool Equals(object obj)
        {
        if (!(obj is BacnetPtpProtocolTransport)) return false;
        BacnetPtpProtocolTransport a = (BacnetPtpProtocolTransport)obj;
        return m_port.Equals(a.m_port);
        }

public override int GetHashCode()
        {
        return m_port.GetHashCode();
        }

public override string ToString()
        {
        return m_port.ToString();
        }

public int Send(byte[] buffer, int offset, int data_length, BacnetAddress address, bool wait_for_transmission, int timeout)
        {
        BacnetPtpFrameTypes frame_type = BacnetPtpFrameTypes.FRAME_TYPE_DATA0;
        if (m_sequence_counter) frame_type = BacnetPtpFrameTypes.FRAME_TYPE_DATA1;
        m_sequence_counter = !m_sequence_counter;

        //add header
        int full_length = PTP.Encode(buffer, offset - PTP.PTP_HEADER_LENGTH, frame_type, data_length);

        //wait for send allowed
        if (!m_may_send.WaitOne(timeout))
        return -BacnetMstpProtocolTransport.ETIMEDOUT;

        //debug
        if (StateLogging)
        Trace.WriteLine("         " + frame_type, null);

        //send
        SendWithXonXoff(buffer, offset - HeaderLength, full_length);
        return data_length;
        }

public bool WaitForAllTransmits(int timeout)
        {
        return true;        //PTP got no send queue
        }

public BacnetAddress GetBroadcastAddress()
        {
        return new BacnetAddress(BacnetAddressTypes.PTP, 0xFFFF, new byte[0]);
        }

public void Start()
        {
        if (m_port == null) return;

        m_thread = new Thread(new ThreadStart(ptp_thread));
        m_thread.Name = "PTP Read";
        m_thread.IsBackground = true;
        m_thread.Start();
        }

private void SendGreeting()
        {
        if (StateLogging)
        Trace.WriteLine("Sending Greeting", null);
        byte[] greeting = { PTP.PTP_GREETING_PREAMBLE1, PTP.PTP_GREETING_PREAMBLE2, 0x43, 0x6E, 0x65, 0x74, 0x0D };        //BACnet\n
        m_port.Write(greeting, 0, greeting.Length);
        }

private bool IsGreeting(byte[] buffer, int offset, int max_offset)
        {
        byte[] greeting = { PTP.PTP_GREETING_PREAMBLE1, PTP.PTP_GREETING_PREAMBLE2, 0x43, 0x6E, 0x65, 0x74, 0x0D };        //BACnet\n
        max_offset = Math.Min(offset + greeting.Length, max_offset);
        for (int i = offset; i < max_offset; i++)
        if (buffer[i] != greeting[i - offset])
        return false;
        return true;
        }

private void RemoveGreetingGarbage(byte[] buffer, ref int max_offset)
        {
        while (max_offset > 0)
        {
        while (max_offset > 0 && buffer[0] != 0x42)
        {
        if (max_offset > 1)
        Array.Copy(buffer, 1, buffer, 0, max_offset - 1);
        max_offset--;
        }
        if (max_offset > 1 && buffer[1] != 0x41)
        buffer[0] = 0xFF;
        else if (max_offset > 2 && buffer[2] != 0x43)
        buffer[0] = 0xFF;
        else if (max_offset > 3 && buffer[3] != 0x6E)
        buffer[0] = 0xFF;
        else if (max_offset > 4 && buffer[4] != 0x65)
        buffer[0] = 0xFF;
        else if (max_offset > 5 && buffer[5] != 0x74)
        buffer[0] = 0xFF;
        else if (max_offset > 6 && buffer[6] != 0x0D)
        buffer[0] = 0xFF;
        else
        break;
        }
        }

private bool WaitForGreeting(int timeout)
        {
        if (m_port == null) return false;
        byte[] buffer = new byte[7];
        int offset = 0;
        int current_timeout;
        while (offset < 7)
        {
        if (offset == 0) current_timeout = timeout;
        else current_timeout = T_FRAME_ABORT;
        int rx = m_port.Read(buffer, offset, 7 - offset, current_timeout);
        if (rx <= 0) return false;
        offset += rx;

        //remove garbage
        RemoveGreetingGarbage(buffer, ref offset);
        }
        return true;
        }

private bool Reconnect()
        {
        m_is_connected = false;
        m_may_send.Reset();
        if (m_port == null) return false;
        try
        {
        m_port.Close();
        }
        catch
        {
        }

        try
        {
        m_port.Open();
        }
        catch
        {
        return false;
        }

        //connect procedure
        if (m_is_server)
        {
        ////wait for greeting
        //if (!WaitForGreeting(-1))
        //{
        //    Trace.WriteLine("Garbage Greeting", null);
        //    return false;
        //}
        //if (StateLogging)
        //    Trace.WriteLine("Got Greeting", null);

        ////request connection
        //SendFrame(BacnetPtpFrameTypes.FRAME_TYPE_CONNECT_REQUEST);
        }
        else
        {
        //send greeting
        SendGreeting();
        }

        m_is_connected = true;
        return true;
        }

private void RemoveGarbage(byte[] buffer, ref int length)
        {
        //scan for preambles
        for (int i = 0; i < (length - 1); i++)
        {
        if ((buffer[i] == PTP.PTP_PREAMBLE1 && buffer[i + 1] == PTP.PTP_PREAMBLE2) || IsGreeting(buffer, i, length))
        {
        if (i > 0)
        {
        //move back
        Array.Copy(buffer, i, buffer, 0, length - i);
        length -= i;
        Trace.WriteLine("Garbage", null);
        }
        return;
        }
        }

        //one preamble?
        if (length > 0 && (buffer[length - 1] == PTP.PTP_PREAMBLE1 || buffer[length - 1] == PTP.PTP_GREETING_PREAMBLE1))
        {
        buffer[0] = buffer[length - 1];
        length = 1;
        Trace.WriteLine("Garbage", null);
        return;
        }

        //no preamble?
        if (length > 0)
        {
        length = 0;
        Trace.WriteLine("Garbage", null);
        }
        }

private void RemoveXonOff(byte[] buffer, int offset, ref int max_offset, ref bool compliment_next)
        {
        //X'10' (DLE)  => X'10' X'90'
        //X'11' (XON)  => X'10' X'91'
        //X'13' (XOFF) => X'10' X'93'

        for (int i = offset; i < max_offset; i++)
        {
        if (compliment_next)
        {
        buffer[i] &= 0x7F;
        compliment_next = false;
        }
        else if (buffer[i] == 0x11 || buffer[i] == 0x13 || buffer[i] == 0x10)
        {
        if (buffer[i] == 0x10) compliment_next = true;
        if ((max_offset - i) > 0)
        Array.Copy(buffer, i + 1, buffer, i, max_offset - i);
        max_offset--;
        i--;
        }
        }
        }

private void SendWithXonXoff(byte[] buffer, int offset, int length)
        {
        byte[] escape = new byte[1] { 0x10 };
        int max_offset = length + offset;

        //scan
        for (int i = offset; i < max_offset; i++)
        {
        if (buffer[i] == 0x10 || buffer[i] == 0x11 || buffer[i] == 0x13)
        {
        m_port.Write(buffer, offset, i - offset);
        m_port.Write(escape, 0, 1);
        buffer[i] |= 0x80;
        offset = i;
        }
        }

        //leftover
        m_port.Write(buffer, offset, max_offset - offset);
        }

private void SendFrame(BacnetPtpFrameTypes frame_type, byte[] buffer = null, int msg_length = 0)
        {
        if (m_port == null) return;
        int full_length = PTP.PTP_HEADER_LENGTH + msg_length + (msg_length > 0 ? 2 : 0);
        if (buffer == null) buffer = new byte[full_length];
        PTP.Encode(buffer, 0, frame_type, msg_length);

        //debug
        if (StateLogging)
        Trace.WriteLine("         " + frame_type, null);

        //send
        SendWithXonXoff(buffer, 0, full_length);
        }

private void SendDisconnect(BacnetPtpFrameTypes bacnetPtpFrameTypes, BacnetPtpDisconnectReasons bacnetPtpDisconnectReasons)
        {
        byte[] buffer = new byte[PTP.PTP_HEADER_LENGTH + 1 + 2];
        buffer[PTP.PTP_HEADER_LENGTH] = (byte)bacnetPtpDisconnectReasons;
        SendFrame(BacnetPtpFrameTypes.FRAME_TYPE_DISCONNECT_REQUEST, buffer, 1);
        }

private BacnetMstpProtocolTransport.GetMessageStatus ProcessRxStatus(byte[] buffer, ref int offset, int rx)
        {
        if (rx == -BacnetMstpProtocolTransport.ETIMEDOUT)
        {
        //drop message
        BacnetMstpProtocolTransport.GetMessageStatus status = offset == 0 ? BacnetMstpProtocolTransport.GetMessageStatus.Timeout : BacnetMstpProtocolTransport.GetMessageStatus.SubTimeout;
        buffer[0] = 0xFF;
        RemoveGarbage(buffer, ref offset);
        return status;
        }
        else if (rx < 0)
        {
        //drop message
        buffer[0] = 0xFF;
        RemoveGarbage(buffer, ref offset);
        return BacnetMstpProtocolTransport.GetMessageStatus.ConnectionError;
        }
        else if (rx == 0)
        {
        //drop message
        buffer[0] = 0xFF;
        RemoveGarbage(buffer, ref offset);
        return BacnetMstpProtocolTransport.GetMessageStatus.ConnectionClose;
        }
        return BacnetMstpProtocolTransport.GetMessageStatus.Good;
        }

private BacnetMstpProtocolTransport.GetMessageStatus GetNextMessage(byte[] buffer, ref int offset, int timeout_ms, out BacnetPtpFrameTypes frame_type, out int msg_length)
        {
        BacnetMstpProtocolTransport.GetMessageStatus status;
        int timeout = timeout_ms;

        frame_type = BacnetPtpFrameTypes.FRAME_TYPE_HEARTBEAT_XOFF;
        msg_length = 0;
        bool compliment_next = false;

        //fetch header
        while (offset < PTP.PTP_HEADER_LENGTH)
        {
        if (m_port == null) return BacnetMstpProtocolTransport.GetMessageStatus.ConnectionClose;

        if (offset > 0)
        timeout = T_FRAME_ABORT;    //set sub timeout
        else
        timeout = timeout_ms;       //set big silence timeout

        //read
        int rx = m_port.Read(buffer, offset, PTP.PTP_HEADER_LENGTH - offset, timeout);
        status = ProcessRxStatus(buffer, ref offset, rx);
        if (status != BacnetMstpProtocolTransport.GetMessageStatus.Good) return status;

        //remove XON/XOFF
        int new_offset = offset + rx;
        RemoveXonOff(buffer, offset, ref new_offset, ref compliment_next);
        offset = new_offset;

        //remove garbage
        RemoveGarbage(buffer, ref offset);
        }

        //greeting
        if (IsGreeting(buffer, 0, offset))
        {
        //get last byte
        int rx = m_port.Read(buffer, offset, 1, timeout);
        status = ProcessRxStatus(buffer, ref offset, rx);
        if (status != BacnetMstpProtocolTransport.GetMessageStatus.Good) return status;
        offset += 1;
        if (IsGreeting(buffer, 0, offset))
        {
        frame_type = BacnetPtpFrameTypes.FRAME_TYPE_GREETING;
        if (StateLogging) Trace.WriteLine(frame_type, null);
        return BacnetMstpProtocolTransport.GetMessageStatus.Good;
        }
        else
        {
        //drop message
        buffer[0] = 0xFF;
        RemoveGarbage(buffer, ref offset);
        return BacnetMstpProtocolTransport.GetMessageStatus.DecodeError;
        }
        }

        //decode
        if (PTP.Decode(buffer, 0, offset, out frame_type, out msg_length) < 0)
        {
        //drop message
        buffer[0] = 0xFF;
        RemoveGarbage(buffer, ref offset);
        return BacnetMstpProtocolTransport.GetMessageStatus.DecodeError;
        }

        //valid length?
        int full_msg_length = msg_length + PTP.PTP_HEADER_LENGTH + (msg_length > 0 ? 2 : 0);
        if (msg_length > MaxBufferLength)
        {
        //drop message
        buffer[0] = 0xFF;
        RemoveGarbage(buffer, ref offset);
        return BacnetMstpProtocolTransport.GetMessageStatus.DecodeError;
        }

        //fetch data
        if (msg_length > 0)
        {
        timeout = T_FRAME_ABORT;    //set sub timeout
        while (offset < full_msg_length)
        {
        //read
        int rx = m_port.Read(buffer, offset, full_msg_length - offset, timeout);
        status = ProcessRxStatus(buffer, ref offset, rx);
        if (status != BacnetMstpProtocolTransport.GetMessageStatus.Good) return status;

        //remove XON/XOFF
        int new_offset = offset + rx;
        RemoveXonOff(buffer, offset, ref new_offset, ref compliment_next);
        offset = new_offset;
        }

        //verify data crc
        if (PTP.Decode(buffer, 0, offset, out frame_type, out msg_length) < 0)
        {
        //drop message
        buffer[0] = 0xFF;
        RemoveGarbage(buffer, ref offset);
        return BacnetMstpProtocolTransport.GetMessageStatus.DecodeError;
        }
        }

        //debug
        if (StateLogging)
        Trace.WriteLine(frame_type, null);

        //done
        return BacnetMstpProtocolTransport.GetMessageStatus.Good;
        }

private void ptp_thread()
        {
        byte[] buffer = new byte[MaxBufferLength];
        try
        {
        while (m_port != null)
        {
        //connect if needed
        if (!m_is_connected)
        {
        if (!Reconnect())
        {
        Thread.Sleep(1000);
        continue;
        }
        }

        //read message
        int offset = 0;
        BacnetPtpFrameTypes frame_type;
        int msg_length;
        BacnetMstpProtocolTransport.GetMessageStatus status = GetNextMessage(buffer, ref offset, T_HEARTBEAT, out frame_type, out msg_length);

        //action
        switch (status)
        {
        case BacnetMstpProtocolTransport.GetMessageStatus.ConnectionClose:
        case BacnetMstpProtocolTransport.GetMessageStatus.ConnectionError:
        Trace.TraceWarning("Connection disturbance");
        Reconnect();
        break;
        case BacnetMstpProtocolTransport.GetMessageStatus.DecodeError:
        Trace.TraceWarning("PTP decode error");
        break;
        case BacnetMstpProtocolTransport.GetMessageStatus.SubTimeout:
        Trace.TraceWarning("PTP frame abort");
        break;
        case BacnetMstpProtocolTransport.GetMessageStatus.Timeout:
        SendFrame(BacnetPtpFrameTypes.FRAME_TYPE_HEARTBEAT_XON);    //both server and client will send this
        break;
        case BacnetMstpProtocolTransport.GetMessageStatus.Good:

        //action
        switch (frame_type)
        {
        case BacnetPtpFrameTypes.FRAME_TYPE_GREETING:
        //request connection
        SendFrame(BacnetPtpFrameTypes.FRAME_TYPE_CONNECT_REQUEST);
        break;
        case BacnetPtpFrameTypes.FRAME_TYPE_HEARTBEAT_XON:
        m_may_send.Set();
        break;
        case BacnetPtpFrameTypes.FRAME_TYPE_HEARTBEAT_XOFF:
        m_may_send.Reset();
        break;
        case BacnetPtpFrameTypes.FRAME_TYPE_DATA0:
        //send confirm
        SendFrame(BacnetPtpFrameTypes.FRAME_TYPE_DATA_ACK0_XON);

        //notify the sky!
        if (MessageRecieved != null)
        MessageRecieved(this, buffer, PTP.PTP_HEADER_LENGTH, msg_length, GetBroadcastAddress());
        break;
        case BacnetPtpFrameTypes.FRAME_TYPE_DATA1:
        //send confirm
        SendFrame(BacnetPtpFrameTypes.FRAME_TYPE_DATA_ACK1_XON);

        //notify the sky!
        if (MessageRecieved != null)
        MessageRecieved(this, buffer, PTP.PTP_HEADER_LENGTH, msg_length, GetBroadcastAddress());
        break;
        case BacnetPtpFrameTypes.FRAME_TYPE_DATA_ACK0_XOFF:
        case BacnetPtpFrameTypes.FRAME_TYPE_DATA_ACK1_XOFF:
        //so, the other one got the message, eh?
        m_may_send.Reset();
        break;
        case BacnetPtpFrameTypes.FRAME_TYPE_DATA_ACK0_XON:
        case BacnetPtpFrameTypes.FRAME_TYPE_DATA_ACK1_XON:
        //so, the other one got the message, eh?
        m_may_send.Set();
        break;
        case BacnetPtpFrameTypes.FRAME_TYPE_DATA_NAK0_XOFF:
        case BacnetPtpFrameTypes.FRAME_TYPE_DATA_NAK1_XOFF:
        m_may_send.Reset();
        //denial, eh?
        break;
        case BacnetPtpFrameTypes.FRAME_TYPE_DATA_NAK0_XON:
        case BacnetPtpFrameTypes.FRAME_TYPE_DATA_NAK1_XON:
        m_may_send.Set();
        //denial, eh?
        break;
        case BacnetPtpFrameTypes.FRAME_TYPE_CONNECT_REQUEST:
        //also send a password perhaps?
        if (!string.IsNullOrEmpty(m_password))
        {
        byte[] pass = System.Text.ASCIIEncoding.ASCII.GetBytes(m_password);
        byte[] tmp_buffer = new byte[PTP.PTP_HEADER_LENGTH + pass.Length + 2];
        Array.Copy(pass, 0, tmp_buffer, PTP.PTP_HEADER_LENGTH, pass.Length);
        SendFrame(BacnetPtpFrameTypes.FRAME_TYPE_CONNECT_RESPONSE, tmp_buffer, pass.Length);
        }
        else
        SendFrame(BacnetPtpFrameTypes.FRAME_TYPE_CONNECT_RESPONSE);

        //we're ready
        SendFrame(BacnetPtpFrameTypes.FRAME_TYPE_HEARTBEAT_XON);
        break;
        case BacnetPtpFrameTypes.FRAME_TYPE_CONNECT_RESPONSE:
        if (msg_length > 0 && !string.IsNullOrEmpty(m_password))
        {
        string password = System.Text.ASCIIEncoding.ASCII.GetString(buffer, PTP.PTP_HEADER_LENGTH, msg_length);
        if (password != m_password)
        SendDisconnect(BacnetPtpFrameTypes.FRAME_TYPE_DISCONNECT_REQUEST, BacnetPtpDisconnectReasons.PTP_DISCONNECT_INVALID_PASSWORD);
        else
        SendFrame(BacnetPtpFrameTypes.FRAME_TYPE_HEARTBEAT_XON);    //we're ready
        }
        else
        {
        //we're ready
        SendFrame(BacnetPtpFrameTypes.FRAME_TYPE_HEARTBEAT_XON);
        }
        break;
        case BacnetPtpFrameTypes.FRAME_TYPE_DISCONNECT_REQUEST:
        BacnetPtpDisconnectReasons reason = BacnetPtpDisconnectReasons.PTP_DISCONNECT_OTHER;
        if (msg_length > 0)
        reason = (BacnetPtpDisconnectReasons)buffer[PTP.PTP_HEADER_LENGTH];
        SendFrame(BacnetPtpFrameTypes.FRAME_TYPE_DISCONNECT_RESPONSE);
        Trace.WriteLine("Disconnect requested: " + reason.ToString(), null);
        Reconnect();
        break;
        case BacnetPtpFrameTypes.FRAME_TYPE_DISCONNECT_RESPONSE:
        m_may_send.Reset();
        //hopefully we'll be closing down now
        break;
        case BacnetPtpFrameTypes.FRAME_TYPE_TEST_REQUEST:
        SendFrame(BacnetPtpFrameTypes.FRAME_TYPE_TEST_RESPONSE, buffer, msg_length);
        break;
        case BacnetPtpFrameTypes.FRAME_TYPE_TEST_RESPONSE:
        //good
        break;
        }

        break;
        }
        }
        Trace.WriteLine("PTP thread is closing down", null);
        }
        catch (Exception ex)
        {
        Trace.TraceError("Exception in PTP thread: " + ex.Message);
        }
        }

public void Dispose()
        {
        if (m_port != null)
        {
        try
        {
        m_port.Close();
        }
        catch
        {
        }
        m_port = null;
        }
        }
        }
