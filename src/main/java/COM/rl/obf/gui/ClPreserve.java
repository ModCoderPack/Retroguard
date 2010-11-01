/* ===========================================================================
 * $RCSfile: ClPreserve.java,v $
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
 * Data structure for class preservations.
 *
 * @author      Mark Welsh
 */
public class ClPreserve extends TreeItemPreserve
{
    private PkCl pkcl;
    private boolean isKeepPublic;
    private boolean isKeepProtected;
    private boolean isKeepMethodsAndFields;
    private boolean isKeepMethodsOnly;
    private boolean isKeepFieldsOnly;

    public PkCl getPkCl() {return pkcl;}

    public boolean isKeepPublic() {return isKeepPublic;}

    public boolean isKeepProtected() {return isKeepProtected;}

    public boolean isKeepMethodsAndFields() {return isKeepMethodsAndFields;}

    public boolean isKeepMethodsOnly() {return isKeepMethodsOnly;}

    public boolean isKeepFieldsOnly() {return isKeepFieldsOnly;}

    public void setPreserve(boolean isPreserve)
    {
        this.isPreserve = isPreserve;
        if (!isPreserve)
        {
            setKeepPublic(false);
        }
    }

    public void setKeepPublic(boolean isKeepPublic)
    {
        this.isKeepPublic = isKeepPublic;
        if (!isKeepPublic)
        {
            setKeepProtected(false);
            keepMethodsAndFields();
        }
    }

    public void setKeepProtected(boolean isKeepProtected) {this.isKeepProtected = isKeepProtected;}

    public void keepMethodsAndFields()
    {
        isKeepMethodsAndFields = true;
        isKeepMethodsOnly = false;
        isKeepFieldsOnly = false;
    }

    public void keepMethodsOnly()
    {
        isKeepMethodsAndFields = false;
        isKeepMethodsOnly = true;
        isKeepFieldsOnly = false;
    }

    public void keepFieldsOnly()
    {
        isKeepMethodsAndFields = false;
        isKeepMethodsOnly = false;
        isKeepFieldsOnly = true;
    }

    public ClPreserve(PkCl pkcl)
    {
        this.pkcl = pkcl;
    }
}
