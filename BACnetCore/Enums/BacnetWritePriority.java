package BACnetCore.Enums;

/**
 * Created by Phil on 3/20/2016.
 */
public enum BacnetWritePriority {

    NO_PRIORITY (0),
    MANUAL_LIFE_SAFETY (1),
    AUTOMATIC_LIFE_SAFETY (2),
    UNSPECIFIED_LEVEL_3 (3),
    UNSPECIFIED_LEVEL_4 (4),
    CRITICAL_EQUIPMENT_CONTROL (5),
    MINIMUM_ON_OFF (6),
    UNSPECIFIED_LEVEL_7 (7),
    MANUAL_OPERATOR (8),
    UNSPECIFIED_LEVEL_9 (9),
    UNSPECIFIED_LEVEL_10 (10),
    UNSPECIFIED_LEVEL_11 (11),
    UNSPECIFIED_LEVEL_12 (12),
    UNSPECIFIED_LEVEL_13 (13),
    UNSPECIFIED_LEVEL_14 (14),
    UNSPECIFIED_LEVEL_15 (15),
    LOWEST_AND_DEFAULT (16);

    private int flags;

    private BacnetWritePriority(int flags) {
        this.flags = flags;
    }
}
