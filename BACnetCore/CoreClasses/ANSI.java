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
public class ASN1

{
    public const int BACNET_MAX_OBJECT = 0x3FF;
    public const int BACNET_INSTANCE_BITS = 22;
    public const int BACNET_MAX_INSTANCE = 0x3FFFFF;
    public const int MAX_BITSTRING_BYTES = 15;
    public const uint BACNET_ARRAY_ALL = 0xFFFFFFFFU;
    public const uint BACNET_NO_PRIORITY = 0;
    public const uint BACNET_MIN_PRIORITY = 1;
    public const uint BACNET_MAX_PRIORITY = 16;



    public static void encode_bacnet_object_id(EncodeBuffer buffer, BacnetObjectTypes object_type, UInt32 instance)
    {
        UInt32 value = 0;
        UInt32 type = 0;

        type = (UInt32)object_type;
        value = ((type & BACNET_MAX_OBJECT) << BACNET_INSTANCE_BITS) | (instance & BACNET_MAX_INSTANCE);
        encode_unsigned32(buffer, value);
    }

    public static void encode_tag(EncodeBuffer buffer, byte tag_number, bool context_specific, UInt32 len_value_type)
    {
        int len = 1;
        byte[] tmp = new byte[3];

        tmp[0] = 0;
        if (context_specific) tmp[0] |= 0x8;

            /* additional tag byte after this byte */
            /* for extended tag byte */
        if (tag_number <= 14)
        {
            tmp[0] |= (byte)(tag_number << 4);
        }
        else
        {
            tmp[0] |= 0xF0;
            tmp[1] = tag_number;
            len++;
        }

            /* NOTE: additional len byte(s) after extended tag byte */
            /* if larger than 4 */
        if (len_value_type <= 4)
        {
            tmp[0] |= (byte)len_value_type;
            buffer.Add(tmp, len);
        }
        else
        {
            tmp[0] |= 5;
            if (len_value_type <= 253)
            {
                tmp[len++] = (byte)len_value_type;
                buffer.Add(tmp, len);
            }
            else if (len_value_type <= 65535)
            {
                tmp[len++] = 254;
                buffer.Add(tmp, len);
                encode_unsigned16(buffer, (UInt16)len_value_type);
            }
            else
            {
                tmp[len++] = 255;
                buffer.Add(tmp, len);
                encode_unsigned32(buffer, len_value_type);
            }
        }
    }

    public static void encode_bacnet_enumerated(EncodeBuffer buffer, UInt32 value)
    {
        encode_bacnet_unsigned(buffer, value);
    }

