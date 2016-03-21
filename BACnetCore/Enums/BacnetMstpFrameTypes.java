package BACnetCore.Enums;

/**
 * Created by czitop on 3/20/2016.
 */
public enum BacnetMstpFrameTypes {

    FRAME_TYPE_TOKEN ((byte)0),
    FRAME_TYPE_POLL_FOR_MASTER ((byte)1),
    FRAME_TYPE_REPLY_TO_POLL_FOR_MASTER ((byte)2),
    FRAME_TYPE_TEST_REQUEST ((byte)3),
    FRAME_TYPE_TEST_RESPONSE ((byte)4),
    FRAME_TYPE_BACNET_DATA_EXPECTING_REPLY ((byte)5),
    FRAME_TYPE_BACNET_DATA_NOT_EXPECTING_REPLY ((byte)6),
    FRAME_TYPE_REPLY_POSTPONED ((byte)7),
        /* Frame Types 128 through 255: Proprietary Frames */
        /* These frames are available to vendors as proprietary (non-BACnet) frames. */
        /* The first two octets of the Data field shall specify the unique vendor */
        /* identification code, most significant octet first, for the type of */
        /* vendor-proprietary frame to be conveyed. The length of the data portion */
        /* of a Proprietary frame shall be in the range of 2 to 501 octets. */
    FRAME_TYPE_PROPRIETARY_MIN ((byte)128),
    FRAME_TYPE_PROPRIETARY_MAX ((byte)255);

    byte flags;

    private BacnetMstpFrameTypes(byte flags){
        this.flags = flags;
    }
}
