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

import java.util.Hashtable;
import java.util.Locale;

import org.ystsrv.debug.Debug;

class BeanFormatterCache {
  private static final String LOGGER_NAME = "ystsrv.transformer";

  private static Hashtable cachedFormatters = new Hashtable();

  static BeanFormatter getFormater(String format, String defaultDatePattern, Locale locale, Class clas) {
    Debug.prec(format);
    String key = format + defaultDatePattern + locale + clas.getName();
    BeanFormatter f = (BeanFormatter)cachedFormatters.get(key);
    if (f == null) {
      synchronized (cachedFormatters) {
        // repito la comprobación por si dos hilos comprobaron a la vez que no
        // había formater. El primero habrá puesto en la cache ya el formatter,
        // pero el segundo segirá entrando en el if (f== null). Por eso
        // compruebo de nuevo aqui. El segundo verá que ya hay un formatter y no
        // hará nada
        f = (BeanFormatter)cachedFormatters.get(key);
        if (f == null) {
          Debug.info(LOGGER_NAME, "Creating BeanFormatter for class "+clas.getName()+": format string "+format+", default date pattern "+defaultDatePattern+" and locale "+locale);
          f = new BeanFormatter(format, clas, defaultDatePattern, locale);
          cachedFormatters.put(key, f);
        }
      }
    }
    return f;
  }
}
