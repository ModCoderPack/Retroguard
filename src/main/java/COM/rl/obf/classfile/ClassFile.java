/* ===========================================================================
 * $RCSfile: ClassFile.java,v $
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
import COM.rl.util.*;

/**
 * This is a representation of the data in a Java class-file (*.class).
 * A ClassFile instance representing a *.class file can be generated
 * using the static create(DataInput) method, manipulated using various
 * operators, and persisted back using the write(DataOutput) method.
 *
 * @author      Mark Welsh
 */
public class ClassFile implements ClassConstants
{
    // Constants -------------------------------------------------------------
    public static final String SEP_REGULAR = "/";
    public static final String SEP_INNER = "$";
    private static final String CLASS_FORNAME_NAME_DESCRIPTOR = 
    "forName(Ljava/lang/String;)Ljava/lang/Class;";
    private static final String[] DANGEROUS_CLASS_SIMPLENAME_DESCRIPTOR_ARRAY = {
        "getDeclaredField(Ljava/lang/String;)Ljava/lang/reflect/Field;",
        "getField(Ljava/lang/String;)Ljava/lang/reflect/Field;",
        "getDeclaredMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;",
        "getMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;"
    };
    private static final String LOG_DANGER_CLASS_PRE = "     Your class ";
    private static final String LOG_DANGER_CLASS_MID = " calls the java/lang/Class method ";
    private static final String LOG_CLASS_FORNAME_MID = " uses '.class' or calls java/lang/Class.";
    private static final String[] DANGEROUS_CLASSLOADER_SIMPLENAME_DESCRIPTOR_ARRAY = {
        "defineClass(Ljava/lang/String;[BII)Ljava/lang/Class;",
        "findLoadedClass(Ljava/lang/String;)Ljava/lang/Class;",
        "findSystemClass(Ljava/lang/String;)Ljava/lang/Class;",
        "loadClass(Ljava/lang/String;)Ljava/lang/Class;",
        "loadClass(Ljava/lang/String;Z)Ljava/lang/Class;"
    };
    private static final String LOG_DANGER_CLASSLOADER_PRE = "     Your class ";
    private static final String LOG_DANGER_CLASSLOADER_MID = " calls the java/lang/ClassLoader method ";


    // Fields ----------------------------------------------------------------
    private int u4magic;
    private int u2minorVersion;
    private int u2majorVersion;
    private ConstantPool constantPool;
    private int u2accessFlags;
    private int u2thisClass;
    private int u2superClass;
    private int u2interfacesCount;
    private int u2interfaces[];
    private int u2fieldsCount;
    private FieldInfo fields[];
    private int u2methodsCount;
    private MethodInfo methods[];
    private int u2attributesCount;
    private AttrInfo attributes[];

    private boolean isUnkAttrGone = false;
    private boolean hasReflection = false;
    private CpInfo cpIdString = null;


    // Class Methods ---------------------------------------------------------
    /**
     * Create a new ClassFile from the class file format data in the DataInput
     * stream.
     *
     * @throws IOException if class file is corrupt or incomplete
     */
    public static ClassFile create(DataInput din) throws Exception
    {
        if (din == null) throw new IOException("No input stream was provided.");
        ClassFile cf = new ClassFile();
        cf.read(din);
        return cf;
    }

    /** Parse a method or field descriptor into a list of parameter names (for methods)
     *  and a return type, in same format as the Class.forName() method returns . */
    public static String[] parseDescriptor(String descriptor) throws Exception
    {
        return parseDescriptor(descriptor, false, true);
    }

    /** Parse a method or field descriptor into a list of parameter names (for methods)
     *  and a return type, optionally in same format as the Class.forName() method returns . */
    public static String[] parseDescriptor(String descriptor, boolean isDisplay) throws Exception
    {
        return parseDescriptor(descriptor, isDisplay, true);
    }

    /** Parse a method or field descriptor into a list of parameter names (for methods)
     *  and a return type, in same format as the Class.forName() method returns . */
    public static String[] parseDescriptor(String descriptor, boolean isDisplay, boolean doTranslate) throws Exception
    {
        // Check for field descriptor
        String[] names = null;
        if (descriptor.charAt(0) != '(')
        {
            names = new String[1];
            names[0] = descriptor;
        }
        else
        {
            // Method descriptor
            Vector namesVec = new Vector();
            descriptor = descriptor.substring(1);
            String type = "";
            while (descriptor.length() > 0)
            {
                switch (descriptor.charAt(0))
                {
                case '[':
                    type = type + "[";
                    descriptor = descriptor.substring(1);
                    break;

                case 'B':
                case 'C':
                case 'D':
                case 'F':
                case 'I':
                case 'J':
                case 'S':
                case 'Z':
                case 'V':
                    namesVec.addElement(type + descriptor.substring(0, 1));
                    descriptor = descriptor.substring(1);
                    type = "";
                    break;

                case ')':
                    descriptor = descriptor.substring(1);
                    break;

                case 'L':
                {
                    int pos = descriptor.indexOf(';') + 1;
                    namesVec.addElement(type + descriptor.substring(0, pos));
                    descriptor = descriptor.substring(pos);
                    type = "";
                }
                break;

                default:
                    throw new Exception("Illegal field or method descriptor: " + descriptor);
                }
            }
            names = new String[namesVec.size()];
            for (int i = 0; i < names.length; i++)
            {
                names[i] = (String)namesVec.elementAt(i);
            }
        }

        if (doTranslate)
        {
            // Translate the names from JVM to Class.forName() format.
            String[] translatedNames = new String[names.length];
            for (int i = 0; i < names.length; i++)
            {
                translatedNames[i] = translateType(names[i], isDisplay);
            }
            return translatedNames;
        } 
        else
        {
            return names;
        }
    }

