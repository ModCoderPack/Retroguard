/* ===========================================================================
 * $RCSfile: EnclosingMethodAttrInfo.java,v $
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
 * Representation of an attribute.
 * 
 * @author Mark Welsh
 */
public class EnclosingMethodAttrInfo extends AttrInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u2classIndex;
    private int u2methodIndex;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /**
     * Constructor
     * 
     * @param cf
     * @param attrNameIndex
     * @param attrLength
     */
    protected EnclosingMethodAttrInfo(ClassFile cf, int attrNameIndex, int attrLength)
    {
        super(cf, attrNameIndex, attrLength);
    }

    /**
     * Return the String name of the attribute.
     */
    @Override
    protected String getAttrName()
    {
        return ClassConstants.ATTR_EnclosingMethod;
    }

    /**
     * Return the class index into the constant pool.
     */
    protected int getClassIndex()
    {
        return this.u2classIndex;
    }

    /**
     * Return the method index into the constant pool.
     */
    protected int getMethodIndex()
    {
        return this.u2methodIndex;
    }

    /**
     * Read the data following the header.
     * 
     * @throws IOException
     * @throws ClassFileException
     */
    @Override
    protected void readInfo(DataInput din) throws IOException, ClassFileException
    {
        this.u2classIndex = din.readUnsignedShort();
        this.u2methodIndex = din.readUnsignedShort();
    }

    /**
     * Export data following the header to a DataOutput stream.
     * 
     * @throws IOException
     * @throws ClassFileException
     */
    @Override
    public void writeInfo(DataOutput dout) throws IOException, ClassFileException
    {
        dout.writeShort(this.u2classIndex);
        dout.writeShort(this.u2methodIndex);
    }

    /**
     * Do necessary name remapping.
     * 
     * @param cf
     * @param nm
     */
    @Override
    protected void remap(ClassFile cf, NameMapper nm)
    {
        // No remap needed
    }
}
