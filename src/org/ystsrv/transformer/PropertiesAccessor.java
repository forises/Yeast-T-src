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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.ystsrv.ConfigurationException;
import org.ystsrv.debug.Debug;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * @author Francisco José García Izquierdo
 * @version 2.0
 */

class PropertiesAccessor {
  private Map propertiesAccessors;
  private Class theClass;
  private static Map alreadyParsedClasses = new Hashtable(); // es una forma de evitar la recursividad
                                                             // al ser static lo comparten todos los PropertiesAccessor

  PropertiesAccessor(Class theClass) {
    Debug.prec(theClass, "theClass can not be null");
    this.theClass = theClass;
    parseClass();
  }

  private void parseClass() {
    // Si varios hilos estan iniciando la construcción de Un properties accesor para la clase, solo uno
    // lo hara el primero. Los demás cogerán lo que ese hizo, pero mientras esperan
    synchronized (alreadyParsedClasses) {
      this.propertiesAccessors = (Map)alreadyParsedClasses.get(this.theClass.getName());
      if (this.propertiesAccessors == null) {
        this.propertiesAccessors = new HashMap();
        alreadyParsedClasses.put(this.theClass.getName(), this.propertiesAccessors);
        try {
          BeanInfo info = Introspector.getBeanInfo(theClass,
              (theClass.isInterface() ? null : Object.class));
          PropertyDescriptor props[] = info.getPropertyDescriptors();

          for (int i = 0; i < props.length; i++) {
            String propName = props[i].getName();
            PropertyAccessor pa = buildPropertyAccessor(props[i]);
            if (pa != null) propertiesAccessors.put(propName, pa);
          }
        } catch (IntrospectionException ex) {
          throw new ConfigurationException("Impossible to transform the variable of class " +
                                            theClass + ". Problems accesing to " +
                                            "the bean properties [" + ex + "].");
        }
      }
    }
  }

  /**
   * getProperty
   *
   * @param name String
   * @param obj Object
   * @return Object
   * @throws NoSuchMethodException
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   */
  Object getProperty(String name, Object obj) throws InvocationTargetException, IllegalAccessException {
    Debug.prec(name!=null,"name no puede ser nulo"); /**/
    if (name.length() == 0)
      throw new ConfigurationException("Impossible to transform. Some of the"+
                                           " property references in your format "+
                                           "string ends with a dot.");

    String nameAux = name;
    int iDot = nameAux.indexOf('.');
    PropertyAccessor pa = null;
    if (iDot != -1) {
      pa = (PropertyAccessor)this.propertiesAccessors.get(nameAux.substring(0, iDot));
      nameAux = nameAux.substring(iDot);  // includes the .
    } else {
      pa = (PropertyAccessor)this.propertiesAccessors.get(nameAux);
    }

    if (pa != null) {
      return pa.getProperty(nameAux, obj);
    } else {
      throw new ConfigurationException("Impossible to transform the " + name +
                                           " variable of class " + this.theClass +
                                           ". Bean has not property " + name);
    }
  }

  private static PropertyAccessor buildPropertyAccessor(PropertyDescriptor prop) {
    Debug.prec(prop);
    Class propType = prop.getPropertyType();
    Method getMethod = prop.getReadMethod();
    if (getMethod == null) return null; // Puede no tener método get
    if (propType.isPrimitive() || propType.isArray() ||
        propType.getName().equals("java.lang.String") ||
        java.util.Collection.class.isAssignableFrom(propType) ||
        java.util.Map.class.isAssignableFrom(propType)||
        java.util.Calendar.class.isAssignableFrom(propType)) {

      return new SinglePropertyAccessor(getMethod);
    } else {
      return new NestedPropertyAccessor(getMethod);
    }
  }
}
