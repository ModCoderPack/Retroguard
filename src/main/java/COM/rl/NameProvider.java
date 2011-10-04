package COM.rl;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;

import COM.rl.obf.*;
import COM.rl.obf.classfile.ClassFile;
import COM.rl.obf.classfile.ClassFileException;

public class NameProvider
{
    public static final int CLASSIC_MODE = 0;
    public static final int CHANGE_NOTHING_MODE = 1;
    public static final int DEOBFUSCATION_MODE = 2;
    public static final int REOBFUSCATION_MODE = 3;

    public static final String DEFAULT_CFG_FILE_NAME = "retroguard.cfg";

    public static int uniqueStart = 100000;
    public static int currentMode = NameProvider.CLASSIC_MODE;
    public static boolean quiet = false;
    public static boolean oldHash = false;
    public static boolean repackage = false;

    private static Set<File> obfFiles = new HashSet<File>();
    private static Set<File> reobFiles = new HashSet<File>();
    private static File npLog = null;
    private static File roLog = null;

    private static List<String> protectedPackages = new ArrayList<String>();
    private static Map<String, String> packageNameLookup = new HashMap<String, String>();

    private static List<PackageEntry> packageDefs = new ArrayList<PackageEntry>();
    private static List<ClassEntry> classDefs = new ArrayList<ClassEntry>();
    private static List<MethodEntry> methodDefs = new ArrayList<MethodEntry>();
    private static List<FieldEntry> fieldDefs = new ArrayList<FieldEntry>();

    private static Map<String, PackageEntry> packagesObf2Deobf = new HashMap<String, PackageEntry>();
    private static Map<String, PackageEntry> packagesDeobf2Obf = new HashMap<String, PackageEntry>();
    private static Map<String, ClassEntry> classesObf2Deobf = new HashMap<String, ClassEntry>();
    private static Map<String, ClassEntry> classesDeobf2Obf = new HashMap<String, ClassEntry>();
    private static Map<String, MethodEntry> methodsObf2Deobf = new HashMap<String, MethodEntry>();
    private static Map<String, MethodEntry> methodsDeobf2Obf = new HashMap<String, MethodEntry>();
    private static Map<String, FieldEntry> fieldsObf2Deobf = new HashMap<String, FieldEntry>();
    private static Map<String, FieldEntry> fieldsDeobf2Obf = new HashMap<String, FieldEntry>();

    public static String[] parseCommandLine(String[] args)
    {
        if ((args.length > 0) && (args[0].equalsIgnoreCase("-searge") || args[0].equalsIgnoreCase("-notch")))
        {
            return NameProvider.parseNameSheetModeArgs(args);
        }

        if (args.length < 5)
        {
            return args;
        }

        int idx;
        try
        {
            idx = Integer.parseInt(args[4]);
        }
        catch (NumberFormatException e)
        {
            System.err.println("ERROR: Invalid start index: " + args[4]);
            throw e;
        }

        NameProvider.uniqueStart = idx;

        String[] newArgs = new String[4];

        for (int i = 0; i < 4; ++i)
        {
            newArgs[i] = args[i];
        }

        return newArgs;
    }

