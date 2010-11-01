/* ===========================================================================
 * $RCSfile: StringCpInfo.java,v $
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
 * Representation of a 'string' entry in the ConstantPool.
 *
 * @author      Mark Welsh
 */
public class StringCpInfo extends CpInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u2stringIndex;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    protected StringCpInfo()
    {
        super(CONSTANT_String);
    }

    /** Return the string index. */
    protected int getStringIndex() {return u2stringIndex;}

    /** Set the string index. */
    protected void setStringIndex(int index) {u2stringIndex = index;}

    /** Check for Utf8 references to constant pool and mark them. */
    protected void markUtf8Refs(ConstantPool pool) throws Exception
    {
        pool.incRefCount(u2stringIndex);
    }

    /** Read the 'info' data following the u1tag byte. */
    protected void readInfo(DataInput din) throws Exception
    {
        u2stringIndex = din.readUnsignedShort();
    }

    /** Write the 'info' data following the u1tag byte. */
    protected void writeInfo(DataOutput dout) throws Exception
    {
        dout.writeShort(u2stringIndex);
    }
}
