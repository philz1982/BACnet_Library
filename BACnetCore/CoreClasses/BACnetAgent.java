package BACnetCore.CoreClasses;

/**************************************************************************
*                           MIT License
* 
* Copyright (C) 2014 Morten Kvistgaard <mk@pch-engineering.dk>
* Copyright (C) 2015 Frederic Chaxel <fchaxel@free.fr>
*
* Permission is hereby granted, free of charge, to any person obtaining
* a copy of this software and associated documentation files (the
* "Software"), to deal in the Software without restriction, including
* without limitation the rights to use, copy, modify, merge, publish,
* distribute, sublicense, and/or sell copies of the Software, and to
* permit persons to whom the Software is furnished to do so, subject to
* the following conditions:
*
* The above copyright notice and this permission notice shall be included
* in all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
* CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
* TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
* SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
*********************************************************************/
public class BACnetAgent
{
    private static BacnetClient bacnet_client;
    private static List<BacNode> DevicesList = new List<BacNode>();


    #region Start Activity
    /*****************************************************************************************************/
    public void StartActivity(string IpAddress)
    {
        // Bacnet on UDP/IP/Ethernet
        bacnet_client = new BacnetClient(new BacnetIpUdpProtocolTransport(0xBAC0, false, false, 1472, IpAddress));

        bacnet_client.Start();    // go

        // Send WhoIs in order to get back all the Iam responses :
        bacnet_client.OnIam += new BacnetClient.IamHandler(handler_OnIam);
        bacnet_client.WhoIs();
    }
    #endregion

    #region Handler_OnIam
    /*****************************************************************************************************/
    static void handler_OnIam(BacnetClient sender, BacnetAddress adr, uint device_id, uint max_apdu, BacnetSegmentations segmentation, ushort vendor_id)
    {
        lock (DevicesList)
        {
            // Device already registred ?
            foreach (BacNode bn in DevicesList)
            if (bn.getAdd(device_id) != null) return;   // Yes

            // Not already in the list
            DevicesList.Add(new BacNode(adr, device_id));   // add it
        }
    }
    #endregion

    #region BACnetAddress
    /*****************************************************************************************************/
    static BacnetAddress DeviceAddr(uint device_id)
    {
        BacnetAddress ret;

        lock (DevicesList)
        {
            foreach (BacNode bn in DevicesList)
            {
                ret = bn.getAdd(device_id);
                if (ret != null) return ret;
            }
            // not in the list
            return null;
        }
    }
    #endregion

    #region ReadScalarValue
    /*****************************************************************************************************/
    private static bool ReadScalarValue(int device_id, BacnetObjectId BacnetObjet, BacnetPropertyIds Property, out BacnetValue Value)
    {
        BacnetAddress adr;
        IList<BacnetValue> NoScalarValue;

        Value = new BacnetValue(null);

        // Looking for the device
        adr = DeviceAddr((uint)device_id);
        if (adr == null) return false;  // not found

        // Property Read
        if (bacnet_client.ReadPropertyRequest(adr, BacnetObjet, Property, out NoScalarValue) == false)
            return false;

        Value = NoScalarValue[0];
        return true;
    }
    #endregion

    #region WriteScalarValue
    /*****************************************************************************************************/
    private static bool WriteScalarValue(int device_id, BacnetObjectId BacnetObjet, BacnetPropertyIds Property, BacnetValue Value)
    {
        BacnetAddress adr;

        // Looking for the device
        adr = DeviceAddr((uint)device_id);
        if (adr == null) return false;  // not found

        // Property Write
        BacnetValue[] NoScalarValue = { Value };
        if (bacnet_client.WritePropertyRequest(adr, BacnetObjet, Property, NoScalarValue) == false)
            return false;

        return true;
    }
    #endregion

    #region Read
    /*****************************************************************************************************/
    public bool Read(int deviceId, BacnetObjectTypes ReadBacObj, uint instance, BacnetPropertyIds BacProp, out string readValue)
    {
        BacnetValue value;
        // Read Present_Value property on the object OBJECT_BINARY_VALUE:3000018 (Room 1001) provided by the device 500
        // Scalar value only
        bool ret = ReadScalarValue(deviceId, new BacnetObjectId(ReadBacObj, instance), BacProp, out value);
        readValue = value.ToString();
        return ret;
    }
    #endregion

    #region Write
    /*****************************************************************************************************/
    public bool Write(int deviceId, BacnetObjectTypes WriteBacObj, uint instance, BacnetPropertyIds BacProp, BacnetValue newValue)
    {
        // Read Present_Value property on the object OBJECT_BINARY_VALUE:3000018 (Room 1001) provided by the device 500
        // Scalar value only
        bool ret = WriteScalarValue(deviceId, new BacnetObjectId(WriteBacObj, instance),
                BacProp, newValue);
        return ret;
    }
    #endregion
}
