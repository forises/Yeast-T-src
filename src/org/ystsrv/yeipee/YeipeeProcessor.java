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
package org.ystsrv.yeipee;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mozilla.javascript.yst.Context;
import org.mozilla.javascript.yst.ContextFactory;
import org.mozilla.javascript.yst.Scriptable;
import org.mozilla.javascript.yst.ScriptableObject;
import org.ystsrv.debug.Debug;
import org.ystsrv.manager.CachedTemplate;

public class YeipeeProcessor {
  private static final String LOGGER_NAME = "ystsrv.yeipee";

  private String id; // Template id

  private static String ystEngine;
  private static String sharedEnv;

  //Configure ContextFactory with dinamic scope feature enabled
  static boolean useDynamicScope = true;
  static class MyFactory extends ContextFactory {
    protected boolean hasFeature(Context cx, int featureIndex) {
      if (featureIndex == Context.FEATURE_DYNAMIC_SCOPE) {
        return useDynamicScope;
      }
      return super.hasFeature(cx, featureIndex);
    }
  }

  static {
    ContextFactory.initGlobal(new MyFactory());
    try {
      //load Yeast Engine
      ystEngine = getResourceAsString("org/ystsrv/yeipee/ysttxt.js");

      //load shared environment
      sharedEnv = getResourceAsString("org/ystsrv/yeipee/sharedEnv.js");
    } catch (Exception ex) {
      Debug.error(LOGGER_NAME, "Error initiallizing Yeipee processor context", ex);
    }
  }

  private ScriptableObject sharedScope;
  private List declareFragments;
  private List fragments;

  private static final String MODEL_FRAGMENT_REGEX = "(?s)<script\\s.*yst\\s*=\\s*[\"\']model[\"\']>.*</script>";
  private static final String DECLARE_SCRIPT_FRAGMENT_REGEX = "(?s)<script\\s.*yst\\s*=\\s*[\"\']declare[\"\']>.*</script>";

  public YeipeeProcessor(CachedTemplate ct, String id) throws YeipeeException, IOException {
    this.id = id;
    Debug.info(LOGGER_NAME, "Building Yeipee processor for template "+id);

    //Get a context
    Context context = ContextFactory.getGlobal().enterContext();

    //Initialize the shared scope with standard objects (expensive)
    this.sharedScope = context.initStandardObjects(null, true);

    //evaluate the shared environment
    context.evaluateString(this.sharedScope, sharedEnv, "sharedEnv", 1, null);

    //load Yeast engine in the shared scope
    context.evaluateString(this.sharedScope, ystEngine, "ystEngine", 1, null);

    //Other class initializations
    this.declareFragments = new ArrayList();
    this.fragments = new ArrayList();
    
    loadTemplate(new ByteArrayInputStream(ct.getContent().getDesignerVersion()), ct.getCharsetEncoding());
  }

  private boolean loadTemplate(InputStream is, String charSet) throws YeipeeException {
    try {
      String newTemplate = new String();

      //Buffer template
      BufferedReader br = null;
      try {
        Reader r = new InputStreamReader(is, charSet);
        StringWriter sw = new StringWriter();
        char[] buffer = new char[4096];
        int bytesLeidos;
        while ( (bytesLeidos = r.read(buffer)) != -1) {
          sw.write(buffer, 0, bytesLeidos);
        }
        newTemplate = sw.toString();
      } catch (IOException ex) {
        throw new YeipeeException(ex);
      }

      //Process template
      int processedChars = 0;
      int j = newTemplate.indexOf("<script", processedChars);
      while (j != -1) {
        //add the fragment
        String fragmentContent = newTemplate.substring(processedChars, j);
        this.fragments.add(new Fragment(fragmentContent));

        //search end of current script
        int k = newTemplate.indexOf("</script>", j);
        if (k == -1) {
          throw new YeipeeException(new ParseException("Expected '</script>' closing tag", j));
        }
        int endIndex = k + 8;
        fragmentContent = newTemplate.substring(j, endIndex + 1);

        Fragment fragment = new Fragment(fragmentContent);
        this.fragments.add(fragment);

        if (fragment.isDeclare)  this.declareFragments.add(fragment);

        //search next script
        processedChars = endIndex + 1;
        j = newTemplate.indexOf("<script", processedChars);
      }

      //add the last fragment
      this.fragments.add(new Fragment(newTemplate.substring(processedChars)));

      //evaluate declare scripts in the shared scope
      evaluateListOnScope(this.declareFragments, this.sharedScope);
      //Seal the shared scope
      //this.sharedScope.sealObject();
    } catch (Exception ex) {
      throw new YeipeeException(ex);
    }

    return true;
  }