    private static String[] parseNameSheetModeArgs(String[] args)
    {
        if (args.length < 2)
        {
            return null;
        }

        String configFileName = args[1];
        File configFile = new File(configFileName);
        if (!configFile.exists() || !configFile.isFile())
        {
            System.err.println("ERROR: could not find config file " + configFileName);
            return null;
        }

        String reobinput = null;
        String reoboutput = null;
        FileReader fileReader = null;
        BufferedReader reader = null;
        String[] newArgs = new String[4];
        try
        {
            fileReader = new FileReader(configFile);
            reader = new BufferedReader(fileReader);
            String line = "";
            while (line != null)
            {
                line = reader.readLine();
                if ((line == null) || line.trim().startsWith("#"))
                {
                    continue;
                }

                String[] defines = line.split("=");
                if (defines.length > 1)
                {
                    defines[1] = line.substring(defines[0].length() + 1).trim();
                    defines[0] = defines[0].trim();

                    if (defines[0].equalsIgnoreCase("obf"))
                    {
                        File obfFile = new File(defines[1]);
                        if (obfFile.isFile())
                        {
                            NameProvider.obfFiles.add(obfFile);
                        }
                        else
                        {
                            System.err.println("ERROR: could not find obf file " + defines[1]);
                            return null;
                        }
                    }
                    else if (defines[0].equalsIgnoreCase("packages"))
                    {
                        File packagesFile = new File(defines[1]);
                        if (packagesFile.isFile())
                        {
                            NameProvider.obfFiles.add(packagesFile);
                        }
                        else
                        {
                            System.err.println("ERROR: could not find packages file " + defines[1]);
                            return null;
                        }
                    }
                    else if (defines[0].equalsIgnoreCase("classes"))
                    {
                        File classesFile = new File(defines[1]);
                        if (classesFile.isFile())
                        {
                            NameProvider.obfFiles.add(classesFile);
                        }
                        else
                        {
                            System.err.println("ERROR: could not find classes file " + defines[1]);
                            return null;
                        }
                    }
                    else if (defines[0].equalsIgnoreCase("methods"))
                    {
                        File methodsFile = new File(defines[1]);
                        if (methodsFile.isFile())
                        {
                            NameProvider.obfFiles.add(methodsFile);
                        }
                        else
                        {
                            System.err.println("ERROR: could not find methods file " + defines[1]);
                            return null;
                        }
                    }
                    else if (defines[0].equalsIgnoreCase("fields"))
                    {
                        File fieldsFile = new File(defines[1]);
                        if (fieldsFile.isFile())
                        {
                            NameProvider.obfFiles.add(fieldsFile);
                        }
                        else
                        {
                            System.err.println("ERROR: could not find fields file " + defines[1]);
                            return null;
                        }
                    }
                    else if (defines[0].equalsIgnoreCase("reob"))
                    {
                        File reobFile = new File(defines[1]);
                        if (reobFile.isFile())
                        {
                            NameProvider.reobFiles.add(reobFile);
                        }
                        else
                        {
                            System.err.println("ERROR: could not find reob file " + defines[1]);
                            return null;
                        }
                    }
                    else if (defines[0].equalsIgnoreCase("input"))
                    {
                        newArgs[0] = defines[1];
                    }
                    else if (defines[0].equalsIgnoreCase("output"))
                    {
                        newArgs[1] = defines[1];
                    }
                    else if (defines[0].equalsIgnoreCase("reobinput"))
                    {
                        reobinput = defines[1];
                    }
                    else if (defines[0].equalsIgnoreCase("reoboutput"))
                    {
                        reoboutput = defines[1];
                    }
                    else if (defines[0].equalsIgnoreCase("script"))
                    {
                        newArgs[2] = defines[1];
                    }
                    else if (defines[0].equalsIgnoreCase("log"))
                    {
                        newArgs[3] = defines[1];
                    }
                    else if (defines[0].equalsIgnoreCase("nplog"))
                    {
                        NameProvider.npLog = new File(defines[1]);
                        if (NameProvider.npLog.exists() && !NameProvider.npLog.isFile())
                        {
                            NameProvider.npLog = null;
                        }
                    }
                    else if (defines[0].equalsIgnoreCase("rolog"))
                    {
                        NameProvider.roLog = new File(defines[1]);
                        if (NameProvider.roLog.exists() && !NameProvider.roLog.isFile())
                        {
                            NameProvider.roLog = null;
                        }
                    }
                    else if (defines[0].equalsIgnoreCase("startindex"))
                    {
                        try
                        {
                            int start = Integer.parseInt(defines[1]);
                            NameProvider.uniqueStart = start;
                        }
                        catch (NumberFormatException e)
                        {
                            System.err.println("Invalid start index: " + defines[1]);
                            return null;
                        }
                    }
                    else if (defines[0].equalsIgnoreCase("protectedpackage"))
                    {
                        NameProvider.protectedPackages.add(defines[1]);
                    }
                    else if (defines[0].equalsIgnoreCase("quiet"))
                    {
                        String value = defines[1].substring(0, 1);
                        if (value.equalsIgnoreCase("1") || value.equalsIgnoreCase("t") || value.equalsIgnoreCase("y"))
                        {
                            NameProvider.quiet = true;
                        }
                    }
                    else if (defines[0].equalsIgnoreCase("oldhash"))
                    {
                        String value = defines[1].substring(0, 1);
                        if (value.equalsIgnoreCase("1") || value.equalsIgnoreCase("t") || value.equalsIgnoreCase("y"))
                        {
                            NameProvider.oldHash = true;
                        }
                    }
                }
            }
        }
        catch (IOException e)
        {
            return null;
        }
        finally
        {
            try
            {
                if (reader != null)
                {
                    reader.close();
                }
                if (fileReader != null)
                {
                    fileReader.close();
                }
            }
            catch (IOException e)
            {
                // ignore
            }
        }

        if (args[0].equalsIgnoreCase("-searge"))
        {
            NameProvider.currentMode = NameProvider.DEOBFUSCATION_MODE;
        }
        else if (args[0].equalsIgnoreCase("-notch"))
        {
            NameProvider.currentMode = NameProvider.REOBFUSCATION_MODE;
        }
        else
        {
            return null;
        }

        if (NameProvider.currentMode == NameProvider.REOBFUSCATION_MODE)
        {
            newArgs[0] = reobinput;
            newArgs[1] = reoboutput;
        }

        if ((newArgs[0] == null) || (newArgs[1] == null) || (newArgs[2] == null) || (newArgs[3] == null))
        {
            return null;
        }

        try
        {
            NameProvider.initLogfiles();
            NameProvider.readSRGFiles();
        }
        catch (IOException e)
        {
            return null;
        }

        return newArgs;
    }

