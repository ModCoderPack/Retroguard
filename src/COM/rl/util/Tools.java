/* ===========================================================================
 * $RCSfile: Tools.java,v $
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
 * A Tools class containing generally useful, miscellaneous static methods.
 * 
 * @author Mark Welsh
 */
public class Tools
{
    // Constants -------------------------------------------------------------
    private static final char[] base64 =
    {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
        'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
        'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
        'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };
    private static final char pad = '=';


    // Fields ----------------------------------------------------------------


    // Class Methods ---------------------------------------------------------
    /**
     * Encode a byte[] as a Base64 (see RFC1521, Section 5.2) String.
     * 
     * @param b
     */
    public static String toBase64(byte[] b)
    {
        StringBuilder sb = new StringBuilder();
        for (int ptr = 0; ptr < b.length; ptr += 3)
        {
            sb.append(Tools.base64[(b[ptr] >> 2) & 0x3F]);
            if ((ptr + 1) < b.length)
            {
                sb.append(Tools.base64[((b[ptr] << 4) & 0x30) | ((b[ptr + 1] >> 4) & 0x0F)]);
                if ((ptr + 2) < b.length)
                {
                    sb.append(Tools.base64[((b[ptr + 1] << 2) & 0x3C) | ((b[ptr + 2] >> 6) & 0x03)]);
                    sb.append(Tools.base64[b[ptr + 2] & 0x3F]);
                }
                else
                {
                    sb.append(Tools.base64[(b[ptr + 1] << 2) & 0x3C]);
                    sb.append(Tools.pad);
                }
            }
            else
            {
                sb.append(Tools.base64[((b[ptr] << 4) & 0x30)]);
                sb.append(Tools.pad);
                sb.append(Tools.pad);
            }
        }
        return sb.toString();
    }
}
