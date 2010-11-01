/* ===========================================================================
 * $RCSfile: RgsEnum.java,v $
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
import java.util.*;
import COM.rl.util.*;
import COM.rl.obf.classfile.*;

/**
 * Parser for RGS script files which provides an enumeration of option entries.
 *
 * @author      Mark Welsh
 */
public class RgsEnum
{
    // Constants -------------------------------------------------------------
    public static final String DIRECTIVE_OPTION        = ".option";
    public static final String DIRECTIVE_ATTR          = ".attribute";
    public static final String DIRECTIVE_CLASS         = ".class";
    public static final String DIRECTIVE_NOTRIM_CLASS  = "^class";
    public static final String DIRECTIVE_NOT_CLASS     = "!class";
    public static final String DIRECTIVE_FIELD         = ".field";
    public static final String DIRECTIVE_NOTRIM_FIELD  = "^field";
    public static final String DIRECTIVE_NOT_FIELD     = "!field";
    public static final String DIRECTIVE_METHOD        = ".method";
    public static final String DIRECTIVE_NOTRIM_METHOD = "^method";
    public static final String DIRECTIVE_NOT_METHOD    = "!method";
    public static final String DIRECTIVE_PACKAGE_MAP   = ".package_map";
    public static final String DIRECTIVE_REPACKAGE_MAP = ".repackage_map";
    public static final String DIRECTIVE_CLASS_MAP     = ".class_map";
    public static final String DIRECTIVE_FIELD_MAP     = ".field_map";
    public static final String DIRECTIVE_METHOD_MAP    = ".method_map";
    public static final String DIRECTIVE_NOWARN        = ".nowarn";

    public static final String OPTION_PUBLIC        = "public";
    public static final String OPTION_PROTECTED     = "protected";
    public static final String OPTION_PUB_PROT_ONLY = "pub_prot_only";
    public static final String OPTION_METHOD        = "method";
    public static final String OPTION_FIELD         = "field";
    public static final String OPTION_AND_CLASS     = "and_class";
    public static final String OPTION_EXTENDS       = "extends";

    public static final String ACCESS_PUBLIC       = "public";
    public static final String ACCESS_PRIVATE      = "private";
    public static final String ACCESS_PROTECTED    = "protected";
    public static final String ACCESS_STATIC       = "static";
    public static final String ACCESS_FINAL        = "final";
    public static final String ACCESS_SYNCHRONIZED = "synchronized";
    public static final String ACCESS_BRIDGE       = "bridge";
    public static final String ACCESS_VOLATILE     = "volatile";
    public static final String ACCESS_VARARGS      = "varargs";
    public static final String ACCESS_TRANSIENT    = "transient";
    public static final String ACCESS_NATIVE       = "native";
    public static final String ACCESS_INTERFACE    = "interface";
    public static final String ACCESS_ABSTRACT     = "abstract";
    public static final String ACCESS_STRICT       = "strict";
    public static final String ACCESS_SYNTHETIC    = "synthetic";
    public static final String ACCESS_ANNOTATION   = "annotation";
    public static final String ACCESS_ENUM         = "enum";

    public static final String[] CLASS_ACCESS = {ACCESS_PUBLIC,
                                                 ACCESS_FINAL,
                                                 ACCESS_INTERFACE,
                                                 ACCESS_ABSTRACT,
                                                 ACCESS_ANNOTATION,
                                                 ACCESS_ENUM};

    public static final String[] METHOD_ACCESS = {ACCESS_PUBLIC,
                                                  ACCESS_PRIVATE,
                                                  ACCESS_PROTECTED,
                                                  ACCESS_STATIC,
                                                  ACCESS_FINAL,
                                                  ACCESS_SYNCHRONIZED,
                                                  ACCESS_BRIDGE,
                                                  ACCESS_VARARGS,
                                                  ACCESS_NATIVE,
                                                  ACCESS_ABSTRACT,
                                                  ACCESS_STRICT};

