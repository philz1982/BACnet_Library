package BACnetCore.Enums;

/**
 * Created by Phil on 3/20/2016.
 */
public enum BacnetUnconfirmedServices {

    SERVICE_UNCONFIRMED_I_AM ((byte)0),
    SERVICE_UNCONFIRMED_I_HAVE ((byte)1),
    SERVICE_UNCONFIRMED_COV_NOTIFICATION ((byte)2),
    SERVICE_UNCONFIRMED_EVENT_NOTIFICATION ((byte)3),
    SERVICE_UNCONFIRMED_PRIVATE_TRANSFER ((byte)4),
    SERVICE_UNCONFIRMED_TEXT_MESSAGE ((byte)5),
    SERVICE_UNCONFIRMED_TIME_SYNCHRONIZATION ((byte)6),
    SERVICE_UNCONFIRMED_WHO_HAS ((byte)7),
    SERVICE_UNCONFIRMED_WHO_IS ((byte)8),
    SERVICE_UNCONFIRMED_UTC_TIME_SYNCHRONIZATION ((byte)9),

    //addendum 2010-aa
    SERVICE_UNCONFIRMED_WRITE_GROUP ((byte)10),
    /*
        Other services to be added as they are defined.
        All choice values in this production are reserved
        for definition by ASHRAE.
        Proprietary extensions are made by using the
        UnconfirmedPrivateTransfer service. See Clause 23.
    */

    MAX_BACNET_UNCONFIRMED_SERVICE ((byte)11);


    private byte flags;

    private BacnetUnconfirmedServices(byte flags){
        this.flags = flags;
    }
}
