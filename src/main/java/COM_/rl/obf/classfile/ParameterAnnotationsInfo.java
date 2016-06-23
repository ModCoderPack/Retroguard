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

package COM_.rl.obf.classfile;

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
     * Check for Utf8 references to constant pool and mark them.
     * 
     * @param pool
     * @throws ClassFileException
     */
    protected void markUtf8Refs(ConstantPool pool) throws ClassFileException
    {
        for (AnnotationInfo a : this.annotationTable)
        {
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
        int u2numAnnotations = din.readUnsignedShort();
        this.annotationTable = new ArrayList<AnnotationInfo>(u2numAnnotations);
        for (int i = 0; i < u2numAnnotations; i++)
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
        dout.writeShort(this.annotationTable.size());
        for (AnnotationInfo a : this.annotationTable)
        {
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
        for (AnnotationInfo a : this.annotationTable)
        {
            a.remap(cf, nm);
        }
    }
}