  public String getProcessedTemplate(String modelSection) throws YeipeeException {
    StringBuffer res = new StringBuffer();
    try {
      //Get a context
      Context context = ContextFactory.getGlobal().enterContext();

      //Get a instance scope and link it to the shared scope
      Scriptable instanceScope = context.newObject(this.sharedScope);
      instanceScope.setPrototype(this.sharedScope);
      instanceScope.setParentScope(null);

      //create output appending fragments
      int i=0;
      Iterator iter = this.fragments.iterator();
      while (iter.hasNext()) {
        Fragment item = (Fragment)iter.next();
        Debug.fine(LOGGER_NAME, "Fragment to process: \n" + item.content + "\n------------------------------------------------------------------");
        if (item.isModel) {
          try {
            //It's the model section fragment. Evaluate the new model in the instance scope ...
            if (modelSection.startsWith("<script")) modelSection = removeScriptTags(modelSection);
            Debug.fine(LOGGER_NAME, "Evaluating new model section: " + modelSection);
            context.evaluateString(instanceScope, modelSection, "modelSection", 1, null);
            // ... and append the new model section instead of template test model
            String newModelSection = "<script yst=\"model\">" + modelSection + "</script>";
            res.append(newModelSection);
            Debug.fine(LOGGER_NAME, "It's model section. Appending new model section: " + newModelSection);
          } catch (Exception ex) {
            res.append("--ERROR EVALUATING NEW MODEL SECTION--" + ex.toString());
          }
        } else if (item.isYSTExecutable) {
          try {
              //It's a Yeast-script. append the result of processing it.
            context.evaluateString(instanceScope, "var res = " + item.getExecutableContent(), "", 1, null);
            String fragmentRes = instanceScope.get("res", instanceScope).toString();
            Debug.fine(LOGGER_NAME, "It's Yeast-script: " + item.getExecutableContent() + "\nAppending processed Yeast code: " + fragmentRes);
            res.append(fragmentRes);
          } catch (Exception ex) {
            res.append("--ERROR GETTING CONTENT--" + ex.toString());
          }
        } else if (item.isOtherScriptExecutable) {
          try {
            //It's a non-Yeast script, evaluete it and ...
            Debug.fine(LOGGER_NAME, "Evaluating JavaScript: " + item.getExecutableContent());
            context.evaluateString(instanceScope, item.getExecutableContent(), "command_" + (i++), 1, null);
          } catch (Exception ex) {
            // Ignore errors. Probably due to lack of Rhino support to objects like window ...
            //ex.printStackTrace();
          }
          //... append it "as is".
          res.append(item.content);
          Debug.fine(LOGGER_NAME, "It's non-Yeast script. Appending it.");

        } else {
            //It's a non-executable script or HTML Fragment. append it "as is".
            res.append(item.content);
            Debug.fine(LOGGER_NAME, "It's a non-executable script or HTML. Appending it.");
        }
      }
      //Exit the context
      context.exit();
    } catch (Exception ex) {
      throw new YeipeeException(ex);
    }
    return res.toString();
  }


  // Clase fragment optimizada pra no tener que duplicar el contenido (raw) y el ejecutable
  private static class Fragment {
    String content;
    boolean isModel;
    boolean isDeclare;
    boolean isYSTExecutable;
    boolean isOtherScriptExecutable;
    int startExec = -1, endExec = -1;

