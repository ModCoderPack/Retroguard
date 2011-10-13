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
    /**
     * Owns a list of sub-package levels
     */
    private Map<String, Pk> pks = new HashMap<String, Pk>();

    // Class Methods ---------------------------------------------------------
    /**
     * Create the root entry for a tree.
     * 
     * @param classTree
     */
    public static Pk createRoot(ClassTree classTree)
    {
        return new Pk(classTree);
    }


    // Instance Methods ------------------------------------------------------
    /**
     * Constructor for default package level.
     * 
     * @param classTree
     */
    public Pk(ClassTree classTree)
    {
        this(null, "");
        this.classTree = classTree;
    }

    /**
     * Constructor for regular package levels.
     * 
     * @param parent
     * @param name
     */
    public Pk(TreeItem parent, String name)
    {
        super(parent, name);

        if (NameProvider.oldHash)
        {
            this.pks = new Hashtable<String, Pk>();
        }

        if ((parent == null) && !name.equals(""))
        {
            throw new RuntimeException("Internal error: only the default package has no parent");
        }
        else if ((parent != null) && name.equals(""))
        {
            throw new RuntimeException("Internal error: the default package cannot have a parent");
        }
    }

    /**
     * Get a package level by name.
     * 
     * @param name
     */
    public Pk getPackage(String name)
    {
        return this.pks.get(name);
    }

    /**
     * Get a {@code Collection<Pk>} of packages.
     */
    public Collection<Pk> getPackages()
    {
        return this.pks.values();
    }

    /**
     * Add a sub-package level.
     * 
     * @param name
     */
    public Pk addPackage(String name)
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
     */
    @Override
    public Cl addClass(String name, String superName, List<String> interfaceNames, int access)
    {
        return this.addClass(false, name, superName, interfaceNames, access);
    }

    /**
     * Add a placeholder class.
     */
    @Override
    public Cl addPlaceholderClass(String name)
    {
        return this.addPlaceholderClass(false, name);
    }

    /**
     * Generate unique obfuscated names for this namespace.
     * 
     * @throws ClassFileException
     */
    @Override
    public void generateNames() throws ClassFileException
    {
        super.generateNames();
        PkCl.generateNames(this.pks);
    }

    /**
     * Construct and return the full obfuscated name of the entry.
     */
    @Override
    public String getFullOutName()
    {
        String repackageName = this.getRepackageName();

        if (repackageName != null)
        {
            return repackageName;
        }

        return super.getFullOutName();
    }
}
