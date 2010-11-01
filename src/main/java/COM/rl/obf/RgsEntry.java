/* ===========================================================================
 * $RCSfile: RgsEntry.java,v $
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

package COM.rl.obf;

import java.io.*;
import java.util.*;

/**
 * Representation of RGS script files entry.
 *
 * @author      Mark Welsh
 */
public class RgsEntry
{
    // Constants -------------------------------------------------------------
    public static final int TYPE_OPTION        =  0;
    public static final int TYPE_ATTR          =  1;
    public static final int TYPE_CLASS         =  2;
    public static final int TYPE_NOTRIM_CLASS  =  3;
    public static final int TYPE_NOT_CLASS     =  4;
    public static final int TYPE_FIELD         =  5;
    public static final int TYPE_NOTRIM_FIELD  =  6;
    public static final int TYPE_NOT_FIELD     =  7;
    public static final int TYPE_METHOD        =  8;
    public static final int TYPE_NOTRIM_METHOD =  9;
    public static final int TYPE_NOT_METHOD    = 10;
    public static final int TYPE_PACKAGE_MAP   = 11;
    public static final int TYPE_REPACKAGE_MAP = 12;
    public static final int TYPE_CLASS_MAP     = 13;
    public static final int TYPE_FIELD_MAP     = 14;
    public static final int TYPE_METHOD_MAP    = 15;
    public static final int TYPE_NOWARN        = 16;


    // Fields ----------------------------------------------------------------
    public int type;
    public String name;
    public String descriptor;
    public String extendsName;
    public String obfName;
    public boolean retainToPublic;
    public boolean retainToProtected;
    public boolean retainPubProtOnly;
    public boolean retainFieldsOnly;
    public boolean retainMethodsOnly;
    public boolean retainAndClass;
    public int accessMask;
    public int accessSetting;


    // Instance Methods-------------------------------------------------------
    public RgsEntry(int type, String name)
    {
        this.type = type;
        this.name = name;
    }

    public RgsEntry(int type, String name, String descriptor)
    {
        this.type = type;
        this.name = name;
        this.descriptor = descriptor;
    }

    public String toString()
    {
        return name;
    }

    public String debugString()
    {
        return 
            (type == TYPE_OPTION ? ".option " :
             (type == TYPE_ATTR ? ".attribute " :
              (type == TYPE_CLASS ? ".class " :
               (type == TYPE_NOTRIM_CLASS ? "^class " :
		(type == TYPE_NOT_CLASS ? "!class " :
		 (type == TYPE_METHOD ? ".method " :
		  (type == TYPE_NOTRIM_METHOD ? "^method " :
		   (type == TYPE_NOT_METHOD ? "!method " :
		    (type == TYPE_FIELD ? ".field " :
		     (type == TYPE_NOTRIM_FIELD ? "^field " :
		      (type == TYPE_NOT_FIELD ? "!field " :
		       (type == TYPE_PACKAGE_MAP ? ".package_map " :
                        (type == TYPE_REPACKAGE_MAP ? ".repackage_map " :
                         (type == TYPE_CLASS_MAP ? ".class_map " :
                          (type == TYPE_METHOD_MAP ? ".method_map " :
                           (type == TYPE_FIELD_MAP ? ".field_map " :
                            (type == TYPE_NOWARN ? ".nowarn " : "")))))))))))))))))
            + name + " " 
            + (descriptor != null ? descriptor + " " : "") 
            + (retainToPublic ? "public " : "")
            + (retainToProtected ? "protected " : "")
            + (retainPubProtOnly ? "pub_prot_only " : "")
            + (retainFieldsOnly ? "field " : "")
            + (retainMethodsOnly ? "method " : "")
            + (retainAndClass ? "and_class " : "")
            + (extendsName != null ? "extends " + extendsName : "");
    }
}
