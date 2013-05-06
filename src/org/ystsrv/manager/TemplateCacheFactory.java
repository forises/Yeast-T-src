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
import java.util.HashMap;
import java.util.Map;

import org.ystsrv.debug.Debug;
import org.ystsrv.yeipee.ClientYeipeeStatus;

/**
 * Provides the suitable {@link CachedTemplate} objects, based on the
 * Yeast-Server configuration.
 *
 * @author Francisco José García Izquierdo
 * @version 2.0
 */
public class TemplateCacheFactory {

  protected static final String LOGGER_NAME = "ystsrv.manager";

  private static Map cachedTemplatesReferences = new HashMap();

  public static CachedTemplate buildCache(String templateId, TemplateSource source) throws IOException {
    int yeipeeStatus = ClientYeipeeStatus.getStatus();
    char prefix;
    if (ClientYeipeeStatus.mustYeipee(yeipeeStatus))
      prefix = 'T';
    else {
      if (Config.MUST_BROWSER_SIDE_CACHE) {
        prefix = 'C';
      } else {
        if (Config.MUST_TRANSLATE_TEMPLATES)
          prefix = 'T';
        else {
          prefix = 'B';
        }
      }
    }

    String internalId = prefix+"_"+source.getStoreName()+"_"+templateId;
    CachedTemplate cached = (CachedTemplate)cachedTemplatesReferences.get(internalId);
    String mode = "";
    switch (prefix) {
      case 'T' : mode = "translated";
                 break;
      case 'C' : mode = "Browser-sice cacheable";
                 break;
      case 'B' : mode = "basic";
    }
    if (cached == null) {
      switch (prefix) {
        case 'T' : cached = new TranslatedCachedTemplate(templateId, source);
                   break;
        case 'C' : cached = new BSCacheableTranslatedCachedTemplate(templateId, source);
                   break;
        case 'B' : cached = new BasicCachedTemplate(templateId, source);
      }

      cachedTemplatesReferences.put(internalId,cached);
      Debug.info(LOGGER_NAME, "Template "+templateId+" for templateStore "+
                              source.getStoreName()+" is now cached in "+
                              mode+" version");

    } else {
      Debug.info(LOGGER_NAME, "Template "+templateId+" for templateStore "+
                              source.getStoreName()+" has been already cached in "+
                              mode+" version");
    }
    return cached;
  }

}
