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
 * @author Mark Welsh
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
    protected ClassItemInfo(ClassFile cf)
    {
        this.cf = cf;
    }

    /** Is the field or method 'Synthetic'? */
    public boolean isSynthetic()
    {
        return this.isSynthetic;
    }

    /** Return method/field name index into Constant Pool. */
    protected int getNameIndex()
    {
        return this.u2nameIndex;
    }

    /** Set the method/field name index. */
    protected void setNameIndex(int index)
    {
        this.u2nameIndex = index;
    }

    /** Return method/field descriptor index into Constant Pool. */
    protected int getDescriptorIndex()
    {
        return this.u2descriptorIndex;
    }

    /** Set the method/field descriptor index. */
    protected void setDescriptorIndex(int index)
    {
        this.u2descriptorIndex = index;
    }

    /** Return method/field string name. */
    public String getName() throws Exception
    {
        return ((Utf8CpInfo)this.cf.getCpEntry(this.u2nameIndex)).getString();
    }

    /** Return descriptor string. */
    public String getDescriptor() throws Exception
    {
        return ((Utf8CpInfo)this.cf.getCpEntry(this.u2descriptorIndex)).getString();
    }

    /** Return access flags. */
    public int getAccessFlags() throws Exception
    {
        return this.u2accessFlags;
    }

    /**
     * Trim attributes from the classfile ('Code', 'Exceptions', 'ConstantValue' are preserved, all others except the list in the
     * String[] are killed).
     */
    protected void trimAttrsExcept(String[] keepAttrs) throws Exception
    {
        // Traverse all attributes, removing all except those on 'keep' list
        for (int i = 0; i < this.attributes.length; i++)
        {
            if (Tools.isInArray(this.attributes[i].getAttrName(), keepAttrs))
            {
                this.attributes[i].trimAttrsExcept(keepAttrs);
            }
            else
            {
                this.attributes[i] = null;
            }
        }

        // Delete the marked attributes
        AttrInfo[] left = new AttrInfo[this.attributes.length];
        int j = 0;
        for (int i = 0; i < this.attributes.length; i++)
        {
            if (this.attributes[i] != null)
            {
                left[j++] = this.attributes[i];
            }
        }
        this.attributes = new AttrInfo[j];
        System.arraycopy(left, 0, this.attributes, 0, j);
        this.u2attributesCount = j;
    }

    /** Check for Utf8 references to constant pool and mark them. */
    protected void markUtf8Refs(ConstantPool pool) throws Exception
    {
        pool.incRefCount(this.u2nameIndex);
        pool.incRefCount(this.u2descriptorIndex);
        for (int i = 0; i < this.attributes.length; i++)
        {
            this.attributes[i].markUtf8Refs(pool);
        }
    }

    /** Import the field or method data to internal representation. */
    protected void read(DataInput din) throws Exception
    {
        this.u2accessFlags = din.readUnsignedShort();
        this.u2nameIndex = din.readUnsignedShort();
        this.u2descriptorIndex = din.readUnsignedShort();
        this.u2attributesCount = din.readUnsignedShort();
        this.attributes = new AttrInfo[this.u2attributesCount];
        for (int i = 0; i < this.u2attributesCount; i++)
        {
            this.attributes[i] = AttrInfo.create(din, this.cf);
            if (this.attributes[i].getAttrName().equals(ClassConstants.ATTR_Synthetic))
            {
                this.isSynthetic = true;
            }
        }
    }

    /** Export the representation to a DataOutput stream. */
    public void write(DataOutput dout) throws Exception
    {
        if (dout == null)
        {
            throw new IOException("No output stream was provided.");
        }
        dout.writeShort(this.u2accessFlags);
        dout.writeShort(this.u2nameIndex);
        dout.writeShort(this.u2descriptorIndex);
        dout.writeShort(this.u2attributesCount);
        for (int i = 0; i < this.u2attributesCount; i++)
        {
            this.attributes[i].write(dout);
        }
    }
}
