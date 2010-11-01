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
import COM.rl.util.*;
import COM.rl.obf.classfile.*;

/**
 * Tree item representing a class or interface.
 *
 * @author      Mark Welsh
 */
public class Cl extends PkCl implements NameListUp, NameListDown
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private Hashtable mds = new Hashtable(); // Owns a list of methods
    private Hashtable mdsSpecial = new Hashtable(); // Owns a list of special methods
    private Hashtable fds = new Hashtable(); // Owns a list of fields
    private boolean isResolved = false; // Has the class been resolved already?
    private boolean isScanned = false; // Has the class been scanned already?
    private String superClass; // Our superclass name
    private String[] superInterfaces; // Names of implemented interfaces
    private boolean isInnerClass; // Is this an inner class?
    private Vector nameListUps = new Vector(); // NameListUp interfaces for super-class/interfaces
    private Vector nameListDowns = new Vector(); // NameListDown interfaces for derived class/interfaces
    private boolean isNoWarn = false; // Are danger-method warnings suppressed?
    private Vector warningList = null; // Danger-method warnings

    public static int nameSpace = 0;
    private static NameMaker methodNameMaker;
    private static NameMaker fieldNameMaker;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /** Ctor. */
    public Cl(TreeItem parent, boolean isInnerClass, String name, String superClass, String[] superInterfaces, int access) throws Exception
    {
        super(parent, name);
        this.superClass = superClass;
        this.superInterfaces = superInterfaces;
        this.isInnerClass = isInnerClass;
        this.access = access;
        if (parent == null || "".equals(name))
        {
            System.err.println("Internal error: class must have parent and name");
        }
        if (parent instanceof Cl)
        {
            sep = ClassFile.SEP_INNER;
        }

        // Do not obfuscate anonymous inner classes
        if (isInnerClass && name != null && name.length() > 0 
            && Character.isDigit(name.charAt(0)))
        {
            setOutName(getInName());
        }
    }

    /** Is this an inner class? */
    public boolean isInnerClass() { return isInnerClass; }

    /** Suppress warnings. */
    public void setNoWarn() { isNoWarn = true; }

    /** Add class's warning. */
    public void setWarnings(ClassFile cf) throws Exception
    {
        if (warningList == null) 
        {
            warningList = new Vector();
        }
        warningList = cf.listDangerMethods(warningList);
    }

    /** Do we have non-suppressed warnings? */
    public boolean hasWarnings()
    {
        return (!isNoWarn && warningList != null && warningList.size() > 0);
    }

    /** Log this class's warnings. */
    public void logWarnings(PrintWriter log)
    {
        if (hasWarnings())
        {
            for (Enumeration enm = warningList.elements(); 
                 enm.hasMoreElements(); ) 
            {
                log.println("# " + (String)enm.nextElement());
            }
        }
    }

    /** Get a method by name. */
    public Md getMethod(String name, String descriptor) throws Exception 
    {
        return (Md)mds.get(name + descriptor);
    }

    /** Get a special method by name. */
    public Md getMethodSpecial(String name, String descriptor) throws Exception 
    {
        return (Md)mdsSpecial.get(name + descriptor);
    }

    /** Get all methods with obfuscated name. */
    public Enumeration getObfMethods(String name) throws Exception 
    {
	Vector mdsMatch = new Vector();
	for (Enumeration enm = mds.elements(); enm.hasMoreElements(); )
	{
	    Md md = (Md)enm.nextElement();
	    if (name.equals(md.getOutName())) 
	    {
		mdsMatch.addElement(md);
	    }
	}
        return mdsMatch.elements();
    }

    /** Get a field by name. */
    public Fd getField(String name) throws Exception {return (Fd)fds.get(name);}

    /** Get an Enumeration of methods. */
    public Enumeration getMethodEnum() throws Exception {return mds.elements();}

    /** Get an Enumeration of fields. */
    public Enumeration getFieldEnum() throws Exception {return fds.elements();}

    /** Return this Cl's superclass Cl */
    public Cl getSuperCl() throws Exception 
    {
        if (superClass != null) 
        {
            return classTree.getCl(superClass);
        } 
        else 
        {
	    return null;
	}
    }

    /** Return Enumeration of this Cl's super-interfaces */
    public Enumeration getSuperInterfaces() throws Exception 
    {
        Vector v = new Vector();
        if (superInterfaces != null)
        {
            for (int i = 0; i < superInterfaces.length; i++) 
            {
                Cl interfaceItem = classTree.getCl(superInterfaces[i]);
                if (interfaceItem != null) 
                {
                    v.addElement(interfaceItem);
                }
            }
        }
        return v.elements();
    }

    /** Does this internal class have the specified class or interface 
        in its super or interface chain? */
    protected boolean hasAsSuperInt(String queryName, 
                                    boolean checkInterfaces)
    {
        try
        {
            // Special case: is this java/lang/Object?
            if (superClass == null) return false;
            // Check our parents
            if (superClass.equals(queryName)) return true; 
            if (checkInterfaces) 
            {
                if (superInterfaces != null)
                {
                    for (int i = 0; i < superInterfaces.length; i++) 
                    {
                        if (queryName.equals(superInterfaces[i])) return true; 
                    }
                }
            }
            // Nothing, so recurse up through parents
            Cl superClInt = classTree.getCl(superClass);
            if (superClInt != null) 
            {
                if (superClInt.hasAsSuperInt(queryName, checkInterfaces)) 
                    return true; 
            } 
            else 
            {
                Class superClExt = Class.forName(ClassFile.translate(superClass));
                if (superClExt != null)
                {
                    if (hasAsSuperExt(queryName, checkInterfaces, 
                                      this.classTree, superClExt)) 
                        return true;
                }
            }
            if (checkInterfaces) 
            {
                if (superInterfaces != null)
                {
                    for (int i = 0; i < superInterfaces.length; i++) 
                    {
                        Cl interClInt = classTree.getCl(superInterfaces[i]);
                        if (interClInt != null) 
                        {
                            if (interClInt.hasAsSuperInt(queryName, checkInterfaces)) 
                                return true; 
                        } 
                        else 
                        {
                            Class interClExt = 
                                Class.forName(ClassFile.translate(superInterfaces[i]));
                            if (interClExt != null)
                            {
                                if (hasAsSuperExt(queryName, checkInterfaces, 
                                              this.classTree, interClExt))
                                    return true;
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            // fall thru
        }
        return false;
    }

    /** Does this class have the specified class or interface in its super 
        or interface chain? */
    protected static boolean hasAsSuperExt(String queryName, 
                                           boolean checkInterfaces, 
                                           ClassTree classTree,
                                           Class clExt)
    {
        try
        {
            // Special case: is this java/lang/Object?
            if (clExt == null ||
                clExt.getName().equals("java.lang.Object")) return false;
            // Check our parents
            String queryNameExt = ClassFile.translate(queryName);
            Class superClass = clExt.getSuperclass();
            Class[] superInterfaces = clExt.getInterfaces();
            if (queryNameExt.equals(superClass.getName())) return true; 
            if (checkInterfaces) 
            {
                if (superInterfaces != null)
                {
                    for (int i = 0; i < superInterfaces.length; i++) 
                    {
                        if (queryNameExt.equals(superInterfaces[i].getName())) return true; 
                    }
                }
            }
            // Nothing, so recurse up through parents
            Cl superClInt = classTree.getCl(ClassFile.backTranslate(superClass.getName()));
            if (superClInt != null) 
            {
                if (superClInt.hasAsSuperInt(queryName, checkInterfaces)) 
                    return true; 
            } 
            else 
            {
                Class superClExt = superClass;
                if (superClExt != null)
                {
                    if (hasAsSuperExt(queryName, checkInterfaces, 
                                      classTree, superClExt))
                        return true;
                }
            }
            if (checkInterfaces) 
            {
                if (superInterfaces != null)
                {
                    for (int i = 0; i < superInterfaces.length; i++) 
                    {
                        Cl interClInt = classTree.getCl(ClassFile.backTranslate(superInterfaces[i].getName()));
                        if (interClInt != null) 
                        {
                            if (interClInt.hasAsSuperInt(queryName, checkInterfaces)) 
                                return true; 
                        } 
                        else 
                        {
                            Class interClExt = superInterfaces[i];
                            if (interClExt != null)
                            {
                                if (hasAsSuperExt(queryName, checkInterfaces, 
                                                  classTree, interClExt))
                                    return true;
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            // fall thru
        }
        return false;
    }

    /** Does this class have the specified class or interface in its super 
        or interface chain? */
    public boolean hasAsSuperOrInterface(String queryName)
    {
        return hasAsSuperInt(queryName, true);
    }

    /** Does this class have the specified class in its super chain? */
    public boolean hasAsSuper(String queryName)
    {
        return hasAsSuperInt(queryName, false);
    }

    /** Add an inner class. */
    public Cl addClass(String name, String superName, String[] interfaceNames, int access) throws Exception
    {
        return addClass(true, name, superName, interfaceNames, access);
    }

    /** Add an inner class, used when copying inner classes from a placeholder. */
    public Cl addClass(Cl cl) throws Exception
    {
        cls.put(cl.getInName(), cl);
        return cl;
    }

    /** Add a placeholder class. */
    public Cl addPlaceholderClass(String name) throws Exception
    {
        return addPlaceholderClass(true, name);
    }

    /** Add a method. */
    public Md addMethod(ClassFile cf, MethodInfo methodInfo, boolean enableTrim) throws Exception
    {
	Md md = addMethod(methodInfo.isSynthetic(),
			  methodInfo.getName(),
			  methodInfo.getDescriptor(),
			  methodInfo.getAccessFlags());
	// Construct method's reference list
	if (enableTrim) 
	{
	    md.findRefs(cf, methodInfo);
	}
	return md;
    }

    /** Add a method. */
    public Md addMethod(boolean isSynthetic, String name, String descriptor,
			int accessFlags) throws Exception
    {
        // Store <init> and <clinit> methods separately - needed only for 
	// reference tracking ('.option trim')
	Md md;
	if (name.charAt(0) == '<')
        {
	    md = getMethodSpecial(name, descriptor);
	    if (md == null)
	    {
		md = new Md(this, isSynthetic, name, descriptor, accessFlags);
		mdsSpecial.put(name + descriptor, md);
	    }
        }
	else 
	{
	    md = getMethod(name, descriptor);
	    if (md == null)
	    {
		md = new Md(this, isSynthetic, name, descriptor, accessFlags);
		mds.put(name + descriptor, md);
	    }
	}
	return md;
    }

    /** Add a field. */
    public Fd addField(ClassFile cf, FieldInfo fieldInfo, boolean enableTrim) throws Exception
    {
	Fd fd = addField(fieldInfo.isSynthetic(),
			 fieldInfo.getName(),
			 fieldInfo.getDescriptor(),
			 fieldInfo.getAccessFlags());
	// Construct field's reference list
	if (enableTrim) 
	{
	    fd.findRefs(cf, fieldInfo);
	}
	return fd;
    }

    /** Add a field. */
    public Fd addField(boolean isSynthetic, String name, String descriptor, int access) throws Exception
    {
        Fd fd = getField(name);
        if (fd == null)
        {
            fd = new Fd(this, isSynthetic, name, descriptor, access);
            fds.put(name, fd);
        }
        return fd;
    }

    /** Prepare for resolve of a class entry by resetting flags. */
    public void resetResolve() throws Exception
    {
        isScanned = false;
        isResolved = false;
        nameListDowns.removeAllElements();
    }

    /** Set up reverse list of reserved names prior to resolving classes. */
    public void setupNameListDowns() throws Exception
    {
        // Special case: we are java/lang/Object
        if (superClass == null) return;

        // Add this class as a NameListDown to the super and each interface, if they are in the JAR
        Cl superClassItem = classTree.getCl(superClass);
        if (superClassItem != null)
        {
            superClassItem.nameListDowns.addElement(this);
        }
        if (superInterfaces != null)
        {
            for (int i = 0; i < superInterfaces.length; i++)
            {
                Cl interfaceItem = classTree.getCl(superInterfaces[i]);
                if (interfaceItem != null)
                {
                    interfaceItem.nameListDowns.addElement(this);
                }
            }
        }
    }

    /**
     * Resolve a class entry - set obfuscation permissions based on super class and interfaces.
     * Overload method and field names maximally.
     */
    public void resolveOptimally() throws Exception
    {
        // Already processed, then do nothing
        if (!isResolved)
        {
            // Get lists of method and field names in inheritance namespace
            Vector methods = new Vector();
            Vector fields = new Vector();
            scanNameSpaceExcept(null, methods, fields);
            String[] methodNames = new String[methods.size()];
            for (int i = 0; i < methodNames.length; i++)
            {
                methodNames[i] = (String)methods.elementAt(i);
            }
            String[] fieldNames = new String[fields.size()];
            for (int i = 0; i < fieldNames.length; i++)
            {
                fieldNames[i] = (String)fields.elementAt(i);
            }

            // Create new name-makers for the namespace
            methodNameMaker = new KeywordNameMaker(methodNames);
            fieldNameMaker = new KeywordNameMaker(fieldNames);

            // Resolve a full name space
            resolveNameSpaceExcept(null);

            // and move to next
            nameSpace++;
        }
    }

    // Get lists of method and field names in inheritance namespace
    private void scanNameSpaceExcept(Cl ignoreCl, Vector methods, 
                                     Vector fields) throws Exception
    {
        // Special case: we are java/lang/Object
        if (superClass == null) return;

        // Traverse one step in each direction in name space, scanning
        if (!isScanned)
        {
            // First step up to super classes, scanning them
            Cl superCl = classTree.getCl(superClass);
            if (superCl != null) // internal to JAR
            {
                if (superCl != ignoreCl)
                {
                    superCl.scanNameSpaceExcept(this, methods, fields);
                }
            }
            else // external to JAR
            {
                scanExtSupers(superClass, methods, fields);
            }
            if (superInterfaces != null)
            {
                for (int i = 0; i < superInterfaces.length; i++)
                {
                    Cl interfaceItem = classTree.getCl(superInterfaces[i]);
                    if (interfaceItem != null && interfaceItem != ignoreCl)
                    {
                        interfaceItem.scanNameSpaceExcept(this, methods, fields);
                    }
                }
            }

            // Next, scan ourself
            if (!isScanned)
            {
                scanThis(methods, fields);

                // Signal class has been scanned
                isScanned = true;
            }

            // Finally step down to derived classes, resolving them
            for (Enumeration clEnum = nameListDowns.elements(); clEnum.hasMoreElements(); )
            {
                Cl cl = (Cl)clEnum.nextElement();
                if (cl != ignoreCl)
                {
                    cl.scanNameSpaceExcept(this, methods, fields);
                }
            }
        }
    }

    // Get lists of method and field names in inheritance namespace
    private void scanExtSupers(String name, Vector methods, 
                               Vector fields) throws Exception
    {
        Class extClass = Class.forName(ClassFile.translate(name));

        // Get public methods and fields from supers and interfaces up the tree
        Method[] allPubMethods = extClass.getMethods();
        if (allPubMethods != null) 
        {
            for (int i = 0; i < allPubMethods.length; i++) 
            {
                String methodName = allPubMethods[i].getName();
                if (methods.indexOf(methodName) == -1) 
                {
                    methods.addElement(methodName);
                }
            }
        }
        Field[] allPubFields = extClass.getFields();
        if (allPubFields != null) 
        {
            for (int i = 0; i < allPubFields.length; i++) 
            {
                String fieldName = allPubFields[i].getName();
                if (fields.indexOf(fieldName) == -1) 
                {
                    fields.addElement(fieldName);
                }
            }
        }
        // Go up the super hierarchy, adding all non-public methods/fields
        while (extClass != null) 
        {
            Method[] allClassMethods = extClass.getDeclaredMethods();
            if (allClassMethods != null) 
            {
                for (int i = 0; i < allClassMethods.length; i++) 
                {
                    if (!Modifier.isPublic(allClassMethods[i].getModifiers())) 
                    {
                        String methodName = allClassMethods[i].getName();
                        if (methods.indexOf(methodName) == -1) 
                        {
                            methods.addElement(methodName);
                        }
                    }
                }
            }
            Field[] allClassFields = extClass.getDeclaredFields();
            if (allClassFields != null) 
            {
                for (int i = 0; i < allClassFields.length; i++) 
                {
                    if (!Modifier.isPublic(allClassFields[i].getModifiers())) 
                    {
                        String fieldName = allClassFields[i].getName();
                        if (fields.indexOf(fieldName) == -1) 
                        {
                            fields.addElement(fieldName);
                        }
                    }
                }
            }
            extClass = extClass.getSuperclass();
        }
    }

    // Add method and field names from this class to the lists
    private void scanThis(Vector methods, Vector fields) throws Exception
    {
        for (Enumeration mdEnum = mds.elements(); mdEnum.hasMoreElements(); )
        {
            Md md = (Md)mdEnum.nextElement();
            if (md.isFixed()) 
            {
                String name = md.getOutName();
                if (methods.indexOf(name) == -1) 
                {
                    methods.addElement(name);
                }
            }
        }
        for (Enumeration fdEnum = fds.elements(); fdEnum.hasMoreElements(); )
        {
            Fd fd = (Fd)fdEnum.nextElement();
            if (fd.isFixed()) 
            {
                String name = fd.getOutName();
                if (fields.indexOf(name) == -1) 
                {
                    fields.addElement(name);
                }
            }
        }
    }

    // Resolve an entire inheritance name space optimally.
    private void resolveNameSpaceExcept(Cl ignoreCl) throws Exception
    {
        // Special case: we are java/lang/Object
        if (superClass == null) return;

        // Traverse one step in each direction in name space, resolving
        if (!isResolved)
        {
            // First step up to super classes, resolving them, since we depend on them
            Cl superCl = classTree.getCl(superClass);
            if (superCl != null && superCl != ignoreCl)
            {
                superCl.resolveNameSpaceExcept(this);
            }
            if (superInterfaces != null)
            {
                for (int i = 0; i < superInterfaces.length; i++)
                {
                    Cl interfaceItem = classTree.getCl(superInterfaces[i]);
                    if (interfaceItem != null && interfaceItem != ignoreCl)
                    {
                        interfaceItem.resolveNameSpaceExcept(this);
                    }
                }
            }

            // Next, resolve ourself
            if (!isResolved)
            {
                resolveThis();

                // Signal class has been processed
                isResolved = true;
            }

            // Finally step down to derived classes, resolving them
            for (Enumeration clEnum = nameListDowns.elements(); clEnum.hasMoreElements(); )
            {
                Cl cl = (Cl)clEnum.nextElement();
                if (cl != ignoreCl)
                {
                    cl.resolveNameSpaceExcept(this);
                }
            }
        }
    }

    // For each super interface and the super class, if it is outside DB, use reflection
    // to merge its list of public/protected methods/fields --
    // while for those in the DB, resolve to get the name-mapping lists
    private void resolveThis() throws Exception
    {
        // Special case: we are java/lang/Object
        if (superClass == null) return;

        Cl superClassItem = classTree.getCl(superClass);
        nameListUps.addElement(superClassItem != null ?
                               (NameListUp)superClassItem :
                               getExtNameListUp(superClass));
        if (superInterfaces != null)
        {
            for (int i = 0; i < superInterfaces.length; i++)
            {
                Cl interfaceItem = classTree.getCl(superInterfaces[i]);
                nameListUps.addElement(interfaceItem != null ?
                                       (NameListUp)interfaceItem :
                                       getExtNameListUp(superInterfaces[i]));
            }
        }

        // Run through each method/field in this class checking for reservations and
        // obfuscating accordingly
    nextMethod:
        for (Enumeration mdEnum = mds.elements(); mdEnum.hasMoreElements(); )
        {
            Md md = (Md)mdEnum.nextElement();
            if (!md.isFixed())
            {
                // Check for name reservation via derived classes
                for (Enumeration nlEnum = nameListDowns.elements(); nlEnum.hasMoreElements(); )
                {
                    String theOutName = ((NameListDown)nlEnum.nextElement()).getMethodObfNameDown(this, md.getInName(), md.getDescriptor());
                    if (theOutName != null)
                    {
                        md.setOutName(theOutName);
                        continue nextMethod;
                    }
                }
                // Check for name reservation via super classes
                for (Enumeration nlEnum = nameListUps.elements(); nlEnum.hasMoreElements(); )
                {
                    String theOutName = ((NameListUp)nlEnum.nextElement()).getMethodOutNameUp(md.getInName(), md.getDescriptor());
                    if (theOutName != null)
                    {
                        md.setOutName(theOutName);
                        md.setIsOverride();
                        continue nextMethod;
                    }
                }
                // If no other restrictions, obfuscate it
                md.setOutName(methodNameMaker.nextName(md.getDescriptor()));
            }
        }
    nextField:
        for (Enumeration fdEnum = fds.elements(); fdEnum.hasMoreElements(); )
        {
            Fd fd = (Fd)fdEnum.nextElement();
            if (!fd.isFixed())
            {
                // Check for name reservation via derived classes
                for (Enumeration nlEnum = nameListDowns.elements(); nlEnum.hasMoreElements(); )
                {
                    String theOutName = ((NameListDown)nlEnum.nextElement()).getFieldObfNameDown(this, fd.getInName());
                    if (theOutName != null)
                    {
                        fd.setOutName(theOutName);
                        continue nextField;
                    }
                }
                // Check for name reservation via super classes
                for (Enumeration nlEnum = nameListUps.elements(); nlEnum.hasMoreElements(); )
                {
                    String superOutName = ((NameListUp)nlEnum.nextElement()).getFieldOutNameUp(fd.getInName());
                    if (superOutName != null)
                    {
                        fd.setOutName(superOutName);
                        fd.setIsOverride();
                        continue nextField;
                    }
                }
                // If no other restrictions, obfuscate it
                fd.setOutName(fieldNameMaker.nextName(null));
            }
        }
    }

    /** Get output method name from list, or null if no mapping exists. */
    public String getMethodOutNameUp(String name, String descriptor) throws Exception
    {
        // Check supers
        for (Enumeration enm = nameListUps.elements(); enm.hasMoreElements(); )
        {
            String superOutName = ((NameListUp)enm.nextElement()).getMethodOutNameUp(name, descriptor);
            if (superOutName != null)
            {
                return superOutName;
            }
        }

        // Check self
        Md md = getMethod(name, descriptor);
        if (md != null && !Modifier.isPrivate(md.access))
        {
            return md.getOutName();
        }
        else
        {
            return null;
        }
    }

    /** Get obfuscated method name from list, or null if no mapping exists. */
    public String getMethodObfNameUp(String name, String descriptor) throws Exception
    {
        // Check supers
        for (Enumeration enm = nameListUps.elements(); enm.hasMoreElements(); )
        {
            String superObfName = ((NameListUp)enm.nextElement()).getMethodObfNameUp(name, descriptor);
            if (superObfName != null)
            {
                return superObfName;
            }
        }

        // Check self
        Md md = getMethod(name, descriptor);
        if (md != null && !Modifier.isPrivate(md.access))
        {
            return md.getObfName();
        }
        else
        {
            return null;
        }
    }

    /** Get output field name from list, or null if no mapping exists. */
    public String getFieldOutNameUp(String name) throws Exception
    {
        // Check supers
        for (Enumeration enm = nameListUps.elements(); enm.hasMoreElements(); )
        {
            String superOutName = ((NameListUp)enm.nextElement()).getFieldOutNameUp(name);
            if (superOutName != null)
            {
                return superOutName;
            }
        }

        // Check self
        Fd fd = getField(name);
        if (fd != null && !Modifier.isPrivate(fd.access))
        {
            return fd.getOutName();
        }
        else
        {
            return null;
        }
    }

    /** Get obfuscated field name from list, or null if no mapping exists. */
    public String getFieldObfNameUp(String name) throws Exception
    {
        // Check supers
        for (Enumeration enm = nameListUps.elements(); enm.hasMoreElements(); )
        {
            String superObfName = ((NameListUp)enm.nextElement()).getFieldObfNameUp(name);
            if (superObfName != null)
            {
                return superObfName;
            }
        }

        // Check self
        Fd fd = getField(name);
        if (fd != null && !Modifier.isPrivate(fd.access))
        {
            return fd.getObfName();
        }
        else
        {
            return null;
        }
    }

    /** Is the method reserved because of its reservation down the class hierarchy? */
    public String getMethodObfNameDown(Cl caller, String name, String descriptor) throws Exception
    {
        // Check ourself for an explicit 'do not obfuscate'
        Md md = getMethod(name, descriptor);
        if (md != null && md.isFixed())
        {
            return md.getOutName();
        }

        // Check our supers, except for our caller (special case if we are java/lang/Object)
        String theObfName = null;
        if (superClass != null)
        {
            Cl superClassItem = classTree.getCl(superClass);
            if (superClassItem != caller)
            {
                NameListUp nl = superClassItem != null ? (NameListUp)superClassItem : getExtNameListUp(superClass);
                theObfName = nl.getMethodObfNameUp(name, descriptor);
                if (theObfName != null)
                {
                    return theObfName;
                }
            }
            if (superInterfaces != null)
            {
                for (int i = 0; i < superInterfaces.length; i++)
                {
                    Cl interfaceItem = classTree.getCl(superInterfaces[i]);
                    if (interfaceItem != caller) 
                    {
                        NameListUp nl = interfaceItem != null ? (NameListUp)interfaceItem : getExtNameListUp(superInterfaces[i]);
                        theObfName = nl.getMethodObfNameUp(name, descriptor);
                        if (theObfName != null)
                        {
                            return theObfName;
                        }
                    }
                }
            }
        }

        // Check our derived classes
        for (Enumeration enm = nameListDowns.elements(); enm.hasMoreElements(); )
        {
            theObfName = ((NameListDown)enm.nextElement()).getMethodObfNameDown(this, name, descriptor);
            if (theObfName != null)
            {
                return theObfName;
            }
        }

        // No reservation found
        return null;
    }

    /** Is the field reserved because of its reservation down the class hierarchy? */
    public String getFieldObfNameDown(Cl caller, String name) throws Exception
    {
        // Check ourself for an explicit 'do not obfuscate'
        Fd fd = getField(name);
        if (fd != null && fd.isFixed())
        {
            return fd.getOutName();
        }

        // Check our supers, except for our caller (special case if we are java/lang/Object)
        String theObfName = null;
        if (superClass != null)
        {
            Cl superClassItem = classTree.getCl(superClass);
            if (superClassItem != caller)
            {
                NameListUp nl = superClassItem != null ? (NameListUp)superClassItem : getExtNameListUp(superClass);
                theObfName = nl.getFieldObfNameUp(name);
                if (theObfName != null)
                {
                    return theObfName;
                }
            }
            if (superInterfaces != null)
            {
                for (int i = 0; i < superInterfaces.length; i++)
                {
                    Cl interfaceItem = classTree.getCl(superInterfaces[i]);
                    if (interfaceItem != caller)
                    {
                        NameListUp nl = interfaceItem != null ? (NameListUp)interfaceItem : getExtNameListUp(superInterfaces[i]);
                        theObfName = nl.getFieldObfNameUp(name);
                        if (theObfName != null)
                        {
                            return theObfName;
                        }
                    }
                }
            }
        }

        // Check our derived classes
        for (Enumeration enm = nameListDowns.elements(); enm.hasMoreElements(); )
        {
            theObfName = ((NameListDown)enm.nextElement()).getFieldObfNameDown(this, name);
            if (theObfName != null)
            {
                return theObfName;
            }
        }

        // No reservation found
        return null;
    }

    // Construct, or retrieve from cache, the NameListUp object for an external class/interface
    private static Hashtable extNameListUpCache = new Hashtable();
    private NameListUp getExtNameListUp(String name) throws Exception
    {
        NameListUp nl = (NameListUp)extNameListUpCache.get(name);
        if (nl == null)
        {
            nl = new ExtNameListUp(name);
            extNameListUpCache.put(name, nl);
        }
        return nl;
    }

    // NameListUp for class/interface not in the database.
    class ExtNameListUp implements NameListUp
    {
        // Class's fully qualified name
        private Class extClass;
        private Method[] methods = null;

        // Ctor.
        public ExtNameListUp(String name) throws Exception
        {
            extClass = Class.forName(ClassFile.translate(name));
        }

        // Ctor.
        public ExtNameListUp(Class extClass) throws Exception
        {
            this.extClass = extClass;
        }

        // Get obfuscated method name from list, or null if no mapping exists.
        public String getMethodObfNameUp(String name, String descriptor) throws Exception
        {
            return getMethodOutNameUp(name, descriptor);
        }

        // Get obfuscated method name from list, or null if no mapping exists.
        public String getMethodOutNameUp(String name, String descriptor) throws Exception
        {
            // Get list of public/protected methods
            if (methods == null)
            {
                methods = getAllDeclaredMethods(extClass);
                Vector pruned = new Vector();
                for (int i = 0; i < methods.length; i++)
                {
                    int modifiers = methods[i].getModifiers();
                    if (!Modifier.isPrivate(modifiers))
                    {
                        pruned.addElement(methods[i]);
                    }
                }
                methods = new Method[pruned.size()];
                for (int i = 0; i < methods.length; i++)
                {
                    methods[i] = (Method)pruned.elementAt(i);
                }
            }

            // Check each public/protected class method against the named one
        nextMethod:
            for (int i = 0; i < methods.length; i++)
            {
                if (name.equals(methods[i].getName()))
                {
                    String[] paramAndReturnNames = ClassFile.parseDescriptor(descriptor);
                    Class[] paramTypes = methods[i].getParameterTypes();
                    Class returnType = methods[i].getReturnType();
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

        // Get obfuscated field name from list, or null if no mapping exists.
        public String getFieldObfNameUp(String name) throws Exception
        {
            return getFieldOutNameUp(name);
        }

        // Get obfuscated field name from list, or null if no mapping exists.
        public String getFieldOutNameUp(String name) throws Exception
        {
            // Use reflection to check class for field
            Field field = getAllDeclaredField(extClass, name);
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

        // Get all methods (from supers too) regardless of access level
        private Method[] getAllDeclaredMethods(Class theClass)
        {
            Vector ma = new Vector();
            int length = 0;

            // Get the public methods from all supers and interfaces up the tree
            Method[] allPubMethods = theClass.getMethods();
            ma.addElement(allPubMethods);
            length += allPubMethods.length;

            // Go up the super hierarchy, getting arrays of all methods (some redundancy
            // here, but that's okay)
            while (theClass != null)
            {
                Method[] methods = theClass.getDeclaredMethods();
                ma.addElement(methods);
                length += methods.length;
                theClass = theClass.getSuperclass();
            }

            // Merge the arrays
            Method[] allMethods = new Method[length];
            int pos = 0;
            for (Enumeration enm = ma.elements(); enm.hasMoreElements(); )
            {
                Method[] methods = (Method[])enm.nextElement();
                System.arraycopy(methods, 0, allMethods, pos, methods.length);
                pos += methods.length;
            }
            return allMethods;
        }

        // Get a specified field (from supers and interfaces too) regardless of access level
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
                catch (Exception e)
                {
                    field = null;
                }
                if (field != null)
                {
                    return field;
                }
                theClass = theClass.getSuperclass();
            }

            // Check for public field in supers and interfaces 
            // (some redundancy here, but that's okay)
            try
            {
                return origClass.getField(name);
            }
            catch (Exception e)
            {
                return null;
            }
        }
    }

    /** Find and add TreeItem references. */
    public void findRefs(ClassFile cf) throws Exception
    {
	// Reference all <init> and <clinit> special methods
	for (Enumeration enm = mdsSpecial.elements(); enm.hasMoreElements(); )
	{
	    addRef((Md)enm.nextElement());
	}
    }

    /** Remove methods and fields that are marked for trim. */
    public void trimClassFile(ClassFile cf) throws Exception
    {
        for (int i = cf.getMethodCount() - 1; i >= 0; i--)
        {
            MethodInfo methodInfo = cf.getMethod(i);
            Md md = getMethod(methodInfo.getName(), 
                              methodInfo.getDescriptor());
            if (md != null && md.isTrimmed())
            {
                cf.removeMethod(i);
            }
        }
        for (int i = cf.getFieldCount() - 1; i >= 0; i--)
        {
            FieldInfo fieldInfo = cf.getField(i);
            Fd fd = getField(fieldInfo.getName());
            if (fd != null && fd.isTrimmed())
            {
                cf.removeField(i);
            }
        }
    }

    /** Walk class inheritance group taking action once only on each class. 
        Must be called after setupNameListDowns() called for all classes. */
    public void walkGroup(TreeAction ta) throws Exception
    {
        Vector done = new Vector();
        walkGroup(ta, this, done);
    }

    // Walk class inheritance group taking action once only on each class.
    private void walkGroup(TreeAction ta, Cl cl, Vector done) throws Exception
    {
        if (!done.contains(cl))
        {
            // Take the action and mark this class as done
            ta.classAction(cl);
            done.addElement(cl);
            // Traverse super class
            Cl superCl = classTree.getCl(superClass);
            if (superCl != null) // ignore external to JAR
            {
                walkGroup(ta, superCl, done);
            }
            // Traverse super interfaces
            if (superInterfaces != null)
            {
                for (int i = 0; i < superInterfaces.length; i++)
                {
                    Cl interfaceItem = classTree.getCl(superInterfaces[i]);
                    if (interfaceItem != null) // ignore external to JAR
                    {
                        walkGroup(ta, interfaceItem, done);
                    }
                }
            }
            // Traverse derived classes
            for (Enumeration clEnum = nameListDowns.elements(); 
                 clEnum.hasMoreElements(); )
            {
                Cl subCl = (Cl)clEnum.nextElement();
                if (subCl != null)
                {
                    walkGroup(ta, subCl, done);
                }
            }
        }
    }
}