    public static final String[] FIELD_ACCESS = {ACCESS_PUBLIC,
                                                 ACCESS_PRIVATE,
                                                 ACCESS_PROTECTED,
                                                 ACCESS_STATIC,
                                                 ACCESS_FINAL,
                                                 ACCESS_VOLATILE,
                                                 ACCESS_TRANSIENT,
                                                 ACCESS_ENUM};

    private static final String DEFAULT_RGS = 
    ".option Applet\n" +
    ".option Application\n" +
    ".option Serializable\n" +
    ".option RMI\n" +
    ".option RuntimeAnnotations\n" +
    ".option MapClassString\n" +
    ".option Trim\n" +
    ".option Repackage\n" +
    ".option Generic\n";


    // Fields ----------------------------------------------------------------
    private StreamTokenizer tk;
    private RgsEntry next;
    private Exception nextException;


    // Class Methods ---------------------------------------------------------
    /** Return the internal default script file. */
    public static String getDefaultRgs() { return DEFAULT_RGS; }

    /** Translate a string access modifier from the script to bit flag */
    private static int toAccessFlag(String accessString) throws Exception
    {
        if      (ACCESS_PUBLIC.equals(accessString)) 
            return ClassConstants.ACC_PUBLIC;
        else if (ACCESS_PRIVATE.equals(accessString)) 
            return ClassConstants.ACC_PRIVATE;
        else if (ACCESS_PROTECTED.equals(accessString)) 
            return ClassConstants.ACC_PROTECTED;
        else if (ACCESS_STATIC.equals(accessString)) 
            return ClassConstants.ACC_STATIC;
        else if (ACCESS_FINAL.equals(accessString)) 
            return ClassConstants.ACC_FINAL;
        else if (ACCESS_SYNCHRONIZED.equals(accessString)) 
            return ClassConstants.ACC_SYNCHRONIZED;
        else if (ACCESS_BRIDGE.equals(accessString)) 
            return ClassConstants.ACC_BRIDGE;
        else if (ACCESS_VOLATILE.equals(accessString)) 
            return ClassConstants.ACC_VOLATILE;
        else if (ACCESS_VARARGS.equals(accessString)) 
            return ClassConstants.ACC_VARARGS;
        else if (ACCESS_TRANSIENT.equals(accessString)) 
            return ClassConstants.ACC_TRANSIENT;
        else if (ACCESS_NATIVE.equals(accessString)) 
            return ClassConstants.ACC_NATIVE;
        else if (ACCESS_INTERFACE.equals(accessString)) 
            return ClassConstants.ACC_INTERFACE;
        else if (ACCESS_ABSTRACT.equals(accessString)) 
            return ClassConstants.ACC_ABSTRACT;
        else if (ACCESS_STRICT.equals(accessString)) 
            return ClassConstants.ACC_STRICT;
        else if (ACCESS_SYNTHETIC.equals(accessString)) 
            return ClassConstants.ACC_SYNTHETIC;
        else if (ACCESS_ANNOTATION.equals(accessString)) 
            return ClassConstants.ACC_ANNOTATION;
        else if (ACCESS_ENUM.equals(accessString)) 
            return ClassConstants.ACC_ENUM;
        else throw new Exception();
    }

    /** Decode a list of access flags into a bit mask for class, method, or
        field access flag u2's. */
    private static int decodeAccessFlags(int entryType, String accessString) throws Exception
    {
        int accessMask = 0;
        int accessSetting = 0;
        while (accessString != null && accessString.length() >= 5) // ';enum'
        {
            boolean invert = false;
            if (accessString.charAt(0) != ';') throw new Exception();
            int startIndex = 1;
            if (accessString.charAt(1) == '!') 
            {
                invert = true;
                startIndex = 2;
            }
            int endIndex = accessString.indexOf(';', startIndex);
            String flagString = (endIndex == -1 ? 
                                 accessString.substring(startIndex) :
                                 accessString.substring(startIndex, endIndex));
            if (endIndex == -1)
            {
                accessString = null;
            } 
            else 
            {
                accessString = accessString.substring(endIndex);
            }
            if (((entryType == RgsEntry.TYPE_CLASS ||
                  entryType == RgsEntry.TYPE_NOT_CLASS) &&
                 !Tools.isInArray(flagString, CLASS_ACCESS)) ||
                ((entryType == RgsEntry.TYPE_METHOD ||
                  entryType == RgsEntry.TYPE_NOT_METHOD) &&
                 !Tools.isInArray(flagString, METHOD_ACCESS)) ||
                ((entryType == RgsEntry.TYPE_FIELD ||
                  entryType == RgsEntry.TYPE_NOT_FIELD) &&
                 !Tools.isInArray(flagString, FIELD_ACCESS))) 
            {
                throw new Exception();
            }
            int flag = toAccessFlag(flagString);
            accessMask |= flag;
            if (!invert) accessSetting |= flag;
        }
        return (accessSetting << 16) + accessMask;
    }

