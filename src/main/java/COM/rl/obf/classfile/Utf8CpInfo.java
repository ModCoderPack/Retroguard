/* ===========================================================================
 * $RCSfile: Utf8CpInfo.java,v $
 * ===========================================================================
 *
 * RetroGuard -- an obfuscation package for Java classfiles.
 *
 * Copyright (c) 1998-2006 Mark Welsh (markw@retrologic.com)
 *
 * This program can be redistributed and/or modified under the terms of the 
 * Version 2 of the GNU General Public License as published by the Free 
 * Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU General Public License for more details.
 *
 */

package COM.rl.obf.classfile;

import java.io.*;
import java.util.*;

/**
 * Representation of a 'UTF8' entry in the ConstantPool.
 *
 * @author      Mark Welsh
 */
public class Utf8CpInfo extends CpInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u2length;
    private byte[] bytes;
    private String utf8string;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    protected Utf8CpInfo()
    {
        super(CONSTANT_Utf8);
    }

    /** Ctor used when appending fresh Utf8 entries to the constant pool. */
    public Utf8CpInfo(String s) throws Exception
    {
        super(CONSTANT_Utf8);
        setString(s);
        refCount = 1;
    }

    /** Decrement the reference count, blanking the entry if no more references. */
    public void decRefCount() throws Exception
    {
        super.decRefCount();
        if (refCount == 0)
        {
            clearString();
        }
    }

    /** Return UTF8 data as a String. */
    public String getString() throws Exception
    {
        if (utf8string == null)
        {
            utf8string = new String(bytes, "UTF8");
        }
        return utf8string;
    }

    /** Set UTF8 data as String. */
    public void setString(String str) throws Exception
    {
        utf8string = str;
        bytes = str.getBytes("UTF8");
        u2length = bytes.length;
    }

    /** Set the UTF8 data to empty. */
    public void clearString() throws Exception
    {
        u2length = 0;
        bytes = new byte[0];
        utf8string = null;
        getString();
    }

    /** Read the 'info' data following the u1tag byte. */
    protected void readInfo(DataInput din) throws Exception
    {
        u2length = din.readUnsignedShort();
        bytes = new byte[u2length];
        din.readFully(bytes);
        getString();
    }

    /** Write the 'info' data following the u1tag byte. */
    protected void writeInfo(DataOutput dout) throws Exception
    {
        dout.writeShort(u2length);
        if (bytes.length > 0) 
        {
            dout.write(bytes);
        }
    }
}
