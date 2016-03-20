package BACnetCore.Enums;

/**
 * Created by Phil on 3/20/2016.
 */
public enum BacnetSegmentations {

    SEGMENTATION_BOTH (0),
    SEGMENTATION_TRANSMIT (1),
    SEGMENTATION_RECEIVE (2),
    SEGMENTATION_NONE (3);

    private int flags;

    private BacnetSegmentations(int flags){
        this.flags = flags;
    }

}