    /** Does this RGS file have an '.option Trim' line? (expensive) */
    public static boolean hasOptionTrim(File rgsFile) throws Exception
    {
	InputStream rgsInputStream = null;
	try 
	{
	    rgsInputStream = rgsFile.exists() ? 
		new FileInputStream(rgsFile) : null;
	    RgsEnum rgsEnum = new RgsEnum(rgsInputStream);
	    while (rgsEnum.hasMoreEntries())
	    {
		RgsEntry entry = rgsEnum.nextEntry();
		if (entry.type == RgsEntry.TYPE_OPTION &&
		    ClassConstants.OPTION_Trim.equals(entry.name))
		{
		    return true;
		}
	    }
	    return false;
	}
	finally
	{
	    if (rgsInputStream != null)
	    {
		rgsInputStream.close();
	    }
	}
    }


    // Instance Methods ------------------------------------------------------
    /** Ctor. */
    public RgsEnum(InputStream rgs)
    {
        tk = new StreamTokenizer(
            new BufferedReader(
                rgs != null ? 
                (Reader)new InputStreamReader(rgs) : 
                (Reader)new StringReader(DEFAULT_RGS)));
        tk.resetSyntax();
        tk.whitespaceChars(0x00, 0x20);
        tk.wordChars('^', '^');
        tk.wordChars('!', '!');
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
        tk.eolIsSignificant(true);
        readNext();
    }

    /** Are there more script entries? */
    public boolean hasMoreEntries() throws Exception
    {
        if (nextException != null)
        {
            throw nextException;
        }
        return next != null;
    }

    /** Return next script entry. */
    public RgsEntry nextEntry() throws Exception
    {
        RgsEntry thisOne = next;
        Exception thisException = nextException;
        readNext();
        if (thisException != null)
        {
            throw thisException;
        }
        return thisOne;
    }