    private static void initLogfiles() throws IOException
    {
        File logFile = null;
        if (NameProvider.currentMode == NameProvider.DEOBFUSCATION_MODE)
        {
            logFile = NameProvider.npLog;
        }
        else if (NameProvider.currentMode == NameProvider.REOBFUSCATION_MODE)
        {
            logFile = NameProvider.roLog;
        }

        if (logFile != null)
        {
            FileWriter writer = null;
            try
            {
                writer = new FileWriter(logFile);
            }
            finally
            {
                if (writer != null)
                {
                    try
                    {
                        writer.close();
                    }
                    catch (IOException e)
                    {
                        // ignore
                    }
                }
            }
        }
    }

    private static void readSRGFiles() throws IOException
    {
        if (NameProvider.currentMode == NameProvider.DEOBFUSCATION_MODE)
        {
            for (File f : NameProvider.obfFiles)
            {
                NameProvider.readSRGFile(f);
            }
        }
        else if (NameProvider.currentMode == NameProvider.REOBFUSCATION_MODE)
        {
            for (File f : NameProvider.reobFiles)
            {
                NameProvider.readSRGFile(f);
            }
        }

        NameProvider.updateAllXrefs();
    }

    private static void updateAllXrefs()
    {
        for (PackageEntry entry : NameProvider.packageDefs)
        {
            NameProvider.packagesObf2Deobf.put(entry.obfName, entry);
            NameProvider.packagesDeobf2Obf.put(entry.deobfName, entry);
        }

        for (ClassEntry entry : NameProvider.classDefs)
        {
            NameProvider.classesObf2Deobf.put(entry.obfName, entry);
            NameProvider.classesDeobf2Obf.put(entry.deobfName, entry);
        }

        for (MethodEntry entry : NameProvider.methodDefs)
        {
            NameProvider.methodsObf2Deobf.put(entry.obfName + entry.obfDesc, entry);
            NameProvider.methodsDeobf2Obf.put(entry.deobfName + entry.deobfDesc, entry);
        }

        for (FieldEntry entry : NameProvider.fieldDefs)
        {
            NameProvider.fieldsObf2Deobf.put(entry.obfName, entry);
            NameProvider.fieldsDeobf2Obf.put(entry.deobfName, entry);
        }
    }

