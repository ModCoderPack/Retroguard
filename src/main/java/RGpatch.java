/* ===========================================================================
 * $RCSfile: RGpatch.java,v $
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

/**
 * Main class for patch utility. Creates a patch Jar from a full 
 * obfuscation run. 
 *
 * @author      Mark Welsh
 */
public class RGpatch {
    // Class Methods ---------------------------------------------------------
    /**
     * Main entry point for RGpatch - runs with obligatory 4 arguments. 
     */
    public static void main(String args[]) 
    {
        // Check arg-list for validity
        try 
        {
            // Get the arguments, or set their default values
            if (args.length != 4) 
            {
                throw new IllegalArgumentException("Invalid number of arguments.");
            }

            // Call the main entry point on the patch generator.
            RGpatchImpl.patch(args[0], args[1], args[2], args[3]);
        } 
        catch (IllegalArgumentException e) 
        {
            System.err.println(Version.getVersionComment());
            System.err.println("Problem: " + (e.getMessage() != null ? e.getMessage() : ""));
            System.err.println("Usage: java RGpatch INPUT-FILE OUTPUT-FILE LOG-FILE LIST-FILE");
            System.err.println("  where INPUT-FILE is the full obfuscated JAR");
            System.err.println("        OUTPUT-FILE is name for the patch JAR");
            System.err.println("        RGS-FILE is the log file from the obfuscation");
            System.err.println("        LIST-FILE is the list of files (unobfuscated names) to include in the patch");
            System.exit(-1);
        } 
        catch (Exception e) 
        {
            System.err.println("RGpatch error: " + e.getMessage());
            System.exit(-1);
        }
        System.exit(0);
    }
}
