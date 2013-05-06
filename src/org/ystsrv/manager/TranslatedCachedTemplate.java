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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;

import org.w3c.dom.Document;
import org.ystsrv.debug.Debug;
import org.ystsrv.util.InMemoryCachedReference;

/**
 * Specialization of {@link CachedTemplate} used to translate Yeast Templates.
 * It is designed for templates to be transformed from the version that uses Yeast
 * attributes to the version that uses calls to JavaScript functions that write
 * the HTML document content.
 *
 * <p> Templates are first converted and then stored in a temporal file. The
 * converted version is holded by a weak reference (that acts as the actual
 * cache). If there is a new version of the original template, the template is
 * reloaded and re-translated. If the weak reference is garbage collected, but
 * the original version is still the same, the content is reloaded from the
 * temporal file. Therefore, there is a two level cache.
 *
 * @author Francisco José García Izquierdo
 * @version 1.0
 */
class TranslatedCachedTemplate extends CachedTemplate {

  // Name of the file containing the translated template
  protected File internalFile;

  protected int modelInit;
  protected int modelEnd;

  protected TranslatedCachedTemplate() {

  }

  TranslatedCachedTemplate(String id, TemplateSource source) throws IOException {
    Debug.prec(source);
    Debug.prec(id);
    this.templateId = id;
    this.source = source;

    if (Config.TRANSLATED_TEMPLATES_DIR != null) {
      String fileName = id.substring(1) + java.util.UUID.randomUUID();
      this.internalFile = new File(Config.TRANSLATED_TEMPLATES_DIR + '/' + fileName + ".html.tmp");
      this.internalFile.deleteOnExit();
    }
    this.rContent = InMemoryCachedReference.newInstance(init());
  }

  /**
   * Translate the template.
   * @return TemplateContent
   * @throws IOException
   */
  protected TemplateContent init() throws IOException {
    lastLoad = this.source.getLastModifiedTime();

    byte[] initialContent = TemplateUtils.readTemplate(this.source.getInputStreamToTemplate());

    this.charSetEncoding = TemplateUtils.guessCharEncoding(initialContent);

    byte [] translatedContent = null;

    YSTTranslator translator = new YSTTranslator(true, false, true, this.charSetEncoding);

    try {
      Document pTemplDoc = translator.translate(new ByteArrayInputStream(initialContent));
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      translator.pprint(pTemplDoc, os);
      translatedContent = os.toByteArray();
    } catch (TranslatingException ex) {
      Debug.error(LOGGER_NAME, "Error translating template " + this.templateId, ex);
      IOException io = new IOException("Error translating template " + ex.getMessage());
      io.initCause(ex);
      throw io;
    }

    if (this.internalFile != null) {
      storeTmpTemplate(translatedContent);
    }
    int[] bounds = TemplateUtils.findModelSectionBounds(translatedContent);
    this.modelInit = bounds[0];
    this.modelEnd = bounds[1];

    return new TemplateContent(translatedContent, bounds[0], bounds[1]);
  }

  protected void storeTmpTemplate(byte[] content) throws IOException {
    store(content, this.internalFile);
    Debug.fine(LOGGER_NAME,
               "Writing translated template " + this.templateId + ": " +
               this.internalFile.getAbsolutePath());
  }

  protected void store(byte[] content, File dest) throws IOException {
    Debug.prec(content);
    File dir = new File(Config.TRANSLATED_TEMPLATES_DIR);
    if (!dir.exists()) {
      Debug.fine(LOGGER_NAME,
                 "Initializing temporal directory for translated templates: " +
                 dir.getAbsolutePath());
      dir.mkdirs();
    }
    FileOutputStream wr = new FileOutputStream(dest);
    wr.write(content);
    wr.close();
  }

  public synchronized TemplateContent getContent() throws IOException {
    Debug.prec(this.rContent);
    TemplateContent content = null;
    if (hasNewVersion()) {
      Debug.info(LOGGER_NAME,
                 "New version of template " + this.templateId + ". Reloading template from " +
                 this.source);
      content = init();
      this.rContent = InMemoryCachedReference.newInstance(content);
    } else {
      content = (TemplateContent)this.rContent.get();
      if (content == null) {
        Debug.info(LOGGER_NAME,
                   "Invalid weak reference. Reloading template " + this.templateId + " from " +
                   this.internalFile.getAbsolutePath());
        try {
          if (this.internalFile != null) { //20090720
            content = new TemplateContent(TemplateUtils.readTemplate(
                new FileInputStream(this.internalFile)), this.modelInit, this.modelEnd);
          } //20090720
          else { //20090720
            content = init(); //20090720
          } //20090720
        } catch (IOException ex) {
          Debug.error(LOGGER_NAME,
                      "Error reloading template " + this.templateId + " from " +
                      this.internalFile.getAbsolutePath(), ex);
          content = init();
        }
        this.rContent = InMemoryCachedReference.newInstance(content);
      }
    }
    return content;
  }
}
