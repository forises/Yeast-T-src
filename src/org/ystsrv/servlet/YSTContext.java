/*
 *  Yeast-Server for Java
 *
 *  Copyright (c) 2009, Francisco José García Izquierdo. University of La
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
package org.ystsrv.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.ystsrv.ConfigurationException;
import org.ystsrv.ModelSection;
import org.ystsrv.debug.Debug;
import org.ystsrv.transformer.NamedData;
import org.ystsrv.yeipee.*;

/**
 * A <code>YSTContext</code> object contains all the data necessary to process a
 * web request and to process a <code>Template</code> in a
 * <code>YSTServlet</code>. It can be used inside the {@link YSTServlet#handle}
 * method to access to the <code>HttpServletRequest</code> and
 * <code>HttpServletResponse</code> objects of the servlet (this provides the
 * method with the servlet context, the servlet config, as well as access to the
 * input parameters and other commonly accessed services: cookies, session, and
 * so on).
 *
 * <p>The <code>YSTContext</code> is also used to make up the response of the
 * {@link YSTServlet#handle} method. The idea is to put all of the data you wish
 * to add to the Template model section into the <code>YSTContext</code>
 * (using the several <code>toResponse</code> methods). The
 * <code>YSTServlet</code> will retrieve these data, then insert them in a
 * <code>Template</code> via the {@link org.ystsrv.Template#print}
 * method. It is also possible to add textual data directly to the model section
 * using the {@link #toResponse(String)} method or getting access to the
 * associated {@link org.ystsrv.ModelSection} object ({@link
 * #getModelSection}). The final overall content for the model section will be
 * the transformation of the response objects together with the directly added
 * data.
 *
 * <p>A <code>YSTContext</code> object is a per-thread data structure. It should
 * not be shared between threads since it is not thread safe. The idea is to put
 * all of the state for a single request into the context and then execute it,
 * with each request having its own separate context. <code>YSTContext</code>
 * methods are not synchronized as they are not shared between threads.
 *
 * @author Francisco José García Izquierdo
 * @version 2.0
 */
public class YSTContext {

  private static final String LOGGER_NAME = "ystsrv.servlet";

  // Container for the objects to be included in the model section
  private List responseObjects;

  // Template store to be used in the response (the selected Template must be
  // in that store)
  private String templateStore;

  private HttpServletRequest request;

  private HttpServletResponse response;

  // Text data that is directly written to the Template model section without
  // any transformation
  private ModelSection modelContent;
  
  
  private boolean avoidBrowserCache;

  /**
   * Builds a YSTContext taking the <code>request</code> and
   * <code>response</code> objects of a servlet execution.
   * @param request HttpServletRequest
   * @param response HttpServletResponse
   * @throws IllegalArgumentException If the given <code>request</code> or
   *   <code>response</code> parameters are null
   */
  public YSTContext(HttpServletRequest request, HttpServletResponse response) {
    if (request == null) throw new IllegalArgumentException("request can not be null");
    if (response == null) throw new IllegalArgumentException("response can not be null");
    this.request = request;
    this.response = response;
    this.modelContent = new ModelSection();
  }


  /**@todo Si se decide prohibir meter datos null en el array a transformar avisar aqui */
  /**
   * Adds an array of objects that will be used to fulfill the actual model
   * section of the Yeast Template. These data will be transformed and pushed into
   * the <code>Template</code> when the template is printed to the requesting
   * client. Each of the array members will be added to the response
   * individually. The template must be initiallized with the suitable
   * transformers for each one of the objects contained in <code>data</code>.
   *
   * @param data array with objects that, once transformed, will be included in
   *   the model section. If some of the objects is null, then it will not be
   *   transformed. Only a JavaScript comment (<code>// Null data
   *   skipped</code>) will be inserted instead. Can be empty. It can not be null
   * @throws IllegalArgumentException If the given <code>data</code> array is
   *   null
   */
  public void toResponse(Object... data) {
    if (data == null) throw new IllegalArgumentException("data can not be null");
    for (int i = 0; i < data.length; i++) {
      toResponse(data[i]);
    }
    Debug.fine(LOGGER_NAME, "Added array to content: "+data);
  }


  /**@todo Si se decide prohibir meter datos null en el array a transformar avisar aqui */
  /**
   * Adds an object to be used to fulfill the <code>Template</code>. This
   * data will be transformed and pushed into the <code>Template</code> when
   * the template is printed to the requesting client. The template must be
   * initiallized with a suitable transformer for the <code>data</code> object.
   *
   * @param data Object. If it is null, data will be ignored. Only a JavaScript
   *   comment (<code>// Null data skipped</code>) will be inserted instead.

   */
  public void toResponse(Object data) {
    if (this.responseObjects == null)
      this.responseObjects = new ArrayList();
    this.responseObjects.add(data);
    Debug.fine(LOGGER_NAME, "Added object to content: "+data);
  }

