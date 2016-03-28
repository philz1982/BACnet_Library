package BACnetCore.Interfaces;

/**
 * Created by Phil on 3/27/2016.
 */
public interface IBacnetTransport : IDisposable
        {
        event MessageRecievedHandler MessageRecieved;
        int Send(byte[] buffer, int offset, int data_length, BacnetAddress address, bool wait_for_transmission, int timeout);
        BacnetAddress GetBroadcastAddress();
        BacnetAddressTypes Type { get; }
        void Start();

        int HeaderLength { get; }
        int MaxBufferLength { get; }
        BacnetMaxAdpu MaxAdpuLength { get; }

        bool WaitForAllTransmits(int timeout);
        byte MaxInfoFrames { get; set; }
        }
