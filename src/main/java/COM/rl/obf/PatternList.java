/* ===========================================================================
 * $RCSfile: PatternList.java,v $
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

import java.util.*;

/**
 * Class used for limited pattern matching with '**' (matches across package
 * boundaries) and '*' (does not match across package boundaries) wildcards.
 *
 * @author      Mark Welsh
 */
public class PatternList 
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private Vector subs;
    private int sc = -1;


    // Class Methods ---------------------------------------------------------
    public static PatternList create(String pattern) throws Exception
    {
        return new PatternList(pattern);
    }


    // Instance Methods ------------------------------------------------------
    // Ctor.
    private PatternList(String pattern) throws Exception
    {
        subs = new Vector();
        int scFirst = pattern.indexOf("**");
        int scLast = pattern.lastIndexOf("**");
        int pos = -1;
        while (pos < pattern.length())
        {
            int oldpos = pos;
            pos = pattern.indexOf(ClassTree.PACKAGE_LEVEL, oldpos+1);
            if (pos == -1) pos = pattern.length();
            if (scFirst >= 0 && oldpos+1 <= scFirst && scFirst+2 <= pos)
            {
                sc = length();
                pos = pattern.indexOf(ClassTree.PACKAGE_LEVEL, scLast+2);
                if (pos == -1) pos = pattern.length();
            }   
            subs.addElement(pattern.substring(oldpos+1, pos));
        }
    }

    /** Number of segments in the list. */
    public int length() 
    { 
        return subs.size(); 
    }

    /** Does a '**' wildcard segment exist? */
    public boolean scExists() 
    {
        return sc >= 0;
    }

    /** Index of the '**' wildcard segment. */
    public int scIndex() 
    {
        return sc;
    }
    
    /** Return the i'th segment. */
    public String getSub(int i) 
    {
        return (String)subs.elementAt(i);
    }
    
    /** Return the i'th through j'th segments, joined by package separators. */
    public String getSub(int i, int j) 
    {
        if (i < 0 || i > j || j >= length()) throw new IllegalArgumentException();
        StringBuffer sb = new StringBuffer();
        for (int k = i; k <= j; k++) 
        {
            sb.append(getSub(k));
            if (k < j) sb.append(ClassTree.PACKAGE_LEVEL);
        }
        return sb.toString();
    }
}
