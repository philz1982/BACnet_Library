package BACnetCore.Enums;

/**
 * Created by Phil on 3/20/2016.
 */
public enum BacnetMaxSegements {

    MAX_SEG0 ((byte)0),
    MAX_SEG2 ((byte)0x10),
    MAX_SEG4 ((byte)0x20),
    MAX_SEG8 ((byte)0x30),
    MAX_SEG16 ((byte)0x40),
    MAX_SEG32 ((byte)0x50),
    MAX_SEG64 ((byte)0x60),
    MAX_SEG65 ((byte)0x70);

    private byte flags;

    private BacnetMaxSegements(byte flags) {
        this.flags = flags;
    }
}
