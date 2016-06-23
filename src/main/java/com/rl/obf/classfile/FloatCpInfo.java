/* ===========================================================================
 * $RCSfile: FloatCpInfo.java,v $
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

package com.rl.obf.classfile;

import java.io.*;
import java.util.*;

/**
 * Representation of a 'float' entry in the ConstantPool.
 * 
 * @author Mark Welsh
 */
public class FloatCpInfo extends CpInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u4bytes;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /**
     * Constructor
     */
    protected FloatCpInfo()
    {
        super(ClassConstants.CONSTANT_Float);
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
        this.u4bytes = din.readInt();
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
        dout.writeInt(this.u4bytes);
    }
}
