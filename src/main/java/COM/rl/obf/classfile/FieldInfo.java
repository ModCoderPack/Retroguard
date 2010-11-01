/* ===========================================================================
 * $RCSfile: FieldInfo.java,v $
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
 * Representation of a field from a class-file.
 *
 * @author      Mark Welsh
 */
public class FieldInfo extends ClassItemInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------


    // Class Methods ---------------------------------------------------------
    /**
     * Create a new FieldInfo from the file format data in the DataInput stream.
     *
     * @throws IOException if class file is corrupt or incomplete
     */
    public static FieldInfo create(DataInput din, ClassFile cf) throws Exception
    {
        if (din == null) throw new IOException("No input stream was provided.");
        FieldInfo fi = new FieldInfo(cf);
        fi.read(din);
        return fi;
    }


    // Instance Methods ------------------------------------------------------
    protected FieldInfo(ClassFile cf) {super(cf);}
}
