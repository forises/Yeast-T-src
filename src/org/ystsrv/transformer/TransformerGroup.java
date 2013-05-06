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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ystsrv.ConfigurationException;
import org.ystsrv.ModelSection;
import org.ystsrv.TransformationException;
import org.ystsrv.Transformer;
import org.ystsrv.debug.Debug;

/**
 * When a Yeast template is build, it will be associated to a
 * <code>TransformerGroup</code>. This <code>TransformerGroup</code>
 * will be responsible for transforming the business objects that the
 * application controller pushes onto the template in order to make up the
 * actual response for a client. That is, this class builds the actual model
 * section that will be inserted in a certain Yeast template substituting the test
 * model section designed by the HTML designer. </p>
 *
 * <p>This class encapsulates a set of {@link org.ystsrv.Transformer}
 * objects, one for each one of the business objects needed to build the actual
 * model section, with the only exception of the {@link
 * org.ystsrv.transformer.NamedData} objects, that are transformed by a
 * single {@link NamedDataTransformer}, included by default in every
 * <code>TransformerGroup</code>. Notice that it is necessary one
 * <code>Transformer</code> for each one of the business object, not for each
 * class of business objects. That is, if the template needs two objects of
 * class <code>X</code>, you have to include in the
 * <code>TransformerGroup</code> two transformers for objects of class
 * <code>X</code>. </p>
 *
 * <p>To perform the transformation you have to use the {@link
 * #transform(java.util.Collection)} or the {@link
 * #transform(java.lang.Object[])} methods. These methods will offer each of the
 * objects received as parameter to each one of the <code>Transformer</code>s
 * it contains. The first of the <code>Transformer</code>s that knows how to
 * transform the object will transform it, appending the result of this
 * individual transformation to the global result (an object of class {@link
 * org.ystsrv.ModelSection}). </p>
 *
 * <p>There are several ways of making up a <code>TransformerGroup</code>,
 * but regardless the one you use you have to be careful with the order of the
 * <code>Transformer</code>s you include, because this order is used in the
 * overall transformation process. Objects are offered to
 * <code>Transformer</code>s taking into account the order in what they are
 * included in the group. Due to the polymorphism, an object (e.g. of class
 * <code>B</code>) can be offered to, and accepted by, a transformer designed
 * for one superclass of <code>B</code>, instead of by a specific transformer
 * for <code>B</code> objects. That is why the <code>Transformer</code>s MUST
 * be included in the group in a order consistent with the class hierarchy (like
 * catch blocks for exceptions), i.e. in first place the transformers for more
 * specific classes, and later the ones for more generic classes. E.g.: lets
 * suppose we have two transformers, <code>tA</code> y <code>tB</code>, for
 * classes <code>A</code> and <code>B</code>; lets suppose <code>B</code>
 * extends <code>A</code>; then <code>tB</code> must be added to the
 * <code>TransformerGroup</code> before <code>tA</code>. </p>
 *
 * @author Francisco José García Izquierdo
 * @version 1.0
 */
public class TransformerGroup {

  private static final String LOGGER_NAME = "ystsrv.manager";

  private static final String LOGGER_NAME2 = "ystsrv.transformer";

  private List transformers;

  /**
   * Builds a <code>TransformerGroup</code> containig only the {@link
   * NamedDataTransformer}.
   *
   * @see NamedDataTransformer
   */
  public TransformerGroup() {
    Debug.fine(LOGGER_NAME, "Creating TransformerGroup with default transformers");
    transformers = new ArrayList();
    transformers.add(new NamedDataTransformer());
  }

  /**
   * You should not use this constructor. It is designed to be used by the
   * Yeast-Server infraestructure.
   * <p>Builds a <code>TransformerGroup</code> made up of the {@link
   * NamedDataTransformer} and the ones specified in the
   * <code>transformers</code> list. This list contains
   * {@link TransformerSpec} objects, each one specifying one of the
   * transformers to include in the group. The order in which the transformers
   * are added to the group it is the order in which they are specified in the
   * list. .
   *
   * @see NamedDataTransformer
   * @param transformerSpecs list of {@link TransformerSpec} objects, each
   *   one specifying one of the transformers to include in the group. It can
   *   not be null
   * @throws IllegalArgumentException If the given <code>transformerSpecs</code>
   *   list is null
   * @throws ConfigurationException if the order of the transformers in the
   *   <code>transformerSpecs</code> parameter does not follow the building
   *   rules described in this class header javadoc (in first place transformers
   *   for derived classes, later transformers for base classes (see this class
   *   header javadoc for more information).
   * @throws ConfigurationException if any of the class names contained in the
   *   list of transformer specifications, can not be loaded or instanciated
   *   (e.g. the transformer class has not got a no params constructor or a
   *   constructor with a <code>java.util.Map</code> param, as described in the
   *   header javadoc of class {@link org.ystsrv.Transformer})
   */
  public TransformerGroup(List transformerSpecs) throws ConfigurationException {
    Debug.fine(LOGGER_NAME, "Creating TransformerGroup with transformers "+transformerSpecs);
    this.transformers = new ArrayList();
    this.transformers.add(new NamedDataTransformer());
    addTransformers(transformerSpecs);
  }

