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
import java.util.ArrayList;
import java.util.List;

public class NestMembersAttrInfo extends AttrInfo
{
    // Constants -------------------------------------------------------------

    // Fields ----------------------------------------------------------------
    private List<Integer> classes;

    // Class Methods ---------------------------------------------------------

    // Instance Methods ------------------------------------------------------
    /**
     * Constructor
     * 
     * @param cf
     * @param attrNameIndex
     * @param attrLength
     */
    protected NestMembersAttrInfo(ClassFile cf, int attrNameIndex, int attrLength)
    {
        super(cf, attrNameIndex, attrLength);
    }

    /**
     * Return the String name of the attribute.
     */
    @Override
    protected String getAttrName()
    {
        return ClassConstants.ATTR_NestMembers;
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
        int count = din.readUnsignedShort();
        this.classes = new ArrayList<Integer>(count);
        for (short i = 0; i < count; i++)
        {
            this.classes.add(din.readUnsignedShort());
        }
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
        dout.writeShort(this.classes.size());
        for (int cls : this.classes)
        {
            dout.writeShort(cls);
        }
    }
}