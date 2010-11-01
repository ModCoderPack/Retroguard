/* ===========================================================================
 * $RCSfile: AnnotationDefaultAttrInfo.java,v $
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
public class AnnotationDefaultAttrInfo extends AttrInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private MemberValueInfo defaultValue;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    protected AnnotationDefaultAttrInfo(ClassFile cf, int attrNameIndex, int attrLength)
    {
        super(cf, attrNameIndex, attrLength);
    }

    /** Return the String name of the attribute. */
    protected String getAttrName() throws Exception
    {
        return ATTR_AnnotationDefault;
    }

    /** Return the default value. */
    protected MemberValueInfo getDefaultValue() throws Exception
    {
        return defaultValue;
    }

    /** Check for Utf8 references in the 'info' data to the constant pool and mark them. */
    protected void markUtf8RefsInInfo(ConstantPool pool) throws Exception
    {
        defaultValue.markUtf8Refs(pool);
    }

    /** Read the data following the header. */
    protected void readInfo(DataInput din) throws Exception
    {
        defaultValue = MemberValueInfo.create(din);
    }

    /** Export data following the header to a DataOutput stream. */
    public void writeInfo(DataOutput dout) throws Exception
    {
        defaultValue.write(dout);
    }

    /** Do necessary name remapping. */
    protected void remap(ClassFile cf, NameMapper nm) throws Exception 
    { 
        defaultValue.remap(cf, nm);
    }
}

