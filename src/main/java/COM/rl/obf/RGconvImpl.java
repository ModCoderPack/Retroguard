/* ===========================================================================
 * $RCSfile: RGconvImpl.java,v $
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
 * Main class for conversion utility. Converts from normal to obfuscated
 * class filenames. 
 *
 * @author      Mark Welsh
 */
public class RGconvImpl
{
    // Class Methods ---------------------------------------------------------
    /**
     * Main entry point for the converter.
     *
     * @param rgsFilename log-file name from the obfuscation.
     * @param inListFilename list of unobfuscated class files
     * @param outListFilename file for list of obfuscated class files
     */
    public static void convert(String rgsFilename, String inListFilename, 
                               String outListFilename) throws Exception 
    {
        File rgsFile = rgsFilename == null ? null : new File(rgsFilename);
        File inListFile = inListFilename == null ? null : new File(inListFilename);
        File outListFile = outListFilename == null ? null : new File(outListFilename);
        
        // Script/log file must exist and be readable
        if (rgsFile == null) 
        {
            throw new IllegalArgumentException("No log file specified.");
        }
        if (!rgsFile.exists()) 
        {
            throw new IllegalArgumentException("Log file specified does not exist.");
        }
        if (!rgsFile.canRead()) 
        {
            throw new IllegalArgumentException("Log file specified exists but cannot be read.");
        }
        
        // Input list file must exist and be readable
        if (inListFile != null && !inListFile.exists()) 
        {
            throw new IllegalArgumentException("Input list specified does not exist.");
        }
        if (inListFile != null && !inListFile.canRead()) 
        {
            throw new IllegalArgumentException("Input list specified exists but cannot be read.");
        }
        
        // Output list file must be writable if it exists
        if (outListFile != null 
            && outListFile.exists() && !outListFile.canWrite()) 
        {
            throw new IllegalArgumentException("Output list file cannot be written to.");
        }
        
        // Call the main entry point
        RGconvImpl.convert(rgsFile, inListFile, outListFile);
    }
    
    /**
     * Main entry point for the converter.
     *
     * @param rgsFile log-file from the obfuscation.
     * @param inListFile list of unobfuscated class files
     * @param outListFile file for list of obfuscated class files
     */
    public static void convert(File rgsFile, File inListFile, 
                               File outListFile) throws Exception 
    {
        // Create the name mapping database, using the log file
        ClassDB db = new ClassDB(rgsFile);

        // Convert the list file to obfuscated form
        Vector output = db.toObfForConv(inListFile);

        // Write the output file
        PrintStream out = null;
        try 
        {
            out = outListFile == null ? System.out :
                new PrintStream(
                    new BufferedOutputStream(
                        new FileOutputStream(outListFile)));
            for (Enumeration enm = output.elements(); enm.hasMoreElements(); ) 
            {
                out.println((String)enm.nextElement());
            }
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