    /** Translate a type specifier from the internal JVM convention to the Class.forName() one. */
    public static String translateType(String inName, boolean isDisplay) throws Exception
    {
        String outName = null;
        switch (inName.charAt(0))
        {
        case '[': // For array types, Class.forName() inconsistently uses the internal type name
                  // but with '/' --> '.'
            if (!isDisplay)
            {
                // return the Class.forName() form
                outName = translate(inName);
            }
            else
            {
                // return the pretty display form
                outName = translateType(inName.substring(1), true) + "[]";
            }
            break;

        case 'B':
            outName = Byte.TYPE.getName();
            break;

        case 'C':
            outName = Character.TYPE.getName();
            break;

        case 'D':
            outName = Double.TYPE.getName();
            break;

        case 'F':
            outName = Float.TYPE.getName();
            break;

        case 'I':
            outName = Integer.TYPE.getName();
            break;

        case 'J':
            outName = Long.TYPE.getName();
            break;

        case 'S':
            outName = Short.TYPE.getName();
            break;

        case 'Z':
            outName = Boolean.TYPE.getName();
            break;

        case 'V':
            outName = Void.TYPE.getName();
            break;

        case 'L':
        {
            int pos = inName.indexOf(';');
            outName = translate(inName.substring(1, inName.indexOf(';')));
        }
        break;

        default:
            throw new Exception("Illegal field or method name: " + inName);
        }
        return outName;
    }

    /** Translate a class name from the internal '/' convention to the regular '.' one. */
    public static String translate(String name) throws Exception
    {
        return name.replace('/', '.');
    }

    /** Translate a class name from the the regular '.' convention to internal '/' one. */
    public static String backTranslate(String name) throws Exception
    {
        return name.replace('.', '/');
    }

    /** Is this class in an unsupported version of the file format? */
    public boolean hasIncompatibleVersion() 
    {
        return (u2majorVersion > MAJOR_VERSION);
    }

    /** Return major version of this class's file format. */
    public int getMajorVersion() 
    {
        return u2majorVersion;
    }


    // Instance Methods ------------------------------------------------------
    // Private constructor.
    private ClassFile() {}

    // Import the class data to internal representation.
    private void read(DataInput din) throws Exception
    {
        // Read the class file
        u4magic = din.readInt();
        u2minorVersion = din.readUnsignedShort();
        u2majorVersion = din.readUnsignedShort();

        // Check this is a valid classfile that we can handle
        if (u4magic != MAGIC)
        {
            throw new IOException("Invalid magic number in class file.");
        }
        //if (u2majorVersion > MAJOR_VERSION)
        //{
        //    throw new IOException("Incompatible version number for class file format.");
        //}

        int u2constantPoolCount = din.readUnsignedShort();
        CpInfo[] cpInfo = new CpInfo[u2constantPoolCount];
        // Fill the constant pool, recalling the zero entry
        // is not persisted, nor are the entries following a Long or Double
        for (int i = 1; i < u2constantPoolCount; i++)
        {
            cpInfo[i] = CpInfo.create(din);
            if ((cpInfo[i] instanceof LongCpInfo) ||
                (cpInfo[i] instanceof DoubleCpInfo))
            {
                i++;
            }
        }
        constantPool = new ConstantPool(this, cpInfo);

        u2accessFlags = din.readUnsignedShort();
        u2thisClass = din.readUnsignedShort();
        u2superClass = din.readUnsignedShort();
        u2interfacesCount = din.readUnsignedShort();
        u2interfaces = new int[u2interfacesCount];
        for (int i = 0; i < u2interfacesCount; i++)
        {
            u2interfaces[i] = din.readUnsignedShort();
        }
        u2fieldsCount = din.readUnsignedShort();
        fields = new FieldInfo[u2fieldsCount];
        for (int i = 0; i < u2fieldsCount; i++)
        {
            fields[i] = FieldInfo.create(din, this);
        }
        u2methodsCount = din.readUnsignedShort();
        methods = new MethodInfo[u2methodsCount];
        for (int i = 0; i < u2methodsCount; i++)
        {
            methods[i] = MethodInfo.create(din, this);
        }
        u2attributesCount = din.readUnsignedShort();
        attributes = new AttrInfo[u2attributesCount];
        for (int i = 0; i < u2attributesCount; i++)
        {
            attributes[i] = AttrInfo.create(din, this);
        }
        checkReflection();
    }

