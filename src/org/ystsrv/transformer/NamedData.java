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

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import org.ystsrv.ConfigurationException;
import org.ystsrv.util.TextUtils;

/**
 * Objects of this class associate a <i>name</i> with a data <i>value</i> (which
 * can be null). The name will be used to name the corresponding JavaScript
 * variable in which this object will be transformed.
 *
 * <p>The general from with which a <code>NamedData</code> is included in the
 * model section is <code>&lt;varName&gt;=&lt;varValue&gt;;</code>, where the
 * <code>varName</code> is the name provided in the <code>NamedData</code>
 * object construction, and the format of <code>varValue</code> depends on the
 * value data type. Note that the semicolon is added at the end. When the value
 * is a Collection or an array the transformation corresponds to the definition
 * of a JavaScript array, such as <code>&lt;arrayName&gt;=[...];</code> where
 * <code>arrayName</code> is the <code>NamedData</code> name, and the values
 * included in the array can adopt diferent forms, depending on its type. Note
 * that, again, the semicolon is added at the end.
 *
 * <p>Objects of this class can hold a single data of textual, numeric, boolean,
 * temporal ({@link java.util.Date} or {@link java.util.Calendar}) types, or
 * objects that follow the bean specification. They can also hold collections or
 * arrays of those types or, recursively, collections of collections of those
 * types.
 *
 * @author Francisco José García Izquierdo
 * @version 1.3
 * @see org.ystsrv.transformer.NamedDataTransformer
 */
public class NamedData {
  private String name;
  private String value;

  /**
   * Creates a <code>NamedData</code> object with a <code>name</code> and
   * textual <code>value</code>.
   *
   * <p>The resulting transformation will be
   * <code>&lt;name&gt;='&lt;value&gt;';</code>
   *
   * @param name String. It can not be null
   * @param value String. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         a string containing 0-n white spaces
   */
  public NamedData(String name, String value) {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = (value != null ? "'" + TextUtils.escape(value) + "'" : null);
  }

  /**
   * Creates a <code>NamedData</code> object with a <code>name</code> and
   * boolean <code>value</code>.
   *
   * <p>The resulting transformation will be <code>&lt;name&gt;=true;</code> or
   * <code>&lt;name&gt;=false;</code>
   *
   * @param name String. It can not be null.
   * @param value boolean
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         a string containing 0-n white spaces
   */
  public NamedData(String name, boolean value) {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = "" + value;
  }

  /**
   * Creates a <code>NamedData</code> object with a <code>name</code> and a
   * temporal <code>value</code>.
   *
   * <p>The resulting transformation will be <code>&lt;name&gt;=new
   * Date(x);</code> where <code>x</code> is the number of milliseconds since
   * January 1, 1970, 00:00:00 GMT represented by the <code>value</code>.
   *
   * @param name String. It can not be null
   * @param value java.util.Date. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         a string containing 0-n white spaces
   */
  public NamedData(String name, Date value) {
    this(name, value, null);
  }

  /**
   * Creates a <code>NamedData</code> object with a <code>name</code> and a
   * temporal <code>value</code>. An string pattern can be specified for date
   * representation. Pattern specification must follow the rules described for
   * the <code>java.text.SimpleDateFormat</code> class (see <a
   * href="http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html">http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html</a>)
   *
   * <p>The resulting transformation will be
   * <code>&lt;name&gt;='&lt;formated_value&gt;';</code> where
   * <code>&lt;formated_value&gt;</code> is the representation of the
   * <code>value</code> according to the pattern provided in
   * <code>datePattern</code>.
   *
   * @param name String. It can not be null
   * @param value java.util.Date. It can be null
   * @param datePattern Format string to be used as pattern for the
   *   rendering of the date values. If it is null or the empty string (""),
   *   then the date value will be represented as <code>new Date(x);</code>
   *   where <code>x</code> is the number of milliseconds since January 1, 1970,
   *   00:00:00 GMT represented by the date.
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         a string containing 0-n white spaces
   * @throws ConfigurationException if the given <code>datePattern</code> is not
   *         suitable for the date rendering
   */
  public NamedData(String name, Date value, String datePattern) throws ConfigurationException {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = Renderer.renderDate(value, datePattern, null);
  }

