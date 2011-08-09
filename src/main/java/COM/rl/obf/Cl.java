/* ===========================================================================
 * $RCSfile: Cl.java,v $
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

import COM.rl.NameProvider;
import COM.rl.util.*;
import COM.rl.obf.classfile.*;

/**
 * Tree item representing a class or interface.
 * 
 * @author Mark Welsh
 */
public class Cl extends PkCl implements NameListUp, NameListDown
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    /** Owns a list of methods */
    private Map mds = new HashMap();

    /** Owns a list of special methods */
    private Map mdsSpecial = new HashMap();

    /** Owns a list of fields */
    private Map fds = new HashMap();

    /** Has the class been resolved already? */
    private boolean isResolved = false;

    /** Has the class been scanned already? */
    private boolean isScanned = false;

    /** Our superclass name */
    private String superClass;

    /** Names of implemented interfaces */
    private List superInterfaces;

    /** Is this an inner class? */
    private boolean isInnerClass;

    /** NameListUp interfaces for super-class/interfaces */
    private List nameListUps = new ArrayList();

    /** NameListDown interfaces for derived class/interfaces */
    private List nameListDowns = new ArrayList();

    /** Are danger-method warnings suppressed? */
    private boolean isNoWarn = false;

    /** Danger-method warnings */
    private List warningList = new ArrayList();

    public static int nameSpace = 0;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /**
     * Ctor.
     * 
     * @throws ClassFileException
     */
    public Cl(TreeItem parent, boolean isInnerClass, String name, String superClass, List superInterfaces, int access)
        throws ClassFileException

    {
        super(parent, name);
        this.superClass = superClass;
        this.superInterfaces = superInterfaces;
        this.isInnerClass = isInnerClass;
        this.access = access;
        if ((parent == null) || "".equals(name))
        {
            throw new ClassFileException("# Internal error: class must have parent and name");
        }
        if (parent instanceof Cl)
        {
            this.sep = ClassFile.SEP_INNER;
        }

        // Do not obfuscate anonymous inner classes
        if (isInnerClass && (name != null) && (name.length() > 0) && Character.isDigit(name.charAt(0)))
        {
            this.setOutName(this.getInName());
        }
    }

    /** Is this an inner class? */
    public boolean isInnerClass()
    {
        return this.isInnerClass;
    }

    /** Suppress warnings. */
    public void setNoWarn()
    {
        this.isNoWarn = true;
    }

    /**
     * Add class's warning.
     * 
     * @throws ClassFileException
     */
    public void setWarnings(ClassFile cf) throws ClassFileException
    {
        this.warningList = cf.listDangerMethods(this.warningList);
    }

    /** Do we have non-suppressed warnings? */
    public boolean hasWarnings()
    {
        return (!this.isNoWarn && (this.warningList.size() > 0));
    }

    /** Log this class's warnings. */
    public void logWarnings(PrintWriter log)
    {
        if (this.hasWarnings())
        {
            for (Iterator iter = this.warningList.iterator(); iter.hasNext();)
            {
                String warning = (String)iter.next();
                log.println("# " + warning);
            }
        }
    }

    /** Get a method by name. */
    public Md getMethod(String name, String descriptor)
    {
        return (Md)this.mds.get(name + descriptor);
    }

    /** Get a special method by name. */
    public Md getMethodSpecial(String name, String descriptor)
    {
        return (Md)this.mdsSpecial.get(name + descriptor);
    }

    /** Get all methods with obfuscated name. */
    public Iterator getObfMethods(String name)
    {
        List mdsMatch = new ArrayList();
        for (Iterator iter = this.mds.values().iterator(); iter.hasNext();)
        {
            Md md = (Md)iter.next();
            if (name.equals(md.getOutName()))
            {
                mdsMatch.add(md);
            }
        }
        return mdsMatch.iterator();
    }

    /** Get a field by name. */
    public Fd getField(String name)
    {
        return (Fd)this.fds.get(name);
    }

    /** Get an Collection of methods. */
    public Iterator getMethodIter()
    {
        return this.mds.values().iterator();
    }

    /** Get an Collection of fields. */
    public Iterator getFieldIter()
    {
        return this.fds.values().iterator();
    }

    /**
     * Return this Cl's superclass Cl
     * 
     * @throws ClassFileException
     */
    public Cl getSuperCl() throws ClassFileException
    {
        if (this.superClass != null)
        {
            return this.classTree.getCl(this.superClass);
        }

        return null;
    }

    /**
     * Return Iterator of this Cl's super-interfaces
     * 
     * @throws ClassFileException
     */
    public Iterator getSuperInterfaces() throws ClassFileException
    {
        List list = new ArrayList();
        for (Iterator iter = this.superInterfaces.iterator(); iter.hasNext();)
        {
            String si = (String)iter.next();
            Cl interfaceItem = this.classTree.getCl(si);
            if (interfaceItem != null)
            {
                list.add(interfaceItem);
            }
        }
        return list.iterator();
    }

    /** Does this internal class have the specified class or interface in its super or interface chain? */
    protected boolean hasAsSuperInt(String queryName, boolean checkInterfaces)
    {
        try
        {
            // Special case: is this java/lang/Object?
            if (this.superClass == null)
            {
                return false;
            }
            // Check our parents
            if (this.superClass.equals(queryName))
            {
                return true;
            }
            if (checkInterfaces)
            {
                for (Iterator iter = this.superInterfaces.iterator(); iter.hasNext();)
                {
                    String si = (String)iter.next();
                    if (queryName.equals(si))
                    {
                        return true;
                    }
                }
            }
            // Nothing, so recurse up through parents
            Cl superClInt = this.classTree.getCl(this.superClass);
            if (superClInt != null)
            {
                if (superClInt.hasAsSuperInt(queryName, checkInterfaces))
                {
                    return true;
                }
            }
            else
            {
                Class superClExt = Class.forName(ClassFile.translate(this.superClass));
                if (superClExt != null)
                {
                    if (Cl.hasAsSuperExt(queryName, checkInterfaces, this.classTree, superClExt))
                    {
                        return true;
                    }
                }
            }
            if (checkInterfaces)
            {
                for (Iterator iter = this.superInterfaces.iterator(); iter.hasNext();)
                {
                    String si = (String)iter.next();
                    Cl interClInt = this.classTree.getCl(si);
                    if (interClInt != null)
                    {
                        if (interClInt.hasAsSuperInt(queryName, checkInterfaces))
                        {
                            return true;
                        }
                    }
                    else
                    {
                        Class interClExt = Class.forName(ClassFile.translate(si));
                        if (interClExt != null)
                        {
                            if (Cl.hasAsSuperExt(queryName, checkInterfaces, this.classTree, interClExt))
                            {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        catch (ClassNotFoundException e)
        {
            // TODO printStackTrace
            e.printStackTrace();
            // fall thru
        }
        catch (ClassFileException e)
        {
            // TODO printStackTrace
            e.printStackTrace();
            // fall thru
        }
        // TODO check for missed exceptions
//        catch (Exception e)
//        {
//            // fall thru
//        }
        return false;
    }

    /** Does this class have the specified class or interface in its super or interface chain? */
    protected static boolean hasAsSuperExt(String queryName, boolean checkInterfaces, ClassTree classTree, Class clExt)
    {
        try
        {
            // Special case: is this java/lang/Object?
            if ((clExt == null) || clExt.getName().equals("java.lang.Object"))
            {
                return false;
            }
            // Check our parents
            String queryNameExt = ClassFile.translate(queryName);
            Class superClass = clExt.getSuperclass();
            List superInterfaces = Arrays.asList(clExt.getInterfaces());
            if (queryNameExt.equals(superClass.getName()))
            {
                return true;
            }
            if (checkInterfaces)
            {
                for (Iterator iter = superInterfaces.iterator(); iter.hasNext();)
                {
                    Class si = (Class)iter.next();
                    if (queryNameExt.equals(si.getName()))
                    {
                        return true;
                    }
                }
            }
            // Nothing, so recurse up through parents
            Cl superClInt = classTree.getCl(ClassFile.backTranslate(superClass.getName()));
            if (superClInt != null)
            {
                if (superClInt.hasAsSuperInt(queryName, checkInterfaces))
                {
                    return true;
                }
            }
            else
            {
                Class superClExt = superClass;
                if (superClExt != null)
                {
                    if (Cl.hasAsSuperExt(queryName, checkInterfaces, classTree, superClExt))
                    {
                        return true;
                    }
                }
            }
            if (checkInterfaces)
            {
                for (Iterator iter = superInterfaces.iterator(); iter.hasNext();)
                {
                    Class si = (Class)iter.next();
                    Cl interClInt = classTree.getCl(ClassFile.backTranslate(si.getName()));
                    if (interClInt != null)
                    {
                        if (interClInt.hasAsSuperInt(queryName, checkInterfaces))
                        {
                            return true;
                        }
                    }
                    else
                    {
                        Class interClExt = si;
                        if (interClExt != null)
                        {
                            if (Cl.hasAsSuperExt(queryName, checkInterfaces, classTree, interClExt))
                            {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        catch (ClassFileException e)
        {
            // TODO printStackTrace
            e.printStackTrace();
            // fall thru
        }
        // TODO check for missed exceptions
//        catch (Exception e)
//        {
//            // fall thru
//        }
        return false;
    }

    /** Does this class have the specified class or interface in its super or interface chain? */
    public boolean hasAsSuperOrInterface(String queryName)
    {
        return this.hasAsSuperInt(queryName, true);
    }

    /** Does this class have the specified class in its super chain? */
    public boolean hasAsSuper(String queryName)
    {
        return this.hasAsSuperInt(queryName, false);
    }

    /**
     * Add an inner class.
     * 
     * @throws ClassFileException
     */
    @Override
    public Cl addClass(String name, String superName, List interfaceNames, int access) throws ClassFileException
    {
        return this.addClass(true, name, superName, interfaceNames, access);
    }

    /** Add an inner class, used when copying inner classes from a placeholder. */
    public Cl addClass(Cl cl)
    {
        this.cls.put(cl.getInName(), cl);
        return cl;
    }

    /**
     * Add a placeholder class.
     * 
     * @throws ClassFileException
     */
    @Override
    public Cl addPlaceholderClass(String name) throws ClassFileException
    {
        return this.addPlaceholderClass(true, name);
    }

    /**
     * Add a method.
     * 
     * @throws ClassFileException
     */
    public Md addMethod(ClassFile cf, MethodInfo methodInfo) throws ClassFileException
    {
        Md md = this.addMethod(methodInfo.isSynthetic(), methodInfo.getName(), methodInfo.getDescriptor(),
            methodInfo.getAccessFlags());
        return md;
    }

    /**
     * Add a method.
     * 
     * @throws ClassFileException
     */
    public Md addMethod(boolean isSynthetic, String name, String descriptor, int accessFlags) throws ClassFileException
    {
        // Store <init> and <clinit> methods separately - needed only for reference tracking
        Md md;
        if (name.charAt(0) == '<')
        {
            md = this.getMethodSpecial(name, descriptor);
            if (md == null)
            {
                md = new Md(this, isSynthetic, name, descriptor, accessFlags);
                this.mdsSpecial.put(name + descriptor, md);
            }
        }
        else
        {
            md = this.getMethod(name, descriptor);
            if (md == null)
            {
                md = new Md(this, isSynthetic, name, descriptor, accessFlags);
                this.mds.put(name + descriptor, md);
            }
        }
        return md;
    }

    /**
     * Add a field.
     * 
     * @throws ClassFileException
     */
    public Fd addField(ClassFile cf, FieldInfo fieldInfo) throws ClassFileException
    {
        Fd fd = this.addField(fieldInfo.isSynthetic(), fieldInfo.getName(), fieldInfo.getDescriptor(), fieldInfo.getAccessFlags());
        return fd;
    }

    /**
     * Add a field.
     * 
     * @throws ClassFileException
     */
    public Fd addField(boolean isSynthetic, String name, String descriptor, int access) throws ClassFileException
    {
        Fd fd = this.getField(name);
        if (fd == null)
        {
            fd = new Fd(this, isSynthetic, name, descriptor, access);
            this.fds.put(name, fd);
        }
        return fd;
    }

    /** Prepare for resolve of a class entry by resetting flags. */
    public void resetResolve()
    {
        this.isScanned = false;
        this.isResolved = false;
        this.nameListDowns.clear();
    }

    /**
     * Set up reverse list of reserved names prior to resolving classes.
     * 
     * @throws ClassFileException
     */
    public void setupNameListDowns() throws ClassFileException
    {
        // Special case: we are java/lang/Object
        if (this.superClass == null)
        {
            return;
        }

        // Add this class as a NameListDown to the super and each interface, if they are in the JAR
        Cl superClassItem = this.classTree.getCl(this.superClass);
        if (superClassItem != null)
        {
            superClassItem.nameListDowns.add(this);
        }
        for (Iterator iter = this.superInterfaces.iterator(); iter.hasNext();)
        {
            String si = (String)iter.next();
            Cl interfaceItem = this.classTree.getCl(si);
            if (interfaceItem != null)
            {
                interfaceItem.nameListDowns.add(this);
            }
        }
    }

    /**
     * Resolve a class entry - set obfuscation permissions based on super class and interfaces.
     * Overload method and field names maximally.
     * 
     * @throws ClassFileException
     * @throws ClassNotFoundException
     */
    public void resolveOptimally() throws ClassFileException, ClassNotFoundException
    {
        // Already processed, then do nothing
        if (!this.isResolved)
        {
            // Get lists of method and field names in inheritance namespace
            List methods = new ArrayList();
            List fields = new ArrayList();
            this.scanNameSpaceExcept(null, methods, fields);

            // Resolve a full name space
            this.resolveNameSpaceExcept(null);

            // and move to next
            Cl.nameSpace++;
        }
    }

    /**
     * Get lists of method and field names in inheritance namespace
     * 
     * @throws ClassFileException
     * @throws ClassNotFoundException
     */
    private void scanNameSpaceExcept(Cl ignoreCl, List methods, List fields) throws ClassFileException, ClassNotFoundException
    {
        // Special case: we are java/lang/Object
        if (this.superClass == null)
        {
            return;
        }

        // Traverse one step in each direction in name space, scanning
        if (!this.isScanned)
        {
            // First step up to super classes, scanning them
            Cl superCl = this.classTree.getCl(this.superClass);
            if (superCl != null)
            {
                // internal to JAR
                if (superCl != ignoreCl)
                {
                    superCl.scanNameSpaceExcept(this, methods, fields);
                }
            }
            else
            {
                // external to JAR
                this.scanExtSupers(this.superClass, methods, fields);
            }
            for (Iterator iter = this.superInterfaces.iterator(); iter.hasNext();)
            {
                String si = (String)iter.next();
                Cl interfaceItem = this.classTree.getCl(si);
                if ((interfaceItem != null) && (interfaceItem != ignoreCl))
                {
                    interfaceItem.scanNameSpaceExcept(this, methods, fields);
                }
            }

            // Next, scan ourself
            if (!this.isScanned)
            {
                this.scanThis(methods, fields);

                // Signal class has been scanned
                this.isScanned = true;
            }

            // Finally step down to derived classes, resolving them
            for (Iterator clIter = this.nameListDowns.iterator(); clIter.hasNext();)
            {
                Cl cl = (Cl)clIter.next();
                if (cl != ignoreCl)
                {
                    cl.scanNameSpaceExcept(this, methods, fields);
                }
            }
        }
    }

    /**
     * Get lists of method and field names in inheritance namespace
     * 
     * @throws ClassNotFoundException
     */
    private void scanExtSupers(String name, List methods, List fields) throws ClassNotFoundException
    {
        Class extClass = Class.forName(ClassFile.translate(name));

        // Get public methods and fields from supers and interfaces up the tree
        List allPubMethods = Arrays.asList(extClass.getMethods());
        for (Iterator iter = allPubMethods.iterator(); iter.hasNext();)
        {
            Method md = (Method)iter.next();
            String methodName = md.getName();
            if (!methods.contains(methodName))
            {
                methods.add(methodName);
            }
        }
        List allPubFields = Arrays.asList(extClass.getFields());
        for (Iterator iter = allPubFields.iterator(); iter.hasNext();)
        {
            Field fd = (Field)iter.next();
            String fieldName = fd.getName();
            if (!fields.contains(fieldName))
            {
                fields.add(fieldName);
            }
        }
        // Go up the super hierarchy, adding all non-public methods/fields
        while (extClass != null)
        {
            List allClassMethods = Arrays.asList(extClass.getDeclaredMethods());
            for (Iterator iter = allClassMethods.iterator(); iter.hasNext();)
            {
                Method md = (Method)iter.next();
                if (!Modifier.isPublic(md.getModifiers()))
                {
                    String methodName = md.getName();
                    if (!methods.contains(methodName))
                    {
                        methods.add(methodName);
                    }
                }
            }
            List allClassFields = Arrays.asList(extClass.getDeclaredFields());
            for (Iterator iter = allClassFields.iterator(); iter.hasNext();)
            {
                Field fd = (Field)iter.next();
                if (!Modifier.isPublic(fd.getModifiers()))
                {
                    String fieldName = fd.getName();
                    if (!fields.contains(fieldName))
                    {
                        fields.add(fieldName);
                    }
                }
            }
            extClass = extClass.getSuperclass();
        }
    }

    /** Add method and field names from this class to the lists */
    private void scanThis(List methods, List fields)
    {
        for (Iterator mdIter = this.mds.values().iterator(); mdIter.hasNext();)
        {
            Md md = (Md)mdIter.next();
            if (md.isFixed())
            {
                String name = md.getOutName();
                if (!methods.contains(name))
                {
                    methods.add(name);
                }
            }
        }
        for (Iterator fdIter = this.fds.values().iterator(); fdIter.hasNext();)
        {
            Fd fd = (Fd)fdIter.next();
            if (fd.isFixed())
            {
                String name = fd.getOutName();
                if (!fields.contains(name))
                {
                    fields.add(name);
                }
            }
        }
    }

    /**
     * Resolve an entire inheritance name space optimally.
     * 
     * @throws ClassFileException
     * @throws ClassNotFoundException
     */
    private void resolveNameSpaceExcept(Cl ignoreCl) throws ClassFileException, ClassNotFoundException
    {
        // Special case: we are java/lang/Object
        if (this.superClass == null)
        {
            return;
        }

        // Traverse one step in each direction in name space, resolving
        if (!this.isResolved)
        {
            // First step up to super classes, resolving them, since we depend on them
            Cl superCl = this.classTree.getCl(this.superClass);
            if ((superCl != null) && (superCl != ignoreCl))
            {
                superCl.resolveNameSpaceExcept(this);
            }
            for (Iterator iter = this.superInterfaces.iterator(); iter.hasNext();)
            {
                String si = (String)iter.next();
                Cl interfaceItem = this.classTree.getCl(si);
                if ((interfaceItem != null) && (interfaceItem != ignoreCl))
                {
                    interfaceItem.resolveNameSpaceExcept(this);
                }
            }

            // Next, resolve ourself
            if (!this.isResolved)
            {
                this.resolveThis();

                // Signal class has been processed
                this.isResolved = true;
            }

            // Finally step down to derived classes, resolving them
            for (Iterator clIter = this.nameListDowns.iterator(); clIter.hasNext();)
            {
                Cl cl = (Cl)clIter.next();
                if (cl != ignoreCl)
                {
                    cl.resolveNameSpaceExcept(this);
                }
            }
        }
    }

    /**
     * For each super interface and the super class, if it is outside DB, use reflection to merge its list of public/protected
     * methods/fields -- while for those in the DB, resolve to get the name-mapping lists
     * 
     * @throws ClassFileException
     * @throws ClassNotFoundException
     */
    private void resolveThis() throws ClassFileException, ClassNotFoundException
    {
        // Special case: we are java/lang/Object
        if (this.superClass == null)
        {
            return;
        }

        Cl superClassItem = this.classTree.getCl(this.superClass);
        this.nameListUps.add(superClassItem != null ? (NameListUp)superClassItem : this.getExtNameListUp(this.superClass));
        for (Iterator iter = this.superInterfaces.iterator(); iter.hasNext();)
        {
            String si = (String)iter.next();
            Cl interfaceItem = this.classTree.getCl(si);
            this.nameListUps.add(interfaceItem != null ? (NameListUp)interfaceItem : this.getExtNameListUp(si));
        }

        // Run through each method/field in this class checking for reservations and obfuscating accordingly
        nextMethod:
        for (Iterator mdIter = this.mds.values().iterator(); mdIter.hasNext();)
        {
            Md md = (Md)mdIter.next();
            if (!md.isFixed())
            {
                // Check for name reservation via derived classes
                for (Iterator nlIter = this.nameListDowns.iterator(); nlIter.hasNext();)
                {
                    NameListDown nl = (NameListDown)nlIter.next();
                    String theOutName = nl.getMethodObfNameDown(this, md.getInName(), md.getDescriptor());
                    if (theOutName != null)
                    {
                        md.setOutName(theOutName);
                        System.out.println("# Method " + md.getFullInName() + " renamed to " + md.getOutName()
                            + " because of derived class.");
                        continue nextMethod;
                    }
                }
                // Check for name reservation via super classes
                for (Iterator nlIter = this.nameListUps.iterator(); nlIter.hasNext();)
                {
                    NameListUp nl = (NameListUp)nlIter.next();
                    String theOutName = nl.getMethodOutNameUp(md.getInName(), md.getDescriptor());
                    if (theOutName != null)
                    {
                        md.setOutName(theOutName);
                        md.setIsOverride();
                        System.out.println("# Method " + md.getFullInName() + " renamed to " + md.getOutName()
                            + " because of super class.");
                        continue nextMethod;
                    }
                }
                // If no other restrictions, obfuscate it
                String theOutName = NameProvider.getNewMethodName(md);
                if (theOutName != null)
                {
                    md.setOutName(theOutName);
                    System.out.println("# Method " + md.getFullInName() + " renamed to " + md.getOutName() + " from name maker.");
                }
            }
        }
        nextField:
        for (Iterator fdIter = this.fds.values().iterator(); fdIter.hasNext();)
        {
            Fd fd = (Fd)fdIter.next();
            if (!fd.isFixed())
            {
                // Check for name reservation via derived classes
                for (Iterator nlIter = this.nameListDowns.iterator(); nlIter.hasNext();)
                {
                    NameListDown nl = (NameListDown)nlIter.next();
                    String theOutName = nl.getFieldObfNameDown(this, fd.getInName());
                    if (theOutName != null)
                    {
                        fd.setOutName(theOutName);
                        System.out.println("# Field " + fd.getFullInName() + " renamed to " + fd.getOutName()
                            + " because of derived class.");
                        continue nextField;
                    }
                }
                // Check for name reservation via super classes
                for (Iterator nlIter = this.nameListUps.iterator(); nlIter.hasNext();)
                {
                    NameListUp nl = (NameListUp)nlIter.next();
                    String theOutName = nl.getFieldOutNameUp(fd.getInName());
                    if (theOutName != null)
                    {
                        fd.setOutName(theOutName);
                        fd.setIsOverride();
                        System.out.println("# Field " + fd.getFullInName() + " renamed to " + fd.getOutName()
                            + " because of super class.");
                        continue nextField;
                    }
                }
                // If no other restrictions, obfuscate it
                String theOutName = NameProvider.getNewFieldName(fd);
                if (theOutName != null)
                {
                    fd.setOutName(theOutName);
                    System.out.println("# Field " + fd.getFullInName() + " renamed to " + fd.getOutName() + " from name maker.");
                }
            }
        }
    }

    /**
     * Get output method name from list, or null if no mapping exists.
     * 
     * @throws ClassFileException
     */
    @Override
    public String getMethodOutNameUp(String name, String descriptor) throws ClassFileException
    {
        // Check supers
        for (Iterator iter = this.nameListUps.iterator(); iter.hasNext();)
        {
            NameListUp nl = (NameListUp)iter.next();
            String superOutName = nl.getMethodOutNameUp(name, descriptor);
            if (superOutName != null)
            {
                return superOutName;
            }
        }

        // Check self
        Md md = this.getMethod(name, descriptor);
        if ((md != null) && !Modifier.isPrivate(md.access))
        {
            return md.getOutName();
        }

        return null;
    }

    /**
     * Get obfuscated method name from list, or null if no mapping exists.
     * 
     * @throws ClassFileException
     */
    @Override
    public String getMethodObfNameUp(String name, String descriptor) throws ClassFileException
    {
        // Check supers
        for (Iterator iter = this.nameListUps.iterator(); iter.hasNext();)
        {
            NameListUp nl = (NameListUp)iter.next();
            String superObfName = nl.getMethodObfNameUp(name, descriptor);
            if (superObfName != null)
            {
                return superObfName;
            }
        }

        // Check self
        Md md = this.getMethod(name, descriptor);
        if ((md != null) && !Modifier.isPrivate(md.access))
        {
            return md.getObfName();
        }

        return null;
    }

    /** Get output field name from list, or null if no mapping exists. */
    @Override
    public String getFieldOutNameUp(String name)
    {
        // Check supers
        for (Iterator iter = this.nameListUps.iterator(); iter.hasNext();)
        {
            NameListUp nl = (NameListUp)iter.next();
            String superOutName = nl.getFieldOutNameUp(name);
            if (superOutName != null)
            {
                return superOutName;
            }
        }

        // Check self
        Fd fd = this.getField(name);
        if ((fd != null) && !Modifier.isPrivate(fd.access))
        {
            return fd.getOutName();
        }

        return null;
    }

    /** Get obfuscated field name from list, or null if no mapping exists. */
    @Override
    public String getFieldObfNameUp(String name)
    {
        // Check supers
        for (Iterator iter = this.nameListUps.iterator(); iter.hasNext();)
        {
            NameListUp nl = (NameListUp)iter.next();
            String superObfName = nl.getFieldObfNameUp(name);
            if (superObfName != null)
            {
                return superObfName;
            }
        }

        // Check self
        Fd fd = this.getField(name);
        if ((fd != null) && !Modifier.isPrivate(fd.access))
        {
            return fd.getObfName();
        }

        return null;
    }

    /**
     * Is the method reserved because of its reservation down the class hierarchy?
     * 
     * @throws ClassFileException
     * @throws ClassNotFoundException
     */
    @Override
    public String getMethodObfNameDown(Cl caller, String name, String descriptor) throws ClassFileException, ClassNotFoundException
    {
        // Check ourself for an explicit 'do not obfuscate'
        Md md = this.getMethod(name, descriptor);
        if ((md != null) && md.isFixed())
        {
            return md.getOutName();
        }

        // Check our supers, except for our caller (special case if we are java/lang/Object)
        String theObfName = null;
        if (this.superClass != null)
        {
            Cl superClassItem = this.classTree.getCl(this.superClass);
            if (superClassItem != caller)
            {
                NameListUp nl = superClassItem != null ? (NameListUp)superClassItem : this.getExtNameListUp(this.superClass);
                theObfName = nl.getMethodObfNameUp(name, descriptor);
                if (theObfName != null)
                {
                    return theObfName;
                }
            }
            for (Iterator iter = this.superInterfaces.iterator(); iter.hasNext();)
            {
                String si = (String)iter.next();
                Cl interfaceItem = this.classTree.getCl(si);
                if (interfaceItem != caller)
                {
                    NameListUp nl = interfaceItem != null ? (NameListUp)interfaceItem : this.getExtNameListUp(si);
                    theObfName = nl.getMethodObfNameUp(name, descriptor);
                    if (theObfName != null)
                    {
                        return theObfName;
                    }
                }
            }
        }

        // Check our derived classes
        for (Iterator iter = this.nameListDowns.iterator(); iter.hasNext();)
        {
            NameListDown nl = (NameListDown)iter.next();
            theObfName = nl.getMethodObfNameDown(this, name, descriptor);
            if (theObfName != null)
            {
                return theObfName;
            }
        }

        // No reservation found
        return null;
    }

    /**
     * Is the field reserved because of its reservation down the class hierarchy?
     * 
     * @throws ClassFileException
     * @throws ClassNotFoundException
     */
    @Override
    public String getFieldObfNameDown(Cl caller, String name) throws ClassFileException, ClassNotFoundException
    {
        // Check ourself for an explicit 'do not obfuscate'
        Fd fd = this.getField(name);
        if ((fd != null) && fd.isFixed())
        {
            return fd.getOutName();
        }

        // Check our supers, except for our caller (special case if we are java/lang/Object)
        String theObfName = null;
        if (this.superClass != null)
        {
            Cl superClassItem = this.classTree.getCl(this.superClass);
            if (superClassItem != caller)
            {
                NameListUp nl = superClassItem != null ? (NameListUp)superClassItem : this.getExtNameListUp(this.superClass);
                theObfName = nl.getFieldObfNameUp(name);
                if (theObfName != null)
                {
                    return theObfName;
                }
            }
            for (Iterator iter = this.superInterfaces.iterator(); iter.hasNext();)
            {
                String si = (String)iter.next();
                Cl interfaceItem = this.classTree.getCl(si);
                if (interfaceItem != caller)
                {
                    NameListUp nl = interfaceItem != null ? (NameListUp)interfaceItem : this.getExtNameListUp(si);
                    theObfName = nl.getFieldObfNameUp(name);
                    if (theObfName != null)
                    {
                        return theObfName;
                    }
                }
            }
        }

        // Check our derived classes
        for (Iterator iter = this.nameListDowns.iterator(); iter.hasNext();)
        {
            NameListDown nl = (NameListDown)iter.next();
            theObfName = nl.getFieldObfNameDown(this, name);
            if (theObfName != null)
            {
                return theObfName;
            }
        }

        // No reservation found
        return null;
    }

    private static Map extNameListUpCache = new HashMap();

    /**
     * Construct, or retrieve from cache, the NameListUp object for an external class/interface
     * 
     * @throws ClassNotFoundException
     */
    private NameListUp getExtNameListUp(String name) throws ClassNotFoundException
    {
        NameListUp nl = (NameListUp)Cl.extNameListUpCache.get(name);
        if (nl == null)
        {
            nl = new ExtNameListUp(name);
            Cl.extNameListUpCache.put(name, nl);
        }
        return nl;
    }

    /** NameListUp for class/interface not in the database. */
    class ExtNameListUp implements NameListUp
    {
        // Class's fully qualified name
        private Class extClass;
        private List methods = null;

        /**
         * Ctor.
         * 
         * @throws ClassNotFoundException
         */
        public ExtNameListUp(String name) throws ClassNotFoundException
        {
            this.extClass = Class.forName(ClassFile.translate(name));
        }

        /** Ctor. */
        public ExtNameListUp(Class extClass)
        {
            this.extClass = extClass;
        }

        /**
         * Get obfuscated method name from list, or null if no mapping exists.
         * 
         * @throws ClassFileException
         */
        @Override
        public String getMethodObfNameUp(String name, String descriptor) throws ClassFileException
        {
            return this.getMethodOutNameUp(name, descriptor);
        }

        /**
         * Get obfuscated method name from list, or null if no mapping exists.
         * 
         * @throws ClassFileException
         */
        @Override
        public String getMethodOutNameUp(String name, String descriptor) throws ClassFileException
        {
            // Get list of public/protected methods
            if (this.methods == null)
            {
                this.methods = this.getAllDeclaredMethods(this.extClass);
                List pruned = new ArrayList();
                for (Iterator iter = this.methods.iterator(); iter.hasNext();)
                {
                    Method md = (Method)iter.next();
                    int modifiers = md.getModifiers();
                    if (!Modifier.isPrivate(modifiers))
                    {
                        pruned.add(md);
                    }
                }
                this.methods = pruned;
            }

            // Check each public/protected class method against the named one
            nextMethod:
            for (Iterator iter = this.methods.iterator(); iter.hasNext();)
            {
                Method md = (Method)iter.next();
                if (name.equals(md.getName()))
                {
                    String[] paramAndReturnNames = ClassFile.parseDescriptor(descriptor);
                    Class[] paramTypes = md.getParameterTypes();
                    Class returnType = md.getReturnType();
                    if (paramAndReturnNames.length == paramTypes.length + 1)
                    {
                        for (int j = 0; j < paramAndReturnNames.length - 1; j++)
                        {
                            if (!paramAndReturnNames[j].equals(paramTypes[j].getName()))
                            {
                                continue nextMethod;
                            }
                        }
                        String returnName = returnType.getName();
                        if (!paramAndReturnNames[paramAndReturnNames.length - 1].equals(returnName))
                        {
                            continue nextMethod;
                        }

                        // We have a match, and so the derived class method name must be made to match
                        return name;
                    }
                }
            }

            // Method is not present
            return null;
        }

        /** Get obfuscated field name from list, or null if no mapping exists. */
        @Override
        public String getFieldObfNameUp(String name)
        {
            return this.getFieldOutNameUp(name);
        }

        /** Get obfuscated field name from list, or null if no mapping exists. */
        @Override
        public String getFieldOutNameUp(String name)
        {
            // Use reflection to check class for field
            Field field = this.getAllDeclaredField(this.extClass, name);
            if (field != null)
            {
                // Field must be public or protected
                int modifiers = field.getModifiers();
                if (!Modifier.isPrivate(modifiers))
                {
                    return name;
                }
            }

            // Field is not present
            return null;
        }

        /** Get all methods (from supers too) regardless of access level */
        private List getAllDeclaredMethods(Class theClass)
        {
            List ma = new ArrayList();

            // Get the public methods from all supers and interfaces up the tree
            ma.addAll(Arrays.asList(theClass.getMethods()));

            // Go up the super hierarchy, getting arrays of all methods (some redundancy here, but that's okay)
            while (theClass != null)
            {
                ma.addAll(Arrays.asList(theClass.getDeclaredMethods()));
                theClass = theClass.getSuperclass();
            }

            return ma;
        }

        /** Get a specified field (from supers and interfaces too) regardless of access level */
        private Field getAllDeclaredField(Class theClass, String name)
        {
            Class origClass = theClass;

            // Check for field in supers
            while (theClass != null)
            {
                Field field = null;
                try
                {
                    field = theClass.getDeclaredField(name);
                }
                catch (NoSuchFieldException e)
                {
                    // TODO printStackTrace
                    e.printStackTrace();
                }
                // TODO check for missed exceptions
//                catch (Exception e)
//                {
//                    field = null;
//                }
                if (field != null)
                {
                    return field;
                }
                theClass = theClass.getSuperclass();
            }

            // Check for public field in supers and interfaces (some redundancy here, but that's okay)
            try
            {
                return origClass.getField(name);
            }
            catch (NoSuchFieldException e)
            {
                // TODO printStackTrace
                e.printStackTrace();
                return null;
            }
            // TODO check for missed exceptions
//            catch (Exception e)
//            {
//                return null;
//            }
        }
    }

    /**
     * Walk class inheritance group taking action once only on each class.
     * Must be called after setupNameListDowns() called for all classes.
     * 
     * @throws ClassFileException
     * @throws ClassNotFoundException
     */
    public void walkGroup(TreeAction ta) throws ClassFileException, ClassNotFoundException
    {
        List done = new ArrayList();
        this.walkGroup(ta, this, done);
    }

    /**
     * Walk class inheritance group taking action once only on each class.
     * 
     * @throws ClassFileException
     * @throws ClassNotFoundException
     */
    private void walkGroup(TreeAction ta, Cl cl, List done) throws ClassFileException, ClassNotFoundException
    {
        if (!done.contains(cl))
        {
            // Take the action and mark this class as done
            ta.classAction(cl);
            done.add(cl);
            // Traverse super class
            Cl superCl = this.classTree.getCl(this.superClass);
            // ignore external to JAR
            if (superCl != null)
            {
                this.walkGroup(ta, superCl, done);
            }
            // Traverse super interfaces
            for (Iterator iter = this.superInterfaces.iterator(); iter.hasNext();)
            {
                String si = (String)iter.next();
                Cl interfaceItem = this.classTree.getCl(si);
                // ignore external to JAR
                if (interfaceItem != null)
                {
                    this.walkGroup(ta, interfaceItem, done);
                }
            }
            // Traverse derived classes
            for (Iterator clIter = this.nameListDowns.iterator(); clIter.hasNext();)
            {
                Cl subCl = (Cl)clIter.next();
                if (subCl != null)
                {
                    this.walkGroup(ta, subCl, done);
                }
            }
        }
    }

    @Override
    public String getFullOutName()
    {
        String clName = super.getFullOutName();

        if (NameProvider.currentMode != NameProvider.CLASSIC_MODE)
        {
            if (this.parent != null)
            {
                if (this.parent.parent == null)
                {
                    if (this.parent instanceof Pk)
                    {
                        String pkName = this.parent.getFullOutName();

                        if (pkName.equals(""))
                        {
                            return clName;
                        }

                        return pkName + this.sep + clName;
                    }
                }
            }
        }

        return clName;
    }

    public Iterator getDownClasses()
    {
        List clsList = new ArrayList();
        for (Iterator iter = this.nameListDowns.iterator(); iter.hasNext();)
        {
            // TODO catch Exception
//            try
//            {
            Cl cl = (Cl)iter.next();
            clsList.add(cl);
//            }
//            catch (Exception e)
//            {
//                continue;
//            }
        }
        return clsList.iterator();
    }
}
