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
import java.util.Map.Entry;

import COM.rl.util.*;

/**
 * This is a representation of the data in a Java class-file (*.class).
 * A ClassFile instance representing a *.class file can be generated using the static create(DataInput) method, manipulated using
 * various operators, and persisted back using the write(DataOutput) method.
 * 
 * @author Mark Welsh
 */
public class ClassFile implements ClassConstants
{
    // Constants -------------------------------------------------------------
    public static final String SEP_REGULAR = "/";
    public static final String SEP_INNER = "$";
    private static final String CLASS_FORNAME_NAME_DESCRIPTOR = "forName(Ljava/lang/String;)Ljava/lang/Class;";
    private static final String[] DANGEROUS_CLASS_SIMPLENAME_DESCRIPTOR_ARRAY =
    {
        "getDeclaredField(Ljava/lang/String;)Ljava/lang/reflect/Field;",
        "getField(Ljava/lang/String;)Ljava/lang/reflect/Field;",
        "getDeclaredMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;",
        "getMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;"
    };
    private static final String LOG_DANGER_CLASS_PRE = "     Your class ";
    private static final String LOG_DANGER_CLASS_MID = " calls the java/lang/Class method ";
    private static final String LOG_CLASS_FORNAME_MID = " uses '.class' or calls java/lang/Class.";
    private static final String[] DANGEROUS_CLASSLOADER_SIMPLENAME_DESCRIPTOR_ARRAY =
    {
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
    private List<Integer> u2interfaces;
    private int u2fieldsCount;
    private List<FieldInfo> fields;
    private int u2methodsCount;
    private List<MethodInfo> methods;
    private int u2attributesCount;
    private List<AttrInfo> attributes;

    private boolean isUnkAttrGone = false;
    private boolean hasReflection = false;
    private CpInfo cpIdString = null;


    // Class Methods ---------------------------------------------------------
    /**
     * Create a new ClassFile from the class file format data in the DataInput stream.
     * 
     * @param din
     * @throws IOException
     * @throws ClassFileException
     */
    public static ClassFile create(DataInput din) throws IOException, ClassFileException
    {
        if (din == null)
        {
            throw new IOException("No input stream was provided.");
        }
        ClassFile cf = new ClassFile();
        cf.read(din);
        return cf;
    }

    /**
     * Parse a method or field descriptor into a list of parameter names (for methods) and a return type, in same format as the
     * Class.forName() method returns .
     * 
     * @param descriptor
     * @throws ClassFileException
     */
    public static List<String> parseDescriptor(String descriptor) throws ClassFileException
    {
        return ClassFile.parseDescriptor(descriptor, false, true);
    }

    /**
     * Parse a method or field descriptor into a list of parameter names (for methods) and a return type, optionally in same format
     * as the Class.forName() method returns .
     * 
     * @param descriptor
     * @param isDisplay
     * @throws ClassFileException
     */
    public static List<String> parseDescriptor(String descriptor, boolean isDisplay) throws ClassFileException
    {
        return ClassFile.parseDescriptor(descriptor, isDisplay, true);
    }

    /**
     * Parse a method or field descriptor into a list of parameter names (for methods) and a return type, in same format as the
     * Class.forName() method returns .
     * 
     * @param descriptor
     * @param isDisplay
     * @param doTranslate
     * @throws ClassFileException
     */
    public static List<String> parseDescriptor(String descriptor, boolean isDisplay, boolean doTranslate) throws ClassFileException
    {
        // Check for field descriptor
        List<String> names = new ArrayList<String>();
        if (descriptor.charAt(0) != '(')
        {
            names.add(descriptor);
        }
        else
        {
            // Method descriptor
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
                        names.add(type + descriptor.substring(0, 1));
                        descriptor = descriptor.substring(1);
                        type = "";
                        break;

                    case ')':
                        descriptor = descriptor.substring(1);
                        break;

                    case 'L':
                    {
                        int pos = descriptor.indexOf(';') + 1;
                        names.add(type + descriptor.substring(0, pos));
                        descriptor = descriptor.substring(pos);
                        type = "";
                        break;
                    }

                    default:
                        throw new ClassFileException("Illegal field or method descriptor: " + descriptor);
                }
            }
        }

        if (doTranslate)
        {
            // Translate the names from JVM to Class.forName() format.
            List<String> translatedNames = new ArrayList<String>();
            for (String name : names)
            {
                translatedNames.add(ClassFile.translateType(name, isDisplay));
            }
            return translatedNames;
        }

