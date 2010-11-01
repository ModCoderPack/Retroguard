/* ===========================================================================
 * $RCSfile: ClassCpInfo.java,v $
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
 * Representation of a 'class' entry in the ConstantPool.
 *
 * @author      Mark Welsh
 */
public class ClassCpInfo extends CpInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u2nameIndex;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    protected ClassCpInfo()
    {
        super(CONSTANT_Class);
    }

    /** Return the name index. */
    protected int getNameIndex() {return u2nameIndex;}

    /** Set the name index. */
    protected void setNameIndex(int index) {u2nameIndex = index;}

    /** Return the string name. */
    public String getName(ClassFile cf) throws Exception
    {
	return ((Utf8CpInfo)cf.getCpEntry(u2nameIndex)).getString();
    }

    /** Check for Utf8 references to constant pool and mark them. */
    protected void markUtf8Refs(ConstantPool pool) throws Exception
    {
        pool.incRefCount(u2nameIndex);
    }

    /** Read the 'info' data following the u1tag byte. */
    protected void readInfo(DataInput din) throws Exception
    {
        u2nameIndex = din.readUnsignedShort();
    }

    /** Write the 'info' data following the u1tag byte. */
    protected void writeInfo(DataOutput dout) throws Exception
    {
        dout.writeShort(u2nameIndex);
    }

    /** Dump the content of the class file to the specified file (used for debugging). */
    public void dump(PrintWriter pw, ClassFile cf) throws Exception
    {
        pw.println("  Class: " + ((Utf8CpInfo)cf.getCpEntry(u2nameIndex)).getString());
    }
}
