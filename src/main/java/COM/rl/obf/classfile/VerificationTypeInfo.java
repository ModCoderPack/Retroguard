/* ===========================================================================
 * $RCSfile: VerificationTypeInfo.java,v $
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
 * Representation of an Verification Type Info entry.
 * 
 * @author Mark Welsh
 */
public class VerificationTypeInfo
{
    // Constants -------------------------------------------------------------
    private static final int ITEM_Top = 0;
    private static final int ITEM_Integer = 1;
    private static final int ITEM_Float = 2;
    private static final int ITEM_Long = 3;
    private static final int ITEM_Double = 4;
    private static final int ITEM_Null = 5;
    private static final int ITEM_UninitializedThis = 6;
    private static final int ITEM_Object = 7;
    private static final int ITEM_Uninitialized = 8;


    // Fields ----------------------------------------------------------------
    private int u1tag;
    private int u2cpoolIndex;
    private int u2offset;


    // Class Methods ---------------------------------------------------------
    /**
     * @param din
     * @throws IOException
     * @throws ClassFileException
     */
    public static VerificationTypeInfo create(DataInput din) throws IOException, ClassFileException
    {
        VerificationTypeInfo vti = new VerificationTypeInfo();
        vti.read(din);
        return vti;
    }


    // Instance Methods ------------------------------------------------------
    /**
     * Private constructor
     */
    private VerificationTypeInfo()
    {
    }

    /**
     * @param din
     * @throws IOException
     * @throws ClassFileException
     */
    private void read(DataInput din) throws IOException, ClassFileException
    {
        this.u1tag = din.readUnsignedByte();
        switch (this.u1tag)
        {
            case ITEM_Top:
            case ITEM_Integer:
            case ITEM_Float:
            case ITEM_Long:
            case ITEM_Double:
            case ITEM_Null:
            case ITEM_UninitializedThis:
                break;

            case ITEM_Object:
                this.u2cpoolIndex = din.readUnsignedShort();
                break;

            case ITEM_Uninitialized:
                this.u2offset = din.readUnsignedShort();
                break;

            default:
                throw new ClassFileException("Illegal Verification Type Info tag: " + this.u1tag);
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
        if (this.u1tag == VerificationTypeInfo.ITEM_Object)
        {
            pool.incRefCount(this.u2cpoolIndex);
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
        dout.writeByte(this.u1tag);
        switch (this.u1tag)
        {
            case ITEM_Top:
            case ITEM_Integer:
            case ITEM_Float:
            case ITEM_Long:
            case ITEM_Double:
            case ITEM_Null:
            case ITEM_UninitializedThis:
                break;

            case ITEM_Object:
                dout.writeShort(this.u2cpoolIndex);
                break;

            case ITEM_Uninitialized:
                dout.writeShort(this.u2offset);
                break;

            default:
                throw new ClassFileException("Illegal Verification Type Info tag: " + this.u1tag);
        }
    }
}
