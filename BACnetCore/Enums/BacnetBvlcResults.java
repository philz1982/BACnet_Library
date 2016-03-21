package BACnetCore.Enums;

/**
 * Created by czitop on 3/20/2016.
 */
public enum BacnetBvlcResults {

    BVLC_RESULT_SUCCESSFUL_COMPLETION ((short)0x0000),
    BVLC_RESULT_WRITE_BROADCAST_DISTRIBUTION_TABLE_NAK ((short)0x0010),
    BVLC_RESULT_READ_BROADCAST_DISTRIBUTION_TABLE_NAK ((short)0x0020),
    BVLC_RESULT_REGISTER_FOREIGN_DEVICE_NAK ((short)0X0030),
    BVLC_RESULT_READ_FOREIGN_DEVICE_TABLE_NAK ((short)0x0040),
    BVLC_RESULT_DELETE_FOREIGN_DEVICE_TABLE_ENTRY_NAK ((short)0x0050),
    BVLC_RESULT_DISTRIBUTE_BROADCAST_TO_NETWORK_NAK ((short)0x0060);

    private short flags;

    private BacnetBvlcResults(short flags){
        this.flags = flags;
    }
}