  /**
   * Adds an String value with name to be used to fulfill the Template. This
   * data will be included in the actual model section of the Template as a
   * line with the format <code>name='value';</code>.
   *
   * @param name used to name de value. It can not be null
   * @param value String
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         a string containing 0-n white spaces
   */
  public void toResponse(String name, String value) {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, value).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named value to content: "+name+"-"+value);
  }

  /**
   * Adds a boolean value with name to be used to fulfill the Template. This
   * data will be included in the actual model section of the Template as a
   * line with the format <code>name=true;</code> or <code>name=false;</code>,
   * dependening on the value.
   *
   * @param name used to name de value. It can not be null
   * @param value boolean
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         a string containing 0-n white spaces
   */
  public void toResponse(String name, boolean value) {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, value).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named value to content: "+name+"-"+value);
  }

  /**
   * Adds a date value with name to be used to fulfill the Template. This
   * data will be included in the actual model section of the Template as a
   * line with the format <code>name=new Date(ddd);</code>, where
   * <code>ddd</code> is the number of milliseconds represented by the value,
   * milliseconds since January 1, 1970, 00:00:00 GMT.
   *
   * @param name used to name de value. It can not be null
   * @param value java.util.Date
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         a string containing 0-n white spaces
   */
  public void toResponse(String name, Date value) {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, value).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named value to content: "+name+"-"+value);
  }

  /**
   * Adds a date value with name to be used to fulfill the Template. An
   * string pattern can be specified for date representation. Pattern
   * specification must follow the rules described for the
   * <code>java.text.SimpleDateFormat</code> class (see
   * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html">http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html</a>)
   *
   *
   * <p>This data will be included in the actual model section of the
   * Template as a line with the following format:
   * <code>&lt;name&gt;='&lt;formated_value&gt;';</code> where
   * <code>&lt;formated_value&gt;</code> is the representation of the
   * <code>value</code> according to the pattern provided in
   * <code>datePattern</code>.
   *
   * @param name used to name de value. It can not be null
   * @param value java.util.Date
   * @param datePattern Format string to be used as pattern for the rendering
   *   of the date values. If it is null or the empty string (""), then the
   *   date value will be represented as <code>new Date(x);</code> where
   *   <code>x</code> is the number of milliseconds since January 1, 1970,
   *   00:00:00 GMT represented by the date.
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         a string containing 0-n white spaces
   * @throws ConfigurationException if the given <code>datePattern</code> is not
   *         suitable for the date rendering
   */
  public void toResponse(String name, Date value, String datePattern) throws ConfigurationException {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, value, datePattern).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named date value with format to content: "+name+"-"+value);
  }

  /**
   * Adds a date value with name to be used to fulfill the Template. An
   * string pattern can be specified for date representation. Pattern
   * specification must follow the rules described for the
   * <code>java.text.SimpleDateFormat</code> class (see
   * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html">http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html</a>)
   *
   *
   * <p>This data will be included in the actual model section of the
   * Template as a line with the following format:
   * <code>&lt;name&gt;='&lt;formated_value&gt;';</code> where
   * <code>&lt;formated_value&gt;</code> is the representation of the
   * <code>value</code> according to the pattern provided in
   * <code>datePattern</code>.
   *
   * @param name used to name de value. It can not be null
   * @param value java.util.Date
   * @param datePattern Format string to be used as pattern for the rendering
   *   of the date values. If it is null or the empty string (""), then the
   *   date value will be represented as <code>new Date(x);</code> where
   *   <code>x</code> is the number of milliseconds since January 1, 1970,
   *   00:00:00 GMT represented by the date.
   * @param locale Locale to be used in date rendering. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         a string containing 0-n white spaces
   * @throws ConfigurationException if the given <code>datePattern</code> is not
   *         suitable for the date rendering
   */
  public void toResponse(String name, Date value, String datePattern, Locale locale) throws ConfigurationException {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, value, datePattern, locale).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named date value with format to content: "+name+"-"+value);
  }


  /**
   * Adds a Calendar value with name to be used to fulfill the Template.
   * This data will be included in the actual model section of the Template
   * as a line with the format <code>name=new Date(ddd);</code>, where
   * <code>ddd</code> is the number of milliseconds represented by the value,
   * milliseconds since January 1, 1970, 00:00:00 GMT.
   *
   * @param name used to name de value. It can not be null
   * @param value java.util.Calendar
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         a string containing 0-n white spaces
   */
  public void toResponse(String name, Calendar value) {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, value).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named value to content: "+name+"-"+value);
  }

  /**
   * Adds a Calendar value with name to be used to fulfill the Template. An
   * string pattern can be specified for date representation. Pattern
   * specification must follow the rules described for the
   * <code>java.text.SimpleDateFormat</code> class (see
   * <ahref="http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html">http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html</a>)
   *
   * <p>This data will be included in the actual model section of the
   * Template as a line with the following format:
   * <code>&lt;name&gt;='&lt;formated_value&gt;';</code> where
   * <code>&lt;formated_value&gt;</code> is the representation of the
   * <code>value</code> according to the pattern provided in
   * <code>datePattern</code>.
   *
   * @param name used to name de value. It can not be null
   * @param value java.util.Calendar
   * @param datePattern Format string to be used as pattern for the rendering
   *   of the date values. If it is null or the empty string (""), then the
   *   date value will be represented as <code>new Date(x);</code> where
   *   <code>x</code> is the number of milliseconds since January 1, 1970,
   *   00:00:00 GMT represented by the date.
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         a string containing 0-n white spaces
   * @throws ConfigurationException if the given <code>datePattern</code> is not
   *         suitable for the date rendering
   */
  public void toResponse(String name, Calendar value, String datePattern) throws ConfigurationException {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, value, datePattern).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named date value with format to content: "+name+"-"+value);
  }

  /**
   * Adds a Calendar value with name to be used to fulfill the Template. An
   * string pattern can be specified for date representation. Pattern
   * specification must follow the rules described for the
   * <code>java.text.SimpleDateFormat</code> class (see
   * <ahref="http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html">http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html</a>)
   *
   * <p>This data will be included in the actual model section of the
   * Template as a line with the following format:
   * <code>&lt;name&gt;='&lt;formated_value&gt;';</code> where
   * <code>&lt;formated_value&gt;</code> is the representation of the
   * <code>value</code> according to the pattern provided in
   * <code>datePattern</code>.
   *
   * @param name used to name de value. It can not be null
   * @param value java.util.Calendar
   * @param datePattern Format string to be used as pattern for the rendering
   *   of the date values. If it is null or the empty string (""), then the
   *   date value will be represented as <code>new Date(x);</code> where
   *   <code>x</code> is the number of milliseconds since January 1, 1970,
   *   00:00:00 GMT represented by the date.
   * @param locale Locale to be used in date rendering. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         a string containing 0-n white spaces
   * @throws ConfigurationException if the given <code>datePattern</code> is not
   *         suitable for the date rendering
   */
  public void toResponse(String name, Calendar value, String datePattern, Locale locale) throws ConfigurationException {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, value, datePattern, locale).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named date value with format to content: "+name+"-"+value);
  }


  /**
   * Adds a long, int, short or byte value with name to be used to fulfill the
   * Template. This data will be included in the actual model section of the
   * Template as a line with the format <code>name=value;</code>.
   *
   * @param name used to name de value. It can not be null
   * @param value long
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         a string containing 0-n white spaces
   */
  public void toResponse(String name, long value) {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, value).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named value to content: "+name+"-"+value);
  }

  /**
   * Adds a float or double value with name to be used to fulfill the
   * Template. This data will be included in the actual model section of the
   * Template as a line with the format <code>name=value;</code>.
   *
   * @param name used to name de value. It can not be null
   * @param value double
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         a string containing 0-n white spaces
   */
  public void toResponse(String name, double value) {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, value).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named value to content: "+name+"-"+value);
  }

  /**
   * Adds an Object value with name to be used to fulfill the Template. This
   * data will be included in the actual model section of the Template as a
   * line with the format <code>&lt;name&gt;='&lt;objectValue&gt;';</code>,
   * where <code>&lt;objectValue&gt</code> is the string version of the object.
   *
   * @param name used to name de value. It can not be null
   * @param value Object. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null
   */
  public void toResponse(String name, Object value) {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, value).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named value to content: "+name+"-"+value);
  }

  /**
   * Adds an Object value with name to be used to fulfill the Template. The
   * object is expected to be a Java Bean. A format string is provided for the
   * object. This data will be included in the actual model section of the
   * Template as a line with the format
   * <code>&lt;name&gt;='&lt;formatted_value&gt;';</code>, where
   * <code>&lt;formatted_value&gt</code> is the string that corresponds to the
   * objects transformation according to the <code>formatPattern</code>. The
   * transformation process is described in the
   * {@link org.ystsrv.transformer.BeanTransformer} class.
   *
   * @param name String. It can not be null
   * @param value Object. It can be null
   * @param formatPattern String containing the format for the object
   *   transformation. It can be null
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
  public void toResponse(String name, Object value, String formatPattern) throws ConfigurationException {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, value, formatPattern).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named value to content: "+name+"-"+value);
  }

  /**
   * Adds an Object value with name to be used to fulfill the Template. The
   * object is expected to be a Java Bean. A format string is provided for the
   * object, and a default date pattern for the object date properties. This
   * data will be included in the actual model section of the Template as a
   * line with the format <code>&lt;name&gt;='&lt;formatted_value&gt;';</code>,
   * where <code>&lt;formatted_value&gt</code> is the string that corresponds to
   * the objects transformation according to the <code>formatPattern</code> and
   * <code>defaultDatePattern</code>. The transformation process is described in
   * the {@link org.ystsrv.transformer.BeanTransformer} class.
   *
   * @param name String. It can not be null
   * @param value Object. It can be null
   * @param formatPattern String containing the format for the object
   *   transformation. It can be null
   * @param defaultDatePattern Format string to be used as pattern for the
   *   rendering of the date values. If it is null or the empty string (""),
   *   then the date value will be represented as <code>new Date(x);</code>
   *   where <code>x</code> is the number of milliseconds since January 1,
   *   1970, 00:00:00 GMT represented by the date.
   * @param locale Locale to be used in date rendering. It can be null
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
  public void toResponse(String name, Object value, String formatPattern,
                         String defaultDatePattern, Locale locale) throws ConfigurationException {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, value, formatPattern, defaultDatePattern, locale).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named value to content: "+name+"-"+value);
  }

  /**
   * Adds an Object value with name to be used to fulfill the Template. The
   * object is expected to be a Java Bean. A format string is provided for the
   * object, and a default date pattern for the object date properties. This
   * data will be included in the actual model section of the Template as a
   * line with the format <code>&lt;name&gt;='&lt;formatted_value&gt;';</code>,
   * where <code>&lt;formatted_value&gt</code> is the string that corresponds to
   * the objects transformation according to the <code>formatPattern</code> and
   * <code>defaultDatePattern</code>. The transformation process is described in
   * the {@link org.ystsrv.transformer.BeanTransformer} class.
   *
   * @param name String. It can not be null
   * @param value Object. It can be null
   * @param formatPattern String containing the format for the object
   *   transformation. It can be null
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
  public void toResponse(String name, Object value, String formatPattern,
                         String defaultDatePattern) throws ConfigurationException {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, value, formatPattern, defaultDatePattern, null).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named value to content: "+name+"-"+value);
  }


  /**
   * Adds a collection of values with name to be used to fulfill the
   * Template. Values can be String, any primitive type wrappers, Date and
   * Calendar, Java Bean objects, or collections or arrays of the previous
   * types.
   * <p> This data will be included in the actual model section of the
   * Template as a line with the format <code>&lt;name&gt;=[x,y,...];</code>,
   * where <code>x</code>, <code>y</code>, ... are the representation of each
   * one of the values contained in the Collection. Each value is represented
   * according to its type:
   * <ul>
   * <li>If the <code>value</code> is null the representation will be
   * <code>null</code>.</li>
   * <li>Textual data are represented between simple quotes (e.g.
   * <code>'John';</code>). </li>
   * <li>Numeric data are represented without quotes (e.g. <code>38;</code>).
   * </li>
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
   * @param name used to name de values. It can not be null
   * @param values java.util.Collection. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *   the empty string
   */
  public void toResponse(String name, Collection values) {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, values).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named collection to content: "+name+"-"+values);
  }

  /**
   * Adds a collection of values with name to be used to fulfill the
   * Template. The <code>formatPattern</code> parameter is used to specify a
   * format for the collection objects rendering. It should be applicable to
   * every collection object. In the case of Calendar or Date object collections
   * this format represents the date pattern for date rendering. Otherwise, for
   * collections of bean objects, this format string must follow the rules
   * specified in the class
   * {@link org.ystsrv.transformer.BeanTransformer}. Date patterns
   * specification must follow the rules described for the
   * <code>java.text.SimpleDateFormat</code> class (see
   * <ahref="http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html">http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html</a>)
   *
   * <p> This data will be included in the actual model section of the
   * Template as a line with the format
   * <code>&lt;name&gt;=[x,y,...];</code>, where <code>x</code>, <code>y</code>,
   * ... are the representation of each one of the values contained in the
   * Collectionaccording to the <code>formatPattern</code>. The transformation
   * process of each value is described in the
   * {@link org.ystsrv.transformer.BeanTransformer} class.
   *
   * @param name used to name de values. It can not be null
   * @param values java.util.Collection. It can be null
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
  public void toResponse(String name, Collection values, String formatPattern) throws ConfigurationException {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, values, formatPattern).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named collection to content: "+name+"-"+values);
  }

  /**
   * Similar to the {@link YSTContext#toResponse(String name, Collection values,
   * String formatPattern, String defaultDatePattern)} method, but in this case
   * a default date pattern can be specified. This pattern will be used for the
   * rendering of a Date or Calendar property of the objects contained in the
   * collection, when no specific format is provided in the
   * <code>formatPattern</code> for that property. The
   * <code>defaultDatePattern</code> will be used also in case of the collection
   * containing a mixture of objects, among which there are Date or Calendar
   * objects.
   *
   * @param name used to name de values. It can not be null
   * @param values java.util.Collection. It can be null
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
  public void toResponse(String name, Collection values, String formatPattern, String defaultDatePattern) throws ConfigurationException {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, values, formatPattern, defaultDatePattern).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named collection to content: "+name+"-"+values);
  }

  /**
   * Similar to the {@link YSTContext#toResponse(String name, Collection values,
   * String formatPattern, String defaultDatePattern)} method, but in this case
   * a default date pattern can be specified. This pattern will be used for the
   * rendering of a Date or Calendar property of the objects contained in the
   * collection, when no specific format is provided in the
   * <code>formatPattern</code> for that property. The
   * <code>defaultDatePattern</code> will be used also in case of the collection
   * containing a mixture of objects, among which there are Date or Calendar
   * objects.
   *
   * @param name used to name de values. It can not be null
   * @param values java.util.Collection. It can be null
   * @param formatPattern Format string to be used as pattern for object
   *   rendering. It can be null (then the toString() method of each object in
   *   the collection will be used for the object rendering
   * @param defaultDatePattern Format string to be used as pattern for the
   *   rendering of the date values. If it is null or the empty string (""),
   *   then the date value will be represented as <code>new Date(x);</code>
   *   where <code>x</code> is the number of milliseconds since January 1,
   *   1970, 00:00:00 GMT represented by the date.
   * @param locale Locale to be used in date rendering. It can be null
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
  public void toResponse(String name, Collection values, String formatPattern,
                         String defaultDatePattern, Locale locale) throws ConfigurationException {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, values, formatPattern, defaultDatePattern, locale).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named collection to content: "+name+"-"+values);
  }


  /**
   * Adds an array of int values with an associated name to be used to fulfill
   * the Template. This data will be included in the actual model section of
   * the Template in the form of a Javascript array definition, with the
   * following format: <code>&lt;name&gt;=[x,y,...];</code>, where
   * <code>x</code>, <code>y</code>, ... are the values contained in the array.
   *
   * @param name used to name de values. It can not be null
   * @param values int[]
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         the empty string
   */
  public void toResponse(String name, int... values) {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, values).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named array to content: "+name+"-"+values);
  }

  /**
   * Adds an array of byte values with an associated name to be used to fulfill
   * the Template. This data will be included in the actual model section of
   * the Template in the form of a Javascript array definition, with the
   * following format: <code>&lt;name&gt;=[x,y,...];</code>, where
   * <code>x</code>, <code>y</code>, ... are the values contained in the array.
   *
   * @param name used to name de values. It can not be null
   * @param values byte[]
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         the empty string
   */
  public void toResponse(String name, byte... values) {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, values).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named array to content: "+name+"-"+values);
  }


  /**
   * Adds an array of short values with an associated name to be used to fulfill
   * the Template. This data will be included in the actual model section of
   * the Template in the form of a Javascript array definition, with the
   * following format: <code>&lt;name&gt;=[x,y,...];</code>, where
   * <code>x</code>, <code>y</code>, ... are the values contained in the array.
   *
   * @param name used to name de values. It can not be null
   * @param values short[]
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         the empty string
   */
  public void toResponse(String name, short... values) {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, values).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named array to content: "+name+"-"+values);
  }

  /**
   * Adds an array of long values with an associated name to be used to fulfill
   * the Template. This data will be included in the actual model section of
   * the Template in the form of a Javascript array definition, with the
   * following format: <code>&lt;name&gt;=[x,y,...];</code>, where
   * <code>x</code>, <code>y</code>, ... are the values contained in the array.
   *
   * @param name used to name de values. It can not be null
   * @param values long[]
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         the empty string
   */
  public void toResponse(String name, long... values) {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, values).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named array to content: "+name+"-"+values);
  }

  /**
   * Adds an array of float values with an associated name to be used to fulfill
   * the Template. This data will be included in the actual model section of
   * the Template in the form of a Javascript array definition, with the
   * following format: <code>&lt;name&gt;=[x,y,...];</code>, where
   * <code>x</code>, <code>y</code>, ... are the values contained in the array.
   *
   * @param name used to name de values. It can not be null
   * @param values float[]
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         the empty string
   */
  public void toResponse(String name, float... values) {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, values).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named array to content: "+name+"-"+values);
  }

  /**
   * Adds an array of double values with an associated name to be used to
   * fulfill the Template. This data will be included in the actual model
   * section of the Template in the form of a Javascript array definition,
   * with the following format: <code>&lt;name&gt;=[x,y,...];</code>, where
   * <code>x</code>, <code>y</code>, ... are the values contained in the array.
   *
   * @param name used to name de values. It can not be null
   * @param values double[]
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         the empty string
   */
  public void toResponse(String name, double... values) {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, values).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named array to content: "+name+"-"+values);
  }

  /**
   * Adds an array of char values with an associated name to be used to fulfill
   * the Template. This data will be included in the actual model section of
   * the Template in the form of a Javascript array definition, with the
   * following format: <code>&lt;name&gt;=['x','y',...];</code>, where
   * <code>x</code>, <code>y</code>, ... are the values contained in the array
   *
   * @param name used to name de values. It can not be null
   * @param values char[]
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         the empty string
   */
  public void toResponse(String name, char... values) {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, values).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named array to content: "+name+"-"+values);
  }

  /**
   * Adds an array of boolean values with an associated name to be used to
   * fulfill the Template. This data will be included in the actual model
   * section of the Template in the form of a Javascript array definition,
   * with the following format: <code>&lt;name&gt;=[true,false,...];</code>,
   * representing the values contained in the <code>values</code> array
   *
   * @param name used to name de values. It can not be null
   * @param values boolean[]
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         the empty string
   */
  public void toResponse(String name, boolean... values) {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, values).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named array to content: "+name+"-"+values);
  }

  /**
   * Similar to {@link YSTContext#toResponse(String name, Collection values)}
   * but for arrays of Objects.
   *
   * @param name used to name de values. It can not be null
   * @param values Object[]
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *   the empty string
   */
  public void toResponse(String name, Object[] values) {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, values).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named array to content: "+name+"-"+values);
  }

  /**
   * Similar to {@link YSTContext#toResponse(String name, Collection values,
   * String formatPattern)} but for arrays of Objects.
   *
   * @param name used to name de values. It can not be null
   * @param values Object[]
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
  public void toResponse(String name, Object[] values, String formatPattern) throws ConfigurationException {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, values,formatPattern).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named array to content: "+name+"-"+values);
  }

  /**
   * Similar to {@link YSTContext#toResponse(String name, Collection values,
   * String formatPattern, String defaultDatePattern)} but for arrays of
   * Objects.
   *
   * @param name used to name de values. It can not be null
   * @param values Object[]
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
  public void toResponse(String name, Object[] values, String formatPattern, String defaultDatePattern) throws ConfigurationException {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, values,formatPattern,defaultDatePattern).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named array to content: "+name+"-"+values);
  }

  /**
   * Similar to {@link YSTContext#toResponse(String name, Collection values,
   * String formatPattern, String defaultDatePattern)} but for arrays of
   * Objects.
   *
   * @param name used to name de values. It can not be null
   * @param values Object[]
   * @param formatPattern Format string to be used as pattern for object
   *   rendering. It can be null (then the toString() method of each object in
   *   the collection will be used for the object rendering
   * @param defaultDatePattern Format string to be used as pattern for the
   *   rendering of the date values. If it is null or the empty string (""),
   *   then the date value will be represented as <code>new Date(x);</code>
   *   where <code>x</code> is the number of milliseconds since January 1,
   *   1970, 00:00:00 GMT represented by the date.
   * @param locale Locale to be used in date rendering. It can be null
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
  public void toResponse(String name, Object[] values, String formatPattern,
                         String defaultDatePattern, Locale locale) throws ConfigurationException {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, values,formatPattern,defaultDatePattern, locale).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named array to content: "+name+"-"+values);
  }

  /**
   * Similar to {@link YSTContext#toResponse(String name, Collection values,
   * String formatPattern)} but for arrays of Date objects.
   *
   * @param name used to name de values. It can not be null
   * @param values Date[]
   * @param datePattern Format string to be used as pattern for the
   *   rendering of the date values. If it is null or the empty string (""),
   *   then the date value will be represented as <code>new Date(x);</code>
   *   where <code>x</code> is the number of milliseconds since January 1,
   *   1970, 00:00:00 GMT represented by the date.
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *   the empty string
   * @throws ConfigurationException if the given <code>datePattern</code> is not
   *         suitable for the date rendering
   */
  public void toResponse(String name, Date[] values, String datePattern) throws ConfigurationException {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, values, datePattern).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named array to content: "+name+"-"+values);
  }

  /**
   * Similar to {@link YSTContext#toResponse(String name, Collection values,
   * String formatPattern)} but for arrays of Date objects.
   *
   * @param name used to name de values. It can not be null
   * @param values Date[]
   * @param datePattern Format string to be used as pattern for the
   *   rendering of the date values. If it is null or the empty string (""),
   *   then the date value will be represented as <code>new Date(x);</code>
   *   where <code>x</code> is the number of milliseconds since January 1,
   *   1970, 00:00:00 GMT represented by the date.
   * @param locale Locale to be used in date rendering. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *   the empty string
   * @throws ConfigurationException if the given <code>datePattern</code> is not
   *         suitable for the date rendering
   */
  public void toResponse(String name, Date[] values, String datePattern, Locale locale) throws ConfigurationException {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, values, datePattern, locale).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named array to content: "+name+"-"+values);
  }


  /**
   * Similar to {@link YSTContext#toResponse(String name, Collection values,
   * String formatPattern)} but for arrays of Calendar objects.
   *
   * @param name used to name de values. It can not be null
   * @param values Calendar[]
   * @param datePattern Format string to be used as pattern for the
   *   rendering of the date values. If it is null or the empty string (""),
   *   then the date value will be represented as <code>new Date(x);</code>
   *   where <code>x</code> is the number of milliseconds since January 1,
   *   1970, 00:00:00 GMT represented by the date.
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *   the empty string
   * @throws ConfigurationException if the given <code>datePattern</code> is not
   *         suitable for the date rendering
   */
  public void toResponse(String name, Calendar[] values, String datePattern) throws ConfigurationException {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, values, datePattern).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named array to content: "+name+"-"+values);
  }


  /**
   * Similar to {@link YSTContext#toResponse(String name, Collection values,
   * String formatPattern)} but for arrays of Calendar objects.
   *
   * @param name used to name de values. It can not be null
   * @param values Calendar[]
   * @param datePattern Format string to be used as pattern for the
   *   rendering of the date values. If it is null or the empty string (""),
   *   then the date value will be represented as <code>new Date(x);</code>
   *   where <code>x</code> is the number of milliseconds since January 1,
   *   1970, 00:00:00 GMT represented by the date.
   * @param locale Locale to be used in date rendering. It can be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *   the empty string
   * @throws ConfigurationException if the given <code>datePattern</code> is not
   *         suitable for the date rendering
   */
  public void toResponse(String name, Calendar[] values, String datePattern, Locale locale) throws ConfigurationException {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(new NamedData(name, values, datePattern,locale).toYSTModel());
    Debug.fine(LOGGER_NAME, "Added named array to content: "+name+"-"+values);
  }




  /**
   * Adds an string that will be inserted directly in the model section of the
   * processed template. A new line character ('\n') will be added at the end
   * of the line.
   *
   * @param data Any text. If it is null, no text will be added
   */
  public void toResponse(String data) {
    modelContent.appendLine(data);
    Debug.fine(LOGGER_NAME, "Added content: "+data);
  }

  /**
   * Returns the <code>HttpServletResponse</code> associated to the servlet
   *
   * @return HttpServletResponse
   */
  public HttpServletResponse getResponse() {
    return response;
  }

  /**
   * Returns the <code>HttpServletRequest</code> associated to the servlet
   *
   * @return HttpServletRequest
   */
  public HttpServletRequest getRequest() {
    return request;
  }

  /**
   * Calls to the <code>setContentType</code> method of the encapsulated
   * <code>HttpServletResponse</code> object. <a
   * href="http://java.sun.com/products/servlet/2.3/javadoc/javax/servlet/ServletResponse.html#setContentType(java.lang.String)">See
   * the <code>setContentType</code> javadoc</a>
   *
   * @param type a String specifying the MIME type of the content
   */
  public void setResponseContentType(String type) {
    this.response.setContentType(type);
  }

  /**
   * Set the name of the template store to be used. That store must contain the
   * selected JTSTemplate that will be used as view in the response. If no
   * template store is specified, the default template store will be used
   * (<code>/yst</code>).
   *
   * @param templateStore String. If it is null, the default "/yst" templates
   *   store will be used
   */
  public void setTemplateStore(String templateStore) {
    this.templateStore = templateStore;
    Debug.fine(LOGGER_NAME, "Templates' store "+templateStore+" is to be used in this request processing");
  }

  /**
   * Returns the name of the selected template store or null if no one has been
   * designated (in this case the default template store -/yst- will be used).
   *
   * @return String
   */
  public String getTemplateStore() {
    return templateStore;
  }

  /**
   * Returns the List of objects that will contribute to make up the actual
   * content of the template model section. This objects can be added with the
   * {@link #toResponse(Object)} and {@link #toResponse(Object[])} methods
   *
   * <p>The new model section that will be inserted in the template will consist
   * of these objects transformation together with the data obtained with
   * {@link #getModelSection}
   *
   * @return java.util.List
   */
  public List getResponseObjects() {
    return responseObjects;
  }

  /**
   * Returns a {@link org.ystsrv.ModelSection} object with the data
   * to be inserted directly in the template. Through this object you can modify
   * directly the actual data that will be inserted in the templates model
   * section. The final model section will consist of the transformation of the
   * objects obtained with {@link #getResponseObjects} together with this data.
   *
   * @return org.ystsrv.ModelSection
   */
  public ModelSection getModelSection() {
    return this.modelContent;
  }

  /**
   * Convenience method that encapsulates a <code>RequestDispatcher</code> that
   * forwards the servlet request to the specified urlPath.
   *
   * @param urlPath String
   * @throws IOException
   * @throws ServletException
   */
  public void forward(String urlPath) throws IOException, ServletException {
    RequestDispatcher disp = this.request.getRequestDispatcher(urlPath);
    disp.forward(this.request, this.response);
  }

  /**
   * Convenience method that encapsulates a <code>RequestDispatcher</code> that
   * forwards the servlet request to the specified urlPath.
   *
   * @param urlPath String
   * @throws IOException
   * @throws ServletException
   */
  public void sendRedirect(String urlPath) throws IOException, ServletException {
    urlPath = YeipeeUtils.addYeipeeToHref(urlPath);
    this.response.sendRedirect(urlPath);
  }


  /**
   * Adds a YST model variable with null value to the Template.
   *
   * @param name used to name de null value. It can not be null
   * @throws IllegalArgumentException If the given <code>name</code> is null or
   *         a string containing 0-n white spaces
   */
  public void toResponseAsNull(String name) {
    if (name == null) throw new IllegalArgumentException("name can not be null");
    this.toResponse(name+"=null;");
    Debug.fine(LOGGER_NAME, "Added named value to content as null");
  }

  /**
   * Convenience method that uses the encapsulated
   * <code>HttpServletRequest</code> object to retrieve request parameter
   * values. Returns the value of a request parameter as a String, or
   * <code>null<code> if the parameter does not exist.
   *
   * <p>You should only use this method when you are sure the parameter has only
   * one value. If the parameter might have more than one value, use {@link
   * #getRequestParameterValues(java.lang.String)}.
   *
   * @param name a String specifying the name of the parameter
   * @return String representing the single value of the parameter
   */
  public String getRequestParameter(String name) {
    return this.request.getParameter(name);
  }

  /**
   * Convenience method that uses the encapsulated
   * <code>HttpServletRequest</code> object to retrieve request parameter
   * values. Returns an array of String objects containing all of the values the
   * given request parameter has, or <code>null<code> if the parameter does not
   * exist.
   *
   * @param name a String specifying the name of the parameter
   * @return an array of String objects containing the parameter's values
   */
  public String[] getRequestParameterValues(String name) {
    return this.request.getParameterValues(name);
  }

  /**
   * Convenience method that uses the encapsulated
   * <code>HttpServletRequest</code> object to retrieve the the current
   * <code>HttpSession</code> associated with this request or, if there is no
   * current session and <code>create</code> is <code>true</code>, create a new
   * session. If <code>create</code> is <code>false</code> and the request has
   * no valid <code>HttpSession</code>, this method returns <code>null</code>.
   *
   * @param create a String specifying the name of the parameter
   * @return the <code>HttpSession</code> associated with this request or
   *   <code>null</code> if <code>create</code> is <code>false</code> and the
   *   request has no valid session
   *
   * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
   */
  public HttpSession getSession(boolean create) {
    return this.request.getSession(create);
  }


  /**
   * Set the <code>avoidBrowserCache</code> flag to <code>true</code> in order
   * to prevent the browser that is receiving the response from caching this
   * response.
   *
   * @param avoidBrowserCache boolean
   */
  public void setAvoidBrowserCache(boolean avoidBrowserCache) {
    this.avoidBrowserCache = avoidBrowserCache;
  }

  /**
   * Returns the state of the <code>avoidBrowserCache</code>
   * @return boolean
   */
  public boolean avoidBrowserCache() {
    return avoidBrowserCache;
  }

}
