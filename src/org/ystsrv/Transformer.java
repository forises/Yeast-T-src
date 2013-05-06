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
package org.ystsrv;

/**
 * Abstract class for classes that will adapt objects of the application
 * business model to the template design model. Using the {@link #transform}
 * method Objects are transformed into a string whose format has been imposed by
 * the HTML designer. Transformed versions of objects will be inserted in the
 * model section of a Yeast template, substituting the test model section designed
 * by the HTML designer.
 *
 * <p>Each descendant class must implement the {@link #transformedClass} and
 * {@link #transform} methods, the former to inform about the class of objects
 * this the transformer is designer for, and the latter to transform a certain
 * object that will be received as a parameter.
 *
 * <p>The transformation process is usually managed by a
 * {@link org.ystsrv.transformer.TransformerGroup}. This object
 * encapsulates a set of <code>YSTTransfomer</code>s. Each time the
 * <code>TransformerGroup</code> has to transform a certain object, it ask
 * its <code>YSTTransfomer</code>s in order to determine which of them knows how
 * to transform that object. For this fact to be determined the
 * <code>TransformerGroup</code> calls the {@link #accept} method of
 * each of its <code>YSTTransfomer</code>. If one <code>YSTTransfomer</code>
 * returns <code>true</code>, that means the <code>Transformer</code> knows
 * how to transform that object. Apart from this automatic process, you can use
 * a <code>YSTTransfomer</code> by its own, calling its {@link #transform}
 * method.
 *
 * <p>The template engine will form a list with the different transformers a
 * template needs. Theoretically a template needs you to develop a specific
 * transformer for each actual business object it uses (there are generic
 * transformers that can help relaxing greately this requirement; see {@link
 * org.ystsrv.transformer.NamedDataTransformer}, {@link
 * org.ystsrv.transformer.BeanTransformer} and {@link
 * org.ystsrv.transformer.BeanCollectionTransformer}).
 *
 * <p>The set of transformers a template needs can be statically configured in
 * the <code>YSTConfig.xml</code> configuration file. In that file a set of
 * configuration parameters (name + value pair) MAY be specified for a certain
 * transformer. The template engine passes these parameters to the transformer
 * constructor in a <code>Map</code>. Therefore, if you are planning to develop
 * a transformer that MAY be configured in the <code>YSTConfig.xml</code> file,
 * you MUST provide the transformer class with a constructor with a sole
 * <code>java.util.Map</code> parameter. That constructor must obtain the
 * construction parameters from the map. If your transformer is not
 * configurable, this requeriment is not relevant.
 *
 * <h3>Example</h3>: consider we have an object of class <code>Person</code>
 * with properties <code>name</code> (<code>String</code>), <code>surname</code>
 * (<code>String</code>), <code>height</code> (<code>float</code>),
 * <code>birth</code> (<code>java.util.Date</code>), <code>married</code>
 * (object of type <code>Person</code>) and <code>childrenNames</code>
 * (<code>java.util.List</code> of <code>Strings</code>). You have to adapt this
 * type of objects to the format imposed in the Yeast template’s design model,
 * which, in this case for <code>Person</code> objects, is the following: <pre>
 *         fullName = 'Jonh Smith';
 *         birth = 'may 27, 1956';
 *         married = true;
 *         numberChildren = 3;
 *  </pre> Therefore, you develop a transformer class like: <blockquote><pre>
 *<b>public class</b> PersonTransformer <b>extends</b> Transformer {
 *  <b>public</b> String transform(Object data) {
 *
 *    Person person = (Person) data;
 *    StringBuffer sb = <b>new</b> StringBuffer();
 *    sb.append("fullName = '" + person.getName() + " " + person.getSurname() + "';\n");
 *    SimpleDateFormat sd = <b>new</b> SimpleDateFormat("MMMM dd, yyyy");
 *    sb.append("birth = '" + sd.format(person.getBirth()) + "';\n");
 *    sb.append("married = " + (person.getMarried()!=null) + ";\n");
 *    sb.append("numberChildren = " + (person.getChildrenNames()!=null?person.getChildrenNames().size():0) + ";\n");
 *    <b>return</b> sb.toString();
 *  }
 *
 *   <b>public</b> Class transformedClass() {
 *    <b>return</b> Person.<b>class</b>;
 *  }
 *}
 *  </pre></blockquote> that applied to <pre>
 *   Person me = <b>new</b> Person("Francisco", "García", 1.80f, new Date(68, 0, 3));
 *   Person myWife = <b>new</b> Person("Gemma", "Garay", 1.72f, new Date(68, 0, 20));
 *   me.setMarried(myWife);
 *   myWife.setMarried(me);
 *   me.setChildrenNames(Arrays.asList(new String[]{"Maria","Maite"}));
 *
 *   PersonTransformer pt = <b>new</b> PersonTransformer();
 *   System.out.println(pt.transform(me));
 *    </pre> will produce <pre>
 *   fullName = 'Francisco García';
 *   birth = 'enero 03, 1968';
 *   married = true;
 *   numberChildren = 2;
 *    </pre>
 *
 * @author Francisco José García Izquierdo
 * @version 1.0
 */
public abstract class Transformer {

  /**
   * In this method you must program the transformation process that will
   * transform the <code>data</code> business object into a String whose format
   * correspond to the format of the respective objects contained in the
   * template design model. According to Yeast philosophy, this way of
   * serializing objects is designated by the HTML designer and documented with
   * a test example in the source of the Yeast template. The transformers
   * programmer must follow that format to render the actual values that will be
   * inserted in the template.
   *
   * @param data Object
   * @return String
   */
  abstract public String transform(Object data) throws TransformationException;

  /**
   * Returns the class of the objects that this transformer knows to transform.
   *
   * @return Class
   */
  abstract public Class transformedClass();

  /**
   * Returns <code>true</code> if the transformer knows how to transform the
   * <code>data</code> object. The implementation of this method is imposed by
   * the use of a list of transformers that will process the set of business
   * objects that an actual request needs. The template engine offers each of
   * these business objects to each one of the transformers in the list, which
   * must transform the object if they know how to do it(see {@link
   * org.ystsrv.transformer.TransformerGroup} for more details).
   *
   * <p>Usually the acceptance of an object depends on the objects class. Each
   * transformer knows how to transform an specific class of objects (see {@link
   * #transformedClass}) The implementation of this method studies if the object
   * received in the <code>data</code> parameter is an instance of the class
   * that this transformer knows to transform (objects of descendant class will
   * also be accepted). This is the default implementation for every transformer
   * but more complicated test can be performed based, for example on object
   * values. In this case you will have to redefine this method.
   *
   * @param data object to transform
   * @return boolean
   * @throws ConfigurationException if the {@link #transformedClass} of this
   *   object returns null
   * @see org.ystsrv.transformer.TransformerGroup
   */
  public boolean accept(Object data) {
    if (data == null)
      return false;

    Class transformedClass = this.transformedClass();

    if (transformedClass == null)
      throw new ConfigurationException("Transformer class " + this.getClass().getName() +
                                       " can not return null in transformedClass() method");

    return transformedClass.isInstance(data);
  }

}
