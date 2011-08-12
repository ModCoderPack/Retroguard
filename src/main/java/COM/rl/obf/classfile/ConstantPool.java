/* ===========================================================================
 * $RCSfile: ConstantPool.java,v $
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
 * A representation of the data in a Java class-file's Constant Pool.
 * Constant Pool entries are managed by reference counting.
 * 
 * @author Mark Welsh
 */
public class ConstantPool
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private ClassFile myClassFile;
    private List pool;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /**
     * Constructor, which initializes Constant Pool using an array of CpInfo.
     * 
     * @param classFile
     * @param cpInfo
     */
    public ConstantPool(ClassFile classFile, CpInfo[] cpInfo)
    {
        this.myClassFile = classFile;
        this.pool = new ArrayList(Arrays.asList(cpInfo));
    }

    /**
     * Return an Iterator of all Constant Pool entries.
     */
    public Iterator iterator()
    {
        return this.pool.iterator();
    }

    /**
     * Return the Constant Pool length.
     */
    public int length()
    {
        return this.pool.size();
    }

    /**
     * Return the specified Constant Pool entry.
     * 
     * @param i
     * @throws ClassFileException
     */
    public CpInfo getCpEntry(int i) throws ClassFileException
    {
        try
        {
            return (CpInfo)this.pool.get(i);
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new ClassFileException("Constant Pool index out of range.");
        }
    }

    /**
     * Set the reference count for each element, using references from the owning ClassFile.
     * 
     * @throws ClassFileException
     */
    public void updateRefCount() throws ClassFileException
    {
        // Reset all reference counts to zero
        this.walkPool(new PoolAction()
        {
            @Override
            public void defaultAction(CpInfo cpInfo)
            {
                cpInfo.resetRefCount();
            }
        });

        // Count the direct references to Utf8 entries
        this.myClassFile.markUtf8Refs();

        // Count the direct references to NameAndType entries
        this.myClassFile.markNTRefs();

        // Go through pool, clearing the Utf8 entries which have no references
        this.walkPool(new PoolAction()
        {
            @Override
            public void utf8Action(Utf8CpInfo cpInfo)
            {
                if (cpInfo.getRefCount() == 0)
                {
                    cpInfo.clearString();
                }
            }
        });
    }

    /**
     * Increment the reference count for the specified element.
     * 
     * @param i
     * @throws ClassFileException
     */
    public void incRefCount(int i) throws ClassFileException
    {
        CpInfo cpInfo = this.getCpEntry(i);
        if (cpInfo == null)
        {
            // TODO check this
            // This can happen for JDK1.2 code so remove - 981123
//            throw new ClassFileException("Illegal access to a Constant Pool element.");
            return;
        }

        cpInfo.incRefCount();
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
        this.decRefCount(oldIndex);
        return this.addUtf8Entry(newString);
    }

    /**
     * Decrement the reference count for the specified element, blanking if Utf and refs are zero.
     * 
     * @param i
     * @throws ClassFileException
     */
    public void decRefCount(int i) throws ClassFileException
    {
        CpInfo cpInfo = this.getCpEntry(i);
        if (cpInfo == null)
        {
            // TODO check this
            // This can happen for JDK1.2 code so remove - 981123
//            throw new CPException("Illegal access to a Constant Pool element.");
            return;
        }

        cpInfo.decRefCount();
    }

    /**
     * Add an entry to the constant pool and return its index.
     * 
     * @param entry
     */
    public int addEntry(CpInfo entry)
    {
        // Try to replace an old, blanked Utf8 entry
        int index = this.pool.size();
        this.pool.add(entry);
        return index;
    }

    /**
     * Add a string to the constant pool and return its index.
     * 
     * @param s
     */
    protected int addUtf8Entry(String s)
    {
        // Search pool for the string. If found, just increment the reference count and return the index
        for (int i = 0; i < this.pool.size(); i++)
        {
            Object o = this.pool.get(i);
            if (o instanceof Utf8CpInfo)
            {
                Utf8CpInfo entry = (Utf8CpInfo)o;
                if (entry.getString().equals(s))
                {
                    entry.incRefCount();
                    return i;
                }
            }
        }

        // No luck, so try to overwrite an old, blanked entry
        for (int i = 0; i < this.pool.size(); i++)
        {
            Object o = this.pool.get(i);
            if (o instanceof Utf8CpInfo)
            {
                Utf8CpInfo entry = (Utf8CpInfo)o;
                if (entry.getRefCount() == 0)
                {
                    entry.setString(s);
                    entry.incRefCount();
                    return i;
                }
            }
        }

        // Still no luck, so append a fresh Utf8CpInfo entry to the pool
        return this.addEntry(new Utf8CpInfo(s));
    }

    /**
     * Data walker
     */
    class PoolAction
    {
        /**
         * @param cpInfo
         */
        public void utf8Action(Utf8CpInfo cpInfo)
        {
            this.defaultAction(cpInfo);
        }

        /**
         * @param cpInfo
         */
        public void defaultAction(CpInfo cpInfo)
        {
            // do nothing
        }
    }

    /**
     * @param pa
     */
    private void walkPool(PoolAction pa)
    {
        for (Iterator iter = this.pool.iterator(); iter.hasNext();)
        {
            Object o = iter.next();
            if (o instanceof Utf8CpInfo)
            {
                pa.utf8Action((Utf8CpInfo)o);
            }
            else if (o instanceof CpInfo)
            {
                pa.defaultAction((CpInfo)o);
            }
        }
    }
}
