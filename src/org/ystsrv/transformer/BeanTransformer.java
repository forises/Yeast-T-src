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

import java.util.Locale;
import java.util.Map;

import org.ystsrv.ConfigurationException;
import org.ystsrv.Transformer;
import org.ystsrv.debug.Debug;

/**
 * Extension of {@link org.ystsrv.Transformer} class that is able to
 * transform any object that follows the Java Beans specification. To transform
 * a bean object it is necessary to provide a <i>format string</i>, which will
 * act as a template for the transformation. In that format string you can
 * insert, enclosed between curly braces (<code>{}</code>), references to the
 * bean properties. To refer to a certain property you must use its name (the
 * capitalization is relevant). When the {@link #transform} method is invoked,
 * this transformer will substitute those references with the values of the
 * referred bean properties.
 *
 * <p>If a certain bean property is a <code>java.util.Date</code> or
 * <code>java.util.Calendar</code> object then you can specify a pattern for the
 * date to be represented. This pattern is specified next to the date property
 * name, enclosed between <code>%</code> symbols (e.g.,
 * <code>{birth %dd-MM-yyyy%}</code>). A default date pattern can be
 * provided during the <code>BeanTransformer</code> construction. If no pattern
 * is supplied (either default or specific), the transformed value will be
 * <code>new Date(x)</code>, that is, the JavaScript instruction that creates a
 * <code>Date</code> object (being <code>x</code> the number of milliseconds
 * since January 1, 1970, 00:00:00 GMT represented by the property’s value). If
 * a pattern is provided, the transformation will follow that pattern (the
 * result will be enclosed between simple quotes)</li>. Pattern specifications
 * must follow the rules described for the
 * <code>java.text.SimpleDateFormat</code> class (see
 * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html">
 * http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html</a>).
 * The empty string pattern (%%) is equivalent to no pattern.
 * Date patterns can also be applied to bean properties that are an array or a
 * collection of <code>java.util.Date</code> or <code>java.util.Calendar</code>
 * objects.
 *
 * <p>If the property is at the same time a bean, you have two choices: (1)
 * you can refer to the properties of this last bean using the usual dot
 * notation; or (2) you can also specify a format string for the whole bean,
 * following the same rules we are specifing. This format string is specified
 * next to the bean property name, enclosed between <code>%</code> symbols. If
 * you do not specify a format string for a property that is a bean, then the
 * <code>toString</code> version of the bean will be used. If the bean property
 * is an array or collection of other beans you can also specify a format string
 * for every array members (all of them will be transformed in same way).
 *
 * <p>If the bean property is a Map, then the property value will be rendered
 * using the JavaScript object notation. The Map keys, or more precisely the
 * string version of the Map keys, will be considered as the names the
 * properties of a JavaScript object (note that this work well if the Map keys
 * are Strings). The Map values will be the values of the JavaScript object
 * properties. Each Map value will be rendered according to its type, following
 * the rules we are stating here. You can specify a unique sub-format string for
 * a map property. This sub-format may represent a date pattern, to be applied
 * to temporal map values, or a format string for objects to be applied to the
 * object map values. Only one sub-format string can be specified, so if the Map
 * contains two objects of different classes, the sub-format will be suitable
 * only for one of them, and an error will be reported when it is applied to the
 * other.
 *
 * <p>Property values of other types are transformed following these rules:
 * <ul>
 * <li>Property values of primitive data types, except from <code>char</code>,
 * are copied without modification. </li>
 * <li><code>String</code> and <code>char</code> values are represented between
 * simple quotes. </li>
 * <li>If the property value is an array or a <code>java.util.Collection</code>
 * object, the transformed value will be <code>[x,y,...]</code>, that
 * is, the JavaScript instruction that creates an array (being <code>x</code>,
 * <code>y</code>, the values of the collection components, each one represented
 * according to its type, following these rules that we are specifying). </li>
 * </ul>
 *
 * <p>By default, if the bean to be transformed is null, then the resultant
 * transformationv will be the empty string.
 *
 * <h3>Examples</h3>Consider we have an object of class <code>Person</code>
 * with properties <code>name</code> (<code>String</code>), <code>surname</code>
 * (<code>String</code>), <code>height</code> (<code>float</code>),
 * <code>birth</code> (<code>java.util.Date</code>), <code>married</code>
 * (object of type <code>Person</code>) and <code>childrenNames</code>
 * (<code>java.util.List</code> of <code>Strings</code>). <pre>
 *  Person me = <b>new</b> Person("Francisco", "García", 1.80f, new Date(68, 0, 3));
 *  Person myWife = <b>new</b> Person("Gemma", "Garay", 1.72f, new Date(68, 0, 20));
 *  me.setMarried(myWife);
 *  List children = Arrays.asList(new String[]{"Maria","Maite"});
 *  me.setChildrenNames(children);
 * </pre> Imagine now that in the template’s design model <code>Person</code>
 * objects are called User, and that they have the following attributes:
 * fullName, birth, height, couplesName, and numberOfChildren. You can use
 * the following string format to obtain the desired transformation:
 * <pre>
 *  String format = "p = new User({name}+' '+{surname}, {birth}, {height}, {married.name}, {childrenNames}.length);";
 *  // or "p = new User({name}+' '+{surname}, {birth}, {height}, {married %{name}%}, {childrenNames}.length);";
 *   </pre> When you transform <pre>
 *  BeanTransformer bt = <b>new</b> BeanTransformer(Person.<b>class</b>, format);
 *  System.out.println(bt.transform(me));
 *   </pre> you get <pre>
 *  p = new User('Francisco'+' '+'García', new Date(-62989200000), 1.8, 'Gemma', ['Maria','Maite'].length);
 *   </pre>
 * You can specify an specific date format for the birthday property:<pre>
 *  String format = "p = new User({name}+' '+{surname}, {birth %dd-MM-yyyy%}, {height}, {married.name}, {childrenNames}.length);";
 *  BeanTransformer bt = <b>new</b> BeanTransformer(Person.<b>class</b>, format);
 *  System.out.println(bt.transform(me));
 *   </pre> you get <pre>
 *  p = new User('Francisco'+' '+'García', '03-01-1968', 1.8, 'Gemma', ['Maria','Maite'].length);
 *   </pre>
 * You can also specify a different string format for the married property,
 * which is a bean:<pre>
 *  String format = "p = new User({name}+' '+{surname}, {birth %dd-MM-yyyy%}, " +
 *                  "{height}, {married %new User({name},{surname},{birth})%}, {childrenNames}.length);";
 *  BeanTransformer bt = <b>new</b> BeanTransformer(Person.<b>class</b>, format);
 *  System.out.println(bt.transform(me));
 *   </pre> you get <pre>
 *  p = new User('Francisco'+' '+'García', '03-01-1968', 1.8, new User('Gemma','Garay',new Date(-61520400000)), ['Maria','Maite'].length);
 *   </pre>
 *
 * You can use a default pattern for date transformation:<pre>
 *  BeanTransformer bt = <b>new</b> BeanTransformer(Person.<b>class</b>, format, "M d, yyyy");
 *  System.out.println(bt.transform(me));
 *   </pre> you get <pre>
 *  p = new User('Francisco'+' '+'García', '03-01-1968', 1.8, new User('Gemma','Garay','1 20, 1968'), ['Maria','Maite'].length);
 *   </pre>
 *
 * If the bean is <code>null</code>, the transformation is the empty string:<pre>
 *  me = <b>null</b>;
 *  BeanTransformer bt = <b>new</b> BeanTransformer(Person.<b>class</b>, format);
 *  System.out.println(bt.transform(me));
 *   </pre> you get <pre>
 *  // Null data skipped
 *   </pre>
 *
 *<h3>Configuration in the <code>YSTConfig.xml</code> file</h3>
 * You can configure a Yeast template to use <code>BeanTransformer</code>. The
 * configuration must specify the  values for the construction parameters using
 * the following syntax:
 * <ul>
 *   <li><code>baseClass</code> param: its value must be the class name of the
 * objects to be transformed</li>
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
 * YSTConfig.xml file where a <code>BeanTransformer</code> is configured to be
 * used with objects belonging to the class <code>ystsrv.demo.Customer</code>.
 * The format string is <code>user = new User({name}+' '+{surname}, {birth},
 * {height}, {married.name}, {childrenNames}.length, {married.birth %M-d,
 * yyyy%});</code>. And finally, a default pattern <code>dd-MM-yyyy</code> is
 * provided. <pre>
 * &lt;template id="confirm"&gt;
 *   &lt;location&gt;custConfirm.html&lt;/location&gt;
 *     &lt;transformer class="org.ystsrv.transformer.BeanTransformer"&gt;
 *       &lt;param name="baseClass"&gt;ystsrv.demo.Customer&lt;/param&gt;
 *       &lt;param name="format"&gt;user = new User({name}+' '+{surname}, {birth}, {height}, {married.name %M-d, yyyy%}, {childrenNames}.length, {married.birth});&lt;/param&gt;
 *       &lt;param name="datePattern"&gt;dd-MM-yyyy&lt;/param&gt;
 *     &lt;/transformer&gt;
 * &lt;/template&gt;
 * </pre>
 *
 * @author Francisco José García Izquierdo
 * @version 2.1
 */
