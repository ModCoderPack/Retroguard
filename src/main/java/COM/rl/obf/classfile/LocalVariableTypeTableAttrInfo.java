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

package COM.rl.obf.classfile;

import java.io.*;
import java.util.*;

/**
 * Representation of an attribute.
 *
 * @author      Mark Welsh
 */
public class LocalVariableTypeTableAttrInfo extends AttrInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u2localVariableTypeTableLength;
    private LocalVariableTypeInfo[] localVariableTypeTable;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    protected LocalVariableTypeTableAttrInfo(ClassFile cf, int attrNameIndex, int attrLength)
    {
        super(cf, attrNameIndex, attrLength);
    }

    /** Return the String name of the attribute; over-ride this in sub-classes. */
    protected String getAttrName() throws Exception
    {
        return ATTR_LocalVariableTypeTable;
    }

    /** Return the array of local variable type table entries. */
    protected LocalVariableTypeInfo[] getLocalVariableTypeTable() throws Exception
    {
        return localVariableTypeTable;
    }

    /** Check for Utf8 references in the 'info' data to the constant pool and mark them. */
    protected void markUtf8RefsInInfo(ConstantPool pool) throws Exception
    {
        for (int i = 0; i < localVariableTypeTable.length; i++)
        {
            localVariableTypeTable[i].markUtf8Refs(pool);
        }
    }

    /** Read the data following the header. */
    protected void readInfo(DataInput din) throws Exception
    {
        u2localVariableTypeTableLength = din.readUnsignedShort();
        localVariableTypeTable = new LocalVariableTypeInfo[u2localVariableTypeTableLength];
        for (int i = 0; i < u2localVariableTypeTableLength; i++)
        {
            localVariableTypeTable[i] = LocalVariableTypeInfo.create(din);
        }
    }

    /** Export data following the header to a DataOutput stream. */
    public void writeInfo(DataOutput dout) throws Exception
    {
        dout.writeShort(u2localVariableTypeTableLength);
        for (int i = 0; i < u2localVariableTypeTableLength; i++)
        {
            localVariableTypeTable[i].write(dout);
        }
    }

    /** Do necessary name remapping. */
    protected void remap(ClassFile cf, NameMapper nm) throws Exception 
    { 
        for (int i = 0; i < u2localVariableTypeTableLength; i++)
        {
            localVariableTypeTable[i].remap(cf, nm);
        }
    }
}

