/* ===========================================================================
 * $RCSfile: Pk.java,v $
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
 * Tree item representing a package.
 * 
 * @author Mark Welsh
 */
public class Pk extends PkCl
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    /** Owns a list of sub-package levels */
    private Map pks = new HashMap();

    /** Compact name for this package */
    private String repackageName = null;


    // Class Methods ---------------------------------------------------------
    /**
     * Create the root entry for a tree.
     * 
     * @throws ClassFileException
     */
    public static Pk createRoot(ClassTree classTree) throws ClassFileException
    {
        return new Pk(classTree);
    }


    // Instance Methods ------------------------------------------------------
    /**
     * Constructor for default package level.
     * 
     * @throws ClassFileException
     */
    public Pk(ClassTree classTree) throws ClassFileException
    {
        this(null, "");
        this.classTree = classTree;
    }

    /**
     * Constructor for regular package levels.
     * 
     * @throws ClassFileException
     */
    public Pk(TreeItem parent, String name) throws ClassFileException
    {
        super(parent, name);
        if ((parent == null) && !name.equals(""))
        {
            throw new ClassFileException("# Internal error: only the default package has no parent");
        }
        else if ((parent != null) && name.equals(""))
        {
            throw new ClassFileException("# Internal error: the default package cannot have a parent");
        }
    }

    /** Set the repackage name of the entry. */
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

    /** Return the repackage name of the entry. */
    public String getRepackageName()
    {
        return this.repackageName;
    }

    /** Get a package level by name. */
    public Pk getPackage(String name)
    {
        return (Pk)this.pks.get(name);
    }

    /** Get a package level by obfuscated name. */
    public Pk getObfPackage(String name)
    {
        for (Iterator iter = this.pks.values().iterator(); iter.hasNext();)
        {
            Pk pk = (Pk)iter.next();
            if (name.equals(pk.getOutName()))
            {
                return pk;
            }
        }
        return null;
    }

    /** Get a package level by obfuscated repackage name. */
    public Pk getObfRepackage(String name)
    {
        for (Iterator iter = this.pks.values().iterator(); iter.hasNext();)
        {
            Pk pk = (Pk)iter.next();
            if (name.equals(pk.getRepackageName()))
            {
                return pk;
            }
            Pk sub = pk.getObfRepackage(name);
            if (sub != null)
            {
                return sub;
            }
        }
        return null;
    }

    /** Get an Iterator of packages. */
    public Iterator getPackageIter()
    {
        return this.pks.values().iterator();
    }

    /** Return number of packages. */
    public int getPackageCount()
    {
        return this.pks.size();
    }

    /**
     * Add a sub-package level.
     * 
     * @throws ClassFileException
     */
    public Pk addPackage(String name) throws ClassFileException
    {
        Pk pk = this.getPackage(name);
        if (pk == null)
        {
            pk = new Pk(this, name);
            this.pks.put(name, pk);
        }
        return pk;
    }

    /**
     * Add a class.
     * 
     * @throws ClassFileException
     */
    @Override
    public Cl addClass(String name, String superName, List interfaceNames, int access) throws ClassFileException
    {
        return this.addClass(false, name, superName, interfaceNames, access);
    }

    /**
     * Add a placeholder class.
     * 
     * @throws ClassFileException
     */
    @Override
    public Cl addPlaceholderClass(String name) throws ClassFileException
    {
        return this.addPlaceholderClass(false, name);
    }

    /** Generate unique obfuscated names for this namespace. */
    @Override
    public void generateNames()
    {
        super.generateNames();
        this.generateNames(this.pks);
    }

    /** Generate unique-across-run obfuscated repackage name. */
    public void repackageName()
    {
        if ((NameProvider.currentMode != NameProvider.CLASSIC_MODE) || (!this.isFixed()))
        {
            String theOutName = NameProvider.getNewPackageName(this);
            if (theOutName != null)
            {
                this.setRepackageName(theOutName);
                this.setOutName(this.getInName());
                String fullInName = this.getFullInName();
                if (fullInName == "")
                {
                    fullInName = ".";
                }
                String fullOutName = this.getFullOutName();
                if (fullOutName == "")
                {
                    fullOutName = ".";
                }
                System.out.println("# Package " + fullInName + " renamed to " + fullOutName + " from name maker.");
            }
        }
    }

    /** Construct and return the full obfuscated name of the entry. */
    @Override
    public String getFullOutName()
    {
        if (this.getRepackageName() == null)
        {
            return super.getFullOutName();
        }

        return this.getRepackageName();
    }
}
