package BACnetCore.Enums;

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
public enum BacnetConfirmedServices {

    // Alarm and Event Services
    SERVICE_CONFIRMED_ACKNOWLEDGE_ALARM ((byte)0),
    SERVICE_CONFIRMED_COV_NOTIFICATION ((byte)1),
    SERVICE_CONFIRMED_EVENT_NOTIFICATION ((byte)2),
    SERVICE_CONFIRMED_GET_ALARM_SUMMARY ((byte)3),
    SERVICE_CONFIRMED_GET_ENROLLMENT_SUMMARY ((byte)4),
    SERVICE_CONFIRMED_SUBSCRIBE_COV ((byte)5),
    SERVICE_CONFIRMED_LIFE_SAFETY_OPERATION ((byte)27),
    SERVICE_CONFIRMED_SUBSCRIBE_COV_PROPERTY ((byte)28),
    SERVICE_CONFIRMED_GET_EVENT_INFORMATION ((byte)29),


    // File Access Services
    SERVICE_CONFIRMED_ATOMIC_READ_FILE ((byte)6),
    SERVICE_CONFIRMED_ATOMIC_WRITE_FILE ((byte)7),

    // Object Access Services
    SERVICE_CONFIRMED_ADD_LIST_ELEMENT ((byte)8),
    SERVICE_CONFIRMED_REMOVE_LIST_ELEMENT ((byte)9),
    SERVICE_CONFIRMED_CREATE_OBJECT ((byte)10),
    SERVICE_CONFIRMED_DELETE_OBJECT ((byte)11),
    SERVICE_CONFIRMED_READ_PROPERTY ((byte)12),
    SERVICE_CONFIRMED_READ_PROP_CONDITIONAL ((byte)13),
    SERVICE_CONFIRMED_READ_PROP_MULTIPLE ((byte)14),
    SERVICE_CONFIRMED_WRITE_PROPERTY ((byte)15),
    SERVICE_CONFIRMED_WRITE_PROP_MULTIPLE ((byte)16),
    SERVICE_CONFIRMED_READ_RANGE ((byte)26),


    // Remote Device Management Services
    SERVICE_CONFIRMED_DEVICE_COMMUNICATION_CONTROL ((byte)17),
    SERVICE_CONFIRMED_PRIVATE_TRANSFER ((byte)18),
    SERVICE_CONFIRMED_TEXT_MESSAGE ((byte)19),
    SERVICE_CONFIRMED_REINITIALIZE_DEVICE ((byte)20),

    // Virtual Terminal Services
    SERVICE_CONFIRMED_VT_OPEN ((byte)21),
    SERVICE_CONFIRMED_VT_CLOSE ((byte)22),
    SERVICE_CONFIRMED_VT_DATA ((byte)23),

    // Security Services
    SERVICE_CONFIRMED_AUTHENTICATE ((byte)24),
    SERVICE_CONFIRMED_REQUEST_KEY ((byte)25),

    /*
        Services added after 1995
        readRange (26) see Object Access Services
        lifeSafetyOperation (27) see Alarm and Event Services
        subscribeCOVProperty (28) see Alarm and Event Services
        getEventInformation (29) see Alarm and Event Services
    */
        MAX_BACNET_CONFIRMED_SERVICE ((byte)30);


    private byte flags;

    private BacnetConfirmedServices(byte flags){
        this.flags = flags;
    }
}
