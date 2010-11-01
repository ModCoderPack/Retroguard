/* ===========================================================================
 * $RCSfile: RGpatchImpl.java,v $
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
 * Main class for patch utility. Creates a patch Jar from a full 
 * obfuscation run. 
 *
 * @author      Mark Welsh
 */
public class RGpatchImpl {
    // Class Methods ---------------------------------------------------------
    /**
     * Main entry point for the patch generator.
     *
     * @param inFile the full, obfuscated JAR
     * @param outFile a writable file for obfuscated patch JAR
     * @param rgsFile log-file from the obfuscation.
     * @param listFile list of files to include in the patch (unobfuscated names)
     */
    public static void patch(String inFilename, String outFilename, 
                             String rgsFilename, String listFilename) throws Exception 
    {
        if (inFilename == null || outFilename == null || 
            rgsFilename == null || listFilename == null) 
        {
            throw new IllegalArgumentException("Invalid number of arguments.");
        }
        File inFile = new File(inFilename);
        File outFile = new File(outFilename);
        File rgsFile = new File(rgsFilename);
        File listFile = new File(listFilename);

        // Input JAR file must exist and be readable
        if (!inFile.exists()) 
        {
            throw new IllegalArgumentException("JAR specified for obfuscation does not exist.");
        }
        if (!inFile.canRead()) 
        {
            throw new IllegalArgumentException("JAR specified for obfuscation exists but cannot be read.");
        }
        
        // Output JAR file must be writable if it exists
        if (outFile.exists() && !outFile.canWrite()) 
        {
                throw new IllegalArgumentException("Output JAR file cannot be written to.");
        }
        
        // Script/log file must exist and be readable
        if (!rgsFile.exists()) 
        {
            throw new IllegalArgumentException("Log file specified does not exist.");
        }
        if (!rgsFile.canRead()) 
        {
            throw new IllegalArgumentException("Log file specified exists but cannot be read.");
            }
        
        // List file must exist and be readable
        if (!listFile.exists()) 
        {
            throw new IllegalArgumentException("List specified does not exist.");
        }
        if (!listFile.canRead()) 
        {
                throw new IllegalArgumentException("List specified exists but cannot be read.");
        }
        
        // Call the main entry point on the patch generator.
        RGpatchImpl.patch(inFile, outFile, rgsFile, listFile);
    }

    /**
     * Main entry point for the patch generator.
     *
     * @param inFile the full, obfuscated JAR
     * @param outFile a writable file for obfuscated patch JAR
     * @param rgsFile log-file from the obfuscation.
     * @param listFile list of files to include in the patch (unobfuscated names)
     */
    public static void patch(File inFile, File outFile, 
                             File rgsFile, File listFile) throws Exception 
    {
        // Create the name mapping database, using the log file
        ClassDB db = new ClassDB(rgsFile);

        // Convert the list file to obfuscated form, and initialize the 
        // patch generator
        PatchMaker pm = new PatchMaker(db.toObf(listFile));

        // Copy the obfuscated Jar to a patch Jar, adding only classes (and
        // all of their inner classes) known to the PatchMaker
        pm.makePatch(inFile, outFile);
    }
}
