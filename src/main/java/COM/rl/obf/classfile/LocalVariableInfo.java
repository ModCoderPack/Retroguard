/* ===========================================================================
 * $RCSfile: LocalVariableInfo.java,v $
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
 * Representation of an Local Variable table entry.
 *
 * @author      Mark Welsh
 */
public class LocalVariableInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u2startpc;
    private int u2length;
    private int u2nameIndex;
    private int u2descriptorIndex;
    private int u2index;


    // Class Methods ---------------------------------------------------------
    public static LocalVariableInfo create(DataInput din) throws Exception
    {
        LocalVariableInfo lvi = new LocalVariableInfo();
        lvi.read(din);
        return lvi;
    }


    // Instance Methods ------------------------------------------------------
    private LocalVariableInfo() {}

    /** Return name index into Constant Pool. */
    protected int getNameIndex() {return u2nameIndex;}

    /** Set the name index. */
    protected void setNameIndex(int index) {u2nameIndex = index;}

    /** Return descriptor index into Constant Pool. */
    protected int getDescriptorIndex() {return u2descriptorIndex;}

    /** Set the descriptor index. */
    protected void setDescriptorIndex(int index) {u2descriptorIndex = index;}

    /** Check for Utf8 references to constant pool and mark them. */
    protected void markUtf8Refs(ConstantPool pool) throws Exception
    {
        pool.incRefCount(u2nameIndex);
        pool.incRefCount(u2descriptorIndex);
    }

    private void read(DataInput din) throws Exception
    {
        u2startpc = din.readUnsignedShort();
        u2length = din.readUnsignedShort();
        u2nameIndex = din.readUnsignedShort();
        u2descriptorIndex = din.readUnsignedShort();
        u2index = din.readUnsignedShort();
    }

    /** Export the representation to a DataOutput stream. */
    public void write(DataOutput dout) throws Exception
    {
        dout.writeShort(u2startpc);
        dout.writeShort(u2length);
        dout.writeShort(u2nameIndex);
        dout.writeShort(u2descriptorIndex);
        dout.writeShort(u2index);
    }

    /** Do necessary name remapping. */
    protected void remap(ClassFile cf, NameMapper nm) throws Exception 
    { 
        String oldDesc = cf.getUtf8(u2descriptorIndex);
        String newDesc = nm.mapDescriptor(oldDesc);
        u2descriptorIndex = cf.remapUtf8To(newDesc, u2descriptorIndex);
    }
}
