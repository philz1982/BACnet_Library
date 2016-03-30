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
public enum BacnetMstpFrameTypes {

    FRAME_TYPE_TOKEN ((byte)0),
    FRAME_TYPE_POLL_FOR_MASTER ((byte)1),
    FRAME_TYPE_REPLY_TO_POLL_FOR_MASTER ((byte)2),
    FRAME_TYPE_TEST_REQUEST ((byte)3),
    FRAME_TYPE_TEST_RESPONSE ((byte)4),
    FRAME_TYPE_BACNET_DATA_EXPECTING_REPLY ((byte)5),
    FRAME_TYPE_BACNET_DATA_NOT_EXPECTING_REPLY ((byte)6),
    FRAME_TYPE_REPLY_POSTPONED ((byte)7),
        /* Frame Types 128 through 255: Proprietary Frames */
        /* These frames are available to vendors as proprietary (non-BACnet) frames. */
        /* The first two octets of the Data field shall specify the unique vendor */
        /* identification code, most significant octet first, for the type of */
        /* vendor-proprietary frame to be conveyed. The length of the data portion */
        /* of a Proprietary frame shall be in the range of 2 to 501 octets. */
    FRAME_TYPE_PROPRIETARY_MIN ((byte)128),
    FRAME_TYPE_PROPRIETARY_MAX ((byte)255);

    byte flags;

    private BacnetMstpFrameTypes(byte flags){
        this.flags = flags;
    }
}
