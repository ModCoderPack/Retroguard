/**
 * RetroGuard -- an obfuscation package for Java classfiles.
 *
 * Copyright (c) 1998-2006 Mark Welsh (markw@retrologic.com)
 * 
 * This class was created by r3qu13m (r3qu13m.minecraft@gmail.com)
 *
 * This program can be redistributed and/or modified under the terms of the
 * Version 2 of the GNU General Public License as published by the Free
 * Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package com.rl.obf.classfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BootStrapMethod
{
    private short factory;
    private final List<Short> bsmArgs = new ArrayList<Short>();

    public void setFactory(final short factory)
    {
        this.factory = factory;
    }

    public void addArgument(final short arg)
    {
        this.bsmArgs.add(arg);
    }

    public List<Short> getArguments()
    {
        return Collections.unmodifiableList(this.bsmArgs);
    }

    public short getFactory()
    {
        return this.factory;
    }

}
