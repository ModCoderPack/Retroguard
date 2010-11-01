/* ===========================================================================
 * $RCSfile: NameListDown.java,v $
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
 * if a name/descriptor is reserved through a derived class/interface.
 *
 * @author      Mark Welsh
 */
public interface NameListDown
{
    /** Is the method reserved because of its reservation down the class hierarchy? */
    public String getMethodObfNameDown(Cl caller, String name, String descriptor) throws Exception;
    /** Is the field reserved because of its reservation down the class hierarchy? */
    public String getFieldObfNameDown(Cl caller, String name) throws Exception;
}

