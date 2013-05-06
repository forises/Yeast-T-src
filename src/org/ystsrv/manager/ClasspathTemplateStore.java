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
 *
 * Specialization of the {@link TemplateStore} class that is used to manage
 * templates that are located in some of the directories specified in the
 * CLASSPATH environment variable.
 *
 * <p> You MUST not use this class by your own. To retrieve a certain
 * <code>Template</code> object, use the class {@link
 * org.ystsrv.TemplateManager} instead.
 *
 * <p>Company: University of La Rioja. Mathematics and Computer Science
 * Department</p>
 *
 * @author Francisco José García Izquierdo
 * @version 3.0
 * @see org.ystsrv.TemplateManager
 */
public class ClasspathTemplateStore extends TemplateStore {

  /**
   * Creates a ClasspathTemplateStore that loads templates from the
   * <code>templateStore</code> directory. This diretory MUST be a subdirectory
   * of some of the directories specified in the CLASSPATH environment variable.
   *
   * @param templateStore path to the directory where templates are stored. The
   *   path MUST be reachable in the classpath.
   * @throws IOException if the store does not exists or can not be reached in
   *   the CLASSPATH
   */
  public ClasspathTemplateStore(String templateStore) throws IOException {
    super(templateStore);
    Debug.prec(templateStore);
    java.net.URL root = ClasspathTemplateStore.class.getResource(templateStore);
    if (root == null) {
      throw new IOException("ClasspathTemplateStore not created. " +
                            this.storeName + " store does not exist in classpath");
    }
    this.initCache();
    Debug.fine(LOGGER_NAME, "Built ClasspathTemplateStore for template store " +
               templateStore);
  }

  /**
   * {@inheritdoc}
   */
  protected TemplateSource getSourceToTemplate(String id) throws IOException {
    Debug.prec(id, "The template id can not be null or empty");
    String fileName;
    if (this.storeName.equals("/"))
      fileName = getTemplateLocation(id);
    else
      fileName = this.storeName + getTemplateLocation(id);

    return new ClasspathSource(fileName, this.storeName);
  }

  /**
   * {@inheritdoc}
   */
  protected InputStream getConfigFile() throws IOException {
    InputStream configIS = ClasspathTemplateStore.class.getResourceAsStream(this.
        storeName + "/YSTConfig.xml"); // It can be null
    if (configIS == null)
      Debug.warning(LOGGER_NAME, "YSTConfig.xml file not found for "+this.storeName+" template store (ClasspathTemplateStore)");

    return configIS;
  }

  public String toString() {
    return "ClasspathTemplateStore for store " + storeName;
  }
}
