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
 * @author Mark Welsh
 */
abstract public class RefCpInfo extends CpInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u2classIndex;
    private int u2nameAndTypeIndex;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /**
     * Constructor
     * 
     * @param tag
     */
    protected RefCpInfo(int tag)
    {
        super(tag);
    }

    /**
     * Return the class index.
     */
    protected int getClassIndex()
    {
        return this.u2classIndex;
    }

    /**
     * Return the name-and-type index.
     */
    protected int getNameAndTypeIndex()
    {
        return this.u2nameAndTypeIndex;
    }

    /**
     * Set the name-and-type index.
     * 
     * @param index
     */
    protected void setNameAndTypeIndex(int index)
    {
        this.u2nameAndTypeIndex = index;
    }

    /**
     * Return the method's class string name.
     * 
     * @param cf
     * @throws ClassFileException
     */
    public String getClassName(ClassFile cf) throws ClassFileException
    {
        ClassCpInfo entry = (ClassCpInfo)cf.getCpEntry(this.u2classIndex);
        return entry.getName(cf);
    }

    /**
     * Return the method's string name.
     * 
     * @param cf
     * @throws ClassFileException
     */
    public String getName(ClassFile cf) throws ClassFileException
    {
        NameAndTypeCpInfo ntCpInfo = (NameAndTypeCpInfo)cf.getCpEntry(this.u2nameAndTypeIndex);
        return cf.getUtf8(ntCpInfo.getNameIndex());
    }

    /**
     * Return the method's string descriptor.
     * 
     * @param cf
     * @throws ClassFileException
     */
    public String getDescriptor(ClassFile cf) throws ClassFileException
    {
        NameAndTypeCpInfo ntCpInfo = (NameAndTypeCpInfo)cf.getCpEntry(this.u2nameAndTypeIndex);
        return cf.getUtf8(ntCpInfo.getDescriptorIndex());
    }

    /**
     * Check for N+T references to constant pool and mark them.
     * 
     * @throws ClassFileException
     */
    @Override
    protected void markNTRefs(ConstantPool pool) throws ClassFileException
    {
        pool.incRefCount(this.u2nameAndTypeIndex);
    }

    /**
     * Read the 'info' data following the u1tag byte.
     * 
     * @throws IOException
     * @throws ClassFileException
     */
    @Override
    protected void readInfo(DataInput din) throws IOException, ClassFileException
    {
        this.u2classIndex = din.readUnsignedShort();
        this.u2nameAndTypeIndex = din.readUnsignedShort();
    }

    /**
     * Write the 'info' data following the u1tag byte.
     * 
     * @throws IOException
     * @throws ClassFileException
     */
    @Override
    protected void writeInfo(DataOutput dout) throws IOException, ClassFileException
    {
        dout.writeShort(this.u2classIndex);
        dout.writeShort(this.u2nameAndTypeIndex);
    }
}
