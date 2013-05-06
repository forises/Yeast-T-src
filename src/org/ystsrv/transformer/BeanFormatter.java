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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.ystsrv.ConfigurationException;
import org.ystsrv.debug.Debug;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author Francisco José García Izquierdo
 * @version 2.0
 */
class BeanFormatter {
  private static final String LOGGER_NAME = "ystsrv.transformer";

  private Class beanClass;
  private PropertiesAccessor properties;
  private List tokens;

  private String defaultDatePattern;  // it can be null
  private Locale locale;  // it can be null

  BeanFormatter(String format, Class beanClass, String defaultDatePattern, Locale locale) {
    if (format == null || format.trim().length() == 0)
      throw new IllegalArgumentException("Illegal format. It can not be null or empty");
    if (beanClass == null)
      throw new IllegalArgumentException("Illegal beanClass. It can not be null or empty");
    this.beanClass = beanClass;
    this.lexeFormat(format);
    properties = new PropertiesAccessor(beanClass);
    this.defaultDatePattern = defaultDatePattern; this.locale = locale;
  }

  private void lexeFormat(String format) {
    this.tokens = new ArrayList();
    int i = 0;
    StringBuffer token = new StringBuffer();
    int len = format.length();
    while (i < len) {
      if (format.charAt(i) == '\\' && (i + 1) < len &&
          (format.charAt(i + 1) == '{' || format.charAt(i + 1) == '}')) {
        i++;
      }
      if (format.charAt(i) == '{') {
        // Comienza propiedad. copia token a tokens
        if (i == 0 || i > 0 && format.charAt(i - 1) != '\\') {
          if (token.length() != 0) tokens.add(token.toString());  // Añado el token anterior
          token = new StringBuffer();
          StringBuffer prop = new StringBuffer();
          StringBuffer form = null;
          int init = i;
          i++;

          // Quita posibles espacios en blanco antes del nombre de la propiedad
          while (i < len && format.charAt(i) == ' ')i++;

          boolean propStarted = (format.charAt(i) != '%');

          while (i < len && format.charAt(i) != '}') {
            if (format.charAt(i) == '%') {
              // Comienzo de especificación de formato de propiedad
              form = new StringBuffer();
              if (!propStarted)
                throw new ConfigurationException("Bad format string specification for class "+
                                                 this.beanClass+": " + format +
                                                 "\n. Column cannot begin with % in position " + (i+1));
              i++;
              int curlyBracesBalance = 0;
              // Paro cuando agoto la cadena de formato (i>=len) or cuando alcanzo un caracter %
              // que no está dentro de la especificación de una propiedad (curlyBracesBalance%2==0)
              // (format.charAt(i)=='%' && curlyBracesBalance%2==0)
              while (i < len && (format.charAt(i) != '%' || curlyBracesBalance%2!=0)) {
                if (format.charAt(i) == '\\' && (i + 1) < len && format.charAt(i + 1) == '%') {
                  // % escapado
                  i++;
                } else if (format.charAt(i) == '{') { // llave interna al formato (inicio propiedad en formato)
                  curlyBracesBalance++;
                } else if (format.charAt(i) == '}') {
                  curlyBracesBalance--;
                }
                // Cojo formato
                form.append(format.charAt(i));
                i++;
              }
              if (i == len) { // agotada cadena sin encontrar pareja %
                throw new ConfigurationException("Bad format string specification for class "+
                                                 this.beanClass+": " + format +
                                                 "\n. Property format specification not finished in position " + (init+1));
              }
            }

            else if (format.charAt(i) == '{') {
              throw new ConfigurationException("Bad format string specification for class "+
                                               this.beanClass+": " + format +
                                               "\n. Nested { in position " + (i+1));
            } else {
              if (format.charAt(i) != ' ' && form != null && form.length() > 0)
                throw new ConfigurationException("Bad format string specification for class "+
                                                 this.beanClass+": " + format +
                                                 "\n. No text after format specification " +
                                                 form + " is allowed " + (init + 1));
              prop.append(format.charAt(i));
            }
            i++;
          }
          if (i == len) { // agotada cadena sin encontrar pareja }
            throw new ConfigurationException("Bad format string specification for class "+
                                                 this.beanClass+": " + format +
                                                 "\n. Curly brace mismatch for { in position " + (init+1));
          } else { // Se acabó la propiedad
            String propName = prop.toString().trim();
            if (propName.length() != 0) {
              tokens.add(new PropertyHolder(prop.toString(), (form != null ? form.toString():null)));
            }
          }
        } else { // era una { escapada con \
          token.append(format.charAt(i));
        }
      } else if (format.charAt(i) == '}') {  // Es una llave de cierre descolocada
        if (i == 0 || i > 0 && format.charAt(i - 1) != '\\') {
          throw new ConfigurationException("Bad format string specification for class "+
                                                 this.beanClass+": " + format +
                                                 "\n. Curly brace mismatch for } in position " + (i+1));
        } else {  // Una llave de cierre escapada con  \
          token.append(format.charAt(i));
        }
      } else { // texto normal
        token.append(format.charAt(i));
      }
      i++;
    }
    if (token.length() != 0) {
      tokens.add(token.toString());
    }
  }

  String format(Object value) {
    return this.formatObj(0, value);
  }

  // La cabecera en una linea y el resto de items en lineas nuevas
  String format(String header, Collection values) {
    StringBuffer sb = new StringBuffer( (header != null ? header : "")+"\n");
    int i = 0;
    Iterator iter = values.iterator();
    while (iter.hasNext()) {
      Object obj = iter.next();
      sb.append(this.formatObj(i++, obj));
      if (iter.hasNext()) sb.append("\n");
    }
    return sb.toString();
  }

  private String formatObj(int order, Object value) {
    if (value == null)
      return "";

    if (!this.beanClass.isInstance(value))
      throw new IllegalArgumentException("This transformer only accepts objects of class "+this.beanClass.getName());

    try {
      int propIdx = 0;
      StringBuffer formatedValue = new StringBuffer();

      Iterator iter = this.tokens.iterator();
      while (iter.hasNext()) {
        Object token = iter.next();
        if (token instanceof PropertyHolder) {
          PropertyHolder p = (PropertyHolder)token;
          if (p.name.equals("#i")) {
            formatedValue.append(order);
          } else {
            String propName = p.name;
            Object propValue = this.properties.getProperty(propName, value);
            formatedValue.append(Renderer.renderValue(propValue, p.format, this.defaultDatePattern, this.locale));
          }
          propIdx++;
        } else {
          formatedValue.append(token);
        }
      }
      return formatedValue.toString();
    } catch (InvocationTargetException ex) {
      throw new ConfigurationException("Impossible to transform the variable of " +
                                           this.beanClass +
                                           ". Problems accesing to the bean properties.", ex);
    } catch (IllegalAccessException ex) {
      Debug.fine(LOGGER_NAME, ex);
      throw new ConfigurationException("Impossible to transform the variable of " +
                                           this.beanClass +
                                           ". Problems accesing to the bean properties [" +
                                           ex.getMessage() + "].", ex);
    }
  }

  private static void replaceSubStr(StringBuffer src, String from, String to) {
    int len = from.length();
    int fromIndx = src.indexOf(from);
    while (fromIndx != -1) {
      src.replace(fromIndx, fromIndx + len, to);
      fromIndx = src.indexOf(from, fromIndx);
    }
  }

  private class PropertyHolder {
    String name;
    String format;

    PropertyHolder(String name, String format) {
      this.name = name.trim();
        this.format = format;
    }
  }
}
