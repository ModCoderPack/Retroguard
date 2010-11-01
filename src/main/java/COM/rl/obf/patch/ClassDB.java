/* ===========================================================================
 * $RCSfile: ClassDB.java,v $
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

package COM.rl.obf.patch;

import java.io.*;
import java.util.*;
import COM.rl.obf.*;

/**
 * Database of package and class relations formed from an RGS log file.
 *
 * @author Mark Welsh
 */
public class ClassDB 
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private Pk root = null;   // Root package (Java default package)


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /** Ctor. */
    public ClassDB(File rgsFile) throws Exception 
    {
        try 
        {
            if (rgsFile != null && rgsFile.exists()) 
            {
                InputStream rgsInputStream = new FileInputStream(rgsFile);
                createHierarchy(new RgsEnum(rgsInputStream));
                rgsInputStream.close();
            }
        } 
        catch (Exception e) 
        {
            // Reset the database
            root = null;
            throw new Exception("Log-file corrupt or in an older format (pre-v1.1)");
        }
    }

    /** Convert entries in file to obfuscated versions. */
    public Vector toObf(File listFile) throws Exception 
    {
        Vector outNames = new Vector();
        
        // Run through the listed names
        for (Enumeration enm = getInNames(listFile); 
             enm.hasMoreElements(); ) 
        {
            // Convert the obfuscated portions
            String inName = (String)enm.nextElement();
            String outName = null;
            
            // Ignore inner class listings
            if (inName.indexOf('$') == -1) 
            {
                // Split at first '.'
                int pos = inName.indexOf('.');
                String pre = (pos == -1 ? inName : inName.substring(0, pos));
                String post = (pos == -1 ? null : inName.substring(pos + 1));
                if (post != null && post.equals("class")) 
                {
                    // Convert class name
                    outName = getOutName(pre) + ".class";
                } 
                else 
                {
                    // Convert resource name
                    outName = getOutName(inName);
                }
                
                // Store the converted name
                if (outName != null) 
                {
                    outNames.addElement(outName);
                }
            }
        }
        return outNames;
    }

    // Map a name
    private String getOutName(String inName) 
    {
        try 
        {
            TreeItem ti = root;
            StringBuffer sb = new StringBuffer();
            for (Enumeration nameEnum = ClassTree.getNameEnum(inName); 
                 nameEnum.hasMoreElements(); ) 
            {
                SimpleName simpleName = (SimpleName)nameEnum.nextElement();
                String name = simpleName.getName();
                if (simpleName.isAsPackage()) 
                {
                    if (ti != null) 
                    {
                        ti = ((Pk)ti).getPackage(name);
                        if (ti != null) 
                        {
                            String repackageName = ((Pk)ti).getRepackageName();
                            if (repackageName != null) {
                                sb = new StringBuffer(repackageName);
                            } else {
                                sb.append(ti.getOutName());
                            }
                        } 
                        else 
                        {
                            sb.append(name); 
                        }
                    } 
                    else 
                    {
                        sb.append(name);
                    }
                    sb.append(ClassTree.PACKAGE_LEVEL);
                }
                else if (simpleName.isAsClass()) 
                {
                    if (ti != null) 
                    {
                        ti = ((Pk)ti).getClass(name);
                        if (ti != null)
                        {
                            sb.append(ti.getOutName());
                        }
                        else 
                        {
                            sb.append(name); 
                        }
                    } 
                    else 
                    {
                        sb.append(name);
                    }
                    return sb.toString();
                }
                else
                {
                    throw new Exception("Internal error: illegal package/class name tag");
                }
            }
        } 
        catch (Exception e) 
        {
            // Just drop through and return the original name
        }
        return inName;
    }

    /** Convert entries in file to obfuscated versions. */
    public Vector toObfForConv(File listFile) throws Exception 
    {
        Vector outNames = new Vector();
        
        // Run through the listed names
        for (Enumeration enm = getInNames(listFile); 
             enm.hasMoreElements(); ) 
        {
            // Convert the obfuscated portions
            String inName = (String)enm.nextElement();
            String outName = null;
            
            // Split at first '.'
            int pos = inName.indexOf('.');
            String pre = (pos == -1 ? inName : inName.substring(0, pos));
            String post = (pos == -1 ? null : inName.substring(pos + 1));
            if (post != null && post.equals("class")) 
            {
                // Convert class name
                outName = getOutNameForConv(pre) + ".class";
            } 
            else 
            {
                // Convert resource name
                outName = getOutNameForConv(inName);
            }
            
            // Store the converted name
            if (outName != null) 
            {
                outNames.addElement(outName);
            }
        }
        return outNames;
    }

    // Map a name
    private String getOutNameForConv(String inName) 
    {
        try 
        {
            TreeItem ti = root;
            StringBuffer sb = new StringBuffer();
            for (Enumeration nameEnum = ClassTree.getNameEnum(inName); 
                 nameEnum.hasMoreElements(); ) 
            {
                SimpleName simpleName = (SimpleName)nameEnum.nextElement();
                String name = simpleName.getName();
                if (simpleName.isAsPackage()) 
                {
                    if (ti != null) 
                    {
                        ti = ((Pk)ti).getPackage(name);
                        if (ti != null) 
                        {
                            String repackageName = ((Pk)ti).getRepackageName();
                            if (repackageName != null) {
                                sb = new StringBuffer(repackageName);
                            } else {
                                sb.append(ti.getOutName());
                            }
                        } 
                        else 
                        {
                            sb.append(name); 
                        }
                    } 
                    else 
                    {
                        sb.append(name);
                    }
                    sb.append(ClassTree.PACKAGE_LEVEL);
                }
                else if (simpleName.isAsClass()) 
                {
                    if (ti != null) 
                    {
                        ti = ((PkCl)ti).getClass(name);
                        if (ti != null) 
                        {
                            sb.append(ti.getOutName());
                        }
                        else 
                        {
                            sb.append(name); 
                        }
                    } 
                    else 
                    {
                        sb.append(name);
                    }
                    if (nameEnum.hasMoreElements()) 
                    {
                        sb.append(ClassTree.CLASS_LEVEL);
                    } 
                    else 
                    {
                        return sb.toString();
                    }
                }
                else
                {
                    throw new Exception("Internal error: illegal package/class name tag");
                }
            }
        } 
        catch (Exception e) 
        {
            // Just drop through and return the original name
        }
        return inName;
    }

    // Parse the file names from the list
    private Enumeration getInNames(File listFile) throws Exception 
    {
        InputStream is = listFile == null ? 
            System.in : 
            new FileInputStream(listFile);
        StreamTokenizer tk = new StreamTokenizer(
            new BufferedReader(
                new InputStreamReader(is)));
        tk.resetSyntax();
        tk.whitespaceChars(0x00, 0x20);
        tk.wordChars('*', '*');
        tk.wordChars('.', '.');
        tk.wordChars(';', ';');
        tk.wordChars('_', '_');
        tk.wordChars('[', '[');
        tk.wordChars('(', ')');
        tk.wordChars('$', '$');
        tk.wordChars('/', '9');
        tk.wordChars('A', 'Z');
        tk.wordChars('a', 'z');
        tk.commentChar('#');
        tk.eolIsSignificant(false);
        Vector inNames = new Vector();
        try 
        {
            int ttype;
            while ((ttype = tk.nextToken()) != StreamTokenizer.TT_EOF) 
            {
                if (ttype == StreamTokenizer.TT_WORD) 
                {
                    inNames.addElement(tk.sval);
                }
            }
        } 
        finally 
        {
            return inNames.elements();
        }
    }

    // Create database from RGS log entries.
    private void createHierarchy(RgsEnum rgsEnum) throws Exception 
    {
        // Create the root of the package hierarchy
        root = new Pk(null, "");

        // Enumerate the entries in the RGS script
        while (rgsEnum.hasMoreEntries()) 
        {
            RgsEntry entry = rgsEnum.nextEntry();
            switch (entry.type) 
            {
            case RgsEntry.TYPE_PACKAGE_MAP:
                addPackage(entry.name, entry.obfName);
                break;
                
            case RgsEntry.TYPE_REPACKAGE_MAP:
                addPackage(entry.name, null, entry.obfName);
                break;
                
            case RgsEntry.TYPE_CLASS:
                addClass(entry.name, getLastIdentifier(entry.name));
                break;
                
            case RgsEntry.TYPE_CLASS_MAP:
                addClass(entry.name, entry.obfName);
                break;
                
            case RgsEntry.TYPE_METHOD:
                addMethod(entry.name, entry.descriptor, getLastIdentifier(entry.name));
                break;
                
            case RgsEntry.TYPE_METHOD_MAP:
                addMethod(entry.name, entry.descriptor, entry.obfName);
                break;
                
            case RgsEntry.TYPE_ATTR:
            case RgsEntry.TYPE_FIELD:
            case RgsEntry.TYPE_FIELD_MAP:
                // Ignore attribute and field entries
                break;
                
            default:
                throw new Exception("Illegal type received from the .rgs script");
            }
        }
    }

    // Add a package to the hierarchy
    private void addPackage(String fullName, String obfName) throws Exception 
    {
        addPackage(fullName, obfName, null);
    }

    // Add a package to the hierarchy
    private void addPackage(String fullName, String obfName, String repackageName) throws Exception 
    {
        TreeItem ti = root;
        for (Enumeration nameEnum = ClassTree.getNameEnum(fullName); 
             nameEnum.hasMoreElements(); ) 
        {
            SimpleName simpleName = (SimpleName)nameEnum.nextElement();
            String name = simpleName.getName();
            ti = ((Pk)ti).addPackage(name);
        }

        // Set the obfuscated name for the package
        if (repackageName != null) 
        {
            ((Pk)ti).setRepackageName(repackageName);
            ti.setOutName(ti.getInName());
        }
        else
        {
            ti.setOutName(obfName);
        }
    }

    // Add a class to the hierarchy
    private Cl addClass(String fullName, String obfName) throws Exception 
    {
        TreeItem ti = root;
        for (Enumeration nameEnum = ClassTree.getNameEnum(fullName); 
             nameEnum.hasMoreElements(); ) 
        {
            SimpleName simpleName = (SimpleName)nameEnum.nextElement();
            String name = simpleName.getName();
            if (simpleName.isAsPackage()) 
            {
                ti = ((Pk)ti).addPackage(name);
            }
            else if (simpleName.isAsClass()) 
            {
                // If inner class, just add placeholder classes up the tree
                if (nameEnum.hasMoreElements()) 
                {
                    ti = ((PkCl)ti).addPlaceholderClass(name);
                } 
                else 
                {
                    ti = ((PkCl)ti).addClass(name, null, null, 0);
                }
            }
            else 
            {
                throw new Exception("Internal error: illegal package/class name tag");
            }
        }

        // Set the obfuscated name for the class (anonymous inner classes 
        // are already set, so leave them be)
        if (obfName != null && !Character.isDigit(obfName.charAt(0))) 
        {
            ti.setOutName(obfName);
        }
	return (Cl)ti;
    }

    // Add a method to the hierarchy
    private void addMethod(String fullName, String descriptor, String obfName) throws Exception 
    {
	// Split at last '/' - method name
	int pos = fullName.lastIndexOf('/');
	String className = fullName.substring(0, pos);
	String methodName = fullName.substring(pos + 1);
	
	// Add the packaged class and the method elements
	Cl cl = addClass(className, null);
	Md md = cl.addMethod(false, methodName, descriptor, 0);
	md.setOutName(obfName);
    }

    // Return the last Java identifier from the passed String
    private String getLastIdentifier(String name) 
    {
        String result = null;
        if (name != null) 
        {
            int outer = name.lastIndexOf('/');
            int inner = name.lastIndexOf('$');
            if (outer != -1 || inner != -1) 
            {
                int pos = Math.max(inner, outer);
                result = name.substring(pos + 1);
            }
        }
        return result;
    }

    /** Convert obfuscated stacktrace to ambiguous, original version. */
    public void fromObfForTrace(File traceFile, PrintStream out) throws Exception
    {
        InputStream is = traceFile == null ? 
            System.in : 
            new FileInputStream(traceFile);
        StreamTokenizer tk = new StreamTokenizer(
            new BufferedReader(
                new InputStreamReader(is)));
        tk.resetSyntax();
        tk.whitespaceChars(0x00, 0x20);
        tk.wordChars('.', '.');
        tk.wordChars(':', ':');
        tk.wordChars('_', '_');
        tk.wordChars('(', ')');
        tk.wordChars('$', '$');
        tk.wordChars('/', '9');
        tk.wordChars('A', 'Z');
        tk.wordChars('a', 'z');
        tk.eolIsSignificant(false);
	int ttype;
	while ((ttype = tk.nextToken()) != StreamTokenizer.TT_EOF) 
	{
	    if (ttype == StreamTokenizer.TT_WORD) 
	    {
		String deobf = deobfTraceLine(tk.sval);
		if (tk.sval.equals("at")) {
		    out.println();
		    out.print("\tat ");
		} else if (deobf != null) {
		    out.print(deobf + " ");
		} else {
		    out.print(tk.sval + " ");
		}
	    }
	}
	out.println();
    }

    // Deobfuscate a string containing an obfuscated method
    private String deobfTraceLine(String line)
	{
	// Split at first '(' if any
	int pos = line.indexOf('(');
	String obfFullMethod = (pos == -1) ? line : line.substring(0, pos);
	String rest = (pos == -1) ? "" : line.substring(pos);
	String deobfMethod = getDeobfMethod(obfFullMethod.replace('.', '/'));
	if (deobfMethod == null)
	{
	    return null;
	}
	else 
	{
	    return deobfMethod.replace('/', '.') + rest;
	}
    }

    private String getDeobfMethod(String obfFullMethod)
    {
	int pos = obfFullMethod.lastIndexOf('/');
	if (pos == -1) return null;
	String className = obfFullMethod.substring(0, pos);
	String methodName = obfFullMethod.substring(pos + 1);

	TreeItem ti = root;
	StringBuffer sb = new StringBuffer();
        try 
        {
            for (Enumeration nameEnum = ClassTree.getNameEnum(className); 
                 nameEnum.hasMoreElements(); ) 
            {
                SimpleName simpleName = (SimpleName)nameEnum.nextElement();
                String name = simpleName.getName();
                if (simpleName.isAsPackage()) 
                {
                    if (ti != null) 
                    {
                        // Check regular obfuscated package names on this level
                        ti = ((Pk)ti).getObfPackage(name);
                        // Not found, so check for obfuscated name among 
                        // repackaged names through the package hierarchy
                        if (ti == null)
                        {
                            ti = root.getObfRepackage(name);
                        }
                        if (ti != null) 
                        {
                            String repackageName = ((Pk)ti).getRepackageName();
                            if (repackageName != null) {
                                sb = new StringBuffer(ti.getFullInName());
                            } else {
                                sb.append(ti.getInName());
                            }
                        } 
                        else 
                        {
                            sb.append(name); 
                        }
                    } 
                    else 
                    {
                        sb.append(name);
                    }
                    sb.append(ClassTree.PACKAGE_LEVEL);
                }
                else if (simpleName.isAsClass()) 
                {
                    if (ti != null) 
                    {
                        ti = ((PkCl)ti).getObfClass(name);
                        if (ti != null) 
                        {
                            sb.append(ti.getInName());
                        }
                        else 
                        {
                            sb.append(name); 
                        }
                    } 
                    else 
                    {
                        sb.append(name);
                    }
                    if (nameEnum.hasMoreElements()) 
                    {
                        sb.append(ClassTree.CLASS_LEVEL);
                    } 
                    else 
                    {
			// Deobf. method name (multi-valued due to overloading)
			sb.append('/');
			if (ti != null) 
			{
			    for (Enumeration mdEnum = 
				     ((Cl)ti).getObfMethods(methodName);
				 mdEnum.hasMoreElements(); )
			    {
				sb.append('{');
				sb.append(((Md)mdEnum.nextElement()).getInName());
				sb.append('}');
			    }
			}
			else 
			{
			    sb.append(methodName);
			}
			return sb.toString();
                    }
                }
                else 
                {
                    throw new Exception("Internal error: illegal package/class name tag");
                }
            }
        } 
        catch (Exception e) 
        {
            // Just drop through and return the original name
        }
        return obfFullMethod;
    }
}




