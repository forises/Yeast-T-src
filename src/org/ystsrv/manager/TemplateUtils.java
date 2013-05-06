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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;

import org.w3c.tidy.yst.EncodingNameMapper;
import org.ystsrv.debug.Debug;

public class TemplateUtils {

  private static final String LOGGER_NAME = "ystsrv.manager";

  private static final int NOT_INITIALIZED = -10;

  private static final String DEFAULT_ENCODING = new OutputStreamWriter(System.out).getEncoding();



  /**
   * Determine <script yst="model">...</script> position
   *
   * @param template String
   * @return int[] two elements; the first contains the start position of the
   *         model section; the second, the re-start position of the document
   *         after the model section.
   */
  static int[] findModelSectionBounds(String template) {
    int init = NOT_INITIALIZED, end = NOT_INITIALIZED;
    String aux = template.toLowerCase();
    boolean found = false;
    int iScript = aux.indexOf("<script");
    while (!found && iScript != -1) {
      int fScript = aux.indexOf(">", iScript);
      int iYst = aux.indexOf("yst", iScript);
      if (iYst < fScript) {
        int iModel = aux.indexOf("model", iYst);
        if (iModel < fScript) {
          int iEq = aux.indexOf("=", iYst);
          if (iEq < iModel) {
            String b = aux.substring(iEq + 1, iModel).trim();
            if (aux.substring(iYst + 3, iEq).trim().length() == 0 && b.equals("\"") || b.equals("'")) {
              found = true;
            }
          }
        }
      }
      if (!found)
        iScript = aux.indexOf("<script", iScript + 1);
    }

    init = found ? iScript : -1;
    if (init != -1) {
      end = aux.indexOf("</script>", init);
      int nested = aux.indexOf("<script>", init); // <script yst=model>xx<script>..<script>yy</script>
      if (end != -1 && (nested == -1 || nested > end)) //                 ^ error                     ^false end
        end += "</script>".length();
      else
        end = -1;
    }
    Debug.fine(LOGGER_NAME,
               "Computed model section position for template: " + init + " - " + end);
    return new int[] {init, end};
  }

  /**
   * Determine <script yst="model">...</script> position
   *
   * @param template byte[] with the template content
   * @return int[] two elements; the first contains the start position of the
   *         model section; the second, the re-start position of the document
   *         after the model section.
   */
  static int[] findModelSectionBounds(byte[] template) {
    int init = NOT_INITIALIZED, end = NOT_INITIALIZED;
    boolean found = false;
    // Asumo que los bytes de "<script" son siempre los mismos
    // independientemente del encoding de la página. Se usa un byte oir caracter
    // he comprobado encodings como big-5, euc-jp ... y se cumple
    int iScript = indexOfIgnoreCase(template, "<script".getBytes());
    while (!found && iScript != -1) {
      int fScript = indexOfIgnoreCase(template, ">".getBytes(), iScript);
      int iYst = indexOfIgnoreCase(template, "yst".getBytes(), iScript);
      if (iYst < fScript) {
        int iModel = indexOfIgnoreCase(template, "model".getBytes(), iYst);
        if (iModel < fScript) {
          iModel = iModel - 1; // descuento el " o ' que debe preceder a "model" en yst="model"
          int iEq = indexOfIgnoreCase(template, "=".getBytes(), iYst);
          if (iEq < iModel) {
            // si entre yst e = solo hay blancos y entre = y "model tambien
            if (allAreBlank(template, iEq + 1, iModel) && allAreBlank(template, iYst + 3, iEq)) {
              found = true;
            }
          }
        }
      }
      if (!found)
        iScript = indexOfIgnoreCase(template, "<script".getBytes(), iScript + 7);
    }

    init = found ? iScript : -1;
    if (init != -1) {
      end = indexOfIgnoreCase(template, "</script>".getBytes(), init);
      int nested = indexOfIgnoreCase(template, "<script>".getBytes(), init); // <script yst=model>xx<script>..<script>yy</script>
      if (end != -1 && (nested == -1 || nested > end)) //                                           ^ error                     ^false end
        end += "</script>".length();
      else
        end = -1;
    }
    Debug.fine(LOGGER_NAME,
               "Computed model section position for template: " + init + " - " + end);

    return new int[] {init, end};
  }

