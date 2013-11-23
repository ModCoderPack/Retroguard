/* ===========================================================================
 * $RCSfile: LineNumberInfo.java,v $
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
 * Representation of an Line Number table entry.
 * 
 * @author Mark Welsh
 */
public class LineNumberInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u2startpc;
    private int u2lineNumber;


    // Class Methods ---------------------------------------------------------
    /**
     * @param din
     * @throws IOException
     */
    public static LineNumberInfo create(DataInput din) throws IOException
    {
        LineNumberInfo lni = new LineNumberInfo();
        lni.read(din);
        return lni;
    }


    // Instance Methods ------------------------------------------------------
    /**
     * Private constructor
     */
    private LineNumberInfo()
    {
    }

    /**
     * @param din
     * @throws IOException
     */
    private void read(DataInput din) throws IOException
    {
        this.u2startpc = din.readUnsignedShort();
        this.u2lineNumber = din.readUnsignedShort();
    }

    /**
     * Export the representation to a DataOutput stream.
     * 
     * @param dout
     * @throws IOException
     */
    public void write(DataOutput dout) throws IOException
    {
        dout.writeShort(this.u2startpc);
        dout.writeShort(this.u2lineNumber);
    }
}
