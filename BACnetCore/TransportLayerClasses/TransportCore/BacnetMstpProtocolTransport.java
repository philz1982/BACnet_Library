package BACnetCore.TransportLayerClasses.TransportCore;

/**
 * Created by Phil on 3/27/2016.
 */
public class BacnetMstpProtocolTransport : IBacnetTransport, IDisposable
        {
private IBacnetSerialTransport m_port;
private short m_TS;             //"This Station," the MAC address of this node. TS is generally read from a hardware DIP switch, or from nonvolatile memory. Valid values for TS are 0 to 254. The value 255 is used to denote broadcast when used as a destination address but is not allowed as a value for TS.
private byte m_NS;              //"Next Station," the MAC address of the node to which This Station passes the token. If the Next Station is unknown, NS shall be equal to TS
private byte m_PS;              //"Poll Station," the MAC address of the node to which This Station last sent a Poll For Master. This is used during token maintenance
private byte m_max_master;
private byte m_max_info_frames;
private byte[] m_local_buffer;
private int m_local_offset;
private Thread m_transmit_thread;
private byte m_frame_count = 0;
private byte m_token_count = 0;
private byte m_max_poll = 50;                //The number of tokens received or used before a Poll For Master cycle is executed
private bool m_sole_master = false;
private byte m_retry_token = 1;
private byte m_reply_source;
private bool m_is_running = true;
private ManualResetEvent m_reply_mutex = new ManualResetEvent(false);
private MessageFrame m_reply = null;
private LinkedList<MessageFrame> m_send_queue = new LinkedList<MessageFrame>();

public const int T_FRAME_ABORT = 80;        //ms    The minimum time without a DataAvailable or ReceiveError event within a frame before a receiving node may discard the frame
public const int T_NO_TOKEN = 500;          //ms    The time without a DataAvailable or ReceiveError event before declaration of loss of token
public const int T_REPLY_TIMEOUT = 295;     //ms    The minimum time without a DataAvailable or ReceiveError event that a node must wait for a station to begin replying to a confirmed request
public const int T_USAGE_TIMEOUT = 95;      //ms    The minimum time without a DataAvailable or ReceiveError event that a node must wait for a remote node to begin using a token or replying to a Poll For Master frame:
public const int T_REPLY_DELAY = 250;       //ms    The maximum time a node may wait after reception of a frame that expects a reply before sending the first octet of a reply or Reply Postponed frame
public const int ETIMEDOUT = 110;

public BacnetAddressTypes Type { get { return BacnetAddressTypes.MSTP; } }
public short SourceAddress { get { return m_TS; } set { m_TS = value; } }
public byte MaxMaster { get { return m_max_master; } set { m_max_master = value; } }
public byte MaxInfoFrames { get { return m_max_info_frames; } set { m_max_info_frames = value; } }
public bool StateLogging { get; set; }

public bool IsRunning { get { return m_is_running; } }

public int HeaderLength { get { return MSTP.MSTP_HEADER_LENGTH; } }
public int MaxBufferLength { get { return 502; } }
public BacnetMaxAdpu MaxAdpuLength { get { return MSTP.MSTP_MAX_APDU; } }

public delegate void FrameRecievedHandler(BacnetMstpProtocolTransport sender, BacnetMstpFrameTypes frame_type, byte destination_address, byte source_address, int msg_length);
public event MessageRecievedHandler MessageRecieved;
public event FrameRecievedHandler FrameRecieved;

        #region " Sniffer mode "

// Used in Sniffer only mode
public delegate void RawMessageReceivedHandler(byte[] buffer, int offset, int lenght);
public event RawMessageReceivedHandler RawMessageRecieved;

public void Start_SpyMode()
        {
        if (m_port == null) return;
        m_port.Open();

        Thread th = new Thread(mstp_thread_sniffer);
        th.IsBackground = true;
        th.Start();

        }

// Just Sniffer mode, no Bacnet activity generated here
// Modif FC
private void mstp_thread_sniffer()
        {
        for (;;)
        {
        BacnetMstpFrameTypes frame_type;
        byte destination_address;
        byte source_address;
        int msg_length;

        try
        {
        GetMessageStatus status = GetNextMessage(T_NO_TOKEN, out frame_type, out destination_address, out source_address, out msg_length);

        if (status == GetMessageStatus.ConnectionClose)
        {
        m_port = null;
        return;
        }
        else if (status == GetMessageStatus.Good)
        {
        // frame event client ?
        if (RawMessageRecieved != null)
        {

        int length = msg_length + MSTP.MSTP_HEADER_LENGTH + (msg_length > 0 ? 2 : 0);

        // Array copy
        // after that it could be put asynchronously another time in the Main message loop
        // without any problem
        byte[] packet = new byte[length];
        Array.Copy(m_local_buffer, 0, packet, 0, length);

        // No need to use the thread pool, if the pipe is too slow
        // frames task list will grow infinitly
        RawMessageRecieved(packet, 0, length);
        }

        RemoveCurrentMessage(msg_length);
        }
        }
        catch
        {
        m_port = null;
        }
        }
        }


        #endregion

public BacnetMstpProtocolTransport(IBacnetSerialTransport transport, short source_address = -1, byte max_master = 127, byte max_info_frames = 1)
        {
        m_max_info_frames = max_info_frames;
        m_TS = source_address;
        m_max_master = max_master;
        m_local_buffer = new byte[MaxBufferLength];
        m_port = transport;
        }

public BacnetMstpProtocolTransport(string port_name, int baud_rate, short source_address = -1, byte max_master = 127, byte max_info_frames = 1)
        : this(new BacnetSerialPortTransport(port_name, baud_rate), source_address, max_master, max_info_frames)
        {
        }

public override bool Equals(object obj)
        {
        if (obj == null) return false;
        else if (!(obj is BacnetMstpProtocolTransport)) return false;
        BacnetMstpProtocolTransport a = (BacnetMstpProtocolTransport)obj;
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

public void Start()
        {
        if (m_port == null) return;
        m_port.Open();

        m_transmit_thread = new Thread(new ThreadStart(mstp_thread));
        m_transmit_thread.IsBackground = true;
        m_transmit_thread.Name = "MSTP Thread";
        m_transmit_thread.Priority = ThreadPriority.Highest;
        m_transmit_thread.Start();
        }

private class MessageFrame
{
    public BacnetMstpFrameTypes frame_type;
    public byte destination_address;
    public byte[] data;
    public int data_length;
    public ManualResetEvent send_mutex;
    public MessageFrame(BacnetMstpFrameTypes frame_type, byte destination_address, byte[] data, int data_length)
    {
        this.frame_type = frame_type;
        this.destination_address = destination_address;
        this.data = data;
        this.data_length = data_length;
        send_mutex = new ManualResetEvent(false);
    }
}

    private void QueueFrame(BacnetMstpFrameTypes frame_type, byte destination_address)
    {
        lock (m_send_queue)
        m_send_queue.AddLast(new MessageFrame(frame_type, destination_address, null, 0));
    }

    private void SendFrame(BacnetMstpFrameTypes frame_type, byte destination_address)
    {
        SendFrame(new MessageFrame(frame_type, destination_address, null, 0));
    }

    private void SendFrame(MessageFrame frame)
    {
        if (m_TS == -1 || m_port == null) return;
        int tx;
        if (frame.data == null || frame.data.Length == 0)
        {
            byte[] tmp_transmit_buffer = new byte[MSTP.MSTP_HEADER_LENGTH];
            tx = MSTP.Encode(tmp_transmit_buffer, 0, frame.frame_type, frame.destination_address, (byte)m_TS, 0);
            m_port.Write(tmp_transmit_buffer, 0, tx);
        }
        else
        {
            tx = MSTP.Encode(frame.data, 0, frame.frame_type, frame.destination_address, (byte)m_TS, frame.data_length);
            m_port.Write(frame.data, 0, tx);
        }
        frame.send_mutex.Set();

        //debug
        if (StateLogging) Trace.WriteLine("         " + frame.frame_type + " " + frame.destination_address.ToString("X2") + " ");
    }

    private void RemoveCurrentMessage(int msg_length)
    {
        int full_msg_length = MSTP.MSTP_HEADER_LENGTH + msg_length + (msg_length > 0 ? 2 : 0);
        if (m_local_offset > full_msg_length)
            Array.Copy(m_local_buffer, full_msg_length, m_local_buffer, 0, m_local_offset - full_msg_length);
        m_local_offset -= full_msg_length;
    }

private enum StateChanges
{
    /* Initializing */
    Reset,
    DoneInitializing,

    /* Idle, NoToken */
    GenerateToken,
    ReceivedDataNeedingReply,
    ReceivedToken,

    /* PollForMaster */
    ReceivedUnexpectedFrame,        //also from WaitForReply
    DoneWithPFM,
    ReceivedReplyToPFM,
    SoleMaster,                     //also from DoneWithToken
    DeclareSoleMaster,

    /* UseToken */
    SendAndWait,
    NothingToSend,
    SendNoWait,

    /* DoneWithToken */
    SendToken,
    ResetMaintenancePFM,
    SendMaintenancePFM,
    SoleMasterRestartMaintenancePFM,
    SendAnotherFrame,
    NextStationUnknown,

    /* WaitForReply */
    ReplyTimeOut,
    InvalidFrame,
    ReceivedReply,
    ReceivedPostpone,

    /* PassToken */
    FindNewSuccessor,
    SawTokenUser,

    /* AnswerDataRequest */
    Reply,
    DeferredReply,
}

    private StateChanges PollForMaster()
    {
        BacnetMstpFrameTypes frame_type;
        byte destination_address;
        byte source_address;
        int msg_length;

        while (true)
        {
            //send
            SendFrame(BacnetMstpFrameTypes.FRAME_TYPE_POLL_FOR_MASTER, m_PS);

            //wait
            GetMessageStatus status = GetNextMessage(T_USAGE_TIMEOUT, out frame_type, out destination_address, out source_address, out msg_length);

            if (status == GetMessageStatus.Good)
            {
                try
                {
                    if (frame_type == BacnetMstpFrameTypes.FRAME_TYPE_REPLY_TO_POLL_FOR_MASTER && destination_address == m_TS)
                    {
                        m_sole_master = false;
                        m_NS = source_address;
                        m_PS = (byte)m_TS;
                        m_token_count = 0;
                        return StateChanges.ReceivedReplyToPFM;
                    }
                    else
                        return StateChanges.ReceivedUnexpectedFrame;
                }
                finally
                {
                    RemoveCurrentMessage(msg_length);
                }
            }
            else
            {
                if (m_sole_master)
                {
                        /* SoleMaster */
                    m_frame_count = 0;
                    return StateChanges.SoleMaster;
                }
                else
                {
                    if (m_NS != m_TS)
                    {
                            /* DoneWithPFM */
                        return StateChanges.DoneWithPFM;
                    }
                    else
                    {
                        if ((m_PS + 1) % (m_max_master + 1) != m_TS)
                        {
                                /* SendNextPFM */
                            m_PS = (byte)((m_PS + 1) % (m_max_master + 1));
                            continue;
                        }
                        else
                        {
                                /* DeclareSoleMaster */
                            m_sole_master = true;
                            m_frame_count = 0;
                            return StateChanges.DeclareSoleMaster;
                        }
                    }
                }
            }
        }
    }

    private StateChanges DoneWithToken()
    {
        if (m_frame_count < m_max_info_frames)
        {
                /* SendAnotherFrame */
            return StateChanges.SendAnotherFrame;
        }
        else if (!m_sole_master && m_NS == m_TS)
        {
                /* NextStationUnknown */
            m_PS = (byte)((m_TS + 1) % (m_max_master + 1));
            return StateChanges.NextStationUnknown;
        }
        else if (m_token_count < (m_max_poll - 1))
        {
            m_token_count++;
            if (m_sole_master && m_NS != ((m_TS + 1) % (m_max_master + 1)))
            {
                    /* SoleMaster */
                m_frame_count = 0;
                return StateChanges.SoleMaster;
            }
            else
            {
                    /* SendToken */
                return StateChanges.SendToken;
            }
        }
        else if ((m_PS + 1) % (m_max_master + 1) == m_NS)
        {
            if (!m_sole_master)
            {
                    /* ResetMaintenancePFM */
                m_PS = (byte)m_TS;
                m_token_count = 1;
                return StateChanges.ResetMaintenancePFM;
            }
            else
            {
                    /* SoleMasterRestartMaintenancePFM */
                m_PS = (byte)((m_NS + 1) % (m_max_master + 1));
                m_NS = (byte)m_TS;
                m_token_count = 1;
                return StateChanges.SoleMasterRestartMaintenancePFM;
            }
        }
        else
        {
                /* SendMaintenancePFM */
            m_PS = (byte)((m_PS + 1) % (m_max_master + 1));
            return StateChanges.SendMaintenancePFM;
        }
    }

    private StateChanges WaitForReply()
    {
        BacnetMstpFrameTypes frame_type;
        byte destination_address;
        byte source_address;
        int msg_length;

        //fetch message
        GetMessageStatus status = GetNextMessage(T_REPLY_TIMEOUT, out frame_type, out destination_address, out source_address, out msg_length);

        if (status == GetMessageStatus.Good)
        {
            try
            {
                if (destination_address == (byte)m_TS && (frame_type == BacnetMstpFrameTypes.FRAME_TYPE_TEST_RESPONSE || frame_type == BacnetMstpFrameTypes.FRAME_TYPE_BACNET_DATA_NOT_EXPECTING_REPLY))
                {
                    //signal upper layer
                    if (MessageRecieved != null && frame_type != BacnetMstpFrameTypes.FRAME_TYPE_TEST_RESPONSE)
                    {
                        BacnetAddress remote_address = new BacnetAddress(BacnetAddressTypes.MSTP, 0, new byte[] { source_address });
                        try
                        {
                            MessageRecieved(this, m_local_buffer, MSTP.MSTP_HEADER_LENGTH, msg_length, remote_address);
                        }
                        catch (Exception ex)
                        {
                            Trace.TraceError("Exception in MessageRecieved event: " + ex.Message);
                        }
                    }

                        /* ReceivedReply */
                    return StateChanges.ReceivedReply;
                }
                else if (frame_type == BacnetMstpFrameTypes.FRAME_TYPE_REPLY_POSTPONED)
                {
                        /* ReceivedPostpone */
                    return StateChanges.ReceivedPostpone;
                }
                else
                {
                        /* ReceivedUnexpectedFrame */
                    return StateChanges.ReceivedUnexpectedFrame;
                }
            }
            finally
            {
                RemoveCurrentMessage(msg_length);
            }
        }
        else if (status == GetMessageStatus.Timeout)
        {
                /* ReplyTimeout */
            m_frame_count = m_max_info_frames;
            return StateChanges.ReplyTimeOut;
        }
        else
        {
                /* InvalidFrame */
            return StateChanges.InvalidFrame;
        }
    }

    private StateChanges UseToken()
    {
        if (m_send_queue.Count == 0)
        {
                /* NothingToSend */
            m_frame_count = m_max_info_frames;
            return StateChanges.NothingToSend;
        }
        else
        {
                /* SendNoWait / SendAndWait */
            MessageFrame message_frame;
            lock (m_send_queue)
            {
                message_frame = m_send_queue.First.Value;
                m_send_queue.RemoveFirst();
            }
            SendFrame(message_frame);
            m_frame_count++;
            if (message_frame.frame_type == BacnetMstpFrameTypes.FRAME_TYPE_BACNET_DATA_EXPECTING_REPLY || message_frame.frame_type == BacnetMstpFrameTypes.FRAME_TYPE_TEST_REQUEST)
                return StateChanges.SendAndWait;
            else
                return StateChanges.SendNoWait;
        }
    }

    private StateChanges PassToken()
    {
        BacnetMstpFrameTypes frame_type;
        byte destination_address;
        byte source_address;
        int msg_length;

        for (int i = 0; i <= m_retry_token; i++)
        {
            //send
            SendFrame(BacnetMstpFrameTypes.FRAME_TYPE_TOKEN, m_NS);

            //wait for it to be used
            GetMessageStatus status = GetNextMessage(T_USAGE_TIMEOUT, out frame_type, out destination_address, out source_address, out msg_length);
            if (status == GetMessageStatus.Good || status == GetMessageStatus.DecodeError)
                return StateChanges.SawTokenUser;   //don't remove current message
        }

        //give up
        m_PS = (byte)((m_NS + 1) % (m_max_master + 1));
        m_NS = (byte)m_TS;
        m_token_count = 0;
        return StateChanges.FindNewSuccessor;
    }

    private StateChanges Idle()
    {
        int no_token_timeout = T_NO_TOKEN + 10 * m_TS;
        BacnetMstpFrameTypes frame_type;
        byte destination_address;
        byte source_address;
        int msg_length;

        while (m_port != null)
        {
            //get message
            GetMessageStatus status = GetNextMessage(no_token_timeout, out frame_type, out destination_address, out source_address, out msg_length);

            if (status == GetMessageStatus.Good)
            {
                try
                {
                    if (destination_address == m_TS || destination_address == 0xFF)
                    {
                        switch (frame_type)
                        {
                            case BacnetMstpFrameTypes.FRAME_TYPE_POLL_FOR_MASTER:
                                if (destination_address == 0xFF)
                                    QueueFrame(BacnetMstpFrameTypes.FRAME_TYPE_REPLY_TO_POLL_FOR_MASTER, source_address);
                                else
                                {
                                    //respond to PFM
                                    SendFrame(BacnetMstpFrameTypes.FRAME_TYPE_REPLY_TO_POLL_FOR_MASTER, source_address);
                                }
                                break;
                            case BacnetMstpFrameTypes.FRAME_TYPE_TOKEN:
                                if (destination_address != 0xFF)
                                {
                                    m_frame_count = 0;
                                    m_sole_master = false;
                                    return StateChanges.ReceivedToken;
                                }
                                break;
                            case BacnetMstpFrameTypes.FRAME_TYPE_TEST_REQUEST:
                                if (destination_address == 0xFF)
                                    QueueFrame(BacnetMstpFrameTypes.FRAME_TYPE_TEST_RESPONSE, source_address);
                                else
                                {
                                    //respond to test
                                    SendFrame(BacnetMstpFrameTypes.FRAME_TYPE_TEST_RESPONSE, source_address);
                                }
                                break;
                            case BacnetMstpFrameTypes.FRAME_TYPE_BACNET_DATA_NOT_EXPECTING_REPLY:
                            case BacnetMstpFrameTypes.FRAME_TYPE_BACNET_DATA_EXPECTING_REPLY:
                                //signal upper layer
                                if (MessageRecieved != null)
                                {
                                    BacnetAddress remote_address = new BacnetAddress(BacnetAddressTypes.MSTP, 0, new byte[] { source_address });
                                    try
                                    {
                                        MessageRecieved(this, m_local_buffer, MSTP.MSTP_HEADER_LENGTH, msg_length, remote_address);
                                    }
                                    catch (Exception ex)
                                    {
                                        Trace.TraceError("Exception in MessageRecieved event: " + ex.Message);
                                    }
                                }
                                if (frame_type == BacnetMstpFrameTypes.FRAME_TYPE_BACNET_DATA_EXPECTING_REPLY)
                                {
                                    m_reply_source = source_address;
                                    m_reply = null;
                                    m_reply_mutex.Reset();
                                    return StateChanges.ReceivedDataNeedingReply;
                                }
                                break;
                        }
                    }
                }
                finally
                {
                    RemoveCurrentMessage(msg_length);
                }
            }
            else if (status == GetMessageStatus.Timeout)
            {
                    /* GenerateToken */
                m_PS = (byte)((m_TS + 1) % (m_max_master + 1));
                m_NS = (byte)m_TS;
                m_token_count = 0;
                return StateChanges.GenerateToken;
            }
            else if (status == GetMessageStatus.ConnectionClose)
            {
                Trace.WriteLine("No connection", null);
            }
            else if (status == GetMessageStatus.ConnectionError)
            {
                Trace.WriteLine("Connection Error", null);
            }
            else
            {
                Trace.WriteLine("Garbage", null);
            }
        }

        return StateChanges.Reset;
    }

    private StateChanges AnswerDataRequest()
    {
        if (m_reply_mutex.WaitOne(T_REPLY_DELAY))
        {
            SendFrame(m_reply);
            lock (m_send_queue)
            m_send_queue.Remove(m_reply);
            return StateChanges.Reply;
        }
        else
        {
            SendFrame(BacnetMstpFrameTypes.FRAME_TYPE_REPLY_POSTPONED, m_reply_source);
            return StateChanges.DeferredReply;
        }
    }

    private StateChanges Initialize()
    {
        m_token_count = m_max_poll;     /* cause a Poll For Master to be sent when this node first receives the token */
        m_frame_count = 0;
        m_sole_master = false;
        m_NS = (byte)m_TS;
        m_PS = (byte)m_TS;
        return StateChanges.DoneInitializing;
    }

    private void mstp_thread()
    {
        try
        {
            StateChanges state_change = StateChanges.Reset;

            while (m_port != null)
            {
                if (StateLogging) Trace.WriteLine(state_change.ToString(), null);
                switch (state_change)
                {
                    case StateChanges.Reset:
                        state_change = Initialize();
                        break;
                    case StateChanges.DoneInitializing:
                    case StateChanges.ReceivedUnexpectedFrame:
                    case StateChanges.Reply:
                    case StateChanges.DeferredReply:
                    case StateChanges.SawTokenUser:
                        state_change = Idle();
                        break;
                    case StateChanges.GenerateToken:
                    case StateChanges.FindNewSuccessor:
                    case StateChanges.SendMaintenancePFM:
                    case StateChanges.SoleMasterRestartMaintenancePFM:
                    case StateChanges.NextStationUnknown:
                        state_change = PollForMaster();
                        break;
                    case StateChanges.DoneWithPFM:
                    case StateChanges.ResetMaintenancePFM:
                    case StateChanges.ReceivedReplyToPFM:
                    case StateChanges.SendToken:
                        state_change = PassToken();
                        break;
                    case StateChanges.ReceivedDataNeedingReply:
                        state_change = AnswerDataRequest();
                        break;
                    case StateChanges.ReceivedToken:
                    case StateChanges.SoleMaster:
                    case StateChanges.DeclareSoleMaster:
                    case StateChanges.SendAnotherFrame:
                        state_change = UseToken();
                        break;
                    case StateChanges.NothingToSend:
                    case StateChanges.SendNoWait:
                    case StateChanges.ReplyTimeOut:
                    case StateChanges.InvalidFrame:
                    case StateChanges.ReceivedReply:
                    case StateChanges.ReceivedPostpone:
                        state_change = DoneWithToken();
                        break;
                    case StateChanges.SendAndWait:
                        state_change = WaitForReply();
                        break;
                }
            }
            Trace.WriteLine("MSTP thread is closing down", null);
        }
        catch (Exception ex)
        {
            Trace.TraceError("Exception in MSTP thread: " + ex.Message);
        }

        m_is_running = false;
    }

    private void RemoveGarbage()
    {
        //scan for preambles
        for (int i = 0; i < (m_local_offset - 1); i++)
        {
            if (m_local_buffer[i] == MSTP.MSTP_PREAMBLE1 && m_local_buffer[i + 1] == MSTP.MSTP_PREAMBLE2)
            {
                if (i > 0)
                {
                    //move back
                    Array.Copy(m_local_buffer, i, m_local_buffer, 0, m_local_offset - i);
                    m_local_offset -= i;
                    Trace.WriteLine("Garbage", null);
                }
                return;
            }
        }

        //one preamble?
        if (m_local_offset > 0 && m_local_buffer[m_local_offset - 1] == MSTP.MSTP_PREAMBLE1)
        {
            m_local_buffer[0] = MSTP.MSTP_PREAMBLE1;
            m_local_offset = 1;
            Trace.WriteLine("Garbage", null);
            return;
        }

        //no preamble?
        if (m_local_offset > 0)
        {
            m_local_offset = 0;
            Trace.WriteLine("Garbage", null);
        }
    }

public enum GetMessageStatus
{
    Good,
    Timeout,
    SubTimeout,
    ConnectionClose,
    ConnectionError,
    DecodeError,
}

    private GetMessageStatus GetNextMessage(int timeout_ms, out BacnetMstpFrameTypes frame_type, out byte destination_address, out byte source_address, out int msg_length)
    {
        int timeout;

        frame_type = BacnetMstpFrameTypes.FRAME_TYPE_TOKEN;
        destination_address = 0;
        source_address = 0;
        msg_length = 0;

        //fetch header
        while (m_local_offset < MSTP.MSTP_HEADER_LENGTH)
        {
            if (m_port == null) return GetMessageStatus.ConnectionClose;

            if (m_local_offset > 0)
                timeout = T_FRAME_ABORT;    //set sub timeout
            else
                timeout = timeout_ms;       //set big silence timeout

            //read
            int rx = m_port.Read(m_local_buffer, m_local_offset, MSTP.MSTP_HEADER_LENGTH - m_local_offset, timeout);
            if (rx == -ETIMEDOUT)
            {
                //drop message
                GetMessageStatus status = m_local_offset == 0 ? GetMessageStatus.Timeout : GetMessageStatus.SubTimeout;
                m_local_buffer[0] = 0xFF;
                RemoveGarbage();
                return status;
            }
            else if (rx < 0)
            {
                //drop message
                m_local_buffer[0] = 0xFF;
                RemoveGarbage();
                return GetMessageStatus.ConnectionError;
            }
            else if (rx == 0)
            {
                //drop message
                m_local_buffer[0] = 0xFF;
                RemoveGarbage();
                return GetMessageStatus.ConnectionClose;
            }
            m_local_offset += rx;

            //remove paddings & garbage
            RemoveGarbage();
        }

        //decode
        if (MSTP.Decode(m_local_buffer, 0, m_local_offset, out frame_type, out destination_address, out source_address, out msg_length) < 0)
        {
            //drop message
            m_local_buffer[0] = 0xFF;
            RemoveGarbage();
            return GetMessageStatus.DecodeError;
        }

        //valid length?
        int full_msg_length = msg_length + MSTP.MSTP_HEADER_LENGTH + (msg_length > 0 ? 2 : 0);
        if (msg_length > MaxBufferLength)
        {
            //drop message
            m_local_buffer[0] = 0xFF;
            RemoveGarbage();
            return GetMessageStatus.DecodeError;
        }

        //fetch data
        if (msg_length > 0)
        {
            timeout = T_FRAME_ABORT;    //set sub timeout
            while (m_local_offset < full_msg_length)
            {
                //read
                int rx = m_port.Read(m_local_buffer, m_local_offset, full_msg_length - m_local_offset, timeout);
                if (rx == -ETIMEDOUT)
                {
                    //drop message
                    GetMessageStatus status = m_local_offset == 0 ? GetMessageStatus.Timeout : GetMessageStatus.SubTimeout;
                    m_local_buffer[0] = 0xFF;
                    RemoveGarbage();
                    return status;
                }
                else if (rx < 0)
                {
                    //drop message
                    m_local_buffer[0] = 0xFF;
                    RemoveGarbage();
                    return GetMessageStatus.ConnectionError;
                }
                else if (rx == 0)
                {
                    //drop message
                    m_local_buffer[0] = 0xFF;
                    RemoveGarbage();
                    return GetMessageStatus.ConnectionClose;
                }
                m_local_offset += rx;
            }

            //verify data crc
            if (MSTP.Decode(m_local_buffer, 0, m_local_offset, out frame_type, out destination_address, out source_address, out msg_length) < 0)
            {
                //drop message
                m_local_buffer[0] = 0xFF;
                RemoveGarbage();
                return GetMessageStatus.DecodeError;
            }
        }

        //signal frame event
        if (FrameRecieved != null)
        {
            BacnetMstpFrameTypes _frame_type = frame_type;
            byte _destination_address = destination_address;
            byte _source_address = source_address;
            int _msg_length = msg_length;
            ThreadPool.QueueUserWorkItem((o) => { FrameRecieved(this, _frame_type, _destination_address, _source_address, _msg_length); }, null);
        }

        if (StateLogging) Trace.WriteLine("" + frame_type + " " + destination_address.ToString("X2") + " ");

        //done
        return GetMessageStatus.Good;
    }

    public int Send(byte[] buffer, int offset, int data_length, BacnetAddress address, bool wait_for_transmission, int timeout)
    {
        if (m_TS == -1) throw new Exception("Source address must be set up before sending messages");

        //add to queue
        BacnetNpduControls function = NPDU.DecodeFunction(buffer, offset);
        BacnetMstpFrameTypes frame_type = (function & BacnetNpduControls.ExpectingReply) == BacnetNpduControls.ExpectingReply ? BacnetMstpFrameTypes.FRAME_TYPE_BACNET_DATA_EXPECTING_REPLY : BacnetMstpFrameTypes.FRAME_TYPE_BACNET_DATA_NOT_EXPECTING_REPLY;
        byte[] copy = new byte[data_length + MSTP.MSTP_HEADER_LENGTH + 2];
        Array.Copy(buffer, offset, copy, MSTP.MSTP_HEADER_LENGTH, data_length);
        MessageFrame f = new MessageFrame(frame_type, address.adr[0], copy, data_length);
        lock (m_send_queue)
        m_send_queue.AddLast(f);
        if (m_reply == null)
        {
            m_reply = f;
            m_reply_mutex.Set();
        }

        //wait for message to be sent
        if (wait_for_transmission)
            if (!f.send_mutex.WaitOne(timeout))
                return -ETIMEDOUT;

        return data_length;
    }

    public bool WaitForAllTransmits(int timeout)
    {
        while (m_send_queue.Count > 0)
        {
            ManualResetEvent ev;
            lock (m_send_queue)
            ev = m_send_queue.First.Value.send_mutex;

            if (ev.WaitOne(timeout))
                return false;
        }
        return true;
    }

    public BacnetAddress GetBroadcastAddress()
    {
        return new BacnetAddress(BacnetAddressTypes.MSTP, 0xFFFF, new byte[] { 0xFF });
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