  /**
   * Creates a <code>NamedData</code> object with a <code>name</code> and a
   * temporal <code>value</code>. An string pattern can be specified for date
   * representation. Pattern specification must follow the rules described for
   * the <code>java.text.SimpleDateFormat</code> class (see <a
   * href="http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html">http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html</a>)
   *
   * <p>The resulting transformation will be
   * <code>&lt;name&gt;='&lt;formated_value&gt;';</code> where
   * <code>&lt;formated_value&gt;</code> is the representation of the
   * <code>value</code> according to the pattern provided in
   * <code>datePattern</code>.
   *
   * @param name String. It can not be null
   * @param value java.util.Date. It can be null
   * @param datePattern Format string to be used as pattern for the
   *   rendering of the date values. If it is null or the empty string (""),
   *   then the date value will be represented as <code>new Date(x);</code>
   *   where <code>x</code> is the number of milliseconds since January 1, 1970,
   *   00:00:00 GMT represented by the date.
   * @param locale java.util.Locale. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         a string containing 0-n white spaces
   * @throws ConfigurationException if the given <code>datePattern</code> is not
   *         suitable for the date rendering
   */
  public NamedData(String name, Date value, String datePattern, Locale locale) throws ConfigurationException {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = Renderer.renderDate(value, datePattern, locale);
  }


  /**
   * Creates a <code>NamedData</code> object with a <code>name</code> and a
   * temporal <code>value</code>.
   *
   * <p>The resulting transformation will be <code>&lt;name&gt;=new
   * Date(x);</code> where <code>x</code> is the number of milliseconds since
   * January 1, 1970, 00:00:00 GMT represented by the <code>value</code>.
   *
   * @param name String. It can not be null
   * @param value java.util.Calendar. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         a string containing 0-n white spaces
   */
  public NamedData(String name, Calendar value) {
    this(name, value, null);
  }

  /**
   * Creates a <code>NamedData</code> object with a <code>name</code> and a
   * temporal <code>value</code>. An string pattern can be specified for date
   * representation. Pattern specification must follow the rules described for
   * the <code>java.text.SimpleDateFormat</code> class (see <a
   * href="http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html">http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html</a>)
   *
   * <p>The resulting transformation will be
   * <code>&lt;name&gt;='&lt;formated_value&gt;';</code> where
   * <code>&lt;formated_value&gt;</code> is the representation of the
   * <code>value</code> according to the pattern provided in
   * <code>datePattern</code>.
   *
   * @param name String. It can not be null
   * @param value java.util.Calendar. It can be null
   * @param datePattern Format string to be used as pattern for the
   *   rendering of the date values. If it is null or the empty string (""),
   *   then the date value will be represented as <code>new Date(x);</code>
   *   where <code>x</code> is the number of milliseconds since January 1, 1970,
   *   00:00:00 GMT represented by the date.
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         a string containing 0-n white spaces
   * @throws ConfigurationException if the given <code>datePattern</code> is not
   *         suitable for the date rendering
   */
  public NamedData(String name, Calendar value, String datePattern) throws ConfigurationException {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = Renderer.renderDate(value, datePattern, null);
  }

  /**
   * Creates a <code>NamedData</code> object with a <code>name</code> and a
   * temporal <code>value</code>. An string pattern can be specified for date
   * representation. Pattern specification must follow the rules described for
   * the <code>java.text.SimpleDateFormat</code> class (see <a
   * href="http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html">http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html</a>)
   *
   * <p>The resulting transformation will be
   * <code>&lt;name&gt;='&lt;formated_value&gt;';</code> where
   * <code>&lt;formated_value&gt;</code> is the representation of the
   * <code>value</code> according to the pattern provided in
   * <code>datePattern</code>.
   *
   * @param name String. It can not be null
   * @param value java.util.Calendar. It can be null
   * @param datePattern Format string to be used as pattern for the
   *   rendering of the date values. If it is null or the empty string (""),
   *   then the date value will be represented as <code>new Date(x);</code>
   *   where <code>x</code> is the number of milliseconds since January 1, 1970,
   *   00:00:00 GMT represented by the date.
   * @param locale java.util.Locale. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         a string containing 0-n white spaces
   * @throws ConfigurationException if the given <code>datePattern</code> is not
   *         suitable for the date rendering
   */
  public NamedData(String name, Calendar value, String datePattern, Locale locale) throws ConfigurationException {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = Renderer.renderDate(value, datePattern, locale);
  }