public class BeanTransformer extends Transformer {

  private static final String LOGGER_NAME = "ystsrv.transformer";

  private String format;
  private Class baseClass;
  private BeanFormatter formatter;

  /**
   * Creates a <code>BeanTransformer</code> object for objects of a given class
   * <code>baseClass</code> and with a given <code>format</code> string.
   *
   * @param format String specifying the format to be applied in the
   *   transformation. It can not be null nor empty.
   * @param baseClass Class of the objects that will be transformed by this
   *   transformer. It can not be null.
   * @throws ConfigurationException if the given <code>baseClass</code> is null;
   *   or if the given <code>format</code> is null or the empty string or it is
   *   malformed (Curly brace mismatches); if there are problems getting the
   *   introspection info of the class <code>baseClass</code>, or any of its
   *   properties
   */
  public BeanTransformer(Class baseClass, String format) throws ConfigurationException {
    this(baseClass, format, null);
  }

  /**
   * Creates a <code>BeanTransformer</code> object for objects of a given class
   * <code>baseClass</code>, a given <code>format</code> string, and with
   * a default date pattern, that will be applied if no specific pattern is
   * provided for a certain temporal property specification in the format string.
   * Date pattern specification must follow the rules described for the
   * <code>java.text.SimpleDateFormat</code> class (see <a
   * href="http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html">
   * http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html</a>)
   *
   * @param baseClass Class of the objects that will be transformed by this
   *   transformer. It can not be null.
   * @param format String specifying the format to be applied in the
   *   transformation. It can not be null nor empty.
   * @param defaultDatePattern default date pattern for date rendering. It will
   *   be applied for the rendering of a date bean property if no specific
   *   pattern is supplied. If it is null or the empty string (""),
   *   then the date value will be represented as <code>new Date(x);</code>
   *   where <code>x</code> is the number of milliseconds since January 1, 1970,
   *   00:00:00 GMT represented by the date.
   * @throws ConfigurationException if the given <code>baseClass</code> is null;
   *   or if the given <code>format</code> is null or the empty string or it is
   *   malformed (Curly brace mismatches); if there are problems getting the
   *   introspection info of the class <code>baseClass</code>, or any of its
   *   properties
   */
  public BeanTransformer(Class baseClass, String format, String defaultDatePattern) throws ConfigurationException {
    this(baseClass, format, defaultDatePattern, null);
  }

