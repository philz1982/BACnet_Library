package BACnetCore.Enums;

/**
 * Created by Phil on 3/20/2016.
 */
public enum BacnetReliability {

    RELIABILITY_NO_FAULT_DETECTED (0),
    RELIABILITY_NO_SENSOR (1),
    RELIABILITY_OVER_RANGE (2),
    RELIABILITY_UNDER_RANGE (3),
    RELIABILITY_OPEN_LOOP (4),
    RELIABILITY_SHORTED_LOOP (5),
    RELIABILITY_NO_OUTPUT (6),
    RELIABILITY_UNRELIABLE_OTHER (7),
    RELIABILITY_PROCESS_ERROR (8),
    RELIABILITY_MULTI_STATE_FAULT (9),
    RELIABILITY_CONFIGURATION_ERROR (10),
    RELIABILITY_MEMBER_FAULT (11),
    RELIABILITY_COMMUNICATION_FAILURE (12),
    RELIABILITY_TRIPPED (13);

    private int flags;

    private BacnetReliability(int flags){
        this.flags = flags;
    }
}