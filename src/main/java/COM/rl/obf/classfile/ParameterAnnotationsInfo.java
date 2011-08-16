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
    private List<AnnotationInfo> annotationTable;


    // Class Methods ---------------------------------------------------------
    /**
     * @param din
     * @throws IOException
     * @throws ClassFileException
     */
    public static ParameterAnnotationsInfo create(DataInput din) throws IOException, ClassFileException
    {
        ParameterAnnotationsInfo pai = new ParameterAnnotationsInfo();
        pai.read(din);
        return pai;
    }


    // Instance Methods ------------------------------------------------------
    /**
     * Private constructor
     */
    private ParameterAnnotationsInfo()
    {
    }

    /**
     * Return the list of annotation table entries.
     */
    protected List<AnnotationInfo> getAnnotationTable()
    {
        return this.annotationTable;
    }

    /**
     * Check for Utf8 references to constant pool and mark them.
     * 
     * @param pool
     * @throws ClassFileException
     */
    protected void markUtf8Refs(ConstantPool pool) throws ClassFileException
    {
        for (Iterator<AnnotationInfo> iter = this.annotationTable.iterator(); iter.hasNext();)
        {
            AnnotationInfo a = iter.next();
            a.markUtf8Refs(pool);
        }
    }

    /**
     * @param din
     * @throws IOException
     * @throws ClassFileException
     */
    private void read(DataInput din) throws IOException, ClassFileException
    {
        this.u2numAnnotations = din.readUnsignedShort();
        this.annotationTable = new ArrayList<AnnotationInfo>(this.u2numAnnotations);
        for (int i = 0; i < this.u2numAnnotations; i++)
        {
            this.annotationTable.add(AnnotationInfo.create(din));
        }
    }

    /**
     * Export the representation to a DataOutput stream.
     * 
     * @param dout
     * @throws IOException
     * @throws ClassFileException
     */
    public void write(DataOutput dout) throws IOException, ClassFileException
    {
        dout.writeShort(this.u2numAnnotations);
        for (Iterator<AnnotationInfo> iter = this.annotationTable.iterator(); iter.hasNext();)
        {
            AnnotationInfo a = iter.next();
            a.write(dout);
        }
    }

    /**
     * Do necessary name remapping.
     * 
     * @param cf
     * @param nm
     * @throws ClassFileException
     */
    protected void remap(ClassFile cf, NameMapper nm) throws ClassFileException
    {
        for (Iterator<AnnotationInfo> iter = this.annotationTable.iterator(); iter.hasNext();)
        {
            AnnotationInfo a = iter.next();
            a.remap(cf, nm);
        }
    }
}
