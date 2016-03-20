package BACnetCore.Enums;

/**
 * Created by Phil on 3/20/2016.
 */
public enum BacnetPolarity {

    POLARITY_NORMAL ((byte)0),
    POLARITY_REVERSE ((byte)1);

    private byte flags;

    private BacnetPolarity(byte flags){
        this.flags = flags;
    }
}
