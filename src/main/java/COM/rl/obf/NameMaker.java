/* ===========================================================================
 * $RCSfile: NameMaker.java,v $
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
 * Base class for name generators for a given namespace.
 * The base class gathers statistics for name mappings.
 *
 * @author      Mark Welsh
 */
abstract public class NameMaker
{
    // Fields ----------------------------------------------------------------
    protected static Hashtable frequency = new Hashtable(); // Used for logging frequency of generated names


    // Class Methods ---------------------------------------------------------
    /** Get an enumeration of distinct obfuscated names for all namespaces. */
    public static Enumeration getNames() 
    {
        return frequency.keys();
    }

    /** Get an enumeration of use count for distinct obfuscated names for all namespaces. */
    public static Enumeration getUseCounts() 
    {
        return frequency.elements();
    }


    // Instance Methods ------------------------------------------------------
    /** Return the next unique name for this namespace, differing only for identical arg-lists. */
    public String nextName(String descriptor) throws Exception
    {
        // Log the name usage globally across all namespaces
        String name = getNextName(descriptor);
        Integer intCount = (Integer)frequency.get(name);
        if (intCount == null)
        {
            frequency.put(name, new Integer(0));
        }
        else
        {
            frequency.remove(name);
            frequency.put(name, new Integer(intCount.intValue() + 1));
        }
        return name;
    }

    /** Return the next unique name for this namespace, differing only for identical arg-lists. */
    abstract protected String getNextName(String descriptor) throws Exception;
}
