/* ===========================================================================
 * $RCSfile: RGdefaultImpl.java,v $
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

package COM.rl.obf;

import java.io.*;
import java.util.*;
import COM.rl.obf.patch.*;

/**
 * Main class for utility to output the internal default script file. 
 *
 * @author      Mark Welsh
 */
public class RGdefaultImpl
{
    // Class Methods ---------------------------------------------------------
    /**
     * Main entry point.
     *
     * @param rgsFilename output file name
     */
    public static void output(String rgsFilename) throws Exception 
    {
        File rgsFile = rgsFilename == null ? null : new File(rgsFilename);
        
        // Output list file must be writable if it exists
        if (rgsFile != null 
            && rgsFile.exists() && !rgsFile.canWrite()) 
        {
            throw new IllegalArgumentException("Output file cannot be written to.");
        }
        
        // Call the main entry point
        RGdefaultImpl.output(rgsFile);
    }
    
    /**
     * Main entry point for the converter.
     *
     * @param rgsFile output file
     */
    public static void output(File rgsFile) throws Exception 
    {
        // Write the output file
        PrintStream out = null;
        try 
        {
            out = rgsFile == null ? System.out :
                new PrintStream(
                    new BufferedOutputStream(
                        new FileOutputStream(rgsFile)));
            out.println("# The RetroGuard internal default script is listed below.");
            out.println("# You can comment out or add lines to change how the obfuscator works.");
            out.println("# Please refer to http://retrologic.com/retroguard-docs.html for more details.");
            out.print(RgsEnum.getDefaultRgs());
            out.flush();
        } 
        finally 
        {
            if (out != null) 
            {
                out.close();
            }
        }
    }
}
