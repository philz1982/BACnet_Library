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

public class NPDU

{
    public const byte BACNET_PROTOCOL_VERSION = 1;

    public static BacnetNpduControls DecodeFunction(byte[] buffer, int offset)
    {
        if (buffer[offset + 0] != BACNET_PROTOCOL_VERSION) return 0;
        return (BacnetNpduControls)buffer[offset + 1];
    }

    public static int Decode(byte[] buffer, int offset, out BacnetNpduControls function, out BacnetAddress destination, out BacnetAddress source, out byte hop_count, out BacnetNetworkMessageTypes network_msg_type, out ushort vendor_id)
    {
        int org_offset = offset;

        offset++;
        function = (BacnetNpduControls)buffer[offset++];

        destination = null;
        if ((function & BacnetNpduControls.DestinationSpecified) == BacnetNpduControls.DestinationSpecified)
        {
            destination = new BacnetAddress(BacnetAddressTypes.None, (ushort)((buffer[offset++] << 8) | (buffer[offset++] << 0)), null);
            int adr_len = buffer[offset++];
            if (adr_len > 0)
            {
                destination.adr = new byte[adr_len];
                for (int i = 0; i < destination.adr.Length; i++)
                    destination.adr[i] = buffer[offset++];
            }
        }

        source = null;
        if ((function & BacnetNpduControls.SourceSpecified) == BacnetNpduControls.SourceSpecified)
        {
            source = new BacnetAddress(BacnetAddressTypes.None, (ushort)((buffer[offset++] << 8) | (buffer[offset++] << 0)), null);
            int adr_len = buffer[offset++];
            if (adr_len > 0)
            {
                source.adr = new byte[adr_len];
                for (int i = 0; i < source.adr.Length; i++)
                    source.adr[i] = buffer[offset++];
            }
        }

        hop_count = 0;
        if ((function & BacnetNpduControls.DestinationSpecified) == BacnetNpduControls.DestinationSpecified)
        {
            hop_count = buffer[offset++];
        }

        network_msg_type = BacnetNetworkMessageTypes.NETWORK_MESSAGE_WHO_IS_ROUTER_TO_NETWORK;
        vendor_id = 0;
        if ((function & BacnetNpduControls.NetworkLayerMessage) == BacnetNpduControls.NetworkLayerMessage)
        {
            network_msg_type = (BacnetNetworkMessageTypes)buffer[offset++];
            if (((byte)network_msg_type) >= 0x80)
            {
                vendor_id = (ushort)((buffer[offset++] << 8) | (buffer[offset++] << 0));
            }
            else if (network_msg_type == BacnetNetworkMessageTypes.NETWORK_MESSAGE_WHO_IS_ROUTER_TO_NETWORK)
                offset += 2;  // Don't care about destination network adress
        }

        if (buffer[org_offset + 0] != BACNET_PROTOCOL_VERSION) return -1;
        return offset - org_offset;
    }

    public static void Encode(EncodeBuffer buffer, BacnetNpduControls function, BacnetAddress destination, BacnetAddress source, byte hop_count, BacnetNetworkMessageTypes network_msg_type, ushort vendor_id)
    {
        // Modif FC
        bool has_destination = destination != null && destination.net > 0; // && destination.net != 0xFFFF;
        bool has_source = source != null && source.net > 0 && source.net != 0xFFFF;

        buffer.buffer[buffer.offset++] = BACNET_PROTOCOL_VERSION;
        buffer.buffer[buffer.offset++] = (byte)(function | (has_destination ? BacnetNpduControls.DestinationSpecified : 0) | (has_source ? BacnetNpduControls.SourceSpecified : 0));

        if (has_destination)
        {
            buffer.buffer[buffer.offset++] = (byte)((destination.net & 0xFF00) >> 8);
            buffer.buffer[buffer.offset++] = (byte)((destination.net & 0x00FF) >> 0);

            if (destination.net == 0xFFFF)                  //patch by F. Chaxel
                buffer.buffer[buffer.offset++] = 0;
            else
            {
                buffer.buffer[buffer.offset++] = (byte)destination.adr.Length;
                if (destination.adr.Length > 0)
                {
                    for (int i = 0; i < destination.adr.Length; i++)
                        buffer.buffer[buffer.offset++] = destination.adr[i];
                }
            }
        }

        if (has_source)
        {
            buffer.buffer[buffer.offset++] = (byte)((source.net & 0xFF00) >> 8);
            buffer.buffer[buffer.offset++] = (byte)((source.net & 0x00FF) >> 0);
            // Modif FC
            if (destination.net == 0xFFFF)
                buffer.buffer[buffer.offset++] = 0;
            else
            {
                buffer.buffer[buffer.offset++] = (byte)destination.adr.Length;
                if (destination.adr.Length > 0)
                {
                    for (int i = 0; i < destination.adr.Length; i++)
                        buffer.buffer[buffer.offset++] = destination.adr[i];
                }
            }
        }

        if (has_destination)
        {
            buffer.buffer[buffer.offset++] = hop_count;
        }

            /*
            //display warning
            if (has_destination || has_source)
            {
                System.Diagnostics.Trace.TraceWarning("NPDU size is more than 4. This will give an error in the current max_apdu calculation");
            }
            */

        if ((function & BacnetNpduControls.NetworkLayerMessage) > 0)
        {
            buffer.buffer[buffer.offset++] = (byte)network_msg_type;
            if (((byte)network_msg_type) >= 0x80)
            {
                buffer.buffer[buffer.offset++] = (byte)((vendor_id & 0xFF00) >> 8);
                buffer.buffer[buffer.offset++] = (byte)((vendor_id & 0x00FF) >> 0);
            }
        }
    }
}