    private static void readSRGFile(File f) throws IOException
    {
        List<String> lines = NameProvider.readAllLines(f);

        for (String line : lines)
        {
            if (line.startsWith("PK:"))
            {
                NameProvider.addPackageLine(line);
            }
            else if (line.startsWith("CL:"))
            {
                NameProvider.addClassLine(line);
            }
            else if (line.startsWith("MD:"))
            {
                NameProvider.addMethodLine(line);
            }
            else if (line.startsWith("FD:"))
            {
                NameProvider.addFieldLine(line);
            }
        }
    }

    private static void addPackageLine(String line)
    {
        String[] lineParts = line.split(" ");
        if ((lineParts.length != 3) || !lineParts[0].startsWith("PK:"))
        {
            // TODO add a warning on invalid lines
            return;
        }

        PackageEntry entry = new PackageEntry();
        if (lineParts[1].equals("."))
        {
            entry.obfName = "";
        }
        else
        {
            entry.obfName = lineParts[1];
        }
        if (lineParts[2].equals("."))
        {
            entry.deobfName = "";
        }
        else
        {
            entry.deobfName = lineParts[2];
        }
        NameProvider.packageDefs.add(entry);
    }

    private static void addClassLine(String line)
    {
        String[] lineParts = line.split(" ");
        if ((lineParts.length != 3) || !lineParts[0].startsWith("CL:"))
        {
            // TODO add a warning on invalid lines
            return;
        }

        ClassEntry entry = new ClassEntry();
        entry.obfName = lineParts[1];
        entry.deobfName = lineParts[2];
        NameProvider.classDefs.add(entry);
    }

    private static void addMethodLine(String line)
    {
        String[] lineParts = line.split(" ");
        if ((lineParts.length < 4) || !lineParts[0].startsWith("MD:"))
        {
            // TODO add a warning on invalid lines
            return;
        }

        MethodEntry entry = new MethodEntry();
        entry.obfName = lineParts[1];
        entry.obfDesc = lineParts[2];
        entry.deobfName = lineParts[3];
        if (lineParts.length > 4)
        {
            entry.deobfDesc = lineParts[4];
        }
        NameProvider.methodDefs.add(entry);
    }

    private static void addFieldLine(String line)
    {
        String[] lineParts = line.split(" ");
        if ((lineParts.length != 3) || !lineParts[0].startsWith("FD:"))
        {
            // TODO add a warning on invalid lines
            return;
        }

        FieldEntry entry = new FieldEntry();
        entry.obfName = lineParts[1];
        entry.deobfName = lineParts[2];
        NameProvider.fieldDefs.add(entry);
    }

    private static List<String> readAllLines(File file) throws IOException
    {
        List<String> lines = new ArrayList<String>();

        FileReader fileReader = null;
        BufferedReader reader = null;
        try
        {
            fileReader = new FileReader(file);
            reader = new BufferedReader(fileReader);

            String line = reader.readLine();
            while (line != null)
            {
                lines.add(line);
                line = reader.readLine();
            }
        }
        finally
        {
            try
            {
                if (reader != null)
                {
                    reader.close();
                }
                if (fileReader != null)
                {
                    fileReader.close();
                }
            }
            catch (IOException e)
            {
                // ignore
            }
        }

        return lines;
    }

    public static void log(String text)
    {
        if (!NameProvider.quiet)
        {
            System.out.println(text);
        }

        File log = null;
        if (NameProvider.currentMode == NameProvider.DEOBFUSCATION_MODE)
        {
            log = NameProvider.npLog;
        }
        else if (NameProvider.currentMode == NameProvider.REOBFUSCATION_MODE)
        {
            log = NameProvider.roLog;
        }

        if (log == null)
        {
            return;
        }

        FileWriter fileWriter = null;
        BufferedWriter writer = null;
        try
        {
            fileWriter = new FileWriter(log, true);
            writer = new BufferedWriter(fileWriter);

            writer.write(text);
            writer.newLine();

            writer.flush();
        }
        catch (IOException e)
        {
            return;
        }
        finally
        {
            try
            {
                if (writer != null)
                {
                    writer.close();
                }
                if (fileWriter != null)
                {
                    fileWriter.close();
                }
            }
            catch (IOException e)
            {
                // ignore
            }
        }
    }

