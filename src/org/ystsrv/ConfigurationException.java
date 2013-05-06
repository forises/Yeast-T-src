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
 * Exception class used to report <code>Template</code> configuration
 * problems, e.g. format problems of the <code>YSTConfig.xml</code> file,
 * transformer classes that can not be found (usually due to a bad name
 * specification in the <code>YSTConfig.xml</code>), missing required
 * transformer parameters in the <code>YSTConfig.xml</code> file or bad order of
 * classes in the construction of the TransformerGroup for a template (see
 * {@link org.ystsrv.transformer.TransformerGroup}.
 *
 * @author Francisco José García Izquierdo
 * @version 1.0
 * @see org.ystsrv.transformer.TransformerGroup
 */
public class ConfigurationException extends RuntimeException {
  public ConfigurationException() {
    super();
  }

  public ConfigurationException(String msg) {
    super(msg);
  }

  public ConfigurationException(Throwable ex) {
    super(ex);
  }

  public ConfigurationException(String msg, Throwable ex) {
    super(msg, ex);
  }

}
