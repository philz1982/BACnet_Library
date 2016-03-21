package BACnetCore.Enums;

/**
 * Created by czitop on 3/20/2016.
 */
public enum BacnetPtpFrameTypes {

    FRAME_TYPE_HEARTBEAT_XOFF ((byte)0),
    FRAME_TYPE_HEARTBEAT_XON ((byte)1),
    FRAME_TYPE_DATA0 ((byte)2),
    FRAME_TYPE_DATA1 ((byte)3),
    FRAME_TYPE_DATA_ACK0_XOFF ((byte)4),
    FRAME_TYPE_DATA_ACK1_XOFF ((byte)5),
    FRAME_TYPE_DATA_ACK0_XON ((byte)6),
    FRAME_TYPE_DATA_ACK1_XON ((byte)7),
    FRAME_TYPE_DATA_NAK0_XOFF ((byte)8),
    FRAME_TYPE_DATA_NAK1_XOFF ((byte)9),
    FRAME_TYPE_DATA_NAK0_XON ((byte)0x0A),
    FRAME_TYPE_DATA_NAK1_XON ((byte)0x0B),
    FRAME_TYPE_CONNECT_REQUEST ((byte)0x0C),
    FRAME_TYPE_CONNECT_RESPONSE ((byte)0x0D),
    FRAME_TYPE_DISCONNECT_REQUEST ((byte)0x0E),
    FRAME_TYPE_DISCONNECT_RESPONSE ((byte)0x0F),
    FRAME_TYPE_TEST_REQUEST ((byte)0x14),
    FRAME_TYPE_TEST_RESPONSE ((byte)0x15),
    FRAME_TYPE_GREETING ((byte)0xFF);

    private byte flags;

    private BacnetPtpFrameTypes(byte flags){
        this.flags = flags;
    }
}
