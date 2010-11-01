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
import COM.rl.util.*;
import COM.rl.obf.classfile.*;

/**
 * Base to package and class tree item.
 *
 * @author      Mark Welsh
 */
abstract public class PkCl extends TreeItem
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    /** Owns a list of classes. */
    protected Hashtable cls = new Hashtable();


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
        return (Cl)cls.get(name);
    }

    /** Get a class by obfuscated name. */
    public Cl getObfClass(String name) throws Exception 
    {
	for (Enumeration enm = cls.elements(); enm.hasMoreElements(); )
	{
	    Cl cl = (Cl)enm.nextElement();
	    if (name.equals(cl.getOutName())) 
	    {
		return cl;
	    }
	}
        return null;
    }

    /** Get an Enumeration of classes directly beneath this PkCl. */
    public Enumeration getClassEnum() 
    {
        return cls.elements();
    }

    /** Get an Enumeration of all classes (outer and inner) in the tree beneath this PkCl. */
    public Enumeration getAllClassEnum() 
    {
        Vector allClasses = new Vector();
        addAllClasses(allClasses);
        return allClasses.elements();
    }
    /** List classes and recursively compose a list of all inner classes. */
    protected void addAllClasses(Vector allClasses)
    {
        for (Enumeration enm = cls.elements(); enm.hasMoreElements(); )
        {
            Cl cl = (Cl)enm.nextElement();
            allClasses.addElement(cl);
            cl.addAllClasses(allClasses);
        }
    }

    /** Return number of classes. */
    public int getClassCount() 
    {
        return cls.size();
    }

    /** Add a class to the list of owned classes. */
    abstract public Cl addClass(String name, String superName, String[] interfaceNames, int access) throws Exception;

    /** Add a class to the list of owned classes. */
    public Cl addClass(boolean isInnerClass, String name, String superName, String[] interfaceNames, int access) throws Exception
    {
        Cl cl = getClass(name);

        // Remove placeholder if present
        PlaceholderCl plClassItem = null;
        if (cl instanceof PlaceholderCl)
        {
            plClassItem = (PlaceholderCl)cl;
            cls.remove(name);
            cl = null;
        }

        // Add the class, if not already present
        if (cl == null)
        {
            cl = new Cl(this, isInnerClass, name, superName, interfaceNames, access);
            cls.put(name, cl);
        }

        // Copy over the inner class data from the placeholder, if any
        if (plClassItem != null)
        {
            for (Enumeration enm = plClassItem.getClassEnum(); enm.hasMoreElements(); )
            {
                Cl innerCl = (Cl)enm.nextElement();
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
        Cl cl = getClass(name);
        if (cl == null)
        {
            cl = new PlaceholderCl(this, isInnerClass, name);
            cls.put(name, cl);
        }
        return cl;
    }

    /** Generate unique obfuscated names for this namespace. */
    public void generateNames() throws Exception
    {
        generateNames(cls);
    }

    /** Generate unique obfuscated names for a given namespace. */
    protected void generateNames(Hashtable hash) throws Exception
    {
        Vector vec = new Vector();
        for (Enumeration enm = hash.elements(); enm.hasMoreElements(); )
        {
            TreeItem ti = (TreeItem)enm.nextElement();
            if (ti.isFixed())
            {
                vec.addElement(ti.getOutName());
            }
        }
        String[] noObfNames = new String[vec.size()];
        for (int i = 0; i < noObfNames.length; i++)
        {
            noObfNames[i] = (String)vec.elementAt(i);
        }
        NameMaker nm = new KeywordNameMaker(noObfNames, false, true);
        for (Enumeration enm = hash.elements(); enm.hasMoreElements(); )
        {
            TreeItem ti = (TreeItem)enm.nextElement();
            if (!ti.isFixed())
            {
                ti.setOutName(nm.nextName(null));
            }
        }
    }
}

