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
public enum BacnetTrendLogValueTypes {

    TL_TYPE_STATUS ((byte)0),
    TL_TYPE_BOOL ((byte)1),
    TL_TYPE_REAL ((byte)2),
    TL_TYPE_ENUM ((byte)3),
    TL_TYPE_UNSIGN ((byte)4),
    TL_TYPE_SIGN ((byte)5),
    TL_TYPE_BITS ((byte)6),
    TL_TYPE_NULL ((byte)7),
    TL_TYPE_ERROR ((byte)8),
    TL_TYPE_DELTA ((byte)9),
    TL_TYPE_ANY ((byte)10);


    private byte flags;

    private BacnetTrendLogValueTypes(byte flags){
        this.flags = flags;
    }
}
