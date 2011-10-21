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

import org.objectweb.asm.signature.*;

import COM.rl.MapSignatureAdapter;
import COM.rl.NameProvider;
import COM.rl.obf.classfile.*;

/**
 * Tree structure of package levels, classes, methods and fields used for obfuscation.
 * 
 * @author Mark Welsh
 */
public class ClassTree implements NameMapper, ClassConstants
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
    /**
     * List of attributes to retain
     */
    private List<String> retainAttrs = new ArrayList<String>();

    /**
     * Root package in database (Java default package)
     */
    private Pk root = null;

    // Class methods ---------------------------------------------------------
    /**
     * Return a fully qualified name broken into package/class segments.
     * 
     * @param name
     * @throws ClassFileException
     */
    public static List<SimpleName> getNameList(String name) throws ClassFileException
    {
        List<SimpleName> list = new ArrayList<SimpleName>();
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
                    throw new ClassFileException("Invalid fully qualified name (a): " + nameOrig);
                }
            }
            list.add(simpleName);
        }
        return list;
    }


    // Instance Methods ------------------------------------------------------
    /**
     * Constructor
     */
    public ClassTree()
    {
        this.root = Pk.createRoot(this);
    }

    /**
     * Update the path of the passed filename, if that path corresponds to a package.
     * 
     * @param inName
     */
    public String getOutName(String inName)
    {
        try
        {
            TreeItem ti = this.root;
            StringBuilder sb = new StringBuilder();
            for (SimpleName simpleName : ClassTree.getNameList(inName))
            {
                String name = simpleName.getName();
                if (simpleName.isAsPackage())
                {
                    if (ti != null)
                    {
                        Pk pk = (Pk)ti;
                        ti = pk.getPackage(name);
                        if (ti != null)
                        {
                            Pk pk2 = (Pk)ti;
                            String repackageName = pk2.getRepackageName();
                            if (repackageName != null)
                            {
                                sb = new StringBuilder(repackageName);
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
                    throw new ClassFileException("Internal error: illegal package/class name tag");
                }
            }
        }
        catch (ClassFileException e)
        {
            // Just drop through and return the original name
        }
        return inName;
    }

    /**
     * Add a classfile's package, class, method and field entries to database.
     * 
     * @param cf
     * @throws ClassFileException
     */
    public void addClassFile(ClassFile cf) throws ClassFileException
    {
        // Add the fully qualified class name
        TreeItem ti = this.root;
        String className = cf.getName();
        for (Iterator<SimpleName> nameIter = ClassTree.getNameList(className).iterator(); nameIter.hasNext();)
        {
            SimpleName simpleName = nameIter.next();
            String name = simpleName.getName();
            if (simpleName.isAsPackage())
            {
                Pk pk = (Pk)ti;
                ti = pk.addPackage(name);
            }
            else if (simpleName.isAsClass())
            {
                PkCl pkCl = (PkCl)ti;
                // If this is an inner class, just add placeholder classes up the tree
                if (nameIter.hasNext())
                {
                    ti = pkCl.addPlaceholderClass(name);
                }
                else
                {
                    ti = pkCl.addClass(name, cf.getSuper(), cf.getInterfaces(), cf.getModifiers());
                }
            }
            else
            {
                throw new ClassFileException("Internal error: illegal package/class name tag");
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
            throw new ClassFileException("Inconsistent class file.");
        }
    }

    /**
     * Mark a class/interface type to suppress warnings from it.
     * 
     * @param name
     * @throws ClassFileException
     */
    public void noWarnClass(String name) throws ClassFileException
    {
        // Mark the class (or classes, if this is a wildcarded specifier)
        for (Cl cl : this.getClList(name))
        {
            cl.setNoWarn();
        }
    }

    /**
     * Write any non-suppressed warnings to the log.
     * 
     * @param log
     */
    public void logWarnings(PrintWriter log)
    {
        try
        {
            final List<Boolean> hasWarnings = new ArrayList<Boolean>();
            this.walkTree(new TreeAction()
            {
                @Override
                public void classAction(Cl cl)
                {
                    if (cl.hasWarnings())
                    {
                        hasWarnings.add(Boolean.valueOf(true));
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
                    public void classAction(Cl cl)
                    {
                        cl.logWarnings(flog);
                    }
                });
            }
        }
        catch (ClassFileException e)
        {
            // shouldn't get here
        }
    }

    /**
     * Mark an attribute type for retention.
     * 
     * @param name
     */
    public void retainAttribute(String name)
    {
        this.retainAttrs.add(name);
    }

    /**
     * Mark a class/interface type (and possibly methods and fields defined in class) for retention.
     * 
     * @param name
     * @param retainToPublic
     * @param retainToProtected
     * @param retainPubProtOnly
     * @param retainFieldsOnly
     * @param retainMethodsOnly
     * @param extendsName
     * @param invert
     * @param accessMask
     * @param accessSetting
     * @throws ClassFileException
     */
    public void retainClass(String name, boolean retainToPublic, boolean retainToProtected, boolean retainPubProtOnly,
        boolean retainFieldsOnly, boolean retainMethodsOnly, String extendsName, boolean invert, int accessMask, int accessSetting)
        throws ClassFileException
    {
        // Mark the class (or classes, if this is a wildcarded specifier)
        List<Cl> classes = this.getClList(name);
        if (classes.size() == 0)
        {
            throw new ClassFileException("ClassNotFound");
        }
        for (Cl cl : classes)
        {
            if (((extendsName == null) || cl.hasAsSuperOrInterface(extendsName))
                && cl.modifiersMatchMask(accessMask, accessSetting))
            {
                this.retainHierarchy(cl, invert);
                if (retainToPublic || retainToProtected || retainPubProtOnly)
                {
                    // Retain methods if requested
                    if (!retainFieldsOnly)
                    {
                        for (Md md : cl.getMethods())
                        {
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
                        for (Fd fd : cl.getFields())
                        {
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

    /**
     * Mark a method type for retention.
     * 
     * @param name
     * @param descriptor
     * @param retainAndClass
     * @param extendsName
     * @param invert
     * @param accessMask
     * @param accessSetting
     * @throws ClassFileException
     */
    public void retainMethod(String name, String descriptor, boolean retainAndClass, String extendsName, boolean invert,
        int accessMask, int accessSetting) throws ClassFileException
    {
        List<Md> methods = this.getMdList(name, descriptor);
        if (methods.size() == 0)
        {
            throw new ClassFileException("MethodNotFound");
        }
        for (Md md : methods)
        {
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

    /**
     * Mark a field type for retention.
     * 
     * @param name
     * @param descriptor
     * @param retainAndClass
     * @param extendsName
     * @param invert
     * @param accessMask
     * @param accessSetting
     * @throws ClassFileException
     */
    public void retainField(String name, String descriptor, boolean retainAndClass, String extendsName, boolean invert,
        int accessMask, int accessSetting) throws ClassFileException
    {
        List<Fd> fields = this.getFdList(name, descriptor);
        if (fields.size() == 0)
        {
            throw new ClassFileException("FieldNotFound");
        }
        for (Fd fd : fields)
        {
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

    /**
     * Mark a package for retention, and specify its new name.
     * 
     * @param name
     * @param obfName
     * @throws ClassFileException
     */
    public TreeItem retainPackageMap(String name, String obfName) throws ClassFileException
    {
        Pk pk = this.getPk(name);
        if (pk == null)
        {
            throw new ClassFileException("PackageNotFound");
        }
        return ClassTree.retainItemMap(pk, obfName);
    }

    /**
     * Mark a package for repackaging under this new name.
     * 
     * @param name
     * @param obfName
     * @throws ClassFileException
     */
    public TreeItem retainRepackageMap(String name, String obfName) throws ClassFileException
    {
        Pk pk = this.getPk(name);
        if (pk == null)
        {
            throw new ClassFileException("PackageNotFound");
        }
        if (!pk.isFixed())
        {
            pk.setRepackageName(obfName);
            pk.setOutName(pk.getInName());
        }
        return pk;
    }

    /**
     * Mark a class/interface type for retention, and specify its new name.
     * 
     * @param name
     * @param obfName
     * @throws ClassFileException
     */
    public TreeItem retainClassMap(String name, String obfName) throws ClassFileException
    {
        Cl cl = this.getCl(name);
        if (cl == null)
        {
            throw new ClassFileException("ClassNotFound");
        }
        return ClassTree.retainItemMap(cl, obfName);
    }

    /**
     * Mark a method type for retention, and specify its new name.
     * 
     * @param name
     * @param descriptor
     * @param obfName
     * @throws ClassFileException
     */
    public TreeItem retainMethodMap(String name, String descriptor, String obfName) throws ClassFileException
    {
        Md md = this.getMd(name, descriptor);
        if (md == null)
        {
            throw new ClassFileException("MethodNotFound");
        }
        return ClassTree.retainItemMap(md, obfName);
    }

    /**
     * Mark a field type for retention, and specify its new name.
     * 
     * @param name
     * @param obfName
     * @throws ClassFileException
     */
    public TreeItem retainFieldMap(String name, String obfName) throws ClassFileException
    {
        Fd fd = this.getFd(name);
        if (fd == null)
        {
            throw new ClassFileException("FieldNotFound");
        }
        return ClassTree.retainItemMap(fd, obfName);
    }

    /**
     * Mark an item for retention, and specify its new name.
     * 
     * @param item
     * @param obfName
     */
    private static TreeItem retainItemMap(TreeItem item, String obfName)
    {
        if (item.isFixed())
        {
            if (obfName.equals(""))
            {
                obfName = ".";
            }
            if (item.isFromScriptMap())
            {
                NameProvider.errorLog("# Trying to remap " + item.getFullInName(true) + " = " + item.getOutName(true)
                    + " to " + obfName + " fixed by ScriptMap");
            }
            else if (item.isFromScript())
            {
                NameProvider.errorLog("# Trying to remap " + item.getFullInName(true) + " = " + item.getOutName(true)
                    + " to " + obfName + " fixed by Script");
            }
            else
            {
                NameProvider.errorLog("# Trying to remap " + item.getFullInName(true) + " = " + item.getOutName(true)
                    + " to " + obfName);
            }
            return item;
        }

        item.setOutName(obfName);
        item.setFromScriptMap();
        return item;
    }

    /**
     * Traverse the class tree, generating obfuscated names within each namespace.
     * 
     * @throws ClassFileException
     */
    public void generateNames() throws ClassFileException
    {
        // Rename default package
        Map<String, Pk> rootMap = new HashMap<String, Pk>();
        rootMap.put("", this.root);
        PkCl.generateNames(rootMap);

        // Rename everything else
        this.walkTree(new TreeAction()
        {
            @Override
            public void packageAction(Pk pk) throws ClassFileException
            {
                pk.generateNames();
            }

            @Override
            public void classAction(Cl cl) throws ClassFileException
            {
                cl.generateNames();
            }
        });
    }

    /**
     * Resolve the polymorphic dependencies of each class.
     * 
     * @throws ClassFileException
     */
    public void resolveClasses() throws ClassFileException
    {
        this.walkTree(new TreeAction()
        {
            @Override
            public void classAction(Cl cl)
            {
                cl.resetResolve();
            }
        });
        this.walkTree(new TreeAction()
        {
            @Override
            public void classAction(Cl cl) throws ClassFileException
            {
                cl.setupNameListDowns();
            }
        });
        Cl.nameSpace = 0;
        this.walkTree(new TreeAction()
        {
            @Override
            public void classAction(Cl cl) throws ClassFileException
            {
                cl.resolveOptimally();
            }
        });
    }

    /**
     * Return a list of attributes marked to keep.
     */
    @Override
    public List<String> getAttrsToKeep()
    {
        return this.retainAttrs;
    }

    /**
     * Get classes in tree from the fully qualified name
     * (can be wildcarded).
     * 
     * @param fullName
     * @throws ClassFileException
     */
    public List<Cl> getClList(String fullName) throws ClassFileException
    {
        final List<Cl> list = new ArrayList<Cl>();
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
                    public void classAction(Cl cl)
                    {
                        if (cl.isOldStyleMatch(fName))
                        {
                            list.add(cl);
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
                    public void classAction(Cl cl)
                    {
                        if (cl.isWildcardMatch(fName))
                        {
                            list.add(cl);
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
                list.add(cl);
            }
        }
        return list;
    }

    /**
     * Get methods in tree from the fully qualified, and possibly wildcarded, name.
     * 
     * @param fullName
     * @param descriptor
     * @throws ClassFileException
     */
    public List<Md> getMdList(String fullName, String descriptor) throws ClassFileException
    {
        final List<Md> list = new ArrayList<Md>();
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
                    public void methodAction(Md md)
                    {
                        if (md.isOldStyleMatch(fName, fDesc))
                        {
                            list.add(md);
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
                    public void methodAction(Md md)
                    {
                        if (md.isWildcardMatch(fName, fDesc))
                        {
                            list.add(md);
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
                list.add(md);
            }
        }
        return list;
    }

    /**
     * Get fields in tree from the fully qualified, and possibly wildcarded, name.
     * 
     * @param fullName
     * @param descriptor
     * @throws ClassFileException
     */
    public List<Fd> getFdList(String fullName, String descriptor) throws ClassFileException
    {
        final List<Fd> list = new ArrayList<Fd>();
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
                    public void fieldAction(Fd fd)
                    {
                        if (fd.isOldStyleMatch(fName))
                        {
                            list.add(fd);
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
                    public void fieldAction(Fd fd)
                    {
                        if (fd.isWildcardMatch(fName, fDesc))
                        {
                            list.add(fd);
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
                list.add(fd);
            }
        }
        return list;
    }

    /**
     * Get class in tree from the fully qualified name, returning null if name not found.
     * 
     * @param fullName
     * @throws ClassFileException
     */
    public Cl getCl(String fullName) throws ClassFileException
    {
        TreeItem ti = this.root;
        for (SimpleName simpleName : ClassTree.getNameList(fullName))
        {
            String name = simpleName.getName();
            if (simpleName.isAsPackage())
            {
                Pk pk = (Pk)ti;
                ti = pk.getPackage(name);
            }
            else if (simpleName.isAsClass())
            {
                PkCl pkCl = (PkCl)ti;
                ti = pkCl.getClass(name);
            }
            else
            {
                throw new ClassFileException("Internal error: illegal package/class name tag");
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
//            throw new ClassFileException("Inconsistent class or interface name: " + fullName);
            return null;
        }
        return (Cl)ti;
    }

    /**
     * Get package in tree from the fully qualified name, returning null if name not found.
     * 
     * @param fullName
     * @throws ClassFileException
     */
    public Pk getPk(String fullName) throws ClassFileException
    {
        TreeItem ti = this.root;
        for (SimpleName simpleName : ClassTree.getNameList(fullName))
        {
            String name = simpleName.getName();
            Pk pk = (Pk)ti;
            ti = pk.getPackage(name);

            // If the name is not in the database, return null
            if (ti == null)
            {
                return null;
            }
            // It is an error if we do not end up with a package
            if (!(ti instanceof Pk))
            {
                throw new ClassFileException("Inconsistent package.");
            }
        }
        return (Pk)ti;
    }

    /**
     * Get method in tree from the fully qualified name.
     * 
     * @param fullName
     * @param descriptor
     * @throws ClassFileException
     */
    public Md getMd(String fullName, String descriptor) throws ClassFileException
    {
        // Split into class and method names
        int pos = fullName.lastIndexOf(ClassTree.METHOD_FIELD_LEVEL);
        String className = fullName.substring(0, pos);
        String methodName = fullName.substring(pos + 1);
        Cl cl = this.getCl(className);
        if (cl == null)
        {
            return null;
        }
        return cl.getMethod(methodName, descriptor);
    }

    /**
     * Get field in tree from the fully qualified name.
     * 
     * @param fullName
     * @throws ClassFileException
     */
    public Fd getFd(String fullName) throws ClassFileException
    {
        // Split into class and field names
        int pos = fullName.lastIndexOf(ClassTree.METHOD_FIELD_LEVEL);
        String className = fullName.substring(0, pos);
        String fieldName = fullName.substring(pos + 1);
        Cl cl = this.getCl(className);
        if (cl == null)
        {
            return null;
        }
        return cl.getField(fieldName);
    }

    /**
     * Mapping for fully qualified class name.
     * 
     * @throws ClassFileException
     * @see NameMapper#mapClass
     */
    @Override
    public String mapClass(String className) throws ClassFileException
    {
        // Check for array -- requires special handling
        if ((className.length() > 0) && (className.charAt(0) == '['))
        {
            StringBuilder newName = new StringBuilder();
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
                            throw new ClassFileException("Invalid class name encountered: " + className);
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
        if (cl != null)
        {
            return cl.getFullOutName();
        }

        return className;
    }

    /**
     * Mapping for method name, of fully qualified class.
     * 
     * @throws ClassFileException
     * @see NameMapper#mapMethod
     */
    @Override
    public String mapMethod(String className, String methodName, String descriptor) throws ClassFileException
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

                Cl superCl = cl.getSuperCl();
                if (superCl != null)
                {
                    s.push(superCl);
                }
                for (Cl intfCl : cl.getSuperInterfaces())
                {
                    if (intfCl != null)
                    {
                        s.push(intfCl);
                    }
                }
            }
        }
        return outName;
    }

    /**
     * Mapping for field name, of fully qualified class.
     * 
     * @throws ClassFileException
     * @see NameMapper#mapField
     */
    @Override
    public String mapField(String className, String fieldName) throws ClassFileException
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

                Cl superCl = cl.getSuperCl();
                if (superCl != null)
                {
                    s.push(superCl);
                }
                for (Cl intfCl : cl.getSuperInterfaces())
                {
                    if (intfCl != null)
                    {
                        s.push(intfCl);
                    }
                }
            }
        }
        return outName;
    }

    private String mapSignature(String signature, AttrSource source) throws ClassFileException
    {
        SignatureWriter sw = new SignatureWriter();
        SignatureVisitor sa = new MapSignatureAdapter(sw, this);
        SignatureReader sr = new SignatureReader(signature);
        try
        {
            switch (source)
            {
                case CLASS:
                    sr.acceptClassType(sa);
                    break;

                case METHOD:
                    sr.acceptMethodType(sa);
                    break;

                case FIELD:
                    sr.acceptFieldType(sa);
                    break;

                default:
                    throw new ClassFileException("Invalid attribute source");
            }

        }
        catch (SignatureException e)
        {
            if (e.getCause() instanceof ClassFileException)
            {
                throw (ClassFileException)e.getCause();
            }
            throw new ClassFileException(e);
        }
        String newSig = sw.toString();
        return newSig;
    }

    /**
     * Mapping for generic type signature of class.
     * 
     * @throws ClassFileException
     * @see NameMapper#mapSignatureClass
     */
    @Override
    public String mapSignatureClass(String signature) throws ClassFileException
    {
        String newSig = this.mapSignature(signature, AttrSource.CLASS);
        System.err.println("!c " + signature + " => " + newSig);
        return newSig;
    }

    /**
     * Mapping for generic type signature of method.
     * 
     * @throws ClassFileException
     * @see NameMapper#mapSignatureMethod
     */
    @Override
    public String mapSignatureMethod(String signature) throws ClassFileException
    {
        String newSig = this.mapSignature(signature, AttrSource.METHOD);
        System.err.println("!m " + signature + " => " + newSig);
        return newSig;
    }

    /**
     * Mapping for generic type signature of field.
     * 
     * @throws ClassFileException
     * @see NameMapper#mapSignatureField
     */
    @Override
    public String mapSignatureField(String signature) throws ClassFileException
    {
        String newSig = this.mapSignature(signature, AttrSource.FIELD);
        System.err.println("!f " + signature + " => " + newSig);
        return newSig;
    }

    /**
     * Mapping for descriptor of field or method.
     * 
     * @throws ClassFileException
     * @see NameMapper#mapDescriptor
     */
    @Override
    public String mapDescriptor(String descriptor) throws ClassFileException
    {
        // Pass everything through unchanged, except for the String between 'L' and ';' -- this is passed through mapClass(String)
        StringBuilder newDesc = new StringBuilder();
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
                        throw new ClassFileException("Invalid descriptor string encountered.");
                    }
                    newDesc.append(this.mapClass(descriptor.substring(i, pos)));
                    i = pos;
                    break;

                default:
                    throw new ClassFileException("Invalid descriptor string encountered.");
            }
        }
        return newDesc.toString();
    }

    /**
     * Dump the content of the class tree to the specified file (used for logging).
     * 
     * @param log
     */
    public void dump(final PrintWriter log)
    {
        try
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
                        log.println(RgsEntryType.CLASS + " " + cl.getFullInName());
                    }
                }

                @Override
                public void methodAction(Md md)
                {
                    if (md.isFromScript())
                    {
                        log.println(RgsEntryType.METHOD + " " + md.getFullInName() + " " + md.getDescriptor());
                    }
                }

                @Override
                public void fieldAction(Fd fd)
                {
                    if (fd.isFromScript())
                    {
                        log.println(RgsEntryType.FIELD + " " + fd.getFullInName() + " " + fd.getDescriptor());
                    }
                }

                /**
                 * @param pk
                 */
                @Override
                public void packageAction(Pk pk)
                {
                    // do nothing
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
                        log.println(RgsEntryType.CLASS_MAP + " " + cl.getFullInName() + " " + cl.getOutName());
                    }
                }

                @Override
                public void methodAction(Md md)
                {
                    if (!md.isFromScript())
                    {
                        log.println(RgsEntryType.METHOD_MAP + " " + md.getFullInName() + " " + md.getDescriptor() + " "
                            + md.getOutName());
                    }
                }

                @Override
                public void fieldAction(Fd fd)
                {
                    if (!fd.isFromScript())
                    {
                        log.println(RgsEntryType.FIELD_MAP + " " + fd.getFullInName() + " " + fd.getOutName());
                    }
                }

                @Override
                public void packageAction(Pk pk)
                {
                    if (!pk.isFromScript())
                    {
                        if (pk.getRepackageName() != null)
                        {
                            log.println(RgsEntryType.REPACKAGE_MAP + " " + pk.getFullInName(true) + " " + pk.getRepackageName(true));
                        }
                        else
                        {
                            log.println(RgsEntryType.PACKAGE_MAP + " " + pk.getFullInName(true) + " " + pk.getOutName(true));
                        }
                    }
                }
            });

            this.walkTree(new TreeAction()
            {
                @Override
                public void classAction(Cl cl)
                {
                    if (cl.isOutput())
                    {
                        NameProvider.outputClass(cl);
                    }
                }

                @Override
                public void methodAction(Md md)
                {
                    if (md.isOutput())
                    {
                        NameProvider.outputMethod(md);
                    }
                }

                @Override
                public void fieldAction(Fd fd)
                {
                    if (fd.isOutput())
                    {
                        NameProvider.outputField(fd);
                    }
                }

                @Override
                public void packageAction(Pk pk)
                {
                    if (pk.isOutput())
                    {
                        NameProvider.outputPackage(pk);
                    }
                }
            });
        }
        catch (ClassFileException e)
        {
            // shouldn't get here
        }
    }


    // Private Methods -------------------------------------------------------
    /**
     * Mark TreeItem and all parents for retention.
     * 
     * @param ti
     * @param invert
     */
    private void retainHierarchy(TreeItem ti, boolean invert)
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

    /**
     * Walk the whole tree taking action once only on each package level, class, method and field.
     * 
     * @param ta
     * @throws ClassFileException
     */
    public void walkTree(TreeAction ta) throws ClassFileException
    {
        this.walkTree(ta, this.root);
    }

    /**
     * Walk the tree which has TreeItem as its root taking action once only on each package level, class, method and field.
     * 
     * @param ta
     * @param ti
     * @throws ClassFileException
     */
    private void walkTree(TreeAction ta, TreeItem ti) throws ClassFileException
    {
        if (ti instanceof Pk)
        {
            Pk pk = (Pk)ti;
            Collection<Pk> packages = pk.getPackages();
            ta.packageAction(pk);
            for (TreeItem tiPk : packages)
            {
                this.walkTree(ta, tiPk);
            }
        }
        if (ti instanceof PkCl)
        {
            PkCl pkCl = (PkCl)ti;
            Collection<Cl> classes = pkCl.getClasses();
            for (TreeItem tiCl : classes)
            {
                this.walkTree(ta, tiCl);
            }
        }
        if (ti instanceof Cl)
        {
            Cl cl = (Cl)ti;
            Collection<Fd> fields = cl.getFields();
            Collection<Md> methods = cl.getMethods();
            ta.classAction(cl);
            for (Fd fd : fields)
            {
                ta.fieldAction(fd);
            }
            for (Md md : methods)
            {
                ta.methodAction(md);
            }
        }
    }
}
