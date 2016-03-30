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
public enum BacnetApplicationTags {

    BACNET_APPLICATION_TAG_NULL (0),
    BACNET_APPLICATION_TAG_BOOLEAN (1),
    BACNET_APPLICATION_TAG_UNSIGNED_INT (2),
    BACNET_APPLICATION_TAG_SIGNED_INT (3),
    BACNET_APPLICATION_TAG_REAL (4),
    BACNET_APPLICATION_TAG_DOUBLE (5),
    BACNET_APPLICATION_TAG_OCTET_STRING (6),
    BACNET_APPLICATION_TAG_CHARACTER_STRING (7),
    BACNET_APPLICATION_TAG_BIT_STRING (8),
    BACNET_APPLICATION_TAG_ENUMERATED (9),
    BACNET_APPLICATION_TAG_DATE (0),
    BACNET_APPLICATION_TAG_TIME (11),
    BACNET_APPLICATION_TAG_OBJECT_ID (12),
    BACNET_APPLICATION_TAG_RESERVE1 (13),
    BACNET_APPLICATION_TAG_RESERVE2 (14),
    BACNET_APPLICATION_TAG_RESERVE3 (15),
    MAX_BACNET_APPLICATION_TAG (16),

    // Extra stuff - complex tagged data - not specifically enumerated

    //Means : "nothing", an empty list, not even a null character
    BACNET_APPLICATION_TAG_EMPTYLIST,

    // BACnetWeeknday
    BACNET_APPLICATION_TAG_WEEKNDAY,

    // BACnetDateRange
    BACNET_APPLICATION_TAG_DATERANGE,

    // BACnetDateTime
    BACNET_APPLICATION_TAG_DATETIME,

    // BACnetTimeStamp
    BACNET_APPLICATION_TAG_TIMESTAMP,

    // Error Class, Error Code
    BACNET_APPLICATION_TAG_ERROR,

    // BACnetDeviceObjectPropertyReference
    BACNET_APPLICATION_TAG_DEVICE_OBJECT_PROPERTY_REFERENCE,

    // BACnetDeviceObjectReference
    BACNET_APPLICATION_TAG_DEVICE_OBJECT_REFERENCE,

    // BACnetObjectPropertyReference
    BACNET_APPLICATION_TAG_OBJECT_PROPERTY_REFERENCE,

    // BACnetDestination (Recipient_List)
    BACNET_APPLICATION_TAG_DESTINATION,

    // BACnetRecipient
    BACNET_APPLICATION_TAG_RECIPIENT,

    // BACnetCOVSubscription
    BACNET_APPLICATION_TAG_COV_SUBSCRIPTION,

    // BACnetCalendarEntry
    BACNET_APPLICATION_TAG_CALENDAR_ENTRY,

    // BACnetWeeklySchedule
    BACNET_APPLICATION_TAG_WEEKLY_SCHEDULE,

    // BACnetSpecialEvent
    BACNET_APPLICATION_TAG_SPECIAL_EVENT,

    // BACnetReadAccessSpecification
    BACNET_APPLICATION_TAG_READ_ACCESS_SPECIFICATION,

    // BACnetReadAccessResult
    BACNET_APPLICATION_TAG_READ_ACCESS_RESULT,

    // BACnetLightingCommand
    BACNET_APPLICATION_TAG_LIGHTING_COMMAND,
    BACNET_APPLICATION_TAG_CONTEXT_SPECIFIC_DECODED,
    BACNET_APPLICATION_TAG_CONTEXT_SPECIFIC_ENCODED,

    // BACnetLogRecord
    BACNET_APPLICATION_TAG_LOG_RECORD;

    private int flags;

    private BacnetApplicationTags(){

    }

    private BacnetApplicationTags(int flags) {
        this.flags = flags;
    }

}
