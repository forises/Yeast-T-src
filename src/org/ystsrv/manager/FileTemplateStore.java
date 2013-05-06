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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

import org.ystsrv.debug.Debug;

/**
 *
 * Specialization of the {@link TemplateStore} class that is used to manage
 * templates that are located in the file system.
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
public class FileTemplateStore extends TemplateStore {

  // Absolute path to the directory where templates are stored
  private String LOCAL_STORE;

  /**
   * Builds a FileTemplateStore that loads templates from the directory
   * <code>templatesDir</code>. This diretory MUST be reachable from the root
   * directory of the web application.
   *
   * @param templateStore path to the directory where templates are stored.
   *   This path MUST be reachable from the root directory of the web
   *   application.
   * @throws IOException if the store does not exists or can not be reached in
   *   the root directory of the web application
   */
  public FileTemplateStore(String templateStore) throws IOException {
    super(templateStore);
    Debug.prec(templateStore);
    java.net.URL root = FileTemplateStore.class.getResource("/../.." + templateStore);
    if (root != null) {
      File rootf = new File(URLDecoder.decode(root.getFile()));
      LOCAL_STORE = rootf.getCanonicalPath();
      Debug.fine(LOGGER_NAME, "FileTemplateStore: The path to templates is: " + LOCAL_STORE);
    } else {
      throw new IOException("FileTemplateStore not created. " +
                            this.storeName + " store does not exist in " +
                            "the root of the web application context.");
    }
    this.initCache();
    Debug.fine(LOGGER_NAME, "Built FileTemplateStore for template store " + templateStore);
  }

  /**
   * {@inheritdoc}
   */
  protected TemplateSource getSourceToTemplate(String id) throws IOException {
    Debug.prec(id, "id is null");
    String fileName = LOCAL_STORE + getTemplateLocation(id);

    return new FileSource(fileName, this.storeName);
  }


  /**
   * {@inheritdoc}
   */
  protected InputStream getConfigFile() throws IOException {
    InputStream configIS = new FileInputStream(LOCAL_STORE + "/YSTConfig.xml");
    if (configIS == null)
      Debug.warning(LOGGER_NAME, "YSTConfig.xml file not found for "+this.storeName+" template store (FileTemplateStore)");

    return configIS;
  }

  public String toString() {
    return "FileTemplateStore for store " + storeName;
  }

}
