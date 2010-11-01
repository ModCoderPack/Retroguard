/* ===========================================================================
 * $RCSfile: Sort.java,v $
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

package COM.rl.util;

import java.io.*;
import java.util.*;

/**
 * Various sorting algorithms. The comparison interface is Compare.
 *
 * @see Compare
 *
 * @author Mark Welsh
 */
public class Sort
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------


    // Class Methods ---------------------------------------------------------
    /** Quicksort. */
    public static void quicksort(Object[] data, Compare cmp)
    {
        if (data != null && data.length > 1 && cmp != null)
        {
            quicksort(data, 0, data.length - 1, cmp);
        }
    }
    private static void quicksort(Object[] data, int left, int right, Compare cmp)
    {
        int i = left;
        int j = right;

        // Sort range of data [left, right] into less, equal, more groups
        Object mid = data[(left + right) / 2];
        do 
        {
            while (cmp.isLess(data[i], mid)) i++;
            while (cmp.isLess(mid, data[j])) j--;
            if (i <= j)
            {
                Object tmp = data[i];
                data[i] = data[j];
                data[j] = tmp;
                i++;
                j--;
            }
        } while (i <= j);

        // Sort less group, if necessary
        if (left < j)
        {
            quicksort(data, left, j, cmp);
        }

        // Sort more group, if necessary
        if (i < right)
        {
            quicksort(data, i, right, cmp);
        }
    }


    // Instance Methods ------------------------------------------------------


}
