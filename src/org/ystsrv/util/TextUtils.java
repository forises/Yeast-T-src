/*
 *  Yeast-Server for Java
 *
 *  Copyright (c) 2011, Francisco José García Izquierdo. University of La
 *  Rioja. Mathematics and Computer Science Department. All Rights Reserved.
 *
 *  Contributing Author(s):
 *
 *     Francisco J. García Iquierdo <francisco.garcia@unirioja.es>
 *
 *  COPYRIGHT PERMISSION STATEMENT:
 *
 *  This file is part of Yeast-Server for Java.
 *
 *  Yeast-Server for Java is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 3 of the
 *  License, or any later version.
 *
 *  Yeast-Server for Java is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  This software uses and includes a modificaction of jTidy-SNAPSHOT 8.0
 *  (Copyright (c) 1998-2000 World Wide Web Consortium).
 *  
 *  This software uses and includes a modificaction of Rhino JavaScript 1.7R1
 *  (see license at https://developer.mozilla.org/en/Rhino_License).
 *
 */
package org.ystsrv.util;

public class TextUtils {
  public static String escape(String str) {
    if (str == null) {
      return str;
    }
    StringBuffer strb = new StringBuffer();
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      switch (c) {
        case '"':
        case '\'':
        case '\\':
          strb.append('\\');
          strb.append(c);
          break;
        case '/':
          strb.append("\\/");
          break;
        case '\b':
          strb.append("\\b");
          break;
        case '\t':
          strb.append("\\t");
          break;
        case '\n':
          strb.append("\\n");
          break;
        case '\f':
          strb.append("\\f");
          break;
        case '\r':
          strb.append("\\r");
          break;
        default:
          if (c < ' ') {
            String t = "000" + Integer.toHexString(c);
            strb.append("\\u").append(t.substring(t.length() - 4));
          } else {
            strb.append(c);
          }
      }
    }
    return strb.toString();
  }



  public static String escapeNew(String str) {
    if (str == null) {
      return str;
    }
    StringBuffer strb = new StringBuffer();
    for (int i = 0; i < str.length(); i++) {
      escapeAux(str.charAt(i),strb);
    }
    return strb.toString();
  }


  public static String escape(char c) {
    StringBuffer strb = new StringBuffer(2);
    escapeAux(c, strb);
    return strb.toString();
  }


  private static void escapeAux(char c, StringBuffer strb) {
    switch (c) {
      case '"':
      case '\'':
      case '\\':
        strb.append('\\');
        strb.append(c);
        break;

      case '/':
        strb.append("\\/");
        break;
      case '\b':
        strb.append("\\b");
        break;
      case '\t':
        strb.append("\\t");
        break;
      case '\n':
        strb.append("\\n");
        break;
      case '\f':
        strb.append("\\f");
        break;
      case '\r':
        strb.append("\\r");
        break;
      default:
        if (c < ' ') {
          String t = "000" + Integer.toHexString(c);
          strb.append("\\u").append(t.substring(t.length() - 4));
        } else {
          strb.append(c);
        }
    }
  }


  public static String entities(String str) {
    if (str == null) {
      return str;
    }
    StringBuffer strb = new StringBuffer();
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      switch (c) {
      case '"':
        strb.append("&quot;");
        break;
      case '&':
        strb.append("&amp;");
        break;
      case '<':
        strb.append("&lt;");
        break;
      case '>':
        strb.append("&gt;");
        break;
      case '\u00A0':
        strb.append("&nbsp;");
        break;
      default:
          strb.append(c);
      }
    }
    return strb.toString();
  }

  public static String normalizePath(String path) {
    path = path.replace('\\', '/');

    if (!path.equals("/")) {
      if (!path.startsWith("/")) {
        path = '/' + path;
      }
      if (path.endsWith("/")) {
        path = path.substring(0, path.length() - 1);
      }
    }
    return path;
  }

  public static boolean isTrue(String value) {
    value = value.toLowerCase().trim();
    return value.equals("yes") || value.equals("true") || value.equals("on") || value.equals("1");
  }

  public static boolean isFalse(String value) {
    value = value.toLowerCase().trim();
    return value.equals("no") || value.equals("false") || value.equals("off") || value.equals("0");
  }

}
