package BACnetCore.Enums;

/**
 * Created by czitop on 3/20/2016.
 */
public enum BacnetNpduControls {

    PriorityNormalMessage ((byte)0),
    PriorityUrgentMessage ((byte)1),
    PriorityCriticalMessage ((byte)2),
    PriorityLifeSafetyMessage ((byte)3),
    ExpectingReply ((byte)4),
    SourceSpecified ((byte)8),
    DestinationSpecified ((byte)32),
    NetworkLayerMessage ((byte)128);

    private byte flags;

    private BacnetNpduControls(byte flags) {
        this.flags = flags;
    }
}
