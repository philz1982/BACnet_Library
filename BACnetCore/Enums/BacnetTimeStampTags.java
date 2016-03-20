package BACnetCore.Enums;

/**
 * Created by Phil on 3/20/2016.
 */
public enum BacnetTimeStampTags {

    TIME_STAMP_NONE (-1),
    TIME_STAMP_TIME (0),
    TIME_STAMP_SEQUENCE (1),
    TIME_STAMP_DATETIME (2);

    private int flags;

    private BacnetTimeStampTags(int flags){
        this.flags = flags;
    }
}
