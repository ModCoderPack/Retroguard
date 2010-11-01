/* ===========================================================================
 * $RCSfile: ParameterAnnotationsInfo.java,v $
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
 * Representation of an parameter annotations table entry.
 *
 * @author      Mark Welsh
 */
public class ParameterAnnotationsInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u2numAnnotations;
    private AnnotationInfo[] annotationTable;


    // Class Methods ---------------------------------------------------------
    public static ParameterAnnotationsInfo create(DataInput din) throws Exception
    {
        ParameterAnnotationsInfo pai = new ParameterAnnotationsInfo();
        pai.read(din);
        return pai;
    }


    // Instance Methods ------------------------------------------------------
    private ParameterAnnotationsInfo() {}

    /** Return the array of annotation table entries. */
    protected AnnotationInfo[] getAnnotationTable() throws Exception
    {
        return annotationTable;
    }

    /** Check for Utf8 references to constant pool and mark them. */
    protected void markUtf8Refs(ConstantPool pool) throws Exception
    {
        for (int i = 0; i < annotationTable.length; i++)
        {
            annotationTable[i].markUtf8Refs(pool);
        }
    }

    private void read(DataInput din) throws Exception
    {
        u2numAnnotations = din.readUnsignedShort();
        annotationTable = new AnnotationInfo[u2numAnnotations];
        for (int i = 0; i < u2numAnnotations; i++)
        {
            annotationTable[i] = AnnotationInfo.create(din);
        }
    }

    /** Export the representation to a DataOutput stream. */
    public void write(DataOutput dout) throws Exception
    {
        dout.writeShort(u2numAnnotations);
        for (int i = 0; i < u2numAnnotations; i++)
        {
            annotationTable[i].write(dout);
        }
    }

    /** Do necessary name remapping. */
    protected void remap(ClassFile cf, NameMapper nm) throws Exception 
    { 
        for (int i = 0; i < u2numAnnotations; i++)
        {
            annotationTable[i].remap(cf, nm);
        }
    }
}
