/**
 * Created by czitop on 2/21/2016.
 */

public class BACnet_Core
{
    public enum BacnetPduTypes : byte
    {
        PDU_TYPE_CONFIRMED_SERVICE_REQUEST (0),
        SERVER (1),
        NEGATIVE_ACK (2),
        SEGEMENTED_RESPONSE_ACCEPTED (2),
        MORE_FOLLOWS (4),
        SEGMENTED_MESSAGE (8),
        PDU_TYPE_UNFONFIRMED_SERVICE_REQUEST (0X10),
        PDU_TYPE_SIMPLE_ACK (0X20),
        PDU_TYPE_COMPLEX_ACK (0X30),
        PDU_TYPE_SEGMENT_ACK (0X40),
        PDU_TYPE_ERROR (0X50),
        PDU_TYPE_REJECT (0X60),
        PDU_TYPE_ABORT (0X70),
        PDU_TYPE_MASK (0XF0)
    }

    public enum BacnetSegementations
    {
        SEGEMENTATION_BOTH (0),
        SEGEMENTATION_TRANSMIT (1),
        SEGEMENTATION_RECEIVE (2),
        SEGEMENTATION_NONE = (3)
    }

    public enum BacnetDeviceStatus : byte
    {
        OPERATIONAL (0),
        OPERATIONAL_READONLY (1),
        DOWNLOAD_REQUIRED (2),
        DOWNLOAD_IN_PROGRESS (3),
        NON_OPERATIONAL (4),
        BACKUP_IN_PROGRESS (5)
    }


}
