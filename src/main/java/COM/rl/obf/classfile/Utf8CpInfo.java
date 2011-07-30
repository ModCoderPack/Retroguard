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
        super(ClassConstants.CONSTANT_Utf8);
    }

    /** Ctor used when appending fresh Utf8 entries to the constant pool. */
    public Utf8CpInfo(String s) throws Exception
    {
        super(ClassConstants.CONSTANT_Utf8);
        this.setString(s);
        this.refCount = 1;
    }

    /** Decrement the reference count, blanking the entry if no more references. */
    @Override
    public void decRefCount() throws Exception
    {
        super.decRefCount();
        if (this.refCount == 0)
        {
            this.clearString();
        }
    }

    /** Return UTF8 data as a String. */
    public String getString() throws Exception
    {
        if (this.utf8string == null)
        {
            this.utf8string = new String(this.bytes, "UTF8");
        }
        return this.utf8string;
    }

    /** Set UTF8 data as String. */
    public void setString(String str) throws Exception
    {
        this.utf8string = str;
        this.bytes = str.getBytes("UTF8");
        this.u2length = this.bytes.length;
    }

    /** Set the UTF8 data to empty. */
    public void clearString() throws Exception
    {
        this.u2length = 0;
        this.bytes = new byte[0];
        this.utf8string = null;
        this.getString();
    }

    /** Read the 'info' data following the u1tag byte. */
    @Override
    protected void readInfo(DataInput din) throws Exception
    {
        this.u2length = din.readUnsignedShort();
        this.bytes = new byte[this.u2length];
        din.readFully(this.bytes);
        this.getString();
    }

    /** Write the 'info' data following the u1tag byte. */
    @Override
    protected void writeInfo(DataOutput dout) throws Exception
    {
        dout.writeShort(this.u2length);
        if (this.bytes.length > 0)
        {
            dout.write(this.bytes);
        }
    }
}
