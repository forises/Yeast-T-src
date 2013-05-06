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
package org.ystsrv;

/**
 * This class encapsulates the dynamic content that will be inserted in the
 * model section of a Yeast Template. Usually it is produced by the {@link
 * org.ystsrv.transformer.TransformerGroup#transform} method, but
 * you can build a <code>ModelSection</code> object by your own. Once you have
 * the <code>ModelSection</code> object, you can add to it any textual data you
 * want using the {@link #append} method and similars. The text appended in that
 * way will be inserted directly in the model section with no transformation.
 * Once you have a <code>ModelSection</code> you can print the template to the
 * client's browser using {@link Template#print(ModelSection,
 * java.io.OutputStream)} (other versions of the <code>print</code> method, deal
 * directly with the data objects, and avoid you the use of
 * <code>ModelSection</code>).
 *
 * <p>You can get the represented model section content using a couple of
 * methods: {@link #getData} and {@link #getScriptData}. The latter will return
 * the model section enclosed by a couple of
 * <code>&lt;script&gt;...&lt;/script&gt;</code> tags.
 *
 * @author Francisco José García Izquierdo
 * @version 1.0
 * @see org.ystsrv.transformer.TransformerGroup#transform
 * @see org.ystsrv.Template#print(org.ystsrv.ModelSection,java.io.OutputStream)
 * @see org.ystsrv.servlet.YSTContext#toResponse(String)
 * @see org.ystsrv.servlet.YSTContext#getModelSection
 */
public class ModelSection {
  private StringBuffer dataStr = new StringBuffer();
  /**
   * Adds to this <code>ModelSection</code> object some textual
   * <code>data</code>. The appended text will be inserted directly in
   * the model section with no transformation.
   *
   * @param data String. If it is null no data will be added
   */
  public void append(String data) {
    if (data != null)
      dataStr.append(data);
  }

  /**
   * Adds to this <code>ModelSection</code> object some textual
   * <code>data</code> followed by a new line character (<code>\n</code>). The
   * appended text will be inserted directly in the model section with no
   * transformation.
   *
   * @param data String. If it is null no data will be added
   */
  public void appendLine(String data) {
    if (data != null)
      dataStr.append(data + "\n");
  }

  /**
   * If the <code>extraContent</code> is not empty, adds the whole content
   * encapsulated by <code>extraContent</code> parameter to this object content,
   * separating the old conten from the new one with a new line character
   * (<code>\n</code>).
   *
   * @param extraContent ModelSection
   */
  public void append(ModelSection extraContent) {
    if (!this.isEmpty() && !extraContent.isEmpty())
      dataStr.append("\n");

    dataStr.append(extraContent.getData());
  }

  /**
   * Returns the text of the model section that is
   * represented by this object, enclosed in a couple of
   * <code>&lt;script&gt;...&lt;/script&gt;</code> tags
   *
   * @return String
   */
  public String getScriptData() {
    return "<script type=\"text/javascript\">\n//<![CDATA[\n" + dataStr.toString() + "//]]>\n</script>";
  }

  /**
   * Returns the text of the model section that is
   * represented by this object.
   *
   * @return String
   */
  public String getData() {
    return dataStr.toString();
  }

  /**
   * Returns trues if the model section objet does not contain any data.
   * @return boolean
   */
  public boolean isEmpty() {
    return this.dataStr.length()==0;
  }

  /**
   * Returns the same than {@link #getScriptData}
   * @return String
   */
  public String toString() {
    return getScriptData();
  }

}
