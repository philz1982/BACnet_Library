package BACnetCore.Interfaces;

/**
 * Created by Phil on 3/27/2016.
 */
public interface IBacnetSerialTransport: IDisposable
        {
        void Open();
        void Write(byte[] buffer, int offset, int length);
        int Read(byte[] buffer, int offset, int length, int timeout_ms);
        void Close();
        int BytesToRead { get; }
        }
