package BACnetCore.Enums;

/**
 * Created by czitop on 3/20/2016.
 */
public enum BacnetBvlcFunctions {

    BVLC_RESULT ((byte)0),
    BVLC_WRITE_BROADCAST_DISTRIBUTION_TABLE ((byte)1),
    BVLC_READ_BROADCAST_DIST_TABLE ((byte)2),
    BVLC_READ_BROADCAST_DIST_TABLE_ACK ((byte)3),
    BVLC_FORWARDED_NPDU ((byte)4),
    BVLC_REGISTER_FOREIGN_DEVICE ((byte)5),
    BVLC_READ_FOREIGN_DEVICE_TABLE ((byte)6),
    BVLC_READ_FOREIGN_DEVICE_TABLE_ACK ((byte)7),
    BVLC_DELETE_FOREIGN_DEVICE_TABLE_ENTRY ((byte)8),
    BVLC_DISTRIBUTE_BROADCAST_TO_NETWORK ((byte)9),
    BVLC_ORIGINAL_UNICAST_NPDU ((byte)10),
    BVLC_ORIGINAL_BROADCAST_NPDU ((byte)11),
    MAX_BVLC_FUNCTION ((byte)12);

    private byte flags;

    private BacnetBvlcFunctions(byte flags){
        this.flags = flags;
    }
}
