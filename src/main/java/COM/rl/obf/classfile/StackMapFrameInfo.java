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
    private StackMapFrameInfo()
    {
    }
    private void read(DataInput din) throws Exception
    {
        this.u1frameType = din.readUnsignedByte();
        if ((StackMapFrameInfo.SAME_MIN <= this.u1frameType) && (this.u1frameType <= StackMapFrameInfo.SAME_MAX))
        {
            // nothing else to read
        }
        else if ((StackMapFrameInfo.SAME_LOCALS_1_STACK_ITEM_MIN <= this.u1frameType) && (this.u1frameType <= StackMapFrameInfo.SAME_LOCALS_1_STACK_ITEM_MAX))
        {
            this.u2numberOfStackItems = 1;
            this.readStackItems(din);
        }
        else if (this.u1frameType == StackMapFrameInfo.SAME_LOCALS_1_STACK_ITEM_EXTENDED)
        {
            this.u2offsetDelta = din.readUnsignedShort();
            this.u2numberOfStackItems = 1;
            this.readStackItems(din);
        }
        else if ((StackMapFrameInfo.CHOP_MIN <= this.u1frameType) && (this.u1frameType <= StackMapFrameInfo.CHOP_MAX))
        {
            this.u2offsetDelta = din.readUnsignedShort();
        }
        else if (this.u1frameType == StackMapFrameInfo.SAME_FRAME_EXTENDED)
        {
            this.u2offsetDelta = din.readUnsignedShort();
        }
        else if ((StackMapFrameInfo.APPEND_MIN <= this.u1frameType) && (this.u1frameType <= StackMapFrameInfo.APPEND_MAX))
        {
            this.u2offsetDelta = din.readUnsignedShort();
            this.u2numberOfLocals = 1 + this.u1frameType - StackMapFrameInfo.APPEND_MIN;
            this.readLocals(din);
        }
        else if (this.u1frameType == StackMapFrameInfo.FULL_FRAME)
        {
            this.u2offsetDelta = din.readUnsignedShort();
            this.u2numberOfLocals = din.readUnsignedShort();
            this.readLocals(din);
            this.u2numberOfStackItems = din.readUnsignedShort();
            this.readStackItems(din);
        }
    }

    /** Check for Utf8 references to constant pool and mark them. */
    protected void markUtf8Refs(ConstantPool pool) throws Exception
    {
        for (int i = 0; i < this.u2numberOfStackItems; i++)
        {
            this.stack[i].markUtf8Refs(pool);
        }
        for (int i = 0; i < this.u2numberOfLocals; i++)
        {
            this.locals[i].markUtf8Refs(pool);
        }
    }

    /** Export the representation to a DataOutput stream. */
    public void write(DataOutput dout) throws Exception
    {
        dout.writeByte(this.u1frameType);
        if ((StackMapFrameInfo.SAME_MIN <= this.u1frameType) && (this.u1frameType <= StackMapFrameInfo.SAME_MAX))
        {
            // nothing else to write
        }
        else if ((StackMapFrameInfo.SAME_LOCALS_1_STACK_ITEM_MIN <= this.u1frameType) && (this.u1frameType <= StackMapFrameInfo.SAME_LOCALS_1_STACK_ITEM_MAX))
        {
            this.writeStackItems(dout);
        }
        else if (this.u1frameType == StackMapFrameInfo.SAME_LOCALS_1_STACK_ITEM_EXTENDED)
        {
            dout.writeShort(this.u2offsetDelta);
            this.writeStackItems(dout);
        }
        else if ((StackMapFrameInfo.CHOP_MIN <= this.u1frameType) && (this.u1frameType <= StackMapFrameInfo.CHOP_MAX))
        {
            dout.writeShort(this.u2offsetDelta);
        }
        else if (this.u1frameType == StackMapFrameInfo.SAME_FRAME_EXTENDED)
        {
            dout.writeShort(this.u2offsetDelta);
        }
        else if ((StackMapFrameInfo.APPEND_MIN <= this.u1frameType) && (this.u1frameType <= StackMapFrameInfo.APPEND_MAX))
        {
            dout.writeShort(this.u2offsetDelta);
            this.writeLocals(dout);
        }
        else if (this.u1frameType == StackMapFrameInfo.FULL_FRAME)
        {
            dout.writeShort(this.u2offsetDelta);
            dout.writeShort(this.u2numberOfLocals);
            this.writeLocals(dout);
            dout.writeShort(this.u2numberOfStackItems);
            this.writeStackItems(dout);
        }
    }

    /** Read 'locals' VerificationTypeInfo */
    private void readLocals(DataInput din) throws Exception
    {
        this.locals = new VerificationTypeInfo[this.u2numberOfLocals];
        for (int i = 0; i < this.u2numberOfLocals; i++)
        {
            this.locals[i] = VerificationTypeInfo.create(din);
        }
    }

    /** Write 'locals' VerificationTypeInfo */
    private void writeLocals(DataOutput dout) throws Exception
    {
        for (int i = 0; i < this.u2numberOfLocals; i++)
        {
            this.locals[i].write(dout);
        }
    }

    /** Read 'stack items' VerificationTypeInfo */
    private void readStackItems(DataInput din) throws Exception
    {
        this.stack = new VerificationTypeInfo[this.u2numberOfStackItems];
        for (int i = 0; i < this.u2numberOfStackItems; i++)
        {
            this.stack[i] = VerificationTypeInfo.create(din);
        }
    }

    /** Write 'stack items' VerificationTypeInfo */
    private void writeStackItems(DataOutput dout) throws Exception
    {
        for (int i = 0; i < this.u2numberOfStackItems; i++)
        {
            this.stack[i].write(dout);
        }
    }
}

