/* ===========================================================================
 * $RCSfile: NameListUp.java,v $
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

/**
 * Interface to a list of method and field names and descriptors -- used for checking
 * if a name/descriptor is in the public/protected lists of the super-class/interface
 * hierarchy.
 *
 * @author      Mark Welsh
 */
public interface NameListUp
{
    /** Get output method name from list, or null if no mapping exists. */
    public String getMethodOutNameUp(String name, String descriptor) throws Exception;

    /** Get obfuscated method name from list, or null if no mapping exists. */
    public String getMethodObfNameUp(String name, String descriptor) throws Exception;

    /** Get output field name from list, or null if no mapping exists. */
    public String getFieldOutNameUp(String name) throws Exception;

    /** Get obfuscated field name from list, or null if no mapping exists. */
    public String getFieldObfNameUp(String name) throws Exception;
}

