package com.rl.obf.classfile;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NestHostAttrInfo extends AttrInfo
{
    // Constants -------------------------------------------------------------

    // Fields ----------------------------------------------------------------
    private short indexHostClass;

    // Class Methods ---------------------------------------------------------

    // Instance Methods ------------------------------------------------------
    /**
     * Constructor
     * 
     * @param cf
     * @param attrNameIndex
     * @param attrLength
     */
    protected NestHostAttrInfo(ClassFile cf, int attrNameIndex, int attrLength)
    {
        super(cf, attrNameIndex, attrLength);
    }

    /**
     * Return the String name of the attribute.
     */
    @Override
    protected String getAttrName()
    {
        return ClassConstants.ATTR_NestHost;
    }

    /**
     * Read the data following the header; over-ride this in sub-classes.
     * 
     * @param din
     * @throws IOException
     * @throws ClassFileException
     */
    @Override
    protected void readInfo(DataInput din) throws IOException, ClassFileException
    {
        super.readInfo(din);
        this.indexHostClass = this.readAsUnsignedShort(this.info, 0);
    }

    /**
     * Read byte data as an unsigned short value
     * 
     * @param arr byte array
     * @param pos target position in given array
     * @return parsed short value
     */
    private short readAsUnsignedShort(final byte[] arr, final int pos)
    {
        final int x1 = Byte.toUnsignedInt(arr[pos]);
        final int x2 = Byte.toUnsignedInt(arr[pos + 1]);
        return (short)(x1 * 256 + x2);
    }
}
