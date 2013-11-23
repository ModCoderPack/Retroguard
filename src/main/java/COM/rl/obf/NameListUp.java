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

import COM.rl.obf.classfile.ClassFileException;

/**
 * Interface to a list of method and field names and descriptors -- used for checking if a name/descriptor is in the
 * public/protected lists of the super-class/interface hierarchy.
 * 
 * @author Mark Welsh
 */
public interface NameListUp
{
    /**
     * Get output method name from list, or null if no mapping exists.
     * 
     * @param name
     * @param descriptor
     * @throws ClassFileException
     */
    public String getMethodOutNameUp(String name, String descriptor) throws ClassFileException;

    /**
     * Get obfuscated method name from list, or null if no mapping exists.
     * 
     * @param name
     * @param descriptor
     * @throws ClassFileException
     */
    public String getMethodObfNameUp(String name, String descriptor) throws ClassFileException;

    /**
     * Get output field name from list, or null if no mapping exists.
     * 
     * @param name
     * @throws ClassFileException
     */
    public String getFieldOutNameUp(String name) throws ClassFileException;

    /**
     * Get obfuscated field name from list, or null if no mapping exists.
     * 
     * @param name
     * @throws ClassFileException
     */
    public String getFieldObfNameUp(String name) throws ClassFileException;
}
