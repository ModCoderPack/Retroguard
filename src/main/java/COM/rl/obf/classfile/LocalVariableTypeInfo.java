/* ===========================================================================
 * $RCSfile: LocalVariableTypeInfo.java,v $
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
 * Representation of an Local Variable Type table entry.
 * 
 * @author Mark Welsh
 */
public class LocalVariableTypeInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u2startpc;
    private int u2length;
    private int u2nameIndex;
    private int u2signatureIndex;
    private int u2index;


    // Class Methods ---------------------------------------------------------
    public static LocalVariableTypeInfo create(DataInput din) throws IOException
    {
        LocalVariableTypeInfo lvti = new LocalVariableTypeInfo();
        lvti.read(din);
        return lvti;
    }


    // Instance Methods ------------------------------------------------------
    private LocalVariableTypeInfo()
    {
    }

    /** Return name index into Constant Pool. */
    protected int getNameIndex()
    {
        return this.u2nameIndex;
    }

    /** Return signature index into Constant Pool. */
    protected int getSignatureIndex()
    {
        return this.u2signatureIndex;
    }

    /**
     * Check for Utf8 references to constant pool and mark them.
     * 
     * @throws ClassFileException
     */
    protected void markUtf8Refs(ConstantPool pool) throws ClassFileException
    {
        pool.incRefCount(this.u2nameIndex);
        pool.incRefCount(this.u2signatureIndex);
    }

    private void read(DataInput din) throws IOException
    {
        this.u2startpc = din.readUnsignedShort();
        this.u2length = din.readUnsignedShort();
        this.u2nameIndex = din.readUnsignedShort();
        this.u2signatureIndex = din.readUnsignedShort();
        this.u2index = din.readUnsignedShort();
    }

    /**
     * Export the representation to a DataOutput stream.
     * 
     * @throws IOException
     */
    public void write(DataOutput dout) throws IOException
    {
        dout.writeShort(this.u2startpc);
        dout.writeShort(this.u2length);
        dout.writeShort(this.u2nameIndex);
        dout.writeShort(this.u2signatureIndex);
        dout.writeShort(this.u2index);
    }

    /**
     * Do necessary name remapping.
     * 
     * @throws ClassFileException
     */
    protected void remap(ClassFile cf, NameMapper nm) throws ClassFileException
    {
        String oldDesc = cf.getUtf8(this.u2signatureIndex);
        String newDesc = nm.mapSignature(oldDesc);
        this.u2signatureIndex = cf.remapUtf8To(newDesc, this.u2signatureIndex);
    }
}