  /**
   * Creates a <code>NamedData</code> for long values.
   *
   * <p>The resulting transformation will be
   * <code>&lt;name&gt;=&lt;value&gt;</code>
   *
   * @param name String. It can not be null
   * @param value long
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         a string containing 0-n white spaces
   */
  public NamedData(String name, long value) {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = "" + value;
  }

  /**
   * Creates a <code>NamedData</code> for double values.
   *
   * <p>The resulting transformation will be
   * <code>&lt;name&gt;=&lt;value&gt;</code>
   *
   * @param name String. It can not be null
   * @param value double
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         a string containing 0-n white spaces
   */
  public NamedData(String name, double value) {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = "" + value;
  }

  /**
   * Creates a <code>NamedData</code> for object values.
   *
   * <p>The resulting transformation will be
   * <code>&lt;name&gt;='&lt;objectValue&gt;';</code>, where
   * <code>&lt;objectValue&gt</code> is the string version of the object.
   *
   * @param name String. It can not be null
   * @param value Object. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         a string containing 0-n white spaces
   */
  public NamedData(String name, Object value) {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = Renderer.renderValue(value,null,null,null);
  }


  /**
   * Creates a <code>NamedData</code> for object values, that is expected to be
   * a Java Bean. A format string is provided for the object.
   *
   * <p>The resulting transformation will be
   * <code>&lt;name&gt;='&lt;formatted_value&gt;';</code>, where
   * <code>&lt;formatted_value&gt</code> is the string that corresponds to the
   * objects transformation according to the <code>formatPattern</code>. The
   * transformation process is described in the {@link BeanTransformer} class.
   *
   * @param name String. It can not be null
   * @param value Object. It can be null
   * @param formatPattern String containing the format for the object
   *        transformation. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *   the empty string
   * @throws ConfigurationException if the given <code>formatPattern</code> is
   *   null or the empty string or it is malformed (e.g., curly brace
   *   mismatches); if there are problems getting the introspection info of the
   *   object class, or any of its properties; if the format string contains the
   *   specification of non-existing bean properties, or properties
   *   specification that end with dot (e.g. <code>{client.}</code>); if there
   *   is any problem accessing the object properties (e.g. no public get
   *   method); if the invocation of the get method of any property throws any
   *   exception
   */
  public NamedData(String name, Object value, String formatPattern) throws ConfigurationException {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = Renderer.renderValue(value,formatPattern,null,null);
  }

  /**
   * Creates a <code>NamedData</code> for object values, that is expected to be
   * a Java Bean. A format string is provided for the object, as well as a
   * default date pattern is specified for those bean properties of type Date or
   * Calendar.
   *
   * <p>The resulting transformation will be
   * <code>&lt;name&gt;='&lt;formatted_value&gt;';</code>, where
   * <code>&lt;formatted_value&gt</code> is the string that corresponds to the
   * objects transformation according to the <code>formatPattern</code> and
   * <code>defaultDatePattern</code>. The transformation process is described in
   * the {@link BeanTransformer} class.
   *
   * @param name String. It can not be null
   * @param value Object. It can be null
   * @param formatPattern String containing the format for the object
   *        transformation. It can be null
   * @param defaultDatePattern Format string to be used as pattern for the
   *   rendering of the date values. If it is null or the empty string (""),
   *   then the date value will be represented as <code>new Date(x);</code>
   *   where <code>x</code> is the number of milliseconds since January 1, 1970,
   *   00:00:00 GMT represented by the date.
   * @param locale java.util.Locale. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *   the empty string
   * @throws ConfigurationException if the given <code>formatPattern</code> is
   *   null or the empty string or it is malformed (e.g., curly brace
   *   mismatches); if there are problems getting the introspection info of the
   *   object class, or any of its properties; if the format string contains the
   *   specification of non-existing bean properties, or properties
   *   specification that end with dot (e.g. <code>{client.}</code>); if there
   *   is any problem accessing the object properties (e.g. no public get
   *   method); if the invocation of the get method of any property throws any
   *   exception; if the given <code>defaultDatePattern</code> is not suitable
   *   for the date rendering
   */
  public NamedData(String name, Object value, String formatPattern, String defaultDatePattern, Locale locale) throws ConfigurationException {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = Renderer.renderValue(value,formatPattern,defaultDatePattern, locale);
  }