    /**
     * Define a constant String to include in this output class file.
     */
    public void setIdString(String id) throws Exception 
    {
        if (id != null) 
        {
            cpIdString = new Utf8CpInfo(id);
        } 
        else 
        {
            cpIdString = null;
        }
    }

    // Check for reflection methods and set flag
    private boolean checkReflection() throws Exception
    {
        // Need only check CONSTANT_Methodref entries of constant pool since
        // methods belong to classes 'Class' and 'ClassLoader', not interfaces.
        for (Enumeration enm = constantPool.elements(); enm.hasMoreElements(); )
        {
            Object o = enm.nextElement();
            if (o instanceof MethodrefCpInfo)
            {
                // Get the method class name, simple name and descriptor
                MethodrefCpInfo entry = (MethodrefCpInfo)o;
                ClassCpInfo classEntry = (ClassCpInfo)getCpEntry(entry.getClassIndex());
                String className = ((Utf8CpInfo)getCpEntry(classEntry.getNameIndex())).getString();
                NameAndTypeCpInfo ntEntry = (NameAndTypeCpInfo)getCpEntry(entry.getNameAndTypeIndex());
                String name = ((Utf8CpInfo)getCpEntry(ntEntry.getNameIndex())).getString();
                String descriptor = ((Utf8CpInfo)getCpEntry(ntEntry.getDescriptorIndex())).getString();

                // Check if this is Class.forName
                if (className.equals("java/lang/Class") &&
                    CLASS_FORNAME_NAME_DESCRIPTOR.equals(name + descriptor))
                {
                    hasReflection = true;
                }
            }
        }
        return hasReflection;
    }

    /** Return the access modifiers for this classfile. */
    public int getModifiers() throws Exception
    {
        return u2accessFlags;
    }

    /** Return the name of this classfile. */
    public String getName() throws Exception
    {
        return toName(u2thisClass);
    }

    /** Return the name of this class's superclass. */
    public String getSuper() throws Exception
    {
        // This may be java/lang/Object, in which case there is no super
        return (u2superClass == 0) ? null : toName(u2superClass);
    }

    /** Return the names of this class's interfaces. */
    public String[] getInterfaces() throws Exception
    {
        String[] interfaces = new String[u2interfacesCount];
        for (int i = 0; i < u2interfacesCount; i++)
        {
            interfaces[i] = toName(u2interfaces[i]);
        }
        return interfaces;
    }

    // Convert a CP index to a class name.
    private String toName(int u2index) throws Exception
    {
        CpInfo classEntry = getCpEntry(u2index);
        if (classEntry instanceof ClassCpInfo)
        {
            CpInfo nameEntry = getCpEntry(((ClassCpInfo)classEntry).getNameIndex());
            if (nameEntry instanceof Utf8CpInfo)
            {
                return ((Utf8CpInfo)nameEntry).getString();
            }
            else
            {
                throw new Exception("Inconsistent Constant Pool in class file.");
            }
        }
        else
        {
            throw new Exception("Inconsistent Constant Pool in class file.");
        }
    }

    /** Return number of methods in class. */
    public int getMethodCount() throws Exception 
    {
        return methods.length;
    }

    /** Return i'th method in class. */
    public MethodInfo getMethod(int i) throws Exception 
    {
        return methods[i];
    }

    /** Remove i'th method from class. */
    public void removeMethod(int i) throws Exception 
    {
        // Trim the method
        MethodInfo newMethods[] = new MethodInfo[methods.length-1];
        if (i > 0) 
        {
            System.arraycopy(methods, 0, newMethods, 0, i);
        }
        if (i < methods.length-1) 
        {
            System.arraycopy(methods, i+1, newMethods, i, methods.length-i-1);
        }
        methods = newMethods;
        --u2methodsCount;
    }

    /** Return number of fields in class. */
    public int getFieldCount() throws Exception 
    {
        return fields.length;
    }

    /** Return i'th field in class. */
    public FieldInfo getField(int i) throws Exception 
    {
        return fields[i];
    }

    /** Remove i'th field from class. */
    public void removeField(int i) throws Exception 
    {
        // Trim the field
        FieldInfo newFields[] = new FieldInfo[fields.length-1];
        if (i > 0) 
        {
            System.arraycopy(fields, 0, newFields, 0, i);
        }
        if (i < fields.length-1) 
        {
            System.arraycopy(fields, i+1, newFields, i, fields.length-i-1);
        }
        fields = newFields;
        --u2fieldsCount;
    }

    /** Lookup the entry in the constant pool and return as an Object. */
    protected CpInfo getCpEntry(int cpIndex) throws Exception
    {
        return constantPool.getCpEntry(cpIndex);
    }

