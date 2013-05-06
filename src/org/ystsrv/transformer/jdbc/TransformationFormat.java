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

import java.util.Locale;

/**
 * Objects of this class are used to specify transformation formats for
 * <code>DBQuery</code> results. The rows of the result of the <code>DBQuery</code> execution will be
 * transformed into JavaScript data structures, usually an array of objects or
 * several arrays of primitive data. Each member of the JavaScript array may
 * correspond to one row of the query result. The JavaScript structures may need
 * to be initialized using some JavaScript code.
 * <code>TransformationFormat</code> objects allow the specification of this
 * initialization code by means of a String parameter provided in the object
 * construction (<code>header</code> param). This parameter is optional.
 *
 * <p>In order to transform each row of the query result it is necessary to
 * provide a <code>format</code> string, which will act as a template for the
 * transformation. In that format string you can insert, enclosed between curly
 * braces ({}), references to the data base columns or expressions included in
 * the query. To refer to a certain column of the result you must use its name
 * or its position in the result (the first column corresponds to the number 1).
 * When the transformation is performed, this transformer will substitute these
 * references with the values of the referred columns.Values are transformed
 * following the same rules specified for the class {@link
 * org.ystsrv.transformer.BeanTransformer}.
 *
 * <p>For the transformation of date values you can specify a default format
 * pattern that will be used if no specific pattern is provided for a certain
 * property. Moreover a <code>java.util.Locale</code> object may be supplied for
 * date transformation.
 *
 * @author Francisco José García Izquierdo
 * @version 1.0
 */
public class TransformationFormat {
  private String header;
  private String format;
  private String defaultDatePattern;
  private Locale locale;

  public TransformationFormat(String header, String format,
                              String defaultDatePattern, Locale locale) {
    if (format == null || format.trim().length()==0) {
      throw new IllegalArgumentException("Empty format string not allowed");
    }
    this.format = format;
    this.header = header;
    this.defaultDatePattern = defaultDatePattern;
    this.locale = locale;
  }

  public TransformationFormat(String header, String format, String defaultDatePattern) {
    this(header, format, defaultDatePattern, null);
  }

  public TransformationFormat(String header, String format) {
    this(header, format, null, null);
  }

  public TransformationFormat(String format) {
    this(null, format, null, null);
  }

  public String getHeader() {
    return header;
  }

  public String getFormat() {
    return format;
  }

  public String getDefaultDatePattern() {
    return defaultDatePattern;
  }

  public Locale getLocale() {
    return locale;
  }

}
