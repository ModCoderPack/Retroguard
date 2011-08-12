/* ===========================================================================
 * $RCSfile: TreeAction.java,v $
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

import COM.rl.obf.classfile.ClassFileException;

/**
 * Set of actions to be performed by a tree walker
 * 
 * @author Mark Welsh
 */
public class TreeAction
{
    /**
     * @param pk
     * @throws ClassFileException
     */
    public void packageAction(Pk pk) throws ClassFileException
    {
        this.defaultAction(pk);
    }

    /**
     * @param cl
     * @throws ClassFileException
     */
    public void classAction(Cl cl) throws ClassFileException
    {
        this.defaultAction(cl);
    }

    /**
     * @param md
     * @throws ClassFileException
     */
    public void methodAction(Md md) throws ClassFileException
    {
        this.defaultAction(md);
    }

    /**
     * @param fd
     * @throws ClassFileException
     */
    public void fieldAction(Fd fd) throws ClassFileException
    {
        this.defaultAction(fd);
    }

    /**
     * @param ti
     * @throws ClassFileException
     */
    public void defaultAction(TreeItem ti) throws ClassFileException
    {
        // do nothing
    }
}
