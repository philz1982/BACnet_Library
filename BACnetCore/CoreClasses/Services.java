package BACnetCore.CoreClasses;

/**
 * Created by Phil on 3/26/2016.
 */
public class Services
{
    public static void EncodeIamBroadcast(EncodeBuffer buffer, UInt32 device_id, uint max_apdu, BacnetSegmentations segmentation, UInt16 vendor_id)
    {
        ASN1.encode_application_object_id(buffer, BacnetObjectTypes.OBJECT_DEVICE, device_id);
        ASN1.encode_application_unsigned(buffer, max_apdu);
        ASN1.encode_application_enumerated(buffer, (uint)segmentation);
        ASN1.encode_application_unsigned(buffer, vendor_id);
    }

    public static int DecodeIamBroadcast(byte[] buffer, int offset, out UInt32 device_id, out UInt32 max_apdu, out BacnetSegmentations segmentation, out UInt16 vendor_id)
    {
        int len;
        int apdu_len = 0;
        int org_offset = offset;
        uint len_value;
        byte tag_number;
        uint decoded_value;
        BacnetObjectId object_id = new BacnetObjectId();

        device_id = 0;
        max_apdu = 0;
        segmentation = BacnetSegmentations.SEGMENTATION_NONE;
        vendor_id = 0;

            /* OBJECT ID - object id */
        len =
                ASN1.decode_tag_number_and_value(buffer, offset + apdu_len, out tag_number, out len_value);
        apdu_len += len;
        if (tag_number != (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_OBJECT_ID)
            return -1;
        len = ASN1.decode_object_id(buffer, offset + apdu_len, out object_id.type, out object_id.instance);
        apdu_len += len;
        if (object_id.type != BacnetObjectTypes.OBJECT_DEVICE)
            return -1;
        device_id = object_id.instance;
            /* MAX APDU - unsigned */
        len =
                ASN1.decode_tag_number_and_value(buffer, offset + apdu_len, out tag_number, out len_value);
        apdu_len += len;
        if (tag_number != (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_UNSIGNED_INT)
            return -1;
        len = ASN1.decode_unsigned(buffer, offset + apdu_len, len_value, out decoded_value);
        apdu_len += len;
        max_apdu = decoded_value;
            /* Segmentation - enumerated */
        len =
                ASN1.decode_tag_number_and_value(buffer, offset + apdu_len, out tag_number, out len_value);
        apdu_len += len;
        if (tag_number != (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_ENUMERATED)
            return -1;
        len = ASN1.decode_enumerated(buffer, offset + apdu_len, len_value, out decoded_value);
        apdu_len += len;
        if (decoded_value > (uint)BacnetSegmentations.SEGMENTATION_NONE)
            return -1;
        segmentation = (BacnetSegmentations)decoded_value;
            /* Vendor ID - unsigned16 */
        len =
                ASN1.decode_tag_number_and_value(buffer, offset + apdu_len, out tag_number, out len_value);
        apdu_len += len;
        if (tag_number != (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_UNSIGNED_INT)
            return -1;
        len = ASN1.decode_unsigned(buffer, offset + apdu_len, len_value, out decoded_value);
        apdu_len += len;
        if (decoded_value > 0xFFFF)
            return -1;
        vendor_id = (ushort)decoded_value;

        return offset - org_offset;
    }

    public static void EncodeIhaveBroadcast(EncodeBuffer buffer, BacnetObjectId device_id, BacnetObjectId object_id, string object_name)
    {
            /* deviceIdentifier */
        ASN1.encode_application_object_id(buffer, device_id.type, device_id.instance);
            /* objectIdentifier */
        ASN1.encode_application_object_id(buffer, object_id.type, object_id.instance);
            /* objectName */
        ASN1.encode_application_character_string(buffer, object_name);
    }

    public static void EncodeWhoHasBroadcast(EncodeBuffer buffer, int low_limit, int high_limit, BacnetObjectId object_id, string object_name)
    {
            /* optional limits - must be used as a pair */
        if ((low_limit >= 0) && (low_limit <= ASN1.BACNET_MAX_INSTANCE) && (high_limit >= 0) && (high_limit <= ASN1.BACNET_MAX_INSTANCE))
        {
            ASN1.encode_context_unsigned(buffer, 0, (uint)low_limit);
            ASN1.encode_context_unsigned(buffer, 1, (uint)high_limit);
        }
        if (!string.IsNullOrEmpty(object_name))
        {
            ASN1.encode_context_character_string(buffer, 3, object_name);
        }
        else
        {
            ASN1.encode_context_object_id(buffer, 2, object_id.type, object_id.instance);
        }
    }

    public static void EncodeWhoIsBroadcast(EncodeBuffer buffer, int low_limit, int high_limit)
    {
            /* optional limits - must be used as a pair */
        if ((low_limit >= 0) && (low_limit <= ASN1.BACNET_MAX_INSTANCE) &&
                (high_limit >= 0) && (high_limit <= ASN1.BACNET_MAX_INSTANCE))
        {
            ASN1.encode_context_unsigned(buffer, 0, (uint)low_limit);
            ASN1.encode_context_unsigned(buffer, 1, (uint)high_limit);
        }
    }

    public static int DecodeWhoIsBroadcast(byte[] buffer, int offset, int apdu_len, out int low_limit, out int high_limit)
    {
        int len = 0;
        byte tag_number;
        uint len_value;
        uint decoded_value;

        low_limit = -1;
        high_limit = -1;

        if (apdu_len <= 0) return len;

            /* optional limits - must be used as a pair */
        len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
        if (tag_number != 0)
            return -1;
        if (apdu_len > len)
        {
            len += ASN1.decode_unsigned(buffer, offset + len, len_value, out decoded_value);
            if (decoded_value <= ASN1.BACNET_MAX_INSTANCE)
                low_limit = (int)decoded_value;
            if (apdu_len > len)
            {
                len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
                if (tag_number != 1)
                    return -1;
                if (apdu_len > len)
                {
                    len += ASN1.decode_unsigned(buffer, offset + len, len_value, out decoded_value);
                    if (decoded_value <= ASN1.BACNET_MAX_INSTANCE)
                        high_limit = (int)decoded_value;
                }
                else
                    return -1;
            }
            else
                return -1;
        }
        else
            return -1;

        return len;
    }

    public static void EncodeAlarmAcknowledge(EncodeBuffer buffer, uint ackProcessIdentifier, BacnetObjectId eventObjectIdentifier, uint eventStateAcked, string ackSource, BacnetGenericTime eventTimeStamp, BacnetGenericTime ackTimeStamp)
    {
        ASN1.encode_context_unsigned(buffer, 0, ackProcessIdentifier);
        ASN1.encode_context_object_id(buffer, 1, eventObjectIdentifier.type, eventObjectIdentifier.instance);
        ASN1.encode_context_enumerated(buffer, 2, eventStateAcked);
        ASN1.bacapp_encode_context_timestamp(buffer, 3, eventTimeStamp);
        ASN1.encode_context_character_string(buffer, 4, ackSource);
        ASN1.bacapp_encode_context_timestamp(buffer, 5, ackTimeStamp);
    }

    public static void EncodeAtomicReadFile(EncodeBuffer buffer, bool is_stream, BacnetObjectId object_id, int position, uint count)
    {
        ASN1.encode_application_object_id(buffer, object_id.type, object_id.instance);
        switch (is_stream)
        {
            case true:
                ASN1.encode_opening_tag(buffer, 0);
                ASN1.encode_application_signed(buffer, position);
                ASN1.encode_application_unsigned(buffer, count);
                ASN1.encode_closing_tag(buffer, 0);
                break;
            case false:
                ASN1.encode_opening_tag(buffer, 1);
                ASN1.encode_application_signed(buffer, position);
                ASN1.encode_application_unsigned(buffer, count);
                ASN1.encode_closing_tag(buffer, 1);
                break;
            default:
                break;
        }
    }

    public static int DecodeAtomicReadFile(byte[] buffer, int offset, int apdu_len, out bool is_stream, out BacnetObjectId object_id, out int position, out uint count)
    {
        int len = 0;
        byte tag_number;
        uint len_value_type;
        int tag_len;

        is_stream = true;
        object_id = new BacnetObjectId();
        position = -1;
        count = 0;

        len = ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
        if (tag_number != (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_OBJECT_ID)
            return -1;
        len += ASN1.decode_object_id(buffer, offset + len, out object_id.type, out object_id.instance);
        if (ASN1.decode_is_opening_tag_number(buffer, offset + len, 0))
        {
            is_stream = true;
                /* a tag number is not extended so only one octet */
            len++;
                /* fileStartPosition */
            tag_len = ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
            len += tag_len;
            if (tag_number != (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_SIGNED_INT)
                return -1;
            len += ASN1.decode_signed(buffer, offset + len, len_value_type, out position);
                /* requestedOctetCount */
            tag_len = ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
            len += tag_len;
            if (tag_number != (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_UNSIGNED_INT)
                return -1;
            len += ASN1.decode_unsigned(buffer, offset + len, len_value_type, out count);
            if (!ASN1.decode_is_closing_tag_number(buffer, offset + len, 0))
                return -1;
                /* a tag number is not extended so only one octet */
            len++;
        }
        else if (ASN1.decode_is_opening_tag_number(buffer, offset + len, 1))
        {
            is_stream = false;
                /* a tag number is not extended so only one octet */
            len++;
                /* fileStartRecord */
            tag_len =
                    ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number,
                            out len_value_type);
            len += tag_len;
            if (tag_number != (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_SIGNED_INT)
                return -1;
            len += ASN1.decode_signed(buffer, offset + len, len_value_type, out position);
                /* RecordCount */
            tag_len = ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
            len += tag_len;
            if (tag_number != (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_UNSIGNED_INT)
                return -1;
            len += ASN1.decode_unsigned(buffer, offset + len, len_value_type, out count);
            if (!ASN1.decode_is_closing_tag_number(buffer, offset + len, 1))
                return -1;
                /* a tag number is not extended so only one octet */
            len++;
        }
        else
            return -1;

        return len;
    }

    public static void EncodeAtomicReadFileAcknowledge(EncodeBuffer buffer, bool is_stream, bool end_of_file, int position, uint block_count, byte[][] blocks, int[] counts)
    {
        ASN1.encode_application_boolean(buffer, end_of_file);
        switch (is_stream)
        {
            case true:
                ASN1.encode_opening_tag(buffer, 0);
                ASN1.encode_application_signed(buffer, position);
                ASN1.encode_application_octet_string(buffer, blocks[0], 0, counts[0]);
                ASN1.encode_closing_tag(buffer, 0);
                break;
            case false:
                ASN1.encode_opening_tag(buffer, 1);
                ASN1.encode_application_signed(buffer, position);
                ASN1.encode_application_unsigned(buffer, block_count);
                for (int i = 0; i < block_count; i++)
                    ASN1.encode_application_octet_string(buffer, blocks[i], 0, counts[i]);
                ASN1.encode_closing_tag(buffer, 1);
                break;
            default:
                break;
        }
    }

    public static void EncodeAtomicWriteFile(EncodeBuffer buffer, bool is_stream, BacnetObjectId object_id, int position, uint block_count, byte[][] blocks, int[] counts)
    {
        ASN1.encode_application_object_id(buffer, object_id.type, object_id.instance);
        switch (is_stream)
        {
            case true:
                ASN1.encode_opening_tag(buffer, 0);
                ASN1.encode_application_signed(buffer, position);
                ASN1.encode_application_octet_string(buffer, blocks[0], 0, counts[0]);
                ASN1.encode_closing_tag(buffer, 0);
                break;
            case false:
                ASN1.encode_opening_tag(buffer, 1);
                ASN1.encode_application_signed(buffer, position);
                ASN1.encode_application_unsigned(buffer, block_count);
                for (int i = 0; i < block_count; i++)
                    ASN1.encode_application_octet_string(buffer, blocks[i], 0, counts[i]);
                ASN1.encode_closing_tag(buffer, 1);
                break;
            default:
                break;
        }
    }

    public static int DecodeAtomicWriteFile(byte[] buffer, int offset, int apdu_len, out bool is_stream, out BacnetObjectId object_id, out int position, out uint block_count, out byte[][] blocks, out int[] counts)
    {
        int len = 0;
        byte tag_number;
        uint len_value_type;
        int i;
        int tag_len;

        object_id = new BacnetObjectId();
        is_stream = true;
        position = -1;
        block_count = 0;
        blocks = null;
        counts = null;

        len = ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
        if (tag_number != (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_OBJECT_ID)
            return -1;
        len += ASN1.decode_object_id(buffer, offset + len, out object_id.type, out object_id.instance);
        if (ASN1.decode_is_opening_tag_number(buffer, offset + len, 0))
        {
            is_stream = true;
                /* a tag number of 2 is not extended so only one octet */
            len++;
                /* fileStartPosition */
            tag_len = ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
            len += tag_len;
            if (tag_number != (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_SIGNED_INT)
                return -1;
            len += ASN1.decode_signed(buffer, offset + len, len_value_type, out position);
                /* fileData */
            tag_len = ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
            len += tag_len;
            if (tag_number != (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_OCTET_STRING)
                return -1;
            block_count = 1;
            blocks = new byte[1][];
            blocks[0] = new byte[len_value_type];
            counts = new int[] { (int)len_value_type };
            len += ASN1.decode_octet_string(buffer, offset + len, apdu_len, blocks[0], 0, len_value_type);
            if (!ASN1.decode_is_closing_tag_number(buffer, offset + len, 0))
                return -1;
                /* a tag number is not extended so only one octet */
            len++;
        }
        else if (ASN1.decode_is_opening_tag_number(buffer, offset + len, 1))
        {
            is_stream = false;
                /* a tag number is not extended so only one octet */
            len++;
                /* fileStartRecord */
            tag_len = ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
            len += tag_len;
            if (tag_number != (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_SIGNED_INT)
                return -1;
            len += ASN1.decode_signed(buffer, offset + len, len_value_type, out position);
                /* returnedRecordCount */
            tag_len = ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
            len += tag_len;
            if (tag_number != (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_UNSIGNED_INT)
                return -1;
            len += ASN1.decode_unsigned(buffer, offset + len, len_value_type, out block_count);
                /* fileData */
            blocks = new byte[block_count][];
            counts = new int[block_count];
            for (i = 0; i < block_count; i++)
            {
                tag_len = ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
                len += tag_len;
                blocks[i] = new byte[len_value_type];
                counts[i] = (int)len_value_type;
                if (tag_number != (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_OCTET_STRING)
                    return -1;
                len += ASN1.decode_octet_string(buffer, offset + len, apdu_len, blocks[i], 0, len_value_type);
            }
            if (!ASN1.decode_is_closing_tag_number(buffer, offset + len, 1))
                return -1;
                /* a tag number is not extended so only one octet */
            len++;
        }
        else
            return -1;

        return len;
    }

    //**********************************************************************************
    // by Christopher Günter
    public static void EncodeCreateProperty(EncodeBuffer buffer, BacnetObjectId object_id, ICollection<BacnetPropertyValue> value_list)
    {

            /* Tag 1: sequence of WriteAccessSpecification */
        ASN1.encode_opening_tag(buffer, 0);
        ASN1.encode_context_object_id(buffer, 1, object_id.type, object_id.instance);
        ASN1.encode_closing_tag(buffer, 0);

        ASN1.encode_opening_tag(buffer, 1);

        foreach (BacnetPropertyValue p_value in value_list)
        {

            ASN1.encode_context_enumerated(buffer, 0, p_value.property.propertyIdentifier);


            if (p_value.property.propertyArrayIndex != ASN1.BACNET_ARRAY_ALL)
                ASN1.encode_context_unsigned(buffer, 1, p_value.property.propertyArrayIndex);


            ASN1.encode_opening_tag(buffer, 2);
            foreach (BacnetValue value in p_value.value)
            {
                ASN1.bacapp_encode_application_data(buffer, value);
            }
            ASN1.encode_closing_tag(buffer, 2);


            if (p_value.priority != ASN1.BACNET_NO_PRIORITY)
                ASN1.encode_context_unsigned(buffer, 3, p_value.priority);
        }

        ASN1.encode_closing_tag(buffer, 1);

    }
    //***************************************************************
    public static void EncodeAddListElement(EncodeBuffer buffer, BacnetObjectId object_id, uint property_id, uint array_index, IList<BacnetValue> value_list)
    {
        ASN1.encode_context_object_id(buffer, 0, object_id.type, object_id.instance);
        ASN1.encode_context_enumerated(buffer, 1, property_id);


        if (array_index != ASN1.BACNET_ARRAY_ALL)
        {
            ASN1.encode_context_unsigned(buffer, 2, array_index);
        }


        ASN1.encode_opening_tag(buffer, 3);
        foreach (BacnetValue value in value_list)
        {
            ASN1.bacapp_encode_application_data(buffer, value);
        }
        ASN1.encode_closing_tag(buffer, 3);

    }

    public static void EncodeAtomicWriteFileAcknowledge(EncodeBuffer buffer, bool is_stream, int position)
    {
        switch (is_stream)
        {
            case true:
                ASN1.encode_context_signed(buffer, 0, position);
                break;
            case false:
                ASN1.encode_context_signed(buffer, 1, position);
                break;
            default:
                break;
        }
    }

    public static void EncodeCOVNotifyConfirmed(EncodeBuffer buffer, uint subscriberProcessIdentifier, uint initiatingDeviceIdentifier, BacnetObjectId monitoredObjectIdentifier, uint timeRemaining, IEnumerable<BacnetPropertyValue> values)
    {
            /* tag 0 - subscriberProcessIdentifier */
        ASN1.encode_context_unsigned(buffer, 0, subscriberProcessIdentifier);
            /* tag 1 - initiatingDeviceIdentifier */
        ASN1.encode_context_object_id(buffer, 1, BacnetObjectTypes.OBJECT_DEVICE, initiatingDeviceIdentifier);
            /* tag 2 - monitoredObjectIdentifier */
        ASN1.encode_context_object_id(buffer, 2, monitoredObjectIdentifier.type, monitoredObjectIdentifier.instance);
            /* tag 3 - timeRemaining */
        ASN1.encode_context_unsigned(buffer, 3, timeRemaining);
            /* tag 4 - listOfValues */
        ASN1.encode_opening_tag(buffer, 4);
        foreach (BacnetPropertyValue value in values)
        {
                /* tag 0 - propertyIdentifier */
            ASN1.encode_context_enumerated(buffer, 0, value.property.propertyIdentifier);
                /* tag 1 - propertyArrayIndex OPTIONAL */
            if (value.property.propertyArrayIndex != ASN1.BACNET_ARRAY_ALL)
            {
                ASN1.encode_context_unsigned(buffer, 1, value.property.propertyArrayIndex);
            }
                /* tag 2 - value */
                /* abstract syntax gets enclosed in a context tag */
            ASN1.encode_opening_tag(buffer, 2);
            foreach (BacnetValue v in value.value)
            {
                ASN1.bacapp_encode_application_data(buffer, v);
            }
            ASN1.encode_closing_tag(buffer, 2);
                /* tag 3 - priority OPTIONAL */
            if (value.priority != ASN1.BACNET_NO_PRIORITY)
            {
                ASN1.encode_context_unsigned(buffer, 3, value.priority);
            }
                /* is there another one to encode? */
                /* FIXME: check to see if there is room in the APDU */
        }
        ASN1.encode_closing_tag(buffer, 4);
    }

    public static void EncodeCOVNotifyUnconfirmed(EncodeBuffer buffer, uint subscriberProcessIdentifier, uint initiatingDeviceIdentifier, BacnetObjectId monitoredObjectIdentifier, uint timeRemaining, IEnumerable<BacnetPropertyValue> values)
    {
            /* tag 0 - subscriberProcessIdentifier */
        ASN1.encode_context_unsigned(buffer, 0, subscriberProcessIdentifier);
            /* tag 1 - initiatingDeviceIdentifier */
        ASN1.encode_context_object_id(buffer, 1, BacnetObjectTypes.OBJECT_DEVICE, initiatingDeviceIdentifier);
            /* tag 2 - monitoredObjectIdentifier */
        ASN1.encode_context_object_id(buffer, 2, monitoredObjectIdentifier.type, monitoredObjectIdentifier.instance);
            /* tag 3 - timeRemaining */
        ASN1.encode_context_unsigned(buffer, 3, timeRemaining);
            /* tag 4 - listOfValues */
        ASN1.encode_opening_tag(buffer, 4);
        foreach (BacnetPropertyValue value in values)
        {
                /* tag 0 - propertyIdentifier */
            ASN1.encode_context_enumerated(buffer, 0, value.property.propertyIdentifier);
                /* tag 1 - propertyArrayIndex OPTIONAL */
            if (value.property.propertyArrayIndex != ASN1.BACNET_ARRAY_ALL)
            {
                ASN1.encode_context_unsigned(buffer, 1, value.property.propertyArrayIndex);
            }
                /* tag 2 - value */
                /* abstract syntax gets enclosed in a context tag */
            ASN1.encode_opening_tag(buffer, 2);
            foreach (BacnetValue v in value.value)
            {
                ASN1.bacapp_encode_application_data(buffer, v);
            }
            ASN1.encode_closing_tag(buffer, 2);
                /* tag 3 - priority OPTIONAL */
            if (value.priority != ASN1.BACNET_NO_PRIORITY)
            {
                ASN1.encode_context_unsigned(buffer, 3, value.priority);
            }
                /* is there another one to encode? */
                /* FIXME: check to see if there is room in the APDU */
        }
        ASN1.encode_closing_tag(buffer, 4);
    }

    public static void EncodeSubscribeCOV(EncodeBuffer buffer, uint subscriberProcessIdentifier, BacnetObjectId monitoredObjectIdentifier, bool cancellationRequest, bool issueConfirmedNotifications, uint lifetime)
    {
            /* tag 0 - subscriberProcessIdentifier */
        ASN1.encode_context_unsigned(buffer, 0, subscriberProcessIdentifier);
            /* tag 1 - monitoredObjectIdentifier */
        ASN1.encode_context_object_id(buffer, 1, monitoredObjectIdentifier.type, monitoredObjectIdentifier.instance);
            /*
               If both the 'Issue Confirmed Notifications' and
               'Lifetime' parameters are absent, then this shall
               indicate a cancellation request.
             */
        if (!cancellationRequest)
        {
                /* tag 2 - issueConfirmedNotifications */
            ASN1.encode_context_boolean(buffer, 2, issueConfirmedNotifications);
                /* tag 3 - lifetime */
            ASN1.encode_context_unsigned(buffer, 3, lifetime);
        }
    }

    public static int DecodeSubscribeCOV(byte[] buffer, int offset, int apdu_len, out uint subscriberProcessIdentifier, out BacnetObjectId monitoredObjectIdentifier, out bool cancellationRequest, out bool issueConfirmedNotifications, out uint lifetime)
    {
        int len = 0;
        byte tag_number;
        uint len_value;

        subscriberProcessIdentifier = 0;
        monitoredObjectIdentifier = new BacnetObjectId();
        cancellationRequest = false;
        issueConfirmedNotifications = false;
        lifetime = 0;

            /* tag 0 - subscriberProcessIdentifier */
        if (ASN1.decode_is_context_tag(buffer, offset + len, 0))
        {
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            len += ASN1.decode_unsigned(buffer, offset + len, len_value, out subscriberProcessIdentifier);
        }
        else
            return -1;
            /* tag 1 - monitoredObjectIdentifier */
        if (ASN1.decode_is_context_tag(buffer, offset + len, 1))
        {
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            len += ASN1.decode_object_id(buffer, offset + len, out monitoredObjectIdentifier.type, out monitoredObjectIdentifier.instance);
        }
        else
            return -1;
            /* optional parameters - if missing, means cancellation */
        if (len < apdu_len)
        {
                /* tag 2 - issueConfirmedNotifications - optional */
            if (ASN1.decode_is_context_tag(buffer, offset + len, 2))
            {
                cancellationRequest = false;
                len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
                issueConfirmedNotifications = buffer[offset + len] > 0;
                len += (int)len_value;
            }
            else
            {
                cancellationRequest = true;
            }
                /* tag 3 - lifetime - optional */
            if (ASN1.decode_is_context_tag(buffer, offset + len, 3))
            {
                len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
                len += ASN1.decode_unsigned(buffer, offset + len, len_value, out lifetime);
            }
            else
            {
                lifetime = 0;
            }
        }
        else
        {
            cancellationRequest = true;
        }

        return len;
    }

    public static void EncodeSubscribeProperty(EncodeBuffer buffer, uint subscriberProcessIdentifier, BacnetObjectId monitoredObjectIdentifier, bool cancellationRequest, bool issueConfirmedNotifications, uint lifetime, BacnetPropertyReference monitoredProperty, bool covIncrementPresent, float covIncrement)
    {
            /* tag 0 - subscriberProcessIdentifier */
        ASN1.encode_context_unsigned(buffer, 0, subscriberProcessIdentifier);
            /* tag 1 - monitoredObjectIdentifier */
        ASN1.encode_context_object_id(buffer, 1, monitoredObjectIdentifier.type, monitoredObjectIdentifier.instance);
        if (!cancellationRequest)
        {
                /* tag 2 - issueConfirmedNotifications */
            ASN1.encode_context_boolean(buffer, 2, issueConfirmedNotifications);
                /* tag 3 - lifetime */
            ASN1.encode_context_unsigned(buffer, 3, lifetime);
        }
            /* tag 4 - monitoredPropertyIdentifier */
        ASN1.encode_opening_tag(buffer, 4);
        ASN1.encode_context_enumerated(buffer, 0, monitoredProperty.propertyIdentifier);
        if (monitoredProperty.propertyArrayIndex != ASN1.BACNET_ARRAY_ALL)
        {
            ASN1.encode_context_unsigned(buffer, 1, monitoredProperty.propertyArrayIndex);

        }
        ASN1.encode_closing_tag(buffer, 4);

            /* tag 5 - covIncrement */
        if (covIncrementPresent)
        {
            ASN1.encode_context_real(buffer, 5, covIncrement);
        }
    }

    public static int DecodeSubscribeProperty(byte[] buffer, int offset, int apdu_len, out uint subscriberProcessIdentifier, out BacnetObjectId monitoredObjectIdentifier, out BacnetPropertyReference monitoredProperty, out bool cancellationRequest, out bool issueConfirmedNotifications, out uint lifetime, out float covIncrement)
    {
        int len = 0;
        byte tag_number;
        uint len_value;
        uint decoded_value;

        subscriberProcessIdentifier = 0;
        monitoredObjectIdentifier = new BacnetObjectId();
        cancellationRequest = false;
        issueConfirmedNotifications = false;
        lifetime = 0;
        covIncrement = 0;
        monitoredProperty = new BacnetPropertyReference();

            /* tag 0 - subscriberProcessIdentifier */
        if (ASN1.decode_is_context_tag(buffer, offset + len, 0))
        {
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            len += ASN1.decode_unsigned(buffer, offset + len, len_value, out subscriberProcessIdentifier);
        }
        else
            return -1;

            /* tag 1 - monitoredObjectIdentifier */
        if (ASN1.decode_is_context_tag(buffer, offset + len, 1))
        {
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            len += ASN1.decode_object_id(buffer, offset + len, out monitoredObjectIdentifier.type, out monitoredObjectIdentifier.instance);
        }
        else
            return -1;

            /* tag 2 - issueConfirmedNotifications - optional */
        if (ASN1.decode_is_context_tag(buffer, offset + len, 2))
        {
            cancellationRequest = false;
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            issueConfirmedNotifications = buffer[offset + len] > 0;
            len++;
        }
        else
        {
            cancellationRequest = true;
        }

            /* tag 3 - lifetime - optional */
        if (ASN1.decode_is_context_tag(buffer, offset + len, 3))
        {
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            len += ASN1.decode_unsigned(buffer, offset + len, len_value, out lifetime);
        }
        else
        {
            lifetime = 0;
        }

            /* tag 4 - monitoredPropertyIdentifier */
        if (!ASN1.decode_is_opening_tag_number(buffer, offset + len, 4))
            return -1;

            /* a tag number of 4 is not extended so only one octet */
        len++;
            /* the propertyIdentifier is tag 0 */
        if (ASN1.decode_is_context_tag(buffer, offset + len, 0))
        {
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            len += ASN1.decode_enumerated(buffer, offset + len, len_value, out decoded_value);
            monitoredProperty.propertyIdentifier = decoded_value;
        }
        else
            return -1;

            /* the optional array index is tag 1 */
        if (ASN1.decode_is_context_tag(buffer, offset + len, 1))
        {
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            len += ASN1.decode_unsigned(buffer, offset + len, len_value, out decoded_value);
            monitoredProperty.propertyArrayIndex = decoded_value;
        }
        else
        {
            monitoredProperty.propertyArrayIndex = ASN1.BACNET_ARRAY_ALL;
        }

        if (!ASN1.decode_is_closing_tag_number(buffer, offset + len, 4))
            return -1;

            /* a tag number of 4 is not extended so only one octet */
        len++;
            /* tag 5 - covIncrement - optional */
        if (ASN1.decode_is_context_tag(buffer, offset + len, 5))
        {
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            len += ASN1.decode_real(buffer, offset + len, out covIncrement);
        }
        else
        {
            covIncrement = 0;
        }

        return len;
    }

    // F Chaxel
    public static int DecodeEventNotifyData(byte[] buffer, int offset, int apdu_len, out BacnetEventNotificationData EventData)
    {
        int len = 0;
        uint len_value;
        byte tag_number;

        EventData = new BacnetEventNotificationData();

            /* tag 0 - processIdentifier */
        if (ASN1.decode_is_context_tag(buffer, offset + len, 0))
        {
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            len += ASN1.decode_unsigned(buffer, offset + len, len_value, out EventData.processIdentifier);
        }
        else
            return -1;

            /*  tag 1 - initiatingObjectIdentifier */
        if (ASN1.decode_is_context_tag(buffer, offset + len, 1))
        {
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            len += ASN1.decode_object_id(buffer, offset + len, out EventData.initiatingObjectIdentifier.type, out EventData.initiatingObjectIdentifier.instance);
        }
        else
            return -1;

            /*  tag 2 - eventObjectIdentifier */
        if (ASN1.decode_is_context_tag(buffer, offset + len, 2))
        {
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            len += ASN1.decode_object_id(buffer, offset + len, out EventData.eventObjectIdentifier.type, out EventData.eventObjectIdentifier.instance);
        }
        else
            return -1;

            /*  tag 3 - timeStamp */
        if (ASN1.decode_is_context_tag(buffer, offset + len, 3))
        {
            DateTime date;
            DateTime time;

            len += 2; // opening Tag 3 then 2

            len += ASN1.decode_application_date(buffer, offset + len, out date);
            len += ASN1.decode_application_time(buffer, offset + len, out time);
            EventData.timeStamp.Time = new DateTime(date.Year, date.Month, date.Day, time.Hour, time.Minute, time.Second, time.Millisecond);

            len += 2; // closing tag 2 then 3
        }
        else
            return -1;

            /* tag 4 - noticicationClass */
        if (ASN1.decode_is_context_tag(buffer, offset + len, 4))
        {
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            len += ASN1.decode_unsigned(buffer, offset + len, len_value, out EventData.notificationClass);
        }
        else
            return -1;

            /* tag 5 - priority */
        if (ASN1.decode_is_context_tag(buffer, offset + len, 5))
        {
            uint priority;

            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            len += ASN1.decode_unsigned(buffer, offset + len, len_value, out priority);
            if (priority > 0xFF) return -1;
            EventData.priority = (byte)priority;
        }
        else
            return -1;

            /* tag 6 - eventType */
        if (ASN1.decode_is_context_tag(buffer, offset + len, 6))
        {
            uint eventType;
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            len += ASN1.decode_enumerated(buffer, offset + len, len_value, out eventType);
            EventData.eventType = (BacnetEventNotificationData.BacnetEventTypes)eventType;
        }
        else
            return -1;

            /* optional tag 7 - messageText  : never tested */
        if (ASN1.decode_is_context_tag(buffer, offset + len, 7))
        {
            // max_lenght 20000 sound like a joke
            len += ASN1.decode_context_character_string(buffer, offset + len, 20000, 7, out EventData.messageText);
        }

            /* tag 8 - notifyType */
        if (ASN1.decode_is_context_tag(buffer, offset + len, 8))
        {
            uint notifyType;
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            len += ASN1.decode_enumerated(buffer, offset + len, len_value, out notifyType);
            EventData.notifyType = (BacnetEventNotificationData.BacnetNotifyTypes)notifyType;
        }
        else
            return -1;

        switch (EventData.notifyType)
        {
            case BacnetEventNotificationData.BacnetNotifyTypes.NOTIFY_ALARM:
            case BacnetEventNotificationData.BacnetNotifyTypes.NOTIFY_EVENT:
                    /* tag 9 - ackRequired */
                byte val;
                uint fromstate;

                len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
                len += ASN1.decode_unsigned8(buffer, offset + len, out val);
                EventData.ackRequired = Convert.ToBoolean(val);

                    /* tag 10 - fromState */
                len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
                len += ASN1.decode_enumerated(buffer, offset + len, len_value, out fromstate);
                EventData.fromState = (BacnetEventNotificationData.BacnetEventStates)fromstate;
                break;
            default:
                break;
        }

            /* tag 11 - toState */
        if (ASN1.decode_is_context_tag(buffer, offset + len, 11))
        {
            uint toState;
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            len += ASN1.decode_enumerated(buffer, offset + len, len_value, out toState);
            EventData.toState = (BacnetEventNotificationData.BacnetEventStates)toState;
        }
        else
            return -1;

        // some work to do for Tag 12
        // somebody want to do it ?

        return len;

    }
    private static void EncodeEventNotifyData(EncodeBuffer buffer, BacnetEventNotificationData data)
    {
            /* tag 0 - processIdentifier */
        ASN1.encode_context_unsigned(buffer, 0, data.processIdentifier);
            /* tag 1 - initiatingObjectIdentifier */
        ASN1.encode_context_object_id(buffer, 1, data.initiatingObjectIdentifier.type, data.initiatingObjectIdentifier.instance);

            /* tag 2 - eventObjectIdentifier */
        ASN1.encode_context_object_id(buffer, 2, data.eventObjectIdentifier.type, data.eventObjectIdentifier.instance);

            /* tag 3 - timeStamp */
        ASN1.bacapp_encode_context_timestamp(buffer, 3, data.timeStamp);

            /* tag 4 - noticicationClass */
        ASN1.encode_context_unsigned(buffer, 4, data.notificationClass);

            /* tag 5 - priority */
        ASN1.encode_context_unsigned(buffer, 5, data.priority);

            /* tag 6 - eventType */
        ASN1.encode_context_enumerated(buffer, 6, (uint)data.eventType);

            /* tag 7 - messageText */
        if (!string.IsNullOrEmpty(data.messageText))
            ASN1.encode_context_character_string(buffer, 7, data.messageText);

            /* tag 8 - notifyType */
        ASN1.encode_context_enumerated(buffer, 8, (uint)data.notifyType);

        switch (data.notifyType)
        {
            case BacnetEventNotificationData.BacnetNotifyTypes.NOTIFY_ALARM:
            case BacnetEventNotificationData.BacnetNotifyTypes.NOTIFY_EVENT:
                    /* tag 9 - ackRequired */
                ASN1.encode_context_boolean(buffer, 9, data.ackRequired);

                    /* tag 10 - fromState */
                ASN1.encode_context_enumerated(buffer, 10, (uint)data.fromState);
                break;
            default:
                break;
        }

            /* tag 11 - toState */
        ASN1.encode_context_enumerated(buffer, 11, (uint)data.toState);

        switch (data.notifyType)
        {
            case BacnetEventNotificationData.BacnetNotifyTypes.NOTIFY_ALARM:
            case BacnetEventNotificationData.BacnetNotifyTypes.NOTIFY_EVENT:
                    /* tag 12 - event values */
                ASN1.encode_opening_tag(buffer, 12);

                switch (data.eventType)
                {
                    case BacnetEventNotificationData.BacnetEventTypes.EVENT_CHANGE_OF_BITSTRING:
                        ASN1.encode_opening_tag(buffer, 0);
                        ASN1.encode_context_bitstring(buffer, 0, data.changeOfBitstring_referencedBitString);
                        ASN1.encode_context_bitstring(buffer, 1, data.changeOfBitstring_statusFlags);
                        ASN1.encode_closing_tag(buffer, 0);
                        break;

                    case BacnetEventNotificationData.BacnetEventTypes.EVENT_CHANGE_OF_STATE:
                        ASN1.encode_opening_tag(buffer, 1);
                        ASN1.encode_opening_tag(buffer, 0);
                        ASN1.bacapp_encode_property_state(buffer, data.changeOfState_newState);
                        ASN1.encode_closing_tag(buffer, 0);
                        ASN1.encode_context_bitstring(buffer, 1, data.changeOfState_statusFlags);
                        ASN1.encode_closing_tag(buffer, 1);
                        break;

                    case BacnetEventNotificationData.BacnetEventTypes.EVENT_CHANGE_OF_VALUE:
                        ASN1.encode_opening_tag(buffer, 2);
                        ASN1.encode_opening_tag(buffer, 0);

                        switch (data.changeOfValue_tag)
                        {
                            case BacnetEventNotificationData.BacnetCOVTypes.CHANGE_OF_VALUE_REAL:
                                ASN1.encode_context_real(buffer, 1, data.changeOfValue_changeValue);
                                break;
                            case BacnetEventNotificationData.BacnetCOVTypes.CHANGE_OF_VALUE_BITS:
                                ASN1.encode_context_bitstring(buffer, 0, data.changeOfValue_changedBits);
                                break;
                            default:
                                throw new Exception("Hmm?");
                        }

                        ASN1.encode_closing_tag(buffer, 0);
                        ASN1.encode_context_bitstring(buffer, 1, data.changeOfValue_statusFlags);
                        ASN1.encode_closing_tag(buffer, 2);
                        break;

                    case BacnetEventNotificationData.BacnetEventTypes.EVENT_FLOATING_LIMIT:
                        ASN1.encode_opening_tag(buffer, 4);
                        ASN1.encode_context_real(buffer, 0, data.floatingLimit_referenceValue);
                        ASN1.encode_context_bitstring(buffer, 1, data.floatingLimit_statusFlags);
                        ASN1.encode_context_real(buffer, 2, data.floatingLimit_setPointValue);
                        ASN1.encode_context_real(buffer, 3, data.floatingLimit_errorLimit);
                        ASN1.encode_closing_tag(buffer, 4);
                        break;

                    case BacnetEventNotificationData.BacnetEventTypes.EVENT_OUT_OF_RANGE:
                        ASN1.encode_opening_tag(buffer, 5);
                        ASN1.encode_context_real(buffer, 0, data.outOfRange_exceedingValue);
                        ASN1.encode_context_bitstring(buffer, 1, data.outOfRange_statusFlags);
                        ASN1.encode_context_real(buffer, 2, data.outOfRange_deadband);
                        ASN1.encode_context_real(buffer, 3, data.outOfRange_exceededLimit);
                        ASN1.encode_closing_tag(buffer, 5);
                        break;

                    case BacnetEventNotificationData.BacnetEventTypes.EVENT_CHANGE_OF_LIFE_SAFETY:
                        ASN1.encode_opening_tag(buffer, 8);
                        ASN1.encode_context_enumerated(buffer, 0, (uint)data.changeOfLifeSafety_newState);
                        ASN1.encode_context_enumerated(buffer, 1, (uint)data.changeOfLifeSafety_newMode);
                        ASN1.encode_context_bitstring(buffer, 2, data.changeOfLifeSafety_statusFlags);
                        ASN1.encode_context_enumerated(buffer, 3, (uint)data.changeOfLifeSafety_operationExpected);
                        ASN1.encode_closing_tag(buffer, 8);
                        break;

                    case BacnetEventNotificationData.BacnetEventTypes.EVENT_BUFFER_READY:
                        ASN1.encode_opening_tag(buffer, 10);
                        ASN1.bacapp_encode_context_device_obj_property_ref(buffer, 0, data.bufferReady_bufferProperty);
                        ASN1.encode_context_unsigned(buffer, 1, data.bufferReady_previousNotification);
                        ASN1.encode_context_unsigned(buffer, 2, data.bufferReady_currentNotification);
                        ASN1.encode_closing_tag(buffer, 10);

                        break;
                    case BacnetEventNotificationData.BacnetEventTypes.EVENT_UNSIGNED_RANGE:
                        ASN1.encode_opening_tag(buffer, 11);
                        ASN1.encode_context_unsigned(buffer, 0, data.unsignedRange_exceedingValue);
                        ASN1.encode_context_bitstring(buffer, 1, data.unsignedRange_statusFlags);
                        ASN1.encode_context_unsigned(buffer, 2, data.unsignedRange_exceededLimit);
                        ASN1.encode_closing_tag(buffer, 11);
                        break;
                    case BacnetEventNotificationData.BacnetEventTypes.EVENT_EXTENDED:
                    case BacnetEventNotificationData.BacnetEventTypes.EVENT_COMMAND_FAILURE:
                    default:
                        throw new NotImplementedException();
                }
                ASN1.encode_closing_tag(buffer, 12);
                break;
            case BacnetEventNotificationData.BacnetNotifyTypes.NOTIFY_ACK_NOTIFICATION:
                /* FIXME: handle this case */
            default:
                break;
        }
    }

    public static void EncodeEventNotifyConfirmed(EncodeBuffer buffer, BacnetEventNotificationData data)
    {
        EncodeEventNotifyData(buffer, data);
    }

    public static void EncodeEventNotifyUnconfirmed(EncodeBuffer buffer, BacnetEventNotificationData data)
    {
        EncodeEventNotifyData(buffer, data);
    }

    public static void EncodeAlarmSummary(EncodeBuffer buffer, BacnetObjectId objectIdentifier, uint alarmState, BacnetBitString acknowledgedTransitions)
    {
            /* tag 0 - Object Identifier */
        ASN1.encode_application_object_id(buffer, objectIdentifier.type, objectIdentifier.instance);
            /* tag 1 - Alarm State */
        ASN1.encode_application_enumerated(buffer, alarmState);
            /* tag 2 - Acknowledged Transitions */
        ASN1.encode_application_bitstring(buffer, acknowledgedTransitions);
    }

    // FChaxel
    public static int DecodeAlarmSummaryOrEvent(byte[] buffer, int offset, int apdu_len, bool GetEvent, ref IList<BacnetGetEventInformationData> Alarms, out bool MoreEvent)
    {
        int len = 0; ;

        if (GetEvent) len++;  // peut être tag 0

        while ((apdu_len - 3 - len) > 0)
        {
            byte tag_number = 0;
            uint len_value = 0;
            uint tmp;

            BacnetGetEventInformationData value = new BacnetGetEventInformationData();

            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            len += ASN1.decode_object_id(buffer, offset + len, out value.objectIdentifier.type, out value.objectIdentifier.instance);
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            len += ASN1.decode_enumerated(buffer, offset + len, len_value, out tmp);
            value.eventState = (BacnetEventNotificationData.BacnetEventStates)tmp;
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            len += ASN1.decode_bitstring(buffer, offset + len, len_value, out value.acknowledgedTransitions);

            if (GetEvent)
            {
                len++;  // opening Tag 3
                value.eventTimeStamps = new BacnetGenericTime[3];

                for (int i = 0; i < 3; i++)
                {
                    DateTime dt1, dt2;

                    len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value); // opening tag

                    if (tag_number != (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_NULL)
                    {
                        len += ASN1.decode_application_date(buffer, offset + len, out dt1);
                        len += ASN1.decode_application_time(buffer, offset + len, out dt2);
                        // oh ... a strange way to do that !
                        DateTime dt = Convert.ToDateTime(dt1.ToString().Split(' ')[0] + " " + dt2.ToString().Split(' ')[1]);
                        value.eventTimeStamps[i] = new BacnetGenericTime(dt, BacnetTimestampTags.TIME_STAMP_DATETIME);
                        len++; // closing tag
                    }
                    else
                        len += (int)len_value;


                }
                len++;  // closing Tag 3

                len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
                len += ASN1.decode_enumerated(buffer, offset + len, len_value, out tmp);
                value.notifyType = (BacnetEventNotificationData.BacnetNotifyTypes)tmp;

                len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
                len += ASN1.decode_bitstring(buffer, offset + len, len_value, out value.eventEnable);

                len++; // opening tag 6;
                value.eventPriorities = new uint[3];
                for (int i = 0; i < 3; i++)
                {
                    len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
                    len += ASN1.decode_unsigned(buffer, offset + len, len_value, out value.eventPriorities[i]);
                }
                len++;  // closing Tag 6
            }

            Alarms.Add(value);

        }

        if (GetEvent)
            MoreEvent = (buffer[apdu_len - 1] == 1);
        else
            MoreEvent = false;

        return len;
    }

    public static void EncodeGetEventInformation(EncodeBuffer buffer, bool send_last, BacnetObjectId lastReceivedObjectIdentifier)
    {
            /* encode optional parameter */
        if (send_last)
            ASN1.encode_context_object_id(buffer, 0, lastReceivedObjectIdentifier.type, lastReceivedObjectIdentifier.instance);
    }

    public static void EncodeGetEventInformationAcknowledge(EncodeBuffer buffer, BacnetGetEventInformationData[] events, bool moreEvents)
    {
            /* service ack follows */
            /* Tag 0: listOfEventSummaries */
        ASN1.encode_opening_tag(buffer, 0);
        foreach (BacnetGetEventInformationData event_data in events)
        {
                /* Tag 0: objectIdentifier */
            ASN1.encode_context_object_id(buffer, 0, event_data.objectIdentifier.type, event_data.objectIdentifier.instance);
                /* Tag 1: eventState */
            ASN1.encode_context_enumerated(buffer, 1, (uint)event_data.eventState);
                /* Tag 2: acknowledgedTransitions */
            ASN1.encode_context_bitstring(buffer, 2, event_data.acknowledgedTransitions);
                /* Tag 3: eventTimeStamps */
            ASN1.encode_opening_tag(buffer, 3);
            for (int i = 0; i < 3; i++)
            {
                ASN1.bacapp_encode_timestamp(buffer, event_data.eventTimeStamps[i]);
            }
            ASN1.encode_closing_tag(buffer, 3);
                /* Tag 4: notifyType */
            ASN1.encode_context_enumerated(buffer, 4, (uint)event_data.notifyType);
                /* Tag 5: eventEnable */
            ASN1.encode_context_bitstring(buffer, 5, event_data.eventEnable);
                /* Tag 6: eventPriorities */
            ASN1.encode_opening_tag(buffer, 6);
            for (int i = 0; i < 3; i++)
            {
                ASN1.encode_application_unsigned(buffer, event_data.eventPriorities[i]);
            }
            ASN1.encode_closing_tag(buffer, 6);
        }
        ASN1.encode_closing_tag(buffer, 0);
        ASN1.encode_context_boolean(buffer, 1, moreEvents);
    }

    public static void EncodeLifeSafetyOperation(EncodeBuffer buffer, uint processId, string requestingSrc, uint operation, BacnetObjectId targetObject)
    {
            /* tag 0 - requestingProcessId */
        ASN1.encode_context_unsigned(buffer, 0, processId);
            /* tag 1 - requestingSource */
        ASN1.encode_context_character_string(buffer, 1, requestingSrc);
            /* Operation */
        ASN1.encode_context_enumerated(buffer, 2, operation);
            /* Object ID */
        ASN1.encode_context_object_id(buffer, 3, targetObject.type, targetObject.instance);
    }

    public static void EncodePrivateTransferConfirmed(EncodeBuffer buffer, uint vendorID, uint serviceNumber, byte[] data)
    {
        ASN1.encode_context_unsigned(buffer, 0, vendorID);
        ASN1.encode_context_unsigned(buffer, 1, serviceNumber);
        ASN1.encode_opening_tag(buffer, 2);
        buffer.Add(data, data.Length);
        ASN1.encode_closing_tag(buffer, 2);
    }

    public static void EncodePrivateTransferUnconfirmed(EncodeBuffer buffer, uint vendorID, uint serviceNumber, byte[] data)
    {
        ASN1.encode_context_unsigned(buffer, 0, vendorID);
        ASN1.encode_context_unsigned(buffer, 1, serviceNumber);
        ASN1.encode_opening_tag(buffer, 2);
        buffer.Add(data, data.Length);
        ASN1.encode_closing_tag(buffer, 2);
    }

    public static void EncodePrivateTransferAcknowledge(EncodeBuffer buffer, uint vendorID, uint serviceNumber, byte[] data)
    {
        ASN1.encode_context_unsigned(buffer, 0, vendorID);
        ASN1.encode_context_unsigned(buffer, 1, serviceNumber);
        ASN1.encode_opening_tag(buffer, 2);
        buffer.Add(data, data.Length);
        ASN1.encode_closing_tag(buffer, 2);
    }

    public static void EncodeDeviceCommunicationControl(EncodeBuffer buffer, uint timeDuration, uint enable_disable, string password)
    {
            /* optional timeDuration */
        if (timeDuration > 0)
            ASN1.encode_context_unsigned(buffer, 0, timeDuration);

            /* enable disable */
        ASN1.encode_context_enumerated(buffer, 1, enable_disable);

            /* optional password */
        if (!string.IsNullOrEmpty(password))
        {
                /* FIXME: must be at least 1 character, limited to 20 characters */
            ASN1.encode_context_character_string(buffer, 2, password);
        }
    }

    public static int DecodeDeviceCommunicationControl(byte[] buffer, int offset, int apdu_len, out uint timeDuration, out uint enable_disable, out string password)
    {
        int len = 0;
        byte tag_number = 0;
        uint len_value_type = 0;

        timeDuration = 0;
        enable_disable = 0;
        password = "";

            /* Tag 0: timeDuration, in minutes --optional--
             * But if not included, take it as indefinite,
             * which we return as "very large" */
        if (ASN1.decode_is_context_tag(buffer, offset + len, 0))
        {
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
            len += ASN1.decode_unsigned(buffer, offset + len, len_value_type, out timeDuration);
        }

            /* Tag 1: enable_disable */
        if (!ASN1.decode_is_context_tag(buffer, offset + len, 1))
            return -1;
        len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
        len += ASN1.decode_enumerated(buffer, offset + len, len_value_type, out enable_disable);

            /* Tag 2: password --optional-- */
        if (len < apdu_len)
        {
            if (!ASN1.decode_is_context_tag(buffer, offset + len, 2))
                return -1;
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
            len += ASN1.decode_character_string(buffer, offset + len, apdu_len - (offset + len), len_value_type, out password);
        }

        return len;
    }

    public static void EncodeReinitializeDevice(EncodeBuffer buffer, BacnetReinitializedStates state, string password)
    {
        ASN1.encode_context_enumerated(buffer, 0, (uint)state);

            /* optional password */
        if (!string.IsNullOrEmpty(password))
        {
                /* FIXME: must be at least 1 character, limited to 20 characters */
            ASN1.encode_context_character_string(buffer, 1, password);
        }
    }

    public static int DecodeReinitializeDevice(byte[] buffer, int offset, int apdu_len, out BacnetReinitializedStates state, out string password)
    {
        int len = 0;
        byte tag_number = 0;
        uint len_value_type = 0;
        uint value;

        state = BacnetReinitializedStates.BACNET_REINIT_IDLE;
        password = "";

            /* Tag 0: reinitializedStateOfDevice */
        if (!ASN1.decode_is_context_tag(buffer, offset + len, 0))
            return -1;
        len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
        len += ASN1.decode_enumerated(buffer, offset + len, len_value_type, out value);
        state = (BacnetReinitializedStates)value;
            /* Tag 1: password - optional */
        if (len < apdu_len)
        {
            if (!ASN1.decode_is_context_tag(buffer, offset + len, 1))
                return -1;
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
            len += ASN1.decode_character_string(buffer, offset + len, apdu_len - (offset + len), len_value_type, out password);
        }

        return len;
    }

    public static void EncodeReadRange(EncodeBuffer buffer, BacnetObjectId object_id, uint property_id, uint arrayIndex, BacnetReadRangeRequestTypes requestType, uint position, DateTime time, int count)
    {
        ASN1.encode_context_object_id(buffer, 0, object_id.type, object_id.instance);
        ASN1.encode_context_enumerated(buffer, 1, property_id);

            /* optional array index */
        if (arrayIndex != ASN1.BACNET_ARRAY_ALL)
        {
            ASN1.encode_context_unsigned(buffer, 2, arrayIndex);
        }

            /* Build the appropriate (optional) range parameter based on the request type */
        switch (requestType)
        {
            case BacnetReadRangeRequestTypes.RR_BY_POSITION:
                ASN1.encode_opening_tag(buffer, 3);
                ASN1.encode_application_unsigned(buffer, position);
                ASN1.encode_application_signed(buffer, count);
                ASN1.encode_closing_tag(buffer, 3);
                break;

            case BacnetReadRangeRequestTypes.RR_BY_SEQUENCE:
                ASN1.encode_opening_tag(buffer, 6);
                ASN1.encode_application_unsigned(buffer, position);
                ASN1.encode_application_signed(buffer, count);
                ASN1.encode_closing_tag(buffer, 6);
                break;

            case BacnetReadRangeRequestTypes.RR_BY_TIME:
                ASN1.encode_opening_tag(buffer, 7);
                ASN1.encode_application_date(buffer, time);
                ASN1.encode_application_time(buffer, time);
                ASN1.encode_application_signed(buffer, count);
                ASN1.encode_closing_tag(buffer, 7);
                break;

            case BacnetReadRangeRequestTypes.RR_READ_ALL:  /* to attempt a read of the whole array or list, omit the range parameter */
                break;

            default:
                break;
        }
    }

    public static int DecodeReadRange(byte[] buffer, int offset, int apdu_len, out BacnetObjectId object_id, out BacnetPropertyReference property, out BacnetReadRangeRequestTypes requestType, out uint position, out DateTime time, out int count)
    {
        int len = 0;
        ushort type = 0;
        byte tag_number = 0;
        uint len_value_type = 0;

        object_id = new BacnetObjectId();
        property = new BacnetPropertyReference();
        requestType = BacnetReadRangeRequestTypes.RR_READ_ALL;
        position = 0;
        time = new DateTime(1, 1, 1);
        count = -1;

            /* Tag 0: Object ID          */
        if (!ASN1.decode_is_context_tag(buffer, offset + len, 0))
            return -1;
        len++;
        len += ASN1.decode_object_id(buffer, offset + len, out type, out object_id.instance);
        object_id.type = (BacnetObjectTypes)type;
            /* Tag 1: Property ID */
        len +=
                ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
        if (tag_number != 1)
            return -1;
        len += ASN1.decode_enumerated(buffer, offset + len, len_value_type, out property.propertyIdentifier);

            /* Tag 2: Optional Array Index */
        if (len < apdu_len && ASN1.decode_is_context_tag(buffer, offset + len, 0))
        {
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
            len += ASN1.decode_unsigned(buffer, offset + len, len_value_type, out property.propertyArrayIndex);
        }
        else
            property.propertyArrayIndex = ASN1.BACNET_ARRAY_ALL;

            /* optional request type */
        if (len < apdu_len)
        {
            len += ASN1.decode_tag_number(buffer, offset + len, out tag_number);    //opening tag
            switch (tag_number)
            {
                case 3:
                    requestType = BacnetReadRangeRequestTypes.RR_BY_POSITION;
                    len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
                    len += ASN1.decode_unsigned(buffer, offset + len, len_value_type, out position);
                    len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
                    len += ASN1.decode_signed(buffer, offset + len, len_value_type, out count);
                    break;
                case 6:
                    requestType = BacnetReadRangeRequestTypes.RR_BY_SEQUENCE;
                    len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
                    len += ASN1.decode_unsigned(buffer, offset + len, len_value_type, out position);
                    len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
                    len += ASN1.decode_signed(buffer, offset + len, len_value_type, out count);
                    break;
                case 7:
                    requestType = BacnetReadRangeRequestTypes.RR_BY_TIME;
                    DateTime date;
                    len += ASN1.decode_application_date(buffer, offset + len, out date);
                    len += ASN1.decode_application_time(buffer, offset + len, out time);
                    time = new DateTime(date.Year, date.Month, date.Day, time.Hour, time.Minute, time.Second, time.Millisecond);
                    len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
                    len += ASN1.decode_signed(buffer, offset + len, len_value_type, out count);
                    break;
                default:
                    return -1;  //don't know this type yet
            }
            len += ASN1.decode_tag_number(buffer, offset + len, out tag_number);    //closing tag
        }
        return len;
    }

    public static void EncodeReadRangeAcknowledge(EncodeBuffer buffer, BacnetObjectId object_id, uint property_id, uint arrayIndex, BacnetBitString ResultFlags, uint ItemCount, byte[] application_data, BacnetReadRangeRequestTypes requestType, uint FirstSequence)
    {
            /* service ack follows */
        ASN1.encode_context_object_id(buffer, 0, object_id.type, object_id.instance);
        ASN1.encode_context_enumerated(buffer, 1, property_id);
            /* context 2 array index is optional */
        if (arrayIndex != ASN1.BACNET_ARRAY_ALL)
        {
            ASN1.encode_context_unsigned(buffer, 2, arrayIndex);
        }
            /* Context 3 BACnet Result Flags */
        ASN1.encode_context_bitstring(buffer, 3, ResultFlags);
            /* Context 4 Item Count */
        ASN1.encode_context_unsigned(buffer, 4, ItemCount);
            /* Context 5 Property list - reading the standard it looks like an empty list still
             * requires an opening and closing tag as the tagged parameter is not optional
             */
        ASN1.encode_opening_tag(buffer, 5);
        if (ItemCount != 0)
        {
            buffer.Add(application_data, application_data.Length);
        }
        ASN1.encode_closing_tag(buffer, 5);

        if ((ItemCount != 0) && (requestType != BacnetReadRangeRequestTypes.RR_BY_POSITION) && (requestType != BacnetReadRangeRequestTypes.RR_READ_ALL))
        {
                /* Context 6 Sequence number of first item */
            ASN1.encode_context_unsigned(buffer, 6, FirstSequence);
        }
    }

    // FC
    public static uint DecodeReadRangeAcknowledge(byte[] buffer, int offset, int apdu_len, out byte[] RangeBuffer)
    {
        int len = 0;
        ushort type = 0;
        byte tag_number;
        uint len_value_type = 0;

        BacnetObjectId object_id = new BacnetObjectId();
        BacnetPropertyReference property = new BacnetPropertyReference();
        BacnetBitString ResultFlag;
        uint ItemCount;

        RangeBuffer = null;

            /* Tag 0: Object ID          */
        if (!ASN1.decode_is_context_tag(buffer, offset + len, 0))
            return 0;
        len++;
        len += ASN1.decode_object_id(buffer, offset + len, out type, out object_id.instance);
        object_id.type = (BacnetObjectTypes)type;

            /* Tag 1: Property ID */
        len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
        if (tag_number != 1)
            return 0;
        len += ASN1.decode_enumerated(buffer, offset + len, len_value_type, out property.propertyIdentifier);

            /* Tag 2: Optional Array Index or Tag 3:  BACnet Result Flags */
        len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
        if ((tag_number == 2) && (len < apdu_len))
            len += ASN1.decode_unsigned(buffer, offset + len, len_value_type, out property.propertyArrayIndex);
        else
                /* Tag 3:  BACnet Result Flags */
            len += ASN1.decode_bitstring(buffer, offset + len, (uint)2, out ResultFlag);

            /* Tag 4 Item Count */
        len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
        len += ASN1.decode_unsigned(buffer, offset + len, len_value_type, out ItemCount);

        if (!(ASN1.decode_is_opening_tag(buffer, offset + len)))
            return 0;
        len += 1;

        RangeBuffer = new byte[buffer.Length - offset - len - 1];

        Array.Copy(buffer, offset + len, RangeBuffer, 0, RangeBuffer.Length);

        return ItemCount;
    }

    public static void EncodeReadProperty(EncodeBuffer buffer, BacnetObjectId object_id, uint property_id, uint array_index = ASN1.BACNET_ARRAY_ALL)
    {
        if ((int)object_id.type <= ASN1.BACNET_MAX_OBJECT)
        {
                /* check bounds so that we could create malformed
                   messages for testing */
            ASN1.encode_context_object_id(buffer, 0, object_id.type, object_id.instance);
        }
        if (property_id <= (uint)BacnetPropertyIds.MAX_BACNET_PROPERTY_ID)
        {
                /* check bounds so that we could create malformed
                   messages for testing */
            ASN1.encode_context_enumerated(buffer, 1, property_id);
        }
            /* optional array index */
        if (array_index != ASN1.BACNET_ARRAY_ALL)
        {
            ASN1.encode_context_unsigned(buffer, 2, array_index);
        }
    }

    public static int DecodeAtomicWriteFileAcknowledge(byte[] buffer, int offset, int apdu_len, out bool is_stream, out int position)
    {
        int len = 0;
        byte tag_number = 0;
        uint len_value_type = 0;

        is_stream = false;
        position = 0;

        len = ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
        if (tag_number == 0)
        {
            is_stream = true;
            len += ASN1.decode_signed(buffer, offset + len, len_value_type, out position);
        }
        else if (tag_number == 1)
        {
            is_stream = false;
            len += ASN1.decode_signed(buffer, offset + len, len_value_type, out position);
        }
        else
            return -1;

        return len;
    }

    public static int DecodeAtomicReadFileAcknowledge(byte[] buffer, int offset, int apdu_len, out bool end_of_file, out bool is_stream, out int position, out uint count, out byte[] target_buffer, out int target_offset)
    {
        int len = 0;
        byte tag_number = 0;
        uint len_value_type = 0;
        int tag_len = 0;

        end_of_file = false;
        is_stream = false;
        position = -1;
        count = 0;
        target_buffer = null;
        target_offset = -1;

        len = ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
        if (tag_number != (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_BOOLEAN)
            return -1;
        end_of_file = len_value_type > 0;
        if (ASN1.decode_is_opening_tag_number(buffer, offset + len, 0))
        {
            is_stream = true;
                /* a tag number is not extended so only one octet */
            len++;
                /* fileStartPosition */
            tag_len = ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
            len += tag_len;
            if (tag_number != (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_SIGNED_INT)
                return -1;
            len += ASN1.decode_signed(buffer, offset + len, len_value_type, out position);
                /* fileData */
            tag_len = ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
            len += tag_len;
            if (tag_number != (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_OCTET_STRING)
                return -1;
            //len += ASN1.decode_octet_string(buffer, offset + len, buffer.Length, target_buffer, target_offset, len_value_type);
            target_buffer = buffer;
            target_offset = offset + len;
            count = len_value_type;
            len += (int)count;
            if (!ASN1.decode_is_closing_tag_number(buffer, offset + len, 0))
                return -1;
                /* a tag number is not extended so only one octet */
            len++;
        }
        else if (ASN1.decode_is_opening_tag_number(buffer, offset + len, 1))
        {
            is_stream = false;
            throw new NotImplementedException("Non stream File transfers are not supported");
            ///* a tag number is not extended so only one octet */
            //len++;
            ///* fileStartRecord */
            //tag_len = ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
            //len += tag_len;
            //if (tag_number != (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_SIGNED_INT)
            //    return -1;
            //len += ASN1.decode_signed(buffer, offset + len, len_value_type, out position);
            ///* returnedRecordCount */
            //tag_len = ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
            //len += tag_len;
            //if (tag_number != (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_UNSIGNED_INT)
            //    return -1;
            //len += ASN1.decode_unsigned(buffer, offset + len, len_value_type, out count);
            //for (i = 0; i < count; i++)
            //{
            //    /* fileData */
            //    tag_len = ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
            //    len += tag_len;
            //    if (tag_number != (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_OCTET_STRING)
            //        return -1;
            //    len += ASN1.decode_octet_string(buffer, offset + len, buffer.Length, target_buffer, target_offset, len_value_type);
            //}
            //if (!ASN1.decode_is_closing_tag_number(buffer, offset + len, 1))
            //    return -1;
            ///* a tag number is not extended so only one octet */
            //len++;
        }
        else
            return -1;

        return len;
    }

    public static int DecodeReadProperty(byte[] buffer, int offset, int apdu_len, out BacnetObjectId object_id, out BacnetPropertyReference property)
    {
        int len = 0;
        ushort type = 0;
        byte tag_number = 0;
        uint len_value_type = 0;

        object_id = new BacnetObjectId();
        property = new BacnetPropertyReference();

            /* Tag 0: Object ID          */
        if (!ASN1.decode_is_context_tag(buffer, offset + len, 0))
            return -1;
        len++;
        len += ASN1.decode_object_id(buffer, offset + len, out type, out object_id.instance);
        object_id.type = (BacnetObjectTypes)type;
            /* Tag 1: Property ID */
        len +=
                ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
        if (tag_number != 1)
            return -1;
        len += ASN1.decode_enumerated(buffer, offset + len, len_value_type, out property.propertyIdentifier);
            /* Tag 2: Optional Array Index */
        if (len < apdu_len)
        {
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
            if ((tag_number == 2) && (len < apdu_len))
            {
                len += ASN1.decode_unsigned(buffer, offset + len, len_value_type, out property.propertyArrayIndex);
            }
            else
                return -1;
        }
        else
            property.propertyArrayIndex = ASN1.BACNET_ARRAY_ALL;

        return len;
    }

    public static void EncodeReadPropertyAcknowledge(EncodeBuffer buffer, BacnetObjectId object_id, uint property_id, uint array_index, IEnumerable<BacnetValue> value_list)
    {
            /* service ack follows */
        ASN1.encode_context_object_id(buffer, 0, object_id.type, object_id.instance);
        ASN1.encode_context_enumerated(buffer, 1, property_id);
            /* context 2 array index is optional */
        if (array_index != ASN1.BACNET_ARRAY_ALL)
        {
            ASN1.encode_context_unsigned(buffer, 2, array_index);
        }

            /* Value */
        ASN1.encode_opening_tag(buffer, 3);
        foreach (BacnetValue value in value_list)
        {
            ASN1.bacapp_encode_application_data(buffer, value);
        }
        ASN1.encode_closing_tag(buffer, 3);
    }

    public static int DecodeReadPropertyAcknowledge(byte[] buffer, int offset, int apdu_len, out BacnetObjectId object_id, out BacnetPropertyReference property, out IList<BacnetValue> value_list)
    {
        byte tag_number = 0;
        uint len_value_type = 0;
        int tag_len = 0;    /* length of tag decode */
        int len = 0;        /* total length of decodes */

        object_id = new BacnetObjectId();
        property = new BacnetPropertyReference();
        value_list = new List<BacnetValue>();

            /* FIXME: check apdu_len against the len during decode   */
            /* Tag 0: Object ID */
        if (!ASN1.decode_is_context_tag(buffer, offset, 0))
            return -1;
        len = 1;
        len += ASN1.decode_object_id(buffer, offset + len, out object_id.type, out object_id.instance);
            /* Tag 1: Property ID */
        len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
        if (tag_number != 1)
            return -1;
        len += ASN1.decode_enumerated(buffer, offset + len, len_value_type, out property.propertyIdentifier);
            /* Tag 2: Optional Array Index */
        tag_len = ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
        if (tag_number == 2)
        {
            len += tag_len;
            len += ASN1.decode_unsigned(buffer, offset + len, len_value_type, out property.propertyArrayIndex);
        }
        else
            property.propertyArrayIndex = ASN1.BACNET_ARRAY_ALL;

            /* Tag 3: opening context tag */
        if (ASN1.decode_is_opening_tag_number(buffer, offset + len, 3))
        {
                /* a tag number of 3 is not extended so only one octet */
            len++;

            BacnetValue value;
            while ((apdu_len - len) > 1)
            {
                tag_len = ASN1.bacapp_decode_application_data(buffer, offset + len, apdu_len + offset, object_id.type, (BacnetPropertyIds)property.propertyIdentifier, out value);
                if (tag_len < 0) return -1;
                len += tag_len;
                value_list.Add(value);
            }
        }
        else
            return -1;

        if (!ASN1.decode_is_closing_tag_number(buffer, offset + len, 3))
            return -1;
        len++;

        return len;
    }

    public static void EncodeReadPropertyMultiple(EncodeBuffer buffer, IList<BacnetReadAccessSpecification> properties)
    {
        foreach (BacnetReadAccessSpecification value in properties)
        ASN1.encode_read_access_specification(buffer, value);
    }

    public static void EncodeReadPropertyMultiple(EncodeBuffer buffer, BacnetObjectId object_id, IList<BacnetPropertyReference> properties)
    {
        EncodeReadPropertyMultiple(buffer, new BacnetReadAccessSpecification[] { new BacnetReadAccessSpecification(object_id, properties) });
    }

    public static int DecodeReadPropertyMultiple(byte[] buffer, int offset, int apdu_len, out IList<BacnetReadAccessSpecification> properties)
    {
        int len = 0;
        int tmp;

        List<BacnetReadAccessSpecification> values = new List<BacnetReadAccessSpecification>();
        properties = null;

        while ((apdu_len - len) > 0)
        {
            BacnetReadAccessSpecification value;
            tmp = ASN1.decode_read_access_specification(buffer, offset + len, apdu_len - len, out value);
            if (tmp < 0) return -1;
            len += tmp;
            values.Add(value);
        }

        properties = values;
        return len;
    }

    public static void EncodeReadPropertyMultipleAcknowledge(EncodeBuffer buffer, IList<BacnetReadAccessResult> values)
    {
        foreach (BacnetReadAccessResult value in values)
        ASN1.encode_read_access_result(buffer, value);
    }

    public static int DecodeReadPropertyMultipleAcknowledge(byte[] buffer, int offset, int apdu_len, out IList<BacnetReadAccessResult> values)
    {
        int len = 0;
        int tmp;

        List<BacnetReadAccessResult> _values = new List<BacnetReadAccessResult>();
        values = null;

        while ((apdu_len - len) > 0)
        {
            BacnetReadAccessResult value;
            tmp = ASN1.decode_read_access_result(buffer, offset + len, apdu_len - len, out value);
            if (tmp < 0) return -1;
            len += tmp;
            _values.Add(value);
        }

        values = _values;
        return len;
    }

    public static void EncodeWriteProperty(EncodeBuffer buffer, BacnetObjectId object_id, uint property_id, uint array_index, uint priority, IEnumerable<BacnetValue> value_list)
    {
        ASN1.encode_context_object_id(buffer, 0, object_id.type, object_id.instance);
        ASN1.encode_context_enumerated(buffer, 1, property_id);

            /* optional array index; ALL is -1 which is assumed when missing */
        if (array_index != ASN1.BACNET_ARRAY_ALL)
        {
            ASN1.encode_context_unsigned(buffer, 2, array_index);
        }

            /* propertyValue */
        ASN1.encode_opening_tag(buffer, 3);
        foreach (BacnetValue value in value_list)
        {
            ASN1.bacapp_encode_application_data(buffer, value);
        }
        ASN1.encode_closing_tag(buffer, 3);

            /* optional priority - 0 if not set, 1..16 if set */
        if (priority != ASN1.BACNET_NO_PRIORITY)
        {
            ASN1.encode_context_unsigned(buffer, 4, priority);
        }
    }

    public static int DecodeCOVNotifyUnconfirmed(byte[] buffer, int offset, int apdu_len, out uint subscriberProcessIdentifier, out BacnetObjectId initiatingDeviceIdentifier, out BacnetObjectId monitoredObjectIdentifier, out uint timeRemaining, out ICollection<BacnetPropertyValue> values)
    {
        int len = 0;
        byte tag_number = 0;
        uint len_value = 0;
        uint decoded_value;

        subscriberProcessIdentifier = 0;
        initiatingDeviceIdentifier = new BacnetObjectId();
        monitoredObjectIdentifier = new BacnetObjectId();
        timeRemaining = 0;
        values = null;

            /* tag 0 - subscriberProcessIdentifier */
        if (ASN1.decode_is_context_tag(buffer, offset + len, 0))
        {
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            len += ASN1.decode_unsigned(buffer, offset + len, len_value, out subscriberProcessIdentifier);
        }
        else
            return -1;

            /* tag 1 - initiatingDeviceIdentifier */
        if (ASN1.decode_is_context_tag(buffer, offset + len, 1))
        {
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            len += ASN1.decode_object_id(buffer, offset + len, out initiatingDeviceIdentifier.type, out initiatingDeviceIdentifier.instance);
        }
        else
            return -1;

            /* tag 2 - monitoredObjectIdentifier */
        if (ASN1.decode_is_context_tag(buffer, offset + len, 2))
        {
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            len += ASN1.decode_object_id(buffer, offset + len, out monitoredObjectIdentifier.type, out monitoredObjectIdentifier.instance);
        }
        else
            return -1;

            /* tag 3 - timeRemaining */
        if (ASN1.decode_is_context_tag(buffer, offset + len, 3))
        {
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            len += ASN1.decode_unsigned(buffer, offset + len, len_value, out timeRemaining);
        }
        else
            return -1;

            /* tag 4: opening context tag - listOfValues */
        if (!ASN1.decode_is_opening_tag_number(buffer, offset + len, 4))
            return -1;

            /* a tag number of 4 is not extended so only one octet */
        len++;
        LinkedList<BacnetPropertyValue> _values = new LinkedList<BacnetPropertyValue>();
        while (!ASN1.decode_is_closing_tag_number(buffer, offset + len, 4))
        {
            BacnetPropertyValue new_entry = new BacnetPropertyValue();

                /* tag 0 - propertyIdentifier */
            if (ASN1.decode_is_context_tag(buffer, offset + len, 0))
            {
                len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
                len += ASN1.decode_enumerated(buffer, offset + len, len_value, out new_entry.property.propertyIdentifier);
            }
            else
                return -1;

                /* tag 1 - propertyArrayIndex OPTIONAL */
            if (ASN1.decode_is_context_tag(buffer, offset + len, 1))
            {
                len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
                len += ASN1.decode_unsigned(buffer, offset + len, len_value, out new_entry.property.propertyArrayIndex);
            }
            else
                new_entry.property.propertyArrayIndex = ASN1.BACNET_ARRAY_ALL;

                /* tag 2: opening context tag - value */
            if (!ASN1.decode_is_opening_tag_number(buffer, offset + len, 2))
                return -1;

                /* a tag number of 2 is not extended so only one octet */
            len++;
            List<BacnetValue> b_values = new List<BacnetValue>();
            while (!ASN1.decode_is_closing_tag_number(buffer, offset + len, 2))
            {
                BacnetValue b_value;
                int tmp = ASN1.bacapp_decode_application_data(buffer, offset + len, apdu_len + offset, monitoredObjectIdentifier.type, (BacnetPropertyIds)new_entry.property.propertyIdentifier, out b_value);
                if (tmp < 0) return -1;
                len += tmp;
                b_values.Add(b_value);
            }
            new_entry.value = b_values;

                /* a tag number of 2 is not extended so only one octet */
            len++;
                /* tag 3 - priority OPTIONAL */
            if (ASN1.decode_is_context_tag(buffer, offset + len, 3))
            {
                len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
                len += ASN1.decode_unsigned(buffer, offset + len, len_value, out decoded_value);
                new_entry.priority = (byte)decoded_value;
            }
            else
                new_entry.priority = (byte)ASN1.BACNET_NO_PRIORITY;

            _values.AddLast(new_entry);
        }

        values = _values;

        return len;
    }

    public static int DecodeWriteProperty(byte[] buffer, int offset, int apdu_len, out BacnetObjectId object_id, out BacnetPropertyValue value)
    {
        int len = 0;
        int tag_len = 0;
        byte tag_number = 0;
        uint len_value_type = 0;
        uint unsigned_value = 0;

        object_id = new BacnetObjectId();
        value = new BacnetPropertyValue();

            /* Tag 0: Object ID          */
        if (!ASN1.decode_is_context_tag(buffer, offset + len, 0))
            return -1;
        len++;
        len += ASN1.decode_object_id(buffer, offset + len, out object_id.type, out object_id.instance);
            /* Tag 1: Property ID */
        len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
        if (tag_number != 1)
            return -1;
        len += ASN1.decode_enumerated(buffer, offset + len, len_value_type, out value.property.propertyIdentifier);
            /* Tag 2: Optional Array Index */
            /* note: decode without incrementing len so we can check for opening tag */
        tag_len = ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
        if (tag_number == 2)
        {
            len += tag_len;
            len += ASN1.decode_unsigned(buffer, offset + len, len_value_type, out value.property.propertyArrayIndex);
        }
        else
            value.property.propertyArrayIndex = ASN1.BACNET_ARRAY_ALL;
            /* Tag 3: opening context tag */
        if (!ASN1.decode_is_opening_tag_number(buffer, offset + len, 3))
            return -1;
        len++;

        //data
        List<BacnetValue> _value_list = new List<BacnetValue>();
        while ((apdu_len - len) > 1 && !ASN1.decode_is_closing_tag_number(buffer, offset + len, 3))
        {
            BacnetValue b_value;
            int l = ASN1.bacapp_decode_application_data(buffer, offset + len, apdu_len + offset, object_id.type, (BacnetPropertyIds)value.property.propertyIdentifier, out b_value);
            if (l <= 0) return -1;
            len += l;
            _value_list.Add(b_value);
        }
        value.value = _value_list;

        if (!ASN1.decode_is_closing_tag_number(buffer, offset + len, 3))
            return -2;
            /* a tag number of 3 is not extended so only one octet */
        len++;
            /* Tag 4: optional Priority - assumed MAX if not explicitly set */
        value.priority = (byte)ASN1.BACNET_MAX_PRIORITY;
        if (len < apdu_len)
        {
            tag_len = ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value_type);
            if (tag_number == 4)
            {
                len += tag_len;
                len = ASN1.decode_unsigned(buffer, offset + len, len_value_type, out unsigned_value);
                if ((unsigned_value >= ASN1.BACNET_MIN_PRIORITY) && (unsigned_value <= ASN1.BACNET_MAX_PRIORITY))
                    value.priority = (byte)unsigned_value;
                else
                    return -1;
            }
        }

        return len;
    }

    public static void EncodeWritePropertyMultiple(EncodeBuffer buffer, BacnetObjectId object_id, ICollection<BacnetPropertyValue> value_list)
    {
        ASN1.encode_context_object_id(buffer, 0, object_id.type, object_id.instance);
            /* Tag 1: sequence of WriteAccessSpecification */
        ASN1.encode_opening_tag(buffer, 1);

        foreach (BacnetPropertyValue p_value in value_list)
        {
                /* Tag 0: Property */
            ASN1.encode_context_enumerated(buffer, 0, p_value.property.propertyIdentifier);

                /* Tag 1: array index */
            if (p_value.property.propertyArrayIndex != ASN1.BACNET_ARRAY_ALL)
                ASN1.encode_context_unsigned(buffer, 1, p_value.property.propertyArrayIndex);

                /* Tag 2: Value */
            ASN1.encode_opening_tag(buffer, 2);
            foreach (BacnetValue value in p_value.value)
            {
                ASN1.bacapp_encode_application_data(buffer, value);
            }
            ASN1.encode_closing_tag(buffer, 2);

                /* Tag 3: Priority */
            if (p_value.priority != ASN1.BACNET_NO_PRIORITY)
                ASN1.encode_context_unsigned(buffer, 3, p_value.priority);
        }

        ASN1.encode_closing_tag(buffer, 1);
    }

    public static void EncodeWriteObjectMultiple(EncodeBuffer buffer, ICollection<BacnetReadAccessResult> value_list)
    {
        foreach (BacnetReadAccessResult r_value in value_list)
        EncodeWritePropertyMultiple(buffer, r_value.objectIdentifier, r_value.values);
    }
    // By C. Gunter
    // quite the same as DecodeWritePropertyMultiple
    public static int DecodeCreateObject(byte[] buffer, int offset, int apdu_len, out BacnetObjectId object_id, out ICollection<BacnetPropertyValue> values_refs)
    {
        int len = 0;
        byte tag_number;
        uint len_value;
        uint ulVal;
        uint property_id;

        object_id = new BacnetObjectId();
        values_refs = null;

        //object id
        len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);

        if ((tag_number == 0) && (apdu_len > len))
        {
            apdu_len -= len;
            if (apdu_len >= 4)
            {
                ushort typenr;

                len += ASN1.decode_context_object_id(buffer, offset + len, 1, out typenr, out object_id.instance);
                object_id.type = (BacnetObjectTypes)typenr;
            }
            else
                return -1;
        }
        else
            return -1;
        if (ASN1.decode_is_closing_tag(buffer, offset + len))
            len++;
        //end objectid

            /* Tag 1: sequence of WriteAccessSpecification */
        if (!ASN1.decode_is_opening_tag_number(buffer, offset + len, 1))
            return -1;
        len++;

        LinkedList<BacnetPropertyValue> _values = new LinkedList<BacnetPropertyValue>();
        while ((apdu_len - len) > 1)
        {
            BacnetPropertyValue new_entry = new BacnetPropertyValue();

                /* tag 0 - Property Identifier */
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            if (tag_number == 0)
                len += ASN1.decode_enumerated(buffer, offset + len, len_value, out property_id);
            else
                return -1;

                /* tag 1 - Property Array Index - optional */
            ulVal = ASN1.BACNET_ARRAY_ALL;
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            if (tag_number == 1)
            {
                len += ASN1.decode_unsigned(buffer, offset + len, len_value, out ulVal);
                len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            }
            new_entry.property = new BacnetPropertyReference(property_id, ulVal);

                /* tag 2 - Property Value */
            if ((tag_number == 2) && (ASN1.decode_is_opening_tag(buffer, offset + len - 1)))
            {
                List<BacnetValue> values = new List<BacnetValue>();
                while (!ASN1.decode_is_closing_tag(buffer, offset + len))
                {
                    BacnetValue value;
                    int l = ASN1.bacapp_decode_application_data(buffer, offset + len, apdu_len + offset, object_id.type, (BacnetPropertyIds)property_id, out value);
                    if (l <= 0) return -1;
                    len += l;
                    values.Add(value);
                }
                len++;
                new_entry.value = values;
            }
            else
                return -1;

            _values.AddLast(new_entry);
        }

            /* Closing tag 1 - List of Properties */
        if (!ASN1.decode_is_closing_tag_number(buffer, offset + len, 1))
            return -1;
        len++;

        values_refs = _values;

        return len;
    }
    public static int DecodeDeleteObject(byte[] buffer, int offset, int apdu_len, out BacnetObjectId object_id)
    {
        int len = 0;
        byte tag_number;
        uint lenght;
        object_id = new BacnetObjectId();
        ASN1.decode_tag_number_and_value(buffer, offset, out tag_number, out lenght);

        if (tag_number != 12)
            return -1;

        len = 1;
        len += ASN1.decode_object_id(buffer, offset + len, out object_id.type, out object_id.instance);

        if (len == apdu_len) //check if packet was correct!
            return len;
        else
            return -1;
    }

    public static void EncodeCreateObjectAcknowledge(EncodeBuffer buffer, BacnetObjectId object_id)
    {
        ASN1.encode_application_object_id(buffer, object_id.type, object_id.instance);
    }

    public static int DecodeWritePropertyMultiple(byte[] buffer, int offset, int apdu_len, out BacnetObjectId object_id, out ICollection<BacnetPropertyValue> values_refs)
    {
        int len = 0;
        byte tag_number;
        uint len_value;
        uint ulVal;
        uint property_id;

        object_id = new BacnetObjectId();
        values_refs = null;

            /* Context tag 0 - Object ID */
        len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
        if ((tag_number == 0) && (apdu_len > len))
        {
            apdu_len -= len;
            if (apdu_len >= 4)
            {
                len += ASN1.decode_object_id(buffer, offset + len, out object_id.type, out object_id.instance);
            }
            else
                return -1;
        }
        else
            return -1;

            /* Tag 1: sequence of WriteAccessSpecification */
        if (!ASN1.decode_is_opening_tag_number(buffer, offset + len, 1))
            return -1;
        len++;

        LinkedList<BacnetPropertyValue> _values = new LinkedList<BacnetPropertyValue>();
        while ((apdu_len - len) > 1)
        {
            BacnetPropertyValue new_entry = new BacnetPropertyValue();

                /* tag 0 - Property Identifier */
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            if (tag_number == 0)
                len += ASN1.decode_enumerated(buffer, offset + len, len_value, out property_id);
            else
                return -1;

                /* tag 1 - Property Array Index - optional */
            ulVal = ASN1.BACNET_ARRAY_ALL;
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            if (tag_number == 1)
            {
                len += ASN1.decode_unsigned(buffer, offset + len, len_value, out ulVal);
                len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            }
            new_entry.property = new BacnetPropertyReference(property_id, ulVal);

                /* tag 2 - Property Value */
            if ((tag_number == 2) && (ASN1.decode_is_opening_tag(buffer, offset + len - 1)))
            {
                List<BacnetValue> values = new List<BacnetValue>();
                while (!ASN1.decode_is_closing_tag(buffer, offset + len))
                {
                    BacnetValue value;
                    int l = ASN1.bacapp_decode_application_data(buffer, offset + len, apdu_len + offset, object_id.type, (BacnetPropertyIds)property_id, out value);
                    if (l <= 0) return -1;
                    len += l;
                    values.Add(value);
                }
                len++;
                new_entry.value = values;
            }
            else
                return -1;

                /* tag 3 - Priority - optional */
            ulVal = ASN1.BACNET_NO_PRIORITY;
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
            if (tag_number == 3)
                len += ASN1.decode_unsigned(buffer, offset + len, len_value, out ulVal);
            else
                len--;
            new_entry.priority = (byte)ulVal;

            _values.AddLast(new_entry);
        }

            /* Closing tag 1 - List of Properties */
        if (!ASN1.decode_is_closing_tag_number(buffer, offset + len, 1))
            return -1;
        len++;

        values_refs = _values;

        return len;
    }

    public static void EncodeTimeSync(EncodeBuffer buffer, DateTime time)
    {
        ASN1.encode_application_date(buffer, time);
        ASN1.encode_application_time(buffer, time);
    }

    public static int DecodeTimeSync(byte[] buffer, int offset, int length, out DateTime dateTime)
    {
        int len = 0;
        byte tag_number;
        uint len_value;
        DateTime d_date, t_date;

        dateTime = new DateTime(1, 1, 1);

            /* date */
        len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
        if (tag_number != (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_DATE)
            return -1;
        len += ASN1.decode_date(buffer, offset + len, out d_date);
            /* time */
        len += ASN1.decode_tag_number_and_value(buffer, offset + len, out tag_number, out len_value);
        if (tag_number != (byte)BacnetApplicationTags.BACNET_APPLICATION_TAG_TIME)
            return -1;
        len += ASN1.decode_bacnet_time(buffer, offset + len, out t_date);

        //merge
        dateTime = new DateTime(d_date.Year, d_date.Month, d_date.Day, t_date.Hour, t_date.Minute, t_date.Second, t_date.Millisecond);

        return len;
    }

    public static void EncodeError(EncodeBuffer buffer, BacnetErrorClasses error_class, BacnetErrorCodes error_code)
    {
        ASN1.encode_application_enumerated(buffer, (uint)error_class);
        ASN1.encode_application_enumerated(buffer, (uint)error_code);
    }

    public static int DecodeError(byte[] buffer, int offset, int length, out BacnetErrorClasses error_class, out BacnetErrorCodes error_code)
    {
        int org_offset = offset;
        uint tmp;

        byte tag_number;
        uint len_value_type;
        offset += ASN1.decode_tag_number_and_value(buffer, offset, out tag_number, out len_value_type);
            /* FIXME: we could validate that the tag is enumerated... */
        offset += ASN1.decode_enumerated(buffer, offset, len_value_type, out tmp);
        error_class = (BacnetErrorClasses)tmp;
        offset += ASN1.decode_tag_number_and_value(buffer, offset, out tag_number, out len_value_type);
            /* FIXME: we could validate that the tag is enumerated... */
        offset += ASN1.decode_enumerated(buffer, offset, len_value_type, out tmp);
        error_code = (BacnetErrorCodes)tmp;

        return offset - org_offset;
    }

    public static void EncodeLogRecord(EncodeBuffer buffer, BacnetLogRecord record)
    {
            /* Tag 0: timestamp */
        ASN1.encode_opening_tag(buffer, 0);
        ASN1.encode_application_date(buffer, record.timestamp);
        ASN1.encode_application_time(buffer, record.timestamp);
        ASN1.encode_closing_tag(buffer, 0);

            /* Tag 1: logDatum */
        if (record.type != BacnetTrendLogValueType.TL_TYPE_NULL)
        {

            if (record.type == BacnetTrendLogValueType.TL_TYPE_ERROR)
            {
                ASN1.encode_opening_tag(buffer, 1);
                ASN1.encode_opening_tag(buffer, 8);
                BacnetError err = record.GetValue<BacnetError>();
                Services.EncodeError(buffer, err.error_class, err.error_code);
                ASN1.encode_closing_tag(buffer, 8);
                ASN1.encode_closing_tag(buffer, 1);
                return;
            }

            ASN1.encode_opening_tag(buffer, 1);
            EncodeBuffer tmp1 = new EncodeBuffer();
            switch (record.type)
            {
                case BacnetTrendLogValueType.TL_TYPE_ANY:
                    throw new NotImplementedException();
                case BacnetTrendLogValueType.TL_TYPE_BITS:
                    ASN1.encode_bitstring(tmp1, record.GetValue<BacnetBitString>());
                    break;
                case BacnetTrendLogValueType.TL_TYPE_BOOL:
                    tmp1.Add(record.GetValue<bool>() ? (byte)1 : (byte)0);
                    break;
                case BacnetTrendLogValueType.TL_TYPE_DELTA:
                    ASN1.encode_bacnet_real(tmp1, record.GetValue<float>());
                    break;
                case BacnetTrendLogValueType.TL_TYPE_ENUM:
                    ASN1.encode_application_enumerated(tmp1, record.GetValue<uint>());
                    break;
                case BacnetTrendLogValueType.TL_TYPE_REAL:
                    ASN1.encode_bacnet_real(tmp1, record.GetValue<float>());
                    break;
                case BacnetTrendLogValueType.TL_TYPE_SIGN:
                    ASN1.encode_bacnet_signed(tmp1, record.GetValue<int>());
                    break;
                case BacnetTrendLogValueType.TL_TYPE_STATUS:
                    ASN1.encode_bitstring(tmp1, record.GetValue<BacnetBitString>());
                    break;
                case BacnetTrendLogValueType.TL_TYPE_UNSIGN:
                    ASN1.encode_bacnet_unsigned(tmp1, record.GetValue<uint>());
                    break;
            }
            ASN1.encode_tag(buffer, (byte)record.type, false, (uint)tmp1.offset);
            buffer.Add(tmp1.buffer, tmp1.offset);
            ASN1.encode_closing_tag(buffer, 1);
        }

            /* Tag 2: status */
        if (record.statusFlags.bits_used > 0)
        {
            ASN1.encode_opening_tag(buffer, 2);
            ASN1.encode_application_bitstring(buffer, record.statusFlags);
            ASN1.encode_closing_tag(buffer, 2);
        }
    }

    public static int DecodeLogRecord(byte[] buffer, int offset, int length, int n_curves, out BacnetLogRecord[] records)
    {
        int len = 0;
        byte tag_number;
        uint len_value;
        records = new BacnetLogRecord[n_curves];

        DateTime date;
        DateTime time;

        len += ASN1.decode_tag_number(buffer, offset + len, out tag_number);
        if (tag_number != 0) return -1;

        // Date and Time in Tag 0
        len += ASN1.decode_application_date(buffer, offset + len, out date);
        len += ASN1.decode_application_time(buffer, offset + len, out time);

        DateTime dt = new DateTime(date.Year, date.Month, date.Day, time.Hour, time.Minute, time.Second, time.Millisecond);

        if (!(ASN1.decode_is_closing_tag(buffer, offset + len))) return -1;
        len++;

        // Value or error in Tag 1
        len += ASN1.decode_tag_number(buffer, offset + len, out tag_number);
        if (tag_number != 1) return -1;

        byte ContextTagType = 0;

        // Not test for TrendLogMultiple
        // Seems to be encoded like this somewhere in an Ashrae document
        for (int CurveNumber = 0; CurveNumber < n_curves; CurveNumber++)
        {
            len += ASN1.decode_tag_number_and_value(buffer, offset + len, out ContextTagType, out len_value);
            records[CurveNumber] = new BacnetLogRecord();
            records[CurveNumber].timestamp = dt;
            records[CurveNumber].type = (BacnetTrendLogValueType)ContextTagType;

            switch ((BacnetTrendLogValueType)ContextTagType)
            {
                case BacnetTrendLogValueType.TL_TYPE_STATUS:
                    BacnetBitString sval;
                    len += ASN1.decode_bitstring(buffer, offset + len, len_value, out sval);
                    records[CurveNumber].Value = sval;
                    break;
                case BacnetTrendLogValueType.TL_TYPE_BOOL:
                    records[CurveNumber].Value = buffer[offset + len] > 0 ? true : false;
                    len++;
                    break;
                case BacnetTrendLogValueType.TL_TYPE_REAL:
                    float rval;
                    len += ASN1.decode_real(buffer, offset + len, out rval);
                    records[CurveNumber].Value = rval;
                    break;
                case BacnetTrendLogValueType.TL_TYPE_ENUM:
                    uint eval;
                    len += ASN1.decode_enumerated(buffer, offset + len, len_value, out eval);
                    records[CurveNumber].Value = eval;
                    break;
                case BacnetTrendLogValueType.TL_TYPE_SIGN:
                    int ival;
                    len += ASN1.decode_signed(buffer, offset + len, len_value, out ival);
                    records[CurveNumber].Value = ival;
                    break;
                case BacnetTrendLogValueType.TL_TYPE_UNSIGN:
                    uint uinval;
                    len += ASN1.decode_unsigned(buffer, offset + len, len_value, out uinval);
                    records[CurveNumber].Value = uinval;
                    break;
                case BacnetTrendLogValueType.TL_TYPE_ERROR:
                    BacnetErrorClasses Errclass;
                    BacnetErrorCodes Errcode;
                    len += DecodeError(buffer, offset + len, length, out Errclass, out Errcode);
                    records[CurveNumber].Value = new BacnetError(Errclass, Errcode);
                    len++; // Closing Tag 8
                    break;
                case BacnetTrendLogValueType.TL_TYPE_NULL:
                    len++;
                    records[CurveNumber].Value = null;
                    break;
                // Time change (Automatic or Synch time) Delta in seconds
                case BacnetTrendLogValueType.TL_TYPE_DELTA:
                    float dval;
                    len += ASN1.decode_real(buffer, offset + len, out dval);
                    records[CurveNumber].Value = dval;
                    break;
                // No way to handle these data types, sure it's the end of this download !
                case BacnetTrendLogValueType.TL_TYPE_ANY:
                    throw new NotImplementedException();
                case BacnetTrendLogValueType.TL_TYPE_BITS:
                    BacnetBitString bval;
                    len += ASN1.decode_bitstring(buffer, offset + len, len_value, out bval);
                    records[CurveNumber].Value = bval;
                    break;
                default:
                    return 0;
            }
        }

        if (!(ASN1.decode_is_closing_tag(buffer, offset + len))) return -1;
        len++;

        // Optional Tag 2
        if (len < length)
        {
            int l = ASN1.decode_tag_number(buffer, offset + len, out tag_number);
            if (tag_number == 2)
            {
                len += l;
                BacnetBitString StatusFlags;
                len += ASN1.decode_bitstring(buffer, offset + len, 2, out StatusFlags);

                //set status to all returns
                for (int CurveNumber = 0; CurveNumber < n_curves; CurveNumber++)
                    records[CurveNumber].statusFlags = StatusFlags;
            }
        }

        return len;
    }
}
