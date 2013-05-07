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
// He metido esta línea nueva
package org.ystsrv;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

import org.ystsrv.debug.Debug;
import org.ystsrv.manager.CachedTemplate;
import org.ystsrv.manager.Config;
import org.ystsrv.manager.TemplateCacheFactory;
import org.ystsrv.manager.TemplateContent;
import org.ystsrv.manager.TemplateSource;
import org.ystsrv.transformer.TransformerGroup;
import org.ystsrv.yeipee.ClientYeipeeStatus;

/**
 * This class defines the basic functionality associated to Yeast templates.
 *
 * <p>The class defines a set of methods that can be used to send the template
 * to a requesting client. These methods take a set of objects (or a {@link
 * ModelSection} object), and a <code>OutputStream</code> or
 * <code>Writer</code>, then use the objects to obtain the actual content of the
 * template model section, and then write the template through the
 * <code>OutputStream</code> or <code>Writer</code>.
 *
 * <p> This class caches the template content using a {@link
 * org.ystsrv.manager.CachedTemplate} object.
 *
 * <p>You MUST NOT create objects of this type by your own. The class {@link
 * org.ystsrv.TemplateManager} is the only one that should create
 * <code>Template</code> objects (see {@link
 * TemplateManager#getTemplate(String, javax.servlet.ServletContext)} or {@link
 * TemplateManager#getTemplate(String, String, javax.servlet.ServletContext)}).
 *
 * <p><code>Template</code> objects can be built taken into account the
 * information specified in the configuration file <code>YSTConfig.xml</code>.
 * In that file you can specify a location for a given template, and a set of
 * transformers that it will use. <code>YSTConfig.xml</code> file is located
 * inside the template store where the template resides.
 *
 * @author Francisco José García Izquierdo
 * @version 2.0
 */
public class Template {

  private static final String LOGGER_NAME = "ystsrv.manager";

  // Transformers associated to the template
  protected TransformerGroup transformers;

  // From where the template will be loaded
  protected TemplateSource source;

  protected String id;

  // Template cache
  protected CachedTemplate cache;

  // This message is inserted preceding the new model section once the template is processed
  private static final String PROCESSING_STAMP = "<!-- Processed with Yeast-Server v "
                                                 +Config.YST_SERVER_VERSION+" -->\n";


  /**
   * Builds a <code>Template</code> object from its <code>source</code>,
   * caches its content and initiallizes the required transformers. You MUST NOT
   * create objects of this type by your own. The class {@link
   * org.ystsrv.TemplateManager} is the only one that should create
   * <code>Template</code> objects (see {@link
   * TemplateManager#getTemplate(String, javax.servlet.ServletContext)} or {@link
   * TemplateManager#getTemplate(String, String,
   * javax.servlet.ServletContext)}).
   *
   * @param id String
   * @param transformerSpecs List of {@link
   *   org.ystsrv.transformer.TransformerSpec} objects specifying
   *   the transformers to be used by the template.
   * @param source {@link org.ystsrv.manager.TemplateSource} object
   *   which provides access to the template source.
   * @throws IOException If any error occurs in the loading of the template
   */
  public Template(String id, List transformerSpecs, TemplateSource source) throws IOException, YSTException {
    Debug.prec(id, "The id of the template can not be null or empty");
    Debug.prec(source, "The source of the template can not be null");
    this.id = id;
    this.source = source;
    if (transformerSpecs != null) {
      this.setTransformers(new TransformerGroup(transformerSpecs));
      Debug.fine(LOGGER_NAME,
                 "Template " + this + ": assigned default transformer + " + transformerSpecs);
    } else {
      this.setTransformers(new TransformerGroup());
      Debug.fine(LOGGER_NAME, "Template " + id + ": assigned default transformer");
    }

    Debug.info(LOGGER_NAME, "Loading template " + this +" for the first time.");
    this.cache = TemplateCacheFactory.buildCache(this.id, this.source);
  }

  /**
   * Returns the encoding used in the template. It is based on the value
   * provided in the content-type meta tag of the HTML template. If this tag is
   * not present or it is malformed, the returned value is null.
   * @return String
   */
  public String getTemplateEncoding() {
    return this.cache.getCharsetEncoding();
  }

