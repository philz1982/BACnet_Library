package BACnetCore.CoreClasses;

/**
 * Created by Phil on 3/26/2016.
 */
public class BacNode
{
    private BacnetAddress adr;
    private uint device_id;

    public BacNode(BacnetAddress adr, uint device_id)
    {
        this.adr = adr;
        this.device_id = device_id;
    }

    public BacnetAddress getAdd(uint device_id)
    {
        if (this.device_id == device_id)
            return adr;
        else
            return null;
    }
}