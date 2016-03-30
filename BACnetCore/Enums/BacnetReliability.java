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
public enum BacnetReliability {

    RELIABILITY_NO_FAULT_DETECTED (0),
    RELIABILITY_NO_SENSOR (1),
    RELIABILITY_OVER_RANGE (2),
    RELIABILITY_UNDER_RANGE (3),
    RELIABILITY_OPEN_LOOP (4),
    RELIABILITY_SHORTED_LOOP (5),
    RELIABILITY_NO_OUTPUT (6),
    RELIABILITY_UNRELIABLE_OTHER (7),
    RELIABILITY_PROCESS_ERROR (8),
    RELIABILITY_MULTI_STATE_FAULT (9),
    RELIABILITY_CONFIGURATION_ERROR (10),
    RELIABILITY_MEMBER_FAULT (11),
    RELIABILITY_COMMUNICATION_FAILURE (12),
    RELIABILITY_TRIPPED (13);

    private int flags;

    private BacnetReliability(int flags){
        this.flags = flags;
    }
}
