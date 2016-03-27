package BACnetCore.BACnetCore.Classes;

/**
 * Created by czitop on 3/21/2016.
 */
public class ASN1 {
    public final int BACNET_MAX_OBJECT = 0x3FF;
    public final int BACNET_INSTANCE_BITS = 22;
    public final int BACNET_MAX_INSTANCE = 0X3FFFF;
    public final int MAX_BITSTRING_BYTES = 15;
    public final int BACNET_ARRAY_ALL = 0XFFFFFFF;
    public final int BACNET_NO_PRIORITY = 0;
    public final int BACNET_MIN_PRIORITY = 1;
    public final int BACNET_MAX_PRIORITY = 16;

    public static void encode_bacnet_object_id(EncodeBuffer buffer, BacnetObjectTypes object_type, int instance)
    {
        int value = 0;
        int type = 0;

        type = (UInt32)object_type;
        value = ((type & BACNET_MAX_OBJECT) << BACNET_INSTANCE_BITS) | (instance & BACNET_MAX_INSTANCE);
        encode_unsigned32(buffer, value);
    }

    public static void encode_tag(EncodeBuffer buffer, byte tag_number, boolean context_specific, int len_value_type)
    {
        int len = 1;
        byte[]tmp = new byte[3];

        tmp[0] = 0;
        if (context_specific) tmp[0] |= 0x8;

        if (tag_number <= 14)
        {
            tmp[0] |= (byte)(tag_number << 4);
        } else
        {
            tmp[0] |= 0xF0;
            tmp[1] = tag_number;
            len++;
        }

        if (len_value_type <= 4)
        {
            tmp[0] |= (byte)len_value_type;
            buffer.Add(tmp,len);
        }
        else
        {
            tmp[0] |= 5;
            if (len_value_type <=255){
                tmp[len++] = (byte)len_value_type;
                buffer.Add(tmp, len);
            } else if (len_value_type <= 65535) {
                tmp[len++] = (byte)254;
                buffer.Add(tmp, len);
                // Need to figure out How to replicate this line  encode_unsigned16(buffer, (UInt16)len_value_type);
            } else {
                tmp[len++] = (byte)255;
                buffer.Add(tmp, len);
                // Need to figure out How to replicate this line  encode_unsigned16(buffer, (UInt32)len_value_type);
            }
        }
    }

    public static void encode_bacnet_enumerated(EncodeBuffer buffer, int value)
    {
        encode_bacnet_unsigned(buffer, value);
    }

    public static void encode_application_object_id(EncodeBuffer buffer, int instance)
    {
        EncodeBuffer tmp1 = new EncodeBuffer();
        encode_bacnet_object_id(tmp1, instance);
        encode_tag(buffer (byte), false, (int)tmp1.offset);
        buffer.Add(tmp1.buffer, tmp1.offset);
    }

}
