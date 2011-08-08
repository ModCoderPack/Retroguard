/* ===========================================================================
 * $RCSfile: ParameterAnnotationsAttrInfo.java,v $
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
abstract public class ParameterAnnotationsAttrInfo extends AttrInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u1numParameters;
    private List parameterAnnotationsTable;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    protected ParameterAnnotationsAttrInfo(ClassFile cf, int attrNameIndex, int attrLength)
    {
        super(cf, attrNameIndex, attrLength);
    }

    /** Return the array of parameter annotations table entries. */
    protected ParameterAnnotationsInfo[] getParameterAnnotationsTable() throws Exception
    {
        return (ParameterAnnotationsInfo[])this.parameterAnnotationsTable.toArray(new ParameterAnnotationsInfo[0]);
    }

    /** Check for Utf8 references in the 'info' data to the constant pool and mark them. */
    @Override
    protected void markUtf8RefsInInfo(ConstantPool pool) throws Exception
    {
        for (Iterator iter = this.parameterAnnotationsTable.iterator(); iter.hasNext();)
        {
            ParameterAnnotationsInfo pa = (ParameterAnnotationsInfo)iter.next();
            pa.markUtf8Refs(pool);
        }
    }

    /** Read the data following the header. */
    @Override
    protected void readInfo(DataInput din) throws Exception
    {
        this.u1numParameters = din.readUnsignedByte();
        this.parameterAnnotationsTable = new ArrayList(this.u1numParameters);
        for (int i = 0; i < this.u1numParameters; i++)
        {
            this.parameterAnnotationsTable.add(ParameterAnnotationsInfo.create(din));
        }
    }

    /** Export data following the header to a DataOutput stream. */
    @Override
    public void writeInfo(DataOutput dout) throws Exception
    {
        dout.writeByte(this.u1numParameters);
        for (Iterator iter = this.parameterAnnotationsTable.iterator(); iter.hasNext();)
        {
            ParameterAnnotationsInfo pa = (ParameterAnnotationsInfo)iter.next();
            pa.write(dout);
        }
    }

    /** Do necessary name remapping. */
    @Override
    protected void remap(ClassFile cf, NameMapper nm) throws Exception
    {
        for (Iterator iter = this.parameterAnnotationsTable.iterator(); iter.hasNext();)
        {
            ParameterAnnotationsInfo pa = (ParameterAnnotationsInfo)iter.next();
            pa.remap(cf, nm);
        }
    }
}
