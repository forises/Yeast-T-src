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
import javax.servlet.ServletContext;

import org.ystsrv.debug.Debug;

/**
 * Specialization of the {@link TemplateStore} class that is used to manage
 * templates that are located in the context of the web application.
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
public class ServletContextTemplateStore extends TemplateStore {

  private ServletContext context;

  /**
   * Creates a ServletContextTemplateStore that loads templates from the
   * <code>templatesDir</code> directory. This diretory MUST be a subdirectory
   * of the root of the web application context..
   *
   * @param templateStore path to the directory where templates are stored. The
   *   path MUST be reachable in the classpath.
   */
  public ServletContextTemplateStore(String templateStore, ServletContext context) {
    super(templateStore);
    Debug.prec(templateStore);
    Debug.prec(context, "The context can not be null");
    this.context = context;
    this.initCache();
    Debug.fine(LOGGER_NAME, "Built ServletContextTemplateStore for template store " +
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

    return new ServletContextSource(fileName, this.storeName, context);
  }


  /**
   * {@inheritdoc}
   */
  protected InputStream getConfigFile() throws IOException {
    InputStream configIS = context.getResourceAsStream(this.storeName + "/YSTConfig.xml");
    if (configIS == null)
      Debug.warning(LOGGER_NAME, "YSTConfig.xml file not found for "+this.storeName+" template store (ServletContextTemplateStore)");
    return configIS;
  }

  public String toString() {
    return "ServletContextTemplateStore for store " + storeName;
  }

}
