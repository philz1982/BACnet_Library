package BACnetCore.Enums;

/**
 * Created by czitop on 3/20/2016.
 */
public enum EncodeResult {

    Good (0),
    NotEnoughBuffer (1);

    private int flags;

    private EncodeResult(int flags){
        this.flags = flags;
    }
}
