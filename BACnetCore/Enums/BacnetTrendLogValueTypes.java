package BACnetCore.Enums;

/**
 * Created by Phil on 3/20/2016.
 */
public enum BacnetTrendLogValueTypes {

    TL_TYPE_STATUS ((byte)0),
    TL_TYPE_BOOL ((byte)1),
    TL_TYPE_REAL ((byte)2),
    TL_TYPE_ENUM ((byte)3),
    TL_TYPE_UNSIGN ((byte)4),
    TL_TYPE_SIGN ((byte)5),
    TL_TYPE_BITS ((byte)6),
    TL_TYPE_NULL ((byte)7),
    TL_TYPE_ERROR ((byte)8),
    TL_TYPE_DELTA ((byte)9),
    TL_TYPE_ANY ((byte)10);


    private byte flags;

    private BacnetTrendLogValueTypes(byte flags){
        this.flags = flags;
    }
}
