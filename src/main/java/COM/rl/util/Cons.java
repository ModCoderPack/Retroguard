/* ===========================================================================
 * $RCSfile: Cons.java,v $
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

/**
 * A 'cons' of two references -- useful as a generic return grouping from Enumerations.
 *
 * @author      Mark Welsh
 */
public class Cons
{
    // Fields ----------------------------------------------------------------
    public Object car;
    public Object cdr;


    // Instance Methods ---------------------------------------------------------
    /** Ctor. */
    public Cons(Object car, Object cdr)
    {
        this.car = car;
        this.cdr = cdr;
    }
}
