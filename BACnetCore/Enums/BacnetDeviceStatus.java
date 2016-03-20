package BACnetCore.Enums;

/**
 * Created by Phil on 3/20/2016.
 */
public enum BacnetDeviceStatus {

    OPERATIONAL ((byte)0),
    OPERATIONAL_READONLY ((byte)1),
    DOWNLOAD_REQUIRED ((byte)2),
    DOWNLOAD_IN_PROGRESS ((byte)3),
    NON_OPERATIONAL ((byte)4),
    BACKUP_IN_PROGRESS ((byte)5);

    private byte flags;

    private BacnetDeviceStatus(byte flags) {
        this.flags = flags;
    }

}
