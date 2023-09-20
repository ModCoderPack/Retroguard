/**
 * RetroGuard -- an obfuscation package for Java classfiles.
 *
 * Copyright (c) 1998-2006 Mark Welsh (markw@retrologic.com)
 * 
 * This class was created by r3qu13m (r3qu13m.minecraft@gmail.com)
 *
 * This program can be redistributed and/or modified under the terms of the
 * Version 2 of the GNU General Public License as published by the Free
 * Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package com.rl.obf.classfile;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NestHostAttrInfo extends AttrInfo
{
    // Constants -------------------------------------------------------------

    // Fields ----------------------------------------------------------------
    private int indexHostClass;

    // Class Methods ---------------------------------------------------------

    // Instance Methods ------------------------------------------------------
    /**
     * Constructor
     * 
     * @param cf
     * @param attrNameIndex
     * @param attrLength
     */
    protected NestHostAttrInfo(ClassFile cf, int attrNameIndex, int attrLength)
    {
        super(cf, attrNameIndex, attrLength);
    }

    /**
     * Return the String name of the attribute.
     */
    @Override
    protected String getAttrName()
    {
        return ClassConstants.ATTR_NestHost;
    }

    /**
     * Read the data following the header; over-ride this in sub-classes.
     * 
     * @param din
     * @throws IOException
     * @throws ClassFileException
     */
    protected void readInfo(DataInput din) throws IOException, ClassFileException
    {
        this.indexHostClass = din.readUnsignedShort();
    }

    /**
     * Export data following the header to a DataOutput stream; over-ride this in sub-classes.
     * 
     * @param dout
     * @throws IOException
     * @throws ClassFileException
     */
    public void writeInfo(DataOutput dout) throws IOException, ClassFileException
    {
        dout.writeShort(indexHostClass);
    }
}
