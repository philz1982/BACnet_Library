package BACnetCore.CoreClasses;

/**
 * Created by Phil on 3/26/2016.
 */
public class APDU
{
    public static BacnetPduTypes GetDecodedType(byte[] buffer, int offset)
    {
        return (BacnetPduTypes)buffer[offset];
    }

    public static void SetDecodedType(byte[] buffer, int offset, BacnetPduTypes type)
    {
        buffer[offset] = (byte)type;
    }

    public static int GetDecodedInvokeId(byte[] buffer, int offset)
    {
        BacnetPduTypes type = GetDecodedType(buffer, offset);
        switch (type & BacnetPduTypes.PDU_TYPE_MASK)
        {
            case BacnetPduTypes.PDU_TYPE_SIMPLE_ACK:
            case BacnetPduTypes.PDU_TYPE_COMPLEX_ACK:
            case BacnetPduTypes.PDU_TYPE_ERROR:
            case BacnetPduTypes.PDU_TYPE_REJECT:
            case BacnetPduTypes.PDU_TYPE_ABORT:
                return buffer[offset + 1];
            case BacnetPduTypes.PDU_TYPE_CONFIRMED_SERVICE_REQUEST:
                return buffer[offset + 2];
            default:
                return -1;
        }
    }

    public static void EncodeConfirmedServiceRequest(EncodeBuffer buffer, BacnetPduTypes type, BacnetConfirmedServices service, BacnetMaxSegments max_segments, BacnetMaxAdpu max_adpu, byte invoke_id, byte sequence_number, byte proposed_window_size)
    {
        buffer.buffer[buffer.offset++] = (byte)type;
        buffer.buffer[buffer.offset++] = (byte)((byte)max_segments | (byte)max_adpu);
        buffer.buffer[buffer.offset++] = invoke_id;

        if ((type & BacnetPduTypes.SEGMENTED_MESSAGE) > 0)
        {
            buffer.buffer[buffer.offset++] = sequence_number;
            buffer.buffer[buffer.offset++] = proposed_window_size;
        }
        buffer.buffer[buffer.offset++] = (byte)service;
    }

    public static int DecodeConfirmedServiceRequest(byte[] buffer, int offset, out BacnetPduTypes type, out BacnetConfirmedServices service, out BacnetMaxSegments max_segments, out BacnetMaxAdpu max_adpu, out byte invoke_id, out byte sequence_number, out byte proposed_window_number)
    {
        int org_offset = offset;

        type = (BacnetPduTypes)buffer[offset++];
        max_segments = (BacnetMaxSegments)(buffer[offset] & 0xF0);
        max_adpu = (BacnetMaxAdpu)(buffer[offset++] & 0x0F);
        invoke_id = buffer[offset++];

        sequence_number = 0;
        proposed_window_number = 0;
        if ((type & BacnetPduTypes.SEGMENTED_MESSAGE) > 0)
        {
            sequence_number = buffer[offset++];
            proposed_window_number = buffer[offset++];
        }
        service = (BacnetConfirmedServices)buffer[offset++];

        return offset - org_offset;
    }

    public static void EncodeUnconfirmedServiceRequest(EncodeBuffer buffer, BacnetPduTypes type, BacnetUnconfirmedServices service)
    {
        buffer.buffer[buffer.offset++] = (byte)type;
        buffer.buffer[buffer.offset++] = (byte)service;
    }

    public static int DecodeUnconfirmedServiceRequest(byte[] buffer, int offset, out BacnetPduTypes type, out BacnetUnconfirmedServices service)
    {
        int org_offset = offset;

        type = (BacnetPduTypes)buffer[offset++];
        service = (BacnetUnconfirmedServices)buffer[offset++];

        return offset - org_offset;
    }

    public static void EncodeSimpleAck(EncodeBuffer buffer, BacnetPduTypes type, BacnetConfirmedServices service, byte invoke_id)
    {
        buffer.buffer[buffer.offset++] = (byte)type;
        buffer.buffer[buffer.offset++] = invoke_id;
        buffer.buffer[buffer.offset++] = (byte)service;
    }

