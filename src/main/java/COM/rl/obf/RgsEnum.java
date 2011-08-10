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
 * Parser for RGS script files which provides an iterator of option entries.
 * 
 * @author Mark Welsh
 */
public class RgsEnum
{
    // Constants -------------------------------------------------------------
    public static final String DIRECTIVE_OPTION = ".option";
    public static final String DIRECTIVE_ATTR = ".attribute";
    public static final String DIRECTIVE_CLASS = ".class";
    public static final String DIRECTIVE_NOT_CLASS = "!class";
    public static final String DIRECTIVE_FIELD = ".field";
    public static final String DIRECTIVE_NOT_FIELD = "!field";
    public static final String DIRECTIVE_METHOD = ".method";
    public static final String DIRECTIVE_NOT_METHOD = "!method";
    public static final String DIRECTIVE_PACKAGE_MAP = ".package_map";
    public static final String DIRECTIVE_REPACKAGE_MAP = ".repackage_map";
    public static final String DIRECTIVE_CLASS_MAP = ".class_map";
    public static final String DIRECTIVE_FIELD_MAP = ".field_map";
    public static final String DIRECTIVE_METHOD_MAP = ".method_map";
    public static final String DIRECTIVE_NOWARN = ".nowarn";

    public static final String OPTION_PUBLIC = "public";
    public static final String OPTION_PROTECTED = "protected";
    public static final String OPTION_PUB_PROT_ONLY = "pub_prot_only";
    public static final String OPTION_METHOD = "method";
    public static final String OPTION_FIELD = "field";
    public static final String OPTION_AND_CLASS = "and_class";
    public static final String OPTION_EXTENDS = "extends";

    public static final String ACCESS_PUBLIC = "public";
    public static final String ACCESS_PRIVATE = "private";
    public static final String ACCESS_PROTECTED = "protected";
    public static final String ACCESS_STATIC = "static";
    public static final String ACCESS_FINAL = "final";
    public static final String ACCESS_SYNCHRONIZED = "synchronized";
    public static final String ACCESS_BRIDGE = "bridge";
    public static final String ACCESS_VOLATILE = "volatile";
    public static final String ACCESS_VARARGS = "varargs";
    public static final String ACCESS_TRANSIENT = "transient";
    public static final String ACCESS_NATIVE = "native";
    public static final String ACCESS_INTERFACE = "interface";
    public static final String ACCESS_ABSTRACT = "abstract";
    public static final String ACCESS_STRICT = "strict";
    public static final String ACCESS_SYNTHETIC = "synthetic";
    public static final String ACCESS_ANNOTATION = "annotation";
    public static final String ACCESS_ENUM = "enum";

    public static final String[] CLASS_ACCESS =
    {
        RgsEnum.ACCESS_PUBLIC,
        RgsEnum.ACCESS_FINAL,
        RgsEnum.ACCESS_INTERFACE,
        RgsEnum.ACCESS_ABSTRACT,
        RgsEnum.ACCESS_ANNOTATION,
        RgsEnum.ACCESS_ENUM
    };

    public static final String[] METHOD_ACCESS =
    {
        RgsEnum.ACCESS_PUBLIC,
        RgsEnum.ACCESS_PRIVATE,
        RgsEnum.ACCESS_PROTECTED,
        RgsEnum.ACCESS_STATIC,
        RgsEnum.ACCESS_FINAL,
        RgsEnum.ACCESS_SYNCHRONIZED,
        RgsEnum.ACCESS_BRIDGE,
        RgsEnum.ACCESS_VARARGS,
        RgsEnum.ACCESS_NATIVE,
        RgsEnum.ACCESS_ABSTRACT,
        RgsEnum.ACCESS_STRICT
    };

    public static final String[] FIELD_ACCESS =
    {
        RgsEnum.ACCESS_PUBLIC,
        RgsEnum.ACCESS_PRIVATE,
        RgsEnum.ACCESS_PROTECTED,
        RgsEnum.ACCESS_STATIC,
        RgsEnum.ACCESS_FINAL,
        RgsEnum.ACCESS_VOLATILE,
        RgsEnum.ACCESS_TRANSIENT,
        RgsEnum.ACCESS_ENUM
    };

