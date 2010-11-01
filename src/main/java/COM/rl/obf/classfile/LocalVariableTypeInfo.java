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
 * @author      Mark Welsh
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
    public static LocalVariableTypeInfo create(DataInput din) throws Exception
    {
        LocalVariableTypeInfo lvti = new LocalVariableTypeInfo();
        lvti.read(din);
        return lvti;
    }


    // Instance Methods ------------------------------------------------------
    private LocalVariableTypeInfo() {}

    /** Return name index into Constant Pool. */
    protected int getNameIndex() {return u2nameIndex;}

    /** Return signature index into Constant Pool. */
    protected int getSignatureIndex() {return u2signatureIndex;}

    /** Check for Utf8 references to constant pool and mark them. */
    protected void markUtf8Refs(ConstantPool pool) throws Exception
    {
        pool.incRefCount(u2nameIndex);
        pool.incRefCount(u2signatureIndex);
    }

    private void read(DataInput din) throws Exception
    {
        u2startpc = din.readUnsignedShort();
        u2length = din.readUnsignedShort();
        u2nameIndex = din.readUnsignedShort();
        u2signatureIndex = din.readUnsignedShort();
        u2index = din.readUnsignedShort();
    }

    /** Export the representation to a DataOutput stream. */
    public void write(DataOutput dout) throws Exception
    {
        dout.writeShort(u2startpc);
        dout.writeShort(u2length);
        dout.writeShort(u2nameIndex);
        dout.writeShort(u2signatureIndex);
        dout.writeShort(u2index);
    }

    /** Do necessary name remapping. */
    protected void remap(ClassFile cf, NameMapper nm) throws Exception 
    { 
        // NOTE - mapSignature does nothing for now; implement if interest
        String oldDesc = cf.getUtf8(u2signatureIndex);
        String newDesc = nm.mapSignature(oldDesc);
        u2signatureIndex = cf.remapUtf8To(newDesc, u2signatureIndex);
    }
}
