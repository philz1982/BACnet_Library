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
public enum BacnetBvlcFunctions {

    BVLC_RESULT ((byte)0),
    BVLC_WRITE_BROADCAST_DISTRIBUTION_TABLE ((byte)1),
    BVLC_READ_BROADCAST_DIST_TABLE ((byte)2),
    BVLC_READ_BROADCAST_DIST_TABLE_ACK ((byte)3),
    BVLC_FORWARDED_NPDU ((byte)4),
    BVLC_REGISTER_FOREIGN_DEVICE ((byte)5),
    BVLC_READ_FOREIGN_DEVICE_TABLE ((byte)6),
    BVLC_READ_FOREIGN_DEVICE_TABLE_ACK ((byte)7),
    BVLC_DELETE_FOREIGN_DEVICE_TABLE_ENTRY ((byte)8),
    BVLC_DISTRIBUTE_BROADCAST_TO_NETWORK ((byte)9),
    BVLC_ORIGINAL_UNICAST_NPDU ((byte)10),
    BVLC_ORIGINAL_BROADCAST_NPDU ((byte)11),
    MAX_BVLC_FUNCTION ((byte)12);

    private byte flags;

    private BacnetBvlcFunctions(byte flags){
        this.flags = flags;
    }
}
