/* ===========================================================================
 * $RCSfile: NameMapper.java,v $
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

package COM.rl.obf.classfile;

import java.io.*;
import java.util.*;

/**
 * Interface to a class, method, field remapping table.
 *
 * @author      Mark Welsh
 */
public interface NameMapper
{
    // Interface Methods -----------------------------------------------------
    /** Return a list of attributes marked to keep. */
    public String[] getAttrsToKeep() throws Exception;

    /** Mapping for fully qualified class name. */
    public String mapClass(String className) throws Exception;

    /** Mapping for method name, of fully qualified class. */
    public String mapMethod(String className, String methodName, String descriptor) throws Exception;

    /** Mapping for field name, of fully qualified class. */
    public String mapField(String className, String fieldName) throws Exception;

    /** Mapping for descriptor of field or method. */
    public String mapDescriptor(String descriptor) throws Exception;

    /** Mapping for generic type signature. */
    public String mapSignature(String signature) throws Exception;
}
