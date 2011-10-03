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
    /**
     * Owns a list of methods
     */
    private Map<String, Md> mds = new HashMap<String, Md>();

    /**
     * Owns a list of special methods
     */
    private Map<String, Md> mdsSpecial = new HashMap<String, Md>();

    /**
     * Owns a list of fields
     */
    private Map<String, Fd> fds = new HashMap<String, Fd>();

    /**
     * Has the class been resolved already?
     */
    private boolean isResolved = false;

    /**
     * Has the class been scanned already?
     */
    private boolean isScanned = false;

    /**
     * Our superclass name
     */
    private String superClass;

    /**
     * Names of implemented interfaces
     */
    private List<String> superInterfaces;

    /**
     * Is this an inner class?
     */
    private boolean isInnerClass;

    /**
     * NameListUp interfaces for super-class/interfaces
     */
    private List<NameListUp> nameListUps = new ArrayList<NameListUp>();

    /**
     * NameListDown interfaces for derived class/interfaces
     */
    private List<NameListDown> nameListDowns = new ArrayList<NameListDown>();

    /**
     * Are danger-method warnings suppressed?
     */
    private boolean isNoWarn = false;

    /**
     * Danger-method warnings
     */
    private List<String> warningList = new ArrayList<String>();

    public static int nameSpace = 0;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /**
     * Constructor
     * 
     * @param parent
     * @param isInnerClass
     * @param name
     * @param superClass
     * @param superInterfaces
     * @param access
     */
    public Cl(TreeItem parent, boolean isInnerClass, String name, String superClass, List<String> superInterfaces, int access)
    {
        super(parent, name);

        if (NameProvider.oldHash)
        {
            this.mds = new Hashtable<String, Md>();
            this.mdsSpecial = new Hashtable<String, Md>();
            this.fds = new Hashtable<String, Fd>();
        }

        this.superClass = superClass;
        this.superInterfaces = superInterfaces;
        this.isInnerClass = isInnerClass;
        this.access = access;
        if ((parent == null) || "".equals(name))
        {
            throw new RuntimeException("Internal error: class must have parent and name");
        }
        if (parent instanceof Cl)
        {
            this.sep = ClassFile.SEP_INNER;
        }
    }

    /**
     * Is this an inner class?
     */
    public boolean isInnerClass()
    {
        return this.isInnerClass;
    }

    /**
     * Suppress warnings.
     */
    public void setNoWarn()
    {
        this.isNoWarn = true;
    }

    /**
     * Add class's warning.
     * 
     * @param cf
     */
    public void setWarnings(ClassFile cf)
    {
        this.warningList = cf.listDangerMethods(this.warningList);
    }

    /**
     * Do we have non-suppressed warnings?
     */
    public boolean hasWarnings()
    {
        return (!this.isNoWarn && (this.warningList.size() > 0));
    }

    /**
     * Log this class's warnings.
     * 
     * @param log
     */
    public void logWarnings(PrintWriter log)
    {
        if (this.hasWarnings())
        {
            for (String warning : this.warningList)
            {
                log.println("# " + warning);
            }
        }
    }

    /**
     * Get a method by name.
     * 
     * @param name
     * @param descriptor
     */
    public Md getMethod(String name, String descriptor)
    {
        return this.mds.get(name + descriptor);
    }

    /**
     * Get a special method by name.
     * 
     * @param name
     * @param descriptor
     */
    public Md getMethodSpecial(String name, String descriptor)
    {
        return this.mdsSpecial.get(name + descriptor);
    }

    /**
     * Get a field by name.
     * 
     * @param name
     */
    public Fd getField(String name)
    {
        return this.fds.get(name);
    }

    /**
     * Get a {@code Collection<Md>} of methods.
     */
    public Collection<Md> getMethods()
    {
        return this.mds.values();
    }

    /**
     * Get a {@code Collection<Md>} of fields.
     */
    public Collection<Fd> getFields()
    {
        return this.fds.values();
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
     * Return {@code List<Cl>} of this Cl's super-interfaces
     * 
     * @throws ClassFileException
     */
    public List<Cl> getSuperInterfaces() throws ClassFileException
    {
        List<Cl> list = new ArrayList<Cl>();
        for (String si : this.superInterfaces)
        {
            Cl interfaceItem = this.classTree.getCl(si);
            if (interfaceItem != null)
            {
                list.add(interfaceItem);
            }
        }
        return list;
    }

    /**
     * Does this internal class have the specified class or interface in its super or interface chain?
     * 
     * @param queryName
     * @param checkInterfaces
     */
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
                for (String si : this.superInterfaces)
                {
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
                Class<?> superClExt = null;
                try
                {
                    superClExt = Class.forName(ClassFile.translate(this.superClass));
                }
                catch (ClassNotFoundException e)
                {
                    // fall thru
                }
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
                for (String si : this.superInterfaces)
                {
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
                        Class<?> interClExt = null;
                        try
                        {
                            interClExt = Class.forName(ClassFile.translate(si));
                        }
                        catch (ClassNotFoundException e)
                        {
                            // fall thru
                        }
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
        catch (ClassFileException e)
        {
            // fall thru
        }
        return false;
    }

    /**
     * Does this class have the specified class or interface in its super or interface chain?
     * 
     * @param queryName
     * @param checkInterfaces
     * @param classTree
     * @param clExt
     */
    protected static boolean hasAsSuperExt(String queryName, boolean checkInterfaces, ClassTree classTree, Class<?> clExt)
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
            Class<?> superClass = clExt.getSuperclass();
            List<Class<?>> superInterfaces = Arrays.asList(clExt.getInterfaces());
            if (superClass != null)
            {
                if (queryNameExt.equals(superClass.getName()))
                {
                    return true;
                }
            }
            if (checkInterfaces)
            {
                for (Class<?> si : superInterfaces)
                {
                    if (queryNameExt.equals(si.getName()))
                    {
                        return true;
                    }
                }
            }
            // Nothing, so recurse up through parents
            if (superClass != null)
            {
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
                    Class<?> superClExt = superClass;
                    if (Cl.hasAsSuperExt(queryName, checkInterfaces, classTree, superClExt))
                    {
                        return true;
                    }
                }
            }
            if (checkInterfaces)
            {
                for (Class<?> si : superInterfaces)
                {
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
                        Class<?> interClExt = si;
                        if (Cl.hasAsSuperExt(queryName, checkInterfaces, classTree, interClExt))
                        {
                            return true;
                        }
                    }
                }
            }
        }
        catch (ClassFileException e)
        {
            // fall thru
        }
        return false;
    }

    /**
     * Does this class have the specified class or interface in its super or interface chain?
     * 
     * @param queryName
     */
    public boolean hasAsSuperOrInterface(String queryName)
    {
        return this.hasAsSuperInt(queryName, true);
    }

    /**
     * Does this class have the specified class in its super chain?
     * 
     * @param queryName
     */
    public boolean hasAsSuper(String queryName)
    {
        return this.hasAsSuperInt(queryName, false);
    }

    /**
     * Add an inner class.
     */
    @Override
    public Cl addClass(String name, String superName, List<String> interfaceNames, int access)
    {
        return this.addClass(true, name, superName, interfaceNames, access);
    }

    /**
     * Add an inner class, used when copying inner classes from a placeholder.
     * 
     * @param cl
     */
    public Cl addClass(Cl cl)
    {
        this.cls.put(cl.getInName(), cl);
        return cl;
    }

    /**
     * Add a placeholder class.
     */
    @Override
    public Cl addPlaceholderClass(String name)
    {
        return this.addPlaceholderClass(true, name);
    }

    /**
     * Add a method.
     * 
     * @param cf
     * @param md
     * @throws ClassFileException
     */
    public Md addMethod(ClassFile cf, MethodInfo md) throws ClassFileException
    {
        return this.addMethod(md.isSynthetic(), md.getName(), md.getDescriptor(), md.getAccessFlags());
    }

    /**
     * Add a method.
     * 
     * @param isSynthetic
     * @param name
     * @param descriptor
     * @param accessFlags
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
     * @param cf
     * @param fd
     * @throws ClassFileException
     */
    public Fd addField(ClassFile cf, FieldInfo fd) throws ClassFileException
    {
        return this.addField(fd.isSynthetic(), fd.getName(), fd.getDescriptor(), fd.getAccessFlags());
    }

    /**
     * Add a field.
     * 
     * @param isSynthetic
     * @param name
     * @param descriptor
     * @param access
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

    /**
     * Prepare for resolve of a class entry by resetting flags.
     */
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
        for (String si : this.superInterfaces)
        {
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
     */
    public void resolveOptimally() throws ClassFileException
    {
        // Already processed, then do nothing
        if (!this.isResolved)
        {
            // Get lists of method and field names in inheritance namespace
            List<String> methods = new ArrayList<String>();
            List<String> fields = new ArrayList<String>();
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
     * @param ignoreCl
     * @param methods
     * @param fields
     * @throws ClassFileException
     */
    private void scanNameSpaceExcept(Cl ignoreCl, List<String> methods, List<String> fields) throws ClassFileException
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
                Cl.scanExtSupers(this.superClass, methods, fields);
            }
            for (String si : this.superInterfaces)
            {
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
            for (NameListDown nameListDown : this.nameListDowns)
            {
                Cl cl = (Cl)nameListDown;
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
     * @param name
     * @param methods
     * @param fields
     */
    private static void scanExtSupers(String name, List<String> methods, List<String> fields)
    {
        Class<?> extClass = null;
        try
        {
            extClass = Class.forName(ClassFile.translate(name));
        }
        catch (ClassNotFoundException e)
        {
            return;
        }

        // Get public methods and fields from supers and interfaces up the tree
        List<Method> allPubMethods = Arrays.asList(extClass.getMethods());
        for (Method md : allPubMethods)
        {
            String methodName = md.getName();
            if (!methods.contains(methodName))
            {
                methods.add(methodName);
            }
        }
        List<Field> allPubFields = Arrays.asList(extClass.getFields());
        for (Field fd : allPubFields)
        {
            String fieldName = fd.getName();
            if (!fields.contains(fieldName))
            {
                fields.add(fieldName);
            }
        }
        // Go up the super hierarchy, adding all non-public methods/fields
        while (extClass != null)
        {
            List<Method> allClassMethods = Arrays.asList(extClass.getDeclaredMethods());
            for (Method md : allClassMethods)
            {
                if (!Modifier.isPublic(md.getModifiers()))
                {
                    String methodName = md.getName();
                    if (!methods.contains(methodName))
                    {
                        methods.add(methodName);
                    }
                }
            }
            List<Field> allClassFields = Arrays.asList(extClass.getDeclaredFields());
            for (Field fd : allClassFields)
            {
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

    /**
     * Add method and field names from this class to the lists
     * 
     * @param methods
     * @param fields
     */
    private void scanThis(List<String> methods, List<String> fields)
    {
        for (Md md : this.mds.values())
        {
            if (md.isFixed())
            {
                String name = md.getOutName();
                if (!methods.contains(name))
                {
                    methods.add(name);
                }
            }
        }
        for (Fd fd : this.fds.values())
        {
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
     * @param ignoreCl
     * @throws ClassFileException
     */
    private void resolveNameSpaceExcept(Cl ignoreCl) throws ClassFileException
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
            for (String si : this.superInterfaces)
            {
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
            for (NameListDown nameListDown : this.nameListDowns)
            {
                Cl cl = (Cl)nameListDown;
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
     */
    private void resolveThis() throws ClassFileException
    {
        // Special case: we are java/lang/Object
        if (this.superClass == null)
        {
            return;
        }

        Cl superClassItem = this.classTree.getCl(this.superClass);
        if (superClassItem != null)
        {
            this.nameListUps.add(superClassItem);
        }
        else
        {
            this.nameListUps.add(this.getExtNameListUp(this.superClass));
        }
        for (String si : this.superInterfaces)
        {
            Cl interfaceItem = this.classTree.getCl(si);
            if (interfaceItem != null)
            {
                this.nameListUps.add(interfaceItem);
            }
            else
            {
                this.nameListUps.add(this.getExtNameListUp(si));
            }
        }

        // Run through each method/field in this class checking for reservations and obfuscating accordingly
        nextMethod:
        for (Md md : this.mds.values())
        {
            String theInName = md.getInName();
            String theInDesc = md.getDescriptor();
            String fullInName = md.getFullInName();
            if (!md.isFixed())
            {
                // Check for name reservation via derived classes
                for (NameListDown nl : this.nameListDowns)
                {
                    String theOutName = nl.getMethodObfNameDown(this, theInName, theInDesc);
                    if (theOutName != null)
                    {
                        md.setOutName(theOutName);
                        if (theOutName.equals(theInName))
                        {
                            NameProvider.log("# Method " + fullInName + " unchanged from derived class");
                        }
                        else
                        {
                            NameProvider.log("# Method " + fullInName + " renamed to " + theOutName + " from derived class");
                        }
                        continue nextMethod;
                    }
                }
                // Check for name reservation via super classes
                for (NameListUp nl : this.nameListUps)
                {
                    String theOutName = nl.getMethodOutNameUp(theInName, theInDesc);
                    if (theOutName != null)
                    {
                        md.setOutName(theOutName);
                        md.setIsOverride();
                        if (theOutName.equals(theInName))
                        {
                            NameProvider.log("# Method " + fullInName + " unchanged from super class");
                        }
                        else
                        {
                            NameProvider.log("# Method " + fullInName + " renamed to " + theOutName + " from super class");
                        }
                        continue nextMethod;
                    }
                }
                // If no other restrictions, obfuscate it
                String theOutName = NameProvider.getNewMethodName(md);
                if (theOutName != null)
                {
                    md.setOutName(theOutName);
                    if (theOutName.equals(theInName))
                    {
                        NameProvider.log("# Method " + fullInName + " unchanged from name maker");
                    }
                    else
                    {
                        NameProvider.log("# Method " + fullInName + " renamed to " + theOutName + " from name maker");
                    }
                }
                else
                {
                    NameProvider.log("# Method " + fullInName + " null from name maker");
                }
            }
        }
        nextField:
        for (Fd fd : this.fds.values())
        {
            String theInName = fd.getInName();
            String fullInName = fd.getFullInName();
            if (!fd.isFixed())
            {
                // Check for name reservation via derived classes
                for (NameListDown nl : this.nameListDowns)
                {
                    String theOutName = nl.getFieldObfNameDown(this, theInName);
                    if (theOutName != null)
                    {
                        fd.setOutName(theOutName);
                        if (theOutName.equals(theInName))
                        {
                            NameProvider.log("# Field " + fullInName + " unchanged from derived class");
                        }
                        else
                        {
                            NameProvider.log("# Field " + fullInName + " renamed to " + theOutName + " from derived class");
                        }
                        continue nextField;
                    }
                }
                // Check for name reservation via super classes
                for (NameListUp nl : this.nameListUps)
                {
                    String theOutName = nl.getFieldOutNameUp(theInName);
                    if (theOutName != null)
                    {
                        fd.setOutName(theOutName);
                        fd.setIsOverride();
                        if (theOutName.equals(theInName))
                        {
                            NameProvider.log("# Field " + fullInName + " unchanged from super class");
                        }
                        else
                        {
                            NameProvider.log("# Field " + fullInName + " renamed to " + theOutName + " from super class");
                        }
                        continue nextField;
                    }
                }
                // If no other restrictions, obfuscate it
                String theOutName = NameProvider.getNewFieldName(fd);
                if (theOutName != null)
                {
                    fd.setOutName(theOutName);
                    if (theOutName.equals(theInName))
                    {
                        NameProvider.log("# Field " + fullInName + " unchanged from name maker");
                    }
                    else
                    {
                        NameProvider.log("# Field " + fullInName + " renamed to " + theOutName + " from name maker");
                    }
                }
                else
                {
                    NameProvider.log("# Field " + fullInName + " null from name maker");
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
        for (NameListUp nl : this.nameListUps)
        {
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
        for (NameListUp nl : this.nameListUps)
        {
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

    /**
     * Get output field name from list, or null if no mapping exists.
     * 
     * @throws ClassFileException
     */
    @Override
    public String getFieldOutNameUp(String name) throws ClassFileException
    {
        // Check supers
        for (NameListUp nl : this.nameListUps)
        {
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

    /**
     * Get obfuscated field name from list, or null if no mapping exists.
     * 
     * @throws ClassFileException
     */
    @Override
    public String getFieldObfNameUp(String name) throws ClassFileException
    {
        // Check supers
        for (NameListUp nl : this.nameListUps)
        {
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
     */
    @Override
    public String getMethodObfNameDown(Cl caller, String name, String descriptor) throws ClassFileException
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
                NameListUp nl;
                if (superClassItem != null)
                {
                    nl = superClassItem;
                }
                else
                {
                    nl = this.getExtNameListUp(this.superClass);
                }
                theObfName = nl.getMethodObfNameUp(name, descriptor);
                if (theObfName != null)
                {
                    return theObfName;
                }
            }
            for (String si : this.superInterfaces)
            {
                Cl interfaceItem = this.classTree.getCl(si);
                if (interfaceItem != caller)
                {
                    NameListUp nl;
                    if (interfaceItem != null)
                    {
                        nl = interfaceItem;
                    }
                    else
                    {
                        nl = this.getExtNameListUp(si);
                    }
                    theObfName = nl.getMethodObfNameUp(name, descriptor);
                    if (theObfName != null)
                    {
                        return theObfName;
                    }
                }
            }
        }

        // Check our derived classes
        for (NameListDown nl : this.nameListDowns)
        {
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
     */
    @Override
    public String getFieldObfNameDown(Cl caller, String name) throws ClassFileException
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
                NameListUp nl;
                if (superClassItem != null)
                {
                    nl = superClassItem;
                }
                else
                {
                    nl = this.getExtNameListUp(this.superClass);
                }
                theObfName = nl.getFieldObfNameUp(name);
                if (theObfName != null)
                {
                    return theObfName;
                }
            }
            for (String si : this.superInterfaces)
            {
                Cl interfaceItem = this.classTree.getCl(si);
                if (interfaceItem != caller)
                {
                    NameListUp nl;
                    if (interfaceItem != null)
                    {
                        nl = interfaceItem;
                    }
                    else
                    {
                        nl = this.getExtNameListUp(si);
                    }
                    theObfName = nl.getFieldObfNameUp(name);
                    if (theObfName != null)
                    {
                        return theObfName;
                    }
                }
            }
        }

        // Check our derived classes
        for (NameListDown nl : this.nameListDowns)
        {
            theObfName = nl.getFieldObfNameDown(this, name);
            if (theObfName != null)
            {
                return theObfName;
            }
        }

        // No reservation found
        return null;
    }

    private static Map<String, NameListUp> extNameListUpCache = new HashMap<String, NameListUp>();

    /**
     * Construct, or retrieve from cache, the NameListUp object for an external class/interface
     * 
     * @param name
     * @throws ClassFileException
     */
    private NameListUp getExtNameListUp(String name) throws ClassFileException
    {
        NameListUp nl = Cl.extNameListUpCache.get(name);
        if (nl == null)
        {
            nl = new ExtNameListUp(name);
            Cl.extNameListUpCache.put(name, nl);
        }
        return nl;
    }

    /**
     * NameListUp for class/interface not in the database.
     */
    class ExtNameListUp implements NameListUp
    {
        // Class's fully qualified name
        private Class<?> extClass;
        private List<Method> methods = null;

        /**
         * Constructor
         * 
         * @param name
         * @throws ClassFileException
         */
        public ExtNameListUp(String name) throws ClassFileException
        {
            try
            {
                this.extClass = Class.forName(ClassFile.translate(name));
            }
            catch (ClassNotFoundException e)
            {
                throw new ClassFileException("ClassNotFound " + name);
            }
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
                List<Method> allMethods = this.getAllDeclaredMethods(this.extClass);
                this.methods = new ArrayList<Method>();
                for (Method md : allMethods)
                {
                    int modifiers = md.getModifiers();
                    if (!Modifier.isPrivate(modifiers))
                    {
                        this.methods.add(md);
                    }
                }
            }

            // Check each public/protected class method against the named one
            nextMethod:
            for (Method md : this.methods)
            {
                if (name.equals(md.getName()))
                {
                    List<String> paramAndReturnNames = ClassFile.parseDescriptor(descriptor);
                    List<String> paramNames = paramAndReturnNames.subList(0, paramAndReturnNames.size() - 1);
                    String returnName = paramAndReturnNames.get(paramAndReturnNames.size() - 1);
                    List<Class<?>> paramTypes = Arrays.asList(md.getParameterTypes());
                    Class<?> returnType = md.getReturnType();
                    if (paramNames.size() == paramTypes.size())
                    {
                        for (int j = 0; j < paramNames.size(); j++)
                        {
                            if (!paramNames.get(j).equals(paramTypes.get(j).getName()))
                            {
                                continue nextMethod;
                            }
                        }
                        if (!returnName.equals(returnType.getName()))
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

        /**
         * Get obfuscated field name from list, or null if no mapping exists.
         */
        @Override
        public String getFieldObfNameUp(String name)
        {
            return this.getFieldOutNameUp(name);
        }

        /**
         * Get obfuscated field name from list, or null if no mapping exists.
         */
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

        /**
         * Get all methods (from supers too) regardless of access level
         * 
         * @param theClass
         */
        private List<Method> getAllDeclaredMethods(Class<?> theClass)
        {
            List<Method> ma = new ArrayList<Method>();

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

        /**
         * Get a specified field (from supers and interfaces too) regardless of access level
         * 
         * @param theClass
         * @param name
         */
        private Field getAllDeclaredField(Class<?> theClass, String name)
        {
            Class<?> origClass = theClass;

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
                    // fall thru
                }
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
                return null;
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

    public Iterator<Cl> getDownClasses()
    {
        List<Cl> clsList = new ArrayList<Cl>();
        for (NameListDown nameListDown : this.nameListDowns)
        {
            Cl cl = (Cl)nameListDown;
            clsList.add(cl);
        }
        return clsList.iterator();
    }
}
