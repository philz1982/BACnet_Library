package BACnetCore.BACnetCore.Classes;

import BACnetCore.Enums.EncodeResult;

import java.lang.reflect.Array;

/**
 * Created by czitop on 3/21/2016.
 */
public class EncodeBuffer {

    public byte[] buffer;
    public int offset;
    public int max_offset;
    public int serialize_counter;
    public int min_limit;
    public EncodeResult result;
    public boolean expandable;

    public EncodeBuffer()
    {
        expandable = true;
        buffer = new byte[128];
        max_offset = buffer.length -1;
    }

    public EncodeBuffer(byte[] buffer, int offset)
    {
        if (buffer == null) buffer = new byte[0];
        this.expandable = false;
        this.buffer = buffer;
        this.offset = offset;
        this.max_offset = buffer.length;
    }

    public void increment()
    {
        if (offset < max_offset)
        {
            if (serialize_counter >= min_limit)
                offset++;
            serialize_counter++;
        } else
        {
            if(serialize_counter >= min_limit)
                offset++;
        }
    }

    public void Add(byte b)
    {
        if( offset < max_offset) {
            if (serialize_counter >= min_limit)
                buffer[offset] = b;
        }else
        {
            if (expandable)
            {
                //need to work through this line Array.Resize<byte>(ref buffer, buffer.Length * 2);
                max_offset = buffer.length -1;
                if (serialize_counter >= min_limit)
                    buffer[offset] = b;
            }else
            {
                result |= EncodeResult.NotEnoughBuffer;
            }
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
        int diff = Math.abs(buffer.offset - offset);
        diff = Math.max(Math.abs(buffer.serialize_counter - serialize_counter), diff);
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
        // need to figure out how to replicate Array.Copy(buffer, 0, ret, 0, ret.Length);
        return ret;
    }

    public void Reset(int offset)
    {
        this.offset = offset;
        serialize_counter = 0;
        result = EncodeResult.Good;
    }

    public String ToString()
    {
        return offset + " - " + serialize_counter;
    }

    public int GetLength()
    {
       return Math.min(offset, max_offset);
    }

}
