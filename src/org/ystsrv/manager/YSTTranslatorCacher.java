/*
 *  Yeast-Server for Java
 *
 *  Copyright (c) 2011. Francisco Jose Garcia Izquierdo. University of La
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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.ystsrv.debug.Debug;
import org.ystsrv.util.TextUtils;
import org.ystsrv.util.XMLUtils;


public class YSTTranslatorCacher extends YSTTranslator{
  private String cachedBodyFunctionName = null;
  private NamedNodeMap oldBodyAttrs = null; // Los guardo para recuperarlos despues si se cacheo la plantilla
  private String templateBody;

  public YSTTranslatorCacher() {
    super();
  }

  public YSTTranslatorCacher(boolean hideYSTAttrs, boolean verbose, boolean hideErrors) {
    super(hideYSTAttrs, verbose, hideErrors);
  }

  public YSTTranslatorCacher(boolean hideYSTAttrs, boolean verbose, boolean hideErrors, String encoding) {
    super(hideYSTAttrs, verbose, hideErrors, encoding);
  }



  public Document translate(InputStream yst, String bodyFileName) throws TranslatingException, IOException {
    Debug.fine(LOGGER_NAME, "Translating template using "+this.encoding+" encoding");
    this.tidy.setInputEncoding(this.encoding);

    if (verbose) {
      System.out.println("INFO: Parsing template using Tidy HTML.");
      System.out.println();
    }


    Document ystDoc = tidy.parseDOM(yst, null); // null no pprint the result

    return doIt(ystDoc, bodyFileName);
  }

  public Document translate(String ystAbsFile, String bodyFileName) throws TranslatingException, IOException {
    this.encoding = "ISO-8859-1"; // por defecto supongo este encoding
    InputStream yst = new FileInputStream(ystAbsFile);

    if (verbose) {
      System.out.println("INFO: Parsing template using Tidy HTML.");
      System.out.println();
    }

    Document ystDoc = tidy.parseDOM(yst, null);
    this.encoding = findEncoding(ystDoc);
    if (!this.encoding.equalsIgnoreCase("ISO-8859-1") ) {
      this.tidy.setInputEncoding(this.encoding);
      if (!hideErrors) {
        PrintWriter errWr = new PrintWriter(new StringWriter());
        this.tidy.setErrout(errWr);
      }
      yst.close();
      yst = new FileInputStream(ystAbsFile);
      ystDoc = tidy.parseDOM(yst, null);
    }
    yst.close();

    if (verbose) {
      System.out.println("INFO: Using template encoding: "+this.encoding);
      System.out.println();
    }

    return doIt(ystDoc, bodyFileName);
  }

  private Document doIt(Document ystDoc, String bodyFileName) throws TranslatingException {

    moveElmtsInHead("title",ystDoc);
    moveElmtsInHead("meta",ystDoc);

    Element body = getBody(ystDoc);
    this.oldBodyAttrs = body.getAttributes();

    preProcessDeclares(ystDoc, body);

    cachedBodyFunctionName = putGlobalDeclare(ystDoc);

    makeTemplateFunction(body, false);

    if (verbose) {
      System.out.println("INFO: Translating template.");
      System.out.println();
    }

    translateNode(ystDoc);

    changeBodyToCacheableVersion(ystDoc, bodyFileName);
    this.templateBody = extractBody(ystDoc);
    return ystDoc;
  }


  private String putGlobalDeclare(Document ystDoc) {
    String id = "";
    Element body = getBody(ystDoc);
    if (body != null) {
      body.setAttribute("yst", "declare");
      id = body.getAttribute("id");
      if (id == null || id.trim().length()==0) {
        id = "__TemplateBody";
      }
      body.setAttribute("id", id);
    }
    return id;
  }

  /**
   *
   * @param node Element
   * @param nested boolean indica si el metodo es llamad para un tag anidado o
   *               no. Si es anidado el procesamiento es distinto
   * @param isDeclare indica si se debe generar codigo para ser incluido en la
   *              funcion a la que se traduce un tag declare. En
   *              este caso cambian los params inciales de llamada a la funcion
   * @return String
   * @throws TranslatingException
   */
  protected String explodeYST(Element node, boolean nested, boolean isDeclare) throws TranslatingException {

    String action = YST_Action(node);

    if (verbose) {
      System.out.println("INFO: Translating node "+node.getNodeName()+" ("+action+").");
    }

    String set = node.getAttribute("ystset");
    if (set == null || set.trim().length() == 0) {
      set = node.getAttribute("ystupto");  // 040208
      if (set == null || set.trim().length() == 0) {
        set = "";
      }
    }
    String ystAux = getYSTAuxAttr(node);

    String firstParams = "([], 0, {},";
    if (isDeclare)
      firstParams = "(contextValues,contextI,params,";
    String func = null;
    if (action.equals("ignore")) {
      return (nested ? "''" : null);
    } else if (action.equals("value")) {
      func = nested ? // 160408
          VALUE_F + ",["+ystAux+"['" + getTemplateFromNode(node, false, false) + "']]" :
          VALUE_F + firstParams+ ystAux + "['" + getTemplateFromNode(node, false, false) + "'])";
    } else if (action.equals("if")) {
      String test = getTestAttr(node);
      func = nested ? // 160408
          IF_F + ",["+ ystAux +"'" + TextUtils.escape(test) + "',['" + getTemplateFromNode(node, false, false) + "']]" :
          IF_F + firstParams + ystAux + "'" + TextUtils.escape(test) + "',['" + getTemplateFromNode(node, false, false) + "'])";
    } else if (action.equals("apply")) {
      func = nested ? // 160408
          APPLY_F + ",["+ ystAux +"'" + set + "',['" + getTemplateFromNode(node, false, false) + "']]" :
          APPLY_F + firstParams+ ystAux + "'" + set + "',['" + getTemplateFromNode(node, false, false) + "'])";
    } else if (action.equals("compapply")) {
      // Get the rest of compApply nodes that are sibling of the node
      List compApplys = new ArrayList();
      compApplys.add(node);
      Node nS = node.getNextSibling();
      while (nS != null) {
        String act = YST_Action(nS);
        if (act != null && act.equals("compapply")) {
          changeYST_Action( (Element)nS, "ignore"); // changes the yst value in order the nodes not being processed again
          compApplys.add(nS);
        }
        nS = nS.getNextSibling();
      }
      func = nested ? COMPAPPLY_F + ", ['" + set + "'" :
          COMPAPPLY_F + firstParams + "'" + set + "'";

      for (int i = 0; i < compApplys.size(); i++) {
        Element cA = (Element)compApplys.get(i);
        String test = getTestAttr(cA);
        func += nested ?
            ",'" + TextUtils.escape(test) + "',"+getYSTAuxAttr(cA)+"['" + getTemplateFromNode(cA, false, false) + "']" :
            ",'" + TextUtils.escape(test) + "',"+getYSTAuxAttr(cA)+"['" + getTemplateFromNode(cA, false, false) + "']";
      }
      func += nested ? "]" : ")";
    } else if (action.equals("declare")) {
      return (nested ? "''" : null); //130209
    } else if (action.equals("include")) {
      func = makeIncludeCall(node, nested, firstParams, ystAux);
    } else if (action.equals("ajax") || action.equals("live")) {
      String funcName = node.getAttribute("id").trim();
      func =  VALUE_F + firstParams + ystAux +
          "['" +  node2Str(node) + "',"+(funcName != null?funcName+",[]":"''")+",'</" + node.getNodeName().toLowerCase() + ">'" + "])";
    } else if (action.equals("literal")) { // 061008
      func = nested ?
          LITERAL_F + ",["+ ystAux +"['" + getTemplateFromNode(node, false, false) + "']]" :
          LITERAL_F + firstParams + ystAux + "['" + getTemplateFromNode(node, false, false) + "'])";
    } else {
      ByteArrayOutputStream st = new ByteArrayOutputStream();
      tidy.pprint(node, st);
      String erroneous = new String(st.toByteArray());
      throw new TranslatingException("Illegal yst attibute value:" + action + " in node\n-------\n"+erroneous+"\n-------\n");
    }
    return func;
  }




  private void changeBodyToCacheableVersion(Document doc, String bodyFile) {
    Element newBody = doc.createElement("body");
    for (int i = 0; i < this.oldBodyAttrs.getLength(); i++) {
      Node atr = this.oldBodyAttrs.item(i);
      String name = atr.getNodeName();
      if (!name.equals("yst"))
        newBody.setAttribute(name, atr.getNodeValue());
    }
    Node parent = doc.getDocumentElement();
    parent.appendChild(newBody);


    Element script1 = createScriptNode(doc);
    script1.setAttribute("src", bodyFile);
    Text scriptText1 = doc.createTextNode("// Move the .js file where it sould be, and change src attribute ");
    script1.appendChild(scriptText1);
    newBody.appendChild(script1);

    putProcessedMark(doc);
  }

  private Element getBody(Document doc) {
    NodeList bodys = doc.getElementsByTagName("body");
    if (bodys != null && bodys.getLength()>0)
      return (Element)bodys.item(0);
    else
      return null;
  }

  private void preProcessDeclares(Document ystDoc, Element element) throws TranslatingException {
    if (element.hasChildNodes()) {
      NodeList children = element.getChildNodes();
      for (int i = 0; i<children.getLength();i++) {
         Node n = children.item(i);
         if (n.getNodeType() == Node.ELEMENT_NODE)
           preProcessDeclares(ystDoc, (Element)n);
      }
    }
    String action = YST_Action(element);

    if (action.equals("declare") || action.equals("ajax") || action.equals("live")) {
      makeTemplateFunction(element, false);
    }
  }

  private String extractBody(Document doc) {
    String body = "";
    NodeList scripts = doc.getElementsByTagName("script");
    for (int i = scripts.getLength()-1;i>=0;i--) {
      Element script = (Element)scripts.item(i);
      if (script.getAttribute("yst").equals("declare")) {
        body += "\n"+XMLUtils.getTextTrim(script);
        Node parent = script.getParentNode();
        parent.removeChild(script);
      }
    }
    body += "\ndocument.write("+this.cachedBodyFunctionName+"([], 0, {}));";
    return body;

  }


  String getCachedBody() {
    return this.templateBody;
  }


  public void storeToFiles(Document doc, OutputStream templOut, OutputStream bodyOut) throws IOException {
    bodyOut.write(this.templateBody.getBytes(this.encoding));
    this.pprint(doc, templOut);
  }
}
