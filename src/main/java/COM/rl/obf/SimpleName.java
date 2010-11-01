/* ===========================================================================
 * $RCSfile: SimpleName.java,v $
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

/**
 * Java 'simple name' -- a class name or a component of a package name, 
 * along with the type of this simple name.
 *
 * @author      Mark Welsh
 */
public class SimpleName
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private String name;
    private boolean isAsPackage;


    // Class methods ---------------------------------------------------------


    // Instance Methods ---------------------------------------------------------
    /** Ctor. */
    public SimpleName(String simpleName)
    {
        name = simpleName;
        isAsPackage = true;
    }

    /** Set simple name as package level. */
    public SimpleName setAsPackage() 
    { 
        isAsPackage = true;
        return this;
    }

    /** Set simple name as class level. */
    public SimpleName setAsClass() 
    { 
        isAsPackage = false;
        return this;
    }

    /** Is this a package level simple name? */
    public boolean isAsPackage() { return isAsPackage; }

    /** Is this a class level simple name? */
    public boolean isAsClass() { return !isAsPackage; }

    /** Return the simple name. */
    public String getName() { return name; }
}


