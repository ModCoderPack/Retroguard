/* ===========================================================================
 * $RCSfile: StackMapFrameInfo.java,v $
 * ===========================================================================
 *
 * RetroGuard -- an obfuscation package for Java classfiles.
 *
 * Copyright (c) 1998-2007 Mark Welsh (markw@retrologic.com)
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
 * Representation of an Stack Map Frame entry.
 *
 * @author      Mark Welsh
 */
public class StackMapFrameInfo
{
    // Constants -------------------------------------------------------------
    private static final int SAME_MIN = 0;
    private static final int SAME_MAX = 63;
    private static final int SAME_LOCALS_1_STACK_ITEM_MIN = 64;
    private static final int SAME_LOCALS_1_STACK_ITEM_MAX = 127;
    private static final int SAME_LOCALS_1_STACK_ITEM_EXTENDED = 247;
    private static final int CHOP_MIN = 248;
    private static final int CHOP_MAX = 250;
    private static final int SAME_FRAME_EXTENDED = 251;
    private static final int APPEND_MIN = 252;
    private static final int APPEND_MAX = 254;
    private static final int FULL_FRAME = 255;


    // Fields ----------------------------------------------------------------
    private int u1frameType;
    private int u2offsetDelta;
    private int u2numberOfStackItems;
    private VerificationTypeInfo stack[];
    private int u2numberOfLocals;
    private VerificationTypeInfo locals[];


    // Class Methods ---------------------------------------------------------
    public static StackMapFrameInfo create(DataInput din) throws Exception
    {
        StackMapFrameInfo smfi = new StackMapFrameInfo();
        smfi.read(din);
        return smfi;
    }


    // Instance Methods ------------------------------------------------------
    private StackMapFrameInfo() {}
    private void read(DataInput din) throws Exception
    {
        u1frameType = din.readUnsignedByte();
        if (SAME_MIN <= u1frameType && 
            u1frameType <= SAME_MAX)
        {
            // nothing else to read
        }
        else if (SAME_LOCALS_1_STACK_ITEM_MIN <= u1frameType && 
                 u1frameType <= SAME_LOCALS_1_STACK_ITEM_MAX)
        {
            u2numberOfStackItems = 1;
            readStackItems(din);
        }
        else if (u1frameType == SAME_LOCALS_1_STACK_ITEM_EXTENDED)
        {
            u2offsetDelta = din.readUnsignedShort();
            u2numberOfStackItems = 1;
            readStackItems(din);
        }
        else if (CHOP_MIN <= u1frameType && 
                 u1frameType <= CHOP_MAX)
        {
            u2offsetDelta = din.readUnsignedShort();
        }
        else if (u1frameType == SAME_FRAME_EXTENDED)
        {
            u2offsetDelta = din.readUnsignedShort();
        }
        else if (APPEND_MIN <= u1frameType && 
                 u1frameType <= APPEND_MAX)
        {
            u2offsetDelta = din.readUnsignedShort();
            u2numberOfLocals = 1 + u1frameType - APPEND_MIN;
            readLocals(din);
        }
        else if (u1frameType == FULL_FRAME)
        {
            u2offsetDelta = din.readUnsignedShort();
            u2numberOfLocals = din.readUnsignedShort();
            readLocals(din);
            u2numberOfStackItems = din.readUnsignedShort();
            readStackItems(din);
        }
    }

    /** Check for Utf8 references to constant pool and mark them. */
    protected void markUtf8Refs(ConstantPool pool) throws Exception
    {
        for (int i = 0; i < u2numberOfStackItems; i++)
        {
            stack[i].markUtf8Refs(pool);
        }
        for (int i = 0; i < u2numberOfLocals; i++)
        {
            locals[i].markUtf8Refs(pool);
        }
    }

    /** Export the representation to a DataOutput stream. */
    public void write(DataOutput dout) throws Exception
    {
        dout.writeByte(u1frameType);
        if (SAME_MIN <= u1frameType && 
            u1frameType <= SAME_MAX)
        {
            // nothing else to write
        }
        else if (SAME_LOCALS_1_STACK_ITEM_MIN <= u1frameType && 
                 u1frameType <= SAME_LOCALS_1_STACK_ITEM_MAX)
        {
            writeStackItems(dout);
        }
        else if (u1frameType == SAME_LOCALS_1_STACK_ITEM_EXTENDED)
        {
            dout.writeShort(u2offsetDelta);
            writeStackItems(dout);
        }
        else if (CHOP_MIN <= u1frameType && 
                 u1frameType <= CHOP_MAX)
        {
            dout.writeShort(u2offsetDelta);
        }
        else if (u1frameType == SAME_FRAME_EXTENDED)
        {
            dout.writeShort(u2offsetDelta);
        }
        else if (APPEND_MIN <= u1frameType && 
                 u1frameType <= APPEND_MAX)
        {
            dout.writeShort(u2offsetDelta);
            writeLocals(dout);
        }
        else if (u1frameType == FULL_FRAME)
        {
            dout.writeShort(u2offsetDelta);
            dout.writeShort(u2numberOfLocals);
            writeLocals(dout);
            dout.writeShort(u2numberOfStackItems);
            writeStackItems(dout);
        }
    }

    // Read 'locals' VerificationTypeInfo
    private void readLocals(DataInput din) throws Exception
    {
        locals = new VerificationTypeInfo[u2numberOfLocals];
        for (int i = 0; i < u2numberOfLocals; i++)
        {
            locals[i] = VerificationTypeInfo.create(din);
        }
    }

    // Write 'locals' VerificationTypeInfo
    private void writeLocals(DataOutput dout) throws Exception
    {
        for (int i = 0; i < u2numberOfLocals; i++)
        {
            locals[i].write(dout);
        }
    }

    // Read 'stack items' VerificationTypeInfo
    private void readStackItems(DataInput din) throws Exception
    {
        stack = new VerificationTypeInfo[u2numberOfStackItems];
        for (int i = 0; i < u2numberOfStackItems; i++)
        {
            stack[i] = VerificationTypeInfo.create(din);
        }
    }

    // Write 'stack items' VerificationTypeInfo
    private void writeStackItems(DataOutput dout) throws Exception
    {
        for (int i = 0; i < u2numberOfStackItems; i++)
        {
            stack[i].write(dout);
        }
    }
}

