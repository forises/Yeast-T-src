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

import java.util.Map;

/**
 * Class that encapsulates data needed to dynamically build a transformer
 * object. These data are the class name, and a list of constructor parameters
 * (name+value)
 *
 * @author Francisco José García Izquierdo
 * @version 2.0
 */
public class TransformerSpec {
  private String className;
  private Map params;

  /**
   * Builds a <code>TransformerSpec</code> object from its corresponding class
   * name and a map with pairs of parameter names (keys in the map) and
   * parameter values (values of the map). The values of this map will be used
   * as the constructor parameters of the transformer.
   *
   * @param className String
   * @param params Map
   * @throws IllegalArgumentException If the given <code>className</code> is
   *   null or the empty string
   */
  public TransformerSpec(String className, Map params) {
    if (className == null || className.trim().length() == 0)
      throw new IllegalArgumentException("className can not be null nor empty");
    this.className = className;
    this.params = params;
  }

  /**
   * Builds a <code>TransformerSpec</code> object from its corresponding class
   * name and no construction parameters
   *
   * @param className String
   * @throws IllegalArgumentException If the given <code>className</code> is
   *   null or the empty string
   */
  public TransformerSpec(String className) {
    this(className, null);
  }

  /**
   * Returns a map with the name and values of the parameters needed to build
   * the transformer to which this <code>TransformerSpec</code> refers to.
   *
   * @return Map
   */
  public java.util.Map getParams() {
    return params;
  }

  /**
   * Returns the name of the class needed to build the transformer to which this
   * <code>TransformerSpec</code> refers to.
   *
   * @return String
   */
  public String getClassName() {
    return className;
  }

  public String toString() {
    return "Transformer " + className +
        (params != null ? " with params " + params : " without params");
  }
}
