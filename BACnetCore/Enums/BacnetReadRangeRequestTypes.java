package BACnetCore.Enums;

/**
 * Created by czitop on 3/20/2016.
 */
public enum BacnetReadRangeRequestTypes {

    RR_BY_POSITION (1),
    RR_BY_SEQUENCE (2),
    RR_BY_TIME (4),
    RR_READ_ALL (8);

    private int flags;

    private BacnetReadRangeRequestTypes(int flags){
        this.flags = flags;
    }
}
