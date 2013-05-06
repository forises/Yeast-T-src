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

package org.ystsrv.manager;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.tidy.yst.CharPrinter;
import org.w3c.tidy.yst.EncodingNameMapper;
import org.w3c.tidy.yst.Tidy;
import org.ystsrv.debug.Debug;
import org.ystsrv.util.TextUtils;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class YSTTranslator {

  protected static final String LOGGER_NAME = "ystsrv.manager";

  protected static final String VALUE_F = "YST.Txt.value";

  protected static final String IF_F = "YST.Txt.iff";

  protected static final String APPLY_F = "YST.Txt.apply";

  protected static final String COMPAPPLY_F = "YST.Txt.select";

  protected static final String INCLUDE_F = "YST.Txt.include";

  protected static final String LITERAL_F = "YST.Txt.literal";

  protected static final String YSTBOOL_F = "YST.Txt.ystBool";

  private static final String DW_F = "document.write";

  private static final String DEFAULT_ENCODING = new OutputStreamWriter(System.out).getEncoding();

  private boolean hideYSTAttrs;
  protected Tidy tidy;
  protected String encoding;  // ** 15-1-2008
  protected boolean verbose = false;
  protected boolean hideErrors = false;

  public YSTTranslator() {
    this(false,false,false);
  }

  public YSTTranslator(boolean hideYSTAttrs, boolean verbose, boolean hideErrors) {
    this.hideYSTAttrs = hideYSTAttrs;
    this.tidy = createTidy(verbose, hideErrors);
    this.encoding = DEFAULT_ENCODING;
    this.verbose = verbose;
    this.hideErrors = hideErrors;
  }

  public YSTTranslator(boolean hideYSTAttrs, boolean verbose, boolean hideErrors, String encoding) {
    this.hideYSTAttrs = hideYSTAttrs;
    this.tidy = createTidy(verbose, hideErrors);
    if (encoding == null) {
      // get the default encoding
      encoding = DEFAULT_ENCODING;
    }
    this.encoding = encoding;

    this.verbose = verbose;
    this.hideErrors = hideErrors;
  }


  /**
   * createTidy
   *
   * @return Tidy
   */
  private static Tidy createTidy(boolean verbose, boolean hideErrors) {
    Tidy t = new Tidy();
    if (verbose)
      t.setShowWarnings(true);
    else {
      t.setShowWarnings(false);
      t.setOnlyErrors(true);
    }

    if (hideErrors) {
      PrintWriter errWr = new PrintWriter(new StringWriter());
      t.setErrout(errWr);
    }


    t.setDropEmptyParas(false);
    t.setDropFontTags(false);
    t.setDropProprietaryAttributes(false);
    t.setTrimEmptyElements(false);
    t.setTidyMark(false);
    t.setDocType("auto");
    return t;
  }

  public Document translate(InputStream yst) throws TranslatingException, IOException {
    Debug.fine(LOGGER_NAME, "Translating template using "+this.encoding+" encoding");
    this.tidy.setInputEncoding(this.encoding);

    if (verbose) {
      System.out.println("INFO: Parsing template using Tidy HTML.");
      System.out.println();
    }


    Document ystDoc = tidy.parseDOM(yst, null); // null no pprint the result
    moveElmtsInHead("title",ystDoc);
    moveElmtsInHead("meta",ystDoc);

    if (verbose) {
      System.out.println("INFO: Translating template.");
      System.out.println();
    }


    translateNode(ystDoc);
    putProcessedMark(ystDoc);
    return ystDoc;
  }

  public Document translate(String ystAbsFile) throws TranslatingException, IOException {
    this.encoding = "ISO-8859-1";
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

    moveElmtsInHead("title",ystDoc);
    moveElmtsInHead("meta",ystDoc);

    if (verbose) {
      System.out.println("INFO: Translating template.");
      System.out.println();
    }


    translateNode(ystDoc);
    putProcessedMark(ystDoc);
    return ystDoc;
  }

  // Al traducir una plantilla, el title y metas suele quedar delante de la insercion
  // del script del motor de yst. Por eso, si el titulo inlcuye instrucciones yst
  // da error
  protected void moveElmtsInHead(String elmtName, Document ystDoc) {
    NodeList heads = ystDoc.getElementsByTagName("head");
    Element head = (Element)heads.item(0);
    if (head != null) {
      List removed = new ArrayList();
      NodeList elmts = head.getElementsByTagName(elmtName);
      // Deben ser eliminados en orden inverso para no afectar al NodeList
      // Luego hay que anadirlos en orden inveso, tb
      for (int i = elmts.getLength()-1; i >= 0; i--) {
        Element e = (Element)elmts.item(i);
        if (isYSTNode(e)) {
          removed.add(head.removeChild(e));
        }
      }
      for (int i = removed.size()-1; i >= 0; i--) {
        Element title = (Element)removed.get(i);
        head.appendChild(title);
      }
    }
  }


  public void pprint(Document html, OutputStream out) {
    this.tidy.setOutputEncoding(this.encoding); // ** 15-1-2008
    this.tidy.pprint(html, out);
  }



  protected static String findEncoding(Document ystDoc) {
    String encoding = "ISO-8859-1";
    NodeList heads = ystDoc.getElementsByTagName("head");
    if (heads.getLength() > 0) {
      Element head = (Element)heads.item(0);
      NodeList metas = head.getElementsByTagName("meta");
      for (int i = 0; i < metas.getLength(); i++) {
          Element meta = (Element) metas.item(i);
          String equiv = meta.getAttribute("http-equiv");
          if (equiv.equalsIgnoreCase("Content-Type")) {
            String cType = meta.getAttribute("content").trim().toUpperCase();
            int i_cT = cType.indexOf("CHARSET");
            if (i_cT != -1) {
              i_cT = cType.indexOf("=",i_cT+7);
              if (i_cT != -1) {
                encoding =cType.substring(i_cT+1);
              }
            }
          }
      }
    }
    if (EncodingNameMapper.toJava(encoding)==null) {
      System.err.println("WARNING: Not recognized encoding "+encoding);
    }
    return encoding;
  }

  protected void translateNode(Node node) throws TranslatingException {
    if (!node.getNodeName().equals("script") && isYSTNode(node)) {
      Node parent = node.getParentNode();
      String translated = explodeYST( (Element)node, false, false);
      if (translated != null) {
        translated = DW_F + "(" + translated + ")";
        Node script = createScriptNode(node.getOwnerDocument());

        Text scrContent = node.getOwnerDocument().createTextNode(translated);
        script.appendChild(scrContent);
        parent.insertBefore(script, node);
      }
      parent.removeChild(node);
    } else if (node.hasChildNodes()) {
      Node currCh = node.getFirstChild();
      while (currCh != null) {
        Node post = currCh.getNextSibling();
        translateNode(currCh);
        currCh = post;
      }
    }
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
      func = nested ?
          VALUE_F + ",["+ystAux+"['" + getTemplateFromNode(node, false, false) + "']]" :
          VALUE_F + firstParams+ ystAux + "['" + getTemplateFromNode(node, false, false) + "'])";
    } else if (action.equals("if")) {
      String test = getTestAttr(node);
      func = nested ?
          IF_F + ",["+ ystAux +"'" + TextUtils.escape(test) + "',['" + getTemplateFromNode(node, false, false) + "']]" :
          IF_F + firstParams + ystAux + "'" + TextUtils.escape(test) + "',['" + getTemplateFromNode(node, false, false) + "'])";
    } else if (action.equals("apply")) {
      func = nested ?
          APPLY_F + ",["+ ystAux +"'" + set + "',['" + getTemplateFromNode(node, false, false) + "']]" :
          APPLY_F + firstParams+ ystAux + "'" + set + "',['" + getTemplateFromNode(node, false, false) + "'])";
    } else if (action.equals("compapply")) {
      List compApplys = new ArrayList();
      compApplys.add(node);
      Node nS = node.getNextSibling();
      while (nS != null) {
        String act = YST_Action(nS);
        if (act != null && act.equals("compapply")) {
          changeYST_Action( (Element)nS, "ignore");
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
      makeTemplateFunction(node, false);
      return (nested ? "''" : null); //130209
    } else if (action.equals("include")) {
      func = makeIncludeCall(node, nested, firstParams, ystAux);
    } else if (action.equals("ajax") || action.equals("live")) {
      String funcName = makeTemplateFunction(node,true);
      func =  VALUE_F + firstParams + ystAux +
          "['" +  node2Str(node) + "',"+(funcName != null?funcName+",[]":"''")+",'</" + node.getNodeName().toLowerCase() + ">'" + "])";
    } else if (action.equals("literal")) {
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

  protected String getYSTAuxAttr(Element node) {
    String ystAux = node.getAttribute("ystaux");
    ystAux = ""+(ystAux!=null && ystAux.trim().length()!=0 ? "'"+TextUtils.escape(ystAux)+"'" : "null") + ",";
    return ystAux;
  }

  protected String makeIncludeCall(Element node, boolean nested, String firstParams, String ystAux) {
    String func;
    String idRef = node.getAttribute("ystidref").trim();
    idRef = idRef.replace(' ', '_');
    String params = node.getAttribute("ystparams");
    if (params == null)
      params = "";
    func = nested ?
        INCLUDE_F + ",["+ystAux+"'" + idRef + "','" + TextUtils.escape(params) + "']" :
        INCLUDE_F + firstParams+ystAux + "'" + idRef + "','" + TextUtils.escape(params) + "')";
    return func;
  }

  /**
   * makeTemplateFunction
   * @return template function name
   */
  protected String makeTemplateFunction(Element node, boolean isAJAX) throws TranslatingException {
    String functionName = node.getAttribute("id").trim();
    if (functionName == null) return null;
    functionName = functionName.replace(' ', '_');
    String function = "function " + functionName + "(contextValues, contextI, params) {\n";

    function += "var result = '" + getTemplateFromNode(node, true, true) + "';\n";
    function += "return result;\n";
    function += "}\n";
    Element script = createScriptNode(node.getOwnerDocument());
    script.setAttribute("yst", "declare");
    Text scrContent = node.getOwnerDocument().createTextNode(function);
    script.appendChild(scrContent);
    NodeList heads = node.getOwnerDocument().getElementsByTagName("head");
    Element head = (Element)heads.item(0);
    head.appendChild(script);

    return functionName;
  }

  /**
   *
   * @param node Node
   * @param isDeclare boolean indica si el elemento a pasar a txt proviene de un tag declare. En ese caso el
   *          txt generado es distinto porque forma parte del cuerpo de una funcion y no son los params de
   *          invocacion de la funcion
   * @param onlyInner omite el txt correspondiente al elemento y solo devuelve el interior. Esto se usa al traducir
   *         elmentos que corresponden con tag declare.
   * @return String
   * @throws TranslatingException
   */
  protected String getTemplateFromNode(Node node, boolean isDeclare, boolean onlyInner) throws TranslatingException {
    String result = "";
    String nName = node.getNodeName().toLowerCase();
    if (node.getNodeType() == Node.COMMENT_NODE) {
      return "";
    }
    if (!onlyInner)
      result += node2Str(node);
    if (node.hasChildNodes()) {
      NodeList children = node.getChildNodes();
      for (int i = 0; i < children.getLength(); i++) {
        Node child = children.item(i);
        if (isYSTNode(child) && !nName.equals("script")) {
          if (isDeclare) {
            String expNode = explodeYST( (Element)child, false, true);
            if (expNode != null && !expNode.equals("''"))
              result += "';\nresult += " + expNode + ";\nresult += '";
          } else {
            String expNode = explodeYST( (Element)child, true, false);
            if (!expNode.equals("''"))
              result += "'," + expNode + ",'";
          }
        } else {
          if (nName.equals("script")) {
            result += toJavaScript(getTemplateFromNode(child, isDeclare, false));
          } else {
            result += getTemplateFromNode(child, isDeclare, false);
          }
        }
      }
    }
    if (!onlyInner) {
      if (node.getNodeType() == Node.ELEMENT_NODE && !nName.equals("br")) {
        result += (nName.equals("script")) ?
            "</'+'" + nName + ">" :
            "</" + nName + ">";
      }
    }
    return result;
  }

  private static String toJavaScript(String str) {
    str = str.replaceAll("&amp;", "&");
    str = str.replaceAll("&lt;", "<");
    str = str.replaceAll("&gt;", ">");
    str = str.replaceAll("\\\\$", "\\\\\\\\$");
    str = str.replaceAll("&#10;", "\\\\n");

    return str;
  }

  protected static String getTestAttr(Element ystElement) {
    String test = ystElement.getAttribute("ysttest");
    if (test == null || test.trim().length() == 0) {
      test = "true";
    }
    return test;
  }

  protected String node2Str(Node node) {
    String text = "";
    String nName = node.getNodeName().toLowerCase();
    int type = node.getNodeType();
    if (type == Node.TEXT_NODE) {
       text = mapEntities(node.getNodeValue());
      return TextUtils.escape(text);
    } else if (type == Node.ELEMENT_NODE) {
      String ystBoolStr = null;
      text = "<" + nName;
      NamedNodeMap attrs = node.getAttributes();
      if (attrs != null) {
        for (int k = 0; k < attrs.getLength(); k++) {
          Attr a = (Attr)attrs.item(k);
          String n = a.getName();
          String v = a.getValue();
          if (n.equals("ystbool")) {
            ystBoolStr = " ',YST.Txt.ystBool,['"+TextUtils.escape(v)+"'],'";
          }
          if (this.hideYSTAttrs && isYSTAttr(n)) {
            continue;
          }
          v = CharPrinter.printAttrValue(this.tidy.getConfiguration(), v, (int)'"', false);
          text += " " + n + v;
        }
      }
      text = TextUtils.escape(text);
      if (ystBoolStr != null)
        text += ystBoolStr;
      text += ">";
    } else if (type == Node.DOCUMENT_TYPE_NODE) {
      text = TextUtils.escape("<!DOCTYPE " + nName + ">");
    }
    return text;
  }


  private String mapEntities(String srcTxt) {
    String text = "";
    for (int i = 0; i < srcTxt.length(); i++) {
      text += CharPrinter.printChar(srcTxt.charAt(i), CharPrinter.NORMAL, this.tidy.getConfiguration());
    }
    return text;
  }


  protected static void putProcessedMark(Document ystDoc) {
    NodeList bs = ystDoc.getElementsByTagName("body");
    if (bs != null && bs.getLength() > 0) {
      Element body = (Element)bs.item(0);

      Element script = createScriptNode(ystDoc);
      Text scriptContent = ystDoc.createTextNode("if (typeof YST != 'undefined') {YST.txtProcessing=true;YST.finishProcessing();}");
      script.appendChild(scriptContent);
      body.appendChild(script);
    }
  }

  protected static Element createScriptNode(Document ystDoc) throws DOMException {
    Element script = ystDoc.createElement("script");
    script.setAttribute("type", "text/javascript");
    return script;
  }

  private static boolean isYSTAttr(String name) {
    name = name.toLowerCase();
    return name.equals("yst") || name.equals("ysttest") || name.equals("ystset")
           || name.equals("ystidref") || name.equals("ystparams") || name.equals("ystupto")
           || name.equals("ystaux") || name.equals("ystbool");
  }

  /**
    Returns the value of the node yst attribute if it is present.
    Otherwise returns null
   */
  protected static String YST_Action(Node node) {
    if (node.getNodeType() == Node.ELEMENT_NODE) { // element
      String yst_a = ( (Element)node).getAttribute("yst");
      return yst_a.trim().toLowerCase();
    } else {
      return null;
    }
  }

  private static boolean isYSTNode(Node node) {
    String YST_a = YST_Action(node);
    return (YST_a != null && YST_a.trim().length() != 0);
  }

  protected static void changeYST_Action(Element node, String newAction) {
    node.removeAttribute("yst"); // JTidy pone todo en minusculas
    node.setAttribute("yst", newAction);
  }

  public static void main(String[] args) {

    if (args.length < 1 || args.length >7) {
      System.out.println("Usage: java -jar ysttrl.jar [-p <path>] [-o <dest_file>] [-v] [-c] <file>");
      System.out.println("  -v             : verbose; the translator shows information about the translation process,\n"+
                         "                   including transformation warnings. Errors are always shown");
      System.out.println("  -c             : cacheable; stores the template body in a separate file");
      System.out.println("  -p <path>      : specifies a directory where the source template is taken and the translation\n"+
                         "                   result is stored. The default directory is '.'.");
      System.out.println("  -o <dest_file> : specifies the result file name. By default ysttrl adds the '_t' suffix to the\n"+
                         "                   source template name.");

      return;
    }

    String path = ".";
    String dest = null;
    String file = "";
    boolean verbose = false;
    boolean cacheable = false;

    for (int i = 0; i < args.length; i++) {
      if (args[i].equalsIgnoreCase("-p")) {
        path = args[++i];
      } else if (args[i].equalsIgnoreCase("-o")) {
        dest = args[++i];
      } else if (args[i].equalsIgnoreCase("-v")) {
        verbose = true;
      } else if (args[i].equalsIgnoreCase("-c")) {
        cacheable = true;
      }
    }
    file = args[args.length-1];

    try {
      String fi = path + "/" + file;

      if (verbose) {
        System.out.println("INFO: Reading input file "+fi);
        System.out.println();
      }

      if (dest == null) {
        int iExt = file.lastIndexOf('.');
        dest = (iExt != -1 ? file.substring(0, iExt) + "_t" + file.substring(iExt) : file+"_t");
      }
      String fo = path + "/" + dest;
      OutputStream out = new FileOutputStream(fo);

      System.out.println();
      System.out.println("INFO: Generated output "+fo);

      if (cacheable) {
        int iExt = dest.lastIndexOf('.');
        String nameJS = (iExt != -1 ? dest.substring(0, iExt) : dest) + "_b.js";
        String foJS = path + "/" + nameJS;
        OutputStream outJS = new FileOutputStream(foJS);

        YSTTranslatorCacher jP = new YSTTranslatorCacher(true, verbose, false);

        Document doc = jP.translate(fi, nameJS);
        System.out.println("INFO: Generated output "+foJS);

        jP.storeToFiles(doc, out, outJS);
        out.close();
        outJS.close();
      } else {
        YSTTranslator jP = new YSTTranslator(true, verbose, false);
        Document doc = jP.translate(fi);
        jP.storeToFile(doc, out);
        out.close();
      }
    } catch (FileNotFoundException ex) {
      ex.printStackTrace();
    } catch (TranslatingException ex) {
      ex.printStackTrace();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  public void storeToFile(Document doc, String file, String path, String dest) throws IOException {
    if (dest == null) {
      int iExt = file.lastIndexOf('.');
      dest = (iExt != -1 ? file.substring(0, iExt) + "_t" + file.substring(iExt) : file+"_t");
    }

    String fo = path + "/" + dest;
    OutputStream out = new FileOutputStream(fo);

    System.out.println();
    System.out.println("INFO: Generated output "+fo);

    this.pprint(doc, out);
    out.close();
  }

  public void storeToFile(Document doc, OutputStream out) throws IOException {
    this.pprint(doc, out);
  }
}