  /**
   * Creates a <code>BeanTransformer</code> object for objects of a given class
   * <code>baseClass</code>, a given <code>format</code> string, and with
   * a default date pattern, that will be applied if no specific pattern is
   * provided for a certain temporal property specification in the format string.
   * Date pattern specification must follow the rules described for the
   * <code>java.text.SimpleDateFormat</code> class (see <a
   * href="http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html">
   * http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html</a>)
   *
   * @param baseClass Class of the objects that will be transformed by this
   *   transformer. It can not be null.
   * @param format String specifying the format to be applied in the
   *   transformation. It can not be null nor empty.
   * @param defaultDatePattern default date pattern for date rendering. It will
   *   be applied for the rendering of a date bean property if no specific
   *   pattern is supplied. If it is null or the empty string (""),
   *   then the date value will be represented as <code>new Date(x);</code>
   *   where <code>x</code> is the number of milliseconds since January 1, 1970,
   *   00:00:00 GMT represented by the date.
   * @param locale java.util.Locale. It can be null
   * @throws ConfigurationException if the given <code>baseClass</code> is null;
   *   or if the given <code>format</code> is null or the empty string or it is
   *   malformed (Curly brace mismatches); if there are problems getting the
   *   introspection info of the class <code>baseClass</code>, or any of its
   *   properties
   */
  public BeanTransformer(Class baseClass, String format, String defaultDatePattern, Locale locale) throws ConfigurationException {
    if (format == null || format.trim().length() == 0)
      throw new ConfigurationException("Impossible to build BeanTransformer without format. " +
                                       "Check the ystsrv configuration file. Param format required");
    if (baseClass == null)
      throw new ConfigurationException(
          "Impossible to build BeanTransformer without specifing transformerd class.");

    this.format = format;
    this.baseClass = baseClass;
    this.formatter = BeanFormatterCache.getFormater(format, defaultDatePattern, locale, this.baseClass);
  }


