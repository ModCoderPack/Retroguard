/* ===========================================================================
 * $RCSfile: MemberValueInfo.java,v $
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
 * Representation of an annotation member's value entry.
 *
 * @author      Mark Welsh
 */
public class MemberValueInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u1tag;
    private int u2constValueIndex;           // Union: const_value_index
    private int u2typeNameIndex;             // Union:     enum_const_value
    private int u2constNameIndex;            //            enum_const_value
    private int u2classInfoIndex;            // Union: class_info_index
    private AnnotationInfo annotationValue;  // Union:     annotation_value
    private int u2numValues;                 // Union: array_value
    private MemberValueInfo[] values;        //        array_value


    // Class Methods ---------------------------------------------------------
    public static MemberValueInfo create(DataInput din) throws Exception
    {
        MemberValueInfo mvi = new MemberValueInfo();
        mvi.read(din);
        return mvi;
    }


    // Instance Methods ------------------------------------------------------
    private MemberValueInfo() {}

    /** Return tag, defining member type. */
    protected int getTag() {return u1tag;}

    /** Check for Utf8 references to constant pool and mark them. */
    protected void markUtf8Refs(ConstantPool pool) throws Exception
    {
        switch (u1tag)
        {
        case 'B': case 'C': case 'D': case 'F': 
        case 'I': case 'J': case 'S': case 'Z':
            break;
        case 's':
            pool.incRefCount(u2constValueIndex);
            break;
        case 'e':
            pool.incRefCount(u2typeNameIndex);
            pool.incRefCount(u2constNameIndex);
            break;
        case 'c':
            pool.incRefCount(u2classInfoIndex);
            break;
        case '@':
            annotationValue.markUtf8Refs(pool);
            break;
        case '[':
            for (int i = 0; i < u2numValues; i++) 
            {
                values[i].markUtf8Refs(pool);
            }
            break;
        default:
            throw new Exception("Illegal tag value in annotation attribute member_value structure: " + u1tag);
        }
    }

    private void read(DataInput din) throws Exception
    {
        u1tag = din.readUnsignedByte();
        switch (u1tag)
        {
        case 'B': case 'C': case 'D': case 'F': 
        case 'I': case 'J': case 'S': case 'Z':
        case 's':
            u2constValueIndex = din.readUnsignedShort();
            break;
        case 'e':
            u2typeNameIndex = din.readUnsignedShort();
            u2constNameIndex = din.readUnsignedShort();
            break;
        case 'c':
            u2classInfoIndex = din.readUnsignedShort();
            break;
        case '@':
            annotationValue = AnnotationInfo.create(din);
            break;
        case '[':
            u2numValues = din.readUnsignedShort();
            values = new MemberValueInfo[u2numValues];
            for (int i = 0; i < u2numValues; i++)
            {
                values[i] = MemberValueInfo.create(din);
            }
            break;
        default:
            throw new Exception("Illegal tag value in annotation attribute member_value structure: " + u1tag);
        }
    }

    /** Export the representation to a DataOutput stream. */
    public void write(DataOutput dout) throws Exception
    {
        dout.writeByte(u1tag);
        switch (u1tag)
        {
        case 'B': case 'C': case 'D': case 'F': 
        case 'I': case 'J': case 'S': case 'Z':
        case 's':
            dout.writeShort(u2constValueIndex);
            break;
        case 'e':
            dout.writeShort(u2typeNameIndex);
            dout.writeShort(u2constNameIndex);
            break;
        case 'c':
            dout.writeShort(u2classInfoIndex);
            break;
        case '@':
            annotationValue.write(dout);
            break;
        case '[':
            dout.writeShort(u2numValues);
            for (int i = 0; i < u2numValues; i++) 
            {
                values[i].write(dout);
            }
            break;
        default:
            throw new Exception("Illegal tag value in annotation attribute member_value structure: " + u1tag);
        }
    }

    /** Do necessary name remapping. */
    protected void remap(ClassFile cf, NameMapper nm) throws Exception 
    { 
        switch (u1tag)
        {
        case 'B': case 'C': case 'D': case 'F': 
        case 'I': case 'J': case 'S': case 'Z':
        case 's':
            break;
        case 'e':
            break;
        case 'c':
            String oldDesc = cf.getUtf8(u2classInfoIndex);
            String newDesc = nm.mapDescriptor(oldDesc);
            u2classInfoIndex = cf.remapUtf8To(newDesc, u2classInfoIndex);
            break;
        case '@':
            annotationValue.remap(cf, nm);
            break;
        case '[':
            for (int i = 0; i < u2numValues; i++) 
            {
                values[i].remap(cf, nm);
            }
            break;
        default:
            throw new Exception("Illegal tag value in annotation attribute member_value structure: " + u1tag);
        }
    }

    /** Provide debugging dump of this object. */
    public void dump(PrintStream ps, ClassFile cf) throws Exception
    {
        ps.println("u1tag : " + u1tag);
    }
}
