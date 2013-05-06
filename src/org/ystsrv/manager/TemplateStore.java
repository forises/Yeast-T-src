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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.ystsrv.ConfigurationException;
import org.ystsrv.Template;
import org.ystsrv.YSTException;
import org.ystsrv.debug.Debug;
import org.ystsrv.transformer.TransformerSpec;
import org.ystsrv.util.TextUtils;
import org.ystsrv.util.XMLUtils;
import org.ystsrv.yeipee.ClientYeipeeStatus;

/**
 * This is the class responsible for the generic functionality related to
 * loading and caching templates. Its descendants must specify the mechanism of
 * building the templates from a concrete storage type (i.e. file system, url,
 * as resources in the classpath, ...).
 *
 * <p> A concrete <code>TemplateStore</code> object will cache the Yeast
 * templates contained in a certain template store.
 *
 * <p>The class offers a method to retrieve a {@link
 * org.ystsrv.Template} object: the method {@link
 * #getTemplate(String)}, that receives as input param the template identifier.
 *
 * <p>Templates in a template store may be configured using a file that MUST be
 * named <code>YSTConfig.xml</code>. Descendant classes must implement a way of
 * accessing that configuration file.
 *
 * @author Francisco José García Izquierdo
 * @version 2.0
 */
public abstract class TemplateStore {

  protected static final String LOGGER_NAME = "ystsrv.manager";

  protected String storeName;

  // Cache of templates
  private Map templateCache;

  // Info asociated to each template in the configuration file YSTConfig.xml:
  // location of the templateInfotempalte and list of transformers
  private Map templateInfo;

  /**
   * Creates a TemplateStore for a given <code>templateStoreName<code>.
   *
   * @param templateStoreName name of the template store
   */
  TemplateStore(String templateStoreName) {
    Debug.prec(templateStoreName, "The templateStoreName can not be null or empty");
    this.storeName = templateStoreName;
  }

  protected void initCache() {
    templateInfo = new HashMap();
    cacheTemplatesInfo();
    templateCache = new java.util.HashMap();
  }

  /**
   * Returns a <code>Template</code> object representing the template with
   * the corresponding <code>id</code>. This template can be stored anywhere, in
   * the file system, in a remote URL, in any of the directories specified in
   * the class path. The descendant of this class will implement the suitable
   * mechanism to build the template. For this mechanism to be implemented,
   * descendants redefine the method {@link #buildTemplate}.
   *
   * @param id identifier of the template. Template identifiers, unless
   *   something different being specified in the <code>YSTConfig.xml</code>
   *   configuration file, refers directly to the name of the file that
   *   contains the template (without the extension). They can also refer to a
   *   configuration element in the configuration file
   *   <code>YSTConfig.xml</code>, where they are asigned to an actual file
   *   name.
   * @throws IOException If there is any problem reading the template document
   *   from its source
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
   * @return Template
   */
  public Template getTemplate(String id) throws IOException, YSTException, ConfigurationException {
    Debug.prec(id, "id can not be null nor empty");
    char reqType = getRequestType();
    Template t = (Template)templateCache.get(reqType+"_"+id);
    if (t == null) {
      t = cacheTemplate(id, reqType);
    }
    return t;
  }

  private char getRequestType() {
    int yeipeeStatus = ClientYeipeeStatus.getStatus();
    return (ClientYeipeeStatus.mustYeipee(yeipeeStatus) ? 'Y' : 'N');
  }

  private synchronized Template cacheTemplate(String id, char requestType) throws IOException, YSTException {
    Debug.prec(id, "id can not be null nor empty");
    String internalId = requestType+"_"+id;
    Template t = (Template)templateCache.get(internalId);
    if (t == null) {
      t = buildTemplate(id, requestType);
      templateCache.put(internalId, t);
    }
    return t;
  }

  private Template buildTemplate(String id, char requestType) throws IOException, YSTException  {
    Debug.prec(id, "The template id can not be null or empty");
    TemplateSource source = getSourceToTemplate(id);
    if (requestType == 'Y')
      return new YeipeeTemplate(id, this.getTemplateTransformers(id), source);
    else
      return new Template(id, this.getTemplateTransformers(id), source);

  }

  /**
   * In this factory method, a concrete descendant of this class will construct
   * the concrete <code>TemplateSource</code> object that will provide access to the
   * template source identified with
   * <code>id</code>. This id helps the concrete descendant to locate the
   * template, because it can be the name of the file or refer to a
   * configuration element in the <code>YSTConfig.xml</code> file where the
   * actual file name can be found.
   *
   * <p>The method locates the actual location of the template file in the
   * <code>YSTConfig.xml</code> file. If this file does not exit or it does not
   * contain a config element for the template, the location of the template
   * correspond to a file of name <code>id<code> and extension ".html". The name
   * of the template store associated to this TemplateStore in its constructor
   * is pre-concatenated to the template's name. In this case default
   * transformers are associated to the template.
   *
   * <p>If there is a configuration element for the template <code>id</code> in
   * the <code>YSTConfig.xml</code>, the name of the template is taken form that
   * configuration. The configuration may include a list of transformers. In
   * this case the template will be created with those transformers inside.
   *
   * @param id String
   * @throws IOException if there is any problem loading the template (e.g. the
   *   template does not exists).
   * @return Template
   */
  abstract protected TemplateSource getSourceToTemplate(String id) throws IOException;

