package org.ystsrv.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;

import org.w3c.dom.Document;
import org.ystsrv.debug.Debug;
import org.ystsrv.util.InMemoryCachedReference;


public class BSCacheableTranslatedCachedTemplate extends TranslatedCachedTemplate {

  // Name of the file containing the translated template
  private File internalBodyFile;
  private String internalBodyFileName;

  BSCacheableTranslatedCachedTemplate(String id, TemplateSource source) throws IOException {
    Debug.prec(source);
    Debug.prec(id);
    this.templateId = id;
    this.source = source;
    if (Config.TRANSLATED_TEMPLATES_DIR != null) {
      String fileName = id.substring(1) + java.util.UUID.randomUUID();
      internalBodyFileName = Config.getCacheBodyResolverURL()+fileName+".js";
      this.internalFile = new File(Config.TRANSLATED_TEMPLATES_DIR + '/' + fileName + ".html.tmp");
      this.internalFile.deleteOnExit();
      this.internalBodyFile = new File(Config.TRANSLATED_TEMPLATES_DIR + '/' + fileName + ".js.tmp");
      this.internalBodyFile.deleteOnExit();
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

    byte [] translatedContent = initialContent;

    YSTTranslatorCacher translator = new YSTTranslatorCacher(true, false, true, this.charSetEncoding);

    try {
      Document pTemplDoc = translator.translate(new ByteArrayInputStream(initialContent), this.internalBodyFileName);
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      translator.pprint(pTemplDoc, os);
      translatedContent = os.toByteArray();

      if (this.internalFile != null) {
        storeTmpBody(translator.getCachedBody().getBytes(this.charSetEncoding));
        storeTmpTemplate(translatedContent);
      }

      int[] bounds = TemplateUtils.findModelSectionBounds(translatedContent);
      this.modelInit = bounds[0];
      this.modelEnd = bounds[1];

      return new TemplateContent(translatedContent, bounds[0], bounds[1]);
    } catch (TranslatingException ex) {
      Debug.error(LOGGER_NAME, "Error translating template " + this.templateId, ex);
      IOException io = new IOException("Error translating template " + ex.getMessage());
      io.initCause(ex);
      throw io;
    }
  }


  protected void storeTmpBody(byte[] content) throws IOException {
    store(content, this.internalBodyFile);
    Debug.fine(LOGGER_NAME,
               "Writing template body " + this.templateId + ": " +
               this.internalBodyFile.getAbsolutePath());
  }

}