  /**
   * Adds an instance of the transformer of class <code>transformerClass</code>
   * to this group of transformers
   *
   * @param transformerClass Transformer class name It can not be null
   * @throws IllegalArgumentException If the given <code>transformerClass</code>
   *   is null or the empty string
   * @throws ConfigurationException if this object already holds a transfomer
   *   for any superclass of the class that the added transformer (of class
   *   <code>transformerClass</code>) knows to transform.
   * @throws ConfigurationException if the given <code>transformerClass</code>
   *   name can not be loaded or instanciated (e.g. the transformer class has
   *   not got a no params constructor or a constructor with a
   *   <code>java.util.Map</code> param, as described in the header javadoc of
   *   class {@link org.ystsrv.Transformer})
   */
  public void add(String transformerClass) throws ConfigurationException {
    if (transformerClass == null || transformerClass.trim().length() == 0)
      throw new IllegalArgumentException("transformerClass name can not be null nor empty");
    this.addTransformer(new TransformerSpec(transformerClass));
  }

  /**
   * Adds an instance of the transformer specified in <code>transformer</code>
   * to this group of transformers
   *
   * @param transformer {@link TransformerSpec} of the transformer to be
   *   included, specifying the transformer class and the required construction
   *   parameters. It can not be null
   * @throws IllegalArgumentException If the given <code>transformerSpec</code>
   *   list is null
   * @throws ConfigurationException if this object already holds a transfomer
   *   for any superclass of the class that the specified transformer knows to
   *   transform (see this class header javadoc for more information).
   * @throws ConfigurationException if the class name contained in the
   *   transformer specification, can not be loaded or instanciated
   *   (e.g. the transformer class has not got a no params constructor or a
   *   constructor with a <code>java.util.Map</code> param, as described in the
   *   header javadoc of class {@link org.ystsrv.Transformer})
   */
  private void addTransformer(TransformerSpec transformer) throws ConfigurationException {
    if (transformer == null)
      throw new IllegalArgumentException("Transformer specification can not be null");

    String className = null;
    try {
      className = transformer.getClassName();
      Map params = transformer.getParams();
      Class tClass = Class.forName(className);
      Transformer t;
      if (params != null) {
        Constructor c = tClass.getConstructor(new Class[] {java.util.Map.class});
        if (c != null) {
          t = (Transformer)c.newInstance(new Object[] {params});
        } else {
          t = (Transformer)tClass.newInstance();
          Debug.warning(LOGGER_NAME, "Transformer " + className + " does not accept params in its " +
                        "constructor. Params present in YSTConfig.xml are discarded.");
        }
      } else {
        t = (Transformer)tClass.newInstance();
      }
      add(t);
    } catch (ClassNotFoundException cnfe) {
      throw new ConfigurationException("Impossible to add transformer to group [" + cnfe + "]", cnfe);
    } catch (IllegalAccessException iae) {
      throw new ConfigurationException("Impossible to add transformer to group [" + iae + "]", iae);
    } catch (InstantiationException ie) {
      throw new ConfigurationException("Impossible to add transformer to group [" + ie + "]", ie);
    } catch (NoSuchMethodException ex) {
      throw new ConfigurationException("Impossible to add transformer to group [" + ex + "]", ex);
    } catch (InvocationTargetException ex1) {
      throw new ConfigurationException("Impossible to add transformer to group [" + ex1.getCause() + "]", ex1.getCause());
    }
  }