    // Read the next entry, returning true if one is available.
    private void readNext() 
    {
        // Reset the 'next error' state
        nextException = null;
        RgsEntry entry = null;
        int ttype;
        try 
        {
            int directive = -1;
            int accessMask = 0;
            int accessSetting = 0;
            String name = null;
            String descriptor = null;
            boolean hasExtends = false;
            String extendsName = null;
            while ((ttype = tk.nextToken()) != StreamTokenizer.TT_EOF) 
            {
                if (ttype == StreamTokenizer.TT_WORD) 
                {
                    if (directive == -1) 
                    {
                        if (tk.sval.equals(DIRECTIVE_OPTION)) 
                        {
                            directive = RgsEntry.TYPE_OPTION;
                        } 
                        else if (tk.sval.equals(DIRECTIVE_ATTR)) 
                        {
                            directive = RgsEntry.TYPE_ATTR;
                        } 
                        else if (tk.sval.equals(DIRECTIVE_NOWARN)) 
                        {
                            directive = RgsEntry.TYPE_NOWARN;
                        } 
                        else if (tk.sval.equals(DIRECTIVE_PACKAGE_MAP)) 
                        {
                            directive = RgsEntry.TYPE_PACKAGE_MAP;
                        } 
                        else if (tk.sval.equals(DIRECTIVE_REPACKAGE_MAP)) 
                        {
                            directive = RgsEntry.TYPE_REPACKAGE_MAP;
                        } 
                        else if (tk.sval.equals(DIRECTIVE_CLASS_MAP)) 
                        {
                            directive = RgsEntry.TYPE_CLASS_MAP;
                        } 
                        else if (tk.sval.equals(DIRECTIVE_METHOD_MAP)) 
                        {
                            directive = RgsEntry.TYPE_METHOD_MAP;
                        } 
                        else if (tk.sval.equals(DIRECTIVE_FIELD_MAP)) 
                        {
                            directive = RgsEntry.TYPE_FIELD_MAP;
                        } 
                        else if (tk.sval.startsWith(DIRECTIVE_CLASS)) 
                        {
                            directive = RgsEntry.TYPE_CLASS;
                            accessMask = decodeAccessFlags
                                (directive,
                                 tk.sval.substring(DIRECTIVE_CLASS.length()));
                            accessSetting = accessMask >> 16;
                            accessMask &= 0xffff;
                        } 
                        else if (tk.sval.startsWith(DIRECTIVE_NOTRIM_CLASS)) 
                        {
                            directive = RgsEntry.TYPE_NOTRIM_CLASS;
                            accessMask = decodeAccessFlags
                                (directive,
                                 tk.sval.substring(DIRECTIVE_NOTRIM_CLASS.length()));
                            accessSetting = accessMask >> 16;
                            accessMask &= 0xffff;
                        } 
                        else if (tk.sval.startsWith(DIRECTIVE_NOT_CLASS)) 
                        {
                            directive = RgsEntry.TYPE_NOT_CLASS;
                            accessMask = decodeAccessFlags
                                (directive,
                                 tk.sval.substring(DIRECTIVE_NOT_CLASS.length()));
                            accessSetting = accessMask >> 16;
                            accessMask &= 0xffff;
                        } 
                        else if (tk.sval.startsWith(DIRECTIVE_METHOD)) 
                        {
                            directive = RgsEntry.TYPE_METHOD;
                            accessMask = decodeAccessFlags
                                (directive,
                                 tk.sval.substring(DIRECTIVE_METHOD.length()));
                            accessSetting = accessMask >> 16;
                            accessMask &= 0xffff;
                        } 
                        else if (tk.sval.startsWith(DIRECTIVE_NOTRIM_METHOD)) 
                        {
                            directive = RgsEntry.TYPE_NOTRIM_METHOD;
                            accessMask = decodeAccessFlags
                                (directive,
                                 tk.sval.substring(DIRECTIVE_NOTRIM_METHOD.length()));
                            accessSetting = accessMask >> 16;
                            accessMask &= 0xffff;
                        } 
                        else if (tk.sval.startsWith(DIRECTIVE_NOT_METHOD)) 
                        {
                            directive = RgsEntry.TYPE_NOT_METHOD;
                            accessMask = decodeAccessFlags
                                (directive,
                                 tk.sval.substring(DIRECTIVE_NOT_METHOD.length()));
                            accessSetting = accessMask >> 16;
                            accessMask &= 0xffff;
                        } 
                        else if (tk.sval.startsWith(DIRECTIVE_FIELD)) 
                        {
                            directive = RgsEntry.TYPE_FIELD;
                            accessMask = decodeAccessFlags
                                (directive,
                                 tk.sval.substring(DIRECTIVE_FIELD.length()));
                            accessSetting = accessMask >> 16;
                            accessMask &= 0xffff;
                        } 
                        else if (tk.sval.startsWith(DIRECTIVE_NOTRIM_FIELD)) 
                        {
                            directive = RgsEntry.TYPE_NOTRIM_FIELD;
                            accessMask = decodeAccessFlags
                                (directive,
                                 tk.sval.substring(DIRECTIVE_NOTRIM_FIELD.length()));
                            accessSetting = accessMask >> 16;
                            accessMask &= 0xffff;
                        } 
                        else if (tk.sval.startsWith(DIRECTIVE_NOT_FIELD)) 
                        {
                            directive = RgsEntry.TYPE_NOT_FIELD;
                            accessMask = decodeAccessFlags
                                (directive,
                                 tk.sval.substring(DIRECTIVE_NOT_FIELD.length()));
                            accessSetting = accessMask >> 16;
                            accessMask &= 0xffff;
                        } 
                        else 
                        {
                            throw new Exception();
                        }
                    } 
                    else if (entry == null) 
                    {
                        switch (directive)
                        {
                        case RgsEntry.TYPE_OPTION:
                            if (!Tools.isInArray(tk.sval, ClassConstants.KNOWN_OPTIONS)) 
                            {
                                throw new Exception();
                            }
                            entry = new RgsEntry(directive, tk.sval);
                            break;
                        case RgsEntry.TYPE_ATTR:
                            if (!Tools.isInArray(tk.sval, ClassConstants.KNOWN_ATTRS)) 
                            {
                                throw new Exception();
                            }
                            entry = new RgsEntry(directive, tk.sval);
                            break;
                        case RgsEntry.TYPE_NOWARN:
                            //checkClassSpec(tk.sval);
                            entry = new RgsEntry(directive, tk.sval);
                            break;
                        case RgsEntry.TYPE_CLASS:
                        case RgsEntry.TYPE_NOTRIM_CLASS:
                        case RgsEntry.TYPE_NOT_CLASS:
                            //checkClassWCSpec(tk.sval);
                            entry = new RgsEntry(directive, tk.sval);
                            entry.accessMask = accessMask;
                            entry.accessSetting = accessSetting;
                            break;
                        case RgsEntry.TYPE_METHOD:
                        case RgsEntry.TYPE_NOTRIM_METHOD:
                        case RgsEntry.TYPE_NOT_METHOD:
                            if (name == null) 
                            {
                                name = tk.sval;
                                //checkMethodOrFieldSpec(name);
                            } 
                            else if (descriptor == null)
                            {
                                descriptor = tk.sval;
                                //checkMethodDescriptor(descriptor);
                                entry = new RgsEntry(directive, name, descriptor);
                                entry.accessMask = accessMask;
                                entry.accessSetting = accessSetting;
                            }
                            break;
                        case RgsEntry.TYPE_FIELD:
                        case RgsEntry.TYPE_NOTRIM_FIELD:
                        case RgsEntry.TYPE_NOT_FIELD:
                            if (name == null) 
                            {
                                name = tk.sval;
                                //checkMethodOrFieldSpec(name);
                            } 
                            else 
                            {
                                descriptor = tk.sval;
                                //checkJavaType(descriptor);
                                entry = new RgsEntry(directive, name, descriptor);
                                entry.accessMask = accessMask;
                                entry.accessSetting = accessSetting;
                            }
                            break;
                        case RgsEntry.TYPE_PACKAGE_MAP:
                        case RgsEntry.TYPE_REPACKAGE_MAP:
                            if (name == null) 
                            {
                                name = tk.sval;
                                checkClassSpec(name);
                            } 
                            else 
                            {
                                String obfName = tk.sval;
                                checkJavaIdentifier(obfName);
                                entry = new RgsEntry(directive, name);
                                entry.obfName = obfName;
                            }
                            break;
                        case RgsEntry.TYPE_CLASS_MAP:
                            if (name == null) 
                            {
                                name = tk.sval;
                                checkClassSpec(name);
                            } 
                            else 
                            {
                                String obfName = tk.sval;
                                checkJavaInnerIdentifier(obfName);
                                entry = new RgsEntry(directive, name);
                                entry.obfName = obfName;
                            }
                            break;
                        case RgsEntry.TYPE_METHOD_MAP:
                            if (name == null) 
                            {
                                name = tk.sval;
                                checkMethodOrFieldSpec(name);
                            } 
                            else if (descriptor == null) 
                            {
                                descriptor = tk.sval;
                                checkMethodDescriptor(descriptor);
                            } 
                            else 
                            {
                                String obfName = tk.sval;
                                checkJavaIdentifier(obfName);
                                entry = new RgsEntry(directive, name, descriptor);
                                entry.obfName = obfName;
                            }
                            break;
                        case RgsEntry.TYPE_FIELD_MAP:
                            if (name == null) 
                            {
                                name = tk.sval;
                                checkMethodOrFieldSpec(name);
                            } 
                            else 
                            {
                                String obfName = tk.sval;
                                checkJavaIdentifier(obfName);
                                entry = new RgsEntry(directive, name);
                                entry.obfName = obfName;
                            }
                            break;
                        }
                    } 
                    else if (directive == RgsEntry.TYPE_CLASS ||
                             directive == RgsEntry.TYPE_NOTRIM_CLASS ||
                             directive == RgsEntry.TYPE_NOT_CLASS) 
                    {
                        if (tk.sval.equals(OPTION_PUBLIC)) 
                        {
                            if (entry.retainToPublic || 
                                entry.retainToProtected || 
                                entry.retainPubProtOnly) 
                            {
                                throw new Exception();
                            }
                            entry.retainToPublic = true;
                        } 
                        else if (tk.sval.equals(OPTION_PUB_PROT_ONLY)) 
                        {
                            if (entry.retainToPublic || 
                                entry.retainToProtected || 
                                entry.retainPubProtOnly) 
                            {
                                throw new Exception();
                            }
                            entry.retainPubProtOnly = true;
                        } 
                        else if (tk.sval.equals(OPTION_PROTECTED)) 
                        {
                            if (entry.retainToPublic || 
                                entry.retainToProtected || 
                                entry.retainPubProtOnly) 
                            {
                                throw new Exception();
                            }
                            entry.retainToProtected = true;
                        } 
                        else if (tk.sval.equals(OPTION_FIELD)) 
                        {
                            if ((!entry.retainToPublic && 
                                 !entry.retainPubProtOnly && 
                                 !entry.retainToProtected) || 
                                entry.retainMethodsOnly) 
                            {
                                throw new Exception();
                            }
                            entry.retainFieldsOnly = true;
                        } 
                        else if (tk.sval.equals(OPTION_METHOD)) 
                        {
                            if ((!entry.retainToPublic && 
                                 !entry.retainPubProtOnly && 
                                 !entry.retainToProtected) || 
                                entry.retainFieldsOnly) 
                            {
                                throw new Exception();
                            }
                            entry.retainMethodsOnly = true;
                        } 
                        else if (tk.sval.equals(OPTION_EXTENDS)) 
                        {
                            hasExtends = true;
                        } 
                        else if (hasExtends) 
                        {
                            extendsName = tk.sval;
                            checkClassSpec(extendsName);
                            entry.extendsName = extendsName;
                        } 
                        else 
                        {
                            throw new Exception();
                        }
                    } 
                    else if (directive == RgsEntry.TYPE_METHOD ||
                             directive == RgsEntry.TYPE_NOTRIM_METHOD ||
                             directive == RgsEntry.TYPE_NOT_METHOD ||
                             directive == RgsEntry.TYPE_FIELD ||
                             directive == RgsEntry.TYPE_NOTRIM_FIELD ||
                             directive == RgsEntry.TYPE_NOT_FIELD) 
                    {
                        if (tk.sval.equals(OPTION_AND_CLASS)) 
                        {
                            entry.retainAndClass = true;
                        } 
                        else if (tk.sval.equals(OPTION_EXTENDS)) 
                        {
                            hasExtends = true;
                        } 
                        else if (hasExtends) 
                        {
                            extendsName = tk.sval;
                            checkClassSpec(extendsName);
                            entry.extendsName = extendsName;
                        } 
                        else 
                        {
                            throw new Exception();
                        }
                    }
                    else
                    {
                        throw new Exception();
                    }
                } 
                else if (ttype == StreamTokenizer.TT_EOL) 
                {
                    if (entry != null) 
                    {
                        break;
                    }
                } 
                else 
                {
                    throw new Exception();
                }
            }
            next = entry;
        } 
        catch (Exception e) 
        {
            // Discard to end of erroneous line
            try 
            {
                while ((ttype = tk.nextToken()) != StreamTokenizer.TT_EOF) 
                {
                    if (ttype == StreamTokenizer.TT_EOL) 
                    {
                        break;
                    }
                }
            } 
            catch (IOException ee) 
            {
                // Take no action if StreamTokenizer fails here
            }

            // Save exception for throw from nextEntry()
            nextException = new Exception("Parser error at line " + Integer.toString(tk.lineno()) + " of script file.");
        }
    }

