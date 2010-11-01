/* ===========================================================================
 * $RCSfile: PlaceholderCl.java,v $
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
import java.lang.reflect.*;
import java.util.*;
import COM.rl.util.*;
import COM.rl.obf.classfile.*;

/**
 * Placeholder class -- used to represent a class which has inner classes, before the
 * class itself has been encountered.
 *
 * @author      Mark Welsh
 */
public class PlaceholderCl extends Cl
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /** Ctor. */
    public PlaceholderCl(TreeItem parent, boolean isInnerClass, String name) throws Exception
    {
        super(parent, isInnerClass, name, null, null, 0);
    }
}

