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
 * Item that forms a tree structure and can represent a package level, a class,
 * or a method or field.
 *
 * @author      Mark Welsh
 */
public class TreeItem
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    protected boolean isSynthetic;  // Is a method or field Synthetic?
    protected int access;  // Access level (interpret using java.lang.reflect.Modifier)
    protected ClassTree classTree = null;   // Our owner
    protected TreeItem parent = null;       // Our immediate parent
    protected String sep = ClassFile.SEP_REGULAR; // Separator preceeding this level's name
    private String inName = null;         // Original name of this item
    private String outName = null;        // Output name of this item
    private boolean isFixed = false; // Has the name been fixed in some way?
    private boolean isFromScript = false; // Is this script constrained?
    private boolean isFromScriptMap = false; // Is this script_map constrained?
    private boolean isTrimmed = false; // Is this to be trimmed?
    private boolean isTrimCheck = false; // Has this been checked for trimming?
    private Vector treeItemRefs = null; // List of referenced Cl/Md/Fd placeholders


    // Class Methods ---------------------------------------------------------
    /** Do a wildcard String match. */
    public static boolean isMatch(String pattern, String string) 
    {
        // Sanity check
        if (pattern == null || string == null) 
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
            else 
            {
                pattern = pattern.substring(pos);
                string = string.substring(pos);
            }
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
            else 
            {
                pattern = pattern.substring(0, pos + 1);
                string = string.substring(0, string.length() - tail.length());
            }
        }
        // Split the pattern at the wildcard positions
        Vector section = new Vector();
        pos = pattern.indexOf('*');
        int rpos = -1;
        while ((rpos = pattern.indexOf('*', pos+1)) != -1) 
        {
            if (rpos != pos + 1) 
            {
                section.addElement(pattern.substring(pos + 1, rpos));
            }
            pos = rpos;
        }
        // Check each section for a non-overlapping match in the string
        for (Enumeration enm = section.elements(); enm.hasMoreElements(); ) 
        {
            String chunk = (String)enm.nextElement();
            pos = string.indexOf(chunk);
            if (pos == -1) 
            {
                return false;
            }
            string = string.substring(pos + chunk.length());
        }
        return true;
    }


    /** Do a generalized wildcard String match - handles '**' wildcard
     *  that matches across package boundaries. */
    public static boolean isGMatch(String pattern, String string) 
    {
        PatternList pl, sl;
        try 
        {
            pl = PatternList.create(pattern);
            sl = PatternList.create(string);
            if (!pl.scExists()) 
            {
                if (pl.length() != sl.length()) return false;
                // check each string identifier against the non-** pattern
                for (int i = 0; i < pl.length(); i++) 
                {
                    if (!isMatch(pl.getSub(i), sl.getSub(i))) return false;
                }
            } 
            else 
            {
                if (pl.length() > sl.length()) return false;
                // check the head identifiers (pre-** segment)
                for (int i = 0; i < pl.scIndex(); i++) 
                {
                    if (!isMatch(pl.getSub(i), sl.getSub(i))) return false;
                }
                // check the tail identifiers (post-** segment)
                for (int i = pl.scIndex() + 1; i < pl.length(); i++) 
                {
                    int j = i + sl.length() - pl.length();
                    if (!isMatch(pl.getSub(i), sl.getSub(j))) return false;
                }
                // check the merged central identifiers against the ** segment
                int j = pl.scIndex() + sl.length() - pl.length();
                if (!isMatch(pl.getSub(pl.scIndex()), 
                             sl.getSub(pl.scIndex(), j))) return false;
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
            classTree = parent.classTree;
        }
    }

    /** Return the modifiers. */
    public int getModifiers() {return access;}

    /** Do the modifiers specified by mask equal the settings? */
    public boolean modifiersMatchMask(int mask, int setting)
    {
        return (getModifiers() & mask) == (setting & mask);
    }

    /** Return the original name of the entry. */
    public String getInName() {return inName;}

    /** Set the output name of the entry. */
    public void setOutName(String outName) 
    {
        this.outName = outName;
        isFixed = true;
    }

    /** Return the output name of the entry, obfuscated or original. */
    public String getOutName() {return outName != null ? outName : inName;}

    /** Return the obfuscated name of the entry. */
    public String getObfName() {return outName;}

    /** Signal that this constraint came from a user script line. */
    public void setFromScript() {isFromScript = true;}

    /** Clear the signal that this constraint came from a user script line. */
    public void clearFromScript() 
    {
        if (isFromScript) 
        {
            isFixed = false;
            isFromScript = false;
        }
    }

    /** Signal that this constraint came from a map script line. */
    public void setFromScriptMap() {isFromScriptMap = true;}

    /** Has the entry been fixed already? */
    public boolean isFixed() {return isFixed;}

    /** Is this constrained by a user script line? */
    public boolean isFromScript() {return isFromScript;}

    /** Is this constrained by a map script line? */
    public boolean isFromScriptMap() {return isFromScriptMap;}

    /** Is a method or field Synthetic? */
    public boolean isSynthetic() {return isSynthetic;}

    /** Set the parent in the tree -- used when stitching in a Cl to replace a PlaceholderCl. */
    public void setParent(TreeItem parent) {this.parent = parent;}

    /** Get the parent in the tree. */
    public TreeItem getParent() {return parent;}

    /** Construct and return the full original name of the entry. */
    public String getFullInName()
    {
        if (parent == null)
        {
            return "";
        }
        else if (parent.parent == null)
        {
            return getInName();
        }
        else
        {
            return parent.getFullInName() + sep + getInName();
        }
    }

    /** Construct and return the full obfuscated name of the entry. */
    public String getFullOutName()
    {
        if (parent == null)
        {
            return "";
        }
        else if (parent.parent == null)
        {
            return getOutName();
        }
        else
        {
            return parent.getFullOutName() + sep + getOutName();
        }
    }

    /** Does this name match the wildcard pattern? (compatibility mode) */
    public boolean isOldStyleMatch(String pattern) 
    {
        return isMatch(pattern, getFullInName());
    }

    /** Does this name match the wildcard pattern? (** and * supported) */
    public boolean isWildcardMatch(String pattern) 
    {
        return isGMatch(pattern, getFullInName());
    }

    /** Set the trimming state for this. */
    public void setTrimmed(boolean trimmed) { isTrimmed = trimmed; }

    /** Is this to be trimmed? */
    public boolean isTrimmed() {return isTrimmed; }

    /** Set the trim-check status for this. */
    public void setTrimCheck(boolean state) { isTrimCheck = state; }

    /** Has this been checked for trimming? */
    public boolean isTrimCheck() {return isTrimCheck; }

    /** Add a TreeItem references. */
    public void addRef(Object ref) 
    {
	if (treeItemRefs == null)
	{
	    treeItemRefs = new Vector();
	}
	treeItemRefs.addElement(ref);
    }

    /** Push internal references from this item to the stack as TreeItems. */
    public void pushRefs(Stack stack) throws Exception
    { 
	if (treeItemRefs != null)
	{
	    for (Enumeration enm = treeItemRefs.elements(); 
		 enm.hasMoreElements(); )
	    {
		TreeItem ti = null;
		Object o = enm.nextElement();
		if (o instanceof TreeItem) 
		{
		    ti = (TreeItem)o;
		}
		else if (o instanceof TreeItemRef) 
		{
		    ti = ((TreeItemRef)o).toTreeItem(classTree);
		}
		stack.push(ti);
	    }
	}
    }
}
