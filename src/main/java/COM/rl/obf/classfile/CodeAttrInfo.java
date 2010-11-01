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
 * @author      Mark Welsh
 */
public class CodeAttrInfo extends AttrInfo
{
    // Constants -------------------------------------------------------------
    public static final int CONSTANT_FIELD_SIZE = 12;


    // Fields ----------------------------------------------------------------
    private int u2maxStack;
    private int u2maxLocals;
    private int u4codeLength;
    private byte[] code;
    private int u2exceptionTableLength;
    private ExceptionInfo[] exceptionTable;
    protected int u2attributesCount;
    protected AttrInfo[] attributes;


    // Class Methods ---------------------------------------------------------
    // Number of bytes following an opcode
    private static int opcodeBytes(int opcode)
    {
        switch (opcode)
        {
        case 0xAA: case 0xAB: case 0xC4:
            return -1; // variable length opcode
        case 0x10: case 0x12: case 0x15: case 0x16:
        case 0x17: case 0x18: case 0x19: case 0x36:
        case 0x37: case 0x38: case 0x39: case 0x3A:
        case 0xBC:
            return 1;
        case 0x11: case 0x13: case 0x14: case 0x84:
        case 0x99: case 0x9A: case 0x9B: case 0x9C:
        case 0x9D: case 0x9E: case 0x9F: case 0xA0:
        case 0xA1: case 0xA2: case 0xA3: case 0xA4:
        case 0xA5: case 0xA6: case 0xA7: case 0xA8:
        case 0xB2: case 0xB3: case 0xB4: case 0xB5:
        case 0xB6: case 0xB7: case 0xB8: case 0xBB:
        case 0xBD: case 0xC0: case 0xC1: case 0xC6:
        case 0xC7:
            return 2;
        case 0xC5:
            return 3;
        case 0xB9: case 0xC8: case 0xC9:
            return 4;
        default:
            return 0;
        }
    }


    // Instance Methods ------------------------------------------------------
    protected CodeAttrInfo(ClassFile cf, int attrNameIndex, int attrLength)
    {
        super(cf, attrNameIndex, attrLength);
    }

    /** Return the length in bytes of the attribute. */
    protected int getAttrInfoLength() throws Exception
    {
        int length = CONSTANT_FIELD_SIZE + u4codeLength +
            u2exceptionTableLength * ExceptionInfo.CONSTANT_FIELD_SIZE;
        for (int i = 0; i < u2attributesCount; i++)
        {
            length += AttrInfo.CONSTANT_FIELD_SIZE + attributes[i].getAttrInfoLength();
        }
        return length;
    }

    /** Return the String name of the attribute; over-ride this in sub-classes. */
    protected String getAttrName() throws Exception
    {
        return ATTR_Code;
    }

    /**
     * Trim attributes from the classfile ('Code', 'Exceptions', 'ConstantValue'
     * are preserved, all others except the list in the String[] are killed).
     */
    protected void trimAttrsExcept(String[] keepAttrs) throws Exception
    {
        // Traverse all attributes, removing all except those on 'keep' list
        for (int i = 0; i < attributes.length; i++)
        {
            if (Tools.isInArray(attributes[i].getAttrName(), keepAttrs))
            {
                attributes[i].trimAttrsExcept(keepAttrs);
            }
            else
            {
                attributes[i] = null;
            }
        }

        // Delete the marked attributes
        AttrInfo[] left = new AttrInfo[attributes.length];
        int j = 0;
        for (int i = 0; i < attributes.length; i++)
        {
            if (attributes[i] != null)
            {
                left[j++] = attributes[i];
            }
        }
        attributes = new AttrInfo[j];
        System.arraycopy(left, 0, attributes, 0, j);
        u2attributesCount = j;
    }

    /** Check for references in the 'info' data to the constant pool and mark them. */
    protected void markUtf8RefsInInfo(ConstantPool pool) throws Exception
    {
        for (int i = 0; i < attributes.length; i++)
        {
            attributes[i].markUtf8Refs(pool);
        }
    }

