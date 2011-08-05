/* ===========================================================================
 * $RCSfile: PkCl.java,v $
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

import COM.rl.NameProvider;
import COM.rl.util.*;
import COM.rl.obf.classfile.*;

/**
 * Base to package and class tree item.
 * 
 * @author Mark Welsh
 */
abstract public class PkCl extends TreeItem
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    /** Owns a list of classes. */
    protected Map cls = new HashMap();


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /** Ctor. */
    public PkCl(TreeItem parent, String name)
    {
        super(parent, name);
    }

    /** Get a class by name. */
    public Cl getClass(String name) throws Exception
    {
        return (Cl)this.cls.get(name);
    }

    /** Get a class by obfuscated name. */
    public Cl getObfClass(String name) throws Exception
    {
        for (Iterator iter = this.cls.values().iterator(); iter.hasNext();)
        {
            Cl cl = (Cl)iter.next();
            if (name.equals(cl.getOutName()))
            {
                return cl;
            }
        }
        return null;
    }

    /** Get an Iterator of classes directly beneath this PkCl. */
    public Iterator getClassIter()
    {
        return this.cls.values().iterator();
    }

    /** Get an Iterator of all classes (outer and inner) in the tree beneath this PkCl. */
    public Iterator getAllClassIter()
    {
        List allClasses = new ArrayList();
        this.addAllClasses(allClasses);
        return allClasses.iterator();
    }

    /** List classes and recursively compose a list of all inner classes. */
    protected void addAllClasses(List allClasses)
    {
        for (Iterator iter = this.cls.values().iterator(); iter.hasNext();)
        {
            Cl cl = (Cl)iter.next();
            allClasses.add(cl);
            cl.addAllClasses(allClasses);
        }
    }

    /** Return number of classes. */
    public int getClassCount()
    {
        return this.cls.size();
    }

    /** Add a class to the list of owned classes. */
    abstract public Cl addClass(String name, String superName, String[] interfaceNames, int access) throws Exception;

    /** Add a class to the list of owned classes. */
    public Cl addClass(boolean isInnerClass, String name, String superName, String[] interfaceNames, int access) throws Exception
    {
        Cl cl = this.getClass(name);

        // Remove placeholder if present
        PlaceholderCl plClassItem = null;
        if (cl instanceof PlaceholderCl)
        {
            plClassItem = (PlaceholderCl)cl;
            this.cls.remove(name);
            cl = null;
        }

        // Add the class, if not already present
        if (cl == null)
        {
            cl = new Cl(this, isInnerClass, name, superName, interfaceNames, access);
            this.cls.put(name, cl);
        }

        // Copy over the inner class data from the placeholder, if any
        if (plClassItem != null)
        {
            for (Iterator iter = plClassItem.getClassIter(); iter.hasNext();)
            {
                Cl innerCl = (Cl)iter.next();
                innerCl.setParent(cl);
                cl.addClass(innerCl);
            }
        }
        return cl;
    }

    /** Add a placeholder class to our list of owned classes, to be replaced later by the full class. */
    abstract public Cl addPlaceholderClass(String name) throws Exception;

    /** Add a placeholder class to our list of owned classes, to be replaced later by the full class. */
    public Cl addPlaceholderClass(boolean isInnerClass, String name) throws Exception
    {
        Cl cl = this.getClass(name);
        if (cl == null)
        {
            cl = new PlaceholderCl(this, isInnerClass, name);
            this.cls.put(name, cl);
        }
        return cl;
    }

    /** Generate unique obfuscated names for this namespace. */
    public void generateNames() throws Exception
    {
        this.generateNames(this.cls);
    }

    /** Generate unique obfuscated names for a given namespace. */
    protected void generateNames(Map hash) throws Exception
    {
        for (Iterator iter = hash.values().iterator(); iter.hasNext();)
        {
            TreeItem ti = (TreeItem)iter.next();
            if ((NameProvider.currentMode != NameProvider.CLASSIC_MODE) || (!ti.isFixed()))
            {
                String theOutName = NameProvider.getNewTreeItemName(ti);
                if (theOutName != null)
                {
                    ti.setOutName(theOutName);
                    String fullInName = ti.getFullInName();
                    if (fullInName == "")
                    {
                        fullInName = ".";
                    }
                    String fullOutName = ti.getFullOutName();
                    if (fullOutName == "")
                    {
                        fullOutName = ".";
                    }
                    String thisType = "Misc";
                    if (ti instanceof Pk)
                    {
                        thisType = "Package";
                    }
                    if (ti instanceof Cl)
                    {
                        thisType = "Class";
                    }
                    System.out.println("# " + thisType + " " + fullInName + " renamed to " + fullOutName + " from name maker.");
                }
            }
        }
    }
}
