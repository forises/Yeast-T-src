package org.ystsrv.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class BodyCacheLoader extends HttpServlet {
  private File getFile(HttpServletRequest req) {
    String id =req.getParameter("id");

    File internalBodyFile = new File(Config.TRANSLATED_TEMPLATES_DIR + '/' + id + ".tmp");
    return internalBodyFile;
  }


  protected long getLastModified(HttpServletRequest req) {
    return getFile(req).lastModified();
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    response.setContentType("application/x-javascript");
    OutputStream out = response.getOutputStream();
    FileInputStream fis = new FileInputStream(getFile(request));
    byte buff[] = new byte[1024*8];
    int n;
    while ((n = fis.read(buff)) != -1) {
      out.write(buff, 0, n);
    }
    fis.close();
  }

}
