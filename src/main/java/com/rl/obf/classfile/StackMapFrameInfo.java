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

package com.rl.obf.classfile;

import java.io.*;
import java.util.*;

/**
 * Representation of an Stack Map Frame entry.
 * 
 * @author Mark Welsh
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
    private List<VerificationTypeInfo> stack = Collections.emptyList();
    private List<VerificationTypeInfo> locals = Collections.emptyList();


    // Class Methods ---------------------------------------------------------
    /**
     * @param din
     * @throws IOException
     * @throws ClassFileException
     */
    public static StackMapFrameInfo create(DataInput din) throws IOException, ClassFileException
    {
        StackMapFrameInfo smfi = new StackMapFrameInfo();
        smfi.read(din);
        return smfi;
    }


    // Instance Methods ------------------------------------------------------
    /**
     * Private constructor
     */
    private StackMapFrameInfo()
    {
    }

    /**
     * @param din
     * @throws IOException
     * @throws ClassFileException
     */
    private void read(DataInput din) throws IOException, ClassFileException
    {
        this.u1frameType = din.readUnsignedByte();
        if ((StackMapFrameInfo.SAME_MIN <= this.u1frameType) && (this.u1frameType <= StackMapFrameInfo.SAME_MAX))
        {
            // nothing else to read
        }
        else if ((StackMapFrameInfo.SAME_LOCALS_1_STACK_ITEM_MIN <= this.u1frameType)
            && (this.u1frameType <= StackMapFrameInfo.SAME_LOCALS_1_STACK_ITEM_MAX))
        {
            this.readStackItems(din, 1);
        }
        else if (this.u1frameType == StackMapFrameInfo.SAME_LOCALS_1_STACK_ITEM_EXTENDED)
        {
            this.u2offsetDelta = din.readUnsignedShort();
            this.readStackItems(din, 1);
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
            int u2numberOfLocals = (1 + this.u1frameType) - StackMapFrameInfo.APPEND_MIN;
            this.readLocals(din, u2numberOfLocals);
        }
        else if (this.u1frameType == StackMapFrameInfo.FULL_FRAME)
        {
            this.u2offsetDelta = din.readUnsignedShort();
            int u2numberOfLocals = din.readUnsignedShort();
            this.readLocals(din, u2numberOfLocals);
            int u2numberOfStackItems = din.readUnsignedShort();
            this.readStackItems(din, u2numberOfStackItems);
        }
    }

    /**
     * Check for Utf8 references to constant pool and mark them.
     * 
     * @param pool
     * @throws ClassFileException
     */
    protected void markUtf8Refs(ConstantPool pool) throws ClassFileException
    {
        for (VerificationTypeInfo vt : this.stack)
        {
            vt.markUtf8Refs(pool);
        }
        for (VerificationTypeInfo vt : this.locals)
        {
            vt.markUtf8Refs(pool);
        }
    }

    /**
     * Export the representation to a DataOutput stream.
     * 
     * @param dout
     * @throws IOException
     * @throws ClassFileException
     */
    public void write(DataOutput dout) throws IOException, ClassFileException
    {
        dout.writeByte(this.u1frameType);
        if ((StackMapFrameInfo.SAME_MIN <= this.u1frameType) && (this.u1frameType <= StackMapFrameInfo.SAME_MAX))
        {
            // nothing else to write
        }
        else if ((StackMapFrameInfo.SAME_LOCALS_1_STACK_ITEM_MIN <= this.u1frameType)
            && (this.u1frameType <= StackMapFrameInfo.SAME_LOCALS_1_STACK_ITEM_MAX))
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
            dout.writeShort(this.locals.size());
            this.writeLocals(dout);
            dout.writeShort(this.stack.size());
            this.writeStackItems(dout);
        }
    }

    /**
     * Read 'locals' VerificationTypeInfo
     * 
     * @param din
     * @throws IOException
     * @throws ClassFileException
     */
    private void readLocals(DataInput din, int items) throws IOException, ClassFileException
    {
        this.locals = new ArrayList<VerificationTypeInfo>(items);
        for (int i = 0; i < items; i++)
        {
            this.locals.add(VerificationTypeInfo.create(din));
        }
    }

    /**
     * Write 'locals' VerificationTypeInfo
     * 
     * @param dout
     * @throws IOException
     * @throws ClassFileException
     */
    private void writeLocals(DataOutput dout) throws IOException, ClassFileException
    {
        for (VerificationTypeInfo vt : this.locals)
        {
            vt.write(dout);
        }
    }

    /**
     * Read 'stack items' VerificationTypeInfo
     * 
     * @param din
     * @throws IOException
     * @throws ClassFileException
     */
    private void readStackItems(DataInput din, int items) throws IOException, ClassFileException
    {
        this.stack = new ArrayList<VerificationTypeInfo>(items);
        for (int i = 0; i < items; i++)
        {
            this.stack.add(VerificationTypeInfo.create(din));
        }
    }

    /**
     * Write 'stack items' VerificationTypeInfo
     * 
     * @param dout
     * @throws IOException
     * @throws ClassFileException
     */
    private void writeStackItems(DataOutput dout) throws IOException, ClassFileException
    {
        for (VerificationTypeInfo vt : this.stack)
        {
            vt.write(dout);
        }
    }
}