    public static int DecodeSimpleAck(byte[] buffer, int offset, out BacnetPduTypes type, out BacnetConfirmedServices service, out byte invoke_id)
    {
        int org_offset = offset;

        type = (BacnetPduTypes)buffer[offset++];
        invoke_id = buffer[offset++];
        service = (BacnetConfirmedServices)buffer[offset++];

        return offset - org_offset;
    }

    public static int EncodeComplexAck(EncodeBuffer buffer, BacnetPduTypes type, BacnetConfirmedServices service, byte invoke_id, byte sequence_number, byte proposed_window_number)
    {
        int len = 3;
        buffer.buffer[buffer.offset++] = (byte)type;
        buffer.buffer[buffer.offset++] = invoke_id;
        if ((type & BacnetPduTypes.SEGMENTED_MESSAGE) > 0)
        {
            buffer.buffer[buffer.offset++] = sequence_number;
            buffer.buffer[buffer.offset++] = proposed_window_number;
            len += 2;
        }
        buffer.buffer[buffer.offset++] = (byte)service;
        return len;
    }

    public static int DecodeComplexAck(byte[] buffer, int offset, out BacnetPduTypes type, out BacnetConfirmedServices service, out byte invoke_id, out byte sequence_number, out byte proposed_window_number)
    {
        int org_offset = offset;

        type = (BacnetPduTypes)buffer[offset++];
        invoke_id = buffer[offset++];

        sequence_number = 0;
        proposed_window_number = 0;
        if ((type & BacnetPduTypes.SEGMENTED_MESSAGE) > 0)
        {
            sequence_number = buffer[offset++];
            proposed_window_number = buffer[offset++];
        }
        service = (BacnetConfirmedServices)buffer[offset++];

        return offset - org_offset;
    }

    public static void EncodeSegmentAck(EncodeBuffer buffer, BacnetPduTypes type, byte original_invoke_id, byte sequence_number, byte actual_window_size)
    {
        buffer.buffer[buffer.offset++] = (byte)type;
        buffer.buffer[buffer.offset++] = original_invoke_id;
        buffer.buffer[buffer.offset++] = sequence_number;
        buffer.buffer[buffer.offset++] = actual_window_size;
    }

    public static int DecodeSegmentAck(byte[] buffer, int offset, out BacnetPduTypes type, out byte original_invoke_id, out byte sequence_number, out byte actual_window_size)
    {
        int org_offset = offset;

        type = (BacnetPduTypes)buffer[offset++];
        original_invoke_id = buffer[offset++];
        sequence_number = buffer[offset++];
        actual_window_size = buffer[offset++];

        return offset - org_offset;
    }

    public static void EncodeError(EncodeBuffer buffer, BacnetPduTypes type, BacnetConfirmedServices service, byte invoke_id)
    {
        buffer.buffer[buffer.offset++] = (byte)type;
        buffer.buffer[buffer.offset++] = invoke_id;
        buffer.buffer[buffer.offset++] = (byte)service;
    }

    public static int DecodeError(byte[] buffer, int offset, out BacnetPduTypes type, out BacnetConfirmedServices service, out byte invoke_id)
    {
        int org_offset = offset;

        type = (BacnetPduTypes)buffer[offset++];
        invoke_id = buffer[offset++];
        service = (BacnetConfirmedServices)buffer[offset++];

        return offset - org_offset;
    }

    /// <summary>
    /// Also EncodeReject
    /// </summary>
    /// <param name="buffer"></param>
    /// <param name="offset"></param>
    /// <param name="type"></param>
    /// <param name="invoke_id"></param>
    /// <param name="reason"></param>
    /// <returns></returns>
    public static void EncodeAbort(EncodeBuffer buffer, BacnetPduTypes type, byte invoke_id, byte reason)
    {
        buffer.buffer[buffer.offset++] = (byte)type;
        buffer.buffer[buffer.offset++] = invoke_id;
        buffer.buffer[buffer.offset++] = reason;
    }

    public static int DecodeAbort(byte[] buffer, int offset, out BacnetPduTypes type, out byte invoke_id, out byte reason)
    {
        int org_offset = offset;

        type = (BacnetPduTypes)buffer[offset++];
        invoke_id = buffer[offset++];
        reason = buffer[offset++];

        return offset - org_offset;
    }
}