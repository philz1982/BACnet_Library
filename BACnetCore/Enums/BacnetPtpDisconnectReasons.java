package BACnetCore.Enums;

/**
 * Created by czitop on 3/20/2016.
 */
public enum BacnetPtpDisconnectReasons {

    PTP_DISCONNECT_NO_MORE_DATA ((byte)0),
    PTP_DISCONNECT_PREEMPTED ((byte)1),
    PTP_DISCONNECT_INVALID_PASSWORD ((byte)2),
    PTP_DISCONNECT_OTHER ((byte)3);

    private byte flags;

    private BacnetPtpDisconnectReasons(byte flags){
        this.flags = flags;
    }
}
