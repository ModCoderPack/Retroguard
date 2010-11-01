/* ===========================================================================
 * $RCSfile: EnclosingMethodAttrInfo.java,v $
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
public class EnclosingMethodAttrInfo extends AttrInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u2classIndex;
    private int u2methodIndex;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    protected EnclosingMethodAttrInfo(ClassFile cf, int attrNameIndex, int attrLength)
    {
        super(cf, attrNameIndex, attrLength);
    }

    /** Return the String name of the attribute. */
    protected String getAttrName() throws Exception
    {
        return ATTR_EnclosingMethod;
    }

    /** Return the class index into the constant pool. */
    protected int getClassIndex() throws Exception
    {
        return u2classIndex;
    }

    /** Return the method index into the constant pool. */
    protected int getMethodIndex() throws Exception
    {
        return u2methodIndex;
    }

    /** Read the data following the header. */
    protected void readInfo(DataInput din) throws Exception
    {
        u2classIndex = din.readUnsignedShort();
        u2methodIndex = din.readUnsignedShort();
    }

    /** Export data following the header to a DataOutput stream. */
    public void writeInfo(DataOutput dout) throws Exception
    {
        dout.writeShort(u2classIndex);
        dout.writeShort(u2methodIndex);
    }

    /** Do necessary name remapping. */
    protected void remap(ClassFile cf, NameMapper nm) throws Exception 
    { 
        // No remap needed
    }
}

