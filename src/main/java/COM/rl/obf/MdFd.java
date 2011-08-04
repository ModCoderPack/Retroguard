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
    /** Ctor. */
    public MdFd(TreeItem parent, boolean isSynthetic, String name, String descriptor, int access) throws Exception
    {
        super(parent, name);
        this.descriptor = descriptor;
        this.access = access;
        this.isSynthetic = isSynthetic;
        if (name.equals("") || descriptor.equals("") || !(parent instanceof Cl))
        {
            System.err.println("# Internal error: method/field must have name and descriptor, "
                + "and have Class or Interface as parent");
        }

        // Disallow obfuscation of 'Synthetic' methods
        if (isSynthetic)
        {
            this.setOutName(this.getInName());
        }
    }

    /** Return the method or field descriptor String. */
    public String getDescriptor()
    {
        return this.descriptor;
    }

    /** Is this member static? */
    public boolean isStatic()
    {
        return Modifier.isStatic(this.getModifiers());
    }

    /** Set that this method or field is an override. */
    public void setIsOverride()
    {
        this.isOverride = true;
    }

    /** Is this method or field an override? */
    public boolean isOverride()
    {
        return this.isOverride;
    }

    /** Return the display name for field. */
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        int modifiers = this.getModifiers();
        // NOTE - could update with new JDK1.5 modifiers, but that would cause incompatibility with earlier systems for RG
        if (Modifier.isAbstract(modifiers))
        {
            sb.append("abstract ");
        }
        if (Modifier.isSynchronized(modifiers))
        {
            sb.append("synchronized ");
        }
        if (Modifier.isTransient(modifiers))
        {
            sb.append("transient ");
        }
        if (Modifier.isVolatile(modifiers))
        {
            sb.append("volatile ");
        }
        if (Modifier.isNative(modifiers))
        {
            sb.append("native ");
        }
        if (Modifier.isPublic(modifiers))
        {
            sb.append("public ");
        }
        if (Modifier.isProtected(modifiers))
        {
            sb.append("protected ");
        }
        if (Modifier.isPrivate(modifiers))
        {
            sb.append("private ");
        }
        if (Modifier.isStatic(modifiers))
        {
            sb.append("static ");
        }
        if (Modifier.isFinal(modifiers))
        {
            sb.append("final ");
        }
        sb.append(this.getReturnTypeName());
        sb.append(this.getInName());
        sb.append(this.getDescriptorName());
        return sb.toString();
    }

    /** Return the display name of the return type. */
    protected String getReturnTypeName()
    {
        String[] types = this.parseTypes();
        return (types.length > 0 ? types[types.length - 1] : "") + " ";
    }

    /** Return the display name of the descriptor types. */
    abstract protected String getDescriptorName();

    private String[] parsedTypes = null;

    /** Return the parsed descriptor types array. */
    protected String[] parseTypes()
    {
        if (this.parsedTypes == null)
        {
            try
            {
                this.parsedTypes = ClassFile.parseDescriptor(this.getDescriptor(), true);
            }
            catch (Exception e)
            {
                this.parsedTypes = null;
            }
        }
        return this.parsedTypes;
    }

    /** Does this member match the wildcard pattern? (** and * supported) */
    public boolean isWildcardMatch(String namePattern, String descPattern)
    {
        return this.isWildcardMatch(namePattern) && TreeItem.isMatch(descPattern, this.getDescriptor());
    }
}