  /**
   * Returns the HTML content of the template as it is defined and stored.
   * Therefore, this content includes a model section with the test data
   * that the HTML designer specified at design time.
   *
   * @throws IOException Exception getting access to the template content
   * @return an String with the whole content (HTML) of the template
   */
  public String getDesignerVersion() throws IOException {
    String encoding = this.getTemplateEncoding();
    if (encoding != null)
      return new String(this.getCachedContent().getDesignerVersion(), encoding);
    else
      // use the default encoding
     return new String(this.getCachedContent().getDesignerVersion());
  }


  /**
   * Returns the content of the template. Depending on the chae policy, this
   * method can have diferent implementations
   * @return TemplateContent object that holds the template content.
   * @throws IOException Exception getting access to the template content
   */
  private TemplateContent getCachedContent() throws IOException {
    Debug.check(this.cache != null, "Template content must not be null");
    return this.cache.getContent();
  }

  /**
   * Writes the template content, with the new actual content of the
   * model section, in the <code>os</code> OutputStream
   *
   * @param newModel new content for the model section
   * @param os OutputStream to which the template will be written
   * @throws IOException Any error writing the template (or reloading if it is
   *   not in the cache)
   * @throws YSTException Any error processing the template has ocurred.
   * @throws IllegalArgumentException if the given <code>os</code> output stream
   *   is null
   */
  protected void print(String newModel, OutputStream os) throws IOException, YSTException {
    if (os == null)
      throw new IllegalArgumentException("Null OutputStream are not allowed");

    Debug.prec(newModel, "NewModel can not be null nor empty");  // Nunca se dará el caso
    Debug.check(transformers != null, "The template has not got transformers");

    TemplateContent template = getCachedContent(); // It may reload the template
    if (template != null) {
      byte[] designVer = template.getDesignerVersion();
      if (template.isTemplate()) {
        os.write(designVer, 0, template.getMODELInit());
        os.write(PROCESSING_STAMP.getBytes());
        String encoding = this.getTemplateEncoding();
        if (encoding != null) {
          // To consider the € (euro) char, not included in ISO-8859-1, ISO-8859-9
          encoding = encoding.equalsIgnoreCase("ISO8859_1") ? "Cp1252" : encoding;
          encoding = encoding.equalsIgnoreCase("ISO8859_9") ? "Cp1254" : encoding;
          os.write(newModel.getBytes(encoding));
        } else { // Uses the default encoding
          os.write(newModel.getBytes());
        }
        os.write(designVer, template.getMODELEnd(), designVer.length - template.getMODELEnd());
        os.flush();
        Debug.fine(LOGGER_NAME, "Printed template " + this.id + " with new model: " + newModel);
      } else {
        Debug.warning(LOGGER_NAME, "Template "+this+" has not got a model section");
        os.write(designVer);
        os.flush();
        Debug.fine(LOGGER_NAME, "Printed template desinger's version. New model not included");
      }
    } else {
      throw new IOException("Unreachable template");
    }
  }

  /**
   * Writes the template content, with the new actual content of the
   * model section, in the <code>os</code> OutputStream. The new data
   * for the model section is extracted from the objects contained in
   * <code>data</code>.
   *
   * @param data objects that will be used to generate the new content of the
   *   model section. If some of the objects is null, then it will not be
   *   transformed. Only a JavaScript comment (<code>// Null data
   *   skipped</code>) will be inserted instead.
   * @param os OutputStream to which the template will be written. It can not be null
   * @throws IOException Any error writing the template (or reloading if it is
   *   not in the cache)
   * @throws ConfigurationException if some of the associated transformers
   *   returns <code>null</code> in its
   *   {@link org.ystsrv.Transformer#transformedClass} method
   * @throws YSTException Any error processing the template has ocurred.
   * @throws IllegalArgumentException if the given <code>os</code> output stream
   *   is null
   */
  public void print(Object data[], OutputStream os)
      throws IOException, YSTException, ConfigurationException {
    ModelSection dD = transformers.transform(data);
    this.print(dD, os);
  }

  /**
   * Writes the template content, with the new actual content of the model
   * section, in the <code>os</code> OutputStream. The new data for the model
   * section is extracted from the objects contained in <code>data</code>.
   *
   * @param data objects that will be used to generate the new content of
   *   the model section
   * @param os OutputStream to which the template will be written. It can not
   *   be null
   * @throws IOException Any error writing the template (or reloading if it is
   *   not in the cache)
   * @throws ConfigurationException if some of the associated transformers
   *   returns <code>null</code> in its {@link
   *   org.ystsrv.Transformer#transformedClass} method
   * @throws YSTException Any error processing the template has ocurred.
   * @throws IllegalArgumentException if the given <code>os</code> output
   *   stream is null
   */
  public void print(Collection data, OutputStream os)
      throws IOException, YSTException, ConfigurationException, IllegalArgumentException {
    ModelSection dD = transformers.transform(data);
    this.print(dD, os);
  }

