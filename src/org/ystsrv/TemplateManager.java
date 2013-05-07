/*
 *  Yeast-Server for Java
 *
 *  Copyright (c) 2011, Francisco Jose Garcia Izquierdo. University of La
 *  Rioja. Mathematics and Computer Science Department. All Rights Reserved.
 *
 *  Contributing Author(s):
 *
 *     Francisco J. Garcia Iquierdo <francisco.garcia@unirioja.es>
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
package org.ystsrv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;

import org.ystsrv.debug.Debug;
import org.ystsrv.manager.ClasspathTemplateStore;
import org.ystsrv.manager.Config;
import org.ystsrv.manager.FileTemplateStore;
import org.ystsrv.manager.ServletContextTemplateStore;
import org.ystsrv.manager.TemplateStore;
import org.ystsrv.util.TextUtils;

/**
 * This class is the responsible for providing the application with Yeast
 * templates. It offers several versions of the static method
 * <code>getTemplate</code> for retrieving a template (object of class {@link
 * org.ystsrv.Template}) identified with a given identifier.
 *
 * <p>The template identifiers, unless something different being specified in
 * the configuration file commented below, refers directly to the name of the
 * file that contains the template (without the extension). E.g.: the template
 * of id <code>test/example1</code> must correspond to a file named
 * test/example1.html, relative to the template store.
 *
 * <p>A templates store (directory) can also be specified. If no store is
 * specified, the class assumes "/yst" as the default store. This directory is
 * expected to be located in the root of the web application, or be reachable
 * from any of the directories defined by the CLASSPATH environment variable.
 * E.g.: the template of id <code>test/example1</code> in the store
 * <code>myTemplates</code> must correspond to a file of absolute path
 * APP_HOME/myTemplates/test/example1.html or to file reachable from the
 * classpath in /myTemplates/test/example1.html.
 *
 * <p>The templates store may contain a configuration file, which MUST be named
 * YSTConfig.xml. In that file, an identifier different from its file name may
 * be specified for each template. Moreover, a list of tranformers can be
 * specified for a template. Please, refer to the Yeast Server documentation
 * looking for more details about the <code>YSTConfig.xml</code> file and its format.
 *
 * <p> In order to improve the application performance,
 * <code>Template</code>s are permanently cached. A direct consequence of
 * that caching is that two different calls requesting the same <code>id</code>
 * template will return exactly the same <code>Template</code> object. This
 * fact does not represent any inconvenience because <code>Template</code>
 * objects are threadsafe
 *
 * @author Francisco Jose Garcia Izquierdo
 * @version 1.0
 */
public class TemplateManager {

  private static final String LOGGER_NAME = "ystsrv.manager";

  private static final String DEFAULT_TEMPLATE_STORE = "/"+Config.DEFAULT_TEMPLATE_STORE_NAME;

  // Cache of TemplateStore objects (key - store name); actually contains lists of TemplateStore objects
  private static Map cachedStores = new HashMap();

  /**
   * Calls the {@link #getTemplate(String, String, ServletContext)} method to
   * locate the template of <code>id</code> in the default templates store
   * (/yst).
   *
   * @param id identifier of the template. Template identifiers, unless
   *   something different being specified in the <code>YSTConfig.xml</code>
   *   configuration file, refers directly to the name of the file that
   *   contains the template (without the extension). They can also refer to a
   *   configuration element in the configuration file
   *   <code>YSTConfig.xml</code>, where they are asigned to an actual file
   *   name.
   * @throws IllegalArgumentException if the given <code>id</code> is null or
   *   the empty string
   * @throws ConfigurationException if the configuration file
   *   (<code>YSTConfig.xml</code>) does follow the rules described in the Yeast
   *   Server documentation
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
   * @throws IOException If there is any problem reading the template document
   *   from its source (e.g. the template does not exists)
   * @return <code>Template</code>
   */
  public static Template getTemplate(String id, ServletContext context)
      throws IOException, ConfigurationException, YSTException {
    return getTemplate(DEFAULT_TEMPLATE_STORE, id, context);
  }

