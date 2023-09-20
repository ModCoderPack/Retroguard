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
        int length = din.readUnsignedShort();
        this.bsmList = new ArrayList<BootStrapMethod>(length);
        for (int bsmCount = 0; bsmCount < length; bsmCount++)
        {
            BootStrapMethod bsm = new BootStrapMethod();
            int factory = din.readUnsignedShort();
            bsm.setFactory(factory);
            int argCount = din.readUnsignedShort();
            for (int j = 0; j < argCount; j++)
            {
                bsm.addArgument(din.readUnsignedShort());
            }
            this.bsmList.add(bsm);
        }
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
        dout.writeShort(this.bsmList.size());
        for (BootStrapMethod bsm : this.bsmList)
        {
            dout.writeShort(bsm.getFactory());
            List<Integer> args = bsm.getArguments();
            dout.writeShort(args.size());
            for (int arg : args)
            {
                dout.writeShort(arg);
            }
        }
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