  /**
   * Creates a <code>NamedData</code> object given the collection of
   * (<code>values</code>) and the <code>name</code> of the JavaScript array
   * that will be created in the model section of the Yeast Template
   *
   * <p>The resulting transformation will be
   * <code>&lt;name&gt;=[x,y,...];</code>, where <code>x</code>, <code>y</code>,
   * ... are the representation of each one of the values contained in the
   * Collection. Each valueis represented according to its type.
   * <ul>
   * <li>If the <code>value</code> is null the representation will be
   * <code>null</code>.</li>
   * <li>Textual data are represented between simple quotes (e.g.
   * <code>'John';</code>). </li>
   * <li>Numeric data are represented without quotes (e.g.
   * <code>38;</code>). </li>
   * <li>Boolean data are represented with <code>true</code> or
   * <code>false</code> without quotes. </li>
   * <li>Temporal data are represented as JavaScript Date object. This
   * JavaScript Date object is built with the number of milliseconds represented
   * by the value (e.g. <code>new Date(1139329452078);</code>).</li>
   * <li>If the value is a <code>java.util.Collection</code> or an array, the
   * object will be represented in the form of JavaScript array, just in the
   * same way we are specifying</li>
   * <li>If the value is an Object of another type, the object will be
   * represented using its string version (using the <code>toString</code>
   * method).</li>
   * </ul>
   *
   * @param name String. It can not be null
   * @param values Collection. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *   the empty string
   */
  public NamedData(String name, Collection values) {
    this(name, values, null);
  }

  /**
   * Creates a <code>NamedData</code> object given the collection of
   * (<code>values</code>) and the <code>name</code> of the JavaScript array
   * that will be created in the model section of the Yeast Template. The
   * <code>formatPattern</code> parameter is used to specify a format for the
   * collection objects rendering. It should be applicable to every collection
   * object. In the case of Calendar or Date object collections this format
   * represents the date pattern for date rendering. Otherwise, for collections
   * of bean objects, this format string must follow the rules specified in the
   * class {@link BeanTransformer}. Date patterns specification must follow the
   * rules described for the <code>java.text.SimpleDateFormat</code> class (see
   * <ahref="http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html">http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html</a>)
   *
   * <p>The resulting transformation will be
   * <code>&lt;name&gt;=[x,y,...];</code>, where <code>x</code>, <code>y</code>,
   * ... are the representation of each one of the values contained in the
   * Collectionaccording to the <code>formatPattern</code>. The transformation
   * process of each value is described in the {@link BeanTransformer} class.
   *
   * @param name String. It can not be null
   * @param values Collection. It can be null
   * @param formatPattern Format string to be used as pattern for object
   *   rendering. It can be null (then the toString() method of each object in
   *   the collection will be used for the object rendering
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *   the empty string
   * @throws ConfigurationException if the given <code>formatPattern</code> is
   *   null or the empty string or it is malformed (e.g., curly brace
   *   mismatches); if there are problems getting the introspection info of the
   *   object class, or any of its properties; if the format string contains the
   *   specification of non-existing bean properties, or properties
   *   specification that end with dot (e.g. <code>{client.}</code>); if there
   *   is any problem accessing the object properties (e.g. no public get
   *   method); if the invocation of the get method of any property throws any
   *   exception
   */
  public NamedData(String name, Collection values, String formatPattern) throws ConfigurationException {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = Renderer.renderCollection(values,formatPattern,null, null);
  }

