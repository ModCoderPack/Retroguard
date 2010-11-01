/* ===========================================================================
 * $RCSfile: TreeItemRef.java,v $
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
import COM.rl.util.*;

/**
 * Reference to a TreeItem that may or may not currently exist in ClassTree.
 *
 * @author      Mark Welsh
 */
abstract public class TreeItemRef
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    protected TreeItem ti = null; // Referenced item in ClassTree
    protected String className = null;
    protected String name = null;
    protected String descriptor = null;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /** Ctor. */
    protected TreeItemRef(String className, String name, String descriptor)
    {
        this.className = className;
        this.name = name;
        this.descriptor = descriptor;
    }

    /** Convert ref to item using ClassTree, or null if not present. */
    abstract public TreeItem toTreeItem(ClassTree classTree) throws Exception;
}






