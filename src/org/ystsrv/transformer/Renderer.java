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
package org.ystsrv.transformer;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.ystsrv.ConfigurationException;
import org.ystsrv.util.TextUtils;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * @author Francisco José García Izquierdo
 * @version 2.0
 */
public class Renderer {
  private static final String LOGGER_NAME = "ystsrv.transformer";

  private static Hashtable cachedFormatters = new Hashtable();

  static String renderValue(Object value, String formatPattern, String defaultDatePattern, Locale locale) {
    if (value == null)
      return "null";

    Class valType = value.getClass();
    if (java.util.Date.class.isAssignableFrom(valType)) {
      String datePattern = getDatePattern(formatPattern, defaultDatePattern);
      return renderDate( (java.util.Date)value, datePattern, locale);
    } else if (java.util.Calendar.class.isAssignableFrom(valType)) {
      String datePattern = getDatePattern(formatPattern, defaultDatePattern);
      return renderDate( (java.util.Calendar)value, datePattern, locale);
    } else if (value instanceof Number || value instanceof Boolean) {
      return "" + value;
    } else if (valType.isArray()) {
      return renderArray(value, formatPattern, defaultDatePattern, locale);
    } else if (java.util.Map.class.isAssignableFrom(valType)) {
      return renderMap( (Map)value, formatPattern, defaultDatePattern, locale);
    } else if (java.util.Collection.class.isAssignableFrom(valType)) {
      return renderCollection( (Collection)value, formatPattern, defaultDatePattern, locale);
    } else if (value instanceof String || value instanceof Character) {
      return "'" + TextUtils.escape("" + value) + "'";
    } else return renderObject(value, formatPattern, defaultDatePattern, locale);
  }

  static String renderValue(Object value, String formatPattern) {
    return renderValue(value, formatPattern, null, null);
  }

  static String renderValue(Object value) {
    return renderValue(value, null, null, null);
  }


  public static String renderDate(Date d, String datePattern, Locale locale) {
    if (d == null)
      return "null";
    else {
      if (datePattern != null && datePattern.equals(""))
        datePattern=null;

      if (datePattern != null) {
        try {
          SimpleDateFormat sdf = Renderer.getDateFormater(datePattern, locale);
          String res;
          synchronized (sdf) {
            res ="'" + sdf.format(d) + "'";
          }
          return res;
        } catch (IllegalArgumentException ex) {
          throw new ConfigurationException("Illegal date pattern ("+datePattern+"). Check your configuration", ex);
        }
      } else
        return "new Date(" + d.getTime() + ")";
    }
  }

  static String renderDate(Calendar d, String datePattern, Locale locale) {
    if (d != null)
      return renderDate(d.getTime(), datePattern, locale);
    else
      return "null";
  }

  static String renderArray(Object array) {
    return renderArray(array, null, null, null);
  }

  static String renderArray(Object array, String formatPattern) {
    return renderArray(array, formatPattern, null, null);
  }

  static String renderArray(Object array, String formatPattern, String defaultDatePattern, Locale locale) {
    if (array == null)
      return "null";

    StringBuffer sb = new StringBuffer("[");
    if (array instanceof String[]) {
      return renderCollection(Arrays.asList( (String[])array));
    } else if (array instanceof boolean[]) {
      boolean[] a = (boolean[])array;
      for (int i = 0; i < a.length; i++) {
        sb.append(a[i] + ",");
      }
    } else if (array instanceof char[]) {
      char[] a = (char[])array;
      for (int i = 0; i < a.length; i++) {
        sb.append("'" + TextUtils.escape("" + a[i]) + "',");
      }
    } else if (array instanceof Character[]) {
      Character[] a = (Character[])array;
      for (int i = 0; i < a.length; i++) {
        sb.append("'" + TextUtils.escape("" + a[i]) + "',");
      }
    } else if (array instanceof int[]) {
      int[] a = (int[])array;
      for (int i = 0; i < a.length; i++) {
        sb.append(a[i] + ",");
      }
    } else if (array instanceof byte[]) {
      byte[] a = (byte[])array;
      for (int i = 0; i < a.length; i++) {
        sb.append(a[i] + ",");
      }
    } else if (array instanceof long[]) {
      long[] a = (long[])array;
      for (int i = 0; i < a.length; i++) {
        sb.append(a[i] + ",");
      }
    } else if (array instanceof short[]) {
      short[] a = (short[])array;
      for (int i = 0; i < a.length; i++) {
        sb.append(a[i] + ",");
      }
    } else if (array instanceof float[]) {
      float[] a = (float[])array;
      for (int i = 0; i < a.length; i++) {
        sb.append(a[i] + ",");
      }
    } else if (array instanceof double[]) {
      double[] a = (double[])array;
      for (int i = 0; i < a.length; i++) {
        sb.append(a[i] + ",");
      }
    } else if (array instanceof int[]) {
      byte[] a = (byte[])array;
      for (int i = 0; i < a.length; i++) {
        sb.append(a[i] + ",");
      }
    } else if (array instanceof java.util.Date[]) {
      String datePattern = getDatePattern(formatPattern, defaultDatePattern);

      java.util.Date[] a = (java.util.Date[])array;
      for (int i = 0; i < a.length; i++) {
        sb.append(Renderer.renderDate(a[i], datePattern, locale) + ",");
      }
    } else if (array instanceof java.util.Calendar[]) {
      String datePattern = getDatePattern(formatPattern, defaultDatePattern);

      java.util.Calendar[] a = (java.util.Calendar[])array;
      for (int i = 0; i < a.length; i++) {
        sb.append(Renderer.renderDate(a[i], datePattern, locale) + ",");
      }
    } else {
      Object[] a = (Object[])array;
      for (int i = 0; i < a.length; i++) {
        sb.append(renderValue(a[i], formatPattern, defaultDatePattern, locale) + ",");
      }
    }
    if (sb.charAt(sb.length() - 1) == ',')sb.deleteCharAt(sb.length() - 1);
    sb.append("]");
    return sb.toString();
  }