    private static final String DEFAULT_RGS = ".option Applet\n"
        + ".option Application\n"
        + ".option Serializable\n"
        + ".option RMI\n"
        + ".option RuntimeAnnotations\n"
        + ".option MapClassString\n"
        + ".option Repackage\n"
        + ".option Generic\n";


    // Fields ----------------------------------------------------------------
    private StreamTokenizer tk;
    private RgsEntry next;
    private RGSException nextException;


    // Class Methods ---------------------------------------------------------
    /** Return the internal default script file. */
    public static String getDefaultRgs()
    {
        return RgsEnum.DEFAULT_RGS;
    }

    /**
     * Translate a string access modifier from the script to bit flag
     * 
     * @throws RGSException
     */
    private static int toAccessFlag(String accessString) throws RGSException
    {
        if (RgsEnum.ACCESS_PUBLIC.equals(accessString))
        {
            return ClassConstants.ACC_PUBLIC;
        }
        else if (RgsEnum.ACCESS_PRIVATE.equals(accessString))
        {
            return ClassConstants.ACC_PRIVATE;
        }
        else if (RgsEnum.ACCESS_PROTECTED.equals(accessString))
        {
            return ClassConstants.ACC_PROTECTED;
        }
        else if (RgsEnum.ACCESS_STATIC.equals(accessString))
        {
            return ClassConstants.ACC_STATIC;
        }
        else if (RgsEnum.ACCESS_FINAL.equals(accessString))
        {
            return ClassConstants.ACC_FINAL;
        }
        else if (RgsEnum.ACCESS_SYNCHRONIZED.equals(accessString))
        {
            return ClassConstants.ACC_SYNCHRONIZED;
        }
        else if (RgsEnum.ACCESS_BRIDGE.equals(accessString))
        {
            return ClassConstants.ACC_BRIDGE;
        }
        else if (RgsEnum.ACCESS_VOLATILE.equals(accessString))
        {
            return ClassConstants.ACC_VOLATILE;
        }
        else if (RgsEnum.ACCESS_VARARGS.equals(accessString))
        {
            return ClassConstants.ACC_VARARGS;
        }
        else if (RgsEnum.ACCESS_TRANSIENT.equals(accessString))
        {
            return ClassConstants.ACC_TRANSIENT;
        }
        else if (RgsEnum.ACCESS_NATIVE.equals(accessString))
        {
            return ClassConstants.ACC_NATIVE;
        }
        else if (RgsEnum.ACCESS_INTERFACE.equals(accessString))
        {
            return ClassConstants.ACC_INTERFACE;
        }
        else if (RgsEnum.ACCESS_ABSTRACT.equals(accessString))
        {
            return ClassConstants.ACC_ABSTRACT;
        }
        else if (RgsEnum.ACCESS_STRICT.equals(accessString))
        {
            return ClassConstants.ACC_STRICT;
        }
        else if (RgsEnum.ACCESS_SYNTHETIC.equals(accessString))
        {
            return ClassConstants.ACC_SYNTHETIC;
        }
        else if (RgsEnum.ACCESS_ANNOTATION.equals(accessString))
        {
            return ClassConstants.ACC_ANNOTATION;
        }
        else if (RgsEnum.ACCESS_ENUM.equals(accessString))
        {
            return ClassConstants.ACC_ENUM;
        }
        else
        {
            throw new RGSException("Invalid access string " + accessString);
        }
    }

    /**
     * Decode a list of access flags into a bit mask for class, method, or field access flag u2's.
     * 
     * @throws RGSException
     */
    private static int decodeAccessFlags(int entryType, String accessString) throws RGSException
    {
        int accessMask = 0;
        int accessSetting = 0;
        while ((accessString != null) && (accessString.length() >= 5)) // ';enum'
        {
            boolean invert = false;
            if (accessString.charAt(0) != ';')
            {
                throw new RGSException("Invalid access flags '" + accessString + "'");
            }
            int startIndex = 1;
            if (accessString.charAt(1) == '!')
            {
                invert = true;
                startIndex = 2;
            }
            int endIndex = accessString.indexOf(';', startIndex);
            String flagString = (endIndex == -1 ? accessString.substring(startIndex) : accessString.substring(startIndex, endIndex));
            if (endIndex == -1)
            {
                accessString = null;
            }
            else
            {
                accessString = accessString.substring(endIndex);
            }
            if ((((entryType == RgsEntry.TYPE_CLASS) || (entryType == RgsEntry.TYPE_NOT_CLASS))
                && !Arrays.asList(RgsEnum.CLASS_ACCESS).contains(flagString))
                || (((entryType == RgsEntry.TYPE_METHOD) || (entryType == RgsEntry.TYPE_NOT_METHOD))
                    && !Arrays.asList(RgsEnum.METHOD_ACCESS).contains(flagString))
                || (((entryType == RgsEntry.TYPE_FIELD) || (entryType == RgsEntry.TYPE_NOT_FIELD))
                    && !Arrays.asList(RgsEnum.FIELD_ACCESS).contains(flagString)))
            {
                throw new RGSException("Invalid access flag '" + flagString + "'");
            }
            int flag = RgsEnum.toAccessFlag(flagString);
            accessMask |= flag;
            if (!invert)
            {
                accessSetting |= flag;
            }
        }
        return (accessSetting << 16) + accessMask;
    }