  /**
   * Creates a <code>BeanTransformer</code>. Gets the required constructor
   * parameters <code>baseClass</code> and <code>format</code> from the
   * <code>params</code> map. The default date pattern parameter
   * (named <code>datePattern</code>) is optional.
   *
   * @param params Map with parameters names and values. It must contain
   *   <code>baseClass</code> and <code>format</code> parameters, and their
   *   respective values; <code>datePattern</code> parameter is optional.
   * @throws ConfigurationException if the given <code>params</code> map does
   *   not contain an entry for the base class (entry of key
   *   <code>baseClass</code>), or the entry is null or the empty String; if the
   *   class specified in the entry <code>baseClass</code> can not be loaded; if
   *   the given <code>params</code> map does not contain an entry for the format
   *   string of this object (entry of key <code>format</code>), or the entry is
   *   null or the empty String or it has curly braces mismatches; if there are
   *   problems getting the introspection info of the class specified in the
   *   entry <code>baseClass</code> of the given <code>params</code> map, or any
   *   of that class properties
   */
  public BeanTransformer(Map params) throws ConfigurationException {
    this.format = (String) params.get("format");
    if (format == null || format.trim().length() == 0)
      throw new ConfigurationException("Impossible to build BeanTransformer without " +
                                       "format. Check the ystsrv configuration file. " +
                                       "Param format required");
    String className = (String) params.get("baseClass");
    if (className == null || className.trim().length() == 0)
      throw new ConfigurationException("Impossible to build BeanTransformer without " +
                                       "specifing base class. Check the ystsrv configuration " +
                                       "file. Param baseClass required");
    try {
      this.baseClass = Class.forName(className);
    } catch (ClassNotFoundException ex) {
      throw new ConfigurationException("Impossible to build BeanTransformer for class " +
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
   * Transforms the <code>data</code> object according to the format supplied in
   * the constructor of this object, and then returns a string that can be
   * inserted in the model section of the Yeast template.
   *
   * @param data Object
   * @return String
   * @throws IllegalArgumentException if received data does not correspond to
   *           the class especified in this transformer construction
   * @throws ConfigurationException If the format string contains the
   *   specification of non-existing bean properties, or properties
   *   specification that end with dot (e.g. <code>{client.}</code>); if there
   *   is any problem accessing the bean properties (e.g. no public get method);
   *   if the invocation of the get method of any property throws any exception
   */
  public String transform(Object data) throws ConfigurationException {
    if (data == null) {
      Debug.warning(LOGGER_NAME, "TransformerGroup.transform: skiping null data to transform");
      return "// Null data skipped";
    }
    return this.formatter.format(data);
  }

  /**
   * {@inheritDoc}
   */
  public Class transformedClass() {
    return this.baseClass;
  }
}
