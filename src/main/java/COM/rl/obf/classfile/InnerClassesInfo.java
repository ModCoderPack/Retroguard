/* ===========================================================================
 * $RCSfile: InnerClassesInfo.java,v $
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
 * Representation of an Inner Classes table entry.
 *
 * @author      Mark Welsh
 */
public class InnerClassesInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u2innerClassInfoIndex;
    private int u2outerClassInfoIndex;
    private int u2innerNameIndex;
    private int u2innerClassAccessFlags;


    // Class Methods ---------------------------------------------------------
    public static InnerClassesInfo create(DataInput din) throws Exception
    {
        InnerClassesInfo ici = new InnerClassesInfo();
        ici.read(din);
        return ici;
    }


    // Instance Methods ------------------------------------------------------
    private InnerClassesInfo() {}

    /** Return the inner class index. */
    protected int getInnerClassIndex() {return u2innerClassInfoIndex;}

    /** Return the name index. */
    protected int getInnerNameIndex() {return u2innerNameIndex;}

    /** Set the name index. */
    protected void setInnerNameIndex(int index) {u2innerNameIndex = index;}

    /** Check for Utf8 references to constant pool and mark them. */
    protected void markUtf8Refs(ConstantPool pool) throws Exception
    {
        // BUGFIX: a Swing1.1beta3 class has name index of zero - this is valid
        if (u2innerNameIndex != 0) 
        {
            pool.incRefCount(u2innerNameIndex);
        }
    }

    private void read(DataInput din) throws Exception
    {
        u2innerClassInfoIndex = din.readUnsignedShort();
        u2outerClassInfoIndex = din.readUnsignedShort();
        u2innerNameIndex = din.readUnsignedShort();
        u2innerClassAccessFlags = din.readUnsignedShort();
    }

    /** Export the representation to a DataOutput stream. */
    public void write(DataOutput dout) throws Exception
    {
        dout.writeShort(u2innerClassInfoIndex);
        dout.writeShort(u2outerClassInfoIndex);
        dout.writeShort(u2innerNameIndex);
        dout.writeShort(u2innerClassAccessFlags);
    }

    /** Do necessary name remapping. */
    protected void remap(ClassFile cf, NameMapper nm) throws Exception 
    { 
        if (u2innerNameIndex != 0)
        {
            // Get the full inner class name
            ClassCpInfo innerClassInfo = 
                (ClassCpInfo)cf.getCpEntry(u2innerClassInfoIndex);
            String innerClassName = ((Utf8CpInfo)cf.getCpEntry(innerClassInfo.getNameIndex())).getString();
            // It is the remapped simple name that must be stored, so truncate
            String remapClass = nm.mapClass(innerClassName);
            remapClass = remapClass.substring(remapClass.lastIndexOf('$') + 1);
            u2innerNameIndex = cf.remapUtf8To(remapClass, u2innerNameIndex);
        }
    }
}
