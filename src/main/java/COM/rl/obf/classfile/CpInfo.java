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
 * Representation of an entry in the ConstantPool. Specific types of entry
 * have their representations sub-classed from this.
 *
 * @author      Mark Welsh
 */
abstract public class CpInfo implements ClassConstants
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u1tag;
    private byte info[];

    protected int refCount = 0;  // Used for reference counting in Constant Pool


    // Class Methods ---------------------------------------------------------
    /**
     * Create a new CpInfo from the data passed.
     *
     * @throws IOException if class file is corrupt or incomplete
     */
    public static CpInfo create(DataInput din) throws Exception
    {
        if (din == null) throw new IOException("No input stream was provided.");

        // Instantiate based on tag byte
        CpInfo ci = null;
        switch (din.readUnsignedByte())
        {
        case CONSTANT_Utf8:                 ci = new Utf8CpInfo();              break;
        case CONSTANT_Integer:              ci = new IntegerCpInfo();           break;
        case CONSTANT_Float:                ci = new FloatCpInfo();             break;
        case CONSTANT_Long:                 ci = new LongCpInfo();              break;
        case CONSTANT_Double:               ci = new DoubleCpInfo();            break;
        case CONSTANT_Class:                ci = new ClassCpInfo();             break;
        case CONSTANT_String:               ci = new StringCpInfo();            break;
        case CONSTANT_Fieldref:             ci = new FieldrefCpInfo();          break;
        case CONSTANT_Methodref:            ci = new MethodrefCpInfo();         break;
        case CONSTANT_InterfaceMethodref:   ci = new InterfaceMethodrefCpInfo();break;
        case CONSTANT_NameAndType:          ci = new NameAndTypeCpInfo();       break;
        default:    throw new IOException("Unknown tag type in constant pool.");
        }
        ci.readInfo(din);
        return ci;
    }


    // Instance Methods ------------------------------------------------------
    protected CpInfo(int tag)
    {
        u1tag = tag;
    }

    /** Read the 'info' data following the u1tag byte; over-ride this in sub-classes. */
    abstract protected void readInfo(DataInput din) throws Exception;

    /** Check for Utf8 references to constant pool and mark them; over-ride this in sub-classes. */
    protected void markUtf8Refs(ConstantPool pool) throws Exception {}

    /** Check for NameAndType references to constant pool and mark them; over-ride this in sub-classes. */
    protected void markNTRefs(ConstantPool pool) throws Exception {}

    /** Export the representation to a DataOutput stream. */
    public void write(DataOutput dout) throws Exception
    {
        if (dout == null) throw new IOException("No output stream was provided.");
        dout.writeByte(u1tag);
        writeInfo(dout);
    }

    /** Write the 'info' data following the u1tag byte; over-ride this in sub-classes. */
    abstract protected void writeInfo(DataOutput dout) throws Exception;

    /** Return the reference count. */
    public int getRefCount() {return refCount;}

    /** Increment the reference count. */
    public void incRefCount() {refCount++;}

    /** Decrement the reference count. */
    public void decRefCount() throws Exception
    {
        if (refCount == 0) throw new Exception("Illegal to decrement ref count that is already zero.");
        refCount--;
    }

    /** Reset the reference count to zero. */
    public void resetRefCount() {refCount = 0;}

    /** Dump the content of the class file to the specified file (used for debugging). */
    public void dump(PrintWriter pw, ClassFile cf, int index) throws Exception {}
}
