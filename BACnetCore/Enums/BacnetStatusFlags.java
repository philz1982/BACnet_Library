package BACnetCore.Enums;

/**
 * Created by Phil on 3/20/2016.
 */
public enum BacnetStatusFlags {

    STATUS_FLAG_IN_ALARM ((byte)1),
    STATUS_FLAG_FAULT ((byte)2),
    STATUS_FLAG_OVERRIDDEN ((byte)4),
    STATUS_FLAG_OUT_OF_SERVICE ((byte)8);

    private byte flags;

    private BacnetStatusFlags(byte flags){
        this.flags = flags;
    }

}
