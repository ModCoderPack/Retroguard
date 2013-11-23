/**
 * 
 */

package COM.rl.obf.classfile;

import java.io.*;
import java.util.*;

/**
 * Representation of a 'methodhandle' entry in the ConstantPool.
 */
public class InvokeDynamicCpInfo extends CpInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u2bootstrapMethodAttrIndex;
    private int u2nameAndTypeIndex;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /**
     * Constructor
     */
    public InvokeDynamicCpInfo()
    {
        super(ClassConstants.CONSTANT_InvokeDynamic);
    }

    /**
     * Return the bootstrap-method-attr index.
     */
    protected int getBootstrapMethodAttrIndex()
    {
        return this.u2bootstrapMethodAttrIndex;
    }

    /**
     * Return the name-and-type index.
     */
    protected int getNameAndTypeIndex()
    {
        return this.u2nameAndTypeIndex;
    }

    /**
     * Set the name-and-type index.
     * 
     * @param index
     */
    protected void setNameAndTypeIndex(int index)
    {
        this.u2nameAndTypeIndex = index;
    }

    /**
     * Return the method's string name.
     * 
     * @param cf
     * @throws ClassFileException
     */
    public String getName(ClassFile cf) throws ClassFileException
    {
        NameAndTypeCpInfo ntCpInfo = (NameAndTypeCpInfo)cf.getCpEntry(this.u2nameAndTypeIndex);
        return cf.getUtf8(ntCpInfo.getNameIndex());
    }

    /**
     * Return the method's string descriptor.
     * 
     * @param cf
     * @throws ClassFileException
     */
    public String getDescriptor(ClassFile cf) throws ClassFileException
    {
        NameAndTypeCpInfo ntCpInfo = (NameAndTypeCpInfo)cf.getCpEntry(this.u2nameAndTypeIndex);
        return cf.getUtf8(ntCpInfo.getDescriptorIndex());
    }

    /**
     * Check for N+T references to constant pool and mark them.
     * 
     * @throws ClassFileException
     */
    @Override
    protected void markNTRefs(ConstantPool pool) throws ClassFileException
    {
        pool.incRefCount(this.u2nameAndTypeIndex);
    }

    /**
     * Read the 'info' data following the u1tag byte.
     * 
     * @throws IOException
     * @throws ClassFileException
     */
    @Override
    protected void readInfo(DataInput din) throws IOException, ClassFileException
    {
        this.u2bootstrapMethodAttrIndex = din.readUnsignedShort();
        this.u2nameAndTypeIndex = din.readUnsignedShort();
    }

    /**
     * Write the 'info' data following the u1tag byte.
     * 
     * @throws IOException
     * @throws ClassFileException
     */
    @Override
    protected void writeInfo(DataOutput dout) throws IOException, ClassFileException
    {
        dout.writeShort(this.u2bootstrapMethodAttrIndex);
        dout.writeShort(this.u2nameAndTypeIndex);
    }
}
