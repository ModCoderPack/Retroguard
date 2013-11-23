/* ===========================================================================
 * $RCSfile: CpInfo.java,v $
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
 * Representation of an entry in the ConstantPool. Specific types of entry have their representations sub-classed from this.
 * 
 * @author Mark Welsh
 */
abstract public class CpInfo implements ClassConstants
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u1tag;

    /**
     * Used for reference counting in Constant Pool
     */
    protected int refCount = 0;


    // Class Methods ---------------------------------------------------------
    /**
     * Create a new CpInfo from the data passed.
     * 
     * @param din
     * @throws IOException
     * @throws ClassFileException
     */
    public static CpInfo create(DataInput din) throws IOException, ClassFileException
    {
        if (din == null)
        {
            throw new IOException("No input stream was provided.");
        }

        // Instantiate based on tag byte
        CpInfo ci = null;
        switch (din.readUnsignedByte())
        {
            case CONSTANT_Utf8:
                ci = new Utf8CpInfo();
                break;
            case CONSTANT_Integer:
                ci = new IntegerCpInfo();
                break;
            case CONSTANT_Float:
                ci = new FloatCpInfo();
                break;
            case CONSTANT_Long:
                ci = new LongCpInfo();
                break;
            case CONSTANT_Double:
                ci = new DoubleCpInfo();
                break;
            case CONSTANT_Class:
                ci = new ClassCpInfo();
                break;
            case CONSTANT_String:
                ci = new StringCpInfo();
                break;
            case CONSTANT_Fieldref:
                ci = new FieldrefCpInfo();
                break;
            case CONSTANT_Methodref:
                ci = new MethodrefCpInfo();
                break;
            case CONSTANT_InterfaceMethodref:
                ci = new InterfaceMethodrefCpInfo();
                break;
            case CONSTANT_NameAndType:
                ci = new NameAndTypeCpInfo();
                break;
            case CONSTANT_MethodHandle:
                ci = new MethodHandleCpInfo();
                break;
            case CONSTANT_MethodType:
                ci = new MethodTypeCpInfo();
                break;
            case CONSTANT_InvokeDynamic:
                ci = new InvokeDynamicCpInfo();
                break;
            default:
                throw new ClassFileException("Unknown tag type in constant pool.");
        }
        ci.readInfo(din);
        return ci;
    }


    // Instance Methods ------------------------------------------------------
    /**
     * Constructor
     * 
     * @param tag
     */
    protected CpInfo(int tag)
    {
        this.u1tag = tag;
    }

    /**
     * Read the 'info' data following the u1tag byte; over-ride this in sub-classes.
     * 
     * @param din
     * @throws IOException
     * @throws ClassFileException
     */
    abstract protected void readInfo(DataInput din) throws IOException, ClassFileException;

    /**
     * Check for Utf8 references to constant pool and mark them; over-ride this in sub-classes.
     * 
     * @param pool
     * @throws ClassFileException
     */
    protected void markUtf8Refs(ConstantPool pool) throws ClassFileException
    {
        // do nothing
    }

    /**
     * Check for NameAndType references to constant pool and mark them; over-ride this in sub-classes.
     * 
     * @param pool
     * @throws ClassFileException
     */
    protected void markNTRefs(ConstantPool pool) throws ClassFileException
    {
        // do nothing
    }

    /**
     * Export the representation to a {@code DataOutput} stream.
     * 
     * @param dout
     * @throws IOException
     * @throws ClassFileException
     */
    public void write(DataOutput dout) throws IOException, ClassFileException
    {
        if (dout == null)
        {
            throw new IOException("No output stream was provided.");
        }
        dout.writeByte(this.u1tag);
        this.writeInfo(dout);
    }

    /**
     * Write the 'info' data following the u1tag byte; over-ride this in sub-classes.
     * 
     * @param dout
     * @throws IOException
     * @throws ClassFileException
     */
    abstract protected void writeInfo(DataOutput dout) throws IOException, ClassFileException;

    /**
     * Return the reference count.
     */
    public int getRefCount()
    {
        return this.refCount;
    }

    /**
     * Increment the reference count.
     */
    public void incRefCount()
    {
        this.refCount++;
    }

    /**
     * Decrement the reference count.
     * 
     * @throws ClassFileException
     */
    public void decRefCount() throws ClassFileException
    {
        if (this.refCount == 0)
        {
            throw new ClassFileException("Illegal to decrement ref count that is already zero.");
        }
        this.refCount--;
    }

    /**
     * Reset the reference count to zero.
     */
    public void resetRefCount()
    {
        this.refCount = 0;
    }
}
