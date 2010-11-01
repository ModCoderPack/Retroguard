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

package COM.rl.obf.classfile;

import java.io.*;
import java.util.*;

/**
 * Representation of an annotation table entry.
 *
 * @author      Mark Welsh
 */
public class AnnotationInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u2typeIndex;
    private int u2numMemberValuePairs;
    private MemberValuePairInfo[] memberValuePairTable;


    // Class Methods ---------------------------------------------------------
    public static AnnotationInfo create(DataInput din) throws Exception
    {
        AnnotationInfo ai = new AnnotationInfo();
        ai.read(din);
        return ai;
    }


    // Instance Methods ------------------------------------------------------
    private AnnotationInfo() {}

    /** Return type index into Constant Pool. */
    protected int getTypeIndex() {return u2typeIndex;}

    /** Check for Utf8 references to constant pool and mark them. */
    protected void markUtf8Refs(ConstantPool pool) throws Exception
    {
        pool.incRefCount(u2typeIndex);
        for (int i = 0; i < memberValuePairTable.length; i++)
        {
            memberValuePairTable[i].markUtf8Refs(pool);
        }
    }

    private void read(DataInput din) throws Exception
    {
        u2typeIndex = din.readUnsignedShort();
        u2numMemberValuePairs = din.readUnsignedShort();
        memberValuePairTable = new MemberValuePairInfo[u2numMemberValuePairs];
        for (int i = 0; i < u2numMemberValuePairs; i++)
        {
            memberValuePairTable[i] = MemberValuePairInfo.create(din);
        }
    }

    /** Export the representation to a DataOutput stream. */
    public void write(DataOutput dout) throws Exception
    {
        dout.writeShort(u2typeIndex);
        dout.writeShort(u2numMemberValuePairs);
        for (int i = 0; i < u2numMemberValuePairs; i++)
        {
            memberValuePairTable[i].write(dout);
        }
    }

    /** Do necessary name remapping. */
    protected void remap(ClassFile cf, NameMapper nm) throws Exception 
    { 
        String oldType = cf.getUtf8(u2typeIndex);
        String newType = nm.mapDescriptor(oldType);
        u2typeIndex = cf.remapUtf8To(newType, u2typeIndex);
        for (int i = 0; i < u2numMemberValuePairs; i++)
        {
            memberValuePairTable[i].remap(cf, nm);
        }
    }

    /** Provide debugging dump of this object. */
    public void dump(PrintStream ps, ClassFile cf) throws Exception
    {
        ps.println("u2typeIndex : " + u2typeIndex + " " + cf.getUtf8(u2typeIndex));
        ps.println("u2numMemberValuePairs : " + u2numMemberValuePairs);
        for (int i = 0; i < u2numMemberValuePairs; i++)
        {
            memberValuePairTable[i].dump(ps, cf);
        }
    }
}