  private static String getDatePattern(String formatPattern, String defaultDatePattern) {
    String datePattern;
    if (formatPattern != null) {
      // Comprobaré que es válido
      SimpleDateFormat d = new SimpleDateFormat();
      try {
        d.applyPattern(formatPattern);
        datePattern = formatPattern;
      } catch (IllegalArgumentException ex) {
        datePattern = (defaultDatePattern != null ? defaultDatePattern : formatPattern);
      }
    } else {
      datePattern = defaultDatePattern;
    }
    return datePattern;
  }

  static String renderCollection(Collection col) {
    return renderCollection(col, null, null, null);
  }

  static String renderCollection(Collection col, String formatPatter) {
    return renderCollection(col, formatPatter, null, null);
  }

  static String renderCollection(Collection col, String formatPattern, String defaultDatePattern, Locale locale) {
    if (col != null) {
      StringBuffer sb = new StringBuffer("[");
      Iterator iter = col.iterator();
      while (iter.hasNext()) {
        Object o = iter.next();
          sb.append(renderValue(o, formatPattern, defaultDatePattern, locale) + ",");
      }
      if (sb.charAt(sb.length() - 1) == ',')sb.deleteCharAt(sb.length() - 1);
      sb.append("]");
      return sb.toString();
    } else
      return "null";
  }

  static String renderMap(Map map) {
    return renderMap(map, null, null, null);
  }

  static String renderMap(Map map, String formatPattern) {
    return renderMap(map, formatPattern, null, null);
  }

  static String renderMap(Map map, String formatPattern, String defaultDatePattern, Locale locale) {
    if (map != null) {
      StringBuffer sb = new StringBuffer("{");
      Iterator iter = map.keySet().iterator();
      while (iter.hasNext()) {
        Object k = iter.next();
        Object v = map.get(k);
          sb.append(k+":"+renderValue(v, formatPattern, defaultDatePattern, locale) + ",");
      }
      if (sb.charAt(sb.length() - 1) == ',')sb.deleteCharAt(sb.length() - 1);
      sb.append("}");
      return sb.toString();
    } else
      return "null";
  }

  private static SimpleDateFormat getDateFormater(String format, Locale locale) {
    String cKey = format+locale;
    SimpleDateFormat f = (SimpleDateFormat)cachedFormatters.get(cKey);
    if (f == null) {
      synchronized (cachedFormatters) {
        f = (SimpleDateFormat)cachedFormatters.get(cKey);
        if (f == null) {
          if (locale != null)
            f = new SimpleDateFormat(format, locale);
          else
            f = new SimpleDateFormat(format);
          cachedFormatters.put(cKey, f);
        }
      }
    }
    return f;
  }

  static String renderObject(Object obj, String format, String defaultDatePattern, Locale locale) {
    if (obj == null)
      return "null";
    else {
      if (format != null && format.trim().length()!=0) {
        BeanFormatter sdf = BeanFormatterCache.getFormater(format, defaultDatePattern, locale, obj.getClass());
        return sdf.format(obj);
      } else
        return "'" + TextUtils.escape("" + obj) + "'";
    }
  }

  public static void main(String[] args) {
    Map obj = new java.util.HashMap();
    obj.put("cadena","Hola\n\"Pepe\t\"");
    obj.put("char",new Character('H'));
    obj.put("int",new Integer(5));
    obj.put("long",new Long(9));
    obj.put("byte",new Byte((byte)8));
    obj.put("short",new Short((short)8));
    obj.put("float",new Float(1.3f));
    obj.put("double",new Double(1.9));
    obj.put("boolean",new Boolean(true));
    obj.put("date",new java.util.Date());
    obj.put("calendar",java.util.Calendar.getInstance());
    Collection col = new java.util.ArrayList(); col.add(new java.util.Date()); col.add(new Integer(7));col.add("Hola");
    obj.put("coleccion",col);
    obj.put("arrayCadenas",new String[]{"Hola","Pepe"});
    obj.put("arrayChars",new char[]{'u','r'});
    obj.put("arrayEnteros",new int[]{9,8});
    obj.put("arrayFloats",new float[]{1.2f,1.8f});
    obj.put("arrayBooleans",new boolean[]{true,false});
    obj.put("arrayCalendars",new java.util.Calendar[]{java.util.Calendar.getInstance(),java.util.Calendar.getInstance()});
    obj.put("arrayDates",new java.util.Date[]{new java.util.Date(),new java.util.Date()});

    Object obj2 = ((HashMap)obj).clone();
    obj.put("map",obj2);
  }
}
