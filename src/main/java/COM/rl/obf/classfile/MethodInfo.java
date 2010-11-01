/* ===========================================================================
 * $RCSfile: MethodInfo.java,v $
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
 * Representation of a method from a class-file.
 *
 * @author      Mark Welsh
 */
public class MethodInfo extends ClassItemInfo
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------


    // Class Methods ---------------------------------------------------------
    /**
     * Create a new MethodInfo from the file format data in the DataInput stream.
     *
     * @throws IOException if class file is corrupt or incomplete
     */
    public static MethodInfo create(DataInput din, ClassFile cf) throws Exception
    {
        if (din == null) throw new IOException("No input stream was provided.");
        MethodInfo mi = new MethodInfo(cf);
        mi.read(din);
        return mi;
    }


    // Instance Methods ------------------------------------------------------
    protected MethodInfo(ClassFile cf) {super(cf);}

    /** List the constant pool entries references from this method or field. */
    public Enumeration listCpRefs() throws Exception
    {
	Vector refs = new Vector();
        for (int i = 0; i < attributes.length; i++)
        {
            if (attributes[i].getAttrName().equals(ATTR_Exceptions))
	    {
		// List references from method's Exceptions attribute
		ExceptionsAttrInfo exceptions = 
		    (ExceptionsAttrInfo)attributes[i];
		for (int j = 0; j < exceptions.count(); j++)
		{
		    refs.addElement(cf.getCpEntry(exceptions.getIndex(j)));
		}
	    }
	    else if (attributes[i].getAttrName().equals(ATTR_Code))
	    {
		// List references from method's Code attribute
		CodeAttrInfo code = (CodeAttrInfo)attributes[i];
		code.addCpRefs(refs);
	    }
	}
	return refs.elements();
    }
}
