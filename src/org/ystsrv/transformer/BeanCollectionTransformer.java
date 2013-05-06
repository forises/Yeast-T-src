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

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import org.ystsrv.ConfigurationException;
import org.ystsrv.debug.Debug;

/**
 * This class is an extension of class {@link CollectionTransformer} specialized
 * in the transformation of collections of objects that follows the Java Beans
 * specification.
 *
 * <p>A collection of beans will be transformed into Javascript data structures,
 * usually an array of objects or several arrays of primitive data. Anycase,
 * these structures may need to be initiallized using some JavaScript code.
 * <code>BeanCollectionTransformer</code> objects allow the specification of
 * this initializacion code by means of a String parameter provided in the
 * object construction (<code>header</code> param). This parameter can be null
 * or empty.
 *
 * In order to transform each bean object in the collection it is
 * necessary to provide a <i>format string</i>, which will act as a template for
 * the transformation. In that format string you can insert, enclosed between
 * curly braces (<code>{}</code>), references to the bean properties. To refer
 * to a certain property you must use its name (the capitalization is relevant).
 * When the {@link #doTransform} method is invoked, this transformer will
 * substitute these references with the values of the referred bean properties.
 * Property values are transformed following these same rules specified for the
 * class {@link BeanTransformer}.
 *
 * <p><code>BeanCollectionTransformer</code> will iterate the collection values.
 * If you want to refer to the implicit counter associated to the iteration
 * process, you can use the predefined internal variable <code>#i</code> in the
 * format string (see the example below).
 *
 * <p>If the collection to be transformed is null, then the resultant
 * transformation will be the empty string.
 *
 * <h3>Example</h3>Consider we have a <code>List</code> of objects of class
 * <code>Person</code> with properties <code>name</code> (<code>String</code>),
 * <code>surname</code> (<code>String</code>), <code>height</code>
 * (<code>float</code>), <code>birth</code> (<code>java.util.Date</code>),
 * <code>married</code> (object of type <code>Person</code>) and
 * <code>childrenNames</code> (<code>java.util.List</code> of
 * <code>Strings</code>). <pre>
 * Person me = <b>new</b> Person("Francisco", "García", 1.80f, new Date(68, 0, 3));
 * Person myWife = <b>new</b> Person("Gemma", "Garay", 1.72f, new Date(68, 0, 20));
 * me.setMarried(myWife);
 * myWife.setMarried(me);
 * List children = Arrays.asList(<b>new</b> String[]{"Maria","Maite"});
 * me.setChildrenNames(children);
 * List persons = Arrays.asList(<b>new</b> Person[]{me,myWife});
 * </pre> Imagine now that in the template’s design model <code>Person</code> is
 * called User, and that they have the following attributes: fullName,
 * birth, height, couplesName, and children. You need the objects
 * transformed into a JavaScript array of <code>User</code>s objects named
 * <code>users</code>. You can use the following format and header strings to
 * obtain the desired transformation: <pre>
 *  String header = "users = new Array();";
 *  String format = "users[{#i}] = new  User({name}+' '+{surname}, {birth}, {height}, {married.name}, {childrenNames});";
 *   </pre> When you transform <pre>
 *  BeanCollectionTransformer bct = <b>new</b> BeanCollectionTransformer(Person.class, header, format);
 *  System.out.println(bct.transform(persons));
 *  </pre> you get <pre>
 *  users = new Array();
 *  users[0] = new  User('Francisco'+' '+'García', new Date(-62989200000), 1.8, 'Gemma', ['Maria','Maite']);
 *  users[1] = new  User('Gemma'+' '+'Garay', new Date(-61520400000), 1.72, 'Francisco', null); </pre>
 * Imagine now that the transformation you need is<pre>
 *  user_names = new Array();
 *  user_names[0] = 'Francisco García';
 *  user_names[1] = 'Gemma Garay';
 *
 *  user_births = new Array();
 *  user_births[0] = '03/01/68';
 *  user_births[1] = '20/01/68';</pre>
 * That is, two different arrays, one for the names and another for the
 * birth dates. In this case the value of the header and format strings must be:
 * <pre>
 *  String header = " user_names = new Array();\nuser_births = new Array();";
 *  String format = " user_names[{#i}] = {name}+' '+{surname}; user_births[{#i}] = {birth %dd/MM/yy%};";</pre>
 * With this configurations we get the following transformation:
 * <pre>
 *  user_names = new Array();
 *  user_births = new Array();
 *  user_names[0] = 'Francisco'+' '+'García'; user_births[0] = '03/01/68';
 *  user_names[1] = 'Gemma'+' '+'Garay'; user_births[1] = '20/01/68';</pre>
 * <h3>Configuration in the <code>YSTConfig.xml</code> file</h3>
 * You can configure a Yeast template to use
 * <code>BeanCollectionTransformer</code>. The configuration must specify the
 * values for the construction parameters using the following syntax:
 * <ul>
 *   <li><code>baseClass</code> param: its value must be the class name of the
 * components of the collection to be transformed</li>
 *   <li><code>header</code>: the header string</li>
 *   <li><code>format</code>: the format string</li>
 *   <li><code>datePattern</code>: the default date format that will be applied
 * if no specific pattern is provided</li>
 *   <li><code>localeLang</code>: the language code to be used as locale for
 * date rendering, which will either be the empty string or a lowercase ISO 639
 * code.</li>
 *   <li><code>localeCountry</code>: the country/region code used to complement
 * the locale defined with the previos parameter, which will either be the empty
 * string or an upercase ISO 3166 2-letter code.</li>
 * </ul>
 * For example, the following configuration excerpt is taken from a
 * YSTConfig.xml file where a <code>BeanCollectionTransformer</code> is
 * configured to be used with objects belonging to the class
 * <code>ystsrv.demo.Customer</code>. The header string is <code>users = new
 * Array();</code>, and the format string is <code>users[{#i}] = new User({name}
 * +' '+{surname}, {birth}, {height}, {married.name}, {childrenNames}.length,
 * {married.birth %M-d, yyyy%});</code>. And finally, a default pattern
 * <code>dd-MM-yyyy</code> is provided. <pre>
 * &lt;template id="custList"&gt;
 *   &lt;location&gt;Customers.html&lt;/location&gt;
 *     &lt;transformer class="org.ystsrv.transformer.BeanCollectionTransformer"&gt;
 *       &lt;param name="baseClass"&gt;ystsrv.demo.Customer&lt;/param&gt;
 *       &lt;param name="header"&gt;users = new Array();&lt;/param&gt;
 *       &lt;param name="format"&gt;users[{#i}] = new User({name}+' '+{surname}, {birth}, {height}, {married.name %M-d, yyyy%}, {childrenNames}.length, {married.birth});&lt;/param&gt;
 *       &lt;param name="datePattern"&gt;dd-MM-yyyy&lt;/param&gt;
 *     &lt;/transformer&gt;
 * &lt;/template&gt;
 * </pre>
 *
 * @author Francisco José García Izquierdo
 * @version 2.0
 */
