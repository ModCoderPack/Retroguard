/* ===========================================================================
 * $RCSfile: ClassItemInfo.java,v $
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
import COM.rl.util.*;

/**
 * Representation of a field or method from a class-file.
 *
 * @author      Mark Welsh
 */
abstract public class ClassItemInfo implements ClassConstants
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u2accessFlags;
    private int u2nameIndex;
    private int u2descriptorIndex;
    protected int u2attributesCount;
    protected AttrInfo attributes[];

    protected ClassFile cf;
    private boolean isSynthetic = false;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    protected ClassItemInfo(ClassFile cf) {this.cf = cf;}

    /** Is the field or method 'Synthetic'? */
    public boolean isSynthetic() {return isSynthetic;}

    /** Return method/field name index into Constant Pool. */
    protected int getNameIndex() {return u2nameIndex;}

    /** Set the method/field name index. */
    protected void setNameIndex(int index) {u2nameIndex = index;}

    /** Return method/field descriptor index into Constant Pool. */
    protected int getDescriptorIndex() {return u2descriptorIndex;}

    /** Set the method/field descriptor index. */
    protected void setDescriptorIndex(int index) {u2descriptorIndex = index;}

    /** Return method/field string name. */
    public String getName() throws Exception
    {
        return ((Utf8CpInfo)cf.getCpEntry(u2nameIndex)).getString();
    }

    /** Return descriptor string. */
    public String getDescriptor() throws Exception
    {
        return ((Utf8CpInfo)cf.getCpEntry(u2descriptorIndex)).getString();
    }

    /** Return access flags. */
    public int getAccessFlags() throws Exception
    {
        return u2accessFlags;
    }

    /**
     * Trim attributes from the classfile ('Code', 'Exceptions', 'ConstantValue'
     * are preserved, all others except the list in the String[] are killed).
     */
    protected void trimAttrsExcept(String[] keepAttrs) throws Exception
    {
        // Traverse all attributes, removing all except those on 'keep' list
        for (int i = 0; i < attributes.length; i++)
        {
            if (Tools.isInArray(attributes[i].getAttrName(), keepAttrs))
            {
                attributes[i].trimAttrsExcept(keepAttrs);
            }
            else
            {
                attributes[i] = null;
            }
        }

        // Delete the marked attributes
        AttrInfo[] left = new AttrInfo[attributes.length];
        int j = 0;
        for (int i = 0; i < attributes.length; i++)
        {
            if (attributes[i] != null)
            {
                left[j++] = attributes[i];
            }
        }
        attributes = new AttrInfo[j];
        System.arraycopy(left, 0, attributes, 0, j);
        u2attributesCount = j;
    }

    /** Check for Utf8 references to constant pool and mark them. */
    protected void markUtf8Refs(ConstantPool pool) throws Exception
    {
        pool.incRefCount(u2nameIndex);
        pool.incRefCount(u2descriptorIndex);
        for (int i = 0; i < attributes.length; i++)
        {
            attributes[i].markUtf8Refs(pool);
        }
    }

    /** List the constant pool entries references from this method or field. */
    public Enumeration listCpRefs() throws Exception {return null;}

    /** Import the field or method data to internal representation. */
    protected void read(DataInput din) throws Exception
    {
        u2accessFlags = din.readUnsignedShort();
        u2nameIndex = din.readUnsignedShort();
        u2descriptorIndex = din.readUnsignedShort();
        u2attributesCount = din.readUnsignedShort();
        attributes = new AttrInfo[u2attributesCount];
        for (int i = 0; i < u2attributesCount; i++)
        {
            attributes[i] = AttrInfo.create(din, cf);
            if (attributes[i].getAttrName().equals(ATTR_Synthetic))
            {
                isSynthetic = true;
            }
        }
    }

    /** Export the representation to a DataOutput stream. */
    public void write(DataOutput dout) throws Exception
    {
        if (dout == null) throw new IOException("No output stream was provided.");
        dout.writeShort(u2accessFlags);
        dout.writeShort(u2nameIndex);
        dout.writeShort(u2descriptorIndex);
        dout.writeShort(u2attributesCount);
        for (int i = 0; i < u2attributesCount; i++)
        {
            attributes[i].write(dout);
        }
    }
}
