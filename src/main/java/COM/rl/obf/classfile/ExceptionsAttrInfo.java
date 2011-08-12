/* ===========================================================================
 * $RCSfile: ExceptionsAttrInfo.java,v $
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
 * Representation of an attribute.
 * 
 * @author Mark Welsh
 */
public class ExceptionsAttrInfo extends AttrInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u2numberOfExceptions;
    private int[] u2exceptionIndexTable;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    protected ExceptionsAttrInfo(ClassFile cf, int attrNameIndex, int attrLength)
    {
        super(cf, attrNameIndex, attrLength);
    }

    /** Return the String name of the attribute; over-ride this in sub-classes. */
    @Override
    protected String getAttrName()
    {
        return ClassConstants.ATTR_Exceptions;
    }

    /** Return the number of exception class indices. */
    public int count()
    {
        return this.u2exceptionIndexTable.length;
    }

    /** Return the i'th exception class indices. */
    public int getIndex(int i)
    {
        return this.u2exceptionIndexTable[i];
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
        this.u2numberOfExceptions = din.readUnsignedShort();
        this.u2exceptionIndexTable = new int[this.u2numberOfExceptions];
        for (int i = 0; i < this.u2numberOfExceptions; i++)
        {
            this.u2exceptionIndexTable[i] = din.readUnsignedShort();
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
        dout.writeShort(this.u2numberOfExceptions);
        for (int i = 0; i < this.u2numberOfExceptions; i++)
        {
            dout.writeShort(this.u2exceptionIndexTable[i]);
        }
    }
}
