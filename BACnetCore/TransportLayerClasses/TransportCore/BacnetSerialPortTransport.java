package BACnetCore.TransportLayerClasses.TransportCore;

/**
 * Created by Phil on 3/27/2016.
 */
public class BacnetSerialPortTransport : IBacnetSerialTransport
        {
private string m_port_name;
private int m_baud_rate;
private SerialPort m_port;

public BacnetSerialPortTransport(string port_name, int baud_rate)
        {
        m_port_name = port_name;
        m_baud_rate = baud_rate;
        m_port = new SerialPort(m_port_name, m_baud_rate, Parity.None, 8, StopBits.One);
        }

public override bool Equals(object obj)
        {
        if (obj == null) return false;
        else if (!(obj is BacnetSerialPortTransport)) return false;
        BacnetSerialPortTransport a = (BacnetSerialPortTransport)obj;
        return m_port_name.Equals(a.m_port_name);
        }

public override int GetHashCode()
        {
        return m_port_name.GetHashCode();
        }

public override string ToString()
        {
        return m_port_name.ToString();
        }

public void Open()
        {
        m_port.Open();
        }

public void Write(byte[] buffer, int offset, int length)
        {
        if (m_port == null) return;
        m_port.Write(buffer, offset, length);
        }

public int Read(byte[] buffer, int offset, int length, int timeout_ms)
        {
        if (m_port == null) return 0;
        m_port.ReadTimeout = timeout_ms;
        try
        {
        int rx = m_port.Read(buffer, offset, length);
        return rx;
        }
        catch (TimeoutException)
        {
        return -BacnetMstpProtocolTransport.ETIMEDOUT;
        }
        catch (Exception)
        {
        return -1;
        }
        }

public void Close()
        {
        if (m_port == null) return;
        m_port.Close();
        }

public int BytesToRead
        {
        get { return m_port == null ? 0 : m_port.BytesToRead; }
        }

public void Dispose()
        {
        Close();
        }
        }
