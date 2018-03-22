/* ===========================================================================
 * $RCSfile: SignatureAttrInfo.java,v $
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
 * Representation of an attribute.
 *
 * @author Mark Welsh
 */
public class SignatureAttrInfo extends AttrInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u2signatureIndex;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /**
     * Constructor
     *
     * @param cf
     * @param attrNameIndex
     * @param attrLength
     */
    protected SignatureAttrInfo(ClassFile cf, int attrNameIndex, int attrLength)
    {
        super(cf, attrNameIndex, attrLength);
    }

    /**
     * Return the String name of the attribute; over-ride this in sub-classes.
     */
    @Override
    protected String getAttrName()
    {
        return ClassConstants.ATTR_Signature;
    }

    /**
     * Check for Utf8 references in the 'info' data to the constant pool and mark them.
     *
     * @throws ClassFileException
     */
    @Override
    protected void markUtf8RefsInInfo(ConstantPool pool) throws ClassFileException
    {
        pool.incRefCount(this.u2signatureIndex);
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
        this.u2signatureIndex = din.readUnsignedShort();
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
        dout.writeShort(this.u2signatureIndex);
    }

    /**
     * Do necessary name remapping.
     *
     * @throws ClassFileException
     */
    @Override
    protected void remap(ClassFile cf, NameMapper nm) throws ClassFileException
    {
        String oldDesc = cf.getUtf8(this.u2signatureIndex);
        String newDesc;
        switch (this.source)
        {
            case CLASS:
                newDesc = nm.mapSignatureClass(oldDesc);
                break;

            case METHOD:
                newDesc = nm.mapSignatureMethod(oldDesc);
                break;

            case FIELD:
                newDesc = nm.mapSignatureField(oldDesc);
                break;

            default:
                throw new ClassFileException("Invalid attribute source");
        }

        this.u2signatureIndex = cf.remapUtf8To(newDesc, this.u2signatureIndex);
    }
}