public class BeanCollectionTransformer extends CollectionTransformer {

  private static final String LOGGER_NAME = "ystsrv.transformer";

  private String format;
  private String header;
  private Class baseClass;
  private BeanFormatter formatter;

  /**
   * Creates a <code>BeanCollectionTransformer</code> object for objects of a
   * given class <code>baseClass</code>, a given <code>format</code> and
   * <code>header</code> strings, and with
   * a default date pattern, that will be applied if no specific pattern is
   * provided for a certain temporal property specification in the format string.
   * Date pattern specification must follow the rules described for the
   * <code>java.text.SimpleDateFormat</code> class (see <a
   * href="http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html">
   * http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html</a>)
   *
   * @param baseClass Class of the objects that must be stored in the
   *   collection that will be transformed by this transformer. It can not be
   *   null.
   * @param header String specifying the text that precedes the transformation
   *   of the collection of objects
   * @param format String specifying the format to be applied in the
   *   transformation. It can not be null nor empty.
   * @param defaultDatePattern default date pattern for date rendering. It will
   *   be applied for the rendering of a certain bean property if no specific
   *   pattern is supplied.
   * @throws ConfigurationException if the given <code>baseClass</code> is
   *   null; or if the given <code>format</code> is null or the empty string or
   *   it is malformed (Curly brace mismatches); if the given
   *   <code>defaultDatePattern</code> is not suitable for the date rendering;
   *   problems getting the introspection info of the class
   *   <code>baseClass</code>, or any of its properties.
   */
  public BeanCollectionTransformer(Class baseClass, String header, String format,
                                   String defaultDatePattern) throws ConfigurationException {
    this(baseClass, header, format, defaultDatePattern, null);
  }

