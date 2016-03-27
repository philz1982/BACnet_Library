package BACnetCore.Enums;

/**
 * Created by czitop on 3/20/2016.
 */
public enum BacnetReinitializedStates {

    BACNET_REINIT_COLDSTART (0),
    BACNET_REINIT_WARMSTART (1),
    BACNET_REINIT_STARTBACKUP (2),
    BACNET_REINIT_ENDBACKUP (3),
    BACNET_REINIT_STARTRESTORE (4),
    BACNET_REINIT_ENDRESTORE (5),
    BACNET_REINIT_ABORTRESTORE (6),
    BACNET_REINIT_IDLE (255);

    private int flags;

    private BacnetReinitializedStates(int flags){
        this.flags = flags;
    }
}