    public static String getNewTreeItemName(TreeItem ti) throws ClassFileException
    {
        if (ti instanceof Pk)
        {
            return NameProvider.getNewPackageName((Pk)ti);
        }
        else if (ti instanceof Cl)
        {
            return NameProvider.getNewClassName((Cl)ti);
        }
        else if (ti instanceof Md)
        {
            return NameProvider.getNewMethodName((Md)ti);
        }
        else if (ti instanceof Fd)
        {
            return NameProvider.getNewFieldName((Fd)ti);
        }
        else
        {
            NameProvider.log("# Warning: trying to rename unknown type " + ti.getFullInName());
        }
        return null;
    }

    public static String getNewPackageName(Pk pk)
    {
        String packageName = pk.getInName();
        String fullPackageName = pk.getFullInName();
        String newPackageName = null;

        if (NameProvider.currentMode == NameProvider.CHANGE_NOTHING_MODE)
        {
            pk.setOutput();
            return null;
        }

        if (NameProvider.currentMode == NameProvider.CLASSIC_MODE)
        {
            newPackageName = "p_" + (++NameProvider.uniqueStart) + "_" + packageName;
            pk.setOutput();
            return newPackageName;
        }

//        boolean known = NameProvider.packageNameLookup.containsKey(fullPackageName);

        if (!NameProvider.isInProtectedPackage(fullPackageName))
        {
            if (NameProvider.currentMode == NameProvider.DEOBFUSCATION_MODE)
            {
                if (NameProvider.packagesObf2Deobf.containsKey(fullPackageName))
                {
                    newPackageName = NameProvider.packagesObf2Deobf.get(fullPackageName).deobfName;
                }
                else
                {
                    // check if parent got remapped
                    TreeItem parent = pk.getParent();
                    if ((parent != null) && (parent instanceof Pk) && (parent.getParent() != null))
                    {
//                        newPackageName = NameProvider.getNewPackageName(parent.getFullOutName()) + pk.getOutName();
                    }
                }
            }
            else if (NameProvider.currentMode == NameProvider.REOBFUSCATION_MODE)
            {
                if (NameProvider.packagesDeobf2Obf.containsKey(fullPackageName))
                {
                    newPackageName = NameProvider.packagesDeobf2Obf.get(fullPackageName).obfName;
                }
                else
                {
                    // check if parent got remapped
                    TreeItem parent = pk.getParent();
                    if ((parent != null) && (parent instanceof Pk) && (parent.getParent() != null))
                    {
//                        newPackageName = NameProvider.getNewPackageName(parent.getFullOutName()) + pk.getOutName();
                    }
                }
            }

            if (newPackageName != null)
            {
                NameProvider.packageNameLookup.put(fullPackageName, newPackageName);
            }
        }

        if (!NameProvider.isInProtectedPackage(fullPackageName))
        {
            pk.setOutput();
        }

        return newPackageName;
    }

    private static String getNewPackageName(String pkgName)
    {
        if (NameProvider.packageNameLookup.containsKey(pkgName))
        {
            pkgName = NameProvider.packageNameLookup.get(pkgName);
        }

        if (pkgName.equals(""))
        {
            return "";
        }

        return pkgName + ClassFile.SEP_REGULAR;
    }

