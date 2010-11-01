/* ===========================================================================
 * $RCSfile: ConstantValueAttrInfo.java,v $
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
public class ConstantValueAttrInfo extends AttrInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u2constantValueIndex;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    protected ConstantValueAttrInfo(ClassFile cf, int attrNameIndex, int attrLength)
    {
        super(cf, attrNameIndex, attrLength);
    }

    /** Return the String name of the attribute; over-ride this in sub-classes. */
    protected String getAttrName() throws Exception
    {
        return ATTR_ConstantValue;
    }

    /** Read the data following the header. */
    protected void readInfo(DataInput din) throws Exception
    {
        u2constantValueIndex = din.readUnsignedShort();
    }

    /** Export data following the header to a DataOutput stream. */
    public void writeInfo(DataOutput dout) throws Exception
    {
        dout.writeShort(u2constantValueIndex);
    }
}