  private static boolean allAreBlank(byte[] src, int from, int to) {
    for (int i = from; i < to; i++) {
      char c = (char)src[i];
      if (c == ' ' || c == '\t' || c == '\n' || c == '\r')
        continue;
      else
        return false;
    }
    return true;
  }

  public static boolean hasAJAX(byte[] content) {
    String regex;
    /*
    regex = ".*"; //anything to the left
    regex += "<[^>]*"; //tag opening and any character except '>'
    regex += "\\s+yst\\s+=\\s+[\"\']ajax[\"\']"; // yst="ajax"
    regex += "[[\\s+[^>]*>]>]"; //one or more spaces, any non-">"'s and a ">"; or a ">"
    regex += ".*"; //anything to the right
    */
    regex=".*YST.AJAX.*";
    String content_str = new String(content);
    return content_str.indexOf("YST.AJAX")!=-1;
  }

  public static String guessCharEncoding(byte[] content) {
    String encoding = null;
    boolean found = false;
    // Asumo que los bytes de "<meta" son siempre los mismos
    // independientemente del encoding de la página. Se usa un byte por caracter
    // he comprobado encodings como big-5, euc-jp ... y se cumple

    // busco el atributo content="text/html; charset=xxxx" en una etiqueta meta que tenga como atributo http-equiv="Content-Type"
    // La etiqueta meta puede terminar en /> o en >

    // 1 busco los limites de la etiqueta
    int iMeta = indexOfIgnoreCase(content, "<meta".getBytes());
    while (!found && iMeta != -1) {
      int fMeta = indexOfIgnoreCase(content, ">".getBytes(), iMeta);
      // En ella miro si hay Content-Type, precedido de http-equiv
      int iContentType = indexOfIgnoreCase(content, "Content-Type".getBytes(), iMeta, fMeta);

      if (iContentType != -1 &&
          (content[iContentType - 1] == '"' ||
           content[iContentType - 1] == '\'')) {
        // Si es el caso busco el valor del atributo content que debe tener charset
        int iCharset = indexOfIgnoreCase(content, "charset".getBytes(), iMeta, fMeta);
        if (iCharset != -1) {
          int iEq = indexOfIgnoreCase(content, "=".getBytes(), iCharset, fMeta);
          int iCharsetValue = iEq + 1;
          // Avanzo por espacios
          while (iCharsetValue < fMeta &&
                 (content[iCharsetValue] == ' ' || content[iCharsetValue] == '\t'
                  || content[iCharsetValue] == '\n' || content[iCharsetValue] == '\r')) {
            iCharsetValue++;
          }
          if (iCharsetValue < fMeta) {
            // Cojo hasta el fin del valor del atrib)
            int eCharsetValue = iCharsetValue;
            while (eCharsetValue < fMeta && content[eCharsetValue] != ' ' &&
                   content[eCharsetValue] != '"' && content[eCharsetValue] != '\'' &&
                   content[eCharsetValue] != '/' && content[eCharsetValue] != '>' ) {
              eCharsetValue++;
            }
            if (eCharsetValue < fMeta) {
              byte[] enc = new byte[eCharsetValue - iCharsetValue];
              System.arraycopy(content, iCharsetValue, enc, 0, enc.length);
              encoding = new String(enc).trim();
              found = true;
            }
          }
        }
      }

      if (!found) {
        iMeta = indexOfIgnoreCase(content, "<meta".getBytes(), iMeta + 5);
      }
    }

    // En ella miro si hay Content-Type, precedido de http-equiv
    // Si es el caso busco el valor del atributo content
    // Proceso ese atributo

    // Habra que ver como se mapean los encodings de HTML a lo de Java

    Debug.fine(LOGGER_NAME, "Detected template encoding: " + encoding);
    if (encoding == null || encoding.length()==0) {
      encoding = DEFAULT_ENCODING;
      Debug.fine(LOGGER_NAME, "Using default encoding: " + encoding);
    }
    encoding = EncodingNameMapper.toJava(encoding);
    if (encoding == null) { // puede haberse leido algo sin sentido y EncodingNameMapper devuelve null
      encoding = DEFAULT_ENCODING;
      Debug.fine(LOGGER_NAME, "Using default encoding: " + encoding);
      encoding = EncodingNameMapper.toJava(encoding);
    }

    Debug.fine(LOGGER_NAME, "Template encoding to be used (java): " + encoding);

    return encoding;
  }

