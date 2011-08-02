/* ===========================================================================
 * $RCSfile: TreeItem.java,v $
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
 * Item that forms a tree structure and can represent a package level, a class, or a method or field.
 * 
 * @author Mark Welsh
 */
public class TreeItem
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    /** Is a method or field Synthetic? */
    protected boolean isSynthetic;

    /** Access level (interpret using java.lang.reflect.Modifier) */
    protected int access;

    /** Our owner */
    protected ClassTree classTree = null;

    /** Our immediate parent */
    protected TreeItem parent = null;

    /** Separator preceeding this level's name */
    protected String sep = ClassFile.SEP_REGULAR;

    /** Original name of this item */
    private String inName = null;

    /** Output name of this item */
    private String outName = null;

    /** Has the name been fixed in some way? */
    private boolean isFixed = false;

    /** Is this script constrained? */
    private boolean isFromScript = false;

    /** Is this script_map constrained? */
    private boolean isFromScriptMap = false;

    /** Has this been checked for trimming? */
    private boolean isTrimCheck = false;


    // Class Methods ---------------------------------------------------------
    /** Do a wildcard String match. */
    public static boolean isMatch(String pattern, String string)
    {
        // Sanity check
        if ((pattern == null) || (string == null))
        {
            return false;
        }

        // Not really a wildcard, then check for exact match
        if (pattern.indexOf('*') == -1)
        {
            return pattern.equals(string);
        }

        // Check for match of head
        int pos = -1;
        if (pattern.charAt(0) != '*')
        {
            pos = pattern.indexOf('*');
            String head = pattern.substring(0, pos);
            if (string.length() < head.length())
            {
                return false;
            }

            if (!string.substring(0, head.length()).equals(head))
            {
                return false;
            }

            pattern = pattern.substring(pos);
            string = string.substring(pos);
        }
        // Check for match of tail
        if (pattern.charAt(pattern.length() - 1) != '*')
        {
            pos = pattern.lastIndexOf('*');
            String tail = pattern.substring(pos + 1);
            if (string.length() < tail.length())
            {
                return false;
            }

            if (!string.substring(string.length() - tail.length()).equals(tail))
            {
                return false;
            }

            pattern = pattern.substring(0, pos + 1);
            string = string.substring(0, string.length() - tail.length());
        }
        // Split the pattern at the wildcard positions
        Vector<String> section = new Vector<String>();
        pos = pattern.indexOf('*');
        int rpos = -1;
        while ((rpos = pattern.indexOf('*', pos + 1)) != -1)
        {
            if (rpos != pos + 1)
            {
                section.addElement(pattern.substring(pos + 1, rpos));
            }
            pos = rpos;
        }
        // Check each section for a non-overlapping match in the string
        for (Enumeration<String> enm = section.elements(); enm.hasMoreElements();)
        {
            String chunk = enm.nextElement();
            pos = string.indexOf(chunk);
            if (pos == -1)
            {
                return false;
            }
            string = string.substring(pos + chunk.length());
        }
        return true;
    }


    /** Do a generalized wildcard String match - handles '**' wildcard that matches across package boundaries. */
    public static boolean isGMatch(String pattern, String string)
    {
        PatternList pl, sl;
        try
        {
            pl = PatternList.create(pattern);
            sl = PatternList.create(string);
            if (!pl.scExists())
            {
                if (pl.length() != sl.length())
                {
                    return false;
                }
                // check each string identifier against the non-** pattern
                for (int i = 0; i < pl.length(); i++)
                {
                    if (!TreeItem.isMatch(pl.getSub(i), sl.getSub(i)))
                    {
                        return false;
                    }
                }
            }
            else
            {
                if (pl.length() > sl.length())
                {
                    return false;
                }
                // check the head identifiers (pre-** segment)
                for (int i = 0; i < pl.scIndex(); i++)
                {
                    if (!TreeItem.isMatch(pl.getSub(i), sl.getSub(i)))
                    {
                        return false;
                    }
                }
                // check the tail identifiers (post-** segment)
                for (int i = pl.scIndex() + 1; i < pl.length(); i++)
                {
                    int j = i + sl.length() - pl.length();
                    if (!TreeItem.isMatch(pl.getSub(i), sl.getSub(j)))
                    {
                        return false;
                    }
                }
                // check the merged central identifiers against the ** segment
                int j = pl.scIndex() + sl.length() - pl.length();
                if (!TreeItem.isMatch(pl.getSub(pl.scIndex()), sl.getSub(pl.scIndex(), j)))
                {
                    return false;
                }
            }
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }


    // Instance Methods ------------------------------------------------------
    /** Ctor. */
    public TreeItem(TreeItem parent, String name)
    {
        this.parent = parent;
        this.inName = name;
        if (parent != null)
        {
            this.classTree = parent.classTree;
        }
    }

    /** Return the modifiers. */
    public int getModifiers()
    {
        return this.access;
    }

    /** Do the modifiers specified by mask equal the settings? */
    public boolean modifiersMatchMask(int mask, int setting)
    {
        return (this.getModifiers() & mask) == (setting & mask);
    }

    /** Return the original name of the entry. */
    public String getInName()
    {
        return this.inName;
    }

    /** Set the output name of the entry. */
    public void setOutName(String outName)
    {
        this.outName = outName;
        this.isFixed = true;
    }

    /** Return the output name of the entry, obfuscated or original. */
    public String getOutName()
    {
        return this.outName != null ? this.outName : this.inName;
    }

    /** Return the obfuscated name of the entry. */
    public String getObfName()
    {
        return this.outName;
    }

    /** Signal that this constraint came from a user script line. */
    public void setFromScript()
    {
        this.isFromScript = true;
    }

    /** Clear the signal that this constraint came from a user script line. */
    public void clearFromScript()
    {
        if (this.isFromScript)
        {
            this.isFixed = false;
            this.isFromScript = false;
        }
    }

    /** Signal that this constraint came from a map script line. */
    public void setFromScriptMap()
    {
        this.isFromScriptMap = true;
    }

    /** Has the entry been fixed already? */
    public boolean isFixed()
    {
        return this.isFixed;
    }

    /** Is this constrained by a user script line? */
    public boolean isFromScript()
    {
        return this.isFromScript;
    }

    /** Is this constrained by a map script line? */
    public boolean isFromScriptMap()
    {
        return this.isFromScriptMap;
    }

    /** Is a method or field Synthetic? */
    public boolean isSynthetic()
    {
        return this.isSynthetic;
    }

    /** Set the parent in the tree -- used when stitching in a Cl to replace a PlaceholderCl. */
    public void setParent(TreeItem parent)
    {
        this.parent = parent;
    }

    /** Get the parent in the tree. */
    public TreeItem getParent()
    {
        return this.parent;
    }

    /** Construct and return the full original name of the entry. */
    public String getFullInName()
    {
        if (this.parent == null)
        {
            return "";
        }
        else if (this.parent.parent == null)
        {
            return this.getInName();
        }
        else
        {
            String s = this.parent.getFullInName();
            if (s.equals(""))
            {
                return this.getInName();
            }
            return s + this.sep + this.getInName();
        }
    }

    /** Construct and return the full obfuscated name of the entry. */
    public String getFullOutName()
    {
        if (this.parent == null)
        {
            return "";
        }
        else if (this.parent.parent == null)
        {
            return this.getOutName();
        }
        else
        {
            String s = this.parent.getFullOutName();
            if (s.equals("") || (this instanceof Pk))
            {
                return this.getOutName();
            }
            return s + this.sep + this.getOutName();
        }
    }

    /** Does this name match the wildcard pattern? (compatibility mode) */
    public boolean isOldStyleMatch(String pattern)
    {
        return TreeItem.isMatch(pattern, this.getFullInName());
    }

    /** Does this name match the wildcard pattern? (** and * supported) */
    public boolean isWildcardMatch(String pattern)
    {
        return TreeItem.isGMatch(pattern, this.getFullInName());
    }

    /** Set the trim-check status for this. */
    public void setTrimCheck(boolean state)
    {
        this.isTrimCheck = state;
    }

    /** Has this been checked for trimming? */
    public boolean isTrimCheck()
    {
        return this.isTrimCheck;
    }
}
