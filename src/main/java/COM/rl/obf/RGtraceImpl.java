/* ===========================================================================
 * $RCSfile: RGtraceImpl.java,v $
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
 * Main class for stacktrace utility. Converts from obfuscated to normal
 * method names in stack traces (multi-valued due to method overloading). 
 *
 * @author      Mark Welsh
 */
public class RGtraceImpl
{
    // Class Methods ---------------------------------------------------------
    /**
     * Main entry point for the converter.
     *
     * @param rgsFilename log-file name from the obfuscation.
     * @param inTraceFilename obfuscated stack trace file
     * @param outTraceFilename file for unobfuscated stacktrace
     */
    public static void convert(String rgsFilename, String inTraceFilename, 
                               String outTraceFilename) throws Exception 
    {
        File rgsFile = rgsFilename == null ? null : new File(rgsFilename);
        File inTraceFile = inTraceFilename == null ? null : new File(inTraceFilename);
        File outTraceFile = outTraceFilename == null ? null : new File(outTraceFilename);
        
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
        
        // Input stacktrace file must be readable if it exists
        if (inTraceFile != null && !inTraceFile.exists()) 
        {
            throw new IllegalArgumentException("Input stacktrace file specified does not exist.");
        }
        if (inTraceFile != null && !inTraceFile.canRead()) 
        {
            throw new IllegalArgumentException("Input stacktrace file specified exists but cannot be read.");
        }
        
        // Output stacktrace file must be writable if it exists
        if (outTraceFile != null 
            && outTraceFile.exists() && !outTraceFile.canWrite()) 
        {
            throw new IllegalArgumentException("Output stacktrace file cannot be written to.");
        }
        
        // Call the main entry point
        RGtraceImpl.convert(rgsFile, inTraceFile, outTraceFile);
    }
    
    /**
     * Main entry point for the converter.
     *
     * @param rgsFile log-file from the obfuscation
     * @param inTraceFile obfuscated stacktrace file
     * @param outTraceFile file for unobfuscated stacktrace
     */
    public static void convert(File rgsFile, File inTraceFile, 
                               File outTraceFile) throws Exception 
    {
        // Create the name mapping database, using the log file
        ClassDB db = new ClassDB(rgsFile);

        // Open the output file
        PrintStream out = null;
        try 
        {
            out = outTraceFile == null ? System.out :
                new PrintStream(
                    new BufferedOutputStream(
                        new FileOutputStream(outTraceFile)));
	    // Convert the trace file to unobfuscated form
	    db.fromObfForTrace(inTraceFile, out);
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
