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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import org.ystsrv.debug.Debug;

/**
 * Based on the content of the <code>yst.properties</code> file.
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class Config {
  private static final String LOGGER_NAME = "ystsrv";
  public static String contextName = null;
  public static String getCacheBodyResolverURL() {
    return contextName+"/BodyCacheLoader?id=";
  }

  public static boolean MUST_BROWSER_SIDE_CACHE = false;

  /**
   * Must the templates be transformed form the DOM version to the text version
   * befored be sent to the client browser?
   */
  public static boolean MUST_TRANSLATE_TEMPLATES = false;

  /**
   * Must the templates be processed on server using Yeipee processor
   * before be sent to the client browser?
   */
  public static boolean MAY_PROCESS_ON_SERVER = false;

  /**
   * Name of the default template store (if no property is specified
   * this will be the 'yst' directory)
   */
  public static String DEFAULT_TEMPLATE_STORE_NAME = "yst";

  /**
   * Enable / disable the Yeast server log messages
   */
  public static boolean YST_SERVER_LOGGING = true;

  public static String YST_SERVER_VERSION = "3.0";

  public static boolean USE_SOFT_REFS = false; // Quitar softRefs 20110621

  public static String TRANSLATED_TEMPLATES_DIR; //20090720

  private static Properties props = new java.util.Properties();

  static {
    try { 
      String tDirN = System.getProperty("java.io.tmpdir");
      if (tDirN == null || tDirN.trim().length() == 0) {
        tDirN = System.getProperty("user.home");
      }
      if (tDirN == null || tDirN.trim().length() == 0) {
        tDirN = System.getProperty("user.dir");
      }
      if (tDirN == null)
        tDirN = "";

      if (!tDirN.endsWith("/") && !tDirN.endsWith("\\") &&
          !tDirN.endsWith(File.separator))
        tDirN = tDirN + File.separator;
      TRANSLATED_TEMPLATES_DIR = tDirN + ".YSTTemplates.tmp";
      File dir = new File(TRANSLATED_TEMPLATES_DIR);
	  
      dir.mkdirs();
      storeReadmeFile();
    } catch (Exception e) { 
      TRANSLATED_TEMPLATES_DIR = null;
      System.err.println("Impossible to create temporal directory for translated templates.");
      System.err.println("Translated template won't be stored on disk");
    }
    
    String configMsg = "INFO: Yeast-Server started using configuration:";
    try {
      String propsFile = System.getProperty("yst.config.file");
      if (propsFile == null || propsFile.trim().length() == 0)
        propsFile = "/yst.properties";
      InputStream is = Config.class.getResourceAsStream(propsFile);
      if (is != null) {
        props.load(is);

        YST_SERVER_LOGGING = getBooleanProperty("ystsrv.logging", true);

        configMsg +=  "\n Yeast-Server config file: " + propsFile;

        MUST_TRANSLATE_TEMPLATES = getBooleanProperty("manager.translate.templates", false);
        
        MUST_BROWSER_SIDE_CACHE = getBooleanProperty("manager.browser-side.cacheable", false); 

        USE_SOFT_REFS = getBooleanProperty("softReferences", false);

        MAY_PROCESS_ON_SERVER = getBooleanProperty("manager.accessibility.support", false);

        String dts = props.getProperty("manager.default.templateStore");
        if (dts != null)
          dts = dts.trim();
        if (dts != null && dts.length() != 0)
          DEFAULT_TEMPLATE_STORE_NAME = dts;
      } else {
        configMsg +=  "\n Yeast-Server config file: " + null;
      }
    } catch (IOException ioe) {
      configMsg += " default configuration:";
    }
    configMsg += "\n Yeast server logging: " + (YST_SERVER_LOGGING ? "ON" : "OFF") + ";";
    configMsg += "\n Accessibility support (Yeipee processing): " + (MAY_PROCESS_ON_SERVER ? "ON" : "OFF") + ";";
    configMsg += "\n translate templates: " + (MUST_TRANSLATE_TEMPLATES ? "ON" : "OFF") + ";";
    configMsg += "\n browser-side caching: " + (MUST_BROWSER_SIDE_CACHE ? "ON" : "OFF") + ";";
    configMsg += "\n default template store: " + DEFAULT_TEMPLATE_STORE_NAME + ";";
    configMsg += "\n tmp directory for cached and translated templates: " + TRANSLATED_TEMPLATES_DIR + ";";
    
    System.out.println(configMsg);
  }

  private static boolean getBooleanProperty(String propName, boolean defaultVal) {
    boolean prop = defaultVal;
    String propValue = props.getProperty(propName);
    if (propValue != null)
      propValue = propValue.toLowerCase().trim();
    else
      return defaultVal;

    prop = propValue.equals("yes") || propValue.equals("true") || propValue.equals("on");

    return prop;
  }

  private static void storeReadmeFile() {
    String msg = "Folder created by Yeast-Server v. "+Config.YST_SERVER_VERSION+
                 " at "+new Date()+". \n\nDo not remove while Yeast-Server is running.";
    String fileName = "/README.txt";

    FileWriter f = null;
    try {
      f = new FileWriter(TRANSLATED_TEMPLATES_DIR + fileName);
      f.write(msg.toCharArray());
    } catch (IOException ex) {
      Debug.error("Error creating the README file.",ex);
    } finally {
      try {
        if (f != null) f.close();
      } catch (IOException ex1) {
        Debug.error("Error closing the README file.",ex1);
      }
    }
  }
}
