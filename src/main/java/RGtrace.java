/* ===========================================================================
 * $RCSfile: RGtrace.java,v $
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
 * Main class for stacktrace utility. Converts from obfuscated to normal
 * method names in stack traces (multi-valued due to method overloading). 
 *
 * @author      Mark Welsh
 */
public class RGtrace 
{
    // Class Methods ---------------------------------------------------------
    /**
     * Main entry point for RGtrace. 
     *
     * Run as: java RGtrace retroguard.log in.stacktrace out.stacktrace
     *  or as: java RGconv retroguard.log in.stacktrace > out.stacktrace
     *  or as: cat in.stacktrace | java RGconv retroguard.log > out.stacktrace
     */
    public static void main(String args[]) 
    {
        // Check arg-list for validity
        try 
        {
            // Get the arguments, or set their default values
            if (args.length < 1 || args.length > 3) 
            {
                throw new IllegalArgumentException("Invalid number of arguments.");
            }
            String rgsFile = args[0];
            String inTraceFile = args.length > 1 ? args[1] : null;
            String outTraceFile = args.length > 2 ? args[2] : null;

            // Call the main entry point
            RGtraceImpl.convert(rgsFile, inTraceFile, outTraceFile);
        } 
        catch (IllegalArgumentException e) 
        {
            System.err.println(Version.getVersionComment());
            System.err.println("Problem: " + (e.getMessage() != null ? e.getMessage() : ""));
            System.err.println("Usage: java RGtrace LOGFILE [INPUT-FILE [OUTPUT-FILE]]");
            System.err.println("  where LOGFILE is the log file from the obfuscation");
            System.err.println("        INPUT-FILE is an obfuscated stack trace (defaults to stdin)");
            System.err.println("        OUTPUT-FILE is the filename for the unobfuscated stack trace (defaults to stdout)");
            System.exit(-1);
        } 
        catch (Exception e) 
        {
            System.err.println("RGtrace error: " + e.toString());
            System.exit(-1);
        }
        System.exit(0);
    }
}