  /**
   * Similar to the {@link NamedData#NamedData(String, java.util.Collection,
   * String)} constructor, but in this case a default date pattern can be
   * specified. This pattern will be used for the rendering of a Date or
   * Calendar property of the objects contained in the collection, when no
   * specific format is provided in the <code>formatPattern</code> for that
   * property. The <code>defaultDatePattern</code> will be used also in case of
   * the collection containing a mixture of objects, among which there are Date
   * or Calendar objects.
   *
   * @param name String. It can not be null
   * @param values Collection. It can be null
   * @param formatPattern Format string to be used as pattern for object
   *   rendering. It can be null (then the toString() method of each object in
   *   the collection will be used for the object rendering
   * @param defaultDatePattern Format string to be used as pattern for the
   *   rendering of the date values. If it is null or the empty string (""),
   *   then the date value will be represented as <code>new Date(x);</code>
   *   where <code>x</code> is the number of milliseconds since January 1,
   *   1970, 00:00:00 GMT represented by the date.
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *   the empty string
   * @throws ConfigurationException if the given <code>formatPattern</code> is
   *   null or the empty string or it is malformed (e.g., curly brace
   *   mismatches); if there are problems getting the introspection info of the
   *   object class, or any of its properties; if the format string contains the
   *   specification of non-existing bean properties, or properties
   *   specification that end with dot (e.g. <code>{client.}</code>); if there
   *   is any problem accessing the object properties (e.g. no public get
   *   method); if the invocation of the get method of any property throws any
   *   exception; if the given <code>defaultDatePattern</code> is not suitable
   *   for the date rendering
   */
  public NamedData(String name, Collection values, String formatPattern, String defaultDatePattern) throws ConfigurationException {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = Renderer.renderCollection(values, formatPattern, defaultDatePattern, null);
  }


  /**
   * Similar to the {@link NamedData#NamedData(String, java.util.Collection,
   * String)} constructor, but in this case a default date pattern can be
   * specified. This pattern will be used for the rendering of a Date or
   * Calendar property of the objects contained in the collection, when no
   * specific format is provided in the <code>formatPattern</code> for that
   * property. The <code>defaultDatePattern</code> will be used also in case of
   * the collection containing a mixture of objects, among which there are Date
   * or Calendar objects.
   *
   * @param name String. It can not be null
   * @param values Collection. It can be null
   * @param formatPattern Format string to be used as pattern for object
   *   rendering. It can be null (then the toString() method of each object in
   *   the collection will be used for the object rendering
   * @param defaultDatePattern Format string to be used as pattern for the
   *   rendering of the date values. If it is null or the empty string (""),
   *   then the date value will be represented as <code>new Date(x);</code>
   *   where <code>x</code> is the number of milliseconds since January 1,
   *   1970, 00:00:00 GMT represented by the date.
   * @param locale java.util.Locale. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *   the empty string
   * @throws ConfigurationException if the given <code>formatPattern</code> is
   *   null or the empty string or it is malformed (e.g., curly brace
   *   mismatches); if there are problems getting the introspection info of the
   *   object class, or any of its properties; if the format string contains the
   *   specification of non-existing bean properties, or properties
   *   specification that end with dot (e.g. <code>{client.}</code>); if there
   *   is any problem accessing the object properties (e.g. no public get
   *   method); if the invocation of the get method of any property throws any
   *   exception; if the given <code>defaultDatePattern</code> is not suitable
   *   for the date rendering
   */
  public NamedData(String name, Collection values, String formatPattern, String defaultDatePattern, Locale locale) throws ConfigurationException {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = Renderer.renderCollection(values, formatPattern, defaultDatePattern, locale);
  }

  /**
   * Creates a <code>NamedData</code> object given the array of
   * (<code>values</code>) and the <code>name</code> of the JavaScript array
   * that will be created in the model section of the Yeast Template
   *
   * <p>The resulting transformation will be
   * <code>&lt;name&gt;=[x,y,...];</code>, where <code>x</code>, <code>y</code>,
   * ... are the values contained in the array
   *
   * @param name String. It can not be null
   * @param values array of byte. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *   the empty string
   */
  public NamedData(String name, byte... values) {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = Renderer.renderArray(values);
  }