    /** Remap a specified Utf8 entry to the given value and return its new index. */
    public int remapUtf8To(String newString, int oldIndex) throws Exception
    {
        return constantPool.remapUtf8To(newString, oldIndex);
    }

    /** Lookup the UTF8 string in the constant pool. Used in debugging. */
    protected String getUtf8(int cpIndex) throws Exception
    {
        CpInfo i = getCpEntry(cpIndex);
        if (i instanceof Utf8CpInfo) 
        {
            return ((Utf8CpInfo)i).getString();
        }
        else
        {
            return "[not UTF8]";
        }
    }

    /** Does this class contain reflection methods? */
    public boolean hasReflection()
    {
        return hasReflection;
    }

    /** List methods which can break obfuscated code, and log to a String[]. */
    public String[] getDangerousMethods() throws Exception
    {
        Vector list = new Vector();
        list = listDangerMethods(list);
        // Copy any warnings to a String[]
        String[] warnings = new String[list.size()];
        for (int i = 0; i < warnings.length; i++)
        {
            warnings[i] = (String)list.elementAt(i);
        }
        return warnings;
    }

    /** List methods which can break obfuscated code, and log to a Vector. */
    public Vector listDangerMethods(Vector list) throws Exception
    {
        // Need only check CONSTANT_Methodref entries of constant pool since
        // dangerous methods belong to classes 'Class' and 'ClassLoader', not to interfaces.
        for (Enumeration enm = constantPool.elements(); enm.hasMoreElements(); )
        {
            Object o = enm.nextElement();
            if (o instanceof MethodrefCpInfo)
            {
                // Get the method class name, simple name and descriptor
                MethodrefCpInfo entry = (MethodrefCpInfo)o;
                ClassCpInfo classEntry = (ClassCpInfo)getCpEntry(entry.getClassIndex());
                String className = ((Utf8CpInfo)getCpEntry(classEntry.getNameIndex())).getString();
                NameAndTypeCpInfo ntEntry = (NameAndTypeCpInfo)getCpEntry(entry.getNameAndTypeIndex());
                String name = ((Utf8CpInfo)getCpEntry(ntEntry.getNameIndex())).getString();
                String descriptor = ((Utf8CpInfo)getCpEntry(ntEntry.getDescriptorIndex())).getString();

                // Check if this is on the proscribed list
                if (className.equals("java/lang/Class"))
                {
                    if (CLASS_FORNAME_NAME_DESCRIPTOR.equals(name + descriptor))
                    {
                        list.addElement(LOG_DANGER_CLASS_PRE + getName() + LOG_CLASS_FORNAME_MID + CLASS_FORNAME_NAME_DESCRIPTOR);
                    }
                    else if (Tools.isInArray(name + descriptor, DANGEROUS_CLASS_SIMPLENAME_DESCRIPTOR_ARRAY))
                    {
                        list.addElement(LOG_DANGER_CLASS_PRE + getName() + LOG_DANGER_CLASS_MID + name + descriptor);
                    }
                }
                else if (Tools.isInArray(name + descriptor, DANGEROUS_CLASSLOADER_SIMPLENAME_DESCRIPTOR_ARRAY))
                {
                    list.addElement(LOG_DANGER_CLASSLOADER_PRE + getName() + LOG_DANGER_CLASSLOADER_MID + name + descriptor);
                }
            }
        }
        return list;
    }

