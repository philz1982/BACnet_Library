package BACnetCore.CoreClasses;

/**
 * Created by Phil on 3/26/2016.
 */
public class PTP

{
    public const byte PTP_PREAMBLE1 = 0x55;
    public const byte PTP_PREAMBLE2 = 0xFF;
    public const byte PTP_GREETING_PREAMBLE1 = 0x42;
    public const byte PTP_GREETING_PREAMBLE2 = 0x41;
    public const BacnetMaxAdpu PTP_MAX_APDU = BacnetMaxAdpu.MAX_APDU480;
    public const byte PTP_HEADER_LENGTH = 6;

    public static int Encode(byte[] buffer, int offset, BacnetPtpFrameTypes frame_type, int msg_length)
    {
        buffer[offset + 0] = PTP_PREAMBLE1;
        buffer[offset + 1] = PTP_PREAMBLE2;
        buffer[offset + 2] = (byte)frame_type;
        buffer[offset + 3] = (byte)((msg_length & 0xFF00) >> 8);
        buffer[offset + 4] = (byte)((msg_length & 0x00FF) >> 0);
        buffer[offset + 5] = MSTP.CRC_Calc_Header(buffer, offset + 2, 3);
        if (msg_length > 0)
        {
            //calculate data crc
            ushort data_crc = MSTP.CRC_Calc_Data(buffer, offset + 6, msg_length);
            buffer[offset + 6 + msg_length + 0] = (byte)(data_crc & 0xFF);  //LSB first!
            buffer[offset + 6 + msg_length + 1] = (byte)(data_crc >> 8);
        }
        return PTP_HEADER_LENGTH + (msg_length) + (msg_length > 0 ? 2 : 0);
    }

    public static int Decode(byte[] buffer, int offset, int max_length, out BacnetPtpFrameTypes frame_type, out int msg_length)
    {
        frame_type = (BacnetPtpFrameTypes)buffer[offset + 2];
        msg_length = (buffer[offset + 3] << 8) | (buffer[offset + 4] << 0);
        byte crc_header = buffer[offset + 5];
        ushort crc_data = 0;
        if (max_length < PTP_HEADER_LENGTH) return -1;     //not enough data
        if (msg_length > 0) crc_data = (ushort)((buffer[offset + 6 + msg_length + 1] << 8) | (buffer[offset + 6 + msg_length + 0] << 0));
        if (buffer[offset + 0] != PTP_PREAMBLE1) return -1;
        if (buffer[offset + 1] != PTP_PREAMBLE2) return -1;
        if (MSTP.CRC_Calc_Header(buffer, offset + 2, 3) != crc_header) return -1;
        if (msg_length > 0 && max_length >= (PTP_HEADER_LENGTH + msg_length + 2) && MSTP.CRC_Calc_Data(buffer, offset + 6, msg_length) != crc_data) return -1;
        return 8 + (msg_length) + (msg_length > 0 ? 2 : 0);
    }

}