/*
 *  Yeast-Server for Java
 *
 *  Copyright (c) 2011 Francisco José García Izquierdo. University of La
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
 * Classes extending this class will represent the different sources
 * from where a template may be loaded (a file, the classpath or the web
 * application context).
 *
 * @author Francisco José García Izquierdo
 * @version 1.0
 */
public abstract class TemplateSource {  //250111d

  protected String storeName;

  protected TemplateSource(String storeName) {
    Debug.prec(storeName, "storeName can not be null nor empty");
    this.storeName = storeName;
  }

  /**
   * Returns the name of the template store to which the template refered by
   * this TemplateSource belongs
   * @return String
   */
  public String getStoreName() {
    return this.storeName;
  }

  /**
   * Returns the source last update time. This value is used to determine if the
   * template content has changed since that last load. If so, the template must
   * be reloaded.
   *
   * @return long
   */
  public abstract long getLastModifiedTime();

  /**
   * Returns an <code>InputStream</code> that provides access to the template
   * content. The way this InputStream is built depends on the type of location
   * of the template (file system, resource in the classpath, ...). Concrete
   * classes implementin <code>TemplateSource</code> will implement each
   * alternative.
   *
   * @throws IOException any error while building the InputStream (e.g. the
   *   location does not correspond to an actual resource).
   * @return Reader
   */
  public abstract InputStream getInputStreamToTemplate() throws IOException;

}
