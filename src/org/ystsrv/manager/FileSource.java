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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.ystsrv.debug.Debug;

/**
 * This class represent sources for Yeast Templates located in files stored in
 * the root of the web application.
 *
 * <p>Company: University of La Rioja. Mathematics and Computer Science
 * Department</p>
 *
 * @author Francisco José García Izquierdo
 * @version 1.0
 */
class FileSource extends TemplateSource {

  private static final String LOGGER_NAME = "ystsrv.manager";

  private File file; // file representing the template

  FileSource(String path, String storeName) throws IOException { //250111d añadido storeName
    super(storeName);
    Debug.prec(path, "path can not be null nor empty");
    this.file = new File(path);
  }

  /**
   * {@inheritdoc}
   */
  public long getLastModifiedTime() {
    Debug.check(file, "null file");
    if (this.file.exists()) {
      return this.file.lastModified();
    } else {
      Debug.fine(LOGGER_NAME,"Template in "+this.file+" does not exists");
      return new Date().getTime();
    }
  }

  /**
   * {@inheritDoc}
   */
  public InputStream getInputStreamToTemplate() throws IOException {
    Debug.check(file, "null file");

    FileInputStream fr = new FileInputStream(this.file);
    Debug.fine(LOGGER_NAME, "Returning reader to template in file " + this.file);

    return fr;
  }

  public String toString() {
    return "File "+this.file.getAbsolutePath();
  }
}
