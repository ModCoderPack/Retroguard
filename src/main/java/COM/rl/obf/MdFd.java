/* ===========================================================================
 * $RCSfile: MdFd.java,v $
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
 * Base to method and field tree items.
 * 
 * @author Mark Welsh
 */
abstract public class MdFd extends TreeItem
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private String descriptor = null;
    private boolean isOverride = false;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /**
     * Constructor
     * 
     * @param parent
     * @param isSynthetic
     * @param name
     * @param descriptor
     * @param access
     * @throws ClassFileException
     */
    public MdFd(TreeItem parent, boolean isSynthetic, String name, String descriptor, int access) throws ClassFileException
    {
        super(parent, name);
        this.descriptor = descriptor;
        this.access = access;
        this.isSynthetic = isSynthetic;
        if (name.equals("") || descriptor.equals("") || !(parent instanceof Cl))
        {
            throw new RuntimeException("Internal error: method/field must have name and descriptor, "
                + "and have Class or Interface as parent");
        }

        // Disallow obfuscation of 'Synthetic' methods
        if (isSynthetic)
        {
            this.setOutName(this.getInName());
        }
    }

    /**
     * Return the method or field descriptor String.
     */
    public String getDescriptor()
    {
        return this.descriptor;
    }

    public String getOutDescriptor()
    {
        try
        {
            return this.classTree.mapDescriptor(this.descriptor);
        }
        catch (ClassFileException e)
        {
            // ignore
        }

        return this.descriptor;
    }

    /**
     * Is this member static?
     */
    public boolean isStatic()
    {
        return Modifier.isStatic(this.getModifiers());
    }

    /**
     * Set that this method or field is an override.
     */
    public void setIsOverride()
    {
        this.isOverride = true;
    }

    /**
     * Is this method or field an override?
     */
    public boolean isOverride()
    {
        return this.isOverride;
    }

    /**
     * Does this member match the wildcard pattern? (** and * supported)
     * 
     * @param namePattern
     * @param descPattern
     */
    public boolean isWildcardMatch(String namePattern, String descPattern)
    {
        return this.isWildcardMatch(namePattern) && TreeItem.isMatch(descPattern, this.getDescriptor());
    }
}
