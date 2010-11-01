/* ===========================================================================
 * $RCSfile: RGgui.java,v $
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
import COM.rl.obf.gui.*;

/**
 * Main class for obfuscator script generating graphical user interface.
 *
 * @author      Mark Welsh
 */
public class RGgui
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------


    // Class Methods ---------------------------------------------------------
    /**
     * Running application with no parameters 'java RGgui' brings up the
     * graphical user interface for constructing script files interactively.
     */
    public static void main(String args[])
    {
        // Run GUI
        try
        {
            Gui.create();
        }
        catch (Exception e)
        {
            System.err.println("RGgui error: " + e.toString());
            System.exit(-1);
        }
    }
}
