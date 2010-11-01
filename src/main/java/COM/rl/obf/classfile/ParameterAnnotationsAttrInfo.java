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
 * @author      Mark Welsh
 */
abstract public class ParameterAnnotationsAttrInfo extends AttrInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u1numParameters;
    private ParameterAnnotationsInfo[] parameterAnnotationsTable;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    protected ParameterAnnotationsAttrInfo(ClassFile cf, int attrNameIndex, int attrLength)
    {
        super(cf, attrNameIndex, attrLength);
    }

    /** Return the array of parameter annotations table entries. */
    protected ParameterAnnotationsInfo[] getParameterAnnotationsTable() throws Exception
    {
        return parameterAnnotationsTable;
    }

    /** Check for Utf8 references in the 'info' data to the constant pool and mark them. */
    protected void markUtf8RefsInInfo(ConstantPool pool) throws Exception
    {
        for (int i = 0; i < parameterAnnotationsTable.length; i++)
        {
            parameterAnnotationsTable[i].markUtf8Refs(pool);
        }
    }

    /** Read the data following the header. */
    protected void readInfo(DataInput din) throws Exception
    {
        u1numParameters = din.readUnsignedByte();
        parameterAnnotationsTable = new ParameterAnnotationsInfo[u1numParameters];
        for (int i = 0; i < u1numParameters; i++)
        {
            parameterAnnotationsTable[i] = ParameterAnnotationsInfo.create(din);
        }
    }

    /** Export data following the header to a DataOutput stream. */
    public void writeInfo(DataOutput dout) throws Exception
    {
        dout.writeByte(u1numParameters);
        for (int i = 0; i < u1numParameters; i++)
        {
            parameterAnnotationsTable[i].write(dout);
        }
    }

    /** Do necessary name remapping. */
    protected void remap(ClassFile cf, NameMapper nm) throws Exception 
    { 
        for (int i = 0; i < u1numParameters; i++)
        {
            parameterAnnotationsTable[i].remap(cf, nm);
        }
    }
}

