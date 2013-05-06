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
package org.ystsrv.manager;

import java.io.IOException;
import java.io.InputStream;

import org.ystsrv.debug.Debug;

/**
 * This class represent sources for Yeast Templates reachable in some of the
 * directories specified in the classpath.
 *
 * <p>Company: University of La Rioja. Mathematics and Computer Science
 * Department</p>
 *
 * @author Francisco José García Izquierdo
 * @version 1.0
 */
class ClasspathSource extends TemplateSource {

  private static final String LOGGER_NAME = "ystsrv.manager";

  private String path;

  ClasspathSource(String path, String storeName) throws IOException { //250111d añadido storeName
    super(storeName);
    Debug.prec(path, "path can not be null nor empty");
    this.path = path;
  }

  /**
   * {@inheritdoc}
   */
   public long getLastModifiedTime() {
    // This type of templates are not reloadable
    return -1;
  }

  /**
   * {@inheritDoc}
   */
   public InputStream getInputStreamToTemplate() throws IOException {
    InputStream is = ClasspathSource.class.getResourceAsStream(this.path);
    if (is == null) {
      throw new IOException("The resource corresponding to template " + this.path +
                            " has not been found in the classpath");
    }
    Debug.fine(LOGGER_NAME, "Returning Reader for template in classpath location " + this.path);
    return is;
  }

  public String toString() {
    return "Classpath resource "+this.path;
  }
}
