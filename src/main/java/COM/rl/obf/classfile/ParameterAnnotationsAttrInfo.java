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
    private List<ParameterAnnotationsInfo> parameterAnnotationsTable;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /**
     * Constructor
     * 
     * @param cf
     * @param attrNameIndex
     * @param attrLength
     */
    protected ParameterAnnotationsAttrInfo(ClassFile cf, int attrNameIndex, int attrLength)
    {
        super(cf, attrNameIndex, attrLength);
    }

    /**
     * Return the list of parameter annotations table entries.
     */
    protected List<ParameterAnnotationsInfo> getParameterAnnotationsTable()
    {
        return this.parameterAnnotationsTable;
    }

    /**
     * Check for Utf8 references in the 'info' data to the constant pool and mark them.
     * 
     * @throws ClassFileException
     */
    @Override
    protected void markUtf8RefsInInfo(ConstantPool pool) throws ClassFileException
    {
        for (Iterator<ParameterAnnotationsInfo> iter = this.parameterAnnotationsTable.iterator(); iter.hasNext();)
        {
            ParameterAnnotationsInfo pa = iter.next();
            pa.markUtf8Refs(pool);
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
        this.u1numParameters = din.readUnsignedByte();
        this.parameterAnnotationsTable = new ArrayList<ParameterAnnotationsInfo>(this.u1numParameters);
        for (int i = 0; i < this.u1numParameters; i++)
        {
            this.parameterAnnotationsTable.add(ParameterAnnotationsInfo.create(din));
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
        dout.writeByte(this.u1numParameters);
        for (Iterator<ParameterAnnotationsInfo> iter = this.parameterAnnotationsTable.iterator(); iter.hasNext();)
        {
            ParameterAnnotationsInfo pa = iter.next();
            pa.write(dout);
        }
    }

    /**
     * Do necessary name remapping.
     * 
     * @throws ClassFileException
     */
    @Override
    protected void remap(ClassFile cf, NameMapper nm) throws ClassFileException
    {
        for (Iterator<ParameterAnnotationsInfo> iter = this.parameterAnnotationsTable.iterator(); iter.hasNext();)
        {
            ParameterAnnotationsInfo pa = iter.next();
            pa.remap(cf, nm);
        }
    }
}