  /**
   * Creates a <code>BeanCollectionTransformer</code> object for objects of a
   * given class <code>baseClass</code>, a given <code>format</code> and
   * <code>header</code> strings, and with
   * a default date pattern, that will be applied if no specific pattern is
   * provided for a certain temporal property specification in the format string.
   * Date pattern specification must follow the rules described for the
   * <code>java.text.SimpleDateFormat</code> class (see <a
   * href="http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html">
   * http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html</a>)
   *
   * @param baseClass Class of the objects that must be stored in the
   *   collection that will be transformed by this transformer. It can not be
   *   null.
   * @param header String specifying the text that precedes the transformation
   *   of the collection of objects
   * @param format String specifying the format to be applied in the
   *   transformation. It can not be null nor empty.
   * @param defaultDatePattern default date pattern for date rendering. It will
   *   be applied for the rendering of a certain bean property if no specific
   *   pattern is supplied.
   * @param locale java.util.Locale. It can be null
   * @throws ConfigurationException if the given <code>baseClass</code> is
   *   null; or if the given <code>format</code> is null or the empty string or
   *   it is malformed (Curly brace mismatches); if the given
   *   <code>defaultDatePattern</code> is not suitable for the date rendering;
   *   problems getting the introspection info of the class
   *   <code>baseClass</code>, or any of its properties.
   */
  public BeanCollectionTransformer(Class baseClass, String header, String format,
                                   String defaultDatePattern, Locale locale) throws ConfigurationException {
    if (format == null || format.trim().length() == 0)
      throw new ConfigurationException("Impossible to build BeanCollectionTransformer " +
                                       "without format. Check the ystsrv configuration file. " +
                                       "Param format required");
    if (baseClass == null)
      throw new ConfigurationException(
          "Impossible to build BeanTransformer without specifing transformerd class.");

    this.header = header;
    this.format = format;
    this.baseClass = baseClass;
    this.formatter = BeanFormatterCache.getFormater(format, defaultDatePattern, locale, this.baseClass);
  }

  /**
   * Creates a <code>BeanCollectionTransformer</code> object for objects of a
   * given class <code>baseClass</code> and with a given <code>format</code> and
   * <code>header</code> strings.
   *
   * @param header String specifying the text that precedes the transformation
   *   of the collection of objects
   * @param format String specifying the format to be applied in the
   *   transformation. It can not be null nor empty.
   * @param baseClass Class of the objects that must be stored in the
   *   collection that will be transformed by this transformer. It can not be
   *   null.
   * @throws ConfigurationException if the given <code>baseClass</code> is null;
   *   or if the given <code>format</code> is null or the empty string or it is
   *   malformed (Curly brace mismatches)
   * @throws ConfigurationException Problems getting the introspection info of
   *   the class <code>baseClass</code>, or any of its properties
   */
  public BeanCollectionTransformer(Class baseClass, String header, String format) throws ConfigurationException {
    this(baseClass, header, format, null);
  }

