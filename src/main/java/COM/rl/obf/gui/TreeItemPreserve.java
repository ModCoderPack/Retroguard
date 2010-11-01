/* ===========================================================================
 * $RCSfile: TreeItemPreserve.java,v $
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

package COM.rl.obf.gui;

import java.io.*;
import java.util.*;
import COM.rl.obf.*;

/**
 * Data structure for preservations.
 *
 * @author      Mark Welsh
 */
public abstract class TreeItemPreserve
{
    protected boolean isPreserve;
    public boolean isPreserve() {return isPreserve;}
    public void setPreserve(boolean isPreserve) {this.isPreserve = isPreserve;}
}
