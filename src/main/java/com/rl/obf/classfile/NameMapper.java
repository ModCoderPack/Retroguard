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

package com.rl.obf.classfile;

import java.io.*;
import java.util.*;

/**
 * Interface to a class, method, field remapping table.
 * 
 * @author Mark Welsh
 */
public interface NameMapper
{
    // Interface Methods -----------------------------------------------------
    /**
     * Return a {@code List<String>} of attributes marked to keep.
     */
    public List<String> getAttrsToKeep();

    /**
     * Mapping for fully qualified class name.
     * 
     * @param className
     * @throws ClassFileException
     */
    public String mapClass(String className) throws ClassFileException;

    /**
     * Mapping for method name, of fully qualified class.
     * 
     * @param className
     * @param methodName
     * @param descriptor
     * @throws ClassFileException
     */
    public String mapMethod(String className, String methodName, String descriptor) throws ClassFileException;

    /**
     * Mapping for field name, of fully qualified class.
     * 
     * @param className
     * @param fieldName
     * @throws ClassFileException
     */
    public String mapField(String className, String fieldName) throws ClassFileException;

    /**
     * Mapping for descriptor of field or method.
     * 
     * @param descriptor
     * @throws ClassFileException
     */
    public String mapDescriptor(String descriptor) throws ClassFileException;

    /**
     * Mapping for generic type signature of class.
     * 
     * @param signature
     * @throws ClassFileException
     */
    public String mapSignatureClass(String signature) throws ClassFileException;

    /**
     * Mapping for generic type signature of method.
     * 
     * @param signature
     * @throws ClassFileException
     */
    public String mapSignatureMethod(String signature) throws ClassFileException;

    /**
     * Mapping for generic type signature of field.
     * 
     * @param signature
     * @throws ClassFileException
     */
    public String mapSignatureField(String signature) throws ClassFileException;
}