    // Instance Methods ------------------------------------------------------
    /** Ctor. */
    public RgsEnum(InputStream rgs)
    {
        this.tk = new StreamTokenizer(new BufferedReader(rgs != null ? (Reader)new InputStreamReader(rgs)
            : (Reader)new StringReader(RgsEnum.DEFAULT_RGS)));
        this.tk.resetSyntax();
        this.tk.whitespaceChars(0x00, 0x20);
        this.tk.wordChars('^', '^');
        this.tk.wordChars('!', '!');
        this.tk.wordChars('*', '*');
        this.tk.wordChars('.', '.');
        this.tk.wordChars(';', ';');
        this.tk.wordChars('_', '_');
        this.tk.wordChars('[', '[');
        this.tk.wordChars('(', ')');
        this.tk.wordChars('$', '$');
        this.tk.wordChars('/', '9');
        this.tk.wordChars('A', 'Z');
        this.tk.wordChars('a', 'z');
        this.tk.commentChar('#');
        this.tk.eolIsSignificant(true);
        this.readNext();
    }

    /**
     * Are there more script entries?
     * 
     * @throws RGSException
     */
    public boolean hasNext() throws RGSException
    {
        if (this.nextException != null)
        {
            throw this.nextException;
        }
        return this.next != null;
    }

    /**
     * Return next script entry.
     * 
     * @throws RGSException
     */
    public RgsEntry next() throws RGSException
    {
        RgsEntry thisOne = this.next;
        RGSException thisException = this.nextException;
        this.readNext();
        if (thisException != null)
        {
            throw thisException;
        }
        return thisOne;
    }

