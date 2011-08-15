/* ===========================================================================
 * $RCSfile: AnnotationsAttrInfo.java,v $
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
 * Representation of an attribute.
 * 
 * @author Mark Welsh
 */
abstract public class AnnotationsAttrInfo extends AttrInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u2numAnnotations;
    private List<AnnotationInfo> annotationTable;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /**
     * Constructor
     * 
     * @param cf
     * @param attrNameIndex
     * @param attrLength
     */
    protected AnnotationsAttrInfo(ClassFile cf, int attrNameIndex, int attrLength)
    {
        super(cf, attrNameIndex, attrLength);
    }

    /**
     * Return the array of annotation table entries.
     */
    protected AnnotationInfo[] getAnnotationTable()
    {
        return this.annotationTable.toArray(new AnnotationInfo[0]);
    }

    /**
     * Check for Utf8 references in the 'info' data to the constant pool and mark them.
     * 
     * @throws ClassFileException
     */
    @Override
    protected void markUtf8RefsInInfo(ConstantPool pool) throws ClassFileException
    {
        for (Iterator<AnnotationInfo> iter = this.annotationTable.iterator(); iter.hasNext();)
        {
            AnnotationInfo ai = iter.next();
            ai.markUtf8Refs(pool);
        }
    }

    /**
     * Read the data following the header.
     * 
     * @throws IOException
     * @throws ClassFileException
     */
    @Override
    protected void readInfo(DataInput din) throws IOException, ClassFileException
    {
        this.u2numAnnotations = din.readUnsignedShort();
        this.annotationTable = new ArrayList<AnnotationInfo>(this.u2numAnnotations);
        for (int i = 0; i < this.u2numAnnotations; i++)
        {
            this.annotationTable.add(AnnotationInfo.create(din));
        }
    }

    /**
     * Export data following the header to a DataOutput stream.
     * 
     * @throws IOException
     * @throws ClassFileException
     */
    @Override
    public void writeInfo(DataOutput dout) throws IOException, ClassFileException
    {
        dout.writeShort(this.u2numAnnotations);
        for (Iterator<AnnotationInfo> iter = this.annotationTable.iterator(); iter.hasNext();)
        {
            AnnotationInfo ai = iter.next();
            ai.write(dout);
        }
    }

    /**
     * Do necessary name remapping.
     * 
     * @throws ClassFileException
     */
    @Override
    protected void remap(ClassFile cf, NameMapper nm) throws ClassFileException
    {
        for (Iterator<AnnotationInfo> iter = this.annotationTable.iterator(); iter.hasNext();)
        {
            AnnotationInfo ai = iter.next();
            ai.remap(cf, nm);
        }
    }

    /**
     * Provide debugging dump of this object.
     */
    @Override
    public void dump(PrintStream ps)
    {
        super.dump(ps);
        ps.println("u2numAnnotations : " + this.u2numAnnotations);
        for (Iterator<AnnotationInfo> iter = this.annotationTable.iterator(); iter.hasNext();)
        {
            AnnotationInfo ai = iter.next();
            ai.dump(ps, this.cf);
        }
    }
}
