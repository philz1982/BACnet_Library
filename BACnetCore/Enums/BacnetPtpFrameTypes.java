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
public enum BacnetPtpFrameTypes {

    FRAME_TYPE_HEARTBEAT_XOFF ((byte)0),
    FRAME_TYPE_HEARTBEAT_XON ((byte)1),
    FRAME_TYPE_DATA0 ((byte)2),
    FRAME_TYPE_DATA1 ((byte)3),
    FRAME_TYPE_DATA_ACK0_XOFF ((byte)4),
    FRAME_TYPE_DATA_ACK1_XOFF ((byte)5),
    FRAME_TYPE_DATA_ACK0_XON ((byte)6),
    FRAME_TYPE_DATA_ACK1_XON ((byte)7),
    FRAME_TYPE_DATA_NAK0_XOFF ((byte)8),
    FRAME_TYPE_DATA_NAK1_XOFF ((byte)9),
    FRAME_TYPE_DATA_NAK0_XON ((byte)0x0A),
    FRAME_TYPE_DATA_NAK1_XON ((byte)0x0B),
    FRAME_TYPE_CONNECT_REQUEST ((byte)0x0C),
    FRAME_TYPE_CONNECT_RESPONSE ((byte)0x0D),
    FRAME_TYPE_DISCONNECT_REQUEST ((byte)0x0E),
    FRAME_TYPE_DISCONNECT_RESPONSE ((byte)0x0F),
    FRAME_TYPE_TEST_REQUEST ((byte)0x14),
    FRAME_TYPE_TEST_RESPONSE ((byte)0x15),
    FRAME_TYPE_GREETING ((byte)0xFF);

    private byte flags;

    private BacnetPtpFrameTypes(byte flags){
        this.flags = flags;
    }
}
