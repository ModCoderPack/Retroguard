/* ===========================================================================
 * $RCSfile: RGconv.java,v $
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
 * Main class for conversion utility. Converts from normal to obfuscated
 * class filenames. 
 *
 * @author      Mark Welsh
 */
public class RGconv 
{
    // Class Methods ---------------------------------------------------------
    /**
     * Main entry point for RGconv. 
     *
     * Run as: java RGconv retroguard.log in.list out.list
     *  or as: java RGconv retroguard.log in.list > out.list
     *  or as: cat in.list | java RGconv retroguard.log > out.list
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
            String inListFile = args.length > 1 ? args[1] : null;
            String outListFile = args.length > 2 ? args[2] : null;

            // Call the main entry point
            RGconvImpl.convert(rgsFile, inListFile, outListFile);
        } 
        catch (IllegalArgumentException e) 
        {
            System.err.println(Version.getVersionComment());
            System.err.println("Problem: " + (e.getMessage() != null ? e.getMessage() : ""));
            System.err.println("Usage: java RGconv RGS-FILE INPUT-FILE OUTPUT-FILE");
            System.err.println("  where RGS-FILE is the log file from the obfuscation");
            System.err.println("        INPUT-FILE is a list of unobfuscated class filenames");
            System.err.println("        OUTPUT-FILE is name for the list of obfuscated class filenames");
            System.exit(-1);
        } 
        catch (Exception e) 
        {
            System.err.println("RGconv error: " + e.toString());
            System.exit(-1);
        }
        System.exit(0);
    }
}
