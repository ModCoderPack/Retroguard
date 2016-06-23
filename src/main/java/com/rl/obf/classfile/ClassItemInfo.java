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

package com.rl.obf.classfile;

import java.io.*;
import java.util.*;

import com.rl.util.*;

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
    protected List<AttrInfo> attributes;

    protected ClassFile cf;
    private boolean isSynthetic = false;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /**
     * Constructor
     * 
     * @param cf
     */
    protected ClassItemInfo(ClassFile cf)
    {
        this.cf = cf;
    }

    /**
     * Is the field or method 'Synthetic'?
     */
    public boolean isSynthetic()
    {
        return this.isSynthetic;
    }

    /**
     * Return method/field name index into Constant Pool.
     */
    protected int getNameIndex()
    {
        return this.u2nameIndex;
    }

    /**
     * Set the method/field name index.
     * 
     * @param index
     */
    protected void setNameIndex(int index)
    {
        this.u2nameIndex = index;
    }

    /**
     * Return method/field descriptor index into Constant Pool.
     */
    protected int getDescriptorIndex()
    {
        return this.u2descriptorIndex;
    }

    /**
     * Set the method/field descriptor index.
     * 
     * @param index
     */
    protected void setDescriptorIndex(int index)
    {
        this.u2descriptorIndex = index;
    }

    /**
     * Return method/field string name.
     * 
     * @throws ClassFileException
     */
    public String getName() throws ClassFileException
    {
        return this.cf.getUtf8(this.u2nameIndex);
    }

    /**
     * Return descriptor string.
     * 
     * @throws ClassFileException
     */
    public String getDescriptor() throws ClassFileException
    {
        return this.cf.getUtf8(this.u2descriptorIndex);
    }

    /**
     * Return access flags.
     */
    public int getAccessFlags()
    {
        return this.u2accessFlags;
    }

    /**
     * Trim attributes from the classfile ('Code', 'Exceptions', 'ConstantValue' are preserved, all others except those in the
     * {@code List<String>} are killed).
     * 
     * @param keepAttrs
     */
    protected void trimAttrsExcept(List<String> keepAttrs)
    {
        // Traverse all attributes, removing all except those on 'keep' list
        List<AttrInfo> delAttrs = new ArrayList<AttrInfo>();
        for (AttrInfo at : this.attributes)
        {
            if (keepAttrs.contains(at.getAttrName()))
            {
                at.trimAttrsExcept(keepAttrs);
            }
            else
            {
                delAttrs.add(at);
            }
        }

        this.attributes.removeAll(delAttrs);
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
        for (AttrInfo at : this.attributes)
        {
            at.markUtf8Refs(pool);
        }
    }

    /**
     * Import the field or method data to internal representation.
     * 
     * @param din
     * @throws IOException
     * @throws ClassFileException
     */
    protected void read(DataInput din) throws IOException, ClassFileException
    {
        this.u2accessFlags = din.readUnsignedShort();
        this.u2nameIndex = din.readUnsignedShort();
        this.u2descriptorIndex = din.readUnsignedShort();
        int u2attributesCount = din.readUnsignedShort();
        this.attributes = new ArrayList<AttrInfo>(u2attributesCount);
        for (int i = 0; i < u2attributesCount; i++)
        {
            AttrInfo at = AttrInfo.create(din, this.cf, (this instanceof FieldInfo ? AttrSource.FIELD : AttrSource.METHOD));
            this.attributes.add(at);
            if (at.getAttrName().equals(ClassConstants.ATTR_Synthetic))
            {
                this.isSynthetic = true;
            }
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
        if (dout == null)
        {
            throw new IOException("No output stream was provided.");
        }
        dout.writeShort(this.u2accessFlags);
        dout.writeShort(this.u2nameIndex);
        dout.writeShort(this.u2descriptorIndex);
        dout.writeShort(this.attributes.size());
        for (AttrInfo at : this.attributes)
        {
            at.write(dout);
        }
    }
}