    // Throw if invalid
    private void checkMethodDescriptor(String s) throws Exception
    {
        if (s.length() == 0 || s.charAt(0) != '(')
        {
            throw new Exception();
        }
        s = s.substring(1);

        // Check each type
        while (s.length() > 0 && s.charAt(0) != ')')
        {
            s = checkFirstJavaType(s);
        }
        checkJavaType(s.substring(1));
    }

    // Throw if first type is invalid, else return all but first type in String
    private String checkFirstJavaType(String s) throws Exception
    {
        // Pull off the array specifiers
        while (s.charAt(0) == '[')
        {
            s = s.substring(1);
            if (s.length() == 0)
            {
                throw new Exception();
            }
        }

        // Check a type
        int pos = 0;
        switch (s.charAt(0))
        {
        case 'B':
        case 'C':
        case 'D':
        case 'F':
        case 'I':
        case 'J':
        case 'S':
        case 'V':
        case 'Z':
            break;

        case 'L':
            pos = s.indexOf(';');
            if (pos == -1)
            {
                throw new Exception();
            }
            // Check the class type
            checkClassSpec(s.substring(0, pos));
            break;

        default:
            throw new Exception();
        }
        return s.substring(pos + 1);
    }

    // Throw if type is invalid
    private void checkJavaType(String s) throws Exception
    {
        if (!checkFirstJavaType(s).equals(""))
        {
            throw new Exception();
        }
    }

