package BACnetCore.Enums;

/**
 * Created by Phil on 3/20/2016.
 */
public enum BacnetMaxApdu {

    MAX_APDU50 ((byte)0),
    MAX_APDU128 ((byte)1),
    MAX_APDU206 ((byte)2),
    MAX_APDU480 ((byte)3),
    MAX_APDU1024 ((byte)4),
    MAX_APDU1476 ((byte)5);

    private byte flags;

    private BacnetMaxApdu(byte flags){
        this.flags = flags;
    }
}
