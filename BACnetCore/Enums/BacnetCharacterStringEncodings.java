package BACnetCore.Enums;

/**
 * Created by Phil on 3/20/2016.
 */
public enum BacnetCharacterStringEncodings {



    CHARACTER_UTF8 (0),
    CHARACTER_MS_DBCS (1),

    CHARACTER_JISX_0208 (2),
    CHARACTER_UCS4 (3),
    CHARACTER_UCS2 (4),
    CHARACTER_ISO8859 (5),

    /**
     * @Depreicated deprecated : Addendum 135-2008k
     */
    @Deprecated
    CHARACTER_ANSI_X34 (0), CHARACTER_JISC_6226 (2);


    private int flags;

    private BacnetCharacterStringEncodings(int flags){
        this.flags = flags;
    }
}
