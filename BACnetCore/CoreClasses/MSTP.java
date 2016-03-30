package BACnetCore.CoreClasses;

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
public class MSTP

{
    public const byte MSTP_PREAMBLE1 = 0x55;
    public const byte MSTP_PREAMBLE2 = 0xFF;
    public const BacnetMaxAdpu MSTP_MAX_APDU = BacnetMaxAdpu.MAX_APDU480;
    public const byte MSTP_HEADER_LENGTH = 8;

    public static byte CRC_Calc_Header(byte dataValue, byte crcValue)
    {
        ushort crc;

        crc = (ushort)(crcValue ^ dataValue); /* XOR C7..C0 with D7..D0 */

            /* Exclusive OR the terms in the table (top down) */
        crc = (ushort)(crc ^ (crc << 1) ^ (crc << 2) ^ (crc << 3) ^ (crc << 4) ^ (crc << 5) ^ (crc << 6) ^ (crc << 7));

            /* Combine bits shifted out left hand end */
        return (byte)((crc & 0xfe) ^ ((crc >> 8) & 1));
    }

    public static byte CRC_Calc_Header(byte[] buffer, int offset, int length)
    {
        byte crc = 0xff;
        for (int i = offset; i < (offset + length); i++)
            crc = CRC_Calc_Header(buffer[i], crc);
        return (byte)~crc;
    }

    public static ushort CRC_Calc_Data(byte dataValue, ushort crcValue)
    {
        ushort crcLow;

        crcLow = (ushort)((crcValue & 0xff) ^ dataValue);     /* XOR C7..C0 with D7..D0 */

            /* Exclusive OR the terms in the table (top down) */
        return (ushort)((crcValue >> 8) ^ (crcLow << 8) ^ (crcLow << 3)
                ^ (crcLow << 12) ^ (crcLow >> 4)
                ^ (crcLow & 0x0f) ^ ((crcLow & 0x0f) << 7));
    }

    public static ushort CRC_Calc_Data(byte[] buffer, int offset, int length)
    {
        ushort crc = 0xffff;
        for (int i = offset; i < (offset + length); i++)
            crc = CRC_Calc_Data(buffer[i], crc);
        return (ushort)~crc;
    }

    public static int Encode(byte[] buffer, int offset, BacnetMstpFrameTypes frame_type, byte destination_address, byte source_address, int msg_length)
    {
        buffer[offset + 0] = MSTP_PREAMBLE1;
        buffer[offset + 1] = MSTP_PREAMBLE2;
        buffer[offset + 2] = (byte)frame_type;
        buffer[offset + 3] = destination_address;
        buffer[offset + 4] = source_address;
        buffer[offset + 5] = (byte)((msg_length & 0xFF00) >> 8);
        buffer[offset + 6] = (byte)((msg_length & 0x00FF) >> 0);
        buffer[offset + 7] = CRC_Calc_Header(buffer, offset + 2, 5);
        if (msg_length > 0)
        {
            //calculate data crc
            ushort data_crc = CRC_Calc_Data(buffer, offset + 8, msg_length);
            buffer[offset + 8 + msg_length + 0] = (byte)(data_crc & 0xFF);  //LSB first!
            buffer[offset + 8 + msg_length + 1] = (byte)(data_crc >> 8);
        }
        //optional pad (0xFF)
        return MSTP_HEADER_LENGTH + (msg_length) + (msg_length > 0 ? 2 : 0);
    }

    public static int Decode(byte[] buffer, int offset, int max_length, out BacnetMstpFrameTypes frame_type, out byte destination_address, out byte source_address, out int msg_length)
    {
        frame_type = (BacnetMstpFrameTypes)buffer[offset + 2];
        destination_address = buffer[offset + 3];
        source_address = buffer[offset + 4];
        msg_length = (buffer[offset + 5] << 8) | (buffer[offset + 6] << 0);
        byte crc_header = buffer[offset + 7];
        ushort crc_data = 0;
        if (max_length < MSTP_HEADER_LENGTH) return -1;     //not enough data
        if (msg_length > 0) crc_data = (ushort)((buffer[offset + 8 + msg_length + 1] << 8) | (buffer[offset + 8 + msg_length + 0] << 0));
        if (buffer[offset + 0] != MSTP_PREAMBLE1) return -1;
        if (buffer[offset + 1] != MSTP_PREAMBLE2) return -1;
        if (CRC_Calc_Header(buffer, offset + 2, 5) != crc_header) return -1;
        if (msg_length > 0 && max_length >= (MSTP_HEADER_LENGTH + msg_length + 2) && CRC_Calc_Data(buffer, offset + 8, msg_length) != crc_data) return -1;
        return 8 + (msg_length) + (msg_length > 0 ? 2 : 0);
    }
}
