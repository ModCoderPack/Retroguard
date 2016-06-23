/* ===========================================================================
 * $RCSfile: LocalVariableTypeTableAttrInfo.java,v $
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

package COM_.rl.obf.classfile;

import java.io.*;
import java.util.*;

/**
 * Representation of an attribute.
 * 
 * @author Mark Welsh
 */
public class LocalVariableTypeTableAttrInfo extends AttrInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private List<LocalVariableTypeInfo> localVariableTypeTable;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /**
     * Constructor
     * 
     * @param cf
     * @param attrNameIndex
     * @param attrLength
     */
    protected LocalVariableTypeTableAttrInfo(ClassFile cf, int attrNameIndex, int attrLength)
    {
        super(cf, attrNameIndex, attrLength);
    }

    /**
     * Return the String name of the attribute; over-ride this in sub-classes.
     */
    @Override
    protected String getAttrName()
    {
        return ClassConstants.ATTR_LocalVariableTypeTable;
    }

    /**
     * Check for Utf8 references in the 'info' data to the constant pool and mark them.
     * 
     * @throws ClassFileException
     */
    @Override
    protected void markUtf8RefsInInfo(ConstantPool pool) throws ClassFileException
    {
        for (LocalVariableTypeInfo lvt : this.localVariableTypeTable)
        {
            lvt.markUtf8Refs(pool);
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
        int u2localVariableTypeTableLength = din.readUnsignedShort();
        this.localVariableTypeTable = new ArrayList<LocalVariableTypeInfo>(u2localVariableTypeTableLength);
        for (int i = 0; i < u2localVariableTypeTableLength; i++)
        {
            this.localVariableTypeTable.add(LocalVariableTypeInfo.create(din));
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
        dout.writeShort(this.localVariableTypeTable.size());
        for (LocalVariableTypeInfo lvt : this.localVariableTypeTable)
        {
            lvt.write(dout);
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
        for (LocalVariableTypeInfo lvt : this.localVariableTypeTable)
        {
            lvt.remap(cf, nm);
        }
    }
}
