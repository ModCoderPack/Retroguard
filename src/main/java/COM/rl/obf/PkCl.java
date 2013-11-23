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
    /**
     * Owns a list of classes.
     */
    protected Map<String, Cl> cls = new HashMap<String, Cl>();

    /**
     * Compact name for this package
     */
    private String repackageName = null;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /**
     * Constructor
     * 
     * @param parent
     * @param name
     */
    public PkCl(TreeItem parent, String name)
    {
        super(parent, name);

        if (NameProvider.oldHash)
        {
            this.cls = new Hashtable<String, Cl>();
        }
    }

    /**
     * Get a class by name.
     * 
     * @param name
     */
    public Cl getClass(String name)
    {
        return this.cls.get(name);
    }

    /**
     * Get a {@code Collection<Cl>} of classes directly beneath this {@code PkCl}.
     */
    public Collection<Cl> getClasses()
    {
        return this.cls.values();
    }

    /**
     * Add a class to the list of owned classes.
     * 
     * @param name
     * @param superName
     * @param interfaceNames
     * @param access
     */
    abstract public Cl addClass(String name, String superName, List<String> interfaceNames, int access);

    /**
     * Add a class to the list of owned classes.
     * 
     * @param isInnerClass
     * @param name
     * @param superName
     * @param interfaceNames
     * @param access
     */
    public Cl addClass(boolean isInnerClass, String name, String superName, List<String> interfaceNames, int access)
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
            for (Cl innerCl : plClassItem.getClasses())
            {
                innerCl.setParent(cl);
                cl.addClass(innerCl);
            }
        }
        return cl;
    }

    /**
     * Add a placeholder class to our list of owned classes, to be replaced later by the full class.
     * 
     * @param name
     */
    abstract public Cl addPlaceholderClass(String name);

    /**
     * Add a placeholder class to our list of owned classes, to be replaced later by the full class.
     * 
     * @param isInnerClass
     * @param name
     */
    public Cl addPlaceholderClass(boolean isInnerClass, String name)
    {
        Cl cl = this.getClass(name);
        if (cl == null)
        {
            cl = new PlaceholderCl(this, isInnerClass, name);
            this.cls.put(name, cl);
        }
        return cl;
    }

    /**
     * Generate unique obfuscated names for this namespace.
     * 
     * @throws ClassFileException
     */
    public void generateNames() throws ClassFileException
    {
        PkCl.generateNames(this.cls);
    }

    /**
     * Set the repackage name of the entry.
     * 
     * @param repackageName
     */
    public void setRepackageName(String repackageName)
    {
        if (repackageName.equals("."))
        {
            this.repackageName = "";
        }
        else
        {
            this.repackageName = repackageName;
        }
    }

    /**
     * Return the repackage name of the entry.
     */
    public String getRepackageName()
    {
        return this.repackageName;
    }

    /**
     * Return the repackage name of the entry.
     */
    public String getRepackageName(boolean output)
    {
        String s = this.repackageName;

        if (output && s.equals(""))
        {
            s = ".";
        }

        return s;
    }

    /**
     * Generate unique obfuscated names for a given namespace.
     * 
     * @param hash
     */
    protected static void generateNames(Map<String, ? extends TreeItem> hash)
    {
        for (TreeItem ti : hash.values())
        {
            String fullInName = ti.getFullInName(true);
            String thisType = "Misc";
            if (ti instanceof Pk)
            {
                thisType = "Package";
            }
            if (ti instanceof Cl)
            {
                thisType = "Class";
            }

            if ((NameProvider.currentMode != NameProvider.CLASSIC_MODE) || (!ti.isFixed()))
            {
                String theOutName = NameProvider.getNewTreeItemName(ti);
                if (theOutName != null)
                {
                    ti.setOutName(theOutName);
                    ti.setFromScriptMap();
                    String fullOutName = ti.getFullOutName(true);
                    if (fullOutName.equals(fullInName))
                    {
                        NameProvider.verboseLog("# " + thisType + " " + fullInName + " unchanged from name maker");
                    }
                    else
                    {
                        NameProvider.verboseLog("# " + thisType + " " + fullInName + " renamed to "
                            + fullOutName + " from name maker");
                    }
                }
                else
                {
                    NameProvider.verboseLog("# " + thisType + " " + fullInName + " null from name maker");
                }
            }
            else if (ti.isFixed())
            {
                if (ti.isFromScriptMap())
                {
                    String fullOutName = ti.getFullOutName(true);
                    if (fullOutName.equals(fullInName))
                    {
                        NameProvider.verboseLog("# " + thisType + " " + fullInName + " unchanged from ScriptMap");
                    }
                    else
                    {
                        NameProvider.verboseLog("# " + thisType + " " + fullInName + " renamed to "
                            + fullOutName + " from ScriptMap");
                    }
                }
                else if (ti.isFromScript())
                {
                    NameProvider.verboseLog("# " + thisType + " " + fullInName + " fixed from Script");
                }
                else
                {
                    NameProvider.verboseLog("# " + thisType + " " + fullInName + " fixed");
                }
            }
        }
    }
}
