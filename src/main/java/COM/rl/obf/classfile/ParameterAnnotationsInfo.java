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
 * @author Mark Welsh
 */
public class ParameterAnnotationsInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u2numAnnotations;
    private List annotationTable;


    // Class Methods ---------------------------------------------------------
    public static ParameterAnnotationsInfo create(DataInput din) throws IOException, ClassFileException
    {
        ParameterAnnotationsInfo pai = new ParameterAnnotationsInfo();
        pai.read(din);
        return pai;
    }


    // Instance Methods ------------------------------------------------------
    private ParameterAnnotationsInfo()
    {
    }

    /** Return the array of annotation table entries. */
    protected AnnotationInfo[] getAnnotationTable()
    {
        return (AnnotationInfo[])this.annotationTable.toArray(new AnnotationInfo[0]);
    }

    /**
     * Check for Utf8 references to constant pool and mark them.
     * 
     * @throws ClassFileException
     */
    protected void markUtf8Refs(ConstantPool pool) throws ClassFileException
    {
        for (Iterator iter = this.annotationTable.iterator(); iter.hasNext();)
        {
            AnnotationInfo a = (AnnotationInfo)iter.next();
            a.markUtf8Refs(pool);
        }
    }

    private void read(DataInput din) throws IOException, ClassFileException
    {
        this.u2numAnnotations = din.readUnsignedShort();
        this.annotationTable = new ArrayList(this.u2numAnnotations);
        for (int i = 0; i < this.u2numAnnotations; i++)
        {
            this.annotationTable.add(AnnotationInfo.create(din));
        }
    }

    /**
     * Export the representation to a DataOutput stream.
     * 
     * @throws IOException
     * @throws ClassFileException
     */
    public void write(DataOutput dout) throws IOException, ClassFileException
    {
        dout.writeShort(this.u2numAnnotations);
        for (Iterator iter = this.annotationTable.iterator(); iter.hasNext();)
        {
            AnnotationInfo a = (AnnotationInfo)iter.next();
            a.write(dout);
        }
    }

    /**
     * Do necessary name remapping.
     * 
     * @throws ClassFileException
     */
    protected void remap(ClassFile cf, NameMapper nm) throws ClassFileException
    {
        for (Iterator iter = this.annotationTable.iterator(); iter.hasNext();)
        {
            AnnotationInfo a = (AnnotationInfo)iter.next();
            a.remap(cf, nm);
        }
    }
}