  /**
   * Adds the <code>transformer</code> to this group of transformers
   *
   * @param transformer {@link org.ystsrv.Transformer} to add. It
   *   can not be null
   * @throws IllegalArgumentException If the given <code>transformer</code>
   *   is null
   * @throws ConfigurationException if this object already holds a transfomer
   *   for any superclass of the class that the added <code>transformer</code>
   *   knows to transform (see this class header javadoc for more information).
   */
  public void add(Transformer transformer) throws ConfigurationException {
    if (transformer == null)
      throw new IllegalArgumentException("Transformer can not be null");

    // Check that in the group there is already no transformer for a superclass of transformer
    checkConfigurationOrder(transformer);

    Debug.fine(LOGGER_NAME, "Adding transformer to group: "+transformer);

    Object namedT = transformers.get(transformers.size()-1);
    transformers.set(transformers.size()-1,transformer);
    transformers.add(namedT); // The NamedDataTransformer must always be the last transformer of the group
  }

  /**
   * Adds an instance of each of the transformers specified in the
   * <code>transformers</code> list to this group of transformers
   *
   * @param transformerSpecs list of {@link TransformerSpec}
   *   objects, each one specifying one of the transformers to be added in the
   *   group. It can not be null
   * @throws IllegalArgumentException If the given <code>transformerSpecs</code>
   *   list is null
   * @throws ConfigurationException if the order of the transformers in the
   *   <code>transformerSpecs</code> parameter does not follow the building
   *   rules described in this class header javadoc (in first place transformers
   *   for derived classes, later transformers for base classes (see this class
   *   header javadoc for more information).
   * @throws ConfigurationException if any of the class names contained in the
   *   list of transformer specifications, can not be loaded or instanciated
   *   (e.g. the transformer class has not got a no params constructor or a
   *   constructor with a <code>java.util.Map</code> param, as described in the
   *   header javadoc of class {@link org.ystsrv.Transformer})
   */
  private void addTransformers(List transformerSpecs) throws ConfigurationException {
    if (transformerSpecs == null)
      throw new IllegalArgumentException("Transformer specification list can not be null");

    Iterator iter = transformerSpecs.iterator();
    while (iter.hasNext()) {
      TransformerSpec ts = (TransformerSpec)iter.next();
      addTransformer(ts);
    }
  }

  public List getTransformers() {
    return this.transformers;
  }

  /**
   * Builds an {@link org.ystsrv.ModelSection} object that holds the
   * content of the actual model section that will substitute the test model
   * section of a Yeast template. This method transforms each one of the received
   * objects contained in <code>data</code>, offering each object to each one of
   * the transformers that this <code>TransformerGroup</code> contains. If
   * one of the <code>Transformer</code>s knows how to transform the object,
   * it will transform it, appending the result of this individual
   * transformation to the global result.
   *
   * @param data Array of objects to be transformed. If some of the objects is
   *   null, then it will not be transformed.Only a JavaScript comment
   * (<code>// Null data skipped</code>) will be inserted instead.
   * @return A {@link org.ystsrv.ModelSection} object holding the
   *   content of the actual model section that will substitute the test model
   *   section of a Yeast template.
   * @throws ConfigurationException if some of the member transformers returns
   *   <code>null</code> in its
   *   {@link org.ystsrv.Transformer#transformedClass} method
   * @throws TransformationException Any exception thrown in the
   *   {@link org.ystsrv.Transformer#transform} method of any of
   *   the member transformers this object holds
   */
  public ModelSection transform(Object ... data) throws TransformationException, ConfigurationException {
    if (data == null || data.length == 0) {
      Debug.fine(LOGGER_NAME2, "TransformerGroup.transform: no plain objetcs received to transform");
    }
    ModelSection dataTr = new ModelSection();
    if (data != null) {
      boolean[] alreadyUsedTransformers = new boolean[this.transformers.size()];
      Arrays.fill(alreadyUsedTransformers, false);
      for (int i = 0; i < data.length; i++) {
        if (data[i] != null) {
          dataTr.appendLine(this.transform(data[i], alreadyUsedTransformers));
        } else {
          Debug.warning(LOGGER_NAME2, "TransformerGroup.transform: skiping null data to transform");
          dataTr.appendLine("// Null data skipped");
        }
      }
    }
    return dataTr;
  }

