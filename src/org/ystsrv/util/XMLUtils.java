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
package org.ystsrv.util;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


public class XMLUtils {

  /**
   * Devuelve el contenido textual de un elemento DOM, eliminando espacios en
   * blanco por delante y por detrás.
   *
   * @param elto Element
   * @return String
   */
  public static String getTextTrim(Element elto) {
    StringBuffer content = new StringBuffer();
    NodeList contentE = elto.getChildNodes();
    int i = 0;
    while (contentE.item(i) != null &&
           (contentE.item(i).getNodeType() == Node.TEXT_NODE ||
            contentE.item(i).getNodeType() == Node.CDATA_SECTION_NODE)) {
      content.append( ( (Text) contentE.item(i)).getNodeValue());
      i++;
    }
    return content.toString().trim();
  }

  /**
   * Devuelve el contenido textual de un hijo, de nombre dado, de un elemento
   * DOM, eliminando espacios en blanco por delante y por detrás.
   *
   * @param elto Element
   * @param childName String
   * @return String
   */
  public static String getChildTextTrim(Element elto, String childName) {
    NodeList subEltos = elto.getElementsByTagName(childName);
    if (subEltos == null || subEltos.getLength() == 0)
      return null;
    else
      return getTextTrim((Element)subEltos.item(0));
  }
}
