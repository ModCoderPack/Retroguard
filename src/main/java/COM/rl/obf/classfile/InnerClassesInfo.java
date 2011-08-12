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
 * @author Mark Welsh
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
    /**
     * @param din
     * @throws IOException
     */
    public static InnerClassesInfo create(DataInput din) throws IOException
    {
        InnerClassesInfo ici = new InnerClassesInfo();
        ici.read(din);
        return ici;
    }


    // Instance Methods ------------------------------------------------------
    /**
     * Private constructor
     */
    private InnerClassesInfo()
    {
    }

    /**
     * Return the inner class index.
     */
    protected int getInnerClassIndex()
    {
        return this.u2innerClassInfoIndex;
    }

    /**
     * Return the name index.
     */
    protected int getInnerNameIndex()
    {
        return this.u2innerNameIndex;
    }

    /**
     * Set the name index.
     * 
     * @param index
     */
    protected void setInnerNameIndex(int index)
    {
        this.u2innerNameIndex = index;
    }

    /**
     * Check for Utf8 references to constant pool and mark them.
     * 
     * @param pool
     * @throws ClassFileException
     */
    protected void markUtf8Refs(ConstantPool pool) throws ClassFileException
    {
        // BUGFIX: a Swing1.1beta3 class has name index of zero - this is valid
        if (this.u2innerNameIndex != 0)
        {
            pool.incRefCount(this.u2innerNameIndex);
        }
    }

    /**
     * @param din
     * @throws IOException
     */
    private void read(DataInput din) throws IOException
    {
        this.u2innerClassInfoIndex = din.readUnsignedShort();
        this.u2outerClassInfoIndex = din.readUnsignedShort();
        this.u2innerNameIndex = din.readUnsignedShort();
        this.u2innerClassAccessFlags = din.readUnsignedShort();
    }

    /**
     * Export the representation to a DataOutput stream.
     * 
     * @param dout
     * @throws IOException
     */
    public void write(DataOutput dout) throws IOException
    {
        dout.writeShort(this.u2innerClassInfoIndex);
        dout.writeShort(this.u2outerClassInfoIndex);
        dout.writeShort(this.u2innerNameIndex);
        dout.writeShort(this.u2innerClassAccessFlags);
    }

    /**
     * Do necessary name remapping.
     * 
     * @param cf
     * @param nm
     * @throws ClassFileException
     */
    protected void remap(ClassFile cf, NameMapper nm) throws ClassFileException
    {
        if (this.u2innerNameIndex != 0)
        {
            // Get the full inner class name
            ClassCpInfo innerClassInfo = (ClassCpInfo)cf.getCpEntry(this.u2innerClassInfoIndex);
            String innerClassName = cf.getUtf8(innerClassInfo.getNameIndex());
            // It is the remapped simple name that must be stored, so truncate
            String remapClass = nm.mapClass(innerClassName);
            remapClass = remapClass.substring(remapClass.lastIndexOf('$') + 1);
            this.u2innerNameIndex = cf.remapUtf8To(remapClass, this.u2innerNameIndex);
        }
    }
}
