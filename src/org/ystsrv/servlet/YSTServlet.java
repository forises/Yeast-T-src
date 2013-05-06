/*
 *  Yeast-Server for Java
 *
 *  Copyright (c) 2009, Francisco José García Izquierdo. University of La
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
package org.ystsrv.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ystsrv.ConfigurationException;
import org.ystsrv.ModelSection;
import org.ystsrv.Template;
import org.ystsrv.TemplateManager;
import org.ystsrv.TransformationException;
import org.ystsrv.YSTException;
import org.ystsrv.debug.Debug;
import org.ystsrv.manager.Config;
import org.ystsrv.transformer.TransformerGroup;
import org.ystsrv.yeipee.YeipeeUtils;

/**
 * This is an abstract class that can be used as the basis to implement a
 * servlet that uses Yeast templates to render its response. Its main purpose is
 * to manage common actions related to the template retrieval and printing to
 * the requesting client.
 *
 * <p>The class defines an abstract method {@link #handle} that must implement
 * the processing of the input parameters, obtain the data necessary to
 * fulfill the template model section and select the template to be used as
 * view. This method receives an {@link YSTContext} object in which some
 * commonly accessed services such as request and response objects are done
 * available.
 *
 * <p>Servlet classes extending this class does not need to implement the usual
 * <code>doGet</code> or <code>doPost</code> methods. They only need to
 * implement the {@link #handle} method. Regardless of the http method used (GET
 * or POST), the <code>handle</code> method will be invoked. If you want that
 * your class does not implement one of these methods you can use the ({@link
 * #banDoGet} or {@link #banDoPost}) methods. The rest of http methods (HEAD,
 * ...) must be implemented by the extending servlet class.
 *
 * @author Francisco José García Izquierdo
 * @version 2.0
 */
public abstract class YSTServlet extends HttpServlet {

  private static final String LOGGER_NAME = "ystsrv.servlet";
  
  private static final String AJAX_PARAM_NAME = "yst.ajax";

  /**
   * {@inheritDoc}
   */
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  protected void initContextNameInConfig(HttpServletRequest sc) {
    if (Config.contextName == null) {
      String contextName = sc.getContextPath();
      Config.contextName = contextName;
      Debug.info(LOGGER_NAME, "Set contextName in Config ("+contextName+")");
    }
  }

  /**
   * This method is called to handle the processing of a request. It should
   * analyze the data in the request, put whatever values are required into the
   * <code>YSTContext</code>, and return the appropriate Yeast template name. It
   * also can specify a template store different from the default one, using the
   * {@link YSTContext#setTemplateStore} context method.
   *
   * @param context provides access to the request and response, and contains
   *   data necesary to fulfill the template and to specify a template store
   * @throws ServletException if the request could not be handled
   * @throws IOException if an input or output error is detected when the
   *   servlet handles the request
   * @throws TransformationException if any is encountered during data
   *   transformation
   * @return the identifier of the selected template (it must be present in the
   *   selected template store). Template identifiers, unless
   *   something different being specified in the <code>YSTConfig.xml</code>
   *   configuration file, refers directly to the name of the file that
   *   contains the template (without the extension). They can also refer to a
   *   configuration element in the configuration file
   *   <code>YSTConfig.xml</code>, where they are asigned to an actual file
   *   name.
   */
  abstract protected String handle(YSTContext context)
      throws IOException, ServletException, TransformationException;

