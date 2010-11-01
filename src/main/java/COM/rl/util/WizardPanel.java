/* ===========================================================================
 * $RCSfile: WizardPanel.java,v $
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
 * A Panel which contains a CardLayout of other Components with prev/next/finish
 * buttons -- used for 'wizards'.
 *
 * @author      Mark Welsh
 */
public class WizardPanel extends Panel
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    public Panel cards;
    public Panel buttonCards;
    private int cardShown = 0;
    private int cardCount = 0;
    private Button finishButton;
    private Label statusField;

    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /** Ctor. */
    public WizardPanel(Panel[] panels, ActionListener firstNextListener,
                       ActionListener finishListener)
    {
        // Background color to dialog-gray
        setBackground(Color.lightGray);

        // Set up the CardLayout with 'prev'/'next'/'finish' buttons.
        setLayout(new BorderLayout());

        // (cards)
        cards = new Panel();
        cards.setLayout(new CardLayout());
        for (int i = 0; i < panels.length; i++)
        {
            cards.add(Integer.toString(i), panels[i]);
        }
        cardCount = panels.length;
        showPanelCard();

        // (buttons)
        buttonCards = new Panel();
        buttonCards.setLayout(new CardLayout());

        Panel buttonsPanelFirst = new Panel();
        buttonsPanelFirst.setLayout(new FlowLayout(FlowLayout.RIGHT));
        Button nextButton = new Button("next>");
        nextButton.addActionListener(firstNextListener);
        buttonsPanelFirst.add(nextButton);

        Panel buttonsPanelNormal = new Panel();
        buttonsPanelNormal.setLayout(new FlowLayout(FlowLayout.RIGHT));
        Button prevButton = new Button("<prev");
        nextButton = new Button("next>");
        prevButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {previousCard();}});
        nextButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {nextCard();}});
        buttonsPanelNormal.add(prevButton);
        buttonsPanelNormal.add(nextButton);

        Panel buttonsPanelLast = new Panel();
        buttonsPanelLast.setLayout(new FlowLayout(FlowLayout.RIGHT));
        prevButton = new Button("<prev");
        finishButton = new Button("finish");
        prevButton.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {previousCard();}});
        finishButton.addActionListener(finishListener);
        buttonsPanelLast.add(prevButton);
        buttonsPanelLast.add(finishButton);

        buttonCards.add("first", buttonsPanelFirst);
        buttonCards.add("normal", buttonsPanelNormal);
        buttonCards.add("last", buttonsPanelLast);
        showButtonCard();

        // A status bar to the left of the flip buttons
        statusField = new Label("                                                                     ");
        statusField.setBackground(Color.lightGray);
        statusField.setFont(new Font("Helvetica", Font.PLAIN, 12));
        Panel statusBorder = new Panel() {public void paint(Graphics g) {g.setColor(Color.lightGray); g.draw3DRect(6, 6, getSize().width - 12, getSize().height - 12, false);}};
        statusBorder.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 7));
        statusBorder.add(statusField);
        Panel lower = new Panel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        lower.setLayout(gridbag);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.insets = new Insets(4, 4, 4, 4);
        c.gridwidth = 1;
        gridbag.setConstraints(statusBorder, c);
        lower.add(statusBorder);
        c.weightx = 0.0;
        c.gridwidth = GridBagConstraints.REMAINDER; //end of row
        gridbag.setConstraints(buttonCards, c);
        lower.add(buttonCards);

        add("South", lower);
        add("Center", cards);
    }

    /** Flip from first to second card. */
    public void flipFirst() {nextCard();}

    /** Activate the finish button? */
    public void setFinishEnabled(boolean active) {finishButton.setEnabled(active);}

    /** Set the status field */
    public void setStatus(String status) {statusField.setText(status);}

    // Flip to previous card.
    void previousCard()
    {
        if (cardShown > 0)
        {
            cardShown--;
            showButtonCard();
            showPanelCard();
        }
    }

    // Flip to next card.
    void nextCard()
    {
        if (cardShown < cardCount - 1)
        {
            cardShown++;
            showButtonCard();
            showPanelCard();
        }
    }

    // Show the appropriate panel.
    private void showPanelCard()
    {
        ((CardLayout)cards.getLayout()).show(cards, Integer.toString(cardShown));
    }

    // Show the appropriate buttons.
    private void showButtonCard()
    {
        if (cardShown == cardCount - 1)
        {
            ((CardLayout)buttonCards.getLayout()).show(buttonCards, "last");
        }
        else if (cardShown == 0)
        {
            ((CardLayout)buttonCards.getLayout()).show(buttonCards, "first");
        }
        else
        {
            ((CardLayout)buttonCards.getLayout()).show(buttonCards, "normal");
        }
    }
}