    /** Read the data following the header. */
    protected void readInfo(DataInput din) throws Exception
    {
        u2maxStack = din.readUnsignedShort();
        u2maxLocals = din.readUnsignedShort();
        u4codeLength = din.readInt();
        code = new byte[u4codeLength];
        din.readFully(code);
        u2exceptionTableLength = din.readUnsignedShort();
        exceptionTable = new ExceptionInfo[u2exceptionTableLength];
        for (int i = 0; i < u2exceptionTableLength; i++)
        {
            exceptionTable[i] = ExceptionInfo.create(din);
        }
        u2attributesCount = din.readUnsignedShort();
        attributes = new AttrInfo[u2attributesCount];
        for (int i = 0; i < u2attributesCount; i++)
        {
            attributes[i] = AttrInfo.create(din, cf);
        }
    }

    /** Export data following the header to a DataOutput stream. */
    public void writeInfo(DataOutput dout) throws Exception
    {
        dout.writeShort(u2maxStack);
        dout.writeShort(u2maxLocals);
        dout.writeInt(u4codeLength);
        dout.write(code);
        dout.writeShort(u2exceptionTableLength);
        for (int i = 0; i < u2exceptionTableLength; i++)
        {
            exceptionTable[i].write(dout);
        }
        dout.writeShort(u2attributesCount);
        for (int i = 0; i < u2attributesCount; i++)
        {
            attributes[i].write(dout);
        }
    }

    /** Do necessary name remapping. */
    protected void remap(ClassFile cf, NameMapper nm) throws Exception 
    { 
        for (int i = 0; i < u2attributesCount; i++)
        {
            attributes[i].remap(cf, nm);
        }
    }

    /** Walk the code, finding .class and Class.forName to update. */
    protected FlagHashtable walkFindClassStrings(FlagHashtable cpToFlag) throws Exception 
    {
        return walkClassStrings(cpToFlag, null);
    }

    /** Walk the code, updating .class and Class.forName strings. */
    protected void walkUpdateClassStrings(Hashtable cpUpdate) throws Exception 
    {
        walkClassStrings(null, cpUpdate);
    }

