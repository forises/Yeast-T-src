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
import java.util.Iterator;

import org.ystsrv.ConfigurationException;
import org.ystsrv.TransformationException;
import org.ystsrv.Transformer;

/**
 * <p>This abstract class provides basic functionality related to the
 * transformation of collections of data. One task that every transformer must
 * accomplish is the recognition of the objects it knows to transform (see the
 * {@link org.ystsrv.Transformer#accept} method). Usually this
 * is done observing the object class. This is valid for objects, but not for
 * collections of objects, since the class of a collection of <code>X</code>
 * class objects and a collection of <code>Y</code> class objects is the same: a
 * <code>Collection</code>. </p>
 *
 * <p><code>CollectionTransformer</code> helps implementing the {@link
 * org.ystsrv.Transformer#accept} method, so that the concrete
 * <code>CollectionTransformer</code> class accepts collections of objects
 * belonging to a certain base class. This class is provided, as usual, implementing
 * the abstract method {@link #transformedClass} (only
 * the first object of the collection is tested). </p>
 *
 * <p>The transformation of the collection is delegated to the {@link
 * #doTransform} method, a convenience transformation method adapted to
 * <code>Collection</code>s.</p>
 *
 * <p>As an example consider a collection of <code>Book</code>
 * objects. Each book has as properties an id, a title, an author, a price and a
 * publisher. A certain Yeast template needs a set of books as part of its
 * model section. The format designed by the HTML designer
 * is:
 * <pre>
 * ids = new Array();
 * titles = new Array();
 * authors = new Array();
 * prices = new Array();
 * publishers = new Array();
 * ids[0]='0';titles[0]='Yeast Tutorial';authors[0]='Peter King';prices[0]=17.46;publishers[0]='Easy book';
 * ids[1]='1';titles[1]='Web design';authors[1]='Jonh Smith';prices[1]=19.95;publishers[1]='Happy O\'Book';
 * </pre>
 *
 * <p>The transformers programmer can develop the following
 * <code>CollectionTransformer</code> transformer that follows the above format:
 * <pre>
 * <b>import</b> java.util.Collection;
 * <b>import</b> java.util.Iterator;
 * <b>import</b> org.ystsrv.transformer.CollectionTransformer;
 * <b>import</b> org.ystsrv.util.TextUtils;
 *
 * <b>public class</b> BookListTransformer <b>extends</b> CollectionTransformer {
 *
 *   <b>public</b> String doTransform(Collection data) {
 *     StringBuffer sb = <b>new</b> StringBuffer("ids = new Array();\n");
 *     sb.append("titles = new Array();\n");
 *     sb.append("authors = new Array();\n");
 *     sb.append("prices = new Array();\n");
 *     sb.append("publishers = new Array();\n");
 *     Iterator iter = data.iterator();
 *     <b>int</b> i = 0;
 *     <b>while</b> (iter.hasNext()) {
 *       Book tit = (Book) iter.next();
 *       sb.append("ids[" + i + "] = '" + tit.getId() + "';titles[" + i + "] = '" +
 *                 TextUtils.escape(tit.getTitle()) + "';authors[" + i + "] = '" +
 *                 TextUtils.escape( (tit.getAuthor().getName() + " " + tit.getAuthor().getSurname())) +
 *                 "';prices[" + i + "] = " + tit.getPrice() + ";publishers[" + i + "] = '" +
 *                 TextUtils.escape(tit.getPublisher()) + "';\n");
 *       i++;
 *     }
 *     <b>return</b> sb.toString();
 *   }
 *
 *    <b>public</b> Class transformedClass() {
 *      <b>return</b> Book.<b>class</b>;
 *    }
 * }
 * </pre>
 *
 * @author Francisco José García Izquierdo
 * @version 1.0
 */
public abstract class CollectionTransformer extends Transformer {

  /**
   * Performs the transformation of the collection calling the {@link
   * #doTransform} method.
   *
   * @param data It MUST be a <code>java.util.Collection</code>
   *   containing objects of the class supplied in the constructor of the
   *   transformer.
   * @return String, result of the transformation
   */
  public String transform(Object data) throws TransformationException  {
    Collection l = (Collection) data;
    return doTransform(l);
  }

  /**
   * Returns <code>true</code> if <code>data</code> is a not null
   * <code>java.util.Collection</code> containing objects of the class supplied
   * in the constructor of the transformer (only the first object of the
   * collection is tested).
   *
   * @param data Object to be transformed
   * @return boolean
   * @throws ConfigurationException if the {@link #transformedClass} of this
   *   object returns null
   */
  public boolean accept(Object data) throws ConfigurationException {
    if (data == null)
      return false;
    if (! (data instanceof Collection))
      return false;
    Collection l = (Collection) data;
    if (l.size() == 0)
      return false;

    Class transformedClass = this.transformedClass();

    if (transformedClass == null)
      throw new ConfigurationException("Transformer class " + this.getClass().getName() +
                                       " can not return null in transformedClass() method");

    Iterator iter = l.iterator();
    return transformedClass.isInstance(iter.next());
  }

  /**
   * Returns the class of the objects that must be contained in the collections
   * that this transformer knows to transform. This class is supplied as a
   * construction parameter of the transformer.
   *
   * @return Class
   */
  abstract public Class transformedClass();

  /**
   * Transforms the <code>data</code> collection of business object into a
   * String whose format correspond with the format of the corresponding objects
   * in the template design model. According to Yeast philosophy, this way of
   * serializing objects is designated by the HTML designer and documented with
   * a test example in the source of the Yeast template. The transformers
   * programmer must follow that format to render the actual values to be
   * inserted in the template.
   *
   * @return String
   * @param data Collection. It can not be null
   */
  public abstract String doTransform(Collection data) throws TransformationException;

}
