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
 * @author Mark Welsh
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
    @Override
    protected String getAttrName() throws Exception
    {
        return ClassConstants.ATTR_EnclosingMethod;
    }

    /** Return the class index into the constant pool. */
    protected int getClassIndex() throws Exception
    {
        return this.u2classIndex;
    }

    /** Return the method index into the constant pool. */
    protected int getMethodIndex() throws Exception
    {
        return this.u2methodIndex;
    }

    /** Read the data following the header. */
    @Override
    protected void readInfo(DataInput din) throws Exception
    {
        this.u2classIndex = din.readUnsignedShort();
        this.u2methodIndex = din.readUnsignedShort();
    }

    /** Export data following the header to a DataOutput stream. */
    @Override
    public void writeInfo(DataOutput dout) throws Exception
    {
        dout.writeShort(this.u2classIndex);
        dout.writeShort(this.u2methodIndex);
    }

    /** Do necessary name remapping. */
    @Override
    protected void remap(ClassFile cf, NameMapper nm) throws Exception
    {
        // No remap needed
    }
}
