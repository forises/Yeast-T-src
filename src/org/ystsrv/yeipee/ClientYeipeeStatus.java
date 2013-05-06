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

import org.ystsrv.manager.Config;

public class ClientYeipeeStatus {
  public static final int YEIPEE_AND_SEND_ON = 1;
  public static final int YEIPEE_AND_SEND_OFF = 2;
  public static final int NOT_YEIPEE_AND_SEND_OFF = -1;
  public static final int DISABLE_YEIPEE_ON_CLIENT = -2;

  private static ThreadLocal must = new ThreadLocal();

  public static void setStatus(int process) {
    must.set(new Integer(process));
  }

  public static int getStatus() {
    int process;
    if (!Config.MAY_PROCESS_ON_SERVER) {
      process = DISABLE_YEIPEE_ON_CLIENT;
    } else {
      process = YEIPEE_AND_SEND_OFF;
        Integer processTL = (Integer)must.get();
        if (processTL != null)
          process = processTL.intValue();
    }
    return process;
  }

  public static boolean mustYeipee(int yeipeeStatus) {
    return yeipeeStatus >0;
  }

  public static String printStatus(int status) {
    switch (status) {
      case DISABLE_YEIPEE_ON_CLIENT : return "DISABLE_YEIPEE_ON_CLIENT";
      case NOT_YEIPEE_AND_SEND_OFF : return "NOT_YEIPEE_AND_SEND_OFF";
      case YEIPEE_AND_SEND_OFF : return "YEIPEE_AND_SEND_OFF";
      case YEIPEE_AND_SEND_ON : return "YEIPEE_AND_SEND_ON";
    }
    return "INVALID_VALUE";
  }
}