  /**
   * Writes the template content, with the new actual content of the
   * model section, in the <code>os</code> OutputStream. The new data
   * for the model section is encapsulated in <code>newModel</code>, an
   * object of type <code>ModelSection</code>.
   *
   * @param newModel <code>ModelSection</code> object encapsulating the new
   *   content of the model section. It can not be null
   * @param os OutputStream to which the template will be written. It can not be null
   * @throws IOException Any error writing the template (or reloading if it is
   *   not in the cache)
   * @throws YSTException Any error processing the template has ocurred.
   * @throws IllegalArgumentException if the given <code>ModelSection</code> is
   *   null; if the given <code>os</code> output stream is null
   */
  public void print(ModelSection newModel, OutputStream os)
      throws IOException, IllegalArgumentException, YSTException {
    if (newModel == null)
      throw new IllegalArgumentException("Null newModel are not allowed");
    manageYeipeeStatus(newModel);

    this.print(newModel.getScriptData(), os);

  }

  private static void manageYeipeeStatus(ModelSection newModel) {
    String yeipeeAdding = null;
    int yeipeeStatus = ClientYeipeeStatus.getStatus();
    Debug.info(LOGGER_NAME, "Reading yeipee status "+ClientYeipeeStatus.printStatus(yeipeeStatus));
    switch (yeipeeStatus) {
      case ClientYeipeeStatus.DISABLE_YEIPEE_ON_CLIENT :
        yeipeeAdding = "YST.Acsbl.enable = false;";
        break;
      case ClientYeipeeStatus.NOT_YEIPEE_AND_SEND_OFF :
      case ClientYeipeeStatus.YEIPEE_AND_SEND_OFF :
        break;
      case ClientYeipeeStatus.YEIPEE_AND_SEND_ON :
        yeipeeAdding = "YST.Acsbl.yeipeeParam.value = 1;";
        break;
    }
    if (yeipeeAdding != null) {
      newModel.appendLine("if (YST.Acsbl) {");
      newModel.appendLine(yeipeeAdding);
      newModel.appendLine("}");
    }
  }

  /**
   * <code>Template</code> clients can use this method to obtain a representation of the
   * objects contained in <code>data</code> array adapted to the format required by the
   * template in its model section.
   *
   * @param data array with objects that, once transformed, will be included in
   *   the model section. If some of the objects is null, then it will not be
   *   transformed. Only a JavaScript comment (<code>// Null data
   *   skipped</code>) will be inserted instead.
   * @return a <code>ModelSection</code> object encapsulating the representation
   *   of the data in <code>data</code> parameter
   * @see org.ystsrv.ModelSection
   * @throws ConfigurationException if some of the associated transformers
   *   returns <code>null</code> in its
   *   {@link org.ystsrv.Transformer#transformedClass} method
   * @throws TransformationException Any exception thrown in the
   *   {@link org.ystsrv.Transformer#transform} method of any of
   *   the member transformers this object holds
   */
  public ModelSection makeModel(Object data[])
      throws TransformationException, ConfigurationException {
    Debug.check(transformers != null, "The template has not got transformers");
    return transformers.transform(data);
  }

  /**
   * <code>Template</code> clients can use this method to obtain a
   * representation of the objects contained in <code>data</code> collection
   * adapted to the format required by the template in its model section.
   *
   * @param data java.util.Collection with objects
   * @return a <code>ModelSection</code> object encapsulating the representation
   *   of the data in <code>data</code> parameter
   * @see org.ystsrv.ModelSection
   * @throws ConfigurationException if some of the associated transformers
   *   returns <code>null</code> in its
   *   {@link org.ystsrv.Transformer#transformedClass} method
   * @throws TransformationException Any exception thrown in the
   *   {@link org.ystsrv.Transformer#transform} method of any of
   *   the member transformers this object holds
   */
  public ModelSection makeModel(Collection data)
      throws TransformationException, ConfigurationException {
    Debug.check(transformers != null, "The template has not transformers");
    return transformers.transform(data);
  }

  private void setTransformers(TransformerGroup transfomers) {
    Debug.prec(transfomers, "transformers can not be null");
    this.transformers = transfomers;
  }

  public String toString() {
    return this.id+" ("+this.source+")";
  }
}
