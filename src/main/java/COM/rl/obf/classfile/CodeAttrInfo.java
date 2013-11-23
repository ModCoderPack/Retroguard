/* ===========================================================================
 * $RCSfile: CodeAttrInfo.java,v $
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

import COM.rl.util.*;

/**
 * Representation of an attribute.
 * 
 * @author Mark Welsh
 */
public class CodeAttrInfo extends AttrInfo
{
    // Constants -------------------------------------------------------------
    @SuppressWarnings("hiding")
    public static final int CONSTANT_FIELD_SIZE = 12;


    // Fields ----------------------------------------------------------------
    private int u2maxStack;
    private int u2maxLocals;
    private int u4codeLength;
    private byte[] code;
    private List<ExceptionInfo> exceptionTable;
    protected List<AttrInfo> attributes;


    // Class Methods ---------------------------------------------------------
    /**
     * Number of bytes following an opcode
     * 
     * @param opcode
     */
    private static int opcodeBytes(int opcode)
    {
        switch (opcode)
        {
            case 0xAA:
            case 0xAB:
            case 0xC4:
                return -1; // variable length opcode
            case 0x10:
            case 0x12:
            case 0x15:
            case 0x16:
            case 0x17:
            case 0x18:
            case 0x19:
            case 0x36:
            case 0x37:
            case 0x38:
            case 0x39:
            case 0x3A:
            case 0xBC:
                return 1;
            case 0x11:
            case 0x13:
            case 0x14:
            case 0x84:
            case 0x99:
            case 0x9A:
            case 0x9B:
            case 0x9C:
            case 0x9D:
            case 0x9E:
            case 0x9F:
            case 0xA0:
            case 0xA1:
            case 0xA2:
            case 0xA3:
            case 0xA4:
            case 0xA5:
            case 0xA6:
            case 0xA7:
            case 0xA8:
            case 0xB2:
            case 0xB3:
            case 0xB4:
            case 0xB5:
            case 0xB6:
            case 0xB7:
            case 0xB8:
            case 0xBB:
            case 0xBD:
            case 0xC0:
            case 0xC1:
            case 0xC6:
            case 0xC7:
                return 2;
            case 0xC5:
                return 3;
            case 0xB9:
            case 0xBA:
            case 0xC8:
            case 0xC9:
                return 4;
            default:
                return 0;
        }
    }


    // Instance Methods ------------------------------------------------------
    /**
     * Constructor
     * 
     * @param cf
     * @param attrNameIndex
     * @param attrLength
     */
    protected CodeAttrInfo(ClassFile cf, int attrNameIndex, int attrLength)
    {
        super(cf, attrNameIndex, attrLength);
    }

    /**
     * Return the length in bytes of the attribute.
     */
    @Override
    protected int getAttrInfoLength()
    {
        int length = CodeAttrInfo.CONSTANT_FIELD_SIZE + this.u4codeLength
            + (this.exceptionTable.size() * ExceptionInfo.CONSTANT_FIELD_SIZE);
        for (AttrInfo at : this.attributes)
        {
            length += AttrInfo.CONSTANT_FIELD_SIZE + at.getAttrInfoLength();
        }
        return length;
    }

    /**
     * Return the String name of the attribute; over-ride this in sub-classes.
     */
    @Override
    protected String getAttrName()
    {
        return ClassConstants.ATTR_Code;
    }

    /**
     * Trim attributes from the classfile ('Code', 'Exceptions', 'ConstantValue' are preserved, all others except those in the
     * {@code List<String>} are killed).
     */
    @Override
    protected void trimAttrsExcept(List<String> keepAttrs)
    {
        // Traverse all attributes, removing all except those on 'keep' list
        List<AttrInfo> delAttrs = new ArrayList<AttrInfo>();
        for (AttrInfo at : this.attributes)
        {
            if (keepAttrs.contains(at.getAttrName()))
            {
                at.trimAttrsExcept(keepAttrs);
            }
            else
            {
                delAttrs.add(at);
            }
        }

        this.attributes.removeAll(delAttrs);
    }