  /**
   * Returns an <code>InputStream</code> to the configuration file to be used by
   * the concrete <code>TemplateStore</code>
   *
   * @throws IOException If there is any problem reading the config file
   *   (<code>YSTConfig.xml</code>) from its source
   * @return InputStream
   */
  abstract protected InputStream getConfigFile() throws IOException;

  private void cacheTemplatesInfo() {
    Debug.fine(LOGGER_NAME, this+ " initializing templates info cache. Trying to load YSTConfig.xml");
    try {
      InputStream configIS = getConfigFile();
      if (configIS != null) {
        parseConfigFile(configIS);
      }
    } catch (IOException ex) {
      Debug.fine(LOGGER_NAME, ex);
    } catch (SAXException ex) {
      Debug.warning(LOGGER_NAME, ex);
    } catch (ParserConfigurationException ex) {
      Debug.warning(LOGGER_NAME, ex);
    } catch (FactoryConfigurationError ex) {
      Debug.warning(LOGGER_NAME, ex);
    }
  }

  private void parseConfigFile(InputStream configIS) throws ConfigurationException, IOException, SAXException, ParserConfigurationException, FactoryConfigurationError {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.parse(configIS);

    NodeList templates = doc.getDocumentElement().getElementsByTagName("template");
    for (int i = 0; i < templates.getLength(); i++) {
      Element template = (Element)templates.item(i);
      String id = template.getAttribute("id");
      id = TextUtils.normalizePath(id);
      String location = XMLUtils.getChildTextTrim(template, "location");
      List transformers = null;
      NodeList transformersEls = template.getElementsByTagName("transformer");
      if (transformersEls != null && transformersEls.getLength() != 0) {
        transformers = new ArrayList(transformersEls.getLength());
        for (int j = 0; j < transformersEls.getLength(); j++) {
          Element transformerEl = (Element)transformersEls.item(j);
          String className = transformerEl.getAttribute("class");
          if (className == null || className.trim().length() == 0)
            throw new ConfigurationException(
                "Error parsing YSTConfig.xml file ("+id+" template): transformer element: required attribute (class)");
          NodeList paramsEls = transformerEl.getElementsByTagName("param");
          Map params = null;
          for (int k = 0; k < paramsEls.getLength(); k++) {
            Element paramEl = (Element)paramsEls.item(k);
            String paramName = paramEl.getAttribute("name");
            if (paramName == null || paramName.trim().length() == 0)
              throw new ConfigurationException(
                  "Error parsing YSTConfig.xml file ("+id+" template; "+className+" transformer): param element: required attribute (name)");
            String value = XMLUtils.getTextTrim(paramEl);
            if (params == null)params = new HashMap();
            params.put(paramName, value);
          }
          transformers.add(new TransformerSpec(className, params));
        }
      }
      TemplateInfo info = new TemplateInfo(location, transformers);
      templateInfo.put(id, info);

      if (Debug.hasFineLevel(LOGGER_NAME)) {
        String logMsg = this+" caching template info for template " +
            id + "." + (location != null ? " Located in " + location : ".");
        logMsg += ". Will use transformes: " + transformers;
        Debug.fine(LOGGER_NAME, logMsg);
      }
    }
  }

  /**
   * Returns the configuration info specified in the <code>YSTConfig.xml</code>
   * file for the template of identifier <code>id</code>
   *
   * @param id String
   * @return TemplateInfo
   */
  protected TemplateInfo getTemplateInfo(String id) {
    Debug.prec(id, "id can not be null");
    return (TemplateInfo)templateInfo.get(id);
  }

  /**
   * Returns the location specified in the <code>YSTConfig.xml</code> file for
   * the template of identifier <code>id</code>
   *
   * @param id String
   * @return String
   */
  protected String getTemplateLocation(String id) {
    Debug.prec(id, "id can not be null");
    TemplateInfo info = getTemplateInfo(id);
    if (info == null) {
      return id + ".html";
    }
    return (info.getLocation() == null) ? id + ".html" : info.getLocation();
  }

  /**
   * Returns the list of transformers (class name and parameters of
   * construction) specified in the <code>YSTConfig.xml</code> file for the
   * template of identifier <code>id</code>
   *
   * @param id String
   * @return List of TransformerSpec
   */
  protected List getTemplateTransformers(String id) {
    Debug.prec(id, "id can not be null");
    TemplateInfo info = getTemplateInfo(id);
    if (info == null) {
      return null;
    }
    return info.getTransformerSpecs();
  }



  /**
   * Holds the data asociated to a template in the configuration file
   * <code>YSTConfig.xml</code>. These data are the transformers of the template
   * and the location of the template.
   *
   * @author Francisco José García Izquierdo
   * @version 1.0
   */
  private class TemplateInfo {
    private String location;
    private List transformerSpecs;

    TemplateInfo(String location, List transformers) {
      this.location = TextUtils.normalizePath(location);
      this.transformerSpecs = transformers;
    }

    /**
     * Returns the location specified in the <code>YSTConfig.xml</code> file for
     * the template represented by this objec
     *
     * @param id String
     * @return String
     */
    String getLocation() {
      return location;
    }

    /**
     * Returns the list of transformerSpecs (class name and parameters of
     * construction) specified in the <code>YSTConfig.xml</code> file for the
     * template represented by this object
     *
     * @return List of strings
     */
    List getTransformerSpecs() {
      return transformerSpecs;
    }
  }

}
