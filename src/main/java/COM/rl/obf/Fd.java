/* ===========================================================================
 * $RCSfile: Fd.java,v $
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
 * Tree item representing a field.
 *
 * @author      Mark Welsh
 */
public class Fd extends MdFd
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /** Ctor. */
    public Fd(TreeItem parent, boolean isSynthetic, String name, String descriptor, int access) throws Exception
    {
        super(parent, isSynthetic, name, descriptor, access);
    }

    /** Return the display name of the descriptor types. */
    protected String getDescriptorName()
    {
        return ";";
    }

    /** Find and add TreeItem references. */
    public void findRefs(ClassFile cf, FieldInfo fieldInfo) throws Exception
    {
	// NOTE - no references from fields
    }
}

