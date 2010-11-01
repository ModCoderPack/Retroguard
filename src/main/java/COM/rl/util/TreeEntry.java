/* ===========================================================================
 * $RCSfile: TreeEntry.java,v $
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

package COM.rl.util;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * An entry in the tree control list.
 *
 * @author      Mark Welsh
 */
public interface TreeEntry
{
    // Interface Methods -----------------------------------------------------
    /** Can the element be opened. */
    public boolean canOpen();
    /** Is the element open in the tree? */
    public boolean isOpen();
    /** Set if the element is open in the tree. */
    public void setOpen(boolean isOpen);
    /** String representation of the element for the List. */
    public String toString();
    /** Number of children. */
    public int childCount();
    /** Return the children TreeEntry's. */
    public Enumeration elements();
}