    /** Walk the code, updating .class and Class.forName strings. */
    // Note that class literals MyClass.class are stored directly in the 
    // constant pool in 1.5 (change from 1.4), not referenced by Utf8 name,
    // so .option MapClassString is not necessary for them. 
    // Still needed for Class.forName("MyClass") though. 
    private FlagHashtable walkClassStrings(FlagHashtable cpToFlag, 
                                           Hashtable cpUpdate) throws Exception 
    {
        int opcodePrev = -1;
        int ldcIndex = -1;
        for (int i = 0; i < code.length; i++) 
        {
            int opcode = code[i] & 0xFF;
            if ((opcode == 0x12) && (i+1 < code.length)) // ldc
            {
                ldcIndex = code[i+1] & 0xFF;
                CpInfo ldcCpInfo = cf.getCpEntry(ldcIndex);
                if (!(ldcCpInfo instanceof StringCpInfo))
                {
                    ldcIndex = -1;
                }
            }
            else if ((opcode == 0x13) && (i+2 < code.length)) // ldc_w
            {
                ldcIndex = ((code[i+1] & 0xFF) << 8) + (code[i+2] & 0xFF);
                CpInfo ldcCpInfo = cf.getCpEntry(ldcIndex);
                if (!(ldcCpInfo instanceof StringCpInfo))
                {
                    ldcIndex = -1;
                }
            }
            if ((opcodePrev == 0x12 || opcodePrev == 0x13) // ldc or ldc_w
                && ldcIndex != -1) // and is a StringCpInfo
            {
                boolean isClassForName = false;
                if ((opcode == 0xB8) && (i+2 < code.length)) // invokestatic
                {
                    int invokeIndex = 
                        ((code[i+1] & 0xFF) << 8) + (code[i+2] & 0xFF);
                    CpInfo cpInfo = cf.getCpEntry(invokeIndex);
                    if (cpInfo instanceof MethodrefCpInfo)
                    {
                        MethodrefCpInfo entry = (MethodrefCpInfo)cpInfo;
                        ClassCpInfo classEntry = (ClassCpInfo)cf.getCpEntry(entry.getClassIndex());
                        String className = ((Utf8CpInfo)cf.getCpEntry(classEntry.getNameIndex())).getString();
                        NameAndTypeCpInfo ntEntry = (NameAndTypeCpInfo)cf.getCpEntry(entry.getNameAndTypeIndex());
                        String name = ((Utf8CpInfo)cf.getCpEntry(ntEntry.getNameIndex())).getString();
                        String descriptor = ((Utf8CpInfo)cf.getCpEntry(ntEntry.getDescriptorIndex())).getString();
                        if (("class$".equals(name) && 
                             ("(Ljava/lang/String;)Ljava/lang/Class;".equals(descriptor) ||
                              "(Ljava/lang/String;Z)Ljava/lang/Class;".equals(descriptor))) ||
                            ("java/lang/Class".equals(className) &&
                             "forName".equals(name) &&
                             "(Ljava/lang/String;)Ljava/lang/Class;".equals(descriptor)))
                        {
                            isClassForName = true;
                            if (cpUpdate != null)
                            {
                                // Update StringCpInfo index in ldc to new one
                                Object o = cpUpdate.get(new Integer(ldcIndex));
                                if (o instanceof Integer) 
                                {
                                    int remapStringIndex = ((Integer)o).intValue();
                                    switch (opcodePrev)
                                    {
                                    case 0x13: // ldc_w
                                        code[i-2] = 0;
                                        // fallthru
                                    case 0x12: // ldc
                                        code[i-1] = (byte)remapStringIndex;
                                        break;
                                    default: // error
                                        throw new Exception(".class or Class.forName remap of non-ldc/ldc_w - please report this error");
                                    }
                                }
                            }
                        }
                    }
                }
                if (cpToFlag != null)
                {
                    cpToFlag.updateFlag((StringCpInfo)cf.getCpEntry(ldcIndex), 
                                        ldcIndex, isClassForName); 
                }
            }
            int bytes = getOpcodeBytes(opcode, i);
            i += bytes;
            opcodePrev = opcode;
        }
        return cpToFlag;
    }

    /** Walk the code, adding pool references to Vector. */
    protected void addCpRefs(Vector refs) throws Exception 
    {
        for (int i = 0; i < code.length; i++) 
        {
            int opcode = code[i] & 0xFF;
            int index;
            switch (opcode)
	    {
		// .class reference
	    case 0x12: // ldc
		index = (code[i+1] & 0xFF);
                CpInfo cpInfo = cf.getCpEntry(index);
                if (cpInfo instanceof ClassCpInfo)
                {
                    refs.addElement(cpInfo);
                }
		break;

		// .class reference
	    case 0x13: // ldc_w
		index = ((code[i+1] & 0xFF) << 8) + (code[i+2] & 0xFF);
                cpInfo = cf.getCpEntry(index);
                if (cpInfo instanceof ClassCpInfo)
                {
                    refs.addElement(cpInfo);
                }
		break;

		// class, array, interface type
	    case 0xBB: // new
	    case 0xBD: // anewarray
	    case 0xC0: // checkcast
	    case 0xC1: // instanceof
	    case 0xC5: // multianewarray
		// static field
	    case 0xB2: // getstatic
	    case 0xB3: // putstatic
		// non-static field
	    case 0xB4: // getfield
	    case 0xB5: // putfield
		// static method
	    case 0xB8: // invokestatic
		// non-static method
	    case 0xB6: // invokevirtual
	    case 0xB7: // invokespecial
	    case 0xB9: // invokeinterface
		index = ((code[i+1] & 0xFF) << 8) + (code[i+2] & 0xFF);
		refs.addElement(cf.getCpEntry(index));
		break;

	    default:
		// skip
		break;
	    }
            int bytes = getOpcodeBytes(opcode, i);
            i += bytes;
        }
    }

