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
 * @author      Mark Welsh
 */
public interface ClassConstants
{
    // Constants -------------------------------------------------------------
    public static final int MAGIC = 0xCAFEBABE;

    public static final int MAJOR_VERSION = 0x32;
                                                      // Class Method Field
    public static final int ACC_PUBLIC      = 0x0001; // X     X      X
    public static final int ACC_PRIVATE     = 0x0002; //       X      X
    public static final int ACC_PROTECTED   = 0x0004; //       X      X
    public static final int ACC_STATIC      = 0x0008; //       X      X
    public static final int ACC_FINAL       = 0x0010; // X     X      X
    public static final int ACC_SUPER       = 0x0020; // X             
    public static final int ACC_SYNCHRONIZED= 0x0020; //       X       
    public static final int ACC_BRIDGE      = 0x0040; //       X       
    public static final int ACC_VOLATILE    = 0x0040; //              X
    public static final int ACC_VARARGS     = 0x0080; //       X       
    public static final int ACC_TRANSIENT   = 0x0080; //              X
    public static final int ACC_NATIVE      = 0x0100; //       X       
    public static final int ACC_INTERFACE   = 0x0200; // X             
    public static final int ACC_ABSTRACT    = 0x0400; // X     X       
    public static final int ACC_STRICT      = 0x0800; //       X       
    public static final int ACC_SYNTHETIC   = 0x1000; //       X       
    public static final int ACC_ANNOTATION  = 0x2000; // X             
    public static final int ACC_ENUM        = 0x4000; // X            X

    public static final int CONSTANT_Utf8               = 1;
    public static final int CONSTANT_Integer            = 3;
    public static final int CONSTANT_Float              = 4;
    public static final int CONSTANT_Long               = 5;
    public static final int CONSTANT_Double             = 6;
    public static final int CONSTANT_Class              = 7;
    public static final int CONSTANT_String             = 8;
    public static final int CONSTANT_Fieldref           = 9;
    public static final int CONSTANT_Methodref          = 10;
    public static final int CONSTANT_InterfaceMethodref = 11;
    public static final int CONSTANT_NameAndType        = 12;

    public static final String ATTR_Unknown             = "Unknown";
    public static final String ATTR_Code                = "Code";
    public static final String ATTR_ConstantValue       = "ConstantValue";
    public static final String ATTR_Exceptions          = "Exceptions";
    public static final String ATTR_LineNumberTable     = "LineNumberTable";
    public static final String ATTR_SourceFile          = "SourceFile";
    public static final String ATTR_LocalVariableTable  = "LocalVariableTable";
    public static final String ATTR_InnerClasses        = "InnerClasses";
    public static final String ATTR_Synthetic           = "Synthetic";
    public static final String ATTR_Deprecated          = "Deprecated";
    public static final String ATTR_Signature           = "Signature";
    public static final String ATTR_LocalVariableTypeTable = "LocalVariableTypeTable";
    public static final String ATTR_RuntimeVisibleAnnotations = "RuntimeVisibleAnnotations";
    public static final String ATTR_RuntimeInvisibleAnnotations = "RuntimeInvisibleAnnotations";
    public static final String ATTR_RuntimeVisibleParameterAnnotations = "RuntimeVisibleParameterAnnotations";
    public static final String ATTR_RuntimeInvisibleParameterAnnotations = "RuntimeInvisibleParameterAnnotations";
    public static final String ATTR_AnnotationDefault   = "AnnotationDefault";
    public static final String ATTR_EnclosingMethod     = "EnclosingMethod";
    public static final String ATTR_StackMapTable       = "StackMapTable";

    // List of known attributes
    public static final String[] KNOWN_ATTRS = 
    {
        ATTR_Code,
        ATTR_ConstantValue,
        ATTR_Exceptions,
        ATTR_LineNumberTable,
        ATTR_SourceFile,
        ATTR_LocalVariableTable,
        ATTR_InnerClasses,
        ATTR_Synthetic,
        ATTR_Deprecated,
        ATTR_Signature,
        ATTR_LocalVariableTypeTable,
        ATTR_RuntimeVisibleAnnotations,
        ATTR_RuntimeInvisibleAnnotations,
        ATTR_RuntimeVisibleParameterAnnotations,
        ATTR_RuntimeInvisibleParameterAnnotations,
        ATTR_AnnotationDefault,
        ATTR_EnclosingMethod,
        ATTR_StackMapTable,
    };

    // List of required attributes
    public static final String[] REQUIRED_ATTRS = 
    {
        ATTR_Code,
        ATTR_ConstantValue,
        ATTR_Exceptions,
        ATTR_InnerClasses,
        ATTR_Synthetic,
        ATTR_StackMapTable,
    };


    public static final String OPTION_Application       = "Application";
    public static final String OPTION_Applet            = "Applet";
    public static final String OPTION_Serializable      = "Serializable";
    public static final String OPTION_RMI               = "RMI";
    public static final String OPTION_Enumeration       = "Enumeration";
    public static final String OPTION_Annotations       = "Annotations";
    public static final String OPTION_RuntimeAnnotations= "RuntimeAnnotations";
    public static final String OPTION_MapClassString    = "MapClassString";
    public static final String OPTION_DigestSHA         = "DigestSHA";
    public static final String OPTION_DigestMD5         = "DigestMD5";
    public static final String OPTION_LineNumberDebug   = "LineNumberDebug";
    public static final String OPTION_Trim              = "Trim";
    public static final String OPTION_Repackage         = "Repackage";
    public static final String OPTION_Generic           = "Generic";

    // List of known script options
    public static final String[] KNOWN_OPTIONS = {OPTION_Application,
                                                  OPTION_Applet,
                                                  OPTION_Serializable,
                                                  OPTION_RMI,
                                                  OPTION_Enumeration,
                                                  OPTION_Annotations,
                                                  OPTION_RuntimeAnnotations,
                                                  OPTION_MapClassString,
                                                  OPTION_DigestSHA,
                                                  OPTION_DigestMD5,
						  OPTION_LineNumberDebug,
						  OPTION_Trim,
						  OPTION_Repackage,
						  OPTION_Generic,
    };
}