  /**
   * Creates a <code>BeanCollectionTransformer</code>. Gets the required
   * constructor parameters <code>baseClass</code> and <code>format</code> from
   * the <code>params</code> map. The <code>header</code> parameter and
   * the default date pattern parameter (named <code>datePattern</code>) are
   * optional.
   *
   * @param params Map with parameters names and values. It MUST contain
   *   <code>baseClass</code> and <code>format</code> parameters, and their
   *   respective values; <code>header</code> and <code>datePattern</code>
   *   parameters are optional.
   * @throws ConfigurationException if the given <code>params</code> map does
   *   not contain an entry for the base class (entry of key
   *   <code>baseClass</code>), or this entry is null or the empty String; if
   *   the class specified in the entry <code>baseClass</code> can not be
   *   loaded; if the given <code>params</code> map does not contain an entry
   *   for the format string of this object (entry of key <code>format</code>),
   *   or this entry is null or the empty String or it has curly braces
   *   mismatches; if the entry of key <code>datePattern</code> is not suitable
   *   for the date rendering;
   * @throws ConfigurationException Problems getting the introspection info of
   *   the class specified in the entry <code>baseClass</code> of the given
   *   <code>params</code> map, or any of that class properties
   */
  public BeanCollectionTransformer(Map params) throws ConfigurationException {
    this.header = (String)params.get("header");
    this.format = (String) params.get("format");
    if (format == null || format.trim().length() == 0)
      throw new ConfigurationException("Impossible to build BeanCollectionTransformer " +
                                       "without format. Check the ystsrv configuration file." +
                                       " Param format required");
    String className = (String) params.get("baseClass");
    if (className == null || className.trim().length() == 0)
      throw new ConfigurationException("Impossible to build BeanCollectionTransformer " +
                                       "without specifing base class. Check the ystsrv " +
                                       "configuration file. Param baseClass required");
    try {
      this.baseClass = Class.forName(className);
    } catch (ClassNotFoundException ex) {
      throw new ConfigurationException(
          "Impossible to build BeanCollectionTransformer for base class " +
          className + ". Class " + className + " not found.",ex);
    }

    String defaultDatePattern = (String)params.get("datePattern");

    Locale locale = null;
    String localeLang = (String)params.get("localeLang");
    if (localeLang != null && localeLang.trim().length() != 0) {
      String localeCountry =  (String)params.get("localeCountry");
      if (localeCountry != null && localeCountry.trim().length() != 0)
        locale = new Locale(localeLang, localeCountry);
      else
        locale = new Locale(localeLang);
    }

    this.formatter = BeanFormatterCache.getFormater(format, defaultDatePattern, locale, this.baseClass);
  }


  /**
   * Returns the class of the bean objects that must be contained in the
   * collections that this transformer knows to transform. This class is
   * supplied as a construction parameter of the BeanCollectionTransformer.
   *
   * @return Class
   */
  public Class transformedClass() {
    return baseClass;
  }

  /**
   * Performs the transformation of the collection calling the {@link
   * #doTransform} method.
   *
   * @param data It MUST be a <code>java.util.Collection</code>
   *   containing objects of the class supplied in the constructor of the
   *   transformer.
   * @return String, result of the transformation
   */
  public String transform(Object data)  {
    Collection l = (Collection) data;
    return doTransform(l);
  }

  /**
   * Transforms the <code>data</code> object collection according to the format
   * supplied in the constructor of this object, and then returns a string that
   * can be inserted in the model section of the Yeast template.
   *
   * @param data Collection
   * @return String
   * @throws IllegalArgumentException if received data does not correspond to
   *           the class especified in this transformer construction
   * @throws ConfigurationException If the format string contains the
   *   specification of non-existing bean properties, or properties
   *   specification that end with dot (e.g. <code>{client.}</code>); if there
   *   is any problem accessing the bean properties (e.g. no public get method);
   *   if the invocation of the get method of any property throws any exception
   */
  public String doTransform(Collection data) throws ConfigurationException {
    if (data == null) {
      Debug.warning(LOGGER_NAME, "TransformerGroup.transform: skiping null data to transform");
      return "// Null data skipped";
    }
    if (data.size() == 0)
      return (header != null ? header : "");
    else
      return this.formatter.format(header, data);
  }
}