    public static void encode_application_object_id(EncodeBuffer buffer, BacnetObjectTypes object_type, UInt32 instance)
    {
        EncodeBuffer tmp1 = new EncodeBuffer();
        encode_bacnet_object_id(tmp1, object_type, instance);
        encode_tag(buffer, (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_OBJECT_ID, false, (uint)tmp1.offset);
        buffer.Add(tmp1.buffer, tmp1.offset);
    }

    public static void encode_application_unsigned(EncodeBuffer buffer, UInt32 value)
    {
        EncodeBuffer tmp1 = new EncodeBuffer();
        encode_bacnet_unsigned(tmp1, value);
        encode_tag(buffer, (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_UNSIGNED_INT, false, (uint)tmp1.offset);
        buffer.Add(tmp1.buffer, tmp1.offset);
    }

    public static void encode_application_enumerated(EncodeBuffer buffer, UInt32 value)
    {
        EncodeBuffer tmp1 = new EncodeBuffer();
        encode_bacnet_enumerated(tmp1, value);
        encode_tag(buffer, (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_ENUMERATED, false, (uint)tmp1.offset);
        buffer.Add(tmp1.buffer, tmp1.offset);
    }

    public static void encode_application_signed(EncodeBuffer buffer, Int32 value)
    {
        EncodeBuffer tmp1 = new EncodeBuffer();
        encode_bacnet_signed(tmp1, value);
        encode_tag(buffer, (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_SIGNED_INT, false, (uint)tmp1.offset);
        buffer.Add(tmp1.buffer, tmp1.offset);
    }

    public static void encode_bacnet_unsigned(EncodeBuffer buffer, UInt32 value)
    {
        if (value < 0x100)
        {
            buffer.Add((byte)value);
        }
        else if (value < 0x10000)
        {
            encode_unsigned16(buffer, (UInt16)value);
        }
        else if (value < 0x1000000)
        {
            encode_unsigned24(buffer, value);
        }
        else
        {
            encode_unsigned32(buffer, value);
        }
    }

    public static void encode_context_boolean(EncodeBuffer buffer, byte tag_number, bool boolean_value)
    {
        encode_tag(buffer, (byte)tag_number, true, 1);
        buffer.Add((boolean_value ? (byte)1 : (byte)0));
    }

    public static void encode_context_real(EncodeBuffer buffer, byte tag_number, float value)
    {
        encode_tag(buffer, tag_number, true, 4);
        encode_bacnet_real(buffer, value);
    }

    public static void encode_context_unsigned(EncodeBuffer buffer, byte tag_number, UInt32 value)
    {
        int len;

            /* length of unsigned is variable, as per 20.2.4 */
        if (value < 0x100)
            len = 1;
        else if (value < 0x10000)
            len = 2;
        else if (value < 0x1000000)
            len = 3;
        else
            len = 4;

        encode_tag(buffer, tag_number, true, (UInt32)len);
        encode_bacnet_unsigned(buffer, value);
    }

    public static void encode_context_character_string(EncodeBuffer buffer, byte tag_number, string value)
    {

        EncodeBuffer tmp = new EncodeBuffer();
        encode_bacnet_character_string(tmp, value);

        encode_tag(buffer, tag_number, true, (UInt32)tmp.offset);
        buffer.Add(tmp.buffer, tmp.offset);

    }

    public static void encode_context_enumerated(EncodeBuffer buffer, byte tag_number, UInt32 value)
    {
        int len = 0;        /* return value */

        if (value < 0x100)
            len = 1;
        else if (value < 0x10000)
            len = 2;
        else if (value < 0x1000000)
            len = 3;
        else
            len = 4;

        encode_tag(buffer, tag_number, true, (uint)len);
        encode_bacnet_enumerated(buffer, value);
    }

    public static void encode_bacnet_signed(EncodeBuffer buffer, Int32 value)
    {
            /* don't encode the leading X'FF' or X'00' of the two's compliment.
               That is, the first octet of any multi-octet encoded value shall
               not be X'00' if the most significant bit (bit 7) of the second
               octet is 0, and the first octet shall not be X'FF' if the most
               significant bit of the second octet is 1. */
        if ((value >= -128) && (value < 128))
            buffer.Add((byte)(sbyte)value);
        else if ((value >= -32768) && (value < 32768))
            encode_signed16(buffer, (Int16)value);
        else if ((value > -8388608) && (value < 8388608))
            encode_signed24(buffer, value);
        else
            encode_signed32(buffer, value);

    }

    public static void encode_octet_string(EncodeBuffer buffer, byte[] octet_string, int octet_offset, int octet_count)
    {
        if (octet_string != null)
        {
            for (int i = octet_offset; i < (octet_offset + octet_count); i++)
                buffer.Add(octet_string[i]);
        }
    }

    public static void encode_application_octet_string(EncodeBuffer buffer, byte[] octet_string, int octet_offset, int octet_count)
    {
        encode_tag(buffer, (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_OCTET_STRING, false, (uint)octet_count);
        encode_octet_string(buffer, octet_string, octet_offset, octet_count);
    }

    public static void encode_application_boolean(EncodeBuffer buffer, bool boolean_value)
    {
        encode_tag(buffer, (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_BOOLEAN, false, boolean_value ? (uint)1 : (uint)0);
    }

    public static void encode_bacnet_real(EncodeBuffer buffer, float value)
    {
        byte[] data = BitConverter.GetBytes(value);
        buffer.Add(data[3]);
        buffer.Add(data[2]);
        buffer.Add(data[1]);
        buffer.Add(data[0]);
    }

    public static void encode_bacnet_double(EncodeBuffer buffer, double value)
    {
        byte[] data = BitConverter.GetBytes(value);
        buffer.Add(data[7]);
        buffer.Add(data[6]);
        buffer.Add(data[5]);
        buffer.Add(data[4]);
        buffer.Add(data[3]);
        buffer.Add(data[2]);
        buffer.Add(data[1]);
        buffer.Add(data[0]);
    }

    public static void encode_application_real(EncodeBuffer buffer, float value)
    {
        encode_tag(buffer, (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_REAL, false, 4);
        encode_bacnet_real(buffer, value);
    }

    public static void encode_application_double(EncodeBuffer buffer, double value)
    {
        encode_tag(buffer, (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_DOUBLE, false, 8);
        encode_bacnet_double(buffer, value);
    }

    private static byte bitstring_bytes_used(BacnetBitString bit_string)
    {
        byte len = 0;    /* return value */
        byte used_bytes = 0;
        byte last_bit = 0;

        if (bit_string.bits_used > 0)
        {
            last_bit = (byte)(bit_string.bits_used - 1);
            used_bytes = (byte)(last_bit / 8);
                /* add one for the first byte */
            used_bytes++;
            len = used_bytes;
        }

        return len;
    }


    private static byte byte_reverse_bits(byte in_byte)
    {
        byte out_byte = 0;

        if ((in_byte & 1) > 0)
        {
            out_byte |= 0x80;
        }
        if ((in_byte & 2) > 0)
        {
            out_byte |= 0x40;
        }
        if ((in_byte & 4) > 0)
        {
            out_byte |= 0x20;
        }
        if ((in_byte & 8) > 0)
        {
            out_byte |= 0x10;
        }
        if ((in_byte & 16) > 0)
        {
            out_byte |= 0x8;
        }
        if ((in_byte & 32) > 0)
        {
            out_byte |= 0x4;
        }
        if ((in_byte & 64) > 0)
        {
            out_byte |= 0x2;
        }
        if ((in_byte & 128) > 0)
        {
            out_byte |= 1;
        }

        return out_byte;
    }

    private static byte bitstring_octet(BacnetBitString bit_string, byte octet_index)
    {
        byte octet = 0;

        if (bit_string.value != null)
        {
            if (octet_index < MAX_BITSTRING_BYTES)
            {
                octet = bit_string.value[octet_index];
            }
        }

        return octet;
    }

    public static void encode_bitstring(EncodeBuffer buffer, BacnetBitString bit_string)
    {
        byte remaining_used_bits = 0;
        byte used_bytes = 0;
        byte i = 0;

            /* if the bit string is empty, then the first octet shall be zero */
        if (bit_string.bits_used == 0)
        {
            buffer.Add(0);
        }
        else
        {
            used_bytes = bitstring_bytes_used(bit_string);
            remaining_used_bits = (byte)(bit_string.bits_used - ((used_bytes - 1) * 8));
                /* number of unused bits in the subsequent final octet */
            buffer.Add((byte)(8 - remaining_used_bits));
            for (i = 0; i < used_bytes; i++)
                buffer.Add(byte_reverse_bits(bitstring_octet(bit_string, i)));
        }
    }

    public static void encode_application_bitstring(EncodeBuffer buffer, BacnetBitString bit_string)
    {
        uint bit_string_encoded_length = 1;     /* 1 for the bits remaining octet */

            /* bit string may use more than 1 octet for the tag, so find out how many */
        bit_string_encoded_length += bitstring_bytes_used(bit_string);
        encode_tag(buffer, (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_BIT_STRING, false, bit_string_encoded_length);
        encode_bitstring(buffer, bit_string);
    }

    public static void bacapp_encode_application_data(EncodeBuffer buffer, BacnetValue value)
    {
        if (value.Value == null)
        {
            // Modif FC
            buffer.Add((byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_NULL);
            return;
        }

        switch (value.Tag)
        {
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_NULL:
                    /* don't encode anything */
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_BOOLEAN:
                encode_application_boolean(buffer, (bool)value.Value);
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_UNSIGNED_INT:
                encode_application_unsigned(buffer, (uint)value.Value);
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_SIGNED_INT:
                encode_application_signed(buffer, (int)value.Value);
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_REAL:
                encode_application_real(buffer, (float)value.Value);
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_DOUBLE:
                encode_application_double(buffer, (double)value.Value);
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_OCTET_STRING:
                encode_application_octet_string(buffer, (byte[])value.Value, 0, ((byte[])value.Value).Length);
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_CHARACTER_STRING:
                encode_application_character_string(buffer, (string)value.Value);
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_BIT_STRING:
                encode_application_bitstring(buffer, (BacnetBitString)value.Value);
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_ENUMERATED:
                encode_application_enumerated(buffer, (uint)value.Value);
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_DATE:
                encode_application_date(buffer, (DateTime)value.Value);
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_TIME:
                encode_application_time(buffer, (DateTime)value.Value);
                break;
            // Added for EventTimeStamp
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_TIMESTAMP:
                bacapp_encode_timestamp(buffer, (BacnetGenericTime)value.Value);
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_DATETIME:
                bacapp_encode_datetime(buffer, (DateTime)value.Value);
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_OBJECT_ID:
                encode_application_object_id(buffer, ((BacnetObjectId)value.Value).type, ((BacnetObjectId)value.Value).instance);
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_COV_SUBSCRIPTION:
                encode_cov_subscription(buffer, ((BacnetCOVSubscription)value.Value));       //is this the right way to do it, I wonder?
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_READ_ACCESS_RESULT:
                encode_read_access_result(buffer, ((BacnetReadAccessResult)value.Value));       //is this the right way to do it, I wonder?
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_READ_ACCESS_SPECIFICATION:
                encode_read_access_specification(buffer, ((BacnetReadAccessSpecification)value.Value));     //is this the right way to do it, I wonder?
                break;
            default:
                //context specific
                if (value.Value is byte[])
            {
                byte[] arr = (byte[])value.Value;
                if (buffer != null) buffer.Add(arr, arr.Length);
            }
            else
            {
                try
                {
                    Type oType = value.Value.GetType();
                    if (oType.IsGenericType && (oType.GetGenericTypeDefinition() == typeof(List<>)))
                    {
                        // last chance to encode a List<object>
                        List<object> t = (List<object>)value.Value;
                        foreach (object o in t)
                        {
                            IASN1encode d = (IASN1encode)o;
                            d.ASN1encode(buffer);
                        }
                    }
                    else
                    {
                        // last chance to encode a value
                        IASN1encode d = (IASN1encode)value.Value;
                        d.ASN1encode(buffer);
                    }
                }
                catch { throw new Exception("I cannot encode this"); }
            }
            break;
        }
    }

    public static void bacapp_encode_device_obj_property_ref(EncodeBuffer buffer, BacnetDeviceObjectPropertyReference value)
    {
        encode_context_object_id(buffer, 0, value.objectIdentifier.type, value.objectIdentifier.instance);
        encode_context_enumerated(buffer, 1, (uint)value.propertyIdentifier);

            /* Array index is optional so check if needed before inserting */
        if (value.arrayIndex != ASN1.BACNET_ARRAY_ALL)
            encode_context_unsigned(buffer, 2, value.arrayIndex);

            /* Likewise, device id is optional so see if needed
             * (set type to non device to omit */
        if (value.deviceIndentifier.type == BacnetObjectTypes.OBJECT_DEVICE)
            encode_context_object_id(buffer, 3, value.deviceIndentifier.type, value.deviceIndentifier.instance);
    }

    public static void bacapp_encode_context_device_obj_property_ref(EncodeBuffer buffer, byte tag_number, BacnetDeviceObjectPropertyReference value)
    {
        encode_opening_tag(buffer, tag_number);
        bacapp_encode_device_obj_property_ref(buffer, value);
        encode_closing_tag(buffer, tag_number);
    }

    public static void bacapp_encode_property_state(EncodeBuffer buffer, BacnetPropetyState value)
    {
        switch (value.tag)
        {
            case BacnetPropetyState.BacnetPropertyStateTypes.BOOLEAN_VALUE:
                encode_context_boolean(buffer, 0, value.state == 1 ? true : false);
                break;
            case BacnetPropetyState.BacnetPropertyStateTypes.BINARY_VALUE:
                encode_context_enumerated(buffer, 1, value.state);
                break;
            case BacnetPropetyState.BacnetPropertyStateTypes.EVENT_TYPE:
                encode_context_enumerated(buffer, 2, value.state);
                break;
            case BacnetPropetyState.BacnetPropertyStateTypes.POLARITY:
                encode_context_enumerated(buffer, 3, value.state);
                break;
            case BacnetPropetyState.BacnetPropertyStateTypes.PROGRAM_CHANGE:
                encode_context_enumerated(buffer, 4, value.state);
                break;
            case BacnetPropetyState.BacnetPropertyStateTypes.PROGRAM_STATE:
                encode_context_enumerated(buffer, 5, value.state);
                break;
            case BacnetPropetyState.BacnetPropertyStateTypes.REASON_FOR_HALT:
                encode_context_enumerated(buffer, 6, value.state);
                break;
            case BacnetPropetyState.BacnetPropertyStateTypes.RELIABILITY:
                encode_context_enumerated(buffer, 7, value.state);
                break;
            case BacnetPropetyState.BacnetPropertyStateTypes.STATE:
                encode_context_enumerated(buffer, 8, value.state);
                break;
            case BacnetPropetyState.BacnetPropertyStateTypes.SYSTEM_STATUS:
                encode_context_enumerated(buffer, 9, value.state);
                break;
            case BacnetPropetyState.BacnetPropertyStateTypes.UNITS:
                encode_context_enumerated(buffer, 10, value.state);
                break;
            case BacnetPropetyState.BacnetPropertyStateTypes.UNSIGNED_VALUE:
                encode_context_unsigned(buffer, 11, value.state);
                break;
            case BacnetPropetyState.BacnetPropertyStateTypes.LIFE_SAFETY_MODE:
                encode_context_enumerated(buffer, 12, value.state);
                break;
            case BacnetPropetyState.BacnetPropertyStateTypes.LIFE_SAFETY_STATE:
                encode_context_enumerated(buffer, 13, value.state);
                break;
            default:
                    /* FIXME: assert(0); - return a negative len? */
                break;
        }
    }

    public static void encode_context_bitstring(EncodeBuffer buffer, byte tag_number, BacnetBitString bit_string)
    {
        uint bit_string_encoded_length = 1;     /* 1 for the bits remaining octet */

            /* bit string may use more than 1 octet for the tag, so find out how many */
        bit_string_encoded_length += bitstring_bytes_used(bit_string);
        encode_tag(buffer, tag_number, true, bit_string_encoded_length);
        encode_bitstring(buffer, bit_string);
    }

    public static void encode_opening_tag(EncodeBuffer buffer, byte tag_number)
    {
        int len = 1;
        byte[] tmp = new byte[2];

            /* set class field to context specific */
        tmp[0] = 0x8;
            /* additional tag byte after this byte for extended tag byte */
        if (tag_number <= 14)
        {
            tmp[0] |= (byte)(tag_number << 4);
        }
        else
        {
            tmp[0] |= 0xF0;
            tmp[1] = tag_number;
            len++;
        }
            /* set type field to opening tag */
        tmp[0] |= 6;

        buffer.Add(tmp, len);
    }

    public static void encode_context_signed(EncodeBuffer buffer, byte tag_number, Int32 value)
    {
        int len = 0;        /* return value */

            /* length of signed int is variable, as per 20.2.11 */
        if ((value >= -128) && (value < 128))
            len = 1;
        else if ((value >= -32768) && (value < 32768))
            len = 2;
        else if ((value > -8388608) && (value < 8388608))
            len = 3;
        else
            len = 4;

        encode_tag(buffer, tag_number, true, (uint)len);
        encode_bacnet_signed(buffer, value);
    }

    public static void encode_context_object_id(EncodeBuffer buffer, byte tag_number, BacnetObjectTypes object_type, uint instance)
    {
        encode_tag(buffer, tag_number, true, 4);
        encode_bacnet_object_id(buffer, object_type, instance);
    }

    public static void encode_closing_tag(EncodeBuffer buffer, byte tag_number)
    {
        int len = 1;
        byte[] tmp = new byte[2];

            /* set class field to context specific */
        tmp[0] = 0x8;
            /* additional tag byte after this byte for extended tag byte */
        if (tag_number <= 14)
        {
            tmp[0] |= (byte)(tag_number << 4);
        }
        else
        {
            tmp[0] |= 0xF0;
            tmp[1] = tag_number;
            len++;
        }
            /* set type field to closing tag */
        tmp[0] |= 7;

        buffer.Add(tmp, len);
    }

    public static void encode_bacnet_time(EncodeBuffer buffer, DateTime value)
    {
        buffer.Add((byte)value.Hour);
        buffer.Add((byte)value.Minute);
        buffer.Add((byte)value.Second);
        buffer.Add((byte)(value.Millisecond / 10));
    }

    public static void encode_context_time(EncodeBuffer buffer, byte tag_number, DateTime value)
    {
        encode_tag(buffer, tag_number, true, 4);
        encode_bacnet_time(buffer, value);
    }

    public static void encode_bacnet_date(EncodeBuffer buffer, DateTime value)
    {
        if (value == new DateTime(1, 1, 1)) // this is the way decode do for 'Date any' = DateTime(0)
        {
            buffer.Add(0xFF); buffer.Add(0xFF); buffer.Add(0xFF); buffer.Add(0xFF);
            return;
        }

            /* allow 2 digit years */
        if (value.Year >= 1900)
            buffer.Add((byte)(value.Year - 1900));
        else if (value.Year < 0x100)
            buffer.Add((byte)value.Year);
        else
            throw new Exception("Date is rubbish");

        buffer.Add((byte)value.Month);
        buffer.Add((byte)value.Day);
        buffer.Add((byte)value.DayOfWeek);
    }

    public static void encode_application_date(EncodeBuffer buffer, DateTime value)
    {
        encode_tag(buffer, (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_DATE, false, 4);
        encode_bacnet_date(buffer, value);
    }

    public static void encode_application_time(EncodeBuffer buffer, DateTime value)
    {
        encode_tag(buffer, (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_TIME, false, 4);
        encode_bacnet_time(buffer, value);
    }

    public static void bacapp_encode_datetime(EncodeBuffer buffer, DateTime value)
    {
        if (value != new DateTime(1, 1, 1))
        {
            encode_application_date(buffer, value);
            encode_application_time(buffer, value);
        }
    }

    public static void bacapp_encode_context_datetime(EncodeBuffer buffer, byte tag_number, DateTime value)
    {
        if (value != new DateTime(1, 1, 1))
        {
            encode_opening_tag(buffer, tag_number);
            bacapp_encode_datetime(buffer, value);
            encode_closing_tag(buffer, tag_number);
        }
    }

    public static void bacapp_encode_timestamp(EncodeBuffer buffer, BacnetGenericTime value)
    {
        switch (value.Tag)
        {
            case BacnetTimestampTags.TIME_STAMP_TIME:
                encode_context_time(buffer, 0, value.Time);
                break;
            case BacnetTimestampTags.TIME_STAMP_SEQUENCE:
                encode_context_unsigned(buffer, 1, value.Sequence);
                break;
            case BacnetTimestampTags.TIME_STAMP_DATETIME:
                bacapp_encode_context_datetime(buffer, 2, value.Time);
                break;
            case BacnetTimestampTags.TIME_STAMP_NONE:
                break;
            default:
                throw new NotImplementedException();
        }
    }

    public static void bacapp_encode_context_timestamp(EncodeBuffer buffer, byte tag_number, BacnetGenericTime value)
    {
        if (value.Tag != BacnetTimestampTags.TIME_STAMP_NONE)
        {
            encode_opening_tag(buffer, tag_number);
            bacapp_encode_timestamp(buffer, value);
            encode_closing_tag(buffer, tag_number);
        }
    }

    public static void encode_application_character_string(EncodeBuffer buffer, string value)
    {

        EncodeBuffer tmp = new EncodeBuffer();
        encode_bacnet_character_string(tmp, value);

        encode_tag(buffer, (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_CHARACTER_STRING, false, (UInt32)tmp.offset);
        buffer.Add(tmp.buffer, tmp.offset);

    }

    public static void encode_bacnet_character_string(EncodeBuffer buffer, string value)
    {
        buffer.Add((byte)BacnetCharacterStringEncodings.CHARACTER_UTF8);
        byte[] bufUTF8 = Encoding.UTF8.GetBytes(value); // Encoding.ASCII depreciated : Addendum 135-2008k
        buffer.Add(bufUTF8, bufUTF8.Length);
    }

    public static void encode_unsigned16(EncodeBuffer buffer, UInt16 value)
    {
        buffer.Add((byte)((value & 0xff00) >> 8));
        buffer.Add((byte)((value & 0x00ff) >> 0));
    }

    public static void encode_unsigned24(EncodeBuffer buffer, UInt32 value)
    {
        buffer.Add((byte)((value & 0xff0000) >> 16));
        buffer.Add((byte)((value & 0x00ff00) >> 8));
        buffer.Add((byte)((value & 0x0000ff) >> 0));
    }

    public static void encode_unsigned32(EncodeBuffer buffer, UInt32 value)
    {
        buffer.Add((byte)((value & 0xff000000) >> 24));
        buffer.Add((byte)((value & 0x00ff0000) >> 16));
        buffer.Add((byte)((value & 0x0000ff00) >> 8));
        buffer.Add((byte)((value & 0x000000ff) >> 0));
    }

    public static void encode_signed16(EncodeBuffer buffer, Int16 value)
    {
        buffer.Add((byte)((value & 0xff00) >> 8));
        buffer.Add((byte)((value & 0x00ff) >> 0));
    }

    public static void encode_signed24(EncodeBuffer buffer, Int32 value)
    {
        buffer.Add((byte)((value & 0xff0000) >> 16));
        buffer.Add((byte)((value & 0x00ff00) >> 8));
        buffer.Add((byte)((value & 0x0000ff) >> 0));
    }

    public static void encode_signed32(EncodeBuffer buffer, Int32 value)
    {
        buffer.Add((byte)((value & 0xff000000) >> 24));
        buffer.Add((byte)((value & 0x00ff0000) >> 16));
        buffer.Add((byte)((value & 0x0000ff00) >> 8));
        buffer.Add((byte)((value & 0x000000ff) >> 0));
    }

    public static void encode_read_access_specification(EncodeBuffer buffer, BacnetReadAccessSpecification value)
    {
            /* Tag 0: BACnetObjectIdentifier */
        ASN1.encode_context_object_id(buffer, 0, value.objectIdentifier.type, value.objectIdentifier.instance);

            /* Tag 1: sequence of BACnetPropertyReference */
        ASN1.encode_opening_tag(buffer, 1);
        foreach (BacnetPropertyReference p in value.propertyReferences)
        {
            ASN1.encode_context_enumerated(buffer, 0, p.propertyIdentifier);

                /* optional array index */
            if (p.propertyArrayIndex != ASN1.BACNET_ARRAY_ALL)
                ASN1.encode_context_unsigned(buffer, 1, p.propertyArrayIndex);
        }
        ASN1.encode_closing_tag(buffer, 1);
    }

    public static void encode_read_access_result(EncodeBuffer buffer, BacnetReadAccessResult value)
    {
            /* Tag 0: BACnetObjectIdentifier */
        ASN1.encode_context_object_id(buffer, 0, value.objectIdentifier.type, value.objectIdentifier.instance);

            /* Tag 1: listOfResults */
        ASN1.encode_opening_tag(buffer, 1);
        foreach (BacnetPropertyValue p_value in value.values)
        {
                /* Tag 2: propertyIdentifier */
            ASN1.encode_context_enumerated(buffer, 2, p_value.property.propertyIdentifier);
                /* Tag 3: optional propertyArrayIndex */
            if (p_value.property.propertyArrayIndex != ASN1.BACNET_ARRAY_ALL)
                ASN1.encode_context_unsigned(buffer, 3, p_value.property.propertyArrayIndex);

            if (p_value.value != null && p_value.value is IList<BacnetError>)
            {
                    /* Tag 5: Error */
                ASN1.encode_opening_tag(buffer, 5);
                ASN1.encode_application_enumerated(buffer, (uint)((IList<BacnetError>)p_value.value)[0].error_class);
                ASN1.encode_application_enumerated(buffer, (uint)((IList<BacnetError>)p_value.value)[0].error_code);
                ASN1.encode_closing_tag(buffer, 5);
            }
            else
            {
                    /* Tag 4: Value */
                ASN1.encode_opening_tag(buffer, 4);
                foreach (BacnetValue v in p_value.value)
                {
                    ASN1.bacapp_encode_application_data(buffer, v);
                }
                ASN1.encode_closing_tag(buffer, 4);
            }
        }
        ASN1.encode_closing_tag(buffer, 1);
    }

    public static int decode_read_access_result(byte[] buffer, int offset, int apdu_len, out BacnetReadAccessResult value)
    {
        int len = 0;
        byte tag_number;
        uint len_value_type;
        int tag_len;

        value = new BacnetReadAccessResult();

        if (!ASN1.decode_is_context_tag(buffer, offset + len, 0))
            return -1;
        len = 1;
        len += ASN1.decode_object_id(buffer, offset + len, out value.objectIdentifier.type, out value.objectIdentifier.instance);

            /* Tag 1: listOfResults */
        if (!ASN1.decode_is_opening_tag_number(buffer, offset + len, 1))
            return -1;
        len++;

        List<BacnetPropertyValue> _value_list = new List<BacnetPropertyValue>();
        while ((apdu_len - len) > 0)
        {
            BacnetPropertyValue new_entry = new BacnetPropertyValue();

                /* end */
            if (ASN1.decode_is_closing_tag_number(buffer, offset + len, 1))
            {
                len++;
                break;
            }

                /* Tag 2: propertyIdentifier */
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
            if (tag_number != 2)
                return -1;
            len += ASN1.decode_enumerated(buffer, offset + len, len_value_type, out new_entry.property.propertyIdentifier);
                /* Tag 3: Optional Array Index */
            tag_len = ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
            if (tag_number == 3)
            {
                len += tag_len;
                len += ASN1.decode_unsigned(buffer, offset + len, len_value_type, out new_entry.property.propertyArrayIndex);
            }
            else
                new_entry.property.propertyArrayIndex = ASN1.BACNET_ARRAY_ALL;

                /* Tag 4: Value */
            tag_len = ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
            len += tag_len;
            if (tag_number == 4)
            {
                BacnetValue v;
                List<BacnetValue> local_value_list = new List<BacnetValue>();
                while (!ASN1.decode_is_closing_tag_number(buffer, offset + len, 4))
                {
                    tag_len = ASN1.bacapp_decode_application_data(buffer, offset + len, apdu_len + offset - 1, value.objectIdentifier.type, (BacnetPropertyIds)new_entry.property.propertyIdentifier, out v);
                    if (tag_len < 0) return -1;
                    len += tag_len;
                    local_value_list.Add(v);
                }
                new_entry.value = local_value_list;
                len++;
            }
            else if (tag_number == 5)
            {
                    /* Tag 5: Error */
                BacnetError err = new BacnetError();
                len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
                len += ASN1.decode_enumerated(buffer, offset + len, len_value_type, out len_value_type);      //error_class
                err.error_class = (BacnetErrorClasses)len_value_type;
                len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
                len += ASN1.decode_enumerated(buffer, offset + len, len_value_type, out len_value_type);       //error_code
                err.error_code = (BacnetErrorCodes)len_value_type;
                if (!ASN1.decode_is_closing_tag_number(buffer, offset + len, 5))
                    return -1;
                len++;

                new_entry.value = new BacnetValue[] { new BacnetValue(BacnetApplicationTags.BACNET_APPLICATION_TAG_ERROR, err) };
            }

            _value_list.Add(new_entry);
        }
        value.values = _value_list;

        return len;
    }

    public static int decode_read_access_specification(byte[] buffer, int offset, int apdu_len, out BacnetReadAccessSpecification value)
    {
        int len = 0;
        byte tag_number;
        uint len_value_type;
        int tmp;

        value = new BacnetReadAccessSpecification();

            /* Tag 0: Object ID */
        if (!ASN1.decode_is_context_tag(buffer, offset + len, 0))
            return -1;
        len++;
        len += ASN1.decode_object_id(buffer, offset + len, out value.objectIdentifier.type, out value.objectIdentifier.instance);

            /* Tag 1: sequence of ReadAccessSpecification */
        if (!ASN1.decode_is_opening_tag_number(buffer, offset + len, 1))
            return -1;
        len++;  /* opening tag is only one octet */

            /* properties */
        List<BacnetPropertyReference> __property_id_and_array_index = new List<BacnetPropertyReference>();
        while ((apdu_len - len) > 1 && !ASN1.decode_is_closing_tag_number(buffer, offset + len, 1))
        {
            BacnetPropertyReference p_ref = new BacnetPropertyReference();

                /* Tag 0: propertyIdentifier */
            if (!ASN1.IS_CONTEXT_SPECIFIC(buffer[offset + len]))
                return -1;

            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
            if (tag_number != 0)
                return -1;

                /* Should be at least the unsigned value + 1 tag left */
            if ((len + len_value_type) >= apdu_len)
                return -1;
            len += ASN1.decode_enumerated(buffer, offset + len, len_value_type, out p_ref.propertyIdentifier);
                /* Assume most probable outcome */
            p_ref.propertyArrayIndex = ASN1.BACNET_ARRAY_ALL;
                /* Tag 1: Optional propertyArrayIndex */
            if (ASN1.IS_CONTEXT_SPECIFIC(buffer[offset + len]) && !ASN1.IS_CLOSING_TAG(buffer[offset + len]))
            {
                tmp = ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
                if (tag_number == 1)
                {
                    len += tmp;
                        /* Should be at least the unsigned array index + 1 tag left */
                    if ((len + len_value_type) >= apdu_len)
                        return -1;
                    len += ASN1.decode_unsigned(buffer, offset + len, len_value_type, out p_ref.propertyArrayIndex);
                }
            }
            __property_id_and_array_index.Add(p_ref);
        }

            /* closing tag */
        if (!ASN1.decode_is_closing_tag_number(buffer, offset + len, 1))
            return -1;
        len++;

        value.propertyReferences = __property_id_and_array_index;
        return len;
    }

    // FC
    public static int decode_device_obj_property_ref(byte[] buffer, int offset, int apdu_len, out BacnetDeviceObjectPropertyReference value)
    {
        int len = 0;
        byte tag_number;
        uint len_value_type;
        int tag_len;

        value = new BacnetDeviceObjectPropertyReference();
        value.arrayIndex = ASN1.BACNET_ARRAY_ALL;

            /* Tag 0: Object ID */
        if (!ASN1.decode_is_context_tag(buffer, offset + len, 0))
            return -1;
        len++;
        len += ASN1.decode_object_id(buffer, offset + len, out value.objectIdentifier.type, out value.objectIdentifier.instance);


            /* Tag 1 : Property identifier */
        len += decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
        if (tag_number != 1)
            return -1;
        uint propertyIdentifier;
        len += decode_enumerated(buffer, offset + len, len_value_type, out propertyIdentifier);
        value.propertyIdentifier = (BacnetPropertyIds)propertyIdentifier;

            /* Tag 2: Optional Array Index */
        tag_len = ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
        if (tag_number == 2)
        {
            len += tag_len;
            len += ASN1.decode_unsigned(buffer, offset + len, len_value_type, out value.arrayIndex);
        }

            /* Tag 3 : Optional Device Identifier */
        if (!ASN1.decode_is_context_tag(buffer, offset + len, 3))
            return len;
        if (IS_CLOSING_TAG(buffer[offset + len])) return len;

        len++;

        len += ASN1.decode_object_id(buffer, offset + len, out value.deviceIndentifier.type, out value.deviceIndentifier.instance);

        return len;
    }

    public static int decode_unsigned(byte[] buffer, int offset, uint len_value, out uint value)
    {
        ushort unsigned16_value = 0;

        switch (len_value)
        {
            case 1:
                value = buffer[offset];
                break;
            case 2:
                decode_unsigned16(buffer, offset, out unsigned16_value);
                value = unsigned16_value;
                break;
            case 3:
                decode_unsigned24(buffer, offset, out value);
                break;
            case 4:
                decode_unsigned32(buffer, offset, out value);
                break;
            default:
                value = 0;
                break;
        }

        return (int)len_value;
    }

    public static int decode_unsigned32(byte[] buffer, int offset, out uint value)
    {
        value = ((uint)((((uint)buffer[offset + 0]) << 24) & 0xff000000));
        value |= ((uint)((((uint)buffer[offset + 1]) << 16) & 0x00ff0000));
        value |= ((uint)((((uint)buffer[offset + 2]) << 8) & 0x0000ff00));
        value |= ((uint)(((uint)buffer[offset + 3]) & 0x000000ff));
        return 4;
    }

    public static int decode_unsigned24(byte[] buffer, int offset, out uint value)
    {
        value = ((uint)((((uint)buffer[offset + 0]) << 16) & 0x00ff0000));
        value |= ((uint)((((uint)buffer[offset + 1]) << 8) & 0x0000ff00));
        value |= ((uint)(((uint)buffer[offset + 2]) & 0x000000ff));
        return 3;
    }

    public static int decode_unsigned16(byte[] buffer, int offset, out ushort value)
    {
        value = ((ushort)((((uint)buffer[offset + 0]) << 8) & 0x0000ff00));
        value |= ((ushort)(((uint)buffer[offset + 1]) & 0x000000ff));
        return 2;
    }

    public static int decode_unsigned8(byte[] buffer, int offset, out byte value)
    {
        value = buffer[offset + 0];
        return 1;
    }

    public static int decode_signed32(byte[] buffer, int offset, out int value)
    {
        value = ((int)((((int)buffer[offset + 0]) << 24) & 0xff000000));
        value |= ((int)((((int)buffer[offset + 1]) << 16) & 0x00ff0000));
        value |= ((int)((((int)buffer[offset + 2]) << 8) & 0x0000ff00));
        value |= ((int)(((int)buffer[offset + 3]) & 0x000000ff));
        return 4;
    }

    public static int decode_signed24(byte[] buffer, int offset, out int value)
    {
        value = ((int)((((int)buffer[offset + 0]) << 16) & 0x00ff0000));
        value |= ((int)((((int)buffer[offset + 1]) << 8) & 0x0000ff00));
        value |= ((int)(((int)buffer[offset + 2]) & 0x000000ff));
        return 3;
    }

    public static int decode_signed16(byte[] buffer, int offset, out short value)
    {
        value = ((short)((((int)buffer[offset + 0]) << 8) & 0x0000ff00));
        value |= ((short)(((int)buffer[offset + 1]) & 0x000000ff));
        return 2;
    }

    public static int decode_signed8(byte[] buffer, int offset, out sbyte value)
    {
        value = (sbyte)buffer[offset + 0];
        return 1;
    }

    public static bool IS_EXTENDED_TAG_NUMBER(byte x)
    {
        return ((x & 0xF0) == 0xF0);
    }

    public static bool IS_EXTENDED_VALUE(byte x)
    {
        return ((x & 0x07) == 5);
    }

    public static bool IS_CONTEXT_SPECIFIC(byte x)
    {
        return ((x & 0x8) == 0x8);
    }

    public static bool IS_OPENING_TAG(byte x)
    {
        return ((x & 0x07) == 6);
    }

    public static bool IS_CLOSING_TAG(byte x)
    {
        return ((x & 0x07) == 7);
    }

    public static int decode_tag_number(byte[] buffer, int offset, out byte tag_number)
    {
        int len = 1;        /* return value */

            /* decode the tag number first */
        if (IS_EXTENDED_TAG_NUMBER(buffer[offset]))
        {
                /* extended tag */
            tag_number = buffer[offset + 1];
            len++;
        }
        else
        {
            tag_number = (byte)(buffer[offset] >> 4);
        }

        return len;
    }

    public static int decode_signed(byte[] buffer, int offset, uint len_value, out int value)
    {
        switch (len_value)
        {
            case 1:
                sbyte sbyte_value;
                decode_signed8(buffer, offset, out sbyte_value);
                value = sbyte_value;
                break;
            case 2:
                short short_value;
                decode_signed16(buffer, offset, out short_value);
                value = short_value;
                break;
            case 3:
                decode_signed24(buffer, offset, out value);
                break;
            case 4:
                decode_signed32(buffer, offset, out value);
                break;
            default:
                value = 0;
                break;
        }

        return (int)len_value;
    }

    public static int decode_real(byte[] buffer, int offset, out float value)
    {
        byte[] tmp = new byte[] { buffer[offset + 3], buffer[offset + 2], buffer[offset + 1], buffer[offset + 0] };
        value = BitConverter.ToSingle(tmp, 0);
        return 4;
    }

    public static int decode_real_safe(byte[] buffer, int offset, uint len_value, out float value)
    {
        if (len_value != 4)
        {
            value = 0.0f;
            return (int)len_value;
        }
        else
        {
            return decode_real(buffer, offset, out value);
        }
    }

    public static int decode_double(byte[] buffer, int offset, out double value)
    {
        byte[] tmp = new byte[] { buffer[offset + 7], buffer[offset + 6], buffer[offset + 5], buffer[offset + 4], buffer[offset + 3], buffer[offset + 2], buffer[offset + 1], buffer[offset + 0] };
        value = BitConverter.ToDouble(tmp, 0);
        return 8;
    }

    public static int decode_double_safe(byte[] buffer, int offset, uint len_value, out double value)
    {
        if (len_value != 8)
        {
            value = 0.0f;
            return (int)len_value;
        }
        else
        {
            return decode_double(buffer, offset, out value);
        }
    }

    private static bool octetstring_copy(byte[] buffer, int offset, int max_offset, byte[] octet_string, int octet_string_offset, uint octet_string_length)
    {
        bool status = false;        /* return value */

        if (octet_string_length <= (max_offset + offset))
        {
            if (octet_string != null) Array.Copy(buffer, offset, octet_string, octet_string_offset, Math.Min(octet_string.Length, buffer.Length - offset));
            status = true;
        }

        return status;
    }

    public static int decode_octet_string(byte[] buffer, int offset, int max_length, byte[] octet_string, int octet_string_offset, uint octet_string_length)
    {
        int len = 0;        /* return value */

        octetstring_copy(buffer, offset, max_length, octet_string, octet_string_offset, octet_string_length);
        len = (int)octet_string_length;

        return len;
    }

    public static int decode_context_octet_string(byte[] buffer, int offset, int max_length, byte tag_number, byte[] octet_string, int octet_string_offset)
    {
        int len = 0;        /* return value */
        uint len_value = 0;

        octet_string = null;
        if (decode_is_context_tag(buffer, offset, tag_number))
        {
            len += decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);

            if (octetstring_copy(buffer, offset + len, max_length, octet_string, octet_string_offset, len_value))
            {
                len += (int)len_value;
            }
        }
        else
            len = -1;

        return len;
    }

    private static bool multi_charset_characterstring_decode(byte[] buffer, int offset, int max_length, byte encoding, uint length, out string char_string)
    {
        char_string = "";
        try
        {
            Encoding e;

            switch ((BacnetCharacterStringEncodings)encoding)
            {
                // 'normal' encoding, backward compatible ANSI_X34 (for decoding only)
                case BacnetCharacterStringEncodings.CHARACTER_UTF8:
                    e = Encoding.UTF8;
                    break;

                // UCS2 is backward compatible UTF16 (for decoding only)
                // http://hackipedia.org/Character%20sets/Unicode,%20UTF%20and%20UCS%20encodings/UCS-2.htm
                // https://en.wikipedia.org/wiki/Byte_order_mark
                case BacnetCharacterStringEncodings.CHARACTER_UCS2:
                    if ((buffer[offset] == 0xFF) && (buffer[offset + 1] == 0xFE)) // Byte Order Mark
                        e = Encoding.Unicode; // little endian encoding
                    else
                        e = Encoding.BigEndianUnicode; // big endian encoding if BOM is not set, or 0xFE-0xFF
                    break;

                // eq. UTF32. In usage somewhere for transmission ? A bad idea !
                case BacnetCharacterStringEncodings.CHARACTER_UCS4:
                    if ((buffer[offset] == 0xFF) && (buffer[offset + 1] == 0xFE) && (buffer[offset + 2] == 0) && (buffer[offset + 3] == 0))
                        e = Encoding.UTF32; // UTF32 little endian encoding
                    else
                        e = Encoding.GetEncoding(12001); // UTF32 big endian encoding if BOM is not set, or 0-0-0xFE-0xFF
                    break;

                case BacnetCharacterStringEncodings.CHARACTER_ISO8859:
                    e = Encoding.GetEncoding(28591); // "iso-8859-1"
                    break;

                // FIXME: somebody in Japan (or elsewhere) could help,test&validate if such devices exist ?
                // http://cgproducts.johnsoncontrols.com/met_pdf/1201531.pdf?ref=binfind.com/web page 18
                case BacnetCharacterStringEncodings.CHARACTER_MS_DBCS:
                    e = Encoding.GetEncoding("shift_jis");
                    break;

                // FIXME: somebody in Japan (or elsewhere) could help,test&validate if such devices exist ?
                // http://www.sljfaq.org/afaq/encodings.html
                case BacnetCharacterStringEncodings.CHARACTER_JISX_0208:
                    e = Encoding.GetEncoding("shift_jis"); // maybe "iso-2022-jp" ?
                    break;

                // unknown code (wrong code, experimental, ...)
                // decoded as ISO-8859-1 (removing controls) : displays certainly a strange content !
                default:
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < length; i++)
                    {
                        char oneChar = (char)buffer[offset + i]; // byte to char on .NET : ISO-8859-1
                        if (char.IsSymbol(oneChar)) sb.Append(oneChar);
                    }
                    char_string = sb.ToString();
                    return true;
            }

            char_string = e.GetString(buffer, offset, (int)length);
        }
        catch
        {
            char_string = "string decoding error !";
        }

        return true; // always OK
    }

    public static int decode_character_string(byte[] buffer, int offset, int max_length, uint len_value, out string char_string)
    {
        int len = 0;        /* return value */
        bool status = false;

        status = multi_charset_characterstring_decode(buffer, offset + 1, max_length, buffer[offset], len_value - 1, out char_string);
        if (status)
        {
            len = (int)len_value;
        }

        return len;
    }

    private static bool bitstring_set_octet(ref BacnetBitString bit_string, byte index, byte octet)
    {
        bool status = false;

        if (index < MAX_BITSTRING_BYTES)
        {
            bit_string.value[index] = octet;
            status = true;
        }

        return status;
    }

    private static bool bitstring_set_bits_used(ref BacnetBitString bit_string, byte bytes_used, byte unused_bits)
    {
        bool status = false;

            /* FIXME: check that bytes_used is at least one? */
        bit_string.bits_used = (byte)(bytes_used * 8);
        bit_string.bits_used -= unused_bits;
        status = true;

        return status;
    }

    public static int decode_bitstring(byte[] buffer, int offset, uint len_value, out BacnetBitString bit_string)
    {
        int len = 0;
        byte unused_bits = 0;
        uint i = 0;
        uint bytes_used = 0;

        bit_string = new BacnetBitString();
        bit_string.value = new byte[MAX_BITSTRING_BYTES];
        if (len_value > 0)
        {
                /* the first octet contains the unused bits */
            bytes_used = len_value - 1;
            if (bytes_used <= MAX_BITSTRING_BYTES)
            {
                len = 1;
                for (i = 0; i < bytes_used; i++)
                {
                    bitstring_set_octet(ref bit_string, (byte)i, byte_reverse_bits(buffer[offset + len++]));
                }
                unused_bits = (byte)(buffer[offset] & 0x07);
                bitstring_set_bits_used(ref bit_string, (byte)bytes_used, unused_bits);
            }
        }

        return len;
    }

    public static int decode_context_character_string(byte[] buffer, int offset, int max_length, byte tag_number, out string char_string)
    {
        int len = 0;        /* return value */
        bool status = false;
        uint len_value = 0;

        char_string = null;
        if (decode_is_context_tag(buffer, offset + len, tag_number))
        {
            len +=
                    decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);

            status =
                    multi_charset_characterstring_decode(buffer, offset + 1 + len, max_length, buffer[offset + len], len_value - 1, out char_string);
            if (status)
            {
                len += (int)len_value;
            }
        }
        else
            len = -1;

        return len;
    }

    public static int decode_date(byte[] buffer, int offset, out DateTime bdate)
    {
        int year = (ushort)(buffer[offset] + 1900);
        int month = buffer[offset + 1];
        int day = buffer[offset + 2];
        int wday = buffer[offset + 3];

        if (month == 0xFF && day == 0xFF && wday == 0xFF && (year - 1900) == 0xFF)
            bdate = new DateTime(1, 1, 1);
        else
            bdate = new DateTime(year, month, day);

        return 4;
    }

    public static int decode_date_safe(byte[] buffer, int offset, uint len_value, out DateTime bdate)
    {
        if (len_value != 4)
        {
            bdate = new DateTime(1, 1, 1);
            return (int)len_value;
        }
        else
        {
            return decode_date(buffer, offset, out bdate);
        }
    }

    public static int decode_bacnet_time(byte[] buffer, int offset, out DateTime btime)
    {
        int hour = buffer[offset + 0];
        int min = buffer[offset + 1];
        int sec = buffer[offset + 2];
        int hundredths = buffer[offset + 3];
        if (hour == 0xFF && min == 0xFF && sec == 0xFF && hundredths == 0xFF)
            btime = new DateTime(1, 1, 1);
        else
        {
            if (hundredths > 100) hundredths = 0;   // sometimes set to 255
            btime = new DateTime(1, 1, 1, hour, min, sec, hundredths * 10);
        }
        return 4;
    }

    public static int decode_bacnet_time_safe(byte[] buffer, int offset, uint len_value, out DateTime btime)
    {
        if (len_value != 4)
        {
            btime = new DateTime(1, 1, 1);
            return (int)len_value;
        }
        else
        {
            return decode_bacnet_time(buffer, offset, out btime);
        }
    }

    public static int decode_object_id(byte[] buffer, int offset, out ushort object_type, out uint instance)
    {
        uint value = 0;
        int len = 0;

        len = decode_unsigned32(buffer, offset, out value);
        object_type =
                (ushort)(((value >> BACNET_INSTANCE_BITS) & BACNET_MAX_OBJECT));
        instance = (value & BACNET_MAX_INSTANCE);

        return len;
    }

    public static int decode_object_id_safe(byte[] buffer, int offset, uint len_value, out ushort object_type, out uint instance)
    {
        if (len_value != 4)
        {
            object_type = 0;
            instance = 0;
            return 0;
        }
        else
        {
            return decode_object_id(buffer, offset, out object_type, out instance);
        }
    }

    public static int decode_context_object_id(byte[] buffer, int offset, byte tag_number, out ushort object_type, out uint instance)
    {
        int len = 0;

        if (decode_is_context_tag_with_length(buffer, offset + len, tag_number, out len))
        {
            len += decode_object_id(buffer, offset + len, out object_type, out instance);
        }
        else
        {
            object_type = 0;
            instance = 0;
            len = -1;
        }
        return len;
    }

    public static int decode_application_time(byte[] buffer, int offset, out DateTime btime)
    {
        int len = 0;
        byte tag_number;
        decode_tag_number(buffer, offset + len, out tag_number);

        if (tag_number == (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_TIME)
        {
            len++;
            len += decode_bacnet_time(buffer, offset + len, out btime);
        }
        else
        {
            btime = new DateTime(1, 1, 1);
            len = -1;
        }
        return len;
    }


    public static int decode_context_bacnet_time(byte[] buffer, int offset, byte tag_number, out DateTime btime)
    {
        int len = 0;

        if (decode_is_context_tag_with_length(buffer, offset + len, tag_number, out len))
        {
            len += decode_bacnet_time(buffer, offset + len, out btime);
        }
        else
        {
            btime = new DateTime(1, 1, 1);
            len = -1;
        }
        return len;
    }

    public static int decode_application_date(byte[] buffer, int offset, out DateTime bdate)
    {
        int len = 0;
        byte tag_number;
        decode_tag_number(buffer, offset + len, out tag_number);

        if (tag_number == (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_DATE)
        {
            len++;
            len += decode_date(buffer, offset + len, out bdate);
        }
        else
        {
            bdate = new DateTime(1, 1, 1);
            len = -1;
        }
        return len;
    }

    public static bool decode_is_context_tag_with_length(byte[] buffer, int offset, byte tag_number, out int tag_length)
    {
        byte my_tag_number = 0;

        tag_length = decode_tag_number(buffer, offset, out my_tag_number);

        return (bool)(IS_CONTEXT_SPECIFIC(buffer[offset]) &&
                (my_tag_number == tag_number));
    }

    public static int decode_context_date(byte[] buffer, int offset, byte tag_number, out DateTime bdate)
    {
        int len = 0;

        if (decode_is_context_tag_with_length(buffer, offset + len, tag_number, out len))
        {
            len += decode_date(buffer, offset + len, out bdate);
        }
        else
        {
            bdate = new DateTime(1, 1, 1);
            len = -1;
        }
        return len;
    }

    public static int bacapp_decode_data(byte[] buffer, int offset, int max_length, BacnetApplicationTags tag_data_type, uint len_value_type, out BacnetValue value)
    {
        int len = 0;
        uint uint_value;
        int int_value;

        value = new BacnetValue();
        value.Tag = tag_data_type;

        switch (tag_data_type)
        {
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_NULL:
                    /* nothing else to do */
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_BOOLEAN:
                value.Value = len_value_type > 0 ? true : false;
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_UNSIGNED_INT:
                len = decode_unsigned(buffer, offset, len_value_type, out uint_value);
                value.Value = uint_value;
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_SIGNED_INT:
                len = decode_signed(buffer, offset, len_value_type, out int_value);
                value.Value = int_value;
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_REAL:
                float float_value;
                len = decode_real_safe(buffer, offset, len_value_type, out float_value);
                value.Value = float_value;
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_DOUBLE:
                double double_value;
                len = decode_double_safe(buffer, offset, len_value_type, out double_value);
                value.Value = double_value;
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_OCTET_STRING:
                byte[] octet_string = new byte[len_value_type];
                len = decode_octet_string(buffer, offset, max_length, octet_string, 0, len_value_type);
                value.Value = octet_string;
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_CHARACTER_STRING:
                string string_value;
                len = decode_character_string(buffer, offset, max_length, len_value_type, out string_value);
                value.Value = string_value;
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_BIT_STRING:
                BacnetBitString bit_value;
                len = decode_bitstring(buffer, offset, len_value_type, out bit_value);
                value.Value = bit_value;
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_ENUMERATED:
                len = decode_enumerated(buffer, offset, len_value_type, out uint_value);
                value.Value = uint_value;
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_DATE:
                DateTime date_value;
                len = decode_date_safe(buffer, offset, len_value_type, out date_value);
                value.Value = date_value;
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_TIME:
                DateTime time_value;
                len = decode_bacnet_time_safe(buffer, offset, len_value_type, out time_value);
                value.Value = time_value;
                break;
            case BacnetApplicationTags.BACNET_APPLICATION_TAG_OBJECT_ID:
            {
                ushort object_type = 0;
                uint instance = 0;
                len = decode_object_id_safe(buffer, offset, len_value_type, out object_type, out instance);
                value.Value = new BacnetObjectId((BacnetObjectTypes)object_type, instance);
            }
            break;
            default:
                break;
        }

        return len;
    }

    /* returns the fixed tag type for certain context tagged properties */
    private static BacnetApplicationTags bacapp_context_tag_type(BacnetPropertyIds property, byte tag_number)
    {
        BacnetApplicationTags tag = BacnetApplicationTags.MAX_BACNET_APPLICATION_TAG;

        switch (property)
        {
            case BacnetPropertyIds.PROP_ACTUAL_SHED_LEVEL:
            case BacnetPropertyIds.PROP_REQUESTED_SHED_LEVEL:
            case BacnetPropertyIds.PROP_EXPECTED_SHED_LEVEL:
                switch (tag_number)
                {
                    case 0:
                    case 1:
                        tag = BacnetApplicationTags.BACNET_APPLICATION_TAG_UNSIGNED_INT;
                        break;
                    case 2:
                        tag = BacnetApplicationTags.BACNET_APPLICATION_TAG_REAL;
                        break;
                    default:
                        break;
                }
                break;
            case BacnetPropertyIds.PROP_ACTION:
                switch (tag_number)
                {
                    case 0:
                    case 1:
                        tag = BacnetApplicationTags.BACNET_APPLICATION_TAG_OBJECT_ID;
                        break;
                    case 2:
                        tag = BacnetApplicationTags.BACNET_APPLICATION_TAG_ENUMERATED;
                        break;
                    case 3:
                    case 5:
                    case 6:
                        tag = BacnetApplicationTags.BACNET_APPLICATION_TAG_UNSIGNED_INT;
                        break;
                    case 7:
                    case 8:
                        tag = BacnetApplicationTags.BACNET_APPLICATION_TAG_BOOLEAN;
                        break;
                    case 4:        /* propertyValue: abstract syntax */
                    default:
                        break;
                }
                break;
            case BacnetPropertyIds.PROP_LIST_OF_GROUP_MEMBERS:
                    /* Sequence of ReadAccessSpecification */
                switch (tag_number)
                {
                    case 0:
                        tag = BacnetApplicationTags.BACNET_APPLICATION_TAG_OBJECT_ID;
                        break;
                    default:
                        break;
                }
                break;
            case BacnetPropertyIds.PROP_EXCEPTION_SCHEDULE:
                switch (tag_number)
                {
                    case 1:
                        tag = BacnetApplicationTags.BACNET_APPLICATION_TAG_OBJECT_ID;
                        break;
                    case 3:
                        tag = BacnetApplicationTags.BACNET_APPLICATION_TAG_UNSIGNED_INT;
                        break;
                    case 0:        /* calendarEntry: abstract syntax + context */
                    case 2:        /* list of BACnetTimeValue: abstract syntax */
                    default:
                        break;
                }
                break;
            case BacnetPropertyIds.PROP_LOG_DEVICE_OBJECT_PROPERTY:
                switch (tag_number)
                {
                    case 0:        /* Object ID */
                    case 3:        /* Device ID */
                        tag = BacnetApplicationTags.BACNET_APPLICATION_TAG_OBJECT_ID;
                        break;
                    case 1:        /* Property ID */
                        tag = BacnetApplicationTags.BACNET_APPLICATION_TAG_ENUMERATED;
                        break;
                    case 2:        /* Array index */
                        tag = BacnetApplicationTags.BACNET_APPLICATION_TAG_UNSIGNED_INT;
                        break;
                    default:
                        break;
                }
                break;
            case BacnetPropertyIds.PROP_SUBORDINATE_LIST:
                    /* BACnetARRAY[N] of BACnetDeviceObjectReference */
                switch (tag_number)
                {
                    case 0:        /* Optional Device ID */
                    case 1:        /* Object ID */
                        tag = BacnetApplicationTags.BACNET_APPLICATION_TAG_OBJECT_ID;
                        break;
                    default:
                        break;
                }
                break;

            case BacnetPropertyIds.PROP_RECIPIENT_LIST:
                    /* List of BACnetDestination */
                switch (tag_number)
                {
                    case 0:        /* Device Object ID */
                        tag = BacnetApplicationTags.BACNET_APPLICATION_TAG_OBJECT_ID;
                        break;
                    default:
                        break;
                }
                break;
            case BacnetPropertyIds.PROP_ACTIVE_COV_SUBSCRIPTIONS:
                    /* BACnetCOVSubscription */
                switch (tag_number)
                {
                    case 0:        /* BACnetRecipientProcess */
                    case 1:        /* BACnetObjectPropertyReference */
                        break;
                    case 2:        /* issueConfirmedNotifications */
                        tag = BacnetApplicationTags.BACNET_APPLICATION_TAG_BOOLEAN;
                        break;
                    case 3:        /* timeRemaining */
                        tag = BacnetApplicationTags.BACNET_APPLICATION_TAG_UNSIGNED_INT;
                        break;
                    case 4:        /* covIncrement */
                        tag = BacnetApplicationTags.BACNET_APPLICATION_TAG_REAL;
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }

        return tag;
    }

    public static int bacapp_decode_context_data(byte[] buffer, int offset, uint max_apdu_len, BacnetApplicationTags property_tag, out BacnetValue value)
    {
        int apdu_len = 0, len = 0;
        int tag_len = 0;
        byte tag_number = 0;
        uint len_value_type = 0;

        value = new BacnetValue();

        if (IS_CONTEXT_SPECIFIC(buffer[offset]))
        {
            //value->context_specific = true;
            tag_len = decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
            apdu_len = tag_len;
                /* Empty construct : (closing tag) => returns NULL value */
            if (tag_len > 0 && (tag_len <= max_apdu_len) && !decode_is_closing_tag_number(buffer, offset + len, tag_number))
            {
                //value->context_tag = tag_number;
                if (property_tag < BacnetApplicationTags.MAX_BACNET_APPLICATION_TAG)
                {
                    len = bacapp_decode_data(buffer, offset + apdu_len, (int)max_apdu_len, property_tag, len_value_type, out value);
                    apdu_len += len;
                }
                else if (len_value_type > 0)
                {
                        /* Unknown value : non null size (elementary type) */
                    apdu_len += (int)len_value_type;
                        /* SHOULD NOT HAPPEN, EXCEPTED WHEN READING UNKNOWN CONTEXTUAL PROPERTY */
                }
                else
                    apdu_len = -1;
            }
            else if (tag_len == 1)        /* and is a Closing tag */
                apdu_len = 0;       /* Don't advance over that closing tag. */
        }

        return apdu_len;
    }

    public static int bacapp_decode_application_data(byte[] buffer, int offset, int max_offset, BacnetObjectTypes object_type, BacnetPropertyIds property_id, out BacnetValue value)
    {
        int len = 0;
        int tag_len = 0;
        int decode_len = 0;
        byte tag_number = 0;
        uint len_value_type = 0;

        value = new BacnetValue();

            /* FIXME: use max_apdu_len! */
        if (!IS_CONTEXT_SPECIFIC(buffer[offset]))
        {
            tag_len = decode_tag_number_and_value(buffer, offset, out tag_number, out len_value_type);
            if (tag_len > 0)
            {
                len += tag_len;
                decode_len = bacapp_decode_data(buffer, offset + len, max_offset, (BacnetApplicationTags)tag_number, len_value_type, out value);
                if (decode_len < 0) return decode_len;
                len += decode_len;
            }
        }
        else
        {
            return bacapp_decode_context_application_data(buffer, offset, max_offset, object_type, property_id, out value);
        }

        return len;
    }

    public static int bacapp_decode_context_application_data(byte[] buffer, int offset, int max_offset, BacnetObjectTypes object_type, BacnetPropertyIds property_id, out BacnetValue value)
    {
        int len = 0;
        int tag_len = 0;
        byte tag_number = 0;
        byte sub_tag_number = 0;
        uint len_value_type = 0;

        value = new BacnetValue();

        if (IS_CONTEXT_SPECIFIC(buffer[offset]))
        {
            //this seems to be a strange way to determine object encodings
            if (property_id == BacnetPropertyIds.PROP_LIST_OF_GROUP_MEMBERS)
            {
                BacnetReadAccessSpecification v;
                tag_len = ASN1.decode_read_access_specification(buffer, offset, max_offset, out v);
                if (tag_len < 0) return -1;
                value.Tag = BacnetApplicationTags.BACNET_APPLICATION_TAG_READ_ACCESS_SPECIFICATION;
                value.Value = v;
                return tag_len;
            }
            else if (property_id == BacnetPropertyIds.PROP_ACTIVE_COV_SUBSCRIPTIONS)
            {
                BacnetCOVSubscription v;
                tag_len = ASN1.decode_cov_subscription(buffer, offset, max_offset, out v);
                if (tag_len < 0) return -1;
                value.Tag = BacnetApplicationTags.BACNET_APPLICATION_TAG_COV_SUBSCRIPTION;
                value.Value = v;
                return tag_len;
            }
            else if (object_type == BacnetObjectTypes.OBJECT_GROUP && property_id == BacnetPropertyIds.PROP_PRESENT_VALUE)
            {
                BacnetReadAccessResult v;
                tag_len = ASN1.decode_read_access_result(buffer, offset, max_offset, out v);
                if (tag_len < 0) return -1;
                value.Tag = BacnetApplicationTags.BACNET_APPLICATION_TAG_READ_ACCESS_RESULT;
                value.Value = v;
                return tag_len;
            }
            else if ((property_id == BacnetPropertyIds.PROP_LIST_OF_OBJECT_PROPERTY_REFERENCES) || (property_id == BacnetPropertyIds.PROP_LOG_DEVICE_OBJECT_PROPERTY))
            {
                BacnetDeviceObjectPropertyReference v;
                tag_len = ASN1.decode_device_obj_property_ref(buffer, offset, max_offset, out v);
                if (tag_len < 0) return -1;
                value.Tag = BacnetApplicationTags.BACNET_APPLICATION_TAG_OBJECT_PROPERTY_REFERENCE;
                value.Value = v;
                return tag_len;
            }
            else if (property_id == BacnetPropertyIds.PROP_DATE_LIST)
            {
                BACnetCalendarEntry v = new BACnetCalendarEntry();
                tag_len = v.ASN1decode(buffer, offset, (uint)max_offset);
                if (tag_len < 0) return -1;
                value.Tag = BacnetApplicationTags.BACNET_APPLICATION_TAG_CONTEXT_SPECIFIC_DECODED;
                value.Value = v;
                return tag_len;
            }

            value.Tag = BacnetApplicationTags.BACNET_APPLICATION_TAG_CONTEXT_SPECIFIC_DECODED;
            List<BacnetValue> list = new List<BacnetValue>();

            decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
            // If an opening tag is not present, no loop to get the values
            bool MultiplValue = IS_OPENING_TAG(buffer[offset + len]);

            while (((len + offset) <= max_offset) && !IS_CLOSING_TAG(buffer[offset + len]))
            {
                tag_len = decode_tag_number_and_value(buffer, offset + len, out sub_tag_number, out len_value_type);
                if (tag_len < 0) return -1;

                if (len_value_type == 0)
                {
                    BacnetValue sub_value;
                    len += tag_len;
                    tag_len = bacapp_decode_application_data(buffer, offset + len, max_offset, BacnetObjectTypes.MAX_BACNET_OBJECT_TYPE, BacnetPropertyIds.MAX_BACNET_PROPERTY_ID, out sub_value);
                    if (tag_len < 0) return -1;
                    list.Add(sub_value);
                    len += tag_len;
                }
                else
                {
                    BacnetValue sub_value = new BacnetValue();

                    //override tag_number
                    BacnetApplicationTags override_tag_number = bacapp_context_tag_type(property_id, sub_tag_number);
                    if (override_tag_number != BacnetApplicationTags.MAX_BACNET_APPLICATION_TAG) sub_tag_number = (byte)override_tag_number;

                    //try app decode
                    int sub_tag_len = bacapp_decode_data(buffer, offset + len + tag_len, max_offset, (BacnetApplicationTags)sub_tag_number, len_value_type, out sub_value);
                    if (sub_tag_len == (int)len_value_type)
                    {
                        list.Add(sub_value);
                        len += tag_len + (int)len_value_type;
                    }
                    else
                    {
                        //fallback to copy byte array
                        byte[] context_specific = new byte[(int)len_value_type];
                        Array.Copy(buffer, offset + len + tag_len, context_specific, 0, (int)len_value_type);
                        sub_value = new BacnetValue(BacnetApplicationTags.BACNET_APPLICATION_TAG_CONTEXT_SPECIFIC_ENCODED, context_specific);

                        list.Add(sub_value);
                        len += tag_len + (int)len_value_type;
                    }
                }

                if (MultiplValue == false)
                {
                    value = list[0];
                    return len;
                }
            }
            if ((len + offset) > max_offset) return -1;

            //end tag
            if (decode_is_closing_tag_number(buffer, offset + len, tag_number))
                len++;

            //context specifique is array of BACNET_VALUE
            value.Value = list.ToArray();
        }
        else
        {
            return -1;
        }

        return len;
    }

    public static int decode_object_id(byte[] buffer, int offset, out BacnetObjectTypes object_type, out uint instance)
    {
        uint value = 0;
        int len = 0;

        len = decode_unsigned32(buffer, offset, out value);
        object_type = (BacnetObjectTypes)(((value >> BACNET_INSTANCE_BITS) & BACNET_MAX_OBJECT));
        instance = (value & BACNET_MAX_INSTANCE);

        return len;
    }

    public static int decode_enumerated(byte[] buffer, int offset, uint len_value, out uint value)
    {
        int len;
        len = decode_unsigned(buffer, offset, len_value, out value);
        return len;
    }

    public static bool decode_is_context_tag(byte[] buffer, int offset, byte tag_number)
    {
        byte my_tag_number = 0;

        decode_tag_number(buffer, offset, out my_tag_number);
        return (bool)(IS_CONTEXT_SPECIFIC(buffer[offset]) && (my_tag_number == tag_number));
    }

    public static bool decode_is_opening_tag_number(byte[] buffer, int offset, byte tag_number)
    {
        byte my_tag_number = 0;

        decode_tag_number(buffer, offset, out my_tag_number);
        return (bool)(IS_OPENING_TAG(buffer[offset]) && (my_tag_number == tag_number));
    }

    public static bool decode_is_closing_tag_number(byte[] buffer, int offset, byte tag_number)
    {
        byte my_tag_number = 0;

        decode_tag_number(buffer, offset, out my_tag_number);
        return (bool)(IS_CLOSING_TAG(buffer[offset]) && (my_tag_number == tag_number));
    }

    public static bool decode_is_closing_tag(byte[] buffer, int offset)
    {
        return (bool)((buffer[offset] & 0x07) == 7);
    }

    public static bool decode_is_opening_tag(byte[] buffer, int offset)
    {
        return (bool)((buffer[offset] & 0x07) == 6);
    }

    public static int decode_tag_number_and_value(byte[] buffer, int offset, out byte tag_number, out uint value)
    {
        int len = 1;
        ushort value16;
        uint value32;

        len = decode_tag_number(buffer, offset, out tag_number);
        if (IS_EXTENDED_VALUE(buffer[offset]))
        {
                /* tagged as uint32_t */
            if (buffer[offset + len] == 255)
            {
                len++;
                len += decode_unsigned32(buffer, offset + len, out value32);
                value = value32;
            }
                /* tagged as uint16_t */
            else if (buffer[offset + len] == 254)
            {
                len++;
                len += decode_unsigned16(buffer, offset + len, out value16);
                value = value16;
            }
                /* no tag - must be uint8_t */
            else
            {
                value = buffer[offset + len];
                len++;
            }
        }
        else if (IS_OPENING_TAG(buffer[offset]))
        {
            value = 0;
        }
        else if (IS_CLOSING_TAG(buffer[offset]))
        {
                /* closing tag */
            value = 0;
        }
        else
        {
                /* small value */
            value = (uint)(buffer[offset] & 0x07);
        }

        return len;
    }

    /// <summary>
    /// This is used by the Active_COV_Subscriptions property in DEVICE
    /// </summary>
    public static void encode_cov_subscription(EncodeBuffer buffer, BacnetCOVSubscription value)
    {
            /* Recipient [0] BACnetRecipientProcess - opening */
        ASN1.encode_opening_tag(buffer, 0);

            /*  recipient [0] BACnetRecipient - opening */
        ASN1.encode_opening_tag(buffer, 0);
            /* CHOICE - device [0] BACnetObjectIdentifier - opening */
            /* CHOICE - address [1] BACnetAddress - opening */
        ASN1.encode_opening_tag(buffer, 1);
            /* network-number Unsigned16, */
            /* -- A value of 0 indicates the local network */
        ASN1.encode_application_unsigned(buffer, value.Recipient.net);
            /* mac-address OCTET STRING */
            /* -- A string of length 0 indicates a broadcast */
        if (value.Recipient.net == 0xFFFF)
            ASN1.encode_application_octet_string(buffer, new byte[0], 0, 0);
        else
            ASN1.encode_application_octet_string(buffer, value.Recipient.adr, 0, value.Recipient.adr.Length);
            /* CHOICE - address [1] BACnetAddress - closing */
        ASN1.encode_closing_tag(buffer, 1);
            /*  recipient [0] BACnetRecipient - closing */
        ASN1.encode_closing_tag(buffer, 0);

            /* processIdentifier [1] Unsigned32 */
        ASN1.encode_context_unsigned(buffer, 1, value.subscriptionProcessIdentifier);
            /* Recipient [0] BACnetRecipientProcess - closing */
        ASN1.encode_closing_tag(buffer, 0);

            /*  MonitoredPropertyReference [1] BACnetObjectPropertyReference, */
        ASN1.encode_opening_tag(buffer, 1);
            /* objectIdentifier [0] */
        ASN1.encode_context_object_id(buffer, 0, value.monitoredObjectIdentifier.type, value.monitoredObjectIdentifier.instance);
            /* propertyIdentifier [1] */
            /* FIXME: we are monitoring 2 properties! How to encode? */
        ASN1.encode_context_enumerated(buffer, 1, value.monitoredProperty.propertyIdentifier);
        if (value.monitoredProperty.propertyArrayIndex != ASN1.BACNET_ARRAY_ALL)
            ASN1.encode_context_unsigned(buffer, 2, value.monitoredProperty.propertyArrayIndex);
            /* MonitoredPropertyReference [1] - closing */
        ASN1.encode_closing_tag(buffer, 1);

            /* IssueConfirmedNotifications [2] BOOLEAN, */
        ASN1.encode_context_boolean(buffer, 2, value.IssueConfirmedNotifications);
            /* TimeRemaining [3] Unsigned, */
        ASN1.encode_context_unsigned(buffer, 3, value.TimeRemaining);
            /* COVIncrement [4] REAL OPTIONAL, */
        if (value.COVIncrement > 0)
            ASN1.encode_context_real(buffer, 4, value.COVIncrement);
    }

    public static int decode_cov_subscription(byte[] buffer, int offset, int apdu_len, out BacnetCOVSubscription value)
    {
        int len = 0;
        int tag_len;
        byte tag_number;
        uint len_value_type;
        uint tmp;

        value = new BacnetCOVSubscription();
        value.Recipient = new BacnetAddress(BacnetAddressTypes.None, 0, null);

        if (!decode_is_opening_tag_number(buffer, offset + len, 0))
            return -1;
        len++;
        if (!decode_is_opening_tag_number(buffer, offset + len, 0))
            return -1;
        len++;
        if (!decode_is_opening_tag_number(buffer, offset + len, 1))
            return -1;
        len++;
        len += decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
        if (tag_number != (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_UNSIGNED_INT)
            return -1;
        len += decode_unsigned(buffer, offset + len, len_value_type, out tmp);
        value.Recipient.net = (ushort)tmp;
        len += decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
        if (tag_number != (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_OCTET_STRING)
            return -1;
        value.Recipient.adr = new byte[len_value_type];
        len += decode_octet_string(buffer, offset + len, apdu_len, value.Recipient.adr, 0, len_value_type);
        if (!decode_is_closing_tag_number(buffer, offset + len, 1))
            return -1;
        len++;
        if (!decode_is_closing_tag_number(buffer, offset + len, 0))
            return -1;
        len++;

        len += decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
        if (tag_number != 1)
            return -1;
        len += decode_unsigned(buffer, offset + len, len_value_type, out value.subscriptionProcessIdentifier);
        if (!decode_is_closing_tag_number(buffer, offset + len, 0))
            return -1;
        len++;

        if (!decode_is_opening_tag_number(buffer, offset + len, 1))
            return -1;
        len++;
        len += decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
        if (tag_number != 0)
            return -1;
        len += decode_object_id(buffer, offset + len, out value.monitoredObjectIdentifier.type, out value.monitoredObjectIdentifier.instance);
        len += decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
        if (tag_number != 1)
            return -1;
        len += decode_enumerated(buffer, offset + len, len_value_type, out value.monitoredProperty.propertyIdentifier);
        tag_len = decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
        if (tag_number == 2)
        {
            len += tag_len;
            len += decode_unsigned(buffer, offset + len, len_value_type, out value.monitoredProperty.propertyArrayIndex);
        }
        else
            value.monitoredProperty.propertyArrayIndex = BACNET_ARRAY_ALL;
        if (!decode_is_closing_tag_number(buffer, offset + len, 1))
            return -1;
        len++;

        len += decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
        if (tag_number != 2)
            return -1;
        value.IssueConfirmedNotifications = buffer[offset + len] > 0 ? true : false;
        len++;

        len += decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
        if (tag_number != 3)
            return -1;
        len += decode_unsigned(buffer, offset + len, len_value_type, out value.TimeRemaining);

        if (len < apdu_len && !IS_CLOSING_TAG(buffer[offset + len]))
        {
            len += decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
            if (tag_number != 4)
                return -1;
            len += decode_real(buffer, offset + len, out value.COVIncrement);
        }

        return len;
    }
}