  private static int indexOfIgnoreCase(byte[] source, byte[] target) {
    return indexOfIgnoreCase(source, target, 0);
  }

  private static int indexOfIgnoreCase(byte[] source, byte[] target, int fromIndex) {
    if (fromIndex >= source.length) {
      return (target.length == 0 ? source.length : -1);
    }
    if (fromIndex < 0) {
      fromIndex = 0;
    }
    if (target.length == 0) {
      return fromIndex;
    }

    byte first = toUpper(target[0]);
    int i = fromIndex;
    int max = source.length - target.length;

    startSearchForFirstChar:while (true) {
      /* Look for first character. */
      while (i <= max && toUpper(source[i]) != first) {
        i++;
      }
      if (i > max) {
        return -1;
      }

      /* Found first character, now look at the rest of v2 */
      int j = i + 1;
      int end = j + target.length - 1;
      int k = 1;
      while (j < end) {
        if (toUpper(source[j++]) != toUpper(target[k++])) {
          i++;
          /* Look for str's first char again. */
          continue startSearchForFirstChar;
        }
      }
      return i; /* Found whole string. */
    }
  }

  private static int indexOfIgnoreCase(byte[] source, byte[] target, int fromIndex, int upTo) {
    upTo = (upTo < 0 ? source.length : upTo);
    upTo = (upTo > source.length ? source.length : upTo);

    if (fromIndex >= source.length) {
      return (target.length == 0 ? source.length : -1);
    }
    if (fromIndex < 0) {
      fromIndex = 0;
    }
    if (target.length == 0) {
      return fromIndex;
    }
    if (upTo <= fromIndex)
      return -1;
    if (upTo - fromIndex < target.length)
      return -1;

    byte first = toUpper(target[0]);
    int i = fromIndex;
    int max = upTo - target.length;

    startSearchForFirstChar:while (true) {
      /* Look for first character. */
      while (i <= max && toUpper(source[i]) != first) {
        i++;
      }
      if (i > max) {
        return -1;
      }

      /* Found first character, now look at the rest of v2 */
      int j = i + 1;
      int end = j + target.length - 1;
      int k = 1;
      while (j < end) {
        if (toUpper(source[j++]) != toUpper(target[k++])) {
          i++;
          /* Look for str's first char again. */
          continue startSearchForFirstChar;
        }
      }
      return i; /* Found whole string. */
    }
  }

  private static byte toUpper(byte c) {
    byte delta = 32;
    return (c >= 90 && c <= 122 ? (byte) (c - delta) : c);
  }


  /**
   * Returns the whole content of a template in a String variable
   *
   * @param source Reader
   * @return String
   * @throws IOException
   */
  static String readTemplate(Reader source) throws IOException {
    BufferedReader in = new BufferedReader(source);
    char[] c = new char[4096];

    StringBuffer auxBS = new StringBuffer();
    int readed = 0;
    while ( (readed = in.read(c, 0, 1024)) > 0) {
      auxBS.append(c, 0, readed);

    }
    in.close();
    return auxBS.toString();
  }

  /**
   * Returns the whole content of a template in a byte[] variable
   *
   * @param source InputStream
   * @return byte[]
   * @throws IOException
   */
  static byte[] readTemplate(InputStream source) throws IOException {
    BufferedInputStream in = new BufferedInputStream(source);
    ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
    byte[] c = new byte[4096];
    int readed = 0;
    while ( (readed = in.read(c, 0, 1024)) > 0) {
      bos.write(c, 0, readed);
    }
    in.close();
    return bos.toByteArray();
  }
}
