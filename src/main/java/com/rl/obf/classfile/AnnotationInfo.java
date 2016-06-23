/* ===========================================================================
 * $RCSfile: AnnotationInfo.java,v $
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

package com.rl.obf.classfile;

import java.io.*;
import java.util.*;

/**
 * Representation of an annotation table entry.
 * 
 * @author Mark Welsh
 */
public class AnnotationInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u2typeIndex;
    private List<MemberValuePairInfo> memberValuePairTable;


    // Class Methods ---------------------------------------------------------
    /**
     * @param din
     * @throws IOException
     * @throws ClassFileException
     */
    public static AnnotationInfo create(DataInput din) throws IOException, ClassFileException
    {
        AnnotationInfo ai = new AnnotationInfo();
        ai.read(din);
        return ai;
    }


    // Instance Methods ------------------------------------------------------
    /**
     * Private constructor
     */
    private AnnotationInfo()
    {
    }

    /**
     * Return type index into Constant Pool.
     */
    protected int getTypeIndex()
    {
        return this.u2typeIndex;
    }

    /**
     * Check for Utf8 references to constant pool and mark them.
     * 
     * @param pool
     * @throws ClassFileException
     */
    protected void markUtf8Refs(ConstantPool pool) throws ClassFileException
    {
        pool.incRefCount(this.u2typeIndex);
        for (MemberValuePairInfo mvp : this.memberValuePairTable)
        {
            mvp.markUtf8Refs(pool);
        }
    }

    /**
     * @param din
     * @throws IOException
     * @throws ClassFileException
     */
    private void read(DataInput din) throws IOException, ClassFileException
    {
        this.u2typeIndex = din.readUnsignedShort();
        int u2numMemberValuePairs = din.readUnsignedShort();
        this.memberValuePairTable = new ArrayList<MemberValuePairInfo>(u2numMemberValuePairs);
        for (int i = 0; i < u2numMemberValuePairs; i++)
        {
            this.memberValuePairTable.add(MemberValuePairInfo.create(din));
        }
    }

    /**
     * Export the representation to a DataOutput stream.
     * 
     * @param dout
     * @throws IOException
     * @throws ClassFileException
     */
    public void write(DataOutput dout) throws IOException, ClassFileException
    {
        dout.writeShort(this.u2typeIndex);
        dout.writeShort(this.memberValuePairTable.size());
        for (MemberValuePairInfo mvp : this.memberValuePairTable)
        {
            mvp.write(dout);
        }
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
        String oldType = cf.getUtf8(this.u2typeIndex);
        String newType = nm.mapDescriptor(oldType);
        this.u2typeIndex = cf.remapUtf8To(newType, this.u2typeIndex);
        for (MemberValuePairInfo mvp : this.memberValuePairTable)
        {
            mvp.remap(cf, nm);
        }
    }
}
