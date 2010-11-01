/* ===========================================================================
 * $RCSfile: Compare.java,v $
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

/**
 * Comparison interface used in sorting algorithms
 *
 * @see Sort
 * @author Mark Welsh
 */
public interface Compare
{
    /** Is first object less than second object? */
    public boolean isLess(Object o1, Object o2);
}
