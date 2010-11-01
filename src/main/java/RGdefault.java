/* ===========================================================================
 * $RCSfile: RGdefault.java,v $
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
 * Main class for output of internal script file. 
 *
 * @author      Mark Welsh
 */
public class RGdefault 
{
    // Class Methods ---------------------------------------------------------
    /**
     * Main entry point for RGdefault.
     *
     * Run as: java RGdefault default.rgs
     *  or as: java RGdefault > default.rgs
     */
    public static void main(String args[]) 
    {
        // Check arg-list for validity
        try 
        {
            // Get the arguments, or set their default values
            if (args.length > 1) 
            {
                throw new IllegalArgumentException("Invalid number of arguments.");
            }
            String outRgsFile = args.length > 0 ? args[0] : null;

            // Call the main entry point
            RGdefaultImpl.output(outRgsFile);
        } 
        catch (IllegalArgumentException e) 
        {
            System.err.println(Version.getVersionComment());
            System.err.println("Problem: " + (e.getMessage() != null ? e.getMessage() : ""));
            System.err.println("Usage: java RGdefault OUTPUT-FILE");
            System.err.println("  where OUTPUT-FILE will contain the internal default script file");
            System.exit(-1);
        } 
        catch (Exception e) 
        {
            System.err.println("RGdefault error: " + e.toString());
            System.exit(-1);
        }
        System.exit(0);
    }
}
