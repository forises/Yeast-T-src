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
 * Base class for template content caches. Stores the content of the template in
 * a weak reference taking care of reloading the template if there is a new
 * version of it or if the weak reference is garbage collected.
 *
 * @author Francisco José García Izquierdo
 * @version 1.0
 */
abstract public class CachedTemplate {

  protected static final String LOGGER_NAME = "ystsrv.manager";

  // The content is cached using a weak reference
  protected InMemoryCachedReference rContent;

  protected TemplateSource source;

  // Time of thas load of the template. Used to determine if the template
  // source code has changed (and to reload the template consequently)
  protected long lastLoad;

  protected String templateId;

  // charSet encoding used in the template
  protected String charSetEncoding;

  protected CachedTemplate() {
  }

  /**
   * CachedTemplate
   */
  protected CachedTemplate(String id, TemplateSource source) throws IOException {
    Debug.prec(source);
    Debug.prec(id);
    this.templateId = id;
    this.source = source;
  }

  /**
   * Returns the encoding used in the template. It is based on the value
   * provided in the content-type meta tag of the HTML template. If this tag is
   * not present or it is malformed, the returned value is the JVM default
   * encoding.
   * @return String
   */
  public String getCharsetEncoding() {
    return charSetEncoding;
  }

  /**
   * Returns the content of the template ({@link TemplateContent}} The cache
   * strategy is implemented here.
   * @return String
   * @throws IOException
   * @see TemplateContent
   */
  public abstract TemplateContent getContent() throws IOException;

  /**
   * Determine if there is a new version of the template. If it is so, the
   * template will be reloaded
   *
   * @return boolean
   */
  protected boolean hasNewVersion() {
    if (lastLoad > 0)
      return lastLoad < this.source.getLastModifiedTime();
    else
      return false; // will not be reloaded
  }
}