    public static String getNewClassName(Cl cl)
    {
        String className = cl.getInName();
        String fullClassName = cl.getFullInName();
        String newClassName = null;

        if (NameProvider.currentMode == NameProvider.CHANGE_NOTHING_MODE)
        {
            cl.setOutput();
            return null;
        }

        if (NameProvider.currentMode == NameProvider.CLASSIC_MODE)
        {
            // don't rename anonymous inner classes
            if (!cl.isInnerClass() || !Character.isDigit(className.charAt(0)))
            {
                newClassName = "C_" + (++NameProvider.uniqueStart) + "_" + className;
            }
            cl.setOutput();
            return newClassName;
        }

        if (NameProvider.currentMode == NameProvider.DEOBFUSCATION_MODE)
        {
            if (NameProvider.classesObf2Deobf.containsKey(fullClassName))
            {
                newClassName = NameProvider.classesObf2Deobf.get(fullClassName).deobfName;
                newClassName = NameProvider.getShortName(newClassName);
            }
            else
            {
                if (!NameProvider.isInProtectedPackage(fullClassName))
                {
                    if (NameProvider.uniqueStart > 0)
                    {
                        // don't rename anonymous inner classes
                        if (!cl.isInnerClass() || !Character.isDigit(className.charAt(0)))
                        {
                            newClassName = "C_" + (NameProvider.uniqueStart++) + "_" + className;
                        }
                    }
                }
            }
        }
        else if (NameProvider.currentMode == NameProvider.REOBFUSCATION_MODE)
        {
            if (NameProvider.classesDeobf2Obf.containsKey(fullClassName))
            {
                newClassName = NameProvider.classesDeobf2Obf.get(fullClassName).obfName;
                newClassName = NameProvider.getShortName(newClassName);
            }
        }

        if (!NameProvider.isInProtectedPackage(fullClassName))
        {
            cl.setOutput();
        }

        return newClassName;
    }

    public static String getNewMethodName(Md md) throws ClassFileException
    {
        String methodName = md.getInName();
        String fullMethodName = md.getFullInName();
        String methodNameKey = fullMethodName + md.getDescriptor();
        String newMethodName = null;

        if (NameProvider.currentMode == NameProvider.CHANGE_NOTHING_MODE)
        {
            md.setOutput();
            return null;
        }

        if (NameProvider.currentMode == NameProvider.CLASSIC_MODE)
        {
            newMethodName = "func_" + (++NameProvider.uniqueStart) + "_" + methodName;
            md.setOutput();
            return newMethodName;
        }

        if (NameProvider.currentMode == NameProvider.DEOBFUSCATION_MODE)
        {
            if (NameProvider.methodsObf2Deobf.containsKey(methodNameKey))
            {
                newMethodName = NameProvider.methodsObf2Deobf.get(methodNameKey).deobfName;
                newMethodName = NameProvider.getShortName(newMethodName);
            }
            else
            {
                if (!NameProvider.isInProtectedPackage(fullMethodName))
                {
                    if (NameProvider.uniqueStart > 0)
                    {
                        newMethodName = "func_" + (NameProvider.uniqueStart++) + "_" + methodName;
                    }
                }
            }
        }
        else if (NameProvider.currentMode == NameProvider.REOBFUSCATION_MODE)
        {
            if (NameProvider.methodsDeobf2Obf.containsKey(methodNameKey))
            {
                newMethodName = NameProvider.methodsDeobf2Obf.get(methodNameKey).obfName;
                newMethodName = NameProvider.getShortName(newMethodName);
            }
            else
            {
                Cl cls = (Cl)md.getParent();
                Iterator<Cl> children = cls.getDownClasses();

                Md tmpMd = new Md(cls, md.isSynthetic(), md.getInName(), md.getDescriptor(), md.getModifiers());

                String tmpMethodKey = tmpMd.getFullInName() + tmpMd.getDescriptor();

                boolean goingDown = false;
                do
                {
                    tmpMd.setParent(cls);
                    tmpMethodKey = tmpMd.getFullInName() + tmpMd.getDescriptor();
                    if (NameProvider.methodsDeobf2Obf.containsKey(tmpMethodKey))
                    {
                        newMethodName = NameProvider.methodsDeobf2Obf.get(tmpMethodKey).obfName;
                        newMethodName = NameProvider.getShortName(newMethodName);
                        break;
                    }

                    boolean found = false;
                    try
                    {
                        for (Cl iface : cls.getSuperInterfaces())
                        {
                            tmpMd.setParent(iface);
                            tmpMethodKey = tmpMd.getFullInName() + tmpMd.getDescriptor();
                            if (NameProvider.methodsDeobf2Obf.containsKey(tmpMethodKey))
                            {
                                newMethodName = NameProvider.methodsDeobf2Obf.get(tmpMethodKey).obfName;
                                newMethodName = NameProvider.getShortName(newMethodName);
                                found = true;
                            }
                        }
                    }
                    catch (ClassFileException e)
                    {
                        // ignore
                    }

                    if (found)
                    {
                        break;
                    }

                    if (!goingDown)
                    {
                        try
                        {
                            cls = cls.getSuperCl();
                        }
                        catch (ClassFileException e)
                        {
                            // ignore
                            cls = null;
                        }

                        if (cls == null)
                        {
                            goingDown = true;
                        }
                    }

                    if (goingDown)
                    {
                        if (children.hasNext())
                        {
                            cls = children.next();
                        }
                        else
                        {
                            cls = null;
                        }
                    }
                } while (cls != null);
            }
        }

        if (!NameProvider.isInProtectedPackage(fullMethodName))
        {
            md.setOutput();
        }

        return newMethodName;
    }