    Fragment(String content) throws ParseException {
      this.content = content;
      if (content.matches(MODEL_FRAGMENT_REGEX)) {
        Debug.fine(LOGGER_NAME, "Found model fragment: " + content);
        this.isModel = true;
      } else if (content.matches(DECLARE_SCRIPT_FRAGMENT_REGEX)) {
        Debug.fine(LOGGER_NAME, "Found declare fragment: " + content);
        removeScriptTags();
        this.isDeclare = true;
        isYSTExecutable = false;
      } else if (content.startsWith("<script")) {
        removeScriptTags();
        int i = content.indexOf("//<![CDATA", this.startExec);
        if (i != -1) {
          this.startExec = i + 11;
          this.endExec = content.lastIndexOf("//]]>");
        }
        i = content.indexOf("document.write(YST.Txt",this.startExec);
        if (i!=-1) {
          Debug.fine(LOGGER_NAME, "Found Yeast-content-script fragment: " + content);
          this.isYSTExecutable = true;
          this.startExec = i + 14; // + "document.write".length()
        } else {
          i = content.indexOf("if (typeof YST != 'undefined')",this.startExec);
          if (i!=-1) {
            Debug.fine(LOGGER_NAME, "Found final-Yeast-script fragment: " + content);
            this.isYSTExecutable = false;
            startExec = -1; endExec = -1;
          } else {
            Debug.fine(LOGGER_NAME, "Found other-script fragment: " + content);
            this.isOtherScriptExecutable = content.substring(startExec,endExec).trim().length()>0; // Si hay algo que ejecutar se ejecutara
          }
        }
      } else {
        Debug.fine(LOGGER_NAME, "Found no-script fragment: " + content);
      }
    }

    public String getExecutableContent() {
      if (isYSTExecutable || isOtherScriptExecutable || isDeclare)
        return content.substring(startExec,endExec);
      else return "";
    }


    private void removeScriptTags() throws ParseException {
      if (!content.startsWith("<script")) {
        throw new ParseException("expected <script in the begining of element expression", 1);
      }
      int openingTagEndIndex = content.indexOf(">");
      if (openingTagEndIndex == -1) {
        throw new ParseException("expected > as delimiter of opening tag "+content, content.length());
      }

      if (!content.substring(openingTagEndIndex - 1, openingTagEndIndex).equals("/")) {
        int closingTagStartIndex = content.indexOf("</script>", openingTagEndIndex);
        if (closingTagStartIndex == -1) {
          throw new ParseException("expected </script> closing tag "+content, content.length());
        }
        this.startExec = openingTagEndIndex + 1;
        this.endExec = closingTagStartIndex;
      }
    }
  }

  private static String removeScriptTags(String element) throws ParseException {
    String res = "";

    if (!element.startsWith("<script")) {
      throw new ParseException("expected <script in the begining of element expression", 1);
    }
    int openingTagEndIndex = element.indexOf(">");
    if (openingTagEndIndex == -1) {
      throw new ParseException("expected > as delimiter of opening tag "+element, element.length());
    }

    if (!element.substring(openingTagEndIndex - 1, openingTagEndIndex).equals("/")) {
      int closingTagStartIndex = element.indexOf("</script>", openingTagEndIndex);
      if (closingTagStartIndex == -1) {
        throw new ParseException("expected </script> closing tag "+element, element.length());
      }
      res = element.substring(openingTagEndIndex + 1, closingTagStartIndex);
    }

    return res;
  }

  private static String getResourceAsString(String resource) throws IOException {
    String res = "";
    BufferedReader br = null;

    //try to get the resource from classpath
    InputStream resource_is = YeipeeProcessor.class.getResourceAsStream("/" + resource);
    if (resource_is != null) {
      br = new BufferedReader(new InputStreamReader(resource_is));
    } else {
      //try to get the resource in standard form
      File f = new File(resource);
      if (f.exists()) {
        br = new BufferedReader(new FileReader(f));
      } else {
        throw new FileNotFoundException(resource);
      }
    }

    //read resource
    String line = "";
    while ( (line = br.readLine()) != null) {
      res += line + "\r\n";
    }
    br.close();

    return res;
  }

  private void evaluateListOnScope(List commands, Scriptable scope) {
    //Get a context
    Context context = ContextFactory.getGlobal().enterContext();

    int i = 0;
    Iterator iter1 = commands.iterator();
    while (iter1.hasNext()) {
      Fragment item = (Fragment)iter1.next();
      Debug.fine(LOGGER_NAME, "Evaluating JavaScript: " + item.getExecutableContent());
      context.evaluateString(scope, item.getExecutableContent(), "command_" + (i++), 1, null);
    }

    //Exit the context
    context.exit();
  }

}
