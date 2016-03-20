package BACnetCore.Enums;

/**
 * Created by Phil on 3/20/2016.
 */
public enum BacnetErrorClasses {

    ERROR_CLASS_DEVICE (0),
    ERROR_CLASS_OBJECT (1),
    ERROR_CLASS_PROPERTY (2),
    ERROR_CLASS_RESOURCES (3),
    ERROR_CLASS_SECURITY (4),
    ERROR_CLASS_SERVICES (5),
    ERROR_CLASS_VT (6),
    ERROR_CLASS_COMMUNICATION (7),
    MAX_BACNET_ERROR_CLASS (8),
    ERROR_CLASS_PROPRIETARY_FIRST (64),
    ERROR_CLASS_PROPRIETARY_LAST (65535);

    private int flags;

    private BacnetErrorClasses(int flags){
        this.flags = flags;
    }
}