    // Compute length of opcode arguments at offset
    private int getOpcodeBytes(int opcode, int i) throws Exception
    {
        int bytes = opcodeBytes(opcode);
        if (bytes < 0) // variable length instructions
        {
            switch (opcode)
            {
            case 0xAA: // tableswitch
                bytes = 3 - (i % 4); // 0-3 byte pad
                bytes += 4; // default value
                int low = 
                    ((code[i+1+bytes] & 0xFF) << 24) +
                    ((code[i+1+bytes+1] & 0xFF) << 16) +
                    ((code[i+1+bytes+2] & 0xFF) << 8) +
                    (code[i+1+bytes+3] & 0xFF); 
                bytes += 4; // low value
                int high = 
                    ((code[i+1+bytes] & 0xFF) << 24) +
                    ((code[i+1+bytes+1] & 0xFF) << 16) +
                    ((code[i+1+bytes+2] & 0xFF) << 8) +
                    (code[i+1+bytes+3] & 0xFF); 
                bytes += 4; // high value
                if (high >= low)
                {
                    bytes += (high - low + 1) * 4; // jump offsets
                }
                break;
            case 0xAB: // lookupswitch
                bytes = 3 - (i % 4); // 0-3 byte pad
                bytes += 4; // default value
                int npairs = 
                    ((code[i+1+bytes] & 0xFF) << 24) +
                    ((code[i+1+bytes+1] & 0xFF) << 16) +
                    ((code[i+1+bytes+2] & 0xFF) << 8) +
                    (code[i+1+bytes+3] & 0xFF); 
                bytes += 4; // npairs value
                if (npairs >= 0)
                {
                    bytes += npairs * 8; // match / offset pairs
                }
                break;
            case 0xC4: // wide
                int wideOpcode = code[i+1] & 0xFF;
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
                    throw new Exception("Illegal wide opcode");
                }
                break;
            default:
                throw new Exception("Illegal variable length opcode");
            }
        }
        return bytes;
    }

    // Convert int to 2 char hex string
    private static String toHexString(int i)
    {
        String hex = "0" + Integer.toHexString(i);
        return hex.substring(hex.length() - 2);
    }
}