    /** Check for direct references to Utf8 constant pool entries. */
    public void markUtf8Refs(ConstantPool pool) throws Exception
    {
        try
        {
            // Check for references to Utf8 from outside the constant pool
            for (int i = 0; i < fields.length; i++)
            {
                fields[i].markUtf8Refs(pool);
            }
            for (int i = 0; i < methods.length; i++)
            {
                methods[i].markUtf8Refs(pool); // also checks Code/LVT attrs here
            }
            for (int i = 0; i < attributes.length; i++)
            {
                attributes[i].markUtf8Refs(pool); // checks InnerClasses, SourceFile and all attr names
            }

            // Now check for references from other CP entries
            for (Enumeration enm = pool.elements(); enm.hasMoreElements(); )
            {
                Object o = enm.nextElement();
                if (o instanceof NameAndTypeCpInfo ||
                    o instanceof ClassCpInfo ||
                    o instanceof StringCpInfo)
                {
                    ((CpInfo)o).markUtf8Refs(pool);
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new Exception("Inconsistent reference to constant pool.");
        }
    }

    /** Check for direct references to NameAndType constant pool entries. */
    public void markNTRefs(ConstantPool pool) throws Exception
    {
        try
        {
            // Now check the method and field CP entries
            for (Enumeration enm = pool.elements(); enm.hasMoreElements(); )
            {
                Object o = enm.nextElement();
                if (o instanceof RefCpInfo)
                {
                    ((CpInfo)o).markNTRefs(pool);
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new Exception("Inconsistent reference to constant pool.");
        }
    }

    /**
     * Trim attributes from the classfile ('Code', 'Exceptions', 'ConstantValue'
     * are preserved, all others except the list in the String[] are killed).
     */
    public void trimAttrsExcept(String[] extraAttrs) throws Exception
    {
        // Merge additional attributes with required list
        String[] keepAttrs = REQUIRED_ATTRS;
        if (extraAttrs != null && extraAttrs.length > 0)
        {
            String[] tmp = new String[keepAttrs.length + extraAttrs.length];
            System.arraycopy(keepAttrs, 0, tmp, 0, keepAttrs.length);
            System.arraycopy(extraAttrs, 0, tmp, keepAttrs.length, extraAttrs.length);
            keepAttrs = tmp;
        }

        // Traverse all attributes, removing all except those on 'keep' list
        for (int i = 0; i < fields.length; i++)
        {
            fields[i].trimAttrsExcept(keepAttrs);
        }
        for (int i = 0; i < methods.length; i++)
        {
            methods[i].trimAttrsExcept(keepAttrs);
        }
        for (int i = 0; i < attributes.length; i++)
        {
            if (Tools.isInArray(attributes[i].getAttrName(), keepAttrs))
            {
                attributes[i].trimAttrsExcept(keepAttrs);
            }
            else
            {
                attributes[i] = null;
            }
        }

        // Delete the marked attributes
        AttrInfo[] left = new AttrInfo[attributes.length];
        int j = 0;
        for (int i = 0; i < attributes.length; i++)
        {
            if (attributes[i] != null)
            {
                left[j++] = attributes[i];
            }
        }
        attributes = new AttrInfo[j];
        System.arraycopy(left, 0, attributes, 0, j);
        u2attributesCount = j;

        // Signal that unknown attributes are gone
        isUnkAttrGone = true;
    }
    
    /** Update the constant pool reference counts. */
    public void updateRefCount() throws Exception
    {
        constantPool.updateRefCount();
    }

    /**
     * Trim attributes from the classfile ('Code', 'Exceptions', 'ConstantValue'
     * are preserved, all others are killed).
     */
    public void trimAttrs() throws Exception {trimAttrsExcept(null);}

    /**
     * Remap SourceFile attribute to constant string "SourceFile"
     */
    public void setDummySourceFile() throws Exception
    {
        for (int i = 0; i < attributes.length; i++)
        {
            if (ATTR_SourceFile.equals(attributes[i].getAttrName())) 
            {
                ((SourceFileAttrInfo)attributes[i]).setAsDummy(constantPool);
            }
        }
    }

    /** Remove unnecessary attributes from the class. */
    public void trimAttrs(NameMapper nm) throws Exception
    {
        String[] attrs = nm.getAttrsToKeep();
        if (attrs.length > 0)
        {
            trimAttrsExcept(attrs);
        }
        else
        {
            trimAttrs();
        }
    }

    /** Remap the entities in the specified ClassFile. */
    public void remap(NameMapper nm, PrintWriter log, boolean enableMapClassString, boolean enableDummySourceFile) throws Exception
    {
        // If requested by '.option LineNumberDebug' make SourceFile attribute
        // into dummy constant string "SourceFile"
        if (enableDummySourceFile) 
        {
            setDummySourceFile();
        }

        // Go through all of class's fields and methods mapping 'name' and 'descriptor' references
        String thisClassName = ((Utf8CpInfo)getCpEntry(((ClassCpInfo)getCpEntry(u2thisClass)).getNameIndex())).getString();
        for (int i = 0; i < u2fieldsCount; i++)
        {
            // Remap field 'name', unless it is 'Synthetic'
            FieldInfo field = fields[i];
            if (!field.isSynthetic())
            {
                Utf8CpInfo nameUtf = (Utf8CpInfo)getCpEntry(field.getNameIndex());
                String remapName = nm.mapField(thisClassName, nameUtf.getString());
                field.setNameIndex(constantPool.remapUtf8To(remapName, field.getNameIndex()));
            }

            // Remap field 'descriptor'
            Utf8CpInfo descUtf = (Utf8CpInfo)getCpEntry(field.getDescriptorIndex());
            String remapDesc = nm.mapDescriptor(descUtf.getString());
            field.setDescriptorIndex(constantPool.remapUtf8To(remapDesc, field.getDescriptorIndex()));
        }
        for (int i = 0; i < u2methodsCount; i++)
        {
            // Remap method 'name', unless it is 'Synthetic'
            MethodInfo method = methods[i];
            Utf8CpInfo descUtf = (Utf8CpInfo)getCpEntry(method.getDescriptorIndex());
            if (!method.isSynthetic())
            {
                Utf8CpInfo nameUtf = (Utf8CpInfo)getCpEntry(method.getNameIndex());
                String remapName = nm.mapMethod(thisClassName, nameUtf.getString(), descUtf.getString());
                method.setNameIndex(constantPool.remapUtf8To(remapName, method.getNameIndex()));
            }

            // Remap method 'descriptor'
            String remapDesc = nm.mapDescriptor(descUtf.getString());
            method.setDescriptorIndex(constantPool.remapUtf8To(remapDesc, method.getDescriptorIndex()));
        }

        // Remap all field/method names and descriptors in the constant pool (depends on class names)
        int currentCpLength = constantPool.length(); // constant pool can be extended (never contracted) during loop
        for (int i = 0; i < currentCpLength; i++)
        {
            CpInfo cpInfo = getCpEntry(i);
            if (cpInfo != null)
            {
                // If this is a CONSTANT_Fieldref, CONSTANT_Methodref or CONSTANT_InterfaceMethodref
                // get the CONSTANT_NameAndType and remap the name and the components of the
                // descriptor string.
                if (cpInfo instanceof RefCpInfo)
                {
                    // Get the unmodified class name
                    ClassCpInfo classInfo = (ClassCpInfo)getCpEntry(((RefCpInfo)cpInfo).getClassIndex());
                    Utf8CpInfo classUtf = (Utf8CpInfo)getCpEntry(classInfo.getNameIndex());
                    String className = classUtf.getString();

                    // Get the current N&T reference and its 'name' and 'descriptor' utf's
                    int ntIndex = ((RefCpInfo)cpInfo).getNameAndTypeIndex();
                    NameAndTypeCpInfo nameTypeInfo = (NameAndTypeCpInfo)getCpEntry(ntIndex);
                    Utf8CpInfo refUtf = (Utf8CpInfo)getCpEntry(nameTypeInfo.getNameIndex());
                    Utf8CpInfo descUtf = (Utf8CpInfo)getCpEntry(nameTypeInfo.getDescriptorIndex());

                    // Get the remapped versions of 'name' and 'descriptor'
                    String remapRef;
                    if (cpInfo instanceof FieldrefCpInfo)
                    {
                        remapRef = nm.mapField(className, refUtf.getString());
                    }
                    else
                    {
                        remapRef = nm.mapMethod(className, refUtf.getString(), descUtf.getString());
                    }
                    String remapDesc = nm.mapDescriptor(descUtf.getString());

                    // If a remap is required, make a new N&T (increment ref count on 'name' and
                    // 'descriptor', decrement original N&T's ref count, set new N&T ref count to 1),
                    // remap new N&T's utf's
                    if (!remapRef.equals(refUtf.getString()) ||
                        !remapDesc.equals(descUtf.getString()))
                    {
                        // Get the new N&T guy
                        NameAndTypeCpInfo newNameTypeInfo;
                        if (nameTypeInfo.getRefCount() == 1)
                        {
                            newNameTypeInfo = nameTypeInfo;
                        }
                        else
                        {
                            // Create the new N&T info
                            newNameTypeInfo = (NameAndTypeCpInfo)nameTypeInfo.clone();

                            // Adjust its reference counts of its utf's
                            ((CpInfo)getCpEntry(newNameTypeInfo.getNameIndex())).incRefCount();
                            ((CpInfo)getCpEntry(newNameTypeInfo.getDescriptorIndex())).incRefCount();

                            // Append it to the Constant Pool, and
                            // point the RefCpInfo entry to the new N&T data
                            ((RefCpInfo)cpInfo).setNameAndTypeIndex(
                                constantPool.addEntry(newNameTypeInfo));

                            // Adjust reference counts from RefCpInfo
                            newNameTypeInfo.incRefCount();
                            nameTypeInfo.decRefCount();
                        }

                        // Remap the 'name' and 'descriptor' utf's in N&T
                        newNameTypeInfo.setNameIndex(constantPool.remapUtf8To(remapRef, newNameTypeInfo.getNameIndex()));
                        newNameTypeInfo.setDescriptorIndex(constantPool.remapUtf8To(remapDesc, newNameTypeInfo.getDescriptorIndex()));
                    }
                }
            }
        }

        // Remap all class references to Utf
        for (int i = 0; i < constantPool.length(); i++)
        {
            CpInfo cpInfo = getCpEntry(i);
            if (cpInfo != null)
            {
                // If this is CONSTANT_Class, remap the class-name Utf8 entry
                if (cpInfo instanceof ClassCpInfo)
                {
                    ClassCpInfo classInfo = (ClassCpInfo)cpInfo;
                    Utf8CpInfo utf = (Utf8CpInfo)getCpEntry(classInfo.getNameIndex());
                    String remapClass = nm.mapClass(utf.getString());
                    int remapIndex = constantPool.remapUtf8To(remapClass, classInfo.getNameIndex());
                    classInfo.setNameIndex(remapIndex);
                }
            }
        }

        // Remap all annotation type references to Utf8 classes
        for (int j = 0; j < u2attributesCount; j++)
        {
            attributes[j].remap(this, nm);
        }
        for (int i = 0; i < u2methodsCount; i++)
        {
            for (int j = 0; j < methods[i].u2attributesCount; j++)
            {
                methods[i].attributes[j].remap(this, nm);
            }
        }
        for (int i = 0; i < u2fieldsCount; i++)
        {
            for (int j = 0; j < fields[i].u2attributesCount; j++)
            {
                fields[i].attributes[j].remap(this, nm);
            }
        }

        // If reflection, attempt to remap all class string references
        // NOTE - hasReflection wasn't picking up reflection in inner classes
        //        because they call to the outer class to do forName(...).
        //        Therefore removed. 
        //if (hasReflection && enableMapClassString)
        if (/* hasReflection && */ enableMapClassString)
        {
            remapClassStrings(nm, log);
        }
    }

    // Remap Class.forName and .class, leaving other identical Strings alone
    private void remapClassStrings(NameMapper nm, PrintWriter log) throws Exception
    {
        // Visit all method Code attributes, collecting information on remap
        FlagHashtable cpToFlag = new FlagHashtable();
        for (int i = 0; i < methods.length; i++)
        {
            MethodInfo methodInfo = methods[i];
            for (int j = 0; j < methodInfo.attributes.length; j++)
            {
                AttrInfo attrInfo = methodInfo.attributes[j];
                if (attrInfo instanceof CodeAttrInfo) 
                {
                    cpToFlag = ((CodeAttrInfo)attrInfo).walkFindClassStrings(cpToFlag);
                }
            }
        }
        // Analyse String mapping flags and generate updated Strings
        Hashtable cpUpdate = new Hashtable();
        for (Enumeration enm = cpToFlag.keys(); enm.hasMoreElements(); )
        {
            StringCpInfo stringCpInfo = (StringCpInfo)enm.nextElement();
            StringCpInfoFlags flags = 
                (StringCpInfoFlags)cpToFlag.get(stringCpInfo);
            String name = backTranslate(((Utf8CpInfo)getCpEntry(stringCpInfo.getStringIndex())).getString());
            // String accessed as Class.forName or .class?
            if (isClassSpec(name) && flags.forNameFlag)
            {
                String remapName = nm.mapClass(name);
                if (!remapName.equals(name)) // skip if no remap needed
                {
                    boolean simpleRemap = false;
                    // String accessed in another way, so split in ConstantPool
                    if (flags.otherFlag)
                    {
                        // Create a new String/Utf8 for remapped Class-name
                        int remapUtf8Index = 
                            constantPool.addUtf8Entry(translate(remapName));
                        StringCpInfo remapStringInfo = new StringCpInfo();
                        remapStringInfo.setStringIndex(remapUtf8Index);
                        int remapStringIndex = 
                            constantPool.addEntry(remapStringInfo);
                        // Default to full remap if new String would require
                        // ldc_w to access - we can't cope with that yet
                        if (remapStringIndex > 0xFF)
                        {
                            simpleRemap = true;
                            log.println("# WARNING MapClassString: non-.class/Class.forName() string remapped");
                        }
                        else 
                        {
                            log.println("# MapClassString (partial) in class " 
                                        + getName() + ": " 
                                        + name + " -> " + remapName);
                            // Add to cpUpdate hash for later remap in Code
                            cpUpdate.put(new Integer(flags.stringIndex),
                                         new Integer(remapStringIndex));
                        }
                    }
                    else  // String only accessed as Class.forName
                    {
                        simpleRemap = true;
                    }
                    if (simpleRemap)
                    {
                        log.println("# MapClassString (full) in class " 
                                    + getName() + ": " 
                                    + name + " -> " + remapName);
                        // Just remap the existing String/Utf8, since it is 
                        // only used for Class.forName or .class, or maybe 
                        // ldc_w was needed (which gives improper String remap)
                        int remapIndex = constantPool.remapUtf8To(translate(remapName), stringCpInfo.getStringIndex());
                        stringCpInfo.setStringIndex(remapIndex);
                    }
                }
            }
        }
        // Visit all method Code attributes, remapping .class/Class.forName
        for (int i = 0; i < methods.length; i++)
        {
            MethodInfo methodInfo = methods[i];
            for (int j = 0; j < methodInfo.attributes.length; j++)
            {
                AttrInfo attrInfo = methodInfo.attributes[j];
                if (attrInfo instanceof CodeAttrInfo) 
                {
                    ((CodeAttrInfo)attrInfo).walkUpdateClassStrings(cpUpdate);
                }
            }
        }
    }

    // Is this String a valid class specifier?
    private static boolean isClassSpec(String s) //throws Exception
    {
        if (s.length() == 0) return false;
        int pos = -1;
        while ((pos = s.lastIndexOf('/')) != -1)
        {
            if (!isJavaIdentifier(s.substring(pos + 1))) return false;
            s = s.substring(0, pos);
        }
        if (!isJavaIdentifier(s)) return false;
        return true;
    }

    // Is this String a valid Java identifier?
    private static boolean isJavaIdentifier(String s) // throws Exception
    {
        if (s.length() == 0 || !Character.isJavaIdentifierStart(s.charAt(0)))
            return false;
        for (int i = 1; i < s.length(); i++)
            if (!Character.isJavaIdentifierPart(s.charAt(i)))
                return false;
        return true;
    }

    /** Export the representation to a DataOutput stream. */
    public void write(DataOutput dout) throws Exception
    {
        if (dout == null) throw new IOException("No output stream was provided.");
        dout.writeInt(u4magic);
        dout.writeShort(u2minorVersion);
        dout.writeShort(u2majorVersion);
        dout.writeShort(constantPool.length() + (cpIdString != null ? 1 : 0));
        for (Enumeration enm = constantPool.elements(); enm.hasMoreElements(); )
        {
            CpInfo cpInfo = (CpInfo)enm.nextElement();
            if (cpInfo != null)
            {
                cpInfo.write(dout);
            }
        }
        if (cpIdString != null) {
            cpIdString.write(dout);
        }
        dout.writeShort(u2accessFlags);
        dout.writeShort(u2thisClass);
        dout.writeShort(u2superClass);
        dout.writeShort(u2interfacesCount);
        for (int i = 0; i < u2interfacesCount; i++)
        {
            dout.writeShort(u2interfaces[i]);
        }
        dout.writeShort(u2fieldsCount);
        for (int i = 0; i < u2fieldsCount; i++)
        {
            fields[i].write(dout);
        }
        dout.writeShort(u2methodsCount);
        for (int i = 0; i < u2methodsCount; i++)
        {
            methods[i].write(dout);
        }
        dout.writeShort(u2attributesCount);
        for (int i = 0; i < u2attributesCount; i++)
        {
            attributes[i].write(dout);
        }
    }

    /** Dump the content of the class file to the specified file (used for debugging). */
    public void dump(PrintWriter pw) throws Exception
    {
        pw.println("_____________________________________________________________________");
        pw.println("CLASS: " + getName());
        pw.println("Magic: " + Integer.toHexString(u4magic));
        pw.println("Minor version: " + Integer.toHexString(u2minorVersion));
        pw.println("Major version: " + Integer.toHexString(u2majorVersion));
        pw.println();
        pw.println("CP length: " + Integer.toHexString(constantPool.length()));
        for (int i = 0; i < constantPool.length(); i++)
        {
            CpInfo cpInfo = (CpInfo)constantPool.getCpEntry(i);
            if (cpInfo != null)
            {
                cpInfo.dump(pw, this, i);
            }
        }
        pw.println("Access: " + Integer.toHexString(u2accessFlags));
        pw.println("This class: " + getName());
        pw.println("Superclass: " + getSuper());
        pw.println("Interfaces count: " + Integer.toHexString(u2interfacesCount));
        for (int i = 0; i < u2interfacesCount; i++)
        {
            CpInfo info = getCpEntry(u2interfaces[i]);
            if (info == null)
            {
                pw.println("  Interface " + Integer.toHexString(i) + ": (null)");
            }
            else
            {
                pw.println("  Interface " + Integer.toHexString(i) + ": " + ((Utf8CpInfo)getCpEntry(((ClassCpInfo)info).getNameIndex())).getString());
            }
        }
        pw.println("Fields count: " + Integer.toHexString(u2fieldsCount));
        for (int i = 0; i < u2fieldsCount; i++)
        {
            ClassItemInfo info = fields[i];
            if (info == null)
            {
                pw.println("  Field " + Integer.toHexString(i) + ": (null)");
            }
            else
            {
                pw.println("  Field " + Integer.toHexString(i) + ": " + ((Utf8CpInfo)getCpEntry(info.getNameIndex())).getString() + " " + ((Utf8CpInfo)getCpEntry(info.getDescriptorIndex())).getString());
            }
            //pw.println("    Attrs count: " + Integer.toHexString(info.u2attributesCount));
            //for (int j = 0; j < info.u2attributesCount; j++)
            //{
            //    info.attributes[j].dump(pw, this);
            //}
        }
        pw.println("Methods count: " + Integer.toHexString(u2methodsCount));
        for (int i = 0; i < u2methodsCount; i++)
        {
            ClassItemInfo info = methods[i];
            if (info == null)
            {
                pw.println("  Method " + Integer.toHexString(i) + ": (null)");
            }
            else
            {
                pw.println("  Method " + Integer.toHexString(i) + ": " + ((Utf8CpInfo)getCpEntry(info.getNameIndex())).getString() + " " + ((Utf8CpInfo)getCpEntry(info.getDescriptorIndex())).getString() + " " + Integer.toHexString(info.getAccessFlags()));
            }
            //pw.println("    Attrs count: " + Integer.toHexString(info.u2attributesCount));
            //for (int j = 0; j < info.u2attributesCount; j++)
            //{
            //    info.attributes[j].dump(pw, this);
            //}
        }
        //pw.println("Attrs count: " + Integer.toHexString(u2attributesCount));
        //for (int i = 0; i < u2attributesCount; i++)
        //{
        //    attributes[i].dump(pw, this);
        //}
    }
}
