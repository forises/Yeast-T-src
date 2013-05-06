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
package org.ystsrv.manager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.util.List;
import org.ystsrv.Template;
import org.ystsrv.YSTException;
import org.ystsrv.yeipee.YeipeeException;
import org.ystsrv.debug.Debug;
import org.ystsrv.yeipee.YeipeeProcessor;
import org.ystsrv.util.InMemoryCachedReference;

public class YeipeeTemplate extends Template {

  private static final String LOGGER_NAME = "ystsrv.manager";
  
  private InMemoryCachedReference rYeipeeProcessor;
  
  public YeipeeTemplate(String id, List transformerSpecs, TemplateSource source) throws IOException, YSTException {
    super(id, transformerSpecs, source);
    try {
      createYeipeeProcessor();
    } catch (YeipeeException ex) {
      throw new YSTException(ex);
    }
  }

  protected void print(String newModel, OutputStream os) throws IOException, YSTException {
    if (os == null)
      throw new IllegalArgumentException("Null OutputStream are not allowed");

    Debug.prec(newModel, "NewModel can not be null nor empty"); // Nunca se dar� el caso
    Debug.check(transformers != null, "The template has not got transformers");
    try {
      YeipeeProcessor yp;
      synchronized (this) {
        if (this.cache.hasNewVersion()) {
          yp = createYeipeeProcessor();
        } else {
          yp = (YeipeeProcessor)this.rYeipeeProcessor.get();
          if (yp == null) {
            Debug.info(LOGGER_NAME, "Invalid weak reference. Rebuilding YeipeeProcessor");
            yp = createYeipeeProcessor();
          }
        }
      }

      String encoding = this.getTemplateEncoding();
      String result = yp.getProcessedTemplate(newModel);

      if (encoding != null)
        os.write(result.getBytes(encoding));
      else
        os.write(result.getBytes());
      os.flush();

      Debug.fine(LOGGER_NAME, "Printed template " + this.id + " with new model: " + newModel);

    } catch (YeipeeException ex) {
      throw new YSTException(ex);
    }
  }

  protected synchronized YeipeeProcessor createYeipeeProcessor() throws IOException, YeipeeException {
    if (this.cache == null)
      throw new IllegalStateException("the template must be cached before calling this method");
    YeipeeProcessor yp = new YeipeeProcessor(this.cache, this.id);
    this.rYeipeeProcessor = InMemoryCachedReference.newInstance(yp);
    return yp;
  }
}
