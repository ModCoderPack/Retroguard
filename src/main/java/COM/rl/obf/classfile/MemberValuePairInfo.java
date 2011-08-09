/* ===========================================================================
 * $RCSfile: MemberValuePairInfo.java,v $
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
 * Representation of an annotation's member-value-pair entry.
 * 
 * @author Mark Welsh
 */
public class MemberValuePairInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u2memberNameIndex;
    private MemberValueInfo value;


    // Class Methods ---------------------------------------------------------
    public static MemberValuePairInfo create(DataInput din) throws IOException, ClassFileException
    {
        MemberValuePairInfo mvpi = new MemberValuePairInfo();
        mvpi.read(din);
        return mvpi;
    }


    // Instance Methods ------------------------------------------------------
    private MemberValuePairInfo()
    {
    }

    /** Return member name index into Constant Pool. */
    protected int getMemberNameIndex()
    {
        return this.u2memberNameIndex;
    }

    /**
     * Check for Utf8 references to constant pool and mark them.
     * 
     * @throws ClassFileException
     */
    protected void markUtf8Refs(ConstantPool pool) throws ClassFileException
    {
        pool.incRefCount(this.u2memberNameIndex);
        this.value.markUtf8Refs(pool);
    }

    private void read(DataInput din) throws IOException, ClassFileException
    {
        this.u2memberNameIndex = din.readUnsignedShort();
        this.value = MemberValueInfo.create(din);
    }

    /**
     * Export the representation to a DataOutput stream.
     * 
     * @throws IOException
     * @throws ClassFileException
     */
    public void write(DataOutput dout) throws IOException, ClassFileException
    {
        dout.writeShort(this.u2memberNameIndex);
        this.value.write(dout);
    }

    /**
     * Do necessary name remapping.
     * 
     * @throws ClassFileException
     */
    protected void remap(ClassFile cf, NameMapper nm) throws ClassFileException
    {
        this.value.remap(cf, nm);
    }

    /**
     * Provide debugging dump of this object.
     * 
     * @throws ClassFileException
     */
    public void dump(PrintStream ps, ClassFile cf) throws ClassFileException
    {
        ps.println("u2memberNameIndex : " + this.u2memberNameIndex + " " + cf.getUtf8(this.u2memberNameIndex));
        this.value.dump(ps, cf);
    }
}