  /**
   * The same as {@link TransformerGroup#transform(java.lang.Object[])}.
   *
   * @param data java.util.Collection.It can not be null. If some of the objects
   *  is null, then it will not be transformed. Only a JavaScript comment
   * (<code>// Null data skipped</code>) will be inserted instead.

   * @return org.ystsrv.ModelSection
   * @throws ConfigurationException if some of the member transformers returns
   *   <code>null</code> in its
   *   {@link org.ystsrv.Transformer#transformedClass} method
   * @throws TransformationException Any exception thrown in the
   *   {@link org.ystsrv.Transformer#transform} method of any of
   *   the member transformers this object holds
   */
  public ModelSection transform(Collection data) throws TransformationException, ConfigurationException {
    return this.transform( (data != null) ? data.toArray() : null);
  }

// Throws
// ConfigurationException if some of the member transformers returns null in its {@link org.ystsrv.Transformer#transformedClass} method
// RuntimeException Any implicit exception thrown in the {@link org.ystsrv.Transformer#transform} method of any of the member transformers this object holds
// TransformationException
  private String transform(Object data, boolean[] alreadyUsedTransformers) throws TransformationException {
    String trData = "";
    boolean transformed = false;
    Debug.fine(LOGGER_NAME2, "Trying to transform "+data);
    for (int i = 0; i < this.transformers.size(); i++) {
      Transformer t = (Transformer)this.transformers.get(i);
      Debug.fine(LOGGER_NAME2, "TransformerGroup testing transformer "+t);
      if (t.accept(data) && !alreadyUsedTransformers[i]) {
        Debug.fine(LOGGER_NAME2, t + " transforms " + data);
        trData = t.transform(data);
        transformed = true;
        if (i != this.transformers.size()-1) alreadyUsedTransformers[i] = true; // NamedDataTransformer is always available
        break;
      }
    }

    if (Debug.hasInfoLevel(LOGGER_NAME2)) {
      if (!transformed) {
        Class cl = data.getClass();
        String msg = "Transformer not found to transform object of class " +
            data.getClass().getName();
        if (java.util.Collection.class.isAssignableFrom(cl)) {
          if ( ( (Collection)data).size() > 0) {
            Iterator iter = ( (Collection)data).iterator();
            msg += "(" + iter.next().getClass().getName() + ")";
          }
        }
        Debug.info(LOGGER_NAME2, msg);
      }
    }

    return trData;
  }

  /*
     El rollo de comprobar si es de tipo CollectionTransformer... es por un problema que
     encontró Astorgano: tenia que transformar objetos Date y listas de objetos
     Date, para lo cual tenía dos transformers pero no podían estar en la misma
     plantilla (en el mismo grupo) xq ambos eran para la misma clase (en el caso
     de CollectionTransformer... se toma la clase base para comparar). AHora comparo los
     CollectionTransformer por un lado y los no CollectionTransformer por otro
   */
  private void checkConfigurationOrder(Transformer newTrans) {
    // Dara error si trato de meter un transfoer de una clase C habiendo ya
    // algun transformer de alguna superclase de C. En ese caso el viejo
    // capturaría los objetos de C y no estríamos haciendo bien el grupo. Clases
    // iguales si pueden aparecer
    Class newTrnsClass = newTrans.transformedClass();
    if (newTrnsClass == null)
      throw new ConfigurationException("Transformer class " + newTrans.getClass().getName() +
                                   " can not return null in transformedClass() method");

    if (newTrans instanceof CollectionTransformer) {
      for (int i = 0; i < this.transformers.size(); i++) {
        Transformer aux = (Transformer)this.transformers.get(i);
        if (aux instanceof CollectionTransformer &&
            !newTrans.transformedClass().equals(aux.transformedClass()) &&
            aux.transformedClass().isAssignableFrom(newTrnsClass)) {
          throw new ConfigurationException(
              "The transformers order in the TransformerGroup is not valid. " +
              "You are adding a transformer for a collection of objects of " +
              "class " + newTrnsClass + " after an already" +
              " present transformer for collections of its superclass ("
              + aux.transformedClass().getName() + ")");
        }
      }
    } else {
      for (int i = 0; i < this.transformers.size(); i++) {
        Transformer aux = (Transformer)this.transformers.get(i);
        if (! (aux instanceof CollectionTransformer) &&
            !newTrans.transformedClass().equals(aux.transformedClass()) &&
            aux.transformedClass().isAssignableFrom(newTrnsClass)) {
          throw new ConfigurationException(
              "The transformers order in the TransformerGroup is not valid. " +
              "You are adding a transformer for class " + newTrnsClass +
              " after an already present transformer of its superclass ("
              + aux.transformedClass().getName() + ")");

        }
      }
    }
  }
}
