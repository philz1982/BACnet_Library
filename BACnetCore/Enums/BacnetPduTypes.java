package BACnetCore.Enums;

/**
 * Created by Phil on 3/20/2016.
 */
public enum BacnetPduTypes {
    PDU_TYPE_CONFIRMED_SERVICE_REQUEST ((byte)0),
    SERVER ((byte)1),
    NEGATIVE_ACK ((byte)2),
    SEGMENTED_RESPONSE_ACCEPTED ((byte)2),
    MORE_FOLLOWS ((byte)4),
    PDU_TYPE_UNCONFIRMED_SERVICE_REQUEST ((byte)0x10),
    PDU_TYPE_SIMPLE_ACK ((byte)0x20),
    PDU_TYPE_COMPLEX_ACK ((byte)0x30),
    PDU_TYPE_SEGMENT_ACK ((byte)0x40),
    PDU_TYPE_ERROR ((byte)0x50),
    PDU_TYPE_REJECT ((byte)0x60),
    PDU_TYPE_ABORT ((byte)0x70),
    PDU_TYPE_MASK ((byte)0xF0);


    private final byte flags;

    private BacnetPduTypes(byte flags) {
        this.flags = flags;
    }
}
