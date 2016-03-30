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
public enum BacnetPduTypes {
    PDU_TYPE_CONFIRMED_SERVICE_REQUEST ((byte)0),
    SERVER ((byte)1),
    NEGATIVE_ACK ((byte)2),
    SEGMENTED_RESPONSE_ACCEPTED ((byte)2),
    MORE_FOLLOWS ((byte)4),
    PDU_TYPE_UNCONFIRMED_SERVICE_REQUEST ((byte)0x10),
    PDU_TYPE_SIMPLE_ACK ((byte)0x20),
    PDU_TYPE_COMPLEX_ACK ((byte)0x30),
    PDU_TYPE_SEGMENT_ACK ((byte)0x40),
    PDU_TYPE_ERROR ((byte)0x50),
    PDU_TYPE_REJECT ((byte)0x60),
    PDU_TYPE_ABORT ((byte)0x70),
    PDU_TYPE_MASK ((byte)0xF0);


    private final byte flags;

    private BacnetPduTypes(byte flags) {
        this.flags = flags;
    }
}