  /**
   * Process an incoming GET request: builds a <code>YSTContext</code> up and
   * then passes it to the {@link #handle} method; once this method returns the
   * name of the selected <code>Template</code> to be be used, the
   * <code>Template</code> is loaded using the {@link
   * org.ystsrv.TemplateManager#getTemplate} method; then the data
   * stored in the <code>YSTContext</code> are retrieved and pushed onto the
   * template using the {@link org.ystsrv.Template#print} method.
   *
   * <p>You can override this method if you want, though for most purposes you
   * are expected to override <code>handle()</code> instead. One possible
   * situation in which you may want to override this <code>doGet</code> method
   * is when you want to avoid the use of the GET http method in the interaction
   * with your servlet. In this case, override the <code>doGet</code> method
   * with a call to the {@link #banDoGet} method.
   *
   * @param request HttpServletRequest
   * @param response HttpServletResponse
   * @throws ServletException if the request for the GET could not be handled
   * @throws IOException if an input or output error is detected when the
   *   servlet handles the GET request
   * @throws ConfigurationException if the configuration file (<code>YSTConfig.xml</code>)
   *   does follow the rules described in the Yeast Server documentation; if some
   *   of the template transformers returns <code>null</code> in its
   *   {@link org.ystsrv.Transformer#transformedClass} method
   * @throws ConfigurationException if the order of the transformers in the
   *   <code>transformerSpecs</code> parameter does not follow the building
   *   rules described in this class header javadoc (in first place transformers
   *   for derived classes, later transformers for base classes (see this class
   *   header javadoc for more information).
   * @throws ConfigurationException if any of the class names contained in the
   *   list of transformer specifications, can not be loaded or instanciated
   *   (e.g. the transformer class has not got a no params constructor or a
   *   constructor with a <code>java.util.Map</code> param, as described in the
   *   header javadoc of class {@link org.ystsrv.Transformer})
   * @throws RuntimeException Any implicit exception thrown in the
   *   {@link org.ystsrv.Transformer#transform} method of any of
   *   the template transformers
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws
      ServletException, IOException, ConfigurationException {
    Debug.fine(LOGGER_NAME, "Received GET request");
    try {
      doIt(request, response);
    } catch (YSTException ex) {
      Debug.info(LOGGER_NAME, "Error sending the query result to response", ex);
      throw new ServletException(ex);
    }
  }

  /**
   * Behaves exactly like {@link #doGet} except for reading data from POST.
   *
   * <p>You can override this method if you want, though for most purposes you
   * are expected to override <code>handle()</code> instead. One possible
   * situation in which you may want to override this <code>doPost</code> method
   * is when you want to avoid the use of the POST http method in the
   * interaction with your servlet. In this case, override the
   * <code>doPost</code> method with a call to the {@link #banDoPost} method.
   *
   * @param request HttpServletRequest
   * @param response HttpServletResponse
   * @throws ServletException if the request for the POST could not be handled
   * @throws IOException if an input or output error is detected when the
   *   servlet handles the POST request (e.g., the template loading)
   * @throws ConfigurationException if the configuration file (<code>YSTConfig.xml</code>)
   *   does follow the rules described in the Yeast Server documentation; if some
   *   of the template transformers returns <code>null</code> in its
   *   {@link org.ystsrv.Transformer#transformedClass} method
   * @throws ConfigurationException if the order of the transformers in the
   *   <code>transformerSpecs</code> parameter does not follow the building
   *   rules described in this class header javadoc (in first place transformers
   *   for derived classes, later transformers for base classes (see this class
   *   header javadoc for more information).
   * @throws ConfigurationException if any of the class names contained in the
   *   list of transformer specifications, can not be loaded or instanciated
   *   (e.g. the transformer class has not got a no params constructor or a
   *   constructor with a <code>java.util.Map</code> param, as described in the
   *   header javadoc of class {@link org.ystsrv.Transformer})
   * @throws RuntimeException Any implicit exception thrown in the
   *   {@link org.ystsrv.Transformer#transform} method of any of
   *   the template transformers
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws
      ServletException, IOException, ConfigurationException {
    Debug.fine(LOGGER_NAME, "Received POST request");
    try {
      doIt(request, response);
    } catch (YSTException ex) {
      Debug.info(LOGGER_NAME, "Error sending the query result to response", ex);
      throw new ServletException(ex);
    }
  }

  void doIt(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, YSTException {
	  
    initContextNameInConfig(request);

    // Detect if the request is Yeippe or not
    YeipeeUtils.detectYeipeeRequest(request);

    YSTContext context = new YSTContext(request, response);

    String templateName = handle(context);
    Debug.fine(LOGGER_NAME, "YSTServlet- Template " + templateName + " is about to be used");
    
    boolean full = ! guessIfIsAJAXRequest(request);

    List data = context.getResponseObjects();
    ModelSection dd2 = null;
    Template template = null;
    if (templateName != null && templateName.trim().length() != 0) {
      template = getTemplate(context, templateName);
      dd2 = template.makeModel(data);
    } else if (!full) {
      TransformerGroup transformers = new TransformerGroup();
      dd2 = transformers.transform(data);
    }
    ModelSection dd = context.getModelSection();
    if (dd2 != null && !dd2.isEmpty()) {
      dd.append(dd2);
    }

    OutputStream out = response.getOutputStream();
    Debug.fine(LOGGER_NAME, "Servlet response using " + response.getCharacterEncoding());

    if (full) {
      // Process the template
      if (template != null) {
        template.print(dd, out);
        Debug.fine(LOGGER_NAME, "YSTServlet- Template sent to client");
      }
    } else {
      Debug.fine(LOGGER_NAME, "YSTServlet- Received AJAX request");
      if (context.avoidBrowserCache()) {
        avoidCaching(response);
      }
      Debug.fine(LOGGER_NAME, "YSTServlet- About to sent data: " + dd.getData());
      response.setContentType("text/javascript; charset=UTF-8");
      out.write(dd.getData().getBytes("UTF-8"));
      Debug.fine(LOGGER_NAME, "YSTServlet- AJAX data sent to client");
    }
    out.flush();

  }

  private boolean guessIfIsAJAXRequest(HttpServletRequest request) {
    boolean ajax = false;
    String param = request.getParameter(AJAX_PARAM_NAME);
    String xrw = null;
    if (param != null && param.trim().length()>0) {
      ajax = !param.equals("0") || param.equals("yes") || param.equals("true") || param.equals("on");
    } else {
      xrw = request.getHeader("X-Requested-With");
      ajax = xrw != null && xrw.equalsIgnoreCase("XMLHttpRequest");
    }
    Debug.fine(LOGGER_NAME, "YSTServlet- detected "+(ajax?"AJAX":"NON-AJAX")+
                            " request ("+AJAX_PARAM_NAME+"="+param+";X-Requested-With="+xrw+")");
    return ajax;
  }

  private Template getTemplate(YSTContext context, String templateName) throws YSTException, IOException, ConfigurationException {
    Template template;
    template = (context.getTemplateStore() == null) ?
        TemplateManager.getTemplate(templateName, this.getServletContext()) :
        TemplateManager.getTemplate(context.getTemplateStore(), templateName, this.getServletContext());
    return template;
  }

  private void avoidCaching(HttpServletResponse response) {
    response.setHeader("Expires", "Sat, 6 May 1995 12:00:00 GMT");
    response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    response.setHeader("pragma", "no-cache");
  }

  /**
   * Use a call to this method inside the <code>doGet</code> method of your
   * class to avoid the use of the http GET method in request to your servlet.
   *
   * @param req HttpServletRequest
   * @param resp HttpServletResponse
   * @throws ServletException
   * @throws IOException
   */
  protected void banDoGet(HttpServletRequest req, HttpServletResponse resp) throws
      ServletException, IOException {
    super.doGet(req, resp);
  }

  /**
   * Use a call to this method inside the <code>doPost</code> method of your
   * class to avoid the use of the http POST method in request to your servlet.
   *
   * @param req HttpServletRequest
   * @param resp HttpServletResponse
   * @throws ServletException
   * @throws IOException
   */
  protected void banDoPost(HttpServletRequest req, HttpServletResponse resp) throws
      ServletException, IOException {
    super.doPost(req, resp);
  }

}