    public static String getNewFieldName(Fd fd)
    {
        String fieldName = fd.getInName();
        String fullFieldName = fd.getFullInName();
        String newFieldName = null;

        if (NameProvider.currentMode == NameProvider.CHANGE_NOTHING_MODE)
        {
            fd.setOutput();
            return null;
        }

        if (NameProvider.currentMode == NameProvider.CLASSIC_MODE)
        {
            newFieldName = "field_" + (++NameProvider.uniqueStart) + "_" + fieldName;
            fd.setOutput();
            return newFieldName;
        }

        if (NameProvider.currentMode == NameProvider.DEOBFUSCATION_MODE)
        {
            if (NameProvider.fieldsObf2Deobf.containsKey(fullFieldName))
            {
                newFieldName = NameProvider.fieldsObf2Deobf.get(fullFieldName).deobfName;
                newFieldName = NameProvider.getShortName(newFieldName);
            }
            else
            {
                if (!NameProvider.isInProtectedPackage(fullFieldName))
                {
                    if (NameProvider.uniqueStart > 0)
                    {
                        newFieldName = "field_" + (NameProvider.uniqueStart++) + "_" + fieldName;
                    }
                }
            }
        }
        else if (NameProvider.currentMode == NameProvider.REOBFUSCATION_MODE)
        {
            if (NameProvider.fieldsDeobf2Obf.containsKey(fullFieldName))
            {
                newFieldName = NameProvider.fieldsDeobf2Obf.get(fullFieldName).obfName;
                newFieldName = NameProvider.getShortName(newFieldName);
            }
        }

        if (!NameProvider.isInProtectedPackage(fullFieldName))
        {
            fd.setOutput();
        }

        return newFieldName;
    }

    private static boolean isInProtectedPackage(String fullInName)
    {
        for (String pkg : NameProvider.protectedPackages)
        {
            if (fullInName.startsWith(pkg))
            {
                return true;
            }
        }
        return false;
    }

    private static String getShortName(String name)
    {
        if ((name != null) && name.contains(ClassFile.SEP_REGULAR))
        {
            name = name.substring(name.lastIndexOf(ClassFile.SEP_REGULAR) + 1);
        }

        return name;
    }

    public static void outputPackage(Pk pk)
    {
        NameProvider.log("PK: " + pk.getFullInName(true) + " " + pk.getFullOutName(true));
    }

    public static void outputClass(Cl cl)
    {
        NameProvider.log("CL: " + cl.getFullInName(true) + " " + cl.getFullOutName(true));
    }

    public static void outputMethod(Md md)
    {
        NameProvider.log("MD: " + md.getFullInName(true) + " " + md.getDescriptor()
            + " " + md.getFullOutName(true) + " " + md.getOutDescriptor());
    }

    public static void outputField(Fd fd)
    {
        NameProvider.log("FD: " + fd.getFullInName(true) + " " + fd.getFullOutName(true));
    }
}

class PackageEntry
{
    public String obfName;
    public String deobfName;
}

class ClassEntry
{
    public String obfName;
    public String deobfName;
}

class MethodEntry
{
    public String obfName;
    public String obfDesc;
    public String deobfName;
    public String deobfDesc;
}

class FieldEntry
{
    public String obfName;
    public String deobfName;
}