  /**
   * Creates a <code>NamedData</code> object given the array of
   * (<code>values</code>) and the <code>name</code> of the JavaScript array
   * that will be created in the model section of the Yeast Template
   *
   * <p>The resulting transformation will be
   * <code>&lt;name&gt;=[x,y,...];</code>, where <code>x</code>, <code>y</code>,
   * ... are the values contained in the array
   *
   * @param name String. It can not be null
   * @param values array of short. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         the empty string
   */
  public NamedData(String name, short... values) {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = Renderer.renderArray(values);
  }

  /**
   * Creates a <code>NamedData</code> object given the array of
   * (<code>values</code>) and the <code>name</code> of the JavaScript array
   * that will be created in the model section of the Yeast Template
   *
   * <p>The resulting transformation will be
   * <code>&lt;name&gt;=[x,y,...];</code>, where <code>x</code>, <code>y</code>,
   * ... are the values contained in the array
   *
   * @param name String. It can not be null
   * @param values array of long. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         the empty string
   */
  public NamedData(String name, long... values) {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = Renderer.renderArray(values);
  }

  /**
   * Creates a <code>NamedData</code> object given the array of
   * (<code>values</code>) and the <code>name</code> of the JavaScript array
   * that will be created in the model section of the Yeast Template
   *
   * <p>The resulting transformation will be
   * <code>&lt;name&gt;=[x,y,...];</code>, where <code>x</code>, <code>y</code>,
   * ... are the values contained in the array
   *
   * @param name String. It can not be null
   * @param values array of float. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         the empty string
   */
  public NamedData(String name, float... values) {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = Renderer.renderArray(values);
  }

  /**
   * Creates a <code>NamedData</code> object given the array of
   * (<code>values</code>) and the <code>name</code> of the JavaScript array
   * that will be created in the model section of the Yeast Template
   *
   * <p>The resulting transformation will be
   * <code>&lt;name&gt;=[x,y,...];</code>, where <code>x</code>, <code>y</code>,
   * ... are the values contained in the array
   *
   * @param name String. It can not be null
   * @param values array of double. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         the empty string
   */
  public NamedData(String name, double... values) {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = Renderer.renderArray(values);
  }

  /**
   * Creates a <code>NamedData</code> object given the array of
   * (<code>values</code>) and the <code>name</code> of the JavaScript array
   * that will be created in the model section of the Yeast Template
   *
   * <p>The resulting transformation will be
   * <code>&lt;name&gt;=[x,y,...];</code>, where <code>x</code>, <code>y</code>,
   * ... are the values contained in the array
   *
   * @param name String. It can not be null
   * @param values array of int. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         the empty string
   */
  public NamedData(String name, int... values) {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = Renderer.renderArray(values);
  }

  /**
   * Creates a <code>NamedData</code> object given the array of
   * (<code>values</code>) and the <code>name</code> of the JavaScript array
   * that will be created in the model section of the Yeast Template
   *
   * <p>The resulting transformation will be
   * <code>&lt;name&gt;=['x','y',...];</code>, where <code>x</code>,
   * <code>y</code>, ... are the values contained in the array
   *
   * @param name String. It can not be null
   * @param values array of char. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         the empty string
   */
  public NamedData(String name, char... values) {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = Renderer.renderArray(values);
  }

  /**
   * Creates a <code>NamedData</code> object given the array of
   * (<code>values</code>) and the <code>name</code> of the JavaScript array
   * that will be created in the model section of the Yeast Template
   *
   * <p>The resulting transformation will be
   * <code>&lt;name&gt;=[true,false,...];</code>, representing the values
   * contained in the <code>values</code> array
   *
   * @param name String. It can not be null
   * @param values array of boolean. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         the empty string
   */
  public NamedData(String name, boolean... values) {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = Renderer.renderArray(values);
  }


  /**
   * Similar to {@link NamedData#NamedData(String name, Collection values)} but
   * for arrays of Objects.
   *
   * @param name String. It can not be null
   * @param values array of Object. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *   the empty string
   */
  public NamedData(String name, Object[] values) {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = Renderer.renderArray(values,null,null, null);
  }

