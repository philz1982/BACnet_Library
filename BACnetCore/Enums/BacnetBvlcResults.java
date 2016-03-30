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
public enum BacnetBvlcResults {

    BVLC_RESULT_SUCCESSFUL_COMPLETION ((short)0x0000),
    BVLC_RESULT_WRITE_BROADCAST_DISTRIBUTION_TABLE_NAK ((short)0x0010),
    BVLC_RESULT_READ_BROADCAST_DISTRIBUTION_TABLE_NAK ((short)0x0020),
    BVLC_RESULT_REGISTER_FOREIGN_DEVICE_NAK ((short)0X0030),
    BVLC_RESULT_READ_FOREIGN_DEVICE_TABLE_NAK ((short)0x0040),
    BVLC_RESULT_DELETE_FOREIGN_DEVICE_TABLE_ENTRY_NAK ((short)0x0050),
    BVLC_RESULT_DISTRIBUTE_BROADCAST_TO_NETWORK_NAK ((short)0x0060);

    private short flags;

    private BacnetBvlcResults(short flags){
        this.flags = flags;
    }
}
