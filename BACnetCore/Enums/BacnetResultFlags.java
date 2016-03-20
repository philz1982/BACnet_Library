package BACnetCore.Enums;

/**
 * Created by Phil on 3/20/2016.
 */
public enum BacnetResultFlags {

    NONE ((byte)0),
    FIRST_ITEM ((byte)1),
    LAST_ITEM ((byte)2),
    MORE_ITEMS ((byte)4);


    private byte flags;

    private BacnetResultFlags(byte flags){
        this.flags = flags;
    }
}