    // Throw if invalid
    private void checkMethodOrFieldSpec(String s) throws Exception
    {
        if (s.length() == 0)
        {
            throw new Exception();
        }

        // Check the method or field name
        int pos = s.lastIndexOf('/');
        if (pos == -1)
        {
            throw new Exception();
        }
        checkJavaIdentifier(s.substring(pos + 1));
        checkClassSpec(s.substring(0, pos));
    }

    // Throw if invalid
    private void checkClassSpec(String s) throws Exception
    {
        if (s.length() == 0)
        {
            throw new Exception();
        }

        int pos = -1;
        while ((pos = s.lastIndexOf('$')) != -1)
        {
            checkJavaInnerIdentifier(s.substring(pos + 1));
            s = s.substring(0, pos);
        }
        while ((pos = s.lastIndexOf('/')) != -1)
        {
            checkJavaIdentifier(s.substring(pos + 1));
            s = s.substring(0, pos);
        }
        checkJavaIdentifier(s);
    }

    // Throw if invalid
    private void checkClassWCSpec(String s) throws Exception
    {
        // Check for wildcard package spec first
        if (s.length() == 0)
        {
            throw new Exception();
        }
        if (s.charAt(s.length() - 1) == '*')
        {
            if (!s.equals("*"))
            {
                if (s.length() < 3 || s.charAt(s.length() - 2) != '/')
                {
                    throw new Exception();
                }
                else
                {
                    s = s.substring(0, s.length() - 2);
                    int pos = -1;
                    while ((pos = s.lastIndexOf('/')) != -1)
                    {
                        checkJavaIdentifier(s.substring(pos + 1));
                        s = s.substring(0, pos);
                    }
                    checkJavaIdentifier(s);
                }
            }
        }
        else
        {
            // Check for regular class spec
            checkClassSpec(s);
        }
    }

    // Throw if invalid
    private void checkJavaIdentifier(String s) throws Exception
    {
        if (s.length() == 0 || !Character.isJavaIdentifierStart(s.charAt(0)))
        {
            throw new Exception();
        }
        for (int i = 1; i < s.length(); i++)
        {
            if (!Character.isJavaIdentifierPart(s.charAt(i)))
            {
                throw new Exception();
            }
        }
    }

    // Throw if invalid (allows for anon. inner class names like '4')
    private void checkJavaInnerIdentifier(String s) throws Exception
    {
        if (s.length() == 0)
        {
            throw new Exception();
        }
        for (int i = 0; i < s.length(); i++)
        {
            if (!Character.isJavaIdentifierPart(s.charAt(i)))
            {
                throw new Exception();
            }
        }
    }
}
