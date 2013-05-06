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
import java.lang.ref.SoftReference;

import org.ystsrv.debug.Debug;
import org.ystsrv.util.InMemoryCachedReference;

/**
 * Stores the content of the template in a weak reference taking care of
 * reloading the template if there is a new version of it or if the weak
 * reference is garbage collected.
 *
 * @author Francisco José García Izquierdo
 * @version 1.0
 */
class BasicCachedTemplate extends CachedTemplate {
  private int modelInit;
  private int modelEnd;

  BasicCachedTemplate(String id, TemplateSource source) throws IOException {
    super(id, source);

    this.rContent = InMemoryCachedReference.newInstance(init());
  }

  private TemplateContent init() throws IOException {
    lastLoad = this.source.getLastModifiedTime();

    byte[] content = TemplateUtils.readTemplate(this.source.getInputStreamToTemplate());

    this.charSetEncoding = TemplateUtils.guessCharEncoding(content);

    int[] bounds = TemplateUtils.findModelSectionBounds(content);
    this.modelInit = bounds[0];
    this.modelEnd = bounds[1];
    return new TemplateContent(content, bounds[0], bounds[1]);
  }

  // Este método puede ser llamado de forma concurrente por varios hilos
  // Usa atributos miembro, que deben sincronizarse
  // Es posible que si no se sincroniza, pueda dar lugar a varias recargas
  // de la misma plantilla, lo cual sería ineficiente
  public synchronized TemplateContent getContent() throws IOException {
    Debug.check(this.rContent != null);
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
        Debug.fine(LOGGER_NAME,
                   "Invalid weak reference. Reloading template " + this.templateId + " from " +
                   this.source + ".");
        content = new TemplateContent(TemplateUtils.readTemplate(this.source.getInputStreamToTemplate()),
                                         this.modelInit, this.modelEnd);
        this.rContent = InMemoryCachedReference.newInstance(content);
      }
    }
    return content;
  }
}