    /**
     * Check for references in the 'info' data to the constant pool and mark them.
     * 
     * @throws ClassFileException
     */
    @Override
    protected void markUtf8RefsInInfo(ConstantPool pool) throws ClassFileException
    {
        for (AttrInfo at : this.attributes)
        {
            at.markUtf8Refs(pool);
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
        this.u2maxStack = din.readUnsignedShort();
        this.u2maxLocals = din.readUnsignedShort();
        this.u4codeLength = din.readInt();
        this.code = new byte[this.u4codeLength];
        din.readFully(this.code);
        int u2exceptionTableLength = din.readUnsignedShort();
        this.exceptionTable = new ArrayList<ExceptionInfo>(u2exceptionTableLength);
        for (int i = 0; i < u2exceptionTableLength; i++)
        {
            this.exceptionTable.add(ExceptionInfo.create(din));
        }
        int u2attributesCount = din.readUnsignedShort();
        this.attributes = new ArrayList<AttrInfo>(u2attributesCount);
        for (int i = 0; i < u2attributesCount; i++)
        {
            this.attributes.add(AttrInfo.create(din, this.cf, AttrSource.CODE));
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
        dout.writeShort(this.u2maxStack);
        dout.writeShort(this.u2maxLocals);
        dout.writeInt(this.u4codeLength);
        dout.write(this.code);
        dout.writeShort(this.exceptionTable.size());
        for (ExceptionInfo ex : this.exceptionTable)
        {
            ex.write(dout);
        }
        dout.writeShort(this.attributes.size());
        for (AttrInfo at : this.attributes)
        {
            at.write(dout);
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
        for (AttrInfo at : this.attributes)
        {
            at.remap(cf, nm);
        }
    }

    /**
     * Walk the code, finding .class and Class.forName to update.
     * 
     * @param cpToFlag
     * @throws ClassFileException
     */
    protected FlagHashtable walkFindClassStrings(FlagHashtable cpToFlag) throws ClassFileException
    {
        return this.walkClassStrings(cpToFlag, Collections.<Integer, Integer>emptyMap());
    }

    /**
     * Walk the code, updating .class and Class.forName strings.
     * 
     * @param cpUpdate
     * @throws ClassFileException
     */
    protected void walkUpdateClassStrings(Map<Integer, ?> cpUpdate) throws ClassFileException
    {
        this.walkClassStrings(null, cpUpdate);
    }

    /**
     * Walk the code, updating .class and Class.forName strings.
     * Note that class literals MyClass.class are stored directly in the constant pool in 1.5 (change from 1.4), not referenced
     * by Utf8 name, so .option MapClassString is not necessary for them.
     * Still needed for Class.forName("MyClass") though.
     * 
     * @param cpToFlag
     * @param cpUpdate
     * @throws ClassFileException
     */
    private FlagHashtable walkClassStrings(FlagHashtable cpToFlag, Map<Integer, ?> cpUpdate) throws ClassFileException
    {
        int opcodePrev = -1;
        int ldcIndex = -1;
        for (int i = 0; i < this.code.length; i++)
        {
            int opcode = this.code[i] & 0xFF;
            if ((opcode == 0x12) && ((i + 1) < this.code.length)) // ldc
            {
                ldcIndex = this.code[i + 1] & 0xFF;
                CpInfo ldcCpInfo = this.cf.getCpEntry(ldcIndex);
                if (!(ldcCpInfo instanceof StringCpInfo))
                {
                    ldcIndex = -1;
                }
            }
            else if ((opcode == 0x13) && ((i + 2) < this.code.length)) // ldc_w
            {
                ldcIndex = ((this.code[i + 1] & 0xFF) << 8) + (this.code[i + 2] & 0xFF);
                CpInfo ldcCpInfo = this.cf.getCpEntry(ldcIndex);
                if (!(ldcCpInfo instanceof StringCpInfo))
                {
                    ldcIndex = -1;
                }
            }
            if (((opcodePrev == 0x12) || (opcodePrev == 0x13)) && (ldcIndex != -1)) // ldc or ldc_w and is a StringCpInfo
            {
                boolean isClassForName = false;
                if ((opcode == 0xB8) && ((i + 2) < this.code.length)) // invokestatic
                {
                    int invokeIndex = ((this.code[i + 1] & 0xFF) << 8) + (this.code[i + 2] & 0xFF);
                    CpInfo cpInfo = this.cf.getCpEntry(invokeIndex);
                    if (cpInfo instanceof MethodrefCpInfo)
                    {
                        MethodrefCpInfo entry = (MethodrefCpInfo)cpInfo;
                        ClassCpInfo classEntry = (ClassCpInfo)this.cf.getCpEntry(entry.getClassIndex());
                        String className = this.cf.getUtf8(classEntry.getNameIndex());
                        NameAndTypeCpInfo ntEntry = (NameAndTypeCpInfo)this.cf.getCpEntry(entry.getNameAndTypeIndex());
                        String name = this.cf.getUtf8(ntEntry.getNameIndex());
                        String descriptor = this.cf.getUtf8(ntEntry.getDescriptorIndex());
                        if (("class$".equals(name) && ("(Ljava/lang/String;)Ljava/lang/Class;".equals(descriptor)
                            || "(Ljava/lang/String;Z)Ljava/lang/Class;".equals(descriptor)))
                            || ("java/lang/Class".equals(className) && "forName".equals(name)
                            && "(Ljava/lang/String;)Ljava/lang/Class;".equals(descriptor)))
                        {
                            isClassForName = true;
                            // Update StringCpInfo index in ldc to new one
                            Object o = cpUpdate.get(new Integer(ldcIndex));
                            if (o instanceof Integer)
                            {
                                Integer oi = (Integer)o;
                                int remapStringIndex = oi.intValue();
                                switch (opcodePrev)
                                {
                                    case 0x13: // ldc_w
                                        this.code[i - 2] = 0;
                                        //$FALL-THROUGH$
                                    case 0x12: // ldc
                                        this.code[i - 1] = (byte)remapStringIndex;
                                        break;
                                    default: // error
                                        throw new RuntimeException("Internal error: "
                                            + ".class or Class.forName remap of non-ldc/ldc_w");
                                }
                            }
                        }
                    }
                }
                if (cpToFlag != null)
                {
                    cpToFlag.updateFlag(this.cf.getCpEntry(ldcIndex), ldcIndex, isClassForName);
                }
            }
            int bytes = this.getOpcodeBytes(opcode, i);
            i += bytes;
            opcodePrev = opcode;
        }
        return cpToFlag;
    }

    /**
     * Compute length of opcode arguments at offset
     * 
     * @param opcode
     * @param i
     * @throws ClassFileException
     */
    private int getOpcodeBytes(int opcode, int i) throws ClassFileException
    {
        int bytes = CodeAttrInfo.opcodeBytes(opcode);
        if (bytes < 0) // variable length instructions
        {
            switch (opcode)
            {
                case 0xAA: // tableswitch
                    bytes = 3 - (i % 4); // 0-3 byte pad
                    bytes += 4; // default value
                    int low = ((this.code[i + 1 + bytes] & 0xFF) << 24) + ((this.code[i + 1 + bytes + 1] & 0xFF) << 16)
                        + ((this.code[i + 1 + bytes + 2] & 0xFF) << 8) + (this.code[i + 1 + bytes + 3] & 0xFF);
                    bytes += 4; // low value
                    int high = ((this.code[i + 1 + bytes] & 0xFF) << 24) + ((this.code[i + 1 + bytes + 1] & 0xFF) << 16)
                        + ((this.code[i + 1 + bytes + 2] & 0xFF) << 8) + (this.code[i + 1 + bytes + 3] & 0xFF);
                    bytes += 4; // high value
                    if (high >= low)
                    {
                        bytes += ((high - low) + 1) * 4; // jump offsets
                    }
                    break;
                case 0xAB: // lookupswitch
                    bytes = 3 - (i % 4); // 0-3 byte pad
                    bytes += 4; // default value
                    int npairs = ((this.code[i + 1 + bytes] & 0xFF) << 24) + ((this.code[i + 1 + bytes + 1] & 0xFF) << 16)
                        + ((this.code[i + 1 + bytes + 2] & 0xFF) << 8) + (this.code[i + 1 + bytes + 3] & 0xFF);
                    bytes += 4; // npairs value
                    if (npairs >= 0)
                    {
                        bytes += npairs * 8; // match / offset pairs
                    }
                    break;
                case 0xC4: // wide
                    int wideOpcode = this.code[i + 1] & 0xFF;
                    switch (wideOpcode)
                    {
                        case 0x15: // iload
                        case 0x16: // lload
                        case 0x17: // fload
                        case 0x18: // dload
                        case 0x19: // aload
                        case 0x36: // istore
                        case 0x37: // lstore
                        case 0x38: // fstore
                        case 0x39: // dstore
                        case 0x3A: // astore
                        case 0xA9: // ret
                            bytes = 3;
                            break;
                        case 0x84: // iinc
                            bytes = 5;
                            break;
                        default:
                            throw new ClassFileException("Illegal wide opcode");
                    }
                    break;
                default:
                    throw new ClassFileException("Illegal variable length opcode");
            }
        }
        return bytes;
    }
}
