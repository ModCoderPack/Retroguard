/**
 * 
 */

package com.rl.obf.classfile;

import java.io.*;
import java.util.*;

/**
 * Representation of an attribute.
 */
public class BootstrapMethodsAttrInfo extends AttrInfo
{
    // Constants -------------------------------------------------------------

    // Fields ----------------------------------------------------------------
    private List<BootStrapMethod> bsmList;

    // Class Methods ---------------------------------------------------------

    // Instance Methods ------------------------------------------------------
    /**
     * Constructor
     * 
     * @param cf
     * @param attrNameIndex
     * @param attrLength
     */
    protected BootstrapMethodsAttrInfo(ClassFile cf, int attrNameIndex, int attrLength)
    {
        super(cf, attrNameIndex, attrLength);
    }

    /**
     * Return the String name of the attribute.
     */
    @Override
    protected String getAttrName()
    {
        return ClassConstants.ATTR_BootstrapMethods;
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
        this.bsmList = this.parseBSM();
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
        return (short) (x1 * 256 + x2);
    }

    /**
     * Parse BootStrapMethods
     * 
     * @return parsed data
     */
    private List<BootStrapMethod> parseBSM()
    {
        short length = this.readAsUnsignedShort(this.info, 0);
        List<BootStrapMethod> ret = new ArrayList<>();
        int i = 2;
        for (int bsmCount = 0; bsmCount < length; bsmCount++)
        {
            BootStrapMethod bsm = new BootStrapMethod();
            short factory = this.readAsUnsignedShort(this.info, i);
            bsm.setFactory(factory);
            i += 2;
            short argCount = this.readAsUnsignedShort(this.info, i);
            i += 2;
            for (int j = 0; j < argCount; j++)
            {
                short argId = this.readAsUnsignedShort(this.info, i);
                i += 2;
                bsm.addArgument(argId);
            }
            ret.add(bsm);
        }
        return ret;
    }
    

    /**
     * The getter of BootStrapMethods
     * 
     * @return List of BSMs
     */
    public List<BootStrapMethod> getBSM()
    {
        return Collections.unmodifiableList(this.bsmList);
    }
}
