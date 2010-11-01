/* ===========================================================================
 * $RCSfile: WarningDialog.java,v $
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
import java.util.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Warning dialog box.
 *
 * @author      Mark Welsh
 */
public class WarningDialog extends Dialog
{
    public WarningDialog(Frame f, String title, String[] phrases)
    {
        this(f, title, phrases, false);
    }

    public WarningDialog(Frame f, String title, String[] phrases, boolean useTextArea)
    {
        super(f, title, false);

        // Warning text
        if (useTextArea)
        {
            TextArea ta = new TextArea(10, 75);
            //ta.setBackground(Color.lightGray);
            ta.setEditable(false);
            ta.setFont(new Font("Helvetica", Font.PLAIN, 12));
            for (int i = 0; i < phrases.length; i++)
            {
                ta.append(phrases[i]);
                ta.append("\n");
            }
            add("Center", ta);
        }
        else
        {
            Panel left = new Panel() {public Insets getInsets() {return new Insets(4, 4, 4, 4);}};
            left.setLayout(new GridLayout(0, 1));
            left.setFont(new Font("Helvetica", Font.PLAIN, 12));
            for (int i = 0; i < phrases.length; i++)
            {
                left.add(new Label(phrases[i]));
            }
            add("Center", left);
        }

        // Okay button
        Button okay = new Button("  Okay  ");
        Panel right = new Panel();
        right.setLayout(new FlowLayout(FlowLayout.CENTER));
        right.add(okay);
        add("East", right);

        // Set closing event handlers and pack to preferred size
        addWindowListener(new WindowAdapter() {public void windowClosing(WindowEvent e) {setVisible(false);}});
        okay.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {setVisible(false);}});
        pack();

        // Position the dialog centrally in its parent
        Rectangle bounds = f.getBounds();
        Rectangle myBounds = getBounds();
        setLocation(bounds.x + (bounds.width - myBounds.width) / 2, bounds.y + (bounds.height - myBounds.height) / 2);
        setVisible(true);
        getToolkit().beep();
    }
}

