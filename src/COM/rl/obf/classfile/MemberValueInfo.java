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
 * @author Mark Welsh
 */
public class MemberValueInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u1tag;

    private int u2constValueIndex;

    private int u2typeNameIndex;
    private int u2constNameIndex;

    private int u2classInfoIndex;

    private AnnotationInfo annotationValue;

    private List<MemberValueInfo> values;


    // Class Methods ---------------------------------------------------------
    /**
     * @param din
     * @throws IOException
     * @throws ClassFileException
     */
    public static MemberValueInfo create(DataInput din) throws IOException, ClassFileException
    {
        MemberValueInfo mvi = new MemberValueInfo();
        mvi.read(din);
        return mvi;
    }


    // Instance Methods ------------------------------------------------------
    /**
     * Private constructor
     */
    private MemberValueInfo()
    {
    }

    /**
     * Return tag, defining member type.
     */
    protected int getTag()
    {
        return this.u1tag;
    }

    /**
     * Check for Utf8 references to constant pool and mark them.
     * 
     * @param pool
     * @throws ClassFileException
     */
    protected void markUtf8Refs(ConstantPool pool) throws ClassFileException
    {
        switch (this.u1tag)
        {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'Z':
                break;
            case 's':
                pool.incRefCount(this.u2constValueIndex);
                break;
            case 'e':
                pool.incRefCount(this.u2typeNameIndex);
                pool.incRefCount(this.u2constNameIndex);
                break;
            case 'c':
                pool.incRefCount(this.u2classInfoIndex);
                break;
            case '@':
                this.annotationValue.markUtf8Refs(pool);
                break;
            case '[':
                for (MemberValueInfo mv : this.values)
                {
                    mv.markUtf8Refs(pool);
                }
                break;
            default:
                throw new ClassFileException("Illegal tag value in annotation attribute member_value structure: " + this.u1tag);
        }
    }

    /**
     * @param din
     * @throws IOException
     * @throws ClassFileException
     */
    private void read(DataInput din) throws IOException, ClassFileException
    {
        this.u1tag = din.readUnsignedByte();
        int u2numValues;
        switch (this.u1tag)
        {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'Z':
            case 's':
                this.u2constValueIndex = din.readUnsignedShort();
                break;
            case 'e':
                this.u2typeNameIndex = din.readUnsignedShort();
                this.u2constNameIndex = din.readUnsignedShort();
                break;
            case 'c':
                this.u2classInfoIndex = din.readUnsignedShort();
                break;
            case '@':
                this.annotationValue = AnnotationInfo.create(din);
                break;
            case '[':
                u2numValues = din.readUnsignedShort();
                this.values = new ArrayList<MemberValueInfo>(u2numValues);
                for (int i = 0; i < u2numValues; i++)
                {
                    this.values.add(MemberValueInfo.create(din));
                }
                break;
            default:
                throw new ClassFileException("Illegal tag value in annotation attribute member_value structure: " + this.u1tag);
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
        dout.writeByte(this.u1tag);
        switch (this.u1tag)
        {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'Z':
            case 's':
                dout.writeShort(this.u2constValueIndex);
                break;
            case 'e':
                dout.writeShort(this.u2typeNameIndex);
                dout.writeShort(this.u2constNameIndex);
                break;
            case 'c':
                dout.writeShort(this.u2classInfoIndex);
                break;
            case '@':
                this.annotationValue.write(dout);
                break;
            case '[':
                dout.writeShort(this.values.size());
                for (MemberValueInfo mv : this.values)
                {
                    mv.write(dout);
                }
                break;
            default:
                throw new ClassFileException("Illegal tag value in annotation attribute member_value structure: " + this.u1tag);
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
        switch (this.u1tag)
        {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'Z':
            case 's':
                break;
            case 'e':
                break;
            case 'c':
                String oldDesc = cf.getUtf8(this.u2classInfoIndex);
                String newDesc = nm.mapDescriptor(oldDesc);
                this.u2classInfoIndex = cf.remapUtf8To(newDesc, this.u2classInfoIndex);
                break;
            case '@':
                this.annotationValue.remap(cf, nm);
                break;
            case '[':
                for (MemberValueInfo mv : this.values)
                {
                    mv.remap(cf, nm);
                }
                break;
            default:
                throw new ClassFileException("Illegal tag value in annotation attribute member_value structure: " + this.u1tag);
        }
    }
}
