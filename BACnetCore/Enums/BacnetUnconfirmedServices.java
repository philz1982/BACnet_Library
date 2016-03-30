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