/*
    private static final String OPCODE_UNUSED = "<unused>";
    private static final String OPCODE_RESERVED = "<reserved>";
    private static final String[] opcodeName = 
    {
        "nop", // 0x00
        "aconst_null", // 0x01
        "iconst_m1", // 0x02
        "iconst_0", // 0x03
        "iconst_1", // 0x04
        "iconst_2", // 0x05
        "iconst_3", // 0x06
        "iconst_4", // 0x07
        "iconst_5", // 0x08
        "lconst_0", // 0x09
        "lconst_1", // 0x0A
        "fconst_0", // 0x0B
        "fconst_1", // 0x0C
        "fconst_2", // 0x0D
        "dconst_0", // 0x0E
        "dconst_1", // 0x0F
        "bipush", // 0x10
        "sipush", // 0x11
        "ldc", // 0x12
        "ldc_w", // 0x13
        "ldc2_w", // 0x14
        "iload", // 0x15
        "lload", // 0x16
        "fload", // 0x17
        "dload", // 0x18
        "aload", // 0x19
        "iload_0", // 0x1A
        "iload_1", // 0x1B
        "iload_2", // 0x1C
        "iload_3", // 0x1D
        "lload_0", // 0x1E
        "lload_1", // 0x1F
        "lload_2", // 0x20
        "lload_3", // 0x21
        "fload_0", // 0x22
        "fload_1", // 0x23
        "fload_2", // 0x24
        "fload_3", // 0x25
        "dload_0", // 0x26
        "dload_1", // 0x27
        "dload_2", // 0x28
        "dload_3", // 0x29
        "aload_0", // 0x2A
        "aload_1", // 0x2B
        "aload_2", // 0x2C
        "aload_3", // 0x2D
        "iaload", // 0x2E
        "laload", // 0x2F
        "faload", // 0x30
        "daload", // 0x31
        "aaload", // 0x32
        "baload", // 0x33
        "caload", // 0x34
        "saload", // 0x35
        "istore", // 0x36
        "lstore", // 0x37
        "fstore", // 0x38
        "dstore", // 0x39
        "astore", // 0x3A
        "istore_0", // 0x3B
        "istore_1", // 0x3C
        "istore_2", // 0x3D
        "istore_3", // 0x3E
        "lstore_0", // 0x3F
        "lstore_1", // 0x40
        "lstore_2", // 0x41
        "lstore_3", // 0x42
        "fstore_0", // 0x43
        "fstore_1", // 0x44
        "fstore_2", // 0x45
        "fstore_3", // 0x46
        "dstore_0", // 0x47
        "dstore_1", // 0x48
        "dstore_2", // 0x49
        "dstore_3", // 0x4A
        "astore_0", // 0x4B
        "astore_1", // 0x4C
        "astore_2", // 0x4D
        "astore_3", // 0x4E
        "iastore", // 0x4F
        "lastore", // 0x50
        "fastore", // 0x51
        "dastore", // 0x52
        "aastore", // 0x53
        "bastore", // 0x54
        "castore", // 0x55
        "sastore", // 0x56
        "pop", // 0x57
        "pop2", // 0x58
        "dup", // 0x59
        "dup_x1", // 0x5A
        "dup_x2", // 0x5B
        "dup2", // 0x5C
        "dup2_x1", // 0x5D
        "dup2_x2", // 0x5E
        "swap", // 0x5F
        "iadd", // 0x60
        "ladd", // 0x61
        "fadd", // 0x62
        "dadd", // 0x63
        "isub", // 0x64
        "lsub", // 0x65
        "fsub", // 0x66
        "dsub", // 0x67
        "imul", // 0x68
        "lmul", // 0x69
        "fmul", // 0x6A
        "dmul", // 0x6B
        "idiv", // 0x6C
        "ldiv", // 0x6D
        "fdiv", // 0x6E
        "ddiv", // 0x6F
        "irem", // 0x70
        "lrem", // 0x71
        "frem", // 0x72
        "drem", // 0x73
        "ineg", // 0x74
        "lneg", // 0x75
        "fneg", // 0x76
        "dneg", // 0x77
        "ishl", // 0x78
        "lshl", // 0x79
        "ishr", // 0x7A
        "lshr", // 0x7B
        "iushr", // 0x7C
        "lushr", // 0x7D
        "iand", // 0x7E
        "land", // 0x7F
        "ior", // 0x80
        "lor", // 0x81
        "ixor", // 0x82
        "lxor", // 0x83
        "iinc", // 0x84
        "i2l", // 0x85
        "i2f", // 0x86
        "i2d", // 0x87
        "l2i", // 0x88
        "l2f", // 0x89
        "l2d", // 0x8A
        "f2i", // 0x8B
        "f2l", // 0x8C
        "f2d", // 0x8D
        "d2i", // 0x8E
        "d2l", // 0x8F
        "d2f", // 0x90
        "i2b", // 0x91
        "i2c", // 0x92
        "i2s", // 0x93
        "lcmp", // 0x94
        "fcmpl", // 0x95
        "fcmpg", // 0x96
        "dcmpl", // 0x97
        "dcmpg", // 0x98
        "ifeq", // 0x99
        "ifne", // 0x9A
        "iflt", // 0x9B
        "ifge", // 0x9C
        "ifgt", // 0x9D
        "ifle", // 0x9E
        "if_icmpeq", // 0x9F
        "if_icmpne", // 0xA0
        "if_icmplt", // 0xA1
        "if_icmpge", // 0xA2
        "if_icmpgt", // 0xA3
        "if_icmple", // 0xA4
        "if_acmpeq", // 0xA5
        "if_acmpne", // 0xA6
        "goto", // 0xA7
        "jsr", // 0xA8
        "ret", // 0xA9
        "tableswitch", // 0xAA
        "lookupswitch", // 0xAB
        "ireturn", // 0xAC
        "lreturn", // 0xAD
        "freturn", // 0xAE
        "dreturn", // 0xAF
        "areturn", // 0xB0
        "return", // 0xB1
        "getstatic", // 0xB2
        "putstatic", // 0xB3
        "getfield", // 0xB4
        "putfield", // 0xB5
        "invokevirtual", // 0xB6
        "invokespecial", // 0xB7
        "invokestatic", // 0xB8
        "invokeinterface", // 0xB9
        OPCODE_UNUSED, // 0xBA
        "new", // 0xBB
        "newarray", // 0xBC
        "anewarray", // 0xBD
        "arraylength", // 0xBE
        "athrow", // 0xBF
        "checkcast", // 0xC0
        "instanceof", // 0xC1
        "monitorenter", // 0xC2
        "monitorexit", // 0xC3
        "wide", // 0xC4
        "multianewarray", // 0xC5
        "ifnull", // 0xC6
        "ifnonnull", // 0xC7
        "goto_w", // 0xC8
        "jsr_w", // 0xC9
        OPCODE_RESERVED, // 0xCA
        OPCODE_UNUSED, // 0xCB
        OPCODE_UNUSED, // 0xCC
        OPCODE_UNUSED, // 0xCD
        OPCODE_UNUSED, // 0xCE
        OPCODE_UNUSED, // 0xCF
        OPCODE_UNUSED, // 0xD0
        OPCODE_UNUSED, // 0xD1
        OPCODE_UNUSED, // 0xD2
        OPCODE_UNUSED, // 0xD3
        OPCODE_UNUSED, // 0xD4
        OPCODE_UNUSED, // 0xD5
        OPCODE_UNUSED, // 0xD6
        OPCODE_UNUSED, // 0xD7
        OPCODE_UNUSED, // 0xD8
        OPCODE_UNUSED, // 0xD9
        OPCODE_UNUSED, // 0xDA
        OPCODE_UNUSED, // 0xDB
        OPCODE_UNUSED, // 0xDC
        OPCODE_UNUSED, // 0xDD
        OPCODE_UNUSED, // 0xDE
        OPCODE_UNUSED, // 0xDF
        OPCODE_UNUSED, // 0xE0
        OPCODE_UNUSED, // 0xE1
        OPCODE_UNUSED, // 0xE2
        OPCODE_UNUSED, // 0xE3
        OPCODE_UNUSED, // 0xE4
        OPCODE_UNUSED, // 0xE5
        OPCODE_UNUSED, // 0xE6
        OPCODE_UNUSED, // 0xE7
        OPCODE_UNUSED, // 0xE8
        OPCODE_UNUSED, // 0xE9
        OPCODE_UNUSED, // 0xEA
        OPCODE_UNUSED, // 0xEB
        OPCODE_UNUSED, // 0xEC
        OPCODE_UNUSED, // 0xED
        OPCODE_UNUSED, // 0xEE
        OPCODE_UNUSED, // 0xEF
        OPCODE_UNUSED, // 0xF0
        OPCODE_UNUSED, // 0xF1
        OPCODE_UNUSED, // 0xF2
        OPCODE_UNUSED, // 0xF3
        OPCODE_UNUSED, // 0xF4
        OPCODE_UNUSED, // 0xF5
        OPCODE_UNUSED, // 0xF6
        OPCODE_UNUSED, // 0xF7
        OPCODE_UNUSED, // 0xF8
        OPCODE_UNUSED, // 0xF9
        OPCODE_UNUSED, // 0xFA
        OPCODE_UNUSED, // 0xFB
        OPCODE_UNUSED, // 0xFC
        OPCODE_UNUSED, // 0xFD
        OPCODE_RESERVED, // 0xFE
        OPCODE_RESERVED, // 0xFF
    };
*/