  /**
   * Returns a <code>Template</code> object representing the template with
   * the corresponding <code>id</code>. This template must be stored in the
   * templates directory <code>templateStore</code>. This directory must be
   * located either in the web application’s context root or in the CLASSPATH.
   *
   * @param templateStore name of the directory that contains the templates
   * @param id identifier of the template. Template identifiers, unless
   *   something different being specified in the <code>YSTConfig.xml</code>
   *   configuration file, refers directly to the name of the file that
   *   contains the template (without the extension). They can also refer to a
   *   configuration element in the configuration file
   *   <code>YSTConfig.xml</code>, where they are asigned to an actual file
   *   name.
   * @param context ServletContext that provides access to web application
   *   resources.
   * @throws IOException If there is any problem reading the template document
   *   from its source
   * @throws ConfigurationException if the configuration file
   *   (<code>YSTConfig.xml</code>) does follow the rules described in the Yeast
   *   Server documentation
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
   * @return <code>Template</code>
   */
  public static Template getTemplate(String templateStore, String id, ServletContext context)
      throws IOException, ConfigurationException, YSTException {
    if (id == null || id.trim().length() == 0)
      throw new IllegalArgumentException("id can not be null nor empty");

    if (templateStore == null) {
      Debug.fine(LOGGER_NAME, "Trying to get template " + id + " from default store");
      templateStore = DEFAULT_TEMPLATE_STORE;
    } else {
      Debug.fine(LOGGER_NAME, "Trying to get template " + id + " from " + templateStore + " store");
      templateStore = TextUtils.normalizePath(templateStore);
    }
    id = TextUtils.normalizePath(id);

    // For a certain templateStore name there may be at most three diferent
    // TemplateStore objects (due to templates accesed as file, as context
    // resources or classpath resources
    List stores = null;
    synchronized (cachedStores) {
      stores = (List)cachedStores.get(templateStore);
      if (stores == null) {
        stores = initStores(templateStore, context);
      }
    }
    Template template = null;
    List triedExcps = new ArrayList(3);
    for (int i = 0; i < stores.size(); i++) {
      TemplateStore store = (TemplateStore)stores.get(i);
      try {
        template = store.getTemplate(id);  // It may be cached. The template is refreshed when it is printed, not here
        Debug.fine(LOGGER_NAME, "Template " + id + " loaded with " + store);
        break;
      } catch (IOException ex1) {
        // Keep exceptions in order to inform about the trials of loading
        triedExcps.add(ex1);
        Debug.fine(LOGGER_NAME,
                   "Template " + id + " not loaded with " + store + " [" + ex1.getMessage() + "]");
      }
    }
    // If the template can not be loaded, throw an exception reporting all the failed attempts
    if (template == null) {
      reportErrors(templateStore, id, triedExcps);
    }
    return template;
  }

  private static void reportErrors(String templateStore, String id, List triedExcps)
      throws IOException {
    StringBuffer msg = new StringBuffer("Exception loading template " + id + " in store " +
                                        templateStore + ". ");
    if (triedExcps.size() != 0) {
      msg.append("Unsuccessful attempts error messages:");
      Iterator iter = triedExcps.iterator();
      while (iter.hasNext()) {
        IOException ex = (IOException)iter.next();
        msg.append("\n  " + ex.getMessage() + "");
      }
    } else {
      msg.append("Template store " + templateStore + " not found." +
                 "Make sure that this directory exists in the root " +
                 "of the web application context or in the classpath.");
    }
    throw new IOException(msg.toString());
  }

  /**
   * Init the list of <code>TemplateStore</code> objects that can hold a
   * template store named as specified in <code>templateStore</code> param. The
   * method first try to create a <code>FileTemplateStore</code> object; if
   * this construction fails, then, the <code>context</code> param is not null,
   * it creates a <code>ServletContextTemplateStore</code>; then it tries to
   * create a <code>ClasspathTemplateStore</code> object.
   *
   * @param templateStore String
   * @param context ServletContext
   * @return List
   */
  private static List initStores(String templateStore, ServletContext context) {
    List managers = new ArrayList(2);
    try {
      managers.add(new FileTemplateStore(templateStore));
    } catch (IOException ex) {
      Debug.fine(LOGGER_NAME,
                 "FileTemplateStore not build for template store " + templateStore + " [" +
                 ex.getMessage() + "]");

      if (context != null) {
        managers.add(new ServletContextTemplateStore(templateStore, context));
      }
    }
    try {
      managers.add(new ClasspathTemplateStore(templateStore));
    } catch (IOException ex) {
      Debug.fine(LOGGER_NAME,
                 "ClasspathTemplateStore not build for template store " + templateStore + " [" +
                 ex.getMessage() + "]");
    }
    cachedStores.put(templateStore, managers);
    return managers;
  }
}
