/* ===========================================================================
 * $RCSfile: GuiConstants.java,v $
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

package COM.rl.obf.gui;

/**
 * Constants for the rgs script GUI.
 *
 * @author      Mark Welsh
 */
public interface GuiConstants
{
    // Constants -------------------------------------------------------------
    public static final String CLASS_EXT = ".class";
    public static final String CLASS_NAME_APPLET = "java/applet/Applet";
    public static final String METHOD_NAME_MAIN = "main";
    public static final String METHOD_DESCRIPTOR_MAIN = "([Ljava/lang/String;)V";
    public static final String SCRIPT_CLASS = ".class ";
    public static final String SCRIPT_PROTECTED = " protected";
    public static final String SCRIPT_PUBLIC = " public";
    public static final String SCRIPT_METHODS_ONLY = " method ";
    public static final String SCRIPT_FIELDS_ONLY = " field ";
    public static final String SCRIPT_METHOD = ".method ";
    public static final String SCRIPT_FIELD = ".field ";
    public static final String SCRIPT_ATTR = ".attribute ";
    public static final String SCRIPT_SF = "SourceFile";
    public static final String SCRIPT_LVT = "LocalVariableTable";
    public static final String SCRIPT_LNT = "LineNumberTable";
}
