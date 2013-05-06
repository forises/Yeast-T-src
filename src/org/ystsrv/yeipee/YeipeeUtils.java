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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.ystsrv.debug.Debug;
import org.ystsrv.util.TextUtils;

public class YeipeeUtils {
  private static final String LOGGER_NAME = "ystsrv.yeipee";

  public static final String YEIPEE_PARAM = "yst.yeipee";

  public static String addYeipeeToHref(String href) {
    int yeipeeStatus = ClientYeipeeStatus.getStatus();
    if (yeipeeStatus == ClientYeipeeStatus.DISABLE_YEIPEE_ON_CLIENT)
      return href;
    else {
      int paramValue = 0; // OFF
      if (yeipeeStatus == ClientYeipeeStatus.YEIPEE_AND_SEND_ON)
        paramValue = 1;

      if (href.indexOf('?') != -1) {
        String hrefparts[] = href.split("\\?");
        String urlQuery = hrefparts[1];
        if (urlQuery.indexOf(YEIPEE_PARAM) != -1)
          return href;
        else
          return href + '&' + YEIPEE_PARAM + '=' + paramValue;
      } else {
        return href + '?' + YEIPEE_PARAM + '=' + paramValue;
      }
    }
  }

  public static void detectYeipeeRequest(HttpServletRequest req) {
    String yeipee = req.getParameter(YEIPEE_PARAM);
    if (yeipee != null && yeipee.length()>0) {
      Debug.info(LOGGER_NAME,"Yeipee filter detect param "+YEIPEE_PARAM+": "+yeipee);
    } else {
      // Miro si hay cookie
      Cookie[] cookies = req.getCookies();
      if (cookies != null) {
        for (int i = 0; i < cookies.length; i++) {
          // Miro si hay alguna cookie con nombre yst.yeipee
          if (cookies[i].getName().equals(YEIPEE_PARAM)) {
            yeipee = cookies[i].getValue();
            Debug.info(LOGGER_NAME, "Yeipee filter detect cookie " + YEIPEE_PARAM + ": " + yeipee);
            break;
          }
        }
      }
    }

    int process = ClientYeipeeStatus.YEIPEE_AND_SEND_OFF;  // Por defecto proceso con yeipee y mando anulacion de Yeipee
    if (yeipee != null && yeipee.length()>0) {
      process = TextUtils.isTrue(yeipee) ? ClientYeipeeStatus.YEIPEE_AND_SEND_ON : ClientYeipeeStatus.NOT_YEIPEE_AND_SEND_OFF;
    }
    ClientYeipeeStatus.setStatus(process);
    Debug.fine(LOGGER_NAME,"Yeipee filter sets yeipee processing: "+ ClientYeipeeStatus.printStatus(process));
  }
}
