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

/**
 * Stores the content of the template, marking the position of the model section
 * in order to accelerate the template rendering.
 *
 * @author Francisco José García Izquierdo
 * @version 1.0
 */
public class TemplateContent {

  private static final String LOGGER_NAME = "ystsrv.manager";

  private byte[] content;
  private int modelInit;
  private int modelEnd;

  /**
   * Creates a <code>TemplateContent</code> object.
   *
   * @param content byte[] containg all the template text, regardless the
   *                template encoding.
   * @param initMODEL begin position of the model section in
   *   the <code>content</code> string
   * @param endMODEL end position of the model section in
   *   the <code>content</code> string.
   */
  public TemplateContent(byte[] content, int initMODEL, int endMODEL) {
    this.content = content;
    this.modelInit = initMODEL;
    this.modelEnd = endMODEL;
  }

  /**
   * Returns the whole text of the template (including the
   * model section with the original test data)
   *
   * @return byte[]
   */
  public byte[] getDesignerVersion() {
    return content;
  }

  /**
   * Returns the begin position of the model section in the
   * template
   *
   * @return int
   */
  public int getMODELInit() {
    return modelInit;
  }

  /**
   * Returns the end position of the model section in the
   * template
   *
   * @return int
   */
  public int getMODELEnd() {
    return modelEnd;
  }

  /**
   * Returns true if the template contains a model section,
   * and false if the template is a pure HTML file.
   *
   * @return boolean
   */
  public boolean isTemplate() {
    return this.modelInit > 0 && this.modelEnd > 0;
  }
}
