/* ===========================================================================
 * $RCSfile: IntegerCpInfo.java,v $
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
 * Representation of a 'integer' entry in the ConstantPool.
 *
 * @author      Mark Welsh
 */
public class IntegerCpInfo extends CpInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u4bytes;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    protected IntegerCpInfo()
    {
        super(CONSTANT_Integer);
    }

    /** Read the 'info' data following the u1tag byte. */
    protected void readInfo(DataInput din) throws Exception
    {
        u4bytes = din.readInt();
    }

    /** Write the 'info' data following the u1tag byte. */
    protected void writeInfo(DataOutput dout) throws Exception
    {
        dout.writeInt(u4bytes);
    }
}
