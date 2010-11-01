/* ===========================================================================
 * $RCSfile: ClassTree.java,v $
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
import COM.rl.obf.classfile.*;

/**
 * Tree structure of package levels, classes, methods and fields used for obfuscation.
 *
 * @author      Mark Welsh
 */
public class ClassTree implements NameMapper
{
    // Constants -------------------------------------------------------------
    public static final char PACKAGE_LEVEL = '/';
    public static final char CLASS_LEVEL = '$';
    public static final char METHOD_FIELD_LEVEL = '/';
    private static final String LOG_PRE_UNOBFUSCATED =
    "# Names reserved from obfuscation:";
    private static final String LOG_PRE_OBFUSCATED =
    "# Obfuscated name mappings (some of these may be unchanged due to polymorphism constraints):";
    private static final String LOG_FREQUENCY_TABLE =
    "# Obfuscated name overloading frequency:";
    private static final String LOG_DANGER_HEADER1 = "# WARNING - Reflection methods are called which may unavoidably break in the";
    private static final String LOG_DANGER_HEADER2 = "# obfuscated version at runtime. Please review your source code to ensure";
    private static final String LOG_DANGER_HEADER3 = "# these methods do not act on classes in the obfuscated Jar file.";


    // Fields ----------------------------------------------------------------
    private Vector retainAttrs = new Vector();  // List of attributes to retain
    private Pk root = null;   // Root package in database (Java default package)

    // Class methods ---------------------------------------------------------
    /** Return a fully qualified name broken into package/class segments. */
    public static Enumeration getNameEnum(String name) throws Exception
    {
        Vector vec = new Vector();
        String nameOrig = name;
        while (name != null && !name.equals(""))
        {
            int posP = name.indexOf(PACKAGE_LEVEL);
            int posC = name.indexOf(CLASS_LEVEL);
            SimpleName simpleName = null;
            if (posP == -1 && posC == -1)
            {
                simpleName = new SimpleName(name).setAsClass();
                name = "";
            }
            if (posP == -1 && posC != -1)
            {
                simpleName = new SimpleName(name.substring(0, posC)).setAsClass();
                name = name.substring(posC + 1, name.length());
            }
            if (posP != -1 && posC == -1)
            {
                simpleName = new SimpleName(name.substring(0, posP)).setAsPackage();
                name = name.substring(posP + 1, name.length());
            }
            if (posP != -1 && posC != -1)
            {
                if (posP < posC)
                {
                    simpleName = new SimpleName(name.substring(0, posP)).setAsPackage();
                    name = name.substring(posP + 1, name.length());
                }
                else
                {
                    throw new IOException("Invalid fully qualified name (a): " + 
                                          nameOrig);
                }
            }
            vec.addElement(simpleName);
        }
        return vec.elements();
    }


    // Instance Methods ------------------------------------------------------
    /** Ctor. */
    public ClassTree()
    {
        root = Pk.createRoot(this);
    }

    /** Return the root node. */
    public Pk getRoot() {return root;}

