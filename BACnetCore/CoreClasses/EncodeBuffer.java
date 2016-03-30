package BACnetCore.CoreClasses;

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
public class EncodeBuffer

{
    public byte[] buffer;           //buffer to serialize into
    public int offset;              //offset in buffer ... will go beyond max_offset (so that you may count what's needed)
    public int max_offset;          //don't write beyond this offset
    public int serialize_counter;   //used with 'min_limit'
    public int min_limit;           //don't write before this limit (used for segmentation)
    public EncodeResult result;
    public bool expandable;
    public EncodeBuffer()
    {
        expandable = true;
        buffer = new byte[128];
        max_offset = buffer.Length - 1;
    }
    public EncodeBuffer(byte[] buffer, int offset)
    {
        if (buffer == null) buffer = new byte[0];
        this.expandable = false;
        this.buffer = buffer;
        this.offset = offset;
        this.max_offset = buffer.Length;
    }
    public void Increment()
    {
        if (offset < max_offset)
        {
            if (serialize_counter >= min_limit)
                offset++;
            serialize_counter++;
        }
        else
        {
            if (serialize_counter >= min_limit)
                offset++;
        }
    }
    public void Add(byte b)
    {
        if (offset < max_offset)
        {
            if (serialize_counter >= min_limit)
                buffer[offset] = b;
        }
        else
        {
            if (expandable)
            {
                Array.Resize<byte>(ref buffer, buffer.Length * 2);
                max_offset = buffer.Length - 1;
                if (serialize_counter >= min_limit)
                    buffer[offset] = b;
            }
            else
                result |= EncodeResult.NotEnoughBuffer;
        }
        Increment();
    }
    public void Add(byte[] buffer, int count)
    {
        for (int i = 0; i < count; i++)
            Add(buffer[i]);
    }
    public int GetDiff(EncodeBuffer buffer)
    {
        int diff = Math.Abs(buffer.offset - offset);
        diff = Math.Max(Math.Abs(buffer.serialize_counter - serialize_counter), diff);
        return diff;
    }
    public EncodeBuffer Copy()
    {
        EncodeBuffer ret = new EncodeBuffer();
        ret.buffer = buffer;
        ret.max_offset = max_offset;
        ret.min_limit = min_limit;
        ret.offset = offset;
        ret.result = result;
        ret.serialize_counter = serialize_counter;
        ret.expandable = expandable;
        return ret;
    }

    public byte[] ToArray()
    {
        byte[] ret = new byte[offset];
        Array.Copy(buffer, 0, ret, 0, ret.Length);
        return ret;
    }
    public void Reset(int offset)
    {
        this.offset = offset;
        serialize_counter = 0;
        result = EncodeResult.Good;
    }
    public override string ToString()
{
    return offset + " - " + serialize_counter;
}
    public int GetLength()
    {
        return Math.Min(offset, max_offset);
    }
}
