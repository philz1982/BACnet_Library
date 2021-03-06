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
public enum BacnetObjectTypes {

    OBJECT_ANALOG_INPUT (0),
    OBJECT_ANALOG_OUTPUT (1),
    OBJECT_ANALOG_VALUE (2),
    OBJECT_BINARY_INPUT (3),
    OBJECT_BINARY_OUTPUT (4),
    OBJECT_BINARY_VALUE (5),
    OBJECT_CALENDAR (6),
    OBJECT_COMMAND (7),
    OBJECT_DEVICE (8),
    OBJECT_EVENT_ENROLLMENT (9),
    OBJECT_FILE (10),
    OBJECT_GROUP (11),
    OBJECT_LOOP (12),
    OBJECT_MULTI_STATE_INPUT (13),
    OBJECT_MULTI_STATE_OUTPUT (14),
    OBJECT_NOTIFICATION_CLASS (15),
    OBJECT_PROGRAM (16),
    OBJECT_SCHEDULE (17),
    OBJECT_AVERAGING (18),
    OBJECT_MULTI_STATE_VALUE (19),
    OBJECT_TRENDLOG (20),
    OBJECT_LIFE_SAFETY_POINT (21),
    OBJECT_LIFE_SAFETY_ZONE (22),
    OBJECT_ACCUMULATOR (23),
    OBJECT_PULSE_CONVERTER (24),
    OBJECT_EVENT_LOG (25),
    OBJECT_GLOBAL_GROUP (26),
    OBJECT_TREND_LOG_MULTIPLE (27),
    OBJECT_LOAD_CONTROL (28),
    OBJECT_STRUCTURED_VIEW (29),
    OBJECT_ACCESS_DOOR (30),
    OBJECT_31 (31), /* 31 was lighting output, but BACnet editor changed it... */
    OBJECT_ACCESS_CREDENTIAL (32),      /* Addendum 2008-j */
    OBJECT_ACCESS_POINT (33),
    OBJECT_ACCESS_RIGHTS (34),
    OBJECT_ACCESS_USER (35),
    OBJECT_ACCESS_ZONE (36),
    OBJECT_CREDENTIAL_DATA_INPUT (37),  /* authentication-factor-input */
    OBJECT_NETWORK_SECURITY (38),       /* Addendum 2008-g */
    OBJECT_BITSTRING_VALUE (39),        /* Addendum 2008-w */
    OBJECT_CHARACTERSTRING_VALUE (40),  /* Addendum 2008-w */
    OBJECT_DATE_PATTERN_VALUE (41),     /* Addendum 2008-w */
    OBJECT_DATE_VALUE (42),     /* Addendum 2008-w */
    OBJECT_DATETIME_PATTERN_VALUE (43), /* Addendum 2008-w */
    OBJECT_DATETIME_VALUE (44), /* Addendum 2008-w */
    OBJECT_INTEGER_VALUE (45),  /* Addendum 2008-w */
    OBJECT_LARGE_ANALOG_VALUE (46),     /* Addendum 2008-w */
    OBJECT_OCTETSTRING_VALUE (47),      /* Addendum 2008-w */
    OBJECT_POSITIVE_INTEGER_VALUE (48), /* Addendum 2008-w */
    OBJECT_TIME_PATTERN_VALUE (49),     /* Addendum 2008-w */
    OBJECT_TIME_VALUE (50),     /* Addendum 2008-w */
    OBJECT_NOTIFICATION_FORWARDER (51), /* Addendum 2010-af */
    OBJECT_ALERT_ENROLLMENT (52),       /* Addendum 2010-af */
    OBJECT_CHANNEL (53),        /* Addendum 2010-aa */
    OBJECT_LIGHTING_OUTPUT (54),        /* Addendum 2010-i */
        /* Enumerated values 0-127 are reserved for definition by ASHRAE. */
        /* Enumerated values 128-1023 may be used by others subject to  */
        /* the procedures and constraints described in Clause 23. */
        /* do the max range inside of enum so that
           compilers will allocate adequate sized datatype for enum
           which is used to store decoding */
    MAX_ASHRAE_OBJECT_TYPE (55),
    OBJECT_PROPRIETARY_MIN (128),
    OBJECT_PROPRIETARY_MAX (1023),
    MAX_BACNET_OBJECT_TYPE (1024);

    private int flags;

    private BacnetObjectTypes(int flags){
        this.flags = flags;
    }
}
