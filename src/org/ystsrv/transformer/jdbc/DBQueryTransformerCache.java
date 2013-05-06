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

package org.ystsrv.transformer.jdbc;

import java.util.Hashtable;
import java.util.Locale;

import org.ystsrv.debug.Debug;

class DBQueryTransformerCache {
  private static final String LOGGER_NAME = "ystsrv.transformer";

  private static Hashtable cachedTransformers = new Hashtable();

  static DBQueryTransformer getTransformer(String header, String format,
                                           String defaultDatePattern, Locale locale,
                                           String query) {
    Debug.prec(format);
    Debug.prec(query);
    query = query.toLowerCase();
    String key = header + '|' + format + '|' + defaultDatePattern + '|' + locale + '|' +
        extractToWhere(query);
    DBQueryTransformer t = (DBQueryTransformer)cachedTransformers.get(key);
    if (t == null) {
      synchronized (cachedTransformers) {
        // repito la comprobación por si dos hilos comprobaron a la vez que no
        // había formater. El primero habrá puesto en la cache ya el formatter,
        // pero el segundo segirá entrando en el if (t== null). Por eso
        // compruebo de nuevo aqui. El segundo verá que ya hay un formatter y no
        // hará nada
        t = (DBQueryTransformer)cachedTransformers.get(key);
        if (t == null) {
          Debug.info(LOGGER_NAME,
                     "Creating DBQueryTransformer for query " + query + "; header " + header +
                     ",format string " + format + ", default date pattern " + defaultDatePattern +
                     " and locale " +
                     locale);
          t = new DBQueryTransformer(header, format, defaultDatePattern, locale);
          cachedTransformers.put(key, t);
        }
      }
    }
    return t;
  }

  private static String extractToWhere(String query) {
    char[] sql = query.toCharArray();
    // puedo comenzar derctamente más alla de "select "
    int i = 6;
    while (i < sql.length) {
      if (sql[i] == '"') {
        // buscar el cierre (caso de algún alias)
        i = findEnd(sql, i, '"');
        // Si es -1  CHUNGO
      } else if (sql[i] == '\'') {
        // buscar el cierre (caso de algún alias)
        i = findEnd(sql, i, '\'');
      } else if (sql[i] == '(') {
        // ojo con anidamientos
        i = findEndNestable(sql, i, ')', '(');
      } else if (sql[i] == 'w') { // comienzo de where
        if (i + 1 < sql.length && sql[i + 1] == 'h' &&
            i + 2 < sql.length && sql[i + 2] == 'e' &&
            i + 3 < sql.length && sql[i + 3] == 'r' &&
            i + 4 < sql.length && sql[i + 4] == 'e') {
          break;
        }
      }
      i++;
    }
    String result = new String(sql, 0, i).trim();
    return result;
  }

  private static int findEnd(char[] txt, int init, char toFind) {
    for (int i = init + 1; i < txt.length; i++) {
      if (txt[i] == toFind)return i;
    }
    return -1;
  }

  private static int findEndNestable(char[] txt, int init, char toFind, char openChar) {
    int balance = 0;
    for (int i = init + 1; i < txt.length; i++) {
      if (txt[i] == openChar)balance++;
      if (txt[i] == toFind) {
        if (balance == 0)return i;
        else balance--;
      }
    }
    return -1;
  }
}
