/* ===========================================================================
 * $RCSfile: MethodInfo.java,v $
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
 * Representation of a method from a class-file.
 * 
 * @author Mark Welsh
 */
public class MethodInfo extends ClassItemInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------


    // Class Methods ---------------------------------------------------------
    /**
     * Create a new MethodInfo from the file format data in the DataInput stream.
     * 
     * @param din
     * @param cf
     * @throws IOException
     * @throws ClassFileException
     */
    public static MethodInfo create(DataInput din, ClassFile cf) throws IOException, ClassFileException
    {
        if (din == null)
        {
            throw new IOException("No input stream was provided.");
        }
        MethodInfo mi = new MethodInfo(cf);
        mi.read(din);
        return mi;
    }


    // Instance Methods ------------------------------------------------------
    /**
     * Constructor
     * 
     * @param cf
     */
    protected MethodInfo(ClassFile cf)
    {
        super(cf);
    }
}
