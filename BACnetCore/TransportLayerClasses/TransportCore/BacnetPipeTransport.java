package BACnetCore.TransportLayerClasses.TransportCore;

/**************************************************************************
*                           MIT License
* 
* Copyright (C) 2014 Morten Kvistgaard <mk@pch-engineering.dk>
* Copyright (C) 2015 Frederic Chaxel <fchaxel@free.fr>
*
* Permission is hereby granted, free of charge, to any person obtaining
* a copy of this software and associated documentation files (the
* "Software"), to deal in the Software without restriction, including
* without limitation the rights to use, copy, modify, merge, publish,
* distribute, sublicense, and/or sell copies of the Software, and to
* permit persons to whom the Software is furnished to do so, subject to
* the following conditions:
*
* The above copyright notice and this permission notice shall be included
* in all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
* CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
* TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
* SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
*********************************************************************/
public class BacnetPipeTransport : IBacnetSerialTransport
        {
private string m_name;
private PipeStream m_conn;
private IAsyncResult m_current_read;
private IAsyncResult m_current_connect;
private bool m_is_server;

public string Name { get { return m_name; } }

public BacnetPipeTransport(string name, bool is_server = false)
        {
        m_name = name;
        m_is_server = is_server;
        }

        /// <summary>
        /// Get the available byte count. (The .NET pipe interface has a few lackings. See also the "InteropAvailablePorts" function)
        /// </summary>
        [System.Runtime.InteropServices.DllImport("kernel32.dll", EntryPoint = "PeekNamedPipe", SetLastError = true)]
private static extern bool PeekNamedPipe(IntPtr handle, IntPtr buffer, uint nBufferSize, IntPtr bytesRead, ref uint bytesAvail, IntPtr BytesLeftThisMessage);

public int PeekPipe()
        {
        uint bytes_avail = 0;
        if (PeekNamedPipe(m_conn.SafePipeHandle.DangerousGetHandle(), IntPtr.Zero, 0, IntPtr.Zero, ref bytes_avail, IntPtr.Zero))
        return (int)bytes_avail;
        else
        return 0;
        }

public override string ToString()
        {
        return m_name.ToString();
        }

public override int GetHashCode()
        {
        return m_name.GetHashCode();
        }

public override bool Equals(object obj)
        {
        if (obj == null) return false;
        else if (!(obj is BacnetPipeTransport)) return false;
        BacnetPipeTransport a = (BacnetPipeTransport)obj;
        return m_name.Equals(a.m_name);
        }

public void Open()
        {
        if (m_conn != null) Close();

        if (!m_is_server)
        {
        m_conn = new NamedPipeClientStream(".", m_name, PipeDirection.InOut, PipeOptions.Asynchronous);
        ((NamedPipeClientStream)m_conn).Connect(3000);
        }
        else
        {
        m_conn = new NamedPipeServerStream(m_name, PipeDirection.InOut, 20, PipeTransmissionMode.Byte, PipeOptions.Asynchronous);
        }
        }

public void Write(byte[] buffer, int offset, int length)
        {
        if (!m_conn.IsConnected) return;
        try
        {
        //doing syncronous writes (to an Asynchronous pipe) seems to be a bad thing
        m_conn.BeginWrite(buffer, offset, length, (r) => { m_conn.EndWrite(r); }, null);
        }
        catch (System.IO.IOException)
        {
        Disconnect();
        }
        }

private void Disconnect()
        {
        if (m_conn is NamedPipeServerStream)
        {
        try
        {
        ((NamedPipeServerStream)m_conn).Disconnect();
        }
        catch
        {
        }
        m_current_connect = null;
        }
        m_current_read = null;
        }

private bool WaitForConnection(int timeout_ms)
        {
        if (m_conn.IsConnected) return true;
        if (m_conn is NamedPipeServerStream)
        {
        NamedPipeServerStream server = (NamedPipeServerStream)m_conn;
        if (m_current_connect == null)
        {
        try
        {
        m_current_connect = server.BeginWaitForConnection(null, null);
        }
        catch (System.IO.IOException)
        {
        Disconnect();
        m_current_connect = server.BeginWaitForConnection(null, null);
        }
        }

        if (m_current_connect.IsCompleted || m_current_connect.AsyncWaitHandle.WaitOne(timeout_ms))
        {
        try
        {
        server.EndWaitForConnection(m_current_connect);
        }
        catch (System.IO.IOException)
        {
        Disconnect();
        }
        m_current_connect = null;
        }
        return m_conn.IsConnected;
        }
        else
        return true;
        }

public int Read(byte[] buffer, int offset, int length, int timeout_ms)
        {
        if (!WaitForConnection(timeout_ms)) return -BacnetMstpProtocolTransport.ETIMEDOUT;

        if (m_current_read == null)
        {
        try
        {
        m_current_read = m_conn.BeginRead(buffer, offset, length, null, null);
        }
        catch (Exception)
        {
        Disconnect();
        return -1;
        }
        }

        if (m_current_read.IsCompleted || m_current_read.AsyncWaitHandle.WaitOne(timeout_ms))
        {
        try
        {
        int rx = m_conn.EndRead(m_current_read);
        m_current_read = null;
        return rx;
        }
        catch (Exception)
        {
        Disconnect();
        return -1;
        }
        }
        else
        return -BacnetMstpProtocolTransport.ETIMEDOUT;
        }

public void Close()
        {
        if (m_conn == null) return;
        m_conn.Close();
        m_conn = null;
        }

public int BytesToRead
        {
        get
        {
        return PeekPipe();
        }
        }

public static string[] AvailablePorts
        {
        get
        {
        try
        {
        String[] listOfPipes = System.IO.Directory.GetFiles(@"\\.\pipe\");
        if (listOfPipes == null) return new string[0];
        else
        {
        for (int i = 0; i < listOfPipes.Length; i++)
        listOfPipes[i] = listOfPipes[i].Replace(@"\\.\pipe\", "");
        return listOfPipes;
        }
        }
        catch (Exception ex)
        {
        Trace.TraceWarning("Exception in AvailablePorts: " + ex.Message);
        return InteropAvailablePorts;
        }
        }
        }

        #region " Interop Get Pipe Names "

        [System.Runtime.InteropServices.StructLayout(System.Runtime.InteropServices.LayoutKind.Sequential)]
private struct FILETIME
        {
public uint dwLowDateTime;
public uint dwHighDateTime;
        }

        [System.Runtime.InteropServices.StructLayout(System.Runtime.InteropServices.LayoutKind.Sequential, CharSet = System.Runtime.InteropServices.CharSet.Unicode)]
private struct WIN32_FIND_DATA
        {
public uint dwFileAttributes;
public FILETIME ftCreationTime;
public FILETIME ftLastAccessTime;
public FILETIME ftLastWriteTime;
public uint nFileSizeHigh;
public uint nFileSizeLow;
public uint dwReserved0;
public uint dwReserved1;
        [System.Runtime.InteropServices.MarshalAs(System.Runtime.InteropServices.UnmanagedType.ByValTStr, SizeConst = 260)]
public string cFileName;
        [System.Runtime.InteropServices.MarshalAs(System.Runtime.InteropServices.UnmanagedType.ByValTStr, SizeConst = 14)]
public string cAlternateFileName;
        }

        [System.Runtime.InteropServices.DllImport("kernel32.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode)]
private static extern IntPtr FindFirstFile(string lpFileName, out WIN32_FIND_DATA lpFindFileData);
        [System.Runtime.InteropServices.DllImport("kernel32.dll", CharSet = System.Runtime.InteropServices.CharSet.Unicode)]
private static extern int FindNextFile(IntPtr hFindFile, out WIN32_FIND_DATA lpFindFileData);
        [System.Runtime.InteropServices.DllImport("kernel32.dll")]
private static extern bool FindClose(IntPtr hFindFile);

/// <summary>
/// The built-in functions for pipe enumeration isn't perfect, I'm afraid. Hence this messy interop.
/// </summary>
static string[] InteropAvailablePorts
        {
        get
        {
        List<string> ret = new List<string>();
        WIN32_FIND_DATA data;
        IntPtr handle = FindFirstFile(@"\\.\pipe\*", out data);
        if (handle != new IntPtr(-1))
        {
        do
        ret.Add(data.cFileName);
        while (FindNextFile(handle, out data) != 0);
        FindClose(handle);
        }
        return ret.ToArray();
        }
        }
        #endregion

public void Dispose()
        {
        Close();
        }
        }