        return names;
    }

    /**
     * Translate a type specifier from the internal JVM convention to the Class.forName() one.
     * 
     * @param inName
     * @param isDisplay
     * @throws ClassFileException
     */
    public static String translateType(String inName, boolean isDisplay) throws ClassFileException
    {
        String outName = null;
        switch (inName.charAt(0))
        {
            case '[':
                // For array types, Class.forName() inconsistently uses the internal type name but with '/' --> '.'
                if (!isDisplay)
                {
                    // return the Class.forName() form
                    outName = ClassFile.translate(inName);
                }
                else
                {
                    // return the pretty display form
                    outName = ClassFile.translateType(inName.substring(1), true) + "[]";
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
                outName = ClassFile.translate(inName.substring(1, pos));
                break;
            }

            default:
                throw new ClassFileException("Illegal field or method name: " + inName);
        }
        return outName;
    }

    /**
     * Translate a class name from the internal '/' convention to the regular '.' one.
     * 
     * @param name
     */
    public static String translate(String name)
    {
        return name.replace('/', '.');
    }

    /**
     * Translate a class name from the the regular '.' convention to internal '/' one.
     * 
     * @param name
     */
    public static String backTranslate(String name)
    {
        return name.replace('.', '/');
    }

    /**
     * Is this class in an unsupported version of the file format?
     */
    public boolean hasIncompatibleVersion()
    {
        return (this.u2majorVersion > ClassConstants.MAJOR_VERSION);
    }

    /**
     * Return major version of this class's file format.
     */
    public int getMajorVersion()
    {
        return this.u2majorVersion;
    }


    // Instance Methods ------------------------------------------------------
    /**
     * Private constructor.
     */
    private ClassFile()
    {
    }

    /**
     * Import the class data to internal representation.
     * 
     * @param din
     * @throws IOException
     * @throws ClassFileException
     */
    private void read(DataInput din) throws IOException, ClassFileException
    {
        // Read the class file
        this.u4magic = din.readInt();
        this.u2minorVersion = din.readUnsignedShort();
        this.u2majorVersion = din.readUnsignedShort();

        // Check this is a valid classfile that we can handle
        if (this.u4magic != ClassConstants.MAGIC)
        {
            throw new ClassFileException("Invalid magic number in class file.");
        }
//        if (this.u2majorVersion > ClassConstants.MAJOR_VERSION)
//        {
//            throw new ClassFileException("Incompatible version number for class file format.");
//        }

        int u2constantPoolCount = din.readUnsignedShort();
        List<CpInfo> cpInfo = new ArrayList<CpInfo>(u2constantPoolCount);
        // Fill the constant pool, recalling the zero entry is not persisted, nor are the entries following a Long or Double
        cpInfo.add(null);
        for (int i = 1; i < u2constantPoolCount; i++)
        {
            CpInfo cp = CpInfo.create(din);
            cpInfo.add(cp);
            if ((cp instanceof LongCpInfo) || (cp instanceof DoubleCpInfo))
            {
                i++;
                cpInfo.add(null);
            }
        }
        this.constantPool = new ConstantPool(this, cpInfo);

        this.u2accessFlags = din.readUnsignedShort();
        this.u2thisClass = din.readUnsignedShort();
        this.u2superClass = din.readUnsignedShort();
        this.u2interfacesCount = din.readUnsignedShort();
        this.u2interfaces = new ArrayList<Integer>(this.u2interfacesCount);
        for (int i = 0; i < this.u2interfacesCount; i++)
        {
            this.u2interfaces.add(din.readUnsignedShort());
        }
        this.u2fieldsCount = din.readUnsignedShort();
        this.fields = new ArrayList<FieldInfo>(this.u2fieldsCount);
        for (int i = 0; i < this.u2fieldsCount; i++)
        {
            this.fields.add(FieldInfo.create(din, this));
        }
        this.u2methodsCount = din.readUnsignedShort();
        this.methods = new ArrayList<MethodInfo>(this.u2methodsCount);
        for (int i = 0; i < this.u2methodsCount; i++)
        {
            this.methods.add(MethodInfo.create(din, this));
        }
        this.u2attributesCount = din.readUnsignedShort();
        this.attributes = new ArrayList<AttrInfo>(this.u2attributesCount);
        for (int i = 0; i < this.u2attributesCount; i++)
        {
            this.attributes.add(AttrInfo.create(din, this));
        }
        this.checkReflection();
    }

    /**
     * Define a constant String to include in this output class file.
     * 
     * @param id
     */
    public void setIdString(String id)
    {
        if (id != null)
        {
            this.cpIdString = new Utf8CpInfo(id);
        }
        else
        {
            this.cpIdString = null;
        }
    }

    /**
     * Check for reflection methods and set flag
     * 
     * @throws ClassFileException
     */
    private boolean checkReflection() throws ClassFileException
    {
        // Need only check CONSTANT_Methodref entries of constant pool since methods belong to classes 'Class' and 'ClassLoader',
        // not interfaces.
        for (CpInfo cpInfo : this.constantPool)
        {
            if (cpInfo instanceof MethodrefCpInfo)
            {
                // Get the method class name, simple name and descriptor
                MethodrefCpInfo entry = (MethodrefCpInfo)cpInfo;
                ClassCpInfo classEntry = (ClassCpInfo)this.getCpEntry(entry.getClassIndex());
                String className = this.getUtf8(classEntry.getNameIndex());
                NameAndTypeCpInfo ntEntry = (NameAndTypeCpInfo)this.getCpEntry(entry.getNameAndTypeIndex());
                String name = this.getUtf8(ntEntry.getNameIndex());
                String descriptor = this.getUtf8(ntEntry.getDescriptorIndex());

                // Check if this is Class.forName
                if (className.equals("java/lang/Class") && ClassFile.CLASS_FORNAME_NAME_DESCRIPTOR.equals(name + descriptor))
                {
                    this.hasReflection = true;
                }
            }
        }
        return this.hasReflection;
    }

    /**
     * Return the access modifiers for this classfile.
     */
    public int getModifiers()
    {
        return this.u2accessFlags;
    }

    /**
     * Return the name of this classfile.
     * 
     * @throws ClassFileException
     */
    public String getName() throws ClassFileException
    {
        return this.toName(this.u2thisClass);
    }

    /**
     * Return the name of this class's superclass.
     * 
     * @throws ClassFileException
     */
    public String getSuper() throws ClassFileException
    {
        // This may be java/lang/Object, in which case there is no super
        return (this.u2superClass == 0) ? null : this.toName(this.u2superClass);
    }

    /**
     * Return the names of this class's interfaces.
     * 
     * @throws ClassFileException
     */
    public List<String> getInterfaces() throws ClassFileException
    {
        List<String> interfaces = new ArrayList<String>();
        for (int intf : this.u2interfaces)
        {
            interfaces.add(this.toName(intf));
        }
        return interfaces;
    }

    /**
     * Convert a CP index to a class name.
     * 
     * @param u2index
     * @throws ClassFileException
     */
    private String toName(int u2index) throws ClassFileException
    {
        CpInfo classEntry = this.getCpEntry(u2index);
        if (classEntry instanceof ClassCpInfo)
        {
            ClassCpInfo entry = (ClassCpInfo)classEntry;
            return entry.getName(this);
        }

        throw new ClassFileException("Inconsistent Constant Pool in class file.");
    }

    /**
     * Return number of methods in class.
     */
    public int getMethodCount()
    {
        return this.methods.size();
    }

    /**
     * Return i'th method in class.
     * 
     * @param i
     */
    public MethodInfo getMethod(int i)
    {
        return this.methods.get(i);
    }

    /**
     * Return number of fields in class.
     */
    public int getFieldCount()
    {
        return this.fields.size();
    }

    /**
     * Return i'th field in class.
     * 
     * @param i
     */
    public FieldInfo getField(int i)
    {
        return this.fields.get(i);
    }

    /**
     * Lookup the entry in the constant pool and return as a {@code CpInfo}.
     * 
     * @param cpIndex
     * @throws ClassFileException
     */
    protected CpInfo getCpEntry(int cpIndex) throws ClassFileException
    {
        return this.constantPool.getCpEntry(cpIndex);
    }

    /**
     * Remap a specified Utf8 entry to the given value and return its new index.
     * 
     * @param newString
     * @param oldIndex
     * @throws ClassFileException
     */
    public int remapUtf8To(String newString, int oldIndex) throws ClassFileException
    {
        return this.constantPool.remapUtf8To(newString, oldIndex);
    }

    /**
     * Lookup the UTF8 string in the constant pool.
     * 
     * @param cpIndex
     * @throws ClassFileException
     */
    protected String getUtf8(int cpIndex) throws ClassFileException
    {
        CpInfo utf8Entry = this.getCpEntry(cpIndex);
        if (utf8Entry instanceof Utf8CpInfo)
        {
            Utf8CpInfo entry = (Utf8CpInfo)utf8Entry;
            return entry.getString();
        }

        throw new ClassFileException("Not UTF8Info");
    }

    /**
     * Lookup the UTF8 string in the constant pool. Used in debugging.
     * 
     * @param cpIndex
     */
    protected String getUtf8Debug(int cpIndex)
    {
        CpInfo utf8Entry = null;
        try
        {
            utf8Entry = this.getCpEntry(cpIndex);
        }
        catch (ClassFileException e)
        {
            return "[bad Utf8: " + cpIndex + "]";
        }
        if (utf8Entry instanceof Utf8CpInfo)
        {
            Utf8CpInfo entry = (Utf8CpInfo)utf8Entry;
            return entry.getString();
        }

        return "[bad Utf8: " + cpIndex + "]";
    }

    /**
     * Does this class contain reflection methods?
     */
    public boolean hasReflection()
    {
        return this.hasReflection;
    }

    /**
     * List methods which can break obfuscated code, and log to a {@code List<String>}.
     */
    public List<String> getDangerousMethods()
    {
        return this.listDangerMethods(new ArrayList<String>());
    }

    /**
     * List methods which can break obfuscated code, and log to a {@code List<String>}.
     * 
     * @param list
     */
    public List<String> listDangerMethods(List<String> list)
    {
        // Need only check CONSTANT_Methodref entries of constant pool since dangerous methods belong to classes 'Class' and
        // 'ClassLoader', not to interfaces.
        for (CpInfo cpInfo : this.constantPool)
        {
            if (cpInfo instanceof MethodrefCpInfo)
            {
                try
                {
                    // Get the method class name, simple name and descriptor
                    MethodrefCpInfo entry = (MethodrefCpInfo)cpInfo;
                    ClassCpInfo classEntry = (ClassCpInfo)this.getCpEntry(entry.getClassIndex());
                    String className = this.getUtf8(classEntry.getNameIndex());
                    NameAndTypeCpInfo ntEntry = (NameAndTypeCpInfo)this.getCpEntry(entry.getNameAndTypeIndex());
                    String name = this.getUtf8(ntEntry.getNameIndex());
                    String descriptor = this.getUtf8(ntEntry.getDescriptorIndex());

                    // Check if this is on the proscribed list
                    if (className.equals("java/lang/Class"))
                    {
                        if (ClassFile.CLASS_FORNAME_NAME_DESCRIPTOR.equals(name + descriptor))
                        {
                            list.add(ClassFile.LOG_DANGER_CLASS_PRE + this.getName() + ClassFile.LOG_CLASS_FORNAME_MID
                                + ClassFile.CLASS_FORNAME_NAME_DESCRIPTOR);
                        }
                        else if (Arrays.asList(ClassFile.DANGEROUS_CLASS_SIMPLENAME_DESCRIPTOR_ARRAY).contains(name + descriptor))
                        {
                            list.add(ClassFile.LOG_DANGER_CLASS_PRE + this.getName() + ClassFile.LOG_DANGER_CLASS_MID
                                + name + descriptor);
                        }
                    }
                    else if (Arrays.asList(ClassFile.DANGEROUS_CLASSLOADER_SIMPLENAME_DESCRIPTOR_ARRAY).contains(name + descriptor))
                    {
                        list.add(ClassFile.LOG_DANGER_CLASSLOADER_PRE + this.getName() + ClassFile.LOG_DANGER_CLASSLOADER_MID
                            + name + descriptor);
                    }
                }
                catch (ClassFileException e)
                {
                    // TODO printStackTrace
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    /**
     * Check for direct references to Utf8 constant pool entries.
     * 
     * @throws ClassFileException
     */
    public void markUtf8Refs() throws ClassFileException
    {
        // Check for references to Utf8 from outside the constant pool
        for (FieldInfo fd : this.fields)
        {
            fd.markUtf8Refs(this.constantPool);
        }
        for (MethodInfo md : this.methods)
        {
            md.markUtf8Refs(this.constantPool);
        }
        for (AttrInfo at : this.attributes)
        {
            at.markUtf8Refs(this.constantPool);
        }

        // Now check for references from other CP entries
        for (CpInfo cpInfo : this.constantPool)
        {
            if ((cpInfo instanceof NameAndTypeCpInfo) || (cpInfo instanceof ClassCpInfo) || (cpInfo instanceof StringCpInfo))
            {
                cpInfo.markUtf8Refs(this.constantPool);
            }
        }
    }

    /**
     * Check for direct references to NameAndType constant pool entries.
     * 
     * @throws ClassFileException
     */
    public void markNTRefs() throws ClassFileException
    {
        // Now check the method and field CP entries
        for (CpInfo cpInfo : this.constantPool)
        {
            if (cpInfo instanceof RefCpInfo)
            {
                cpInfo.markNTRefs(this.constantPool);
            }
        }
    }

    /**
     * Trim attributes from the classfile ('Code', 'Exceptions', 'ConstantValue' are preserved, all others except those in the
     * {@code List<String>} are killed).
     * 
     * @param keepAttrs
     */
    public void trimAttrsExcept(List<String> keepAttrs)
    {
        // Merge additional attributes with required list
        keepAttrs.addAll(Arrays.asList(ClassConstants.REQUIRED_ATTRS));

        // Traverse all attributes, removing all except those on 'keep' list
        for (FieldInfo fd : this.fields)
        {
            fd.trimAttrsExcept(keepAttrs);
        }
        for (MethodInfo md : this.methods)
        {
            md.trimAttrsExcept(keepAttrs);
        }

        List<AttrInfo> delAttrs = new ArrayList<AttrInfo>();
        for (AttrInfo at : this.attributes)
        {
            if (keepAttrs.contains(at.getAttrName()))
            {
                at.trimAttrsExcept(keepAttrs);
            }
            else
            {
                delAttrs.add(at);
            }
        }

        this.attributes.removeAll(delAttrs);

        this.u2attributesCount = this.attributes.size();

        // Signal that unknown attributes are gone
        this.isUnkAttrGone = true;
    }

    /**
     * Update the constant pool reference counts.
     * 
     * @throws ClassFileException
     */
    public void updateRefCount() throws ClassFileException
    {
        this.constantPool.updateRefCount();
    }

    /**
     * Trim attributes from the classfile ('Code', 'Exceptions', 'ConstantValue' are preserved, all others are killed).
     */
    public void trimAttrs()
    {
        this.trimAttrsExcept(Collections.<String>emptyList());
    }

    /**
     * Remove unnecessary attributes from the class.
     * 
     * @param nm
     */
    public void trimAttrs(NameMapper nm)
    {
        List<String> attrs = nm.getAttrsToKeep();
        this.trimAttrsExcept(attrs);
    }

    /**
     * Remap the entities in the specified ClassFile.
     * 
     * @param nm
     * @param log
     * @param enableMapClassString
     * @throws ClassFileException
     */
    public void remap(NameMapper nm, PrintWriter log, boolean enableMapClassString) throws ClassFileException
    {
        // Go through all of class's fields and methods mapping 'name' and 'descriptor' references
        ClassCpInfo cls = (ClassCpInfo)this.getCpEntry(this.u2thisClass);
        String thisClassName = this.getUtf8(cls.getNameIndex());
        for (FieldInfo fd : this.fields)
        {
            // Remap field 'name', unless it is 'Synthetic'
            if (!fd.isSynthetic())
            {
                String name = this.getUtf8(fd.getNameIndex());
                String remapName = nm.mapField(thisClassName, name);
                fd.setNameIndex(this.constantPool.remapUtf8To(remapName, fd.getNameIndex()));
            }

            // Remap field 'descriptor'
            String desc = this.getUtf8(fd.getDescriptorIndex());
            String remapDesc = nm.mapDescriptor(desc);
            fd.setDescriptorIndex(this.constantPool.remapUtf8To(remapDesc, fd.getDescriptorIndex()));
        }
        for (MethodInfo md : this.methods)
        {
            // Remap method 'name', unless it is 'Synthetic'
            String desc = this.getUtf8(md.getDescriptorIndex());
            if (!md.isSynthetic())
            {
                String name = this.getUtf8(md.getNameIndex());
                String remapName = nm.mapMethod(thisClassName, name, desc);
                md.setNameIndex(this.constantPool.remapUtf8To(remapName, md.getNameIndex()));
            }

            // Remap method 'descriptor'
            String remapDesc = nm.mapDescriptor(desc);
            md.setDescriptorIndex(this.constantPool.remapUtf8To(remapDesc, md.getDescriptorIndex()));
        }

        // Remap all field/method names and descriptors in the constant pool (depends on class names)
        int currentCpLength = this.constantPool.length(); // constant pool can be extended (never contracted) during loop
        for (int i = 0; i < currentCpLength; i++)
        {
            CpInfo cpInfo = this.getCpEntry(i);
            // If this is a CONSTANT_Fieldref, CONSTANT_Methodref or CONSTANT_InterfaceMethodref get the CONSTANT_NameAndType
            // and remap the name and the components of the descriptor string.
            if (cpInfo instanceof RefCpInfo)
            {
                // Get the unmodified class name
                RefCpInfo refInfo = (RefCpInfo)cpInfo;
                ClassCpInfo classInfo = (ClassCpInfo)this.getCpEntry(refInfo.getClassIndex());
                String className = this.getUtf8(classInfo.getNameIndex());

                // Get the current N&T reference and its 'name' and 'descriptor' utf's
                int ntIndex = refInfo.getNameAndTypeIndex();
                NameAndTypeCpInfo nameTypeInfo = (NameAndTypeCpInfo)this.getCpEntry(ntIndex);
                String ref = this.getUtf8(nameTypeInfo.getNameIndex());
                String desc = this.getUtf8(nameTypeInfo.getDescriptorIndex());

                // Get the remapped versions of 'name' and 'descriptor'
                String remapRef;
                if (cpInfo instanceof FieldrefCpInfo)
                {
                    remapRef = nm.mapField(className, ref);
                }
                else
                {
                    remapRef = nm.mapMethod(className, ref, desc);
                }
                String remapDesc = nm.mapDescriptor(desc);

                // If a remap is required, make a new N&T (increment ref count on 'name' and 'descriptor', decrement original
                // N&T's ref count, set new N&T ref count to 1), remap new N&T's utf's
                if (!remapRef.equals(ref) || !remapDesc.equals(desc))
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
                        this.getCpEntry(newNameTypeInfo.getNameIndex()).incRefCount();
                        this.getCpEntry(newNameTypeInfo.getDescriptorIndex()).incRefCount();

                        // Append it to the Constant Pool, and point the RefCpInfo entry to the new N&T data
                        refInfo.setNameAndTypeIndex(this.constantPool.addEntry(newNameTypeInfo));

                        // Adjust reference counts from RefCpInfo
                        newNameTypeInfo.incRefCount();
                        nameTypeInfo.decRefCount();
                    }

                    // Remap the 'name' and 'descriptor' utf's in N&T
                    newNameTypeInfo.setNameIndex(this.constantPool.remapUtf8To(remapRef, newNameTypeInfo.getNameIndex()));
                    newNameTypeInfo.setDescriptorIndex(this.constantPool.remapUtf8To(remapDesc,
                        newNameTypeInfo.getDescriptorIndex()));
                }
            }
        }

        // Remap all class references to Utf
        for (int i = 0; i < this.constantPool.length(); i++)
        {
            CpInfo cpInfo = this.getCpEntry(i);
            // If this is CONSTANT_Class, remap the class-name Utf8 entry
            if (cpInfo instanceof ClassCpInfo)
            {
                ClassCpInfo classInfo = (ClassCpInfo)cpInfo;
                String className = this.getUtf8(classInfo.getNameIndex());
                String remapClass = nm.mapClass(className);
                int remapIndex = this.constantPool.remapUtf8To(remapClass, classInfo.getNameIndex());
                classInfo.setNameIndex(remapIndex);
            }
        }

        // Remap all annotation type references to Utf8 classes
        for (AttrInfo at : this.attributes)
        {
            at.remap(this, nm);
        }
        for (MethodInfo md : this.methods)
        {
            for (AttrInfo at : md.attributes)
            {
                at.remap(this, nm);
            }
        }
        for (FieldInfo fd : this.fields)
        {
            for (AttrInfo at : fd.attributes)
            {
                at.remap(this, nm);
            }
        }

        // If reflection, attempt to remap all class string references
        // NOTE - hasReflection wasn't picking up reflection in inner classes because they call to the outer class to do
        // forName(...). Therefore removed.
//        if (hasReflection && enableMapClassString)
        if (enableMapClassString)
        {
            this.remapClassStrings(nm, log);
        }
    }

    /**
     * Remap Class.forName and .class, leaving other identical Strings alone
     * 
     * @param nm
     * @param log
     * @throws ClassFileException
     */
    private void remapClassStrings(NameMapper nm, PrintWriter log) throws ClassFileException
    {
        // Visit all method Code attributes, collecting information on remap
        FlagHashtable cpToFlag = new FlagHashtable();
        for (MethodInfo methodInfo : this.methods)
        {
            for (AttrInfo attrInfo : methodInfo.attributes)
            {
                if (attrInfo instanceof CodeAttrInfo)
                {
                    cpToFlag = ((CodeAttrInfo)attrInfo).walkFindClassStrings(cpToFlag);
                }
            }
        }
        // Analyse String mapping flags and generate updated Strings
        Map<Integer, Integer> cpUpdate = new HashMap<Integer, Integer>();
        for (Entry<CpInfo, StringCpInfoFlags> entry : cpToFlag.entrySet())
        {
            StringCpInfo stringCpInfo = (StringCpInfo)entry.getKey();
            StringCpInfoFlags flags = entry.getValue();
            String name = ClassFile.backTranslate(this.getUtf8(stringCpInfo.getStringIndex()));
            // String accessed as Class.forName or .class?
            if (ClassFile.isClassSpec(name) && flags.forNameFlag)
            {
                String remapName = nm.mapClass(name);
                if (!remapName.equals(name)) // skip if no remap needed
                {
                    boolean simpleRemap = false;
                    // String accessed in another way, so split in ConstantPool
                    if (flags.otherFlag)
                    {
                        // Create a new String/Utf8 for remapped Class-name
                        int remapUtf8Index = this.constantPool.addUtf8Entry(ClassFile.translate(remapName));
                        StringCpInfo remapStringInfo = new StringCpInfo();
                        remapStringInfo.setStringIndex(remapUtf8Index);
                        int remapStringIndex = this.constantPool.addEntry(remapStringInfo);
                        // Default to full remap if new String would require ldc_w to access - we can't cope with that yet
                        if (remapStringIndex > 0xFF)
                        {
                            simpleRemap = true;
                            log.println("# WARNING MapClassString: non-.class/Class.forName() string remapped");
                        }
                        else
                        {
                            log.println("# MapClassString (partial) in class " + this.getName() + ": " + name + " -> " + remapName);
                            // Add to cpUpdate hash for later remap in Code
                            cpUpdate.put(new Integer(flags.stringIndex), new Integer(remapStringIndex));
                        }
                    }
                    else
                    // String only accessed as Class.forName
                    {
                        simpleRemap = true;
                    }
                    if (simpleRemap)
                    {
                        log.println("# MapClassString (full) in class " + this.getName() + ": " + name + " -> " + remapName);
                        // Just remap the existing String/Utf8, since it is only used for Class.forName or .class, or maybe ldc_w
                        // was needed (which gives improper String remap)
                        int remapIndex = this.constantPool.remapUtf8To(ClassFile.translate(remapName),
                            stringCpInfo.getStringIndex());
                        stringCpInfo.setStringIndex(remapIndex);
                    }
                }
            }
        }
        // Visit all method Code attributes, remapping .class/Class.forName
        for (MethodInfo methodInfo : this.methods)
        {
            for (AttrInfo attrInfo : methodInfo.attributes)
            {
                if (attrInfo instanceof CodeAttrInfo)
                {
                    CodeAttrInfo codeAttrInfo = (CodeAttrInfo)attrInfo;
                    codeAttrInfo.walkUpdateClassStrings(cpUpdate);
                }
            }
        }
    }

    /**
     * Is this String a valid class specifier?
     * 
     * @param s
     */
    private static boolean isClassSpec(String s)
    {
        if (s.length() == 0)
        {
            return false;
        }
        int pos = -1;
        while ((pos = s.lastIndexOf('/')) != -1)
        {
            if (!ClassFile.isJavaIdentifier(s.substring(pos + 1)))
            {
                return false;
            }
            s = s.substring(0, pos);
        }
        if (!ClassFile.isJavaIdentifier(s))
        {
            return false;
        }
        return true;
    }

    /**
     * Is this String a valid Java identifier?
     * 
     * @param s
     */
    private static boolean isJavaIdentifier(String s)
    {
        if ((s.length() == 0) || !Character.isJavaIdentifierStart(s.charAt(0)))
        {
            return false;
        }
        for (int i = 1; i < s.length(); i++)
        {
            if (!Character.isJavaIdentifierPart(s.charAt(i)))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Export the representation to a DataOutput stream.
     * 
     * @param dout
     * @throws IOException
     * @throws ClassFileException
     */
    public void write(DataOutput dout) throws IOException, ClassFileException
    {
        if (dout == null)
        {
            throw new IOException("No output stream was provided.");
        }
        dout.writeInt(this.u4magic);
        dout.writeShort(this.u2minorVersion);
        dout.writeShort(this.u2majorVersion);
        dout.writeShort(this.constantPool.length() + (this.cpIdString != null ? 1 : 0));
        for (CpInfo cpInfo : this.constantPool)
        {
            if (cpInfo != null)
            {
                cpInfo.write(dout);
            }
        }
        if (this.cpIdString != null)
        {
            this.cpIdString.write(dout);
        }
        dout.writeShort(this.u2accessFlags);
        dout.writeShort(this.u2thisClass);
        dout.writeShort(this.u2superClass);
        dout.writeShort(this.u2interfacesCount);
        for (int intf : this.u2interfaces)
        {
            dout.writeShort(intf);
        }
        dout.writeShort(this.u2fieldsCount);
        for (FieldInfo fd : this.fields)
        {
            fd.write(dout);
        }
        dout.writeShort(this.u2methodsCount);
        for (MethodInfo md : this.methods)
        {
            md.write(dout);
        }
        dout.writeShort(this.u2attributesCount);
        for (AttrInfo at : this.attributes)
        {
            at.write(dout);
        }
    }

    /**
     * Dump the content of the class file to the specified file (used for debugging).
     * 
     * @param pw
     * @throws ClassFileException
     */
    public void dump(PrintWriter pw) throws ClassFileException
    {
        // TODO ClassFileException
        pw.println("_____________________________________________________________________");
        pw.println("CLASS: " + this.getName());
        pw.println("Magic: " + Integer.toHexString(this.u4magic));
        pw.println("Minor version: " + Integer.toHexString(this.u2minorVersion));
        pw.println("Major version: " + Integer.toHexString(this.u2majorVersion));
        pw.println();
        pw.println("CP length: " + Integer.toHexString(this.constantPool.length()));
        for (int i = 0; i < this.constantPool.length(); i++)
        {
            CpInfo cpInfo = null;
            try
            {
                cpInfo = this.constantPool.getCpEntry(i);
            }
            catch (ClassFileException e)
            {
                // ignore
            }
            if (cpInfo != null)
            {
                cpInfo.dump(pw, this, i);
            }
        }
        pw.println("Access: " + Integer.toHexString(this.u2accessFlags));
        pw.println("This class: " + this.getName());
        pw.println("Superclass: " + this.getSuper());
        pw.println("Interfaces count: " + Integer.toHexString(this.u2interfacesCount));
        for (int i = 0; i < this.u2interfacesCount; i++)
        {
            int intf = this.u2interfaces.get(i);
            CpInfo info = this.getCpEntry(intf);
            if (info == null)
            {
                pw.println("  Interface " + Integer.toHexString(i) + ": (null)");
            }
            else
            {
                pw.println("  Interface " + Integer.toHexString(i) + ": "
                    + this.getUtf8Debug(((ClassCpInfo)info).getNameIndex()));
            }
        }
        pw.println("Fields count: " + Integer.toHexString(this.u2fieldsCount));
        for (int i = 0; i < this.u2fieldsCount; i++)
        {
            ClassItemInfo info = this.fields.get(i);
            if (info == null)
            {
                pw.println("  Field " + Integer.toHexString(i) + ": (null)");
            }
            else
            {
                pw.println("  Field " + Integer.toHexString(i) + ": "
                    + this.getUtf8Debug(info.getNameIndex()) + " "
                    + this.getUtf8Debug(info.getDescriptorIndex()));
                pw.println("    Attrs count: " + Integer.toHexString(info.u2attributesCount));
//                for (int j = 0; j < info.u2attributesCount; j++)
//                {
//                    info.attributes.get(j).dump(pw, this);
//                }
            }
        }
        pw.println("Methods count: " + Integer.toHexString(this.u2methodsCount));
        for (int i = 0; i < this.u2methodsCount; i++)
        {
            ClassItemInfo info = this.methods.get(i);
            if (info == null)
            {
                pw.println("  Method " + Integer.toHexString(i) + ": (null)");
            }
            else
            {
                pw.println("  Method " + Integer.toHexString(i) + ": "
                    + this.getUtf8Debug(info.getNameIndex()) + " "
                    + this.getUtf8Debug(info.getDescriptorIndex()) + " "
                    + Integer.toHexString(info.getAccessFlags()));
                pw.println("    Attrs count: " + Integer.toHexString(info.u2attributesCount));
//                for (int j = 0; j < info.u2attributesCount; j++)
//                {
//                    info.attributes.get(j).dump(pw, this);
//                }
            }
        }
        pw.println("Attrs count: " + Integer.toHexString(this.u2attributesCount));
//        for (int i = 0; i < this.u2attributesCount; i++)
//        {
//            this.attributes.get(i).dump(pw, this);
//        }
    }
}