    /** Update the path of the passed filename, if that path corresponds to a package. */
    public String getOutName(String inName) 
    {
        try
        {
            TreeItem ti = root;
            StringBuffer sb = new StringBuffer();
            for (Enumeration nameEnum = getNameEnum(inName); nameEnum.hasMoreElements(); )
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
                    sb.append(PACKAGE_LEVEL);
                }
                else if (simpleName.isAsClass()) 
                {
                    sb.append(name);
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

    /** Add a classfile's package, class, method and field entries to database. */
    public void addClassFile(ClassFile cf) throws Exception
    {
	addClassFile(cf, false);
    }

    /** Add a classfile's package, class, method and field entries to database. */
    public void addClassFile(ClassFile cf, boolean enableTrim) throws Exception
    {
        // Add the fully qualified class name
        TreeItem ti = root;
        for (Enumeration nameEnum = getNameEnum(cf.getName()); nameEnum.hasMoreElements(); )
        {
            SimpleName simpleName = (SimpleName)nameEnum.nextElement();
            String name = simpleName.getName();
            if (simpleName.isAsPackage()) 
            {
                ti = ((Pk)ti).addPackage(name);
            }
            else if (simpleName.isAsClass()) 
            {
                // If this is an inner class, just add placeholder classes up the tree
                if (nameEnum.hasMoreElements())
                {
                    ti = ((PkCl)ti).addPlaceholderClass(name);
                }
                else
                {
                    ti = ((PkCl)ti).addClass(name, cf.getSuper(), cf.getInterfaces(), cf.getModifiers());
                }
            }
            else 
            {
                throw new Exception("Internal error: illegal package/class name tag");
            }
        }

        // We must have a class before adding methods and fields
        if (ti instanceof Cl)
        {
            Cl cl = (Cl)ti;

            // Add the class's methods to the database
	    for (int i = 0; i < cf.getMethodCount(); i++)
	    {
                Md md = cl.addMethod(cf, cf.getMethod(i), enableTrim);
            }

            // Add the class's fields to the database
	    for (int i = 0; i < cf.getFieldCount(); i++)
	    {
		Fd fd = cl.addField(cf, cf.getField(i), enableTrim);
            }
	    
	    // Construct class's reference list
	    cl.findRefs(cf);

            // Add warnings about class
            cl.setWarnings(cf);
        }
        else
        {
            throw new Exception("Inconsistent class file.");
        }
    }

    /** Mark a class/interface type to suppress warnings from it. */
    public void noWarnClass(String name) throws Exception
    {
        // Mark the class (or classes, if this is a wildcarded specifier)
        for (Enumeration clEnum = getClEnum(name); clEnum.hasMoreElements(); )
        {
            Cl cl = (Cl)clEnum.nextElement();
            cl.setNoWarn();
        }
    }

    /** Write any non-suppressed warnings to the log. */
    public void logWarnings(PrintWriter log) throws Exception
    {
        final Vector hasWarnings = new Vector();
        walkTree(new TreeAction() {
            public void classAction(Cl cl) throws Exception { if (cl.hasWarnings()) hasWarnings.addElement(Boolean.valueOf(true)); }
        });
        if (hasWarnings.size() > 0) 
        {
            log.println("#");
            log.println(LOG_DANGER_HEADER1);
            log.println(LOG_DANGER_HEADER2);
            log.println(LOG_DANGER_HEADER3);
            final PrintWriter flog = log;
            walkTree(new TreeAction() {
                public void classAction(Cl cl) throws Exception { cl.logWarnings(flog); }
            });
        }
    }

    /** Mark an attribute type for retention. */
    public void retainAttribute(String name) throws Exception
    {
        retainAttrs.addElement(name);
    }

    /** Mark a class/interface type (and possibly methods and fields defined in class) for retention. */
    public void retainClass(String name, boolean retainToPublic,
                            boolean retainToProtected,
                            boolean retainPubProtOnly,
                            boolean retainFieldsOnly,
                            boolean retainMethodsOnly,
                            String extendsName,
                            boolean invert,
                            boolean notrimOnly, // TODO - use notrimOnly
                            int accessMask, 
                            int accessSetting) throws Exception
    {
        // Mark the class (or classes, if this is a wildcarded specifier)
        for (Enumeration clEnum = getClEnum(name); clEnum.hasMoreElements(); )
        {
            Cl cl = (Cl)clEnum.nextElement();
            if ((extendsName == null ||
                 cl.hasAsSuperOrInterface(extendsName)) && 
                cl.modifiersMatchMask(accessMask, accessSetting))
            {
                retainHierarchy(cl, invert);
                if (retainToPublic || retainToProtected || retainPubProtOnly)
                {
                    // Retain methods if requested
                    if (!retainFieldsOnly)
                    {
                        for (Enumeration enm = cl.getMethodEnum(); 
                             enm.hasMoreElements(); )
                        {
                            Md md = (Md)enm.nextElement();
                            if ((retainToPublic &&
                                 Modifier.isPublic(md.getModifiers())) || 
                                (retainToProtected && 
                                 !Modifier.isPrivate(md.getModifiers())) ||
                                (retainPubProtOnly &&
                                 (Modifier.isPublic(md.getModifiers()) ||
                                  Modifier.isProtected(md.getModifiers()))))
                            {
                                if (invert)
                                {
                                    md.setOutName(null);
                                    md.clearFromScript();
                                }
                                else
                                {
                                    md.setOutName(md.getInName());
                                    md.setFromScript();
                                }
                            }
                        }
                    }
                    
                    // Retain fields if requested
                    if (!retainMethodsOnly)
                    {
                        for (Enumeration enm = cl.getFieldEnum(); 
                             enm.hasMoreElements(); )
                        {
                            Fd fd = (Fd)enm.nextElement();
                            if ((retainToPublic && 
                                 Modifier.isPublic(fd.getModifiers())) ||
                                (retainToProtected && 
                                 !Modifier.isPrivate(fd.getModifiers())) ||
                                (retainPubProtOnly && 
                                 (Modifier.isPublic(fd.getModifiers()) || 
                                  Modifier.isProtected(fd.getModifiers()))))
                            {
                                if (invert)
                                {
                                    fd.setOutName(null);
                                    fd.clearFromScript();
                                }
                                else
                                {
                                    fd.setOutName(fd.getInName());
                                    fd.setFromScript();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /** Mark a method type for retention. */
    public void retainMethod(String name, String descriptor, 
                             boolean retainAndClass,
                             String extendsName,
                             boolean invert,
			     boolean notrimOnly, // TODO - use notrimOnly
                             int accessMask, 
                             int accessSetting) throws Exception
    {
        for (Enumeration enm = getMdEnum(name, descriptor); 
             enm.hasMoreElements(); ) {
            Md md = (Md)enm.nextElement();
            Cl thisCl = (Cl)md.getParent();
            if ((extendsName == null ||
                 thisCl.hasAsSuperOrInterface(extendsName)) && 
                md.modifiersMatchMask(accessMask, accessSetting))
            {
                if (invert)
                {
                    md.setOutName(null);
                    md.clearFromScript();
                }
                else
                {
                    md.setOutName(md.getInName());
                    md.setFromScript();
                }
                if (retainAndClass) 
                {
                    retainHierarchy(thisCl, invert);
                }
            }
        }
    }

    /** Mark a field type for retention. */
    public void retainField(String name, String descriptor, 
                            boolean retainAndClass,
                            String extendsName,
                            boolean invert,
                            boolean notrimOnly, // TODO - use notrimOnly
                            int accessMask, 
                            int accessSetting) throws Exception
    {
        for (Enumeration enm = getFdEnum(name, descriptor); 
             enm.hasMoreElements(); ) {
            Fd fd = (Fd)enm.nextElement();
            Cl thisCl = (Cl)fd.getParent();
            if ((extendsName == null ||
                 thisCl.hasAsSuperOrInterface(extendsName)) && 
                fd.modifiersMatchMask(accessMask, accessSetting))
            {
                if (invert)
                {
                    fd.setOutName(null);
                    fd.clearFromScript();
                }
                else
                {
                    fd.setOutName(fd.getInName());
                    fd.setFromScript();
                }
                if (retainAndClass) 
                {
                    retainHierarchy(thisCl, invert);
                }
            }
        }
    }

    /** Mark a package for retention, and specify its new name. */
    public void retainPackageMap(String name, String obfName) throws Exception
    {
        retainItemMap(getPk(name), obfName);
    }

    /** Mark a package for repackaging under this new name. */
    public void retainRepackageMap(String name, String obfName) throws Exception
    {
        Pk pk = getPk(name);
        if (!pk.isFixed()) 
        {
            pk.setRepackageName(obfName);
            pk.setOutName(pk.getInName());
        }
    }

    /** Mark a class/interface type for retention, and specify its new name. */
    public void retainClassMap(String name, String obfName) throws Exception
    {
        retainItemMap(getCl(name), obfName);
    }

    /** Mark a method type for retention, and specify its new name. */
    public void retainMethodMap(String name, String descriptor, 
                                String obfName) throws Exception
    {
        retainItemMap(getMd(name, descriptor), obfName);
    }

    /** Mark a field type for retention, and specify its new name. */
    public void retainFieldMap(String name, String obfName) throws Exception
    {
        retainItemMap(getFd(name), obfName);
    }

    // Mark an item for retention, and specify its new name.
    private void retainItemMap(TreeItem item, String obfName) throws Exception
    {
        if (!item.isFixed()) 
        {
            item.setOutName(obfName);
            item.setFromScriptMap();
        }
    }

    /** Traverse the class tree, generating obfuscated names within each namespace. */
    public void generateNames(boolean enableRepackage) throws Exception
    {
        // Repackage first, if requested 
        // (need TreeItem.isFixed set properly, so must be done first)
        if (enableRepackage) {
            // Exclude package names already fixed at the root level
            String[] noObfNames = getRootPackageNames();
            final NameMaker nm = new KeywordNameMaker(noObfNames, false, true);
            // Generate single-level package names, unique across jar
            walkTree(new TreeAction() {
                    public void packageAction(Pk pk) throws Exception {pk.repackageName(nm);}
            });
        }
        // Now rename everything in the traditional way (no repackaging)
        walkTree(new TreeAction() {
            public void packageAction(Pk pk) throws Exception {pk.generateNames();}
            public void classAction(Cl cl) throws Exception {cl.generateNames();}
        });
    }

    // Return list of currently fixed root package names
    private String[] getRootPackageNames() throws Exception
    {
        Vector vec = new Vector();
        for (Enumeration enm = root.getPackageEnum(); enm.hasMoreElements(); )
        {
            Pk pk = (Pk)enm.nextElement();
            if (pk.isFixed())
            {
                vec.addElement(pk.getOutName());
            }
        }
        String[] noObfNames = new String[vec.size()];
        for (int i = 0; i < noObfNames.length; i++)
        {
            noObfNames[i] = (String)vec.elementAt(i);
        }
        return noObfNames;
    }

    /** Resolve the polymorphic dependencies of each class. */
    public void resolveClasses() throws Exception
    {
        walkTree(new TreeAction() {
            public void classAction(Cl cl) throws Exception {cl.resetResolve();}
        });
        walkTree(new TreeAction() {
            public void classAction(Cl cl) throws Exception {cl.setupNameListDowns();}
        });
        Cl.nameSpace = 0;
        walkTree(new TreeAction() {
            public void classAction(Cl cl) throws Exception {cl.resolveOptimally();}
        });
    }

    /** Return a list of attributes marked to keep. */
    public String[] getAttrsToKeep() throws Exception
    {
        String[] attrs = new String[retainAttrs.size()];
        for (int i = 0; i < attrs.length; i++)
        {
            attrs[i] = (String)retainAttrs.elementAt(i);
        }
        return attrs;
    }

    /** Get classes in tree from the fully qualified name 
        (can be wildcarded). */
    public Enumeration getClEnum(String fullName) throws Exception
    {
        final Vector vec = new Vector();
        // Wildcard? then return list of all matching classes (including inner)
        if (fullName.indexOf('*') != -1) 
        {
            // Old !a/b/* wildcard syntax, for backward compatibility
            // (acts as if every * becomes a ** in new-style match)
            if (fullName.indexOf('!') == 0) 
            {
                final String fName = fullName.substring(1);
                walkTree(new TreeAction() 
                {
                    public void classAction(Cl cl) throws Exception 
                    {
                        if (cl.isOldStyleMatch(fName)) 
                        {
                            vec.addElement(cl);
                        }
                    }
                });
            } 
            // New a/b/** wildcard syntax
            else
            {
                final String fName = fullName;
                walkTree(new TreeAction() 
                {
                    public void classAction(Cl cl) throws Exception 
                    {
                        if (cl.isWildcardMatch(fName)) 
                        {
                            vec.addElement(cl);
                        }
                    }
                });                
            }
        }
        else
        {
            // Single class
            Cl cl = getCl(fullName);
            if (cl != null)
            {
                vec.addElement(cl);
            }
        }
        return vec.elements();
    }

    /** Get methods in tree from the fully qualified, and possibly
        wildcarded, name. */
    public Enumeration getMdEnum(String fullName, 
                                 String descriptor) throws Exception
    {
        final Vector vec = new Vector();
        final String fDesc = descriptor;
        // Wildcard? then return list of all matching methods
        if (fullName.indexOf('*') != -1 ||
            descriptor.indexOf('*') != -1) 
        {
            // Old !a/b/* wildcard syntax, for backward compatibility
            // (acts as if every * becomes a ** in new-style match)
            if (fullName.indexOf('!') == 0) 
            {
                final String fName = fullName.substring(1);
                walkTree(new TreeAction() 
                {
                    public void methodAction(Md md) throws Exception 
                    {
                        if (md.isOldStyleMatch(fName, fDesc)) 
                        {
                            vec.addElement(md);
                        }
                    }
                });
            }
            // New a/b/** wildcard syntax
            else
            {
                final String fName = fullName;
                walkTree(new TreeAction() 
                {
                    public void methodAction(Md md) throws Exception 
                    {
                        if (md.isWildcardMatch(fName, fDesc)) 
                        {
                            vec.addElement(md);
                        }
                    }
                });
            }
        } 
        else 
        {
            Md md = getMd(fullName, descriptor);
            if (md != null) 
            {
                vec.addElement(md);
            }
        }
        return vec.elements();
    }

    /** Get fields in tree from the fully qualified, and possibly
        wildcarded, name. */
    public Enumeration getFdEnum(String fullName, 
                                 String descriptor) throws Exception
    {
        final Vector vec = new Vector();
        // Wildcard? then return list of all matching methods
        if (fullName.indexOf('*') != -1) 
        {
            // Old !a/b/* wildcard syntax, for backward compatibility
            // (acts as if every * becomes a ** in new-style match)
            if (fullName.indexOf('!') == 0) 
            {
                final String fName = fullName.substring(1);
                walkTree(new TreeAction() 
                {
                    public void fieldAction(Fd fd) throws Exception 
                    {
                        if (fd.isOldStyleMatch(fName)) 
                        {
                            vec.addElement(fd);
                        }
                    }
                });
            }
            // New a/b/** wildcard syntax
            else
            {
                final String fName = fullName;
                final String fDesc = descriptor;
                walkTree(new TreeAction() 
                {
                    public void fieldAction(Fd fd) throws Exception 
                    {
                        if (fd.isWildcardMatch(fName, fDesc)) 
                        {
                            vec.addElement(fd);
                        }
                    }
                });
            }
        } 
        else 
        {
            Fd fd = getFd(fullName);
            if (fd != null) 
            {
                vec.addElement(fd);
            }
        }
        return vec.elements();
    }

    /** Get class in tree from the fully qualified name, returning null if name not found. */
    public Cl getCl(String fullName) throws Exception
    {
        TreeItem ti = root;
        for (Enumeration nameEnum = getNameEnum(fullName); nameEnum.hasMoreElements(); )
        {
            SimpleName simpleName = (SimpleName)nameEnum.nextElement();
            String name = simpleName.getName();
            if (simpleName.isAsPackage()) 
            {
                ti = ((Pk)ti).getPackage(name);
            }
            else if (simpleName.isAsClass()) 
            {
                ti = ((PkCl)ti).getClass(name);
            }
            else
            {
                throw new Exception("Internal error: illegal package/class name tag");
            }

            // If the name is not in the database, return null
            if (ti == null)
            {
                return null;
            }
        }

        // It is an error if we do not end up with a class or interface
        if (!(ti instanceof Cl))
        {
            // 15Jul2005 - this exception is being over-sensitive with fullName of null (should never get here) so safely return null instead
            //throw new Exception("Inconsistent class or interface name: " + fullName);
            return null;
        }
        return (Cl)ti;
    }

    /** Get package in tree from the fully qualified name, returning null if name not found. */
    public Pk getPk(String fullName) throws Exception
    {
        TreeItem ti = root;
        for (Enumeration nameEnum = getNameEnum(fullName); nameEnum.hasMoreElements(); )
        {
            SimpleName simpleName = (SimpleName)nameEnum.nextElement();
            String name = simpleName.getName();
            ti = ((Pk)ti).getPackage(name);

            // If the name is not in the database, return null
            if (ti == null)
            {
                return null;
            }
            // It is an error if we do not end up with a package
            if (!(ti instanceof Pk))
            {
                throw new Exception("Inconsistent package.");
            }
        }
        return (Pk)ti;
    }

    /** Get method in tree from the fully qualified name. */
    public Md getMd(String fullName, String descriptor) throws Exception
    {
        // Split into class and method names
        int pos = fullName.lastIndexOf(METHOD_FIELD_LEVEL);
        Cl cl = getCl(fullName.substring(0, pos));
        return cl.getMethod(fullName.substring(pos + 1), descriptor);
    }

    /** Get field in tree from the fully qualified name. */
    public Fd getFd(String fullName) throws Exception
    {
        // Split into class and field names
        int pos = fullName.lastIndexOf(METHOD_FIELD_LEVEL);
        Cl cl = getCl(fullName.substring(0, pos));
        return cl.getField(fullName.substring(pos + 1));
    }

    /** Mapping for fully qualified class name.
     *  @see NameMapper#mapClass */
    public String mapClass(String className) throws Exception
    {
        // Check for array -- requires special handling
        if (className.length() > 0 && className.charAt(0) == '[') 
        {
            StringBuffer newName = new StringBuffer();
            int i = 0;
            while (i < className.length()) 
            {
                char ch = className.charAt(i++);
                switch (ch) 
                {
                case '[':
                case ';':
                    newName.append(ch);
                    break;

                case 'L':
                    newName.append(ch);
                    int pos = className.indexOf(';', i);
                    if (pos < 0) 
                    {
                        throw new Exception("Invalid class name encountered: " + className);
                    }
                    newName.append(mapClass(className.substring(i, pos)));
                    i = pos;
                    break;
                    
                default:
                    return className;
                }
            }
            return newName.toString();
        } 
        else 
        {
            Cl cl = getCl(className);
            return cl != null ? cl.getFullOutName() : className;
        }
    }

    /** Mapping for method name, of fully qualified class.
     *  @see NameMapper#mapMethod */
    public String mapMethod(String className, String methodName, String descriptor) throws Exception
    {
        String outName = methodName;
        if (!methodName.equals("<init>")) 
        {
            Stack s = new Stack();
            Cl nextCl = getCl(className);
            if (nextCl != null) 
            {
                s.push(nextCl);
            }
            while (!s.empty()) 
            {
                Cl cl = (Cl)s.pop();
                Md md = cl.getMethod(methodName, descriptor);
                if (md != null) 
                {
                    outName = md.getOutName();
                    break;
                } 
                else 
                {
                    nextCl = cl.getSuperCl();
                    if (nextCl != null) 
                    {
                        s.push(nextCl);
                    }
                    Enumeration enm = cl.getSuperInterfaces();
                    while (enm.hasMoreElements()) 
                    {
                        nextCl = (Cl)enm.nextElement();
                        if (nextCl != null) 
                        {
                            s.push(nextCl);
                        }
                    }
                }
            }
        }
        return outName; 
    }

    /** Mapping for field name, of fully qualified class.
     *  @see NameMapper#mapField */
    public String mapField(String className, String fieldName) throws Exception
    {
        String outName = fieldName;
        if (!fieldName.equals("<init>")) 
        {
            Stack s = new Stack();
            Cl nextCl = getCl(className);
            if (nextCl != null) 
            {
                s.push(nextCl);
            }
            while (!s.empty()) 
            {
                Cl cl = (Cl)s.pop();
                Fd fd = cl.getField(fieldName);
                if (fd != null) 
                {
                    outName = fd.getOutName();
                    break;
                } 
                else 
                {
                    nextCl = cl.getSuperCl();
                    if (nextCl != null) 
                    {
                        s.push(nextCl);
                    }
                    Enumeration enm = cl.getSuperInterfaces();
                    while (enm.hasMoreElements()) 
                    {
                        nextCl = (Cl)enm.nextElement();
                        if (nextCl != null) 
                        {
                            s.push(nextCl);
                        }
                    }
                }
            }
        }
        return outName; 
    }

    /** Mapping for generic type signature.
     *  @see NameMapper#mapSignature */
    public String mapSignature(String signature) throws Exception
    {
        // NOTE - not currently parsed and mapped; reserve identifiers
        //     appearing in type signatures for reflective methods to work.
        return signature;
    }

    /** Mapping for descriptor of field or method.
     *  @see NameMapper#mapDescriptor */
    public String mapDescriptor(String descriptor) throws Exception
    {
        // Pass everything through unchanged, except for the String between
        // 'L' and ';' -- this is passed through mapClass(String)
        StringBuffer newDesc = new StringBuffer();
        int i = 0;
        while (i < descriptor.length())
        {
            char ch = descriptor.charAt(i++);
            switch (ch)
            {
            case '[':
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'Z':
            case 'V':
            case '(':
            case ')':
            case ';':
                newDesc.append(ch);
                break;

            case 'L':
                newDesc.append(ch);
                int pos = descriptor.indexOf(';', i);
                if (pos < 0)
                {
                    throw new Exception("Invalid descriptor string encountered.");
                }
                newDesc.append(mapClass(descriptor.substring(i, pos)));
                i = pos;
                break;

            default:
                throw new Exception("Invalid descriptor string encountered.");
            }
        }
        return newDesc.toString();
    }

    /** Dump the content of the class tree to the specified file (used for logging). */
    public void dump(final PrintWriter log) throws Exception
    {
        log.println("#");
        log.println(LOG_FREQUENCY_TABLE);
        dumpFrequencyCount(log);
        log.println("#");
        log.println(LOG_PRE_UNOBFUSCATED);
        log.println("#");
        walkTree(new TreeAction() 
        {
            public void classAction(Cl cl) 
            {
                if (cl.isFromScript()) 
                {
                    log.println(".class " + cl.getFullInName());
		    if (cl.isTrimmed())
		    {
			log.println("# ERROR: incorrectly trimmed class " + cl.getFullInName());
		    }
                }
            }
            public void methodAction(Md md) 
            {
                if (md.isFromScript()) 
                {
                    log.println(".method " + md.getFullInName() + " " + md.getDescriptor());
		    if (md.isTrimmed())
		    {
			log.println("# ERROR: incorrectly trimmed method " + md.getFullInName() + " " + md.getDescriptor());
		    }
                }
            }
            public void fieldAction(Fd fd) 
            {
                if (fd.isFromScript()) 
                {
                    log.println(".field " + fd.getFullInName() + " " + fd.getDescriptor());
		    if (fd.isTrimmed())
		    {
			log.println("# ERROR: incorrectly trimmed field " + fd.getFullInName() + " " + fd.getDescriptor());
		    }
                }
            }
            public void packageAction(Pk pk) 
            {
                // No action
            }
        });
        log.println("#");
        log.println("#");
        log.println(LOG_PRE_OBFUSCATED);
        log.println("#");
        walkTree(new TreeAction() 
        {
            public void classAction(Cl cl) 
            {
                if (!cl.isFromScript()) 
                {
		    if (cl.isTrimmed()) 
		    {
			log.println("# trimmed class " + cl.getFullInName());
		    }
                    else 
                    {
                        log.println(".class_map " + cl.getFullInName() + " " + cl.getOutName());
                    }
                }
            }
            public void methodAction(Md md) 
            {
                if (!md.isFromScript()) 
                {
                    if (!md.getParent().isTrimmed())
                    {
                        if (md.isTrimmed()) 
                        {
                            log.println("# trimmed method " + md.getFullInName() + " " + md.getDescriptor());
                        }
                        else
                        {
                            log.println(".method_map " + md.getFullInName() + " " + md.getDescriptor() + " " + md.getOutName());
                        }
                    }
                }
            }
            public void fieldAction(Fd fd) 
            {
                if (!fd.isFromScript()) 
                {
                    if (!fd.getParent().isTrimmed())
                    {
                        if (fd.isTrimmed()) 
                        {
                            log.println("# trimmed field " + fd.getFullInName() + " " + fd.getDescriptor());
                        }
                        else 
                        {
                            log.println(".field_map " + fd.getFullInName() + " " + fd.getOutName());
                        }
                    }
                }
            }
            public void packageAction(Pk pk) 
            {
                if (!pk.isFromScript() && pk.getFullInName().length() > 0) 
                {
                    if (pk.getRepackageName() != null) {
                        log.println(".repackage_map " + pk.getFullInName() + " " + pk.getRepackageName());
                    } 
                    else
                    {
                        log.println(".package_map " + pk.getFullInName() + " " + pk.getOutName());
                    }
                }
            }
        });
    }

    private void dumpFrequencyCount(PrintWriter log)
    {
        // Compute total use count
        int totalUseCount = 0;
        for (Enumeration useCountEnum = NameMaker.getUseCounts(); useCountEnum.hasMoreElements(); )
        {
            totalUseCount += ((Integer)useCountEnum.nextElement()).intValue() + 1;
        }

        // Log the individual use counts for names
        if (totalUseCount != 0)
        {
            int sumPercent = 0;
            int sumUseCount = 0;
            Vector sort = new Vector();
            for (Enumeration nameEnum = NameMaker.getNames(), useCountEnum = NameMaker.getUseCounts(); nameEnum.hasMoreElements(); )
            {
                String name = (String)nameEnum.nextElement();
                int useCount = ((Integer)useCountEnum.nextElement()).intValue() + 1;
                int percent = 100 * useCount / totalUseCount;
                if (percent != 0)
                {
                    sort.addElement(new SortElement(name, useCount));
                    sumUseCount += useCount;
                    sumPercent += percent;
                }
            }
            while (sort.size() != 0)
            {
                SortElement se = null;
                int largestUseCount = 0;
                for (Enumeration enm = sort.elements(); enm.hasMoreElements(); )
                {
                    SortElement thisSe = (SortElement)enm.nextElement();
                    if (thisSe.useCount >= largestUseCount)
                    {
                        se = thisSe;
                        largestUseCount = se.useCount;
                    }
                }
                sort.removeElement(se);
                log.println("#  '" + se.name + "'   \tused " + se.useCount + " times\t(" + Integer.toString(100 * se.useCount / totalUseCount) + "%)");
            }

            // Log the remainder percentage
            log.println("#  Other names (each used in <1% of mappings) used a total of " +
                        Integer.toString(totalUseCount - sumUseCount) +
                        " times (" + Integer.toString(100 - sumPercent) + "%)");
            log.println("#");
        }
    }

    class SortElement
    {
        int useCount;
        String name;
        SortElement(String name, int useCount) 
        {
            this.useCount = useCount; 
            this.name = name;
        }
    }

    // Private Methods -------------------------------------------------------
    // Mark TreeItem and all parents for retention.
    private void retainHierarchy(TreeItem ti, boolean invert) throws Exception
    {
        if (invert)
        {
            // error to force package level obfuscation
            if (!(ti instanceof Pk)) {
                ti.setOutName(null);
                ti.clearFromScript();
            }
        }
        else
        {
            if (!ti.isFixed()) 
            {
                ti.setOutName(ti.getInName());
                ti.setFromScript();
            }
        }
        if (ti.parent != null)
        {
            retainHierarchy(ti.parent, invert);
        }
    }

    /** Walk the whole tree taking action once only on each package level, class, method and field. */
    public void walkTree(TreeAction ta) throws Exception
    {
        walkTree(ta, root);
    }

    // Walk the tree which has TreeItem as its root taking action once only on each
    // package level, class, method and field.
    private void walkTree(TreeAction ta, TreeItem ti) throws Exception
    {
        if (ti instanceof Pk)
        {
            Enumeration packageEnum = ((Pk)ti).getPackageEnum();
            ta.packageAction((Pk)ti);
            while (packageEnum.hasMoreElements())
            {
                walkTree(ta, (TreeItem)packageEnum.nextElement());
            }
        }
        if (ti instanceof PkCl)
        {
            Enumeration classEnum = ((PkCl)ti).getClassEnum();
            while (classEnum.hasMoreElements())
            {
                walkTree(ta, (TreeItem)classEnum.nextElement());
            }
        }
        if (ti instanceof Cl)
        {
            Enumeration fieldEnum = ((Cl)ti).getFieldEnum();
            Enumeration methodEnum = ((Cl)ti).getMethodEnum();
            ta.classAction((Cl)ti);
            while (fieldEnum.hasMoreElements())
            {
                ta.fieldAction((Fd)fieldEnum.nextElement());
            }
            while (methodEnum.hasMoreElements())
            {
                ta.methodAction((Md)methodEnum.nextElement());
            }
        }
    }
}


