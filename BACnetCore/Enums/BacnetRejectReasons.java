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
public enum BacnetRejectReasons {

    REJECT_REASON_OTHER (0),
    REJECT_REASON_BUFFER_OVERFLOW (1),
    REJECT_REASON_INCONSISTENT_PARAMETERS (2),
    REJECT_REASON_INVALID_PARAMETER_DATA_TYPE (3),
    REJECT_REASON_INVALID_TAG (4),
    REJECT_REASON_MISSING_REQUIRED_PARAMETERS (5),
    REJECT_REASON_PARAMETER_OUT_OF_RANGE (6),
    REJECT_REASON_TOO_MANY_ARGUMENTS (7),
    REJECT_REASON_UNDEFINED_ENUMERATION (8),
    REJECT_REASON_UNRECOGNIZED_SERVICE (9),
    MAX_BACNET_REJECT_REASON (10),
    REJECT_REASON_PROPRIETARY_FIRST (64),
    REJECT_REASON_PROPRIETARY_LAST (65535);

    private int flags;

    private BacnetRejectReasons(int flags){
        this.flags = flags;
    }
}
