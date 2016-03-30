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
public enum BacnetWritePriority {

    NO_PRIORITY (0),
    MANUAL_LIFE_SAFETY (1),
    AUTOMATIC_LIFE_SAFETY (2),
    UNSPECIFIED_LEVEL_3 (3),
    UNSPECIFIED_LEVEL_4 (4),
    CRITICAL_EQUIPMENT_CONTROL (5),
    MINIMUM_ON_OFF (6),
    UNSPECIFIED_LEVEL_7 (7),
    MANUAL_OPERATOR (8),
    UNSPECIFIED_LEVEL_9 (9),
    UNSPECIFIED_LEVEL_10 (10),
    UNSPECIFIED_LEVEL_11 (11),
    UNSPECIFIED_LEVEL_12 (12),
    UNSPECIFIED_LEVEL_13 (13),
    UNSPECIFIED_LEVEL_14 (14),
    UNSPECIFIED_LEVEL_15 (15),
    LOWEST_AND_DEFAULT (16);

    private int flags;

    private BacnetWritePriority(int flags) {
        this.flags = flags;
    }
}
