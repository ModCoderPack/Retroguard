/* ===========================================================================
 * $RCSfile: ClassConstants.java,v $
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
 * Constants used in representing a Java class-file (*.class).
 * 
 * @author Mark Welsh
 */
public interface ClassConstants
{
    // Constants -------------------------------------------------------------
    public static final int MAGIC = 0xCAFEBABE;

    public static final int MAJOR_VERSION = 0x32;

    public static final int ACC_PUBLIC = 0x0001;
    public static final int ACC_PRIVATE = 0x0002;
    public static final int ACC_PROTECTED = 0x0004;
    public static final int ACC_STATIC = 0x0008;
    public static final int ACC_FINAL = 0x0010;
    public static final int ACC_SUPER = 0x0020;
    public static final int ACC_SYNCHRONIZED = 0x0020;
    public static final int ACC_BRIDGE = 0x0040;
    public static final int ACC_VOLATILE = 0x0040;
    public static final int ACC_VARARGS = 0x0080;
    public static final int ACC_TRANSIENT = 0x0080;
    public static final int ACC_NATIVE = 0x0100;
    public static final int ACC_INTERFACE = 0x0200;
    public static final int ACC_ABSTRACT = 0x0400;
    public static final int ACC_STRICT = 0x0800;
    public static final int ACC_SYNTHETIC = 0x1000;
    public static final int ACC_ANNOTATION = 0x2000;
    public static final int ACC_ENUM = 0x4000;

    public static final int CONSTANT_Utf8 = 1;
    public static final int CONSTANT_Integer = 3;
    public static final int CONSTANT_Float = 4;
    public static final int CONSTANT_Long = 5;
    public static final int CONSTANT_Double = 6;
    public static final int CONSTANT_Class = 7;
    public static final int CONSTANT_String = 8;
    public static final int CONSTANT_Fieldref = 9;
    public static final int CONSTANT_Methodref = 10;
    public static final int CONSTANT_InterfaceMethodref = 11;
    public static final int CONSTANT_NameAndType = 12;

    public static final String ATTR_Unknown = "Unknown";
    public static final String ATTR_Code = "Code";
    public static final String ATTR_ConstantValue = "ConstantValue";
    public static final String ATTR_Exceptions = "Exceptions";
    public static final String ATTR_LineNumberTable = "LineNumberTable";
    public static final String ATTR_SourceFile = "SourceFile";
    public static final String ATTR_LocalVariableTable = "LocalVariableTable";
    public static final String ATTR_InnerClasses = "InnerClasses";
    public static final String ATTR_Synthetic = "Synthetic";
    public static final String ATTR_Deprecated = "Deprecated";
    public static final String ATTR_Signature = "Signature";
    public static final String ATTR_LocalVariableTypeTable = "LocalVariableTypeTable";
    public static final String ATTR_RuntimeVisibleAnnotations = "RuntimeVisibleAnnotations";
    public static final String ATTR_RuntimeInvisibleAnnotations = "RuntimeInvisibleAnnotations";
    public static final String ATTR_RuntimeVisibleParameterAnnotations = "RuntimeVisibleParameterAnnotations";
    public static final String ATTR_RuntimeInvisibleParameterAnnotations = "RuntimeInvisibleParameterAnnotations";
    public static final String ATTR_AnnotationDefault = "AnnotationDefault";
    public static final String ATTR_EnclosingMethod = "EnclosingMethod";
    public static final String ATTR_StackMapTable = "StackMapTable";

    /**
     * List of known attributes
     */
    public static final String[] KNOWN_ATTRS =
    {
        ClassConstants.ATTR_Code,
        ClassConstants.ATTR_ConstantValue,
        ClassConstants.ATTR_Exceptions,
        ClassConstants.ATTR_LineNumberTable,
        ClassConstants.ATTR_SourceFile,
        ClassConstants.ATTR_LocalVariableTable,
        ClassConstants.ATTR_InnerClasses,
        ClassConstants.ATTR_Synthetic,
        ClassConstants.ATTR_Deprecated,
        ClassConstants.ATTR_Signature,
        ClassConstants.ATTR_LocalVariableTypeTable,
        ClassConstants.ATTR_RuntimeVisibleAnnotations,
        ClassConstants.ATTR_RuntimeInvisibleAnnotations,
        ClassConstants.ATTR_RuntimeVisibleParameterAnnotations,
        ClassConstants.ATTR_RuntimeInvisibleParameterAnnotations,
        ClassConstants.ATTR_AnnotationDefault,
        ClassConstants.ATTR_EnclosingMethod,
        ClassConstants.ATTR_StackMapTable,
    };

    /**
     * List of required attributes
     */
    public static final String[] REQUIRED_ATTRS =
    {
        ClassConstants.ATTR_Code,
        ClassConstants.ATTR_ConstantValue,
        ClassConstants.ATTR_Exceptions,
        ClassConstants.ATTR_InnerClasses,
        ClassConstants.ATTR_Synthetic,
        ClassConstants.ATTR_StackMapTable,
    };


    public static final String OPTION_Application = "Application";
    public static final String OPTION_Applet = "Applet";
    public static final String OPTION_Serializable = "Serializable";
    public static final String OPTION_RMI = "RMI";
    public static final String OPTION_Enumeration = "Enumeration";
    public static final String OPTION_Annotations = "Annotations";
    public static final String OPTION_RuntimeAnnotations = "RuntimeAnnotations";
    public static final String OPTION_MapClassString = "MapClassString";
    public static final String OPTION_DigestSHA = "DigestSHA";
    public static final String OPTION_DigestMD5 = "DigestMD5";
    public static final String OPTION_LineNumberDebug = "LineNumberDebug";
    public static final String OPTION_Repackage = "Repackage";
    public static final String OPTION_Generic = "Generic";

    /**
     * List of known script options
     */
    public static final String[] KNOWN_OPTIONS =
    {
        ClassConstants.OPTION_Application,
        ClassConstants.OPTION_Applet,
        ClassConstants.OPTION_Serializable,
        ClassConstants.OPTION_RMI,
        ClassConstants.OPTION_Enumeration,
        ClassConstants.OPTION_Annotations,
        ClassConstants.OPTION_RuntimeAnnotations,
        ClassConstants.OPTION_MapClassString,
        ClassConstants.OPTION_DigestSHA,
        ClassConstants.OPTION_DigestMD5,
        ClassConstants.OPTION_LineNumberDebug,
        ClassConstants.OPTION_Repackage,
        ClassConstants.OPTION_Generic,
    };

    public enum AttrSource
    {
        CLASS, FIELD, METHOD, CODE;
    }
}
