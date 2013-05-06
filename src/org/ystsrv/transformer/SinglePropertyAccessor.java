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
class SinglePropertyAccessor implements PropertyAccessor {
  private Method methodName;

  SinglePropertyAccessor(Method methodName) {
    Debug.prec(methodName);
    this.methodName = methodName;
  }

  public Object getProperty(String name, Object obj) throws InvocationTargetException, IllegalAccessException {
    Debug.prec(name);

    Debug.check(name.indexOf(".") == -1, "The property "+name+" is not simple");
    if (obj == null) return null;
    return methodName.invoke(obj, null);
  }
}
