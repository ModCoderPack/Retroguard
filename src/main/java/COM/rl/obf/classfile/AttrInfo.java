/* ===========================================================================
 * $RCSfile: AttrInfo.java,v $
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
 * Representation of an attribute. Specific attributes have their representations sub-classed from this.
 * 
 * @author Mark Welsh
 */
public class AttrInfo implements ClassConstants
{
    // Constants -------------------------------------------------------------
    public static final int CONSTANT_FIELD_SIZE = 6;


    // Fields ----------------------------------------------------------------
    private int u2attrNameIndex;
    private int u4attrLength;
    private byte info[];

    protected ClassFile cf;


    // Class Methods ---------------------------------------------------------
    /**
     * Create a new AttrInfo from the data passed.
     * 
     * @param din
     * @param cf
     * @throws IOException
     * @throws ClassFileException
     */
    public static AttrInfo create(DataInput din, ClassFile cf) throws IOException, ClassFileException
    {
        if (din == null)
        {
            throw new IOException("No input stream was provided.");
        }

        // Instantiate based on attribute name
        AttrInfo ai = null;
        int attrNameIndex = din.readUnsignedShort();
        int attrLength = din.readInt();
        String attrName = cf.getUtf8(attrNameIndex);
        if (attrName.equals(ClassConstants.ATTR_Code))
        {
            ai = new CodeAttrInfo(cf, attrNameIndex, attrLength);
        }
        else if (attrName.equals(ClassConstants.ATTR_ConstantValue))
        {
            ai = new ConstantValueAttrInfo(cf, attrNameIndex, attrLength);
        }
        else if (attrName.equals(ClassConstants.ATTR_Exceptions))
        {
            ai = new ExceptionsAttrInfo(cf, attrNameIndex, attrLength);
        }
        else if (attrName.equals(ClassConstants.ATTR_LineNumberTable))
        {
            ai = new LineNumberTableAttrInfo(cf, attrNameIndex, attrLength);
        }
        else if (attrName.equals(ClassConstants.ATTR_SourceFile))
        {
            ai = new SourceFileAttrInfo(cf, attrNameIndex, attrLength);
        }
        else if (attrName.equals(ClassConstants.ATTR_LocalVariableTable))
        {
            ai = new LocalVariableTableAttrInfo(cf, attrNameIndex, attrLength);
        }
        else if (attrName.equals(ClassConstants.ATTR_InnerClasses))
        {
            ai = new InnerClassesAttrInfo(cf, attrNameIndex, attrLength);
        }
        else if (attrName.equals(ClassConstants.ATTR_Synthetic))
        {
            ai = new SyntheticAttrInfo(cf, attrNameIndex, attrLength);
        }
        else if (attrName.equals(ClassConstants.ATTR_Deprecated))
        {
            ai = new DeprecatedAttrInfo(cf, attrNameIndex, attrLength);
        }
        else if (attrName.equals(ClassConstants.ATTR_Signature))
        {
            ai = new SignatureAttrInfo(cf, attrNameIndex, attrLength);
        }
        else if (attrName.equals(ClassConstants.ATTR_LocalVariableTypeTable))
        {
            ai = new LocalVariableTypeTableAttrInfo(cf, attrNameIndex, attrLength);
        }
        else if (attrName.equals(ClassConstants.ATTR_RuntimeVisibleAnnotations))
        {
            ai = new RuntimeVisibleAnnotationsAttrInfo(cf, attrNameIndex, attrLength);
        }
        else if (attrName.equals(ClassConstants.ATTR_RuntimeInvisibleAnnotations))
        {
            ai = new RuntimeInvisibleAnnotationsAttrInfo(cf, attrNameIndex, attrLength);
        }
        else if (attrName.equals(ClassConstants.ATTR_RuntimeVisibleParameterAnnotations))
        {
            ai = new RuntimeVisibleParameterAnnotationsAttrInfo(cf, attrNameIndex, attrLength);
        }
        else if (attrName.equals(ClassConstants.ATTR_RuntimeInvisibleParameterAnnotations))
        {
            ai = new RuntimeInvisibleParameterAnnotationsAttrInfo(cf, attrNameIndex, attrLength);
        }
        else if (attrName.equals(ClassConstants.ATTR_AnnotationDefault))
        {
            ai = new AnnotationDefaultAttrInfo(cf, attrNameIndex, attrLength);
        }
        else if (attrName.equals(ClassConstants.ATTR_EnclosingMethod))
        {
            ai = new EnclosingMethodAttrInfo(cf, attrNameIndex, attrLength);
        }
        else if (attrName.equals(ClassConstants.ATTR_StackMapTable))
        {
            ai = new StackMapTableAttrInfo(cf, attrNameIndex, attrLength);
        }
        else
        {
            ai = new AttrInfo(cf, attrNameIndex, attrLength);
        }
        ai.readInfo(din);
        return ai;
    }