    private void readNext()
    {
        // Reset the 'next error' state
        this.nextException = null;
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
            while ((ttype = this.tk.nextToken()) != StreamTokenizer.TT_EOF)
            {
                if (ttype == StreamTokenizer.TT_WORD)
                {
                    if (directive == -1)
                    {
                        if (this.tk.sval.equals(RgsEnum.DIRECTIVE_OPTION))
                        {
                            directive = RgsEntry.TYPE_OPTION;
                        }
                        else if (this.tk.sval.equals(RgsEnum.DIRECTIVE_ATTR))
                        {
                            directive = RgsEntry.TYPE_ATTR;
                        }
                        else if (this.tk.sval.equals(RgsEnum.DIRECTIVE_NOWARN))
                        {
                            directive = RgsEntry.TYPE_NOWARN;
                        }
                        else if (this.tk.sval.equals(RgsEnum.DIRECTIVE_PACKAGE_MAP))
                        {
                            directive = RgsEntry.TYPE_PACKAGE_MAP;
                        }
                        else if (this.tk.sval.equals(RgsEnum.DIRECTIVE_REPACKAGE_MAP))
                        {
                            directive = RgsEntry.TYPE_REPACKAGE_MAP;
                        }
                        else if (this.tk.sval.equals(RgsEnum.DIRECTIVE_CLASS_MAP))
                        {
                            directive = RgsEntry.TYPE_CLASS_MAP;
                        }
                        else if (this.tk.sval.equals(RgsEnum.DIRECTIVE_METHOD_MAP))
                        {
                            directive = RgsEntry.TYPE_METHOD_MAP;
                        }
                        else if (this.tk.sval.equals(RgsEnum.DIRECTIVE_FIELD_MAP))
                        {
                            directive = RgsEntry.TYPE_FIELD_MAP;
                        }
                        else if (this.tk.sval.startsWith(RgsEnum.DIRECTIVE_CLASS))
                        {
                            directive = RgsEntry.TYPE_CLASS;
                            accessMask = RgsEnum.decodeAccessFlags(directive,
                                this.tk.sval.substring(RgsEnum.DIRECTIVE_CLASS.length()));
                            accessSetting = accessMask >> 16;
                            accessMask &= 0xffff;
                        }
                        else if (this.tk.sval.startsWith(RgsEnum.DIRECTIVE_NOT_CLASS))
                        {
                            directive = RgsEntry.TYPE_NOT_CLASS;
                            accessMask = RgsEnum.decodeAccessFlags(directive,
                                this.tk.sval.substring(RgsEnum.DIRECTIVE_NOT_CLASS.length()));
                            accessSetting = accessMask >> 16;
                            accessMask &= 0xffff;
                        }
                        else if (this.tk.sval.startsWith(RgsEnum.DIRECTIVE_METHOD))
                        {
                            directive = RgsEntry.TYPE_METHOD;
                            accessMask = RgsEnum.decodeAccessFlags(directive,
                                this.tk.sval.substring(RgsEnum.DIRECTIVE_METHOD.length()));
                            accessSetting = accessMask >> 16;
                            accessMask &= 0xffff;
                        }
                        else if (this.tk.sval.startsWith(RgsEnum.DIRECTIVE_NOT_METHOD))
                        {
                            directive = RgsEntry.TYPE_NOT_METHOD;
                            accessMask = RgsEnum.decodeAccessFlags(directive,
                                this.tk.sval.substring(RgsEnum.DIRECTIVE_NOT_METHOD.length()));
                            accessSetting = accessMask >> 16;
                            accessMask &= 0xffff;
                        }
                        else if (this.tk.sval.startsWith(RgsEnum.DIRECTIVE_FIELD))
                        {
                            directive = RgsEntry.TYPE_FIELD;
                            accessMask = RgsEnum.decodeAccessFlags(directive,
                                this.tk.sval.substring(RgsEnum.DIRECTIVE_FIELD.length()));
                            accessSetting = accessMask >> 16;
                            accessMask &= 0xffff;
                        }
                        else if (this.tk.sval.startsWith(RgsEnum.DIRECTIVE_NOT_FIELD))
                        {
                            directive = RgsEntry.TYPE_NOT_FIELD;
                            accessMask = RgsEnum.decodeAccessFlags(directive,
                                this.tk.sval.substring(RgsEnum.DIRECTIVE_NOT_FIELD.length()));
                            accessSetting = accessMask >> 16;
                            accessMask &= 0xffff;
                        }
                        else
                        {
                            throw new RGSException("Unknown keyword '" + this.tk.sval + "'");
                        }
                    }
                    else if (entry == null)
                    {
                        switch (directive)
                        {
                            case RgsEntry.TYPE_OPTION:
                                if (!Arrays.asList(ClassConstants.KNOWN_OPTIONS).contains(this.tk.sval))
                                {
                                    throw new RGSException("Unknown .option '" + this.tk.sval + "'");
                                }
                                entry = new RgsEntry(directive, this.tk.sval);
                                break;
                            case RgsEntry.TYPE_ATTR:
                                if (!Arrays.asList(ClassConstants.KNOWN_ATTRS).contains(this.tk.sval))
                                {
                                    throw new RGSException("Unknown .attribute '" + this.tk.sval + "'");
                                }
                                entry = new RgsEntry(directive, this.tk.sval);
                                break;
                            case RgsEntry.TYPE_NOWARN:
//                                this.checkClassWCSpec(this.tk.sval); // TODO this check doesn't handle wildcards properly
                                entry = new RgsEntry(directive, this.tk.sval);
                                break;
                            case RgsEntry.TYPE_CLASS:
                            case RgsEntry.TYPE_NOT_CLASS:
//                                this.checkClassWCSpec(this.tk.sval); // TODO this check doesn't handle wildcards properly
                                entry = new RgsEntry(directive, this.tk.sval);
                                entry.accessMask = accessMask;
                                entry.accessSetting = accessSetting;
                                break;
                            case RgsEntry.TYPE_METHOD:
                            case RgsEntry.TYPE_NOT_METHOD:
                                if (name == null)
                                {
                                    name = this.tk.sval;
//                                    this.checkMethodOrFieldSpec(name); // TODO this check doesn't handle wildcards properly
                                }
                                else if (descriptor == null)
                                {
                                    descriptor = this.tk.sval;
//                                    this.checkMethodDescriptor(descriptor); // TODO this check doesn't handle wildcards properly
                                    entry = new RgsEntry(directive, name, descriptor);
                                    entry.accessMask = accessMask;
                                    entry.accessSetting = accessSetting;
                                }
                                break;
                            case RgsEntry.TYPE_FIELD:
                            case RgsEntry.TYPE_NOT_FIELD:
                                if (name == null)
                                {
                                    name = this.tk.sval;
//                                    this.checkMethodOrFieldSpec(name); // TODO this check doesn't handle wildcards properly
                                }
                                else
                                {
                                    descriptor = this.tk.sval;
//                                    this.checkJavaType(descriptor); // TODO this doesn't check handle wildcards properly
                                    entry = new RgsEntry(directive, name, descriptor);
                                    entry.accessMask = accessMask;
                                    entry.accessSetting = accessSetting;
                                }
                                break;
                            case RgsEntry.TYPE_PACKAGE_MAP:
                            case RgsEntry.TYPE_REPACKAGE_MAP:
                                if (name == null)
                                {
                                    name = this.tk.sval;
                                    this.checkClassSpec(name);
                                }
                                else
                                {
                                    String obfName = this.tk.sval;
                                    this.checkJavaIdentifier(obfName);
                                    entry = new RgsEntry(directive, name);
                                    entry.obfName = obfName;
                                }
                                break;
                            case RgsEntry.TYPE_CLASS_MAP:
                                if (name == null)
                                {
                                    name = this.tk.sval;
                                    this.checkClassSpec(name);
                                }
                                else
                                {
                                    String obfName = this.tk.sval;
                                    this.checkJavaInnerIdentifier(obfName);
                                    entry = new RgsEntry(directive, name);
                                    entry.obfName = obfName;
                                }
                                break;
                            case RgsEntry.TYPE_METHOD_MAP:
                                if (name == null)
                                {
                                    name = this.tk.sval;
                                    this.checkMethodOrFieldSpec(name);
                                }
                                else if (descriptor == null)
                                {
                                    descriptor = this.tk.sval;
                                    this.checkMethodDescriptor(descriptor);
                                }
                                else
                                {
                                    String obfName = this.tk.sval;
                                    this.checkJavaIdentifier(obfName);
                                    entry = new RgsEntry(directive, name, descriptor);
                                    entry.obfName = obfName;
                                }
                                break;
                            case RgsEntry.TYPE_FIELD_MAP:
                                if (name == null)
                                {
                                    name = this.tk.sval;
                                    this.checkMethodOrFieldSpec(name);
                                }
                                else
                                {
                                    String obfName = this.tk.sval;
                                    this.checkJavaIdentifier(obfName);
                                    entry = new RgsEntry(directive, name);
                                    entry.obfName = obfName;
                                }
                                break;
                        }
                    }
                    else if ((directive == RgsEntry.TYPE_CLASS) || (directive == RgsEntry.TYPE_NOT_CLASS))
                    {
                        if (this.tk.sval.equals(RgsEnum.OPTION_PUBLIC))
                        {
                            if (entry.retainToPublic || entry.retainToProtected || entry.retainPubProtOnly)
                            {
                                throw new RGSException("Multiple access levels");
                            }
                            entry.retainToPublic = true;
                        }
                        else if (this.tk.sval.equals(RgsEnum.OPTION_PUB_PROT_ONLY))
                        {
                            if (entry.retainToPublic || entry.retainToProtected || entry.retainPubProtOnly)
                            {
                                throw new RGSException("Multiple access levels");
                            }
                            entry.retainPubProtOnly = true;
                        }
                        else if (this.tk.sval.equals(RgsEnum.OPTION_PROTECTED))
                        {
                            if (entry.retainToPublic || entry.retainToProtected || entry.retainPubProtOnly)
                            {
                                throw new RGSException("Multiple access levels");
                            }
                            entry.retainToProtected = true;
                        }
                        else if (this.tk.sval.equals(RgsEnum.OPTION_FIELD))
                        {
                            if (!entry.retainToPublic && !entry.retainPubProtOnly && !entry.retainToProtected)
                            {
                                throw new RGSException("No access level");
                            }
                            if (entry.retainMethodsOnly || entry.retainFieldsOnly)
                            {
                                throw new RGSException("Multiple field or method");
                            }
                            entry.retainFieldsOnly = true;
                        }
                        else if (this.tk.sval.equals(RgsEnum.OPTION_METHOD))
                        {
                            if (!entry.retainToPublic && !entry.retainPubProtOnly && !entry.retainToProtected)
                            {
                                throw new RGSException("No access level");
                            }
                            if (entry.retainMethodsOnly || entry.retainFieldsOnly)
                            {
                                throw new RGSException("Multiple field or method");
                            }
                            entry.retainMethodsOnly = true;
                        }
                        else if (this.tk.sval.equals(RgsEnum.OPTION_EXTENDS))
                        {
                            hasExtends = true;
                        }
                        else if (hasExtends)
                        {
                            extendsName = this.tk.sval;
                            this.checkClassSpec(extendsName);
                            entry.extendsName = extendsName;
                        }
                        else
                        {
                            throw new RGSException("Unknown keyword '" + this.tk.sval + "'");
                        }
                    }
                    else if ((directive == RgsEntry.TYPE_METHOD) || (directive == RgsEntry.TYPE_NOT_METHOD)
                        || (directive == RgsEntry.TYPE_FIELD) || (directive == RgsEntry.TYPE_NOT_FIELD))
                    {
                        if (this.tk.sval.equals(RgsEnum.OPTION_AND_CLASS))
                        {
                            entry.retainAndClass = true;
                        }
                        else if (this.tk.sval.equals(RgsEnum.OPTION_EXTENDS))
                        {
                            hasExtends = true;
                        }
                        else if (hasExtends)
                        {
                            extendsName = this.tk.sval;
                            this.checkClassSpec(extendsName);
                            entry.extendsName = extendsName;
                        }
                        else
                        {
                            throw new RGSException("Unknown keyword '" + this.tk.sval + "'");
                        }
                    }
                    else
                    {
                        throw new RGSException("Unknown keyword '" + this.tk.sval + "'");
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
                    throw new RGSException("Invalid character '" + (char)ttype + "'");
                }
            }
            this.next = entry;
        }
        catch (RGSException e)
        {
            // Discard to end of erroneous line
            try
            {
                while ((ttype = this.tk.nextToken()) != StreamTokenizer.TT_EOF)
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
            String excMsg = e.getMessage();
            if (excMsg != null)
            {
                this.nextException = new RGSException("Parser error at line " + Integer.toString(this.tk.lineno())
                    + " of script file: " + excMsg);
            }
            else
            {
                this.nextException = new RGSException("Parser error at line " + Integer.toString(this.tk.lineno())
                    + " of script file");
            }
        }
        catch (IOException e)
        {
            // Discard to end of erroneous line
            try
            {
                while ((ttype = this.tk.nextToken()) != StreamTokenizer.TT_EOF)
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
            String excMsg = e.getMessage();
            if (excMsg != null)
            {
                this.nextException = new RGSException("Parser error at line " + Integer.toString(this.tk.lineno())
                    + " of script file: " + excMsg);
            }
            else
            {
                this.nextException = new RGSException("Parser error at line " + Integer.toString(this.tk.lineno())
                    + " of script file");
            }
        }
    }

    /**
     * Throw if invalid
     * 
     * @throws RGSException
     */
    private void checkMethodDescriptor(String s) throws RGSException
    {
        if ((s.length() == 0) || (s.charAt(0) != '('))
        {
            throw new RGSException("Invalid method descriptor '" + s + "'");
        }
        s = s.substring(1);

        // Check each type
        while ((s.length() > 0) && (s.charAt(0) != ')'))
        {
            s = this.checkFirstJavaType(s);
        }
        this.checkJavaType(s.substring(1));
    }

    /**
     * Throw if first type is invalid, else return all but first type in String
     * 
     * @throws RGSException
     */
    private String checkFirstJavaType(String s) throws RGSException
    {
        // Pull off the array specifiers
        while (s.charAt(0) == '[')
        {
            s = s.substring(1);
            if (s.length() == 0)
            {
                throw new RGSException("Invalid type '" + s + "'");
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
                    throw new RGSException("Invalid type '" + s + "'");
                }
                // Check the class type
                this.checkClassSpec(s.substring(0, pos));
                break;

            default:
                throw new RGSException("Invalid type '" + s + "'");
        }
        return s.substring(pos + 1);
    }

    /**
     * Throw if type is invalid
     * 
     * @throws RGSException
     */
    private void checkJavaType(String s) throws RGSException
    {
        if (!this.checkFirstJavaType(s).equals(""))
        {
            throw new RGSException("Invalid type '" + s + "'");
        }
    }

    /**
     * Throw if invalid
     * 
     * @throws RGSException
     */
    private void checkMethodOrFieldSpec(String s) throws RGSException
    {
        if (s.length() == 0)
        {
            throw new RGSException("Invalid method/field specifier '" + s + "'");
        }

        // Check the method or field name
        int pos = s.lastIndexOf('/');
        if (pos == -1)
        {
            throw new RGSException("Invalid method/field specifier '" + s + "'");
        }
        this.checkJavaIdentifier(s.substring(pos + 1));
        this.checkClassSpec(s.substring(0, pos));
    }

    /**
     * Throw if invalid
     * 
     * @throws RGSException
     */
    private void checkClassSpec(String s) throws RGSException
    {
        if (s.length() == 0)
        {
            throw new RGSException("Invalid class specifier '" + s + "'");
        }

        int pos = -1;
        while ((pos = s.lastIndexOf('$')) != -1)
        {
            this.checkJavaInnerIdentifier(s.substring(pos + 1));
            s = s.substring(0, pos);
        }
        while ((pos = s.lastIndexOf('/')) != -1)
        {
            this.checkJavaIdentifier(s.substring(pos + 1));
            s = s.substring(0, pos);
        }
        this.checkJavaIdentifier(s);
    }

    /**
     * Throw if invalid
     * 
     * @throws RGSException
     */
    private void checkClassWCSpec(String s) throws RGSException
    {
        // Check for wildcard package spec first
        if (s.length() == 0)
        {
            throw new RGSException("Invalid class specifier '" + s + "'");
        }
        if (s.charAt(s.length() - 1) == '*')
        {
            // this is all wrong and doesn't handle *_mod etc
            if ((s.length() > 1) && (s.charAt(s.length() - 2) == '*'))
            {
                if (!s.equals("**"))
                {
                    if ((s.length() < 4) || (s.charAt(s.length() - 3) != '/'))
                    {
                        throw new RGSException("Invalid class specifier '" + s + "'");
                    }
    
                    s = s.substring(0, s.length() - 3);
                }
            }
            else
            {
                if (!s.equals("*"))
                {
                    if ((s.length() < 3) || (s.charAt(s.length() - 2) != '/'))
                    {
                        throw new RGSException("Invalid class specifier '" + s + "'");
                    }
    
                    s = s.substring(0, s.length() - 2);
                }
            }
            int pos = -1;
            while ((pos = s.lastIndexOf('/')) != -1)
            {
                this.checkJavaIdentifier(s.substring(pos + 1));
                s = s.substring(0, pos);
            }
            this.checkJavaIdentifier(s);
        }
        else
        {
            // Check for regular class spec
            this.checkClassSpec(s);
        }
    }

    /**
     * Throw if invalid
     * 
     * @throws RGSException
     */
    private void checkJavaIdentifier(String s) throws RGSException
    {
        if ((s.length() == 0) || !Character.isJavaIdentifierStart(s.charAt(0)))
        {
            throw new RGSException("Invalid identifier '" + s + "'");
        }
        for (int i = 1; i < s.length(); i++)
        {
            if (!Character.isJavaIdentifierPart(s.charAt(i)))
            {
                throw new RGSException("Invalid identifier '" + s + "'");
            }
        }
    }

    /**
     * Throw if invalid (allows for anon. inner class names like '4')
     * 
     * @throws RGSException
     */
    private void checkJavaInnerIdentifier(String s) throws RGSException
    {
        if (s.length() == 0)
        {
            throw new RGSException("Invalid inner identifier '" + s + "'");
        }
        for (int i = 0; i < s.length(); i++)
        {
            if (!Character.isJavaIdentifierPart(s.charAt(i)))
            {
                throw new RGSException("Invalid inner identifier '" + s + "'");
            }
        }
    }
}