  /**
   * Similar to {@link NamedData#NamedData(String name, Collection values,
   * String formatPattern)} but for arrays of Objects.
   *
   * @param name String. It can not be null
   * @param values array of Object. It can be null
   * @param formatPattern Format string to be used as pattern for object
   *   rendering. It can be null (then the toString() method of each object in
   *   the collection will be used for the object rendering
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *   the empty string
   * @throws ConfigurationException if the given <code>formatPattern</code> is
   *   null or the empty string or it is malformed (e.g., curly brace
   *   mismatches); if there are problems getting the introspection info of the
   *   object class, or any of its properties; if the format string contains the
   *   specification of non-existing bean properties, or properties
   *   specification that end with dot (e.g. <code>{client.}</code>); if there
   *   is any problem accessing the object properties (e.g. no public get
   *   method); if the invocation of the get method of any property throws any
   *   exception
   */
  public NamedData(String name, Object[] values, String formatPattern) throws ConfigurationException {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = Renderer.renderArray(values,formatPattern,null,null);
  }

  /**
   * Similar to {@link NamedData#NamedData(String name, Collection values,
   * String formatPattern, String defaultDatePattern, Locale locale)} but for
   * arrays of Objects.
   *
   * @param name String. It can not be null
   * @param values array of Object. It can be null
   * @param formatPattern Format string to be used as pattern for object
   *   rendering. It can be null (then the toString() method of each object in
   *   the collection will be used for the object rendering
   * @param defaultDatePattern Format string to be used as pattern for the
   *   rendering of the date values. If it is null or the empty string (""),
   *   then the date value will be represented as <code>new Date(x);</code>
   *   where <code>x</code> is the number of milliseconds since January 1,
   *   1970, 00:00:00 GMT represented by the date.
   * @throws ConfigurationException if the given <code>formatPattern</code> is
   *   null or the empty string or it is malformed (e.g., curly brace
   *   mismatches); if there are problems getting the introspection info of the
   *   object class, or any of its properties; if the format string contains
   *   the specification of non-existing bean properties, or properties
   *   specification that end with dot (e.g. <code>{client.}</code>); if there
   *   is any problem accessing the object properties (e.g. no public get
   *   method); if the invocation of the get method of any property throws any
   *   exception; if the given <code>defaultDatePattern</code> is not suitable
   *   for the date rendering
   */
  public NamedData(String name, Object[] values, String formatPattern, String defaultDatePattern) throws ConfigurationException {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = Renderer.renderArray(values,formatPattern,defaultDatePattern, null);
  }

  /**
   * Similar to {@link NamedData#NamedData(String name, Collection values,
   * String formatPattern, String defaultDatePattern, Locale locale)} but for
   * arrays of Objects.
   *
   * @param name String. It can not be null
   * @param values array of Object. It can be null
   * @param formatPattern Format string to be used as pattern for object
   *   rendering. It can be null (then the toString() method of each object in
   *   the collection will be used for the object rendering
   * @param defaultDatePattern Format string to be used as pattern for the
   *   rendering of the date values. If it is null or the empty string (""),
   *   then the date value will be represented as <code>new Date(x);</code>
   *   where <code>x</code> is the number of milliseconds since January 1,
   *   1970, 00:00:00 GMT represented by the date.
   * @param locale java.util.Locale. It can be null
   * @throws ConfigurationException if the given <code>formatPattern</code> is
   *   null or the empty string or it is malformed (e.g., curly brace
   *   mismatches); if there are problems getting the introspection info of the
   *   object class, or any of its properties; if the format string contains
   *   the specification of non-existing bean properties, or properties
   *   specification that end with dot (e.g. <code>{client.}</code>); if there
   *   is any problem accessing the object properties (e.g. no public get
   *   method); if the invocation of the get method of any property throws any
   *   exception; if the given <code>defaultDatePattern</code> is not suitable
   *   for the date rendering
   */
  public NamedData(String name, Object[] values, String formatPattern,
                   String defaultDatePattern, Locale locale) throws ConfigurationException {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = Renderer.renderArray(values,formatPattern,defaultDatePattern, locale);
  }