    // Instance Methods ------------------------------------------------------
    /**
     * Constructor
     * 
     * @param cf
     * @param attrNameIndex
     * @param attrLength
     */
    protected AttrInfo(ClassFile cf, int attrNameIndex, int attrLength)
    {
        this.cf = cf;
        this.u2attrNameIndex = attrNameIndex;
        this.u4attrLength = attrLength;
    }

    /**
     * Return the length in bytes of the attribute; over-ride this in sub-classes.
     */
    protected int getAttrInfoLength()
    {
        return this.u4attrLength;
    }

    /**
     * Return the String name of the attribute; over-ride this in sub-classes.
     */
    @SuppressWarnings("static-method")
    protected String getAttrName()
    {
        return ClassConstants.ATTR_Unknown;
    }

    /**
     * Trim attributes from the classfile except those in the <tt>List</tt>.
     * 
     * @param keepAttrs
     */
    protected void trimAttrsExcept(List<String> keepAttrs)
    {
        // do nothing
    }

    /**
     * Check for Utf8 references to constant pool and mark them.
     * 
     * @param pool
     * @throws ClassFileException
     */
    protected void markUtf8Refs(ConstantPool pool) throws ClassFileException
    {
        pool.incRefCount(this.u2attrNameIndex);
        this.markUtf8RefsInInfo(pool);
    }

    /**
     * Check for Utf8 references in the 'info' data to the constant pool and mark them; over-ride this in sub-classes.
     * 
     * @param pool
     * @throws ClassFileException
     */
    protected void markUtf8RefsInInfo(ConstantPool pool) throws ClassFileException
    {
        // do nothing
    }

    /**
     * Read the data following the header; over-ride this in sub-classes.
     * 
     * @param din
     * @throws IOException
     * @throws ClassFileException
     */
    protected void readInfo(DataInput din) throws IOException, ClassFileException
    {
        this.info = new byte[this.u4attrLength];
        din.readFully(this.info);
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
        if (dout == null)
        {
            throw new IOException("No output stream was provided.");
        }
        dout.writeShort(this.u2attrNameIndex);
        dout.writeInt(this.getAttrInfoLength());
        this.writeInfo(dout);
    }

    /**
     * Export data following the header to a DataOutput stream; over-ride this in sub-classes.
     * 
     * @param dout
     * @throws IOException
     * @throws ClassFileException
     */
    public void writeInfo(DataOutput dout) throws IOException, ClassFileException
    {
        dout.write(this.info);
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
        // do nothing
    }

    /**
     * Provide debugging dump of this object.
     * 
     * @param ps
     */
    public void dump(PrintStream ps)
    {
        ps.println("u2attrNameIndex : " + this.u2attrNameIndex + " " + this.cf.getUtf8Debug(this.u2attrNameIndex));
        ps.println("u4attrLength : " + this.u4attrLength);
    }
}
