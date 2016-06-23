/**
 * 
 */

package COM_.rl.obf.classfile;

import java.io.*;
import java.util.*;

/**
 * Representation of a 'methodhandle' entry in the ConstantPool.
 */
public class MethodHandleCpInfo extends CpInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private int u1referenceKind;
    private int u2referenceIndex;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /**
     * Constructor
     */
    public MethodHandleCpInfo()
    {
        super(ClassConstants.CONSTANT_MethodHandle);
    }

    /**
     * Return the type of the reference.
     */
    protected int getReferenceKind()
    {
        return this.u1referenceKind;
    }

    /**
     * Return the reference index.
     */
    protected int getReferenceIndex()
    {
        return this.u2referenceIndex;
    }

    /**
     * Return the method's string name.
     * 
     * @param cf
     * @throws ClassFileException
     */
    public String getName(ClassFile cf) throws ClassFileException
    {
        RefCpInfo refCpInfo = (RefCpInfo)cf.getCpEntry(this.u2referenceIndex);
        return refCpInfo.getName(cf);
    }

    /**
     * Return the method's string descriptor.
     * 
     * @param cf
     * @throws ClassFileException
     */
    public String getDescriptor(ClassFile cf) throws ClassFileException
    {
        RefCpInfo refCpInfo = (RefCpInfo)cf.getCpEntry(this.u2referenceIndex);
        return refCpInfo.getDescriptor(cf);
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
        this.u1referenceKind = din.readUnsignedByte();
        this.u2referenceIndex = din.readUnsignedShort();
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
        dout.writeByte(this.u1referenceKind);
        dout.writeShort(this.u2referenceIndex);
    }
}
