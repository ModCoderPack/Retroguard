/* ===========================================================================
 * $RCSfile: RetroGuard.java,v $
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

import java.io.*;
import java.util.*;
import COM.rl.obf.*;
import COM.rl.obf.classfile.ClassFile;

/**
 * Main class for obfuscator package. Calls through to packaged implementation
 * class. 
 *
 * @author      Mark Welsh
 */
public class RetroGuard
{
    // Constants -------------------------------------------------------------
    // For interface compatibility with prior versions of RetroGuard
    public static final String DEFAULT_IN_FILE_NAME = RetroGuardImpl.DEFAULT_IN_FILE_NAME;
    public static final String DEFAULT_OUT_FILE_NAME = RetroGuardImpl.DEFAULT_OUT_FILE_NAME;
    public static final String DEFAULT_RGS_FILE_NAME = RetroGuardImpl.DEFAULT_RGS_FILE_NAME;
    public static final String DEFAULT_LOG_FILE_NAME = RetroGuardImpl.DEFAULT_LOG_FILE_NAME;


    // Class Methods ---------------------------------------------------------
    /**
     * Obfuscate an input Jar file on the file system into an output Jar file,
     * using the script file supplied to change obfuscation settings from their
     * default. <p>
     *
     * Usage: java RetroGuard [INPUT-FILE [OUTPUT-FILE [SCRIPT-FILE [LOG-FILE]]]]
     * where INPUT-FILE is the name of the JAR to be obfuscated (defaults to 'in.jar'),
     * OUTPUT-FILE is the name for the obfuscated JAR (defaults to 'out.jar'),
     * SCRIPT-FILE is the file name of a valid RetroGuard script (*.rgs) file
     * (defaults to 'script.rgs'), LOG-FILE is the file name of the output log file
     * in which name mappings are listed (defaults to 'retroguard.log'). <p>
     */
    public static void main(String args[])
    {
        // Check arg-list for validity
        try
        {
            // Get the arguments, or set their default values
            if (args.length < 0 || args.length > 4)
            {
                throw new IllegalArgumentException("Invalid number of arguments.");
            }
            String inFilename = (args.length < 1 ? null : args[0]);
            String outFilename = (args.length < 2 ? null : args[1]);
            String rgsFilename = (args.length < 3 ? null : args[2]);
            String logFilename = (args.length < 4 ? null : args[3]);

            // Call the main entry point on the obfuscator.
            RetroGuardImpl.obfuscate(inFilename, outFilename, 
                                     rgsFilename, logFilename);
            System.exit(0);
        }
        catch (IllegalArgumentException e)
        {
            System.err.println(Version.getVersionComment());
            System.err.println("Problem: " + (e.getMessage() != null ? e.getMessage() : ""));
            System.err.println("Usage: java RetroGuard [INPUT-FILE [OUTPUT-FILE [SCRIPT-FILE [LOG-FILE]]]]");
            System.err.println("  where INPUT-FILE is the JAR to be obfuscated (default: '" + DEFAULT_IN_FILE_NAME + "')");
            System.err.println("        OUTPUT-FILE is name for the obfuscated JAR (default: '" + DEFAULT_OUT_FILE_NAME + "')");
            System.err.println("        SCRIPT-FILE is a valid RetroGuard script (default: '" + DEFAULT_RGS_FILE_NAME + "').");
            System.err.println("        LOG-FILE is the name for the log file (default: '" + DEFAULT_LOG_FILE_NAME + "').");
            System.exit(-1);
        }
        catch (Exception e)
        {
            System.err.println("RetroGuard error: " + e.toString());
            System.exit(-1);
        }
    }
}
