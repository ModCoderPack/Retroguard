/* ===========================================================================
 * $RCSfile: ExceptionInfo.java,v $
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
 * Representation of an Exception table entry.
 *
 * @author      Mark Welsh
 */
public class ExceptionInfo
{
    // Constants -------------------------------------------------------------
    public static final int CONSTANT_FIELD_SIZE = 8;


    // Fields ----------------------------------------------------------------
    private int u2startpc;
    private int u2endpc;
    private int u2handlerpc;
    private int u2catchType;


    // Class Methods ---------------------------------------------------------
    public static ExceptionInfo create(DataInput din) throws Exception
    {
        ExceptionInfo ei = new ExceptionInfo();
        ei.read(din);
        return ei;
    }


    // Instance Methods ------------------------------------------------------
    private ExceptionInfo() {}
    private void read(DataInput din) throws Exception
    {
        u2startpc = din.readUnsignedShort();
        u2endpc = din.readUnsignedShort();
        u2handlerpc = din.readUnsignedShort();
        u2catchType = din.readUnsignedShort();
    }

    /** Export the representation to a DataOutput stream. */
    public void write(DataOutput dout) throws Exception
    {
        dout.writeShort(u2startpc);
        dout.writeShort(u2endpc);
        dout.writeShort(u2handlerpc);
        dout.writeShort(u2catchType);
    }
}
