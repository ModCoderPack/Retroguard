/* ===========================================================================
 * $RCSfile: PkClEntry.java,v $
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

package COM.rl.obf.gui;

import java.io.*;
import java.util.*;
import COM.rl.util.*;
import COM.rl.obf.*;

/**
 * An entry in the tree control list.
 *
 * @author      Mark Welsh
 */
public class PkClEntry implements TreeEntry
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private PkCl pkcl;
    private boolean isOpen;
    private String string;
    private int depth;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /** Ctor. */
    public PkClEntry(int depth, PkCl pkcl)
    {
        this.depth = depth;
        this.pkcl = pkcl;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < depth; i++)
        {
            sb.append("        ");
        }
        if (pkcl instanceof Pk)
        {
            string = sb.toString() + "[Package]" + pkcl.getInName();
        }
        else
        {
            string = sb.toString() + pkcl.getInName();
        }
    }

    /** Return the wrapped PkCl. */
    public PkCl getPkCl() {return pkcl;}

    /** Can the element be opened. */
    public boolean canOpen() {return true;}

    /** Is the element open in the tree? */
    public boolean isOpen() {return isOpen;}

    /** Set if the element is open in the tree. */
    public void setOpen(boolean isOpen) {this.isOpen = isOpen;}

    /** String representation of the element for the List. */
    public String toString() {return string;}

    /** Number of children. */
    public int childCount()
    {
        int totalCount = pkcl.getClassCount();
        if (pkcl instanceof Pk)
        {
            totalCount += ((Pk)pkcl).getPackageCount();
        }
        return totalCount;
    }

    /** Return the children TreeEntry's. */
    public Enumeration elements() 
    {
        Vector vec = new Vector();
        try 
        {
            // For a package, list sub-packages
            if (pkcl instanceof Pk) 
            {
                PkClEntry[] sortPackages = new PkClEntry[((Pk)pkcl).getPackageCount()];
                int i = 0;
                for (Enumeration enm = ((Pk)pkcl).getPackageEnum(); enm.hasMoreElements(); i++) {
                    sortPackages[i] = new PkClEntry(depth + 1, (PkCl)enm.nextElement());
                }
                Sort.quicksort(sortPackages, 
                               new Compare() {
                                   public boolean isLess(Object o1, Object o2) 
                                   {
                                       return ((PkClEntry)o1).getPkCl().getInName().compareTo(((PkClEntry)o2).getPkCl().getInName()) < 0;
                                   }
                               });
                for (i = 0; i < sortPackages.length; i++) 
                {
                    vec.addElement(sortPackages[i]);
                }
            }
            // Now list class entries
            PkClEntry[] sortClasses = new PkClEntry[pkcl.getClassCount()];
            int i = 0;
            for (Enumeration enm = pkcl.getClassEnum(); 
                 enm.hasMoreElements(); i++) 
            {
                sortClasses[i] = new PkClEntry(depth + 1, (PkCl)enm.nextElement());
            }
            Sort.quicksort(sortClasses, 
                           new Compare() {
                               public boolean isLess(Object o1, Object o2) 
                               {
                                   return ((PkClEntry)o1).getPkCl().getInName().compareTo(((PkClEntry)o2).getPkCl().getInName()) < 0;
                               }
                           });
            for (i = 0; i < sortClasses.length; i++) 
            {
                vec.addElement(sortClasses[i]);
            }
        } 
        catch (Exception e) 
        {
            vec = new Vector();
        }
        return vec.elements();
    }
}
