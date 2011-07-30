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

package COM.rl.obf.classfile;

import java.io.*;
import java.util.*;

/**
 * Representation of an attribute.
 *
 * @author      Mark Welsh
 */
public class StackMapTableAttrInfo extends AttrInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u2numberOfEntries;
    private StackMapFrameInfo entries[];


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    protected StackMapTableAttrInfo(ClassFile cf, int attrNameIndex, int attrLength)
    {
        super(cf, attrNameIndex, attrLength);
    }

    /** Return the String name of the attribute; over-ride this in sub-classes. */
    @Override
    protected String getAttrName() throws Exception
    {
        return ClassConstants.ATTR_StackMapTable;
    }

    /** Check for Utf8 references in the 'info' data to the constant pool and mark them. */
    @Override
    protected void markUtf8RefsInInfo(ConstantPool pool) throws Exception
    {
        for (int i = 0; i < this.u2numberOfEntries; i++)
        {
            this.entries[i].markUtf8Refs(pool);
        }
    }

    /** Read the data following the header. */
    @Override
    protected void readInfo(DataInput din) throws Exception
    {
        this.u2numberOfEntries = din.readUnsignedShort();
        this.entries = new StackMapFrameInfo[this.u2numberOfEntries];
        for (int i = 0; i < this.u2numberOfEntries; i++)
        {
            this.entries[i] = StackMapFrameInfo.create(din);
        }
    }

    /** Export data following the header to a DataOutput stream. */
    @Override
    public void writeInfo(DataOutput dout) throws Exception
    {
        dout.writeShort(this.u2numberOfEntries);
        for (int i = 0; i < this.u2numberOfEntries; i++)
        {
            this.entries[i].write(dout);
        }
    }
}

