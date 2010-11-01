/* ===========================================================================
 * $RCSfile: NameAndTypeCpInfo.java,v $
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
 * Representation of a 'nameandtype' entry in the ConstantPool.
 *
 * @author      Mark Welsh
 */
public class NameAndTypeCpInfo extends CpInfo implements Cloneable
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u2nameIndex;
    private int u2descriptorIndex;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    protected NameAndTypeCpInfo()
    {
        super(CONSTANT_NameAndType);
    }

    /** Clone the entry. */
    public Object clone()
    {
        NameAndTypeCpInfo cloneInfo = new NameAndTypeCpInfo();
        cloneInfo.u2nameIndex = this.u2nameIndex;
        cloneInfo.u2descriptorIndex = this.u2descriptorIndex;
        cloneInfo.resetRefCount();
        return cloneInfo;
    }

    /** Return the name index. */
    protected int getNameIndex() {return u2nameIndex;}

    /** Set the name index. */
    protected void setNameIndex(int index) {u2nameIndex = index;}

    /** Return the descriptor index. */
    protected int getDescriptorIndex() {return u2descriptorIndex;}

    /** Set the descriptor index. */
    protected void setDescriptorIndex(int index) {u2descriptorIndex = index;}

    /** Check for Utf8 references to constant pool and mark them. */
    protected void markUtf8Refs(ConstantPool pool) throws Exception
    {
        pool.incRefCount(u2nameIndex);
        pool.incRefCount(u2descriptorIndex);
    }

    /** Read the 'info' data following the u1tag byte. */
    protected void readInfo(DataInput din) throws Exception
    {
        u2nameIndex = din.readUnsignedShort();
        u2descriptorIndex = din.readUnsignedShort();
    }

    /** Write the 'info' data following the u1tag byte. */
    protected void writeInfo(DataOutput dout) throws Exception
    {
        dout.writeShort(u2nameIndex);
        dout.writeShort(u2descriptorIndex);
    }
}
