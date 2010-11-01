/* ===========================================================================
 * $RCSfile: Md.java,v $
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
import java.lang.reflect.*;
import java.util.*;
import COM.rl.util.*;
import COM.rl.obf.classfile.*;

/**
 * Tree item representing a method.
 *
 * @author      Mark Welsh
 */
public class Md extends MdFd
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /** Ctor. */
    public Md(TreeItem parent, boolean isSynthetic, String name, String descriptor, int access) throws Exception
    {
        super(parent, isSynthetic, name, descriptor, access);
    }

    /** Return the display name of the descriptor types. */
    protected String getDescriptorName()
    {
        String[] types = parseTypes();
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        if (types.length > 0)
        {
            for (int i = 0; i < types.length - 1; i++)
            {
                sb.append(types[i]);
                if (i < types.length - 2)
                {
                    sb.append(", ");
                }
            }
        }
        sb.append(");");
        return sb.toString();
    }

    /** Does this method match the wildcard pattern? (compatibility mode) */
    public boolean isOldStyleMatch(String namePattern, String descPattern) 
    {
        return 
            isOldStyleMatch(namePattern) &&
            isMatch(descPattern, getDescriptor());
    }

    /** Find and add TreeItem references. */
    public void findRefs(ClassFile cf, MethodInfo methodInfo) throws Exception
    {
	// References from method Code and Exceptions attributes
	for (Enumeration enm = methodInfo.listCpRefs(); 
	     enm.hasMoreElements(); )
	{
	    CpInfo cpInfo = (CpInfo)enm.nextElement();
	    if (cpInfo instanceof ClassCpInfo)
	    {
                String name = ((ClassCpInfo)cpInfo).getName(cf);
                if (name.charAt(0) == '[') 
                {
                    // Pull the class ref from an array ref
                    name = name.substring(name.indexOf('L') + 1, 
                                          name.length() - 1);
                }
		addRef(new ClRef(name));
	    }
	    else if (cpInfo instanceof RefCpInfo)
	    {
		RefCpInfo refCpInfo = (RefCpInfo)cpInfo;
		if ((cpInfo instanceof MethodrefCpInfo) || 
		    (cpInfo instanceof InterfaceMethodrefCpInfo))
		{
                    // addRef to method
		    addRef(new MdRef(refCpInfo.getClassName(cf), 
				     refCpInfo.getName(cf), 
				     refCpInfo.getDescriptor(cf)));
		}
		else if (cpInfo instanceof FieldrefCpInfo)
		{
                    // addRef to field
		    addRef(new FdRef(refCpInfo.getClassName(cf), 
				     refCpInfo.getName(cf), 
				     refCpInfo.getDescriptor(cf)));
		}
	    }
	}	    
    }
}

