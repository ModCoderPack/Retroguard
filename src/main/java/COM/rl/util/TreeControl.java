/* ===========================================================================
 * $RCSfile: TreeControl.java,v $
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

package COM.rl.util;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * A tree control which maintains its state simultaneously in one or more
 * List UI elements.
 *
 * @author      Mark Welsh
 */
public class TreeControl implements ActionListener, ItemListener
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private java.awt.List[] lists;
    private Vector content;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /** Ctor - multiple synchronized lists. */
    public TreeControl(java.awt.List[] lists)
    {
        init(lists);
    }

    /** Ctor - single list. */
    public TreeControl(java.awt.List list)
    {
        java.awt.List[] lists = {list};
        init(lists);
    }

    // Private initializer required because array specifiers cannot be constructed inline.
    private void init(java.awt.List[] lists)
    {
        // Intercept ActionEvent's from the lists, clear them, and make them single select
        this.lists = lists;
        for (int i = 0; i < lists.length; i++)
        {
            java.awt.List list = lists[i];
            list.addActionListener(this);
            list.addItemListener(this);
            list.removeAll();
            list.setMultipleMode(false);
        }

        // No content initially in the lists
        content = new Vector();
    }

    /** Get an indexed entry. */
    public TreeEntry getEntry(int i) throws Exception 
    {
        return (TreeEntry)content.elementAt(i);
    }

    /** Add an element to the root of the tree. */
    public void add(TreeEntry entry)
    {
        if (entry.canOpen())
        {
            entry.setOpen(false);
        }
        content.addElement(entry);
        for (int i = 0; i < lists.length; i++)
        {
            lists[i].add(entry.toString());
        }
    }

    /** Remove all elements from the tree. */
    public void removeAll()
    {
        content.removeAllElements();
        for (int i = 0; i < lists.length; i++)
        {
            lists[i].removeAll();
        }
    }

    /** Sole action is to open or close the selected list item. */
    public void actionPerformed(ActionEvent e)
    {
        // Disable lists
        for (int i = 0; i < lists.length; i++)
        {
            lists[i].setEnabled(false);
        }

        // Get the source List's current item index
        if (e.getSource() instanceof java.awt.List)
        {
            int index = ((java.awt.List)e.getSource()).getSelectedIndex();
            if (index != -1)
            {
                // Get the associated TreeEntry
                TreeEntry entry = (TreeEntry)content.elementAt(index);
                if (entry.canOpen())
                {
                    if (entry.isOpen())
                    {
                        close(index + 1, entry);
                    }
                    else
                    {
                        open(index + 1, entry);
                    }
                }
            }
        }

        // Enable lists
        for (int i = 0; i < lists.length; i++)
        {
            lists[i].setEnabled(true);
        }
    }

    /** Synchronize the lists when a selection is made in one of them. */
    public void itemStateChanged(ItemEvent e)
    {
        // Get the source List's current item index
        if (e.getSource() instanceof java.awt.List)
        {
            java.awt.List sourceList = (java.awt.List)e.getSource();
            int index = sourceList.getSelectedIndex();
            if (index != -1)
            {
                for (int i = 0; i < lists.length; i++)
                {
                    if (lists[i] != sourceList)
                    {
                        lists[i].select(index);
                    }
                }
            }
        }
    }

    // Open a single TreeEntry into the Lists
    private void open(int index, TreeEntry entry)
    {
        for (Enumeration enm = entry.elements(); enm.hasMoreElements(); )
        {
            TreeEntry childEntry = (TreeEntry)enm.nextElement();
            content.insertElementAt(childEntry, index);
            for (int i = 0; i < lists.length; i++)
            {
                lists[i].add(childEntry.toString(), index);
            }
            index++;
        }
        entry.setOpen(true);
    }

    // Recursively close a TreeEntry in the Lists
    private void close(int index, TreeEntry entry)
    {
        for (int i = 0; i < entry.childCount(); i++)
        {
            TreeEntry childEntry = (TreeEntry)content.elementAt(index);
            content.removeElementAt(index);
            for (int j = 0; j < lists.length; j++)
            {
                lists[j].remove(index);
            }
            if (childEntry.isOpen())
            {
                close(index, childEntry);
            }
        }
        entry.setOpen(false);
    }
}
