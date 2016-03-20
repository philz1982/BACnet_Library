package BACnetCore.Enums;

/**
 * Created by Phil on 3/20/2016.
 */
public enum BacnetRestartReason {
    UNKNOWN (0),
    COLD_START (1),
    WARM_START (2),
    DETECTED_POWER_LOST (3),
    DETECTED_POWER_OFF (4),
    HARDWARE_WATCHDOG (5),
    SOFTWARE_WATCHDOG (6),
    SUSPENDED (7);

    private int flags;

    private BacnetRestartReason(int flags){
        this.flags = flags;
    }
}
