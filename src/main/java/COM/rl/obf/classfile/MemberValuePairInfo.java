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
 * @author      Mark Welsh
 */
public class MemberValuePairInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u2memberNameIndex;
    private MemberValueInfo value;


    // Class Methods ---------------------------------------------------------
    public static MemberValuePairInfo create(DataInput din) throws Exception
    {
        MemberValuePairInfo mvpi = new MemberValuePairInfo();
        mvpi.read(din);
        return mvpi;
    }


    // Instance Methods ------------------------------------------------------
    private MemberValuePairInfo() {}

    /** Return member name index into Constant Pool. */
    protected int getMemberNameIndex() {return u2memberNameIndex;}

    /** Check for Utf8 references to constant pool and mark them. */
    protected void markUtf8Refs(ConstantPool pool) throws Exception
    {
        pool.incRefCount(u2memberNameIndex);
        value.markUtf8Refs(pool);
    }

    private void read(DataInput din) throws Exception
    {
        u2memberNameIndex = din.readUnsignedShort();
        value = MemberValueInfo.create(din);
    }

    /** Export the representation to a DataOutput stream. */
    public void write(DataOutput dout) throws Exception
    {
        dout.writeShort(u2memberNameIndex);
        value.write(dout);
    }

    /** Do necessary name remapping. */
    protected void remap(ClassFile cf, NameMapper nm) throws Exception 
    { 
        value.remap(cf, nm);
    }

    /** Provide debugging dump of this object. */
    public void dump(PrintStream ps, ClassFile cf) throws Exception
    {
        ps.println("u2memberNameIndex : " + u2memberNameIndex + " " + cf.getUtf8(u2memberNameIndex));
        value.dump(ps, cf);
    }
}
