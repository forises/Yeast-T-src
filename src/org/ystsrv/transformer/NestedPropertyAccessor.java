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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.ystsrv.debug.Debug;

/**
 * <p>Title: </p>
 * <p>Description: </p>

 * @author Francisco José García Izquierdo
 * @version 2.0
 */

class NestedPropertyAccessor implements PropertyAccessor {
  private PropertiesAccessor accessor;

  private Method methodName;

  NestedPropertyAccessor(Method methodName) {
    Debug.prec(methodName);
    this.methodName = methodName;
    this.accessor = new PropertiesAccessor(methodName.getReturnType());
  }

  public Object getProperty(String subPropName, Object containerObj) throws InvocationTargetException,
       IllegalAccessException {
    Debug.prec(subPropName, "Empty property name");

    Object propValue = containerObj != null ? methodName.invoke(containerObj, null) : null; /**/
    if (subPropName.charAt(0) == '.') // It is an actual sub property
      return accessor.getProperty(subPropName.substring(1), propValue);
    else // It is the container object property
      return propValue;
  }

}
