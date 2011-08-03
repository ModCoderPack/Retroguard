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
 * @author Mark Welsh
 */
public class ClassTree implements NameMapper
{
    // Constants -------------------------------------------------------------
    public static final char PACKAGE_LEVEL = '/';
    public static final char CLASS_LEVEL = '$';
    public static final char METHOD_FIELD_LEVEL = '/';
    private static final String LOG_PRE_UNOBFUSCATED = "# Names reserved from obfuscation:";
    private static final String LOG_PRE_OBFUSCATED =
        "# Obfuscated name mappings (some of these may be unchanged due to polymorphism constraints):";
    private static final String LOG_DANGER_HEADER1 =
        "# WARNING - Reflection methods are called which may unavoidably break in the";
    private static final String LOG_DANGER_HEADER2 =
        "# obfuscated version at runtime. Please review your source code to ensure";
    private static final String LOG_DANGER_HEADER3 =
        "# these methods do not act on classes in the obfuscated Jar file.";


    // Fields ----------------------------------------------------------------
    /** List of attributes to retain */
    private Vector<String> retainAttrs = new Vector<String>();

    /** Root package in database (Java default package) */
    private Pk root = null;

    // Class methods ---------------------------------------------------------
    /** Return a fully qualified name broken into package/class segments. */
    public static Enumeration<SimpleName> getNameEnum(String name) throws Exception
    {
        Vector<SimpleName> vec = new Vector<SimpleName>();
        String nameOrig = name;
        while ((name != null) && !name.equals(""))
        {
            int posP = name.indexOf(ClassTree.PACKAGE_LEVEL);
            int posC = name.indexOf(ClassTree.CLASS_LEVEL);
            SimpleName simpleName = null;
            if ((posP == -1) && (posC == -1))
            {
                simpleName = new SimpleName(name).setAsClass();
                name = "";
            }
            if ((posP == -1) && (posC != -1))
            {
                simpleName = new SimpleName(name.substring(0, posC)).setAsClass();
                name = name.substring(posC + 1, name.length());
            }
            if ((posP != -1) && (posC == -1))
            {
                simpleName = new SimpleName(name.substring(0, posP)).setAsPackage();
                name = name.substring(posP + 1, name.length());
            }
            if ((posP != -1) && (posC != -1))
            {
                if (posP < posC)
                {
                    simpleName = new SimpleName(name.substring(0, posP)).setAsPackage();
                    name = name.substring(posP + 1, name.length());
                }
                else
                {
                    throw new IOException("Invalid fully qualified name (a): " + nameOrig);
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
        this.root = Pk.createRoot(this);
    }

    /** Return the root node. */
    public Pk getRoot()
    {
        return this.root;
    }

    /** Update the path of the passed filename, if that path corresponds to a package. */
    public String getOutName(String inName)
    {
        try
        {
            TreeItem ti = this.root;
            StringBuffer sb = new StringBuffer();
            for (Enumeration<SimpleName> nameEnum = ClassTree.getNameEnum(inName); nameEnum.hasMoreElements();)
            {
                SimpleName simpleName = nameEnum.nextElement();
                String name = simpleName.getName();
                if (simpleName.isAsPackage())
                {
                    if (ti != null)
                    {
                        ti = ((Pk)ti).getPackage(name);
                        if (ti != null)
                        {
                            String repackageName = ((Pk)ti).getRepackageName();
                            if (repackageName != null)
                            {
                                sb = new StringBuffer(repackageName);
                            }
                            else
                            {
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
        // Add the fully qualified class name
        TreeItem ti = this.root;
        for (Enumeration<SimpleName> nameEnum = ClassTree.getNameEnum(cf.getName()); nameEnum.hasMoreElements();)
        {
            SimpleName simpleName = nameEnum.nextElement();
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
                cl.addMethod(cf, cf.getMethod(i));
            }

            // Add the class's fields to the database
            for (int i = 0; i < cf.getFieldCount(); i++)
            {
                cl.addField(cf, cf.getField(i));
            }

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
        for (Enumeration<Cl> clEnum = this.getClEnum(name); clEnum.hasMoreElements();)
        {
            Cl cl = clEnum.nextElement();
            cl.setNoWarn();
        }
    }

    /** Write any non-suppressed warnings to the log. */
    public void logWarnings(PrintWriter log) throws Exception
    {
        final Vector<Boolean> hasWarnings = new Vector<Boolean>();
        this.walkTree(new TreeAction()
        {
            @Override
            public void classAction(Cl cl) throws Exception
            {
                if (cl.hasWarnings())
                {
                    hasWarnings.addElement(Boolean.valueOf(true));
                }
            }
        });
        if (hasWarnings.size() > 0)
        {
            log.println("#");
            log.println(ClassTree.LOG_DANGER_HEADER1);
            log.println(ClassTree.LOG_DANGER_HEADER2);
            log.println(ClassTree.LOG_DANGER_HEADER3);
            final PrintWriter flog = log;
            this.walkTree(new TreeAction()
            {
                @Override
                public void classAction(Cl cl) throws Exception
                {
                    cl.logWarnings(flog);
                }
            });
        }
    }

    /** Mark an attribute type for retention. */
    public void retainAttribute(String name) throws Exception
    {
        this.retainAttrs.addElement(name);
    }

    /** Mark a class/interface type (and possibly methods and fields defined in class) for retention. */
    public void retainClass(String name, boolean retainToPublic, boolean retainToProtected, boolean retainPubProtOnly,
        boolean retainFieldsOnly, boolean retainMethodsOnly, String extendsName, boolean invert, int accessMask, int accessSetting)
        throws Exception
    {
        // Mark the class (or classes, if this is a wildcarded specifier)
        for (Enumeration<Cl> clEnum = this.getClEnum(name); clEnum.hasMoreElements();)
        {
            Cl cl = clEnum.nextElement();
            if (((extendsName == null) || cl.hasAsSuperOrInterface(extendsName))
                && cl.modifiersMatchMask(accessMask, accessSetting))
            {
                this.retainHierarchy(cl, invert);
                if (retainToPublic || retainToProtected || retainPubProtOnly)
                {
                    // Retain methods if requested
                    if (!retainFieldsOnly)
                    {
                        for (Enumeration<Md> enm = cl.getMethodEnum(); enm.hasMoreElements();)
                        {
                            Md md = enm.nextElement();
                            if ((retainToPublic && Modifier.isPublic(md.getModifiers()))
                                || (retainToProtected && !Modifier.isPrivate(md.getModifiers()))
                                || (retainPubProtOnly && (Modifier.isPublic(md.getModifiers())
                                    || Modifier.isProtected(md.getModifiers()))))
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
                        for (Enumeration<Fd> enm = cl.getFieldEnum(); enm.hasMoreElements();)
                        {
                            Fd fd = enm.nextElement();
                            if ((retainToPublic && Modifier.isPublic(fd.getModifiers()))
                                || (retainToProtected && !Modifier.isPrivate(fd.getModifiers()))
                                || (retainPubProtOnly && (Modifier.isPublic(fd.getModifiers())
                                    || Modifier.isProtected(fd.getModifiers()))))
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
    public void retainMethod(String name, String descriptor, boolean retainAndClass, String extendsName, boolean invert,
        int accessMask, int accessSetting) throws Exception
    {
        for (Enumeration<Md> enm = this.getMdEnum(name, descriptor); enm.hasMoreElements();)
        {
            Md md = enm.nextElement();
            Cl thisCl = (Cl)md.getParent();
            if (((extendsName == null) || thisCl.hasAsSuperOrInterface(extendsName))
                && md.modifiersMatchMask(accessMask, accessSetting))
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
                    this.retainHierarchy(thisCl, invert);
                }
            }
        }
    }

    /** Mark a field type for retention. */
    public void retainField(String name, String descriptor, boolean retainAndClass, String extendsName, boolean invert,
        int accessMask, int accessSetting) throws Exception
    {
        for (Enumeration<Fd> enm = this.getFdEnum(name, descriptor); enm.hasMoreElements();)
        {
            Fd fd = enm.nextElement();
            Cl thisCl = (Cl)fd.getParent();
            if (((extendsName == null) || thisCl.hasAsSuperOrInterface(extendsName))
                && fd.modifiersMatchMask(accessMask, accessSetting))
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
                    this.retainHierarchy(thisCl, invert);
                }
            }
        }
    }

    /** Mark a package for retention, and specify its new name. */
    public void retainPackageMap(String name, String obfName) throws Exception
    {
        this.retainItemMap(this.getPk(name), obfName);
    }

    /** Mark a package for repackaging under this new name. */
    public void retainRepackageMap(String name, String obfName) throws Exception
    {
        Pk pk = this.getPk(name);
        if (!pk.isFixed())
        {
            pk.setRepackageName(obfName);
            pk.setOutName(pk.getInName());
        }
    }

    /** Mark a class/interface type for retention, and specify its new name. */
    public void retainClassMap(String name, String obfName) throws Exception
    {
        this.retainItemMap(this.getCl(name), obfName);
    }

    /** Mark a method type for retention, and specify its new name. */
    public void retainMethodMap(String name, String descriptor, String obfName) throws Exception
    {
        this.retainItemMap(this.getMd(name, descriptor), obfName);
    }

    /** Mark a field type for retention, and specify its new name. */
    public void retainFieldMap(String name, String obfName) throws Exception
    {
        this.retainItemMap(this.getFd(name), obfName);
    }

    /** Mark an item for retention, and specify its new name. */
    private void retainItemMap(TreeItem item, String obfName) throws Exception
    {
        if (!item.isFixed())
        {
            item.setOutName(obfName);
            item.setFromScriptMap();
        }
        else
        {
            System.out.println("# Trying to map fixed " + item.getFullInName() + " = " + item.getFullOutName() + " to " + obfName);
        }
    }

    /** Traverse the class tree, generating obfuscated names within each namespace. */
    public void generateNames(boolean enableRepackage) throws Exception
    {
        // Repackage first, if requested
        // (need TreeItem.isFixed set properly, so must be done first)
        if (enableRepackage)
        {
            // Generate single-level package names, unique across jar
            this.walkTree(new TreeAction()
            {
                @Override
                public void packageAction(Pk pk) throws Exception
                {
                    pk.repackageName();
                }
            });
        }
        // Now rename everything in the traditional way (no repackaging)
        this.walkTree(new TreeAction()
        {
            @Override
            public void packageAction(Pk pk) throws Exception
            {
                pk.generateNames();
            }

            @Override
            public void classAction(Cl cl) throws Exception
            {
                cl.generateNames();
            }
        });
    }

    /** Resolve the polymorphic dependencies of each class. */
    public void resolveClasses() throws Exception
    {
        this.walkTree(new TreeAction()
        {
            @Override
            public void classAction(Cl cl) throws Exception
            {
                cl.resetResolve();
            }
        });
        this.walkTree(new TreeAction()
        {
            @Override
            public void classAction(Cl cl) throws Exception
            {
                cl.setupNameListDowns();
            }
        });
        Cl.nameSpace = 0;
        this.walkTree(new TreeAction()
        {
            @Override
            public void classAction(Cl cl) throws Exception
            {
                cl.resolveOptimally();
            }
        });
    }

    /** Return a list of attributes marked to keep. */
    @Override
    public String[] getAttrsToKeep() throws Exception
    {
        String[] attrs = new String[this.retainAttrs.size()];
        for (int i = 0; i < attrs.length; i++)
        {
            attrs[i] = this.retainAttrs.elementAt(i);
        }
        return attrs;
    }

    /**
     * Get classes in tree from the fully qualified name
     * (can be wildcarded).
     */
    public Enumeration<Cl> getClEnum(String fullName) throws Exception
    {
        final Vector<Cl> vec = new Vector<Cl>();
        // Wildcard? then return list of all matching classes (including inner)
        if (fullName.indexOf('*') != -1)
        {
            // Old !a/b/* wildcard syntax, for backward compatibility
            // (acts as if every * becomes a ** in new-style match)
            if (fullName.indexOf('!') == 0)
            {
                final String fName = fullName.substring(1);
                this.walkTree(new TreeAction()
                {
                    @Override
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
                this.walkTree(new TreeAction()
                {
                    @Override
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
            Cl cl = this.getCl(fullName);
            if (cl != null)
            {
                vec.addElement(cl);
            }
        }
        return vec.elements();
    }

    /** Get methods in tree from the fully qualified, and possibly wildcarded, name. */
    public Enumeration<Md> getMdEnum(String fullName, String descriptor) throws Exception
    {
        final Vector<Md> vec = new Vector<Md>();
        final String fDesc = descriptor;
        // Wildcard? then return list of all matching methods
        if ((fullName.indexOf('*') != -1) || (descriptor.indexOf('*') != -1))
        {
            // Old !a/b/* wildcard syntax, for backward compatibility
            // (acts as if every * becomes a ** in new-style match)
            if (fullName.indexOf('!') == 0)
            {
                final String fName = fullName.substring(1);
                this.walkTree(new TreeAction()
                {
                    @Override
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
                this.walkTree(new TreeAction()
                {
                    @Override
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
            Md md = this.getMd(fullName, descriptor);
            if (md != null)
            {
                vec.addElement(md);
            }
        }
        return vec.elements();
    }

    /** Get fields in tree from the fully qualified, and possibly wildcarded, name. */
    public Enumeration<Fd> getFdEnum(String fullName, String descriptor) throws Exception
    {
        final Vector<Fd> vec = new Vector<Fd>();
        // Wildcard? then return list of all matching methods
        if (fullName.indexOf('*') != -1)
        {
            // Old !a/b/* wildcard syntax, for backward compatibility
            // (acts as if every * becomes a ** in new-style match)
            if (fullName.indexOf('!') == 0)
            {
                final String fName = fullName.substring(1);
                this.walkTree(new TreeAction()
                {
                    @Override
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
                this.walkTree(new TreeAction()
                {
                    @Override
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
            Fd fd = this.getFd(fullName);
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
        TreeItem ti = this.root;
        for (Enumeration<SimpleName> nameEnum = ClassTree.getNameEnum(fullName); nameEnum.hasMoreElements();)
        {
            SimpleName simpleName = nameEnum.nextElement();
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
            // 15Jul2005 - this exception is being over-sensitive with fullName of null (should never get here) so safely return
            // null instead
//            throw new Exception("Inconsistent class or interface name: " + fullName);
            return null;
        }
        return (Cl)ti;
    }

    /** Get package in tree from the fully qualified name, returning null if name not found. */
    public Pk getPk(String fullName) throws Exception
    {
        TreeItem ti = this.root;
        for (Enumeration<SimpleName> nameEnum = ClassTree.getNameEnum(fullName); nameEnum.hasMoreElements();)
        {
            SimpleName simpleName = nameEnum.nextElement();
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
        int pos = fullName.lastIndexOf(ClassTree.METHOD_FIELD_LEVEL);
        Cl cl = this.getCl(fullName.substring(0, pos));
        return cl.getMethod(fullName.substring(pos + 1), descriptor);
    }

    /** Get field in tree from the fully qualified name. */
    public Fd getFd(String fullName) throws Exception
    {
        // Split into class and field names
        int pos = fullName.lastIndexOf(ClassTree.METHOD_FIELD_LEVEL);
        Cl cl = this.getCl(fullName.substring(0, pos));
        return cl.getField(fullName.substring(pos + 1));
    }

    /**
     * Mapping for fully qualified class name.
     * 
     * @see NameMapper#mapClass
     */
    @Override
    public String mapClass(String className) throws Exception
    {
        // Check for array -- requires special handling
        if ((className.length() > 0) && (className.charAt(0) == '['))
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
                        newName.append(this.mapClass(className.substring(i, pos)));
                        i = pos;
                        break;

                    default:
                        return className;
                }
            }
            return newName.toString();
        }

        Cl cl = this.getCl(className);
        return cl != null ? cl.getFullOutName() : className;
    }

    /**
     * Mapping for method name, of fully qualified class.
     * 
     * @see NameMapper#mapMethod
     */
    @Override
    public String mapMethod(String className, String methodName, String descriptor) throws Exception
    {
        String outName = methodName;
        if (!methodName.equals("<init>"))
        {
            Stack<Cl> s = new Stack<Cl>();
            Cl nextCl = this.getCl(className);
            if (nextCl != null)
            {
                s.push(nextCl);
            }
            while (!s.empty())
            {
                Cl cl = s.pop();
                Md md = cl.getMethod(methodName, descriptor);
                if (md != null)
                {
                    outName = md.getOutName();
                    break;
                }

                nextCl = cl.getSuperCl();
                if (nextCl != null)
                {
                    s.push(nextCl);
                }
                Enumeration<Cl> enm = cl.getSuperInterfaces();
                while (enm.hasMoreElements())
                {
                    nextCl = enm.nextElement();
                    if (nextCl != null)
                    {
                        s.push(nextCl);
                    }
                }
            }
        }
        return outName;
    }

    /**
     * Mapping for field name, of fully qualified class.
     * 
     * @see NameMapper#mapField
     */
    @Override
    public String mapField(String className, String fieldName) throws Exception
    {
        String outName = fieldName;
        if (!fieldName.equals("<init>"))
        {
            Stack<Cl> s = new Stack<Cl>();
            Cl nextCl = this.getCl(className);
            if (nextCl != null)
            {
                s.push(nextCl);
            }
            while (!s.empty())
            {
                Cl cl = s.pop();
                Fd fd = cl.getField(fieldName);
                if (fd != null)
                {
                    outName = fd.getOutName();
                    break;
                }

                nextCl = cl.getSuperCl();
                if (nextCl != null)
                {
                    s.push(nextCl);
                }
                Enumeration<Cl> enm = cl.getSuperInterfaces();
                while (enm.hasMoreElements())
                {
                    nextCl = enm.nextElement();
                    if (nextCl != null)
                    {
                        s.push(nextCl);
                    }
                }
            }
        }
        return outName;
    }

    /**
     * Mapping for generic type signature.
     * 
     * @see NameMapper#mapSignature
     */
    @Override
    public String mapSignature(String signature) throws Exception
    {
        // NOTE - not currently parsed and mapped; reserve identifiers appearing in type signatures for reflective methods to work.
        return signature;
    }

    /**
     * Mapping for descriptor of field or method.
     * 
     * @see NameMapper#mapDescriptor
     */
    @Override
    public String mapDescriptor(String descriptor) throws Exception
    {
        // Pass everything through unchanged, except for the String between 'L' and ';' -- this is passed through mapClass(String)
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
                    newDesc.append(this.mapClass(descriptor.substring(i, pos)));
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
        log.println(ClassTree.LOG_PRE_UNOBFUSCATED);
        log.println("#");
        this.walkTree(new TreeAction()
        {
            @Override
            public void classAction(Cl cl)
            {
                if (cl.isFromScript())
                {
                    log.println(".class " + cl.getFullInName());
                }
            }

            @Override
            public void methodAction(Md md)
            {
                if (md.isFromScript())
                {
                    log.println(".method " + md.getFullInName() + " " + md.getDescriptor());
                }
            }

            @Override
            public void fieldAction(Fd fd)
            {
                if (fd.isFromScript())
                {
                    log.println(".field " + fd.getFullInName() + " " + fd.getDescriptor());
                }
            }

            @Override
            public void packageAction(Pk pk)
            {
                // No action
            }
        });
        log.println("#");
        log.println("#");
        log.println(ClassTree.LOG_PRE_OBFUSCATED);
        log.println("#");
        this.walkTree(new TreeAction()
        {
            @Override
            public void classAction(Cl cl)
            {
                if (!cl.isFromScript())
                {
                    log.println(".class_map " + cl.getFullInName() + " " + cl.getOutName());
                }
            }

            @Override
            public void methodAction(Md md)
            {
                if (!md.isFromScript())
                {
                    log.println(".method_map " + md.getFullInName() + " " + md.getDescriptor() + " " + md.getOutName());
                }
            }

            @Override
            public void fieldAction(Fd fd)
            {
                if (!fd.isFromScript())
                {
                    log.println(".field_map " + fd.getFullInName() + " " + fd.getOutName());
                }
            }

            @Override
            public void packageAction(Pk pk)
            {
                if (!pk.isFromScript() && (pk.getFullInName().length() > 0))
                {
                    if (pk.getRepackageName() != null)
                    {
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
    /** Mark TreeItem and all parents for retention. */
    private void retainHierarchy(TreeItem ti, boolean invert) throws Exception
    {
        if (invert)
        {
            // error to force package level obfuscation
            if (!(ti instanceof Pk))
            {
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
            this.retainHierarchy(ti.parent, invert);
        }
    }

    /** Walk the whole tree taking action once only on each package level, class, method and field. */
    public void walkTree(TreeAction ta) throws Exception
    {
        this.walkTree(ta, this.root);
    }

    /** Walk the tree which has TreeItem as its root taking action once only on each package level, class, method and field. */
    private void walkTree(TreeAction ta, TreeItem ti) throws Exception
    {
        if (ti instanceof Pk)
        {
            Enumeration packageEnum = ((Pk)ti).getPackageEnum();
            ta.packageAction((Pk)ti);
            while (packageEnum.hasMoreElements())
            {
                this.walkTree(ta, (TreeItem)packageEnum.nextElement());
            }
        }
        if (ti instanceof PkCl)
        {
            Enumeration classEnum = ((PkCl)ti).getClassEnum();
            while (classEnum.hasMoreElements())
            {
                this.walkTree(ta, (TreeItem)classEnum.nextElement());
            }
        }
        if (ti instanceof Cl)
        {
            Enumeration<Fd> fieldEnum = ((Cl)ti).getFieldEnum();
            Enumeration<Md> methodEnum = ((Cl)ti).getMethodEnum();
            ta.classAction((Cl)ti);
            while (fieldEnum.hasMoreElements())
            {
                ta.fieldAction(fieldEnum.nextElement());
            }
            while (methodEnum.hasMoreElements())
            {
                ta.methodAction(methodEnum.nextElement());
            }
        }
    }
}
