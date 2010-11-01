/* ===========================================================================
 * $RCSfile: RefCpInfo.java,v $
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
 * Representation of a 'ref'-type entry in the ConstantPool.
 *
 * @author      Mark Welsh
 */
abstract public class RefCpInfo extends CpInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u2classIndex;
    private int u2nameAndTypeIndex;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    protected RefCpInfo(int tag)
    {
        super(tag);
    }

    /** Return the class index. */
    protected int getClassIndex() {return u2classIndex;}

    /** Return the name-and-type index. */
    protected int getNameAndTypeIndex() {return u2nameAndTypeIndex;}

    /** Set the name-and-type index. */
    protected void setNameAndTypeIndex(int index) {u2nameAndTypeIndex = index;}

    /** Return the method's class string name. */
    public String getClassName(ClassFile cf) throws Exception
    {
	return ((ClassCpInfo)cf.getCpEntry(u2classIndex)).getName(cf);
    }

    /** Return the method's string name. */
    public String getName(ClassFile cf) throws Exception
    {
	NameAndTypeCpInfo ntCpInfo = 
	    (NameAndTypeCpInfo)cf.getCpEntry(u2nameAndTypeIndex);
	return ((Utf8CpInfo)cf.getCpEntry(ntCpInfo.getNameIndex())).getString();
    }

    /** Return the method's string descriptor. */
    public String getDescriptor(ClassFile cf) throws Exception
    {
	NameAndTypeCpInfo ntCpInfo = 
	    (NameAndTypeCpInfo)cf.getCpEntry(u2nameAndTypeIndex);
	return ((Utf8CpInfo)cf.getCpEntry(ntCpInfo.getDescriptorIndex())).getString();
    }

    /** Check for N+T references to constant pool and mark them. */
    protected void markNTRefs(ConstantPool pool) throws Exception
    {
        pool.incRefCount(u2nameAndTypeIndex);
    }

    /** Read the 'info' data following the u1tag byte. */
    protected void readInfo(DataInput din) throws Exception
    {
        u2classIndex = din.readUnsignedShort();
        u2nameAndTypeIndex = din.readUnsignedShort();
    }

    /** Write the 'info' data following the u1tag byte. */
    protected void writeInfo(DataOutput dout) throws Exception
    {
        dout.writeShort(u2classIndex);
        dout.writeShort(u2nameAndTypeIndex);
    }

    /** Dump the content of the class file to the specified file (used for debugging). */
    public void dump(PrintWriter pw, ClassFile cf, int index) throws Exception
    {
        pw.println("  Ref " + Integer.toString(index) + ": " + ((Utf8CpInfo)cf.getCpEntry(((ClassCpInfo)cf.getCpEntry(u2classIndex)).getNameIndex())).getString() +
                   " " + ((Utf8CpInfo)cf.getCpEntry(((NameAndTypeCpInfo)cf.getCpEntry(u2nameAndTypeIndex)).getNameIndex())).getString() +
                   " " + ((Utf8CpInfo)cf.getCpEntry(((NameAndTypeCpInfo)cf.getCpEntry(u2nameAndTypeIndex)).getDescriptorIndex())).getString());
    }
}
