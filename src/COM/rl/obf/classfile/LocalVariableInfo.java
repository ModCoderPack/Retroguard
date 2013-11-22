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
 * @author Mark Welsh
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
    /**
     * @param din
     * @throws IOException
     */
    public static LocalVariableInfo create(DataInput din) throws IOException
    {
        LocalVariableInfo lvi = new LocalVariableInfo();
        lvi.read(din);
        return lvi;
    }


    // Instance Methods ------------------------------------------------------
    /**
     * Private constructor
     */
    private LocalVariableInfo()
    {
    }

    /**
     * Return name index into Constant Pool.
     */
    protected int getNameIndex()
    {
        return this.u2nameIndex;
    }

    /**
     * Set the name index.
     * 
     * @param index
     */
    protected void setNameIndex(int index)
    {
        this.u2nameIndex = index;
    }

    /**
     * Return descriptor index into Constant Pool.
     */
    protected int getDescriptorIndex()
    {
        return this.u2descriptorIndex;
    }

    /**
     * Set the descriptor index.
     * 
     * @param index
     */
    protected void setDescriptorIndex(int index)
    {
        this.u2descriptorIndex = index;
    }

    /**
     * Check for Utf8 references to constant pool and mark them.
     * 
     * @param pool
     * @throws ClassFileException
     */
    protected void markUtf8Refs(ConstantPool pool) throws ClassFileException
    {
        pool.incRefCount(this.u2nameIndex);
        pool.incRefCount(this.u2descriptorIndex);
    }

    /**
     * @param din
     * @throws IOException
     */
    private void read(DataInput din) throws IOException
    {
        this.u2startpc = din.readUnsignedShort();
        this.u2length = din.readUnsignedShort();
        this.u2nameIndex = din.readUnsignedShort();
        this.u2descriptorIndex = din.readUnsignedShort();
        this.u2index = din.readUnsignedShort();
    }

    /**
     * Export the representation to a DataOutput stream.
     * 
     * @param dout
     * @throws IOException
     */
    public void write(DataOutput dout) throws IOException
    {
        dout.writeShort(this.u2startpc);
        dout.writeShort(this.u2length);
        dout.writeShort(this.u2nameIndex);
        dout.writeShort(this.u2descriptorIndex);
        dout.writeShort(this.u2index);
    }

    /**
     * Do necessary name remapping.
     * 
     * @param cf
     * @param nm
     * @throws ClassFileException
     */
    protected void remap(ClassFile cf, NameMapper nm) throws ClassFileException
    {
        String oldDesc = cf.getUtf8(this.u2descriptorIndex);
        String newDesc = nm.mapDescriptor(oldDesc);
        this.u2descriptorIndex = cf.remapUtf8To(newDesc, this.u2descriptorIndex);
    }
}