  /**
   * Similar to {@link NamedData#NamedData(String name, Collection values,
   * String formatPattern)} but for arrays of Dates.
   *
   * @param name String. It can not be null
   * @param values array of Object. It can be null
   * @param datePattern Format string to be used as pattern for the rendering
   *   of the date values. If it is null or the empty string (""), then the
   *   date value will be represented as <code>new Date(x);</code> where
   *   <code>x</code> is the number of milliseconds since January 1, 1970,
   *   00:00:00 GMT represented by the date.
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *   the empty string
   * @throws ConfigurationException if the given <code>datePattern</code> is not
   *         suitable for the date rendering
   */
  public NamedData(String name, Date[] values, String datePattern) throws ConfigurationException {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = Renderer.renderArray(values,null, datePattern, null);
  }

  /**
   * Similar to {@link NamedData#NamedData(String name, Collection values,
   * String formatPattern)} but for arrays of Dates.
   *
   * @param name String. It can not be null
   * @param values array of Object. It can be null
   * @param datePattern Format string to be used as pattern for the rendering
   *   of the date values. If it is null or the empty string (""), then the
   *   date value will be represented as <code>new Date(x);</code> where
   *   <code>x</code> is the number of milliseconds since January 1, 1970,
   *   00:00:00 GMT represented by the date.
   * @param locale java.util.Locale. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *   the empty string
   * @throws ConfigurationException if the given <code>datePattern</code> is not
   *         suitable for the date rendering
   */
  public NamedData(String name, Date[] values, String datePattern, Locale locale) throws ConfigurationException {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = Renderer.renderArray(values,null, datePattern, locale);
  }

  /**
   * Similar to {@link NamedData#NamedData(String name, Collection values,
   * String formatPattern)} but for arrays of Calendars.
   *
   * @param name String. It can not be null
   * @param values array of Object. It can be null
   * @param datePattern Format string to be used as pattern for the rendering
   *   of the date values. If it is null or the empty string (""), then the
   *   date value will be represented as <code>new Date(x);</code> where
   *   <code>x</code> is the number of milliseconds since January 1, 1970,
   *   00:00:00 GMT represented by the date.
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *   the empty string
   * @throws ConfigurationException if the given <code>datePattern</code> is not
   *         suitable for the date rendering
   */
  public NamedData(String name, Calendar[] values, String datePattern) throws ConfigurationException {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = Renderer.renderArray(values,null, datePattern, null);
  }

  /**
   * Similar to {@link NamedData#NamedData(String name, Collection values,
   * String formatPattern)} but for arrays of Calendars.
   *
   * @param name String. It can not be null
   * @param values array of Object. It can be null
   * @param datePattern Format string to be used as pattern for the rendering
   *   of the date values. If it is null or the empty string (""), then the
   *   date value will be represented as <code>new Date(x);</code> where
   *   <code>x</code> is the number of milliseconds since January 1, 1970,
   *   00:00:00 GMT represented by the date.
   * @param locale java.util.Locale. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *   the empty string
   * @throws ConfigurationException if the given <code>datePattern</code> is not
   *         suitable for the date rendering
   */
  public NamedData(String name, Calendar[] values, String datePattern, Locale locale) throws ConfigurationException {
    if (name == null || name.trim().length() == 0)
      throw new IllegalArgumentException("Illegal name. It can not be null nor empty");

    this.name = name;
    this.value = Renderer.renderArray(values,null, datePattern, locale);
  }

  /**
   * Returns a string version of the object in the form
   * <code>&lt;varName&gt;=&lt;value&gt;;</code>, where <code>varName</code> is
   * the name encapsulated in this object (provided in the constructor), and
   * <code>value</code> is represented as specified in the documentation of the
   * corresponding object constructor.
   *
   * <p>The {@link org.ystsrv.transformer.NamedDataTransformer} will use
   * this <code>toYSTModel</code> method to transform this
   * <code>NamedData</code> object.
   *
   * @return String
   */
  public String toYSTModel() {
    return name + "=" + value + ";";
  }
}
