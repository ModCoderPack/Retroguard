/* ===========================================================================
 * $RCSfile: StackMapTableAttrInfo.java,v $
 * ===========================================================================
 *
 * RetroGuard -- an obfuscation package for Java classfiles.
 *
 * Copyright (c) 1998-2007 Mark Welsh (markw@retrologic.com)
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
public class StackMapTableAttrInfo extends AttrInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private List<StackMapFrameInfo> entries;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /**
     * Constructor
     * 
     * @param cf
     * @param attrNameIndex
     * @param attrLength
     */
    protected StackMapTableAttrInfo(ClassFile cf, int attrNameIndex, int attrLength)
    {
        super(cf, attrNameIndex, attrLength);
    }

    /**
     * Return the String name of the attribute; over-ride this in sub-classes.
     */
    @Override
    protected String getAttrName()
    {
        return ClassConstants.ATTR_StackMapTable;
    }

    /**
     * Check for Utf8 references in the 'info' data to the constant pool and mark them.
     * 
     * @throws ClassFileException
     */
    @Override
    protected void markUtf8RefsInInfo(ConstantPool pool) throws ClassFileException
    {
        for (StackMapFrameInfo smf : this.entries)
        {
            smf.markUtf8Refs(pool);
        }
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
        int u2numberOfEntries = din.readUnsignedShort();
        this.entries = new ArrayList<StackMapFrameInfo>(u2numberOfEntries);
        for (int i = 0; i < u2numberOfEntries; i++)
        {
            StackMapFrameInfo smf = StackMapFrameInfo.create(din);
            this.entries.add(smf);
        }
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
        dout.writeShort(this.entries.size());
        for (StackMapFrameInfo smf : this.entries)
        {
            smf.write(dout);
        }
    }
}
