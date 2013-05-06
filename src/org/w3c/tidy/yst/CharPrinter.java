package org.w3c.tidy.yst;

import java.io.*;
import org.ystsrv.TransformationException;

public class CharPrinter {
  public static String printAttrValue(Configuration configuration, String value, int delim, boolean wrappable) {
    StringBuffer attrTxt = new StringBuffer();
    int c;
    int[] ci = new int[1];
    boolean inString;
    byte[] valueChars = null;
    int i;
    short mode = (wrappable ? (short) (NORMAL | ATTRIBVALUE) : (short) (PREFORMATTED | ATTRIBVALUE));

    if (value != null) {
      try {
        valueChars = value.getBytes("UTF8");
      } catch (UnsupportedEncodingException ex) {
        //throw new TransformationException("String to UTF-8 conversion failed: " + ex.getMessage());
      }
    }

    if (delim == 0) {
      delim = '"';
    }

    attrTxt.append('=');

    attrTxt.append((char)delim);

    if (value != null) {
      inString = false;

      i = 0;
      while (i < valueChars.length) {
        c = (valueChars[i]) & 0xFF; // Convert to unsigned.

        if (c == delim) {
          String entity;

          entity = (c == '"' ? "&quot;" : "&#39;"); // " o '

          for (int j = 0; j < entity.length(); j++) {
            attrTxt.append(entity.charAt(j));
          }

          ++i;
          continue;
        } else if (c == '"') {
          if (configuration.quoteMarks) {
            attrTxt.append("&quot;");
          } else {
            attrTxt.append('"');
          }

          if (delim == '\'') {
            inString = !inString;
          }

          ++i;
          continue;
        } else if (c == '\'') {
          if (configuration.quoteMarks) {
            attrTxt.append("&#39;");
          } else {
            attrTxt.append('\'');
          }

          if (delim == '"') {
            inString = !inString;
          }

          ++i;
          continue;
        }

        // look for UTF-8 multibyte character
        if (c > 0x7F) {
          i += getUTF8(valueChars, i, ci);
          c = ci[0];
        }

        ++i;

        if (c == '\n') {
          attrTxt.append(' '); //flushLine(fout, indent);
          continue;
        }

        attrTxt.append(printChar(c, mode, configuration));
      }
    }

    inString = false;
    attrTxt.append((char)delim);
    return attrTxt.toString();
  }

  /**
   * return one less than the number of bytes used by the UTF-8 byte sequence. The Unicode char is returned in ch.
   * @param str points to the UTF-8 byte sequence
   * @param start starting offset in str
   * @param ch initialized to 1st byte, passed as an array to allow modification
   * @return one less that the number of bytes used by UTF-8 char
   */
  public static int getUTF8(byte[] str, int start, int[] ch) {
    int[] n = new int[1];

    int[] bytes = new int[] {0};

    // first byte "str[0]" is passed in separately from the
    // rest of the UTF-8 byte sequence starting at "str[1]"
    byte[] successorBytes = str;

    boolean err = EncodingUtils.decodeUTF8BytesToChar(
        n,
        TidyUtils.toUnsigned(str[start]),
        successorBytes,
        null,
        bytes,
        start + 1);

    if (err) {
      n[0] = 0xFFFD; // replacement char
    }
    ch[0] = n[0];
    return bytes[0] - 1;

  }

  public static String printChar(int c, short mode, Configuration configuration) {
    StringBuffer charTxt = new StringBuffer();
    String entity;
    boolean breakable = false; // #431953 - RJ

    if (c == ' ' && !TidyUtils.toBoolean(mode & (PREFORMATTED | COMMENT | ATTRIBVALUE | CDATA))) {
      // coerce a space character to a non-breaking space
      if (TidyUtils.toBoolean(mode & NOWRAP)) {
        return "&nbsp;";
      }
    }

    // comment characters are passed raw
    if (TidyUtils.toBoolean(mode & (COMMENT | CDATA))) {
      return "" + (char)c;
    }

    // except in CDATA map < to &lt; etc.
    if (!TidyUtils.toBoolean(mode & CDATA)) {
      if (c == '<') {
        return "&lt;";
      }

      if (c == '>') {
        return "&gt;";
      }

      // naked '&' chars can be left alone or quoted as &amp;
      // The latter is required for XML where naked '&' are illegal.
      if (c == '&') {
        return "&amp;";
      }

      if (c == 160) {
        return "&nbsp;";
      }
    }

    // #431953 - start RJ
    // Handle encoding-specific issues

    switch (configuration.getOutCharEncoding()) {
      case Configuration.BIG5:
        return "" + (char)c;
      case Configuration.SHIFTJIS:
      case Configuration.ISO2022: // ISO 2022 characters are passed raw
        return "" + (char)c;
    }

    // if preformatted text, map &nbsp; to space
    if (c == 160 && TidyUtils.toBoolean(mode & PREFORMATTED)) {
      return "" + (char)c;
    }

    // don't map latin-1 chars to entities
    if (configuration.getOutCharEncoding() == Configuration.LATIN1) {
      if (c > 255) { /* multi byte chars */
        entity = EntityTable.getDefaultEntityTable().entityName( (short)c);
        if (entity != null) {
          entity = "&" + entity + ";";
        } else {
          entity = "&#" + c + ";";
        }

        return entity;
      }

      if (c > 126 && c < 160) {
        entity = "&#" + c + ";";

        return entity;
      }

      return "" + (char)c;
    }

    // don't map utf8 or utf16 chars to entities
    if (configuration.getOutCharEncoding() == Configuration.UTF8
        || configuration.getOutCharEncoding() == Configuration.UTF16
        || configuration.getOutCharEncoding() == Configuration.UTF16LE
        || configuration.getOutCharEncoding() == Configuration.UTF16BE) {
      return "" + (char)c;
    }

    // default treatment for ASCII
    if (configuration.getOutCharEncoding() == Configuration.ASCII && (c > 126 || (c < ' ' && c != '\t'))) {
      entity = EntityTable.getDefaultEntityTable().entityName( (short)c);
      if (entity != null) {
        entity = "&" + entity + ";";
      } else {
        entity = "&#" + c + ";";
      }

      return entity;
    }

    return "" + (char)c;
  }

  /**
   * position: normal.
   */
  public static final short NORMAL = 0;

  /**
   * position: preformatted text.
   */
  public static final short PREFORMATTED = 1;

  /**
   * position: comment.
   */
  public static final short COMMENT = 2;

  /**
   * position: attribute value.
   */
  public static final short ATTRIBVALUE = 4;

  /**
   * position: nowrap.
   */
  public static final short NOWRAP = 8;

  /**
   * position: cdata.
   */
  public static final short CDATA = 16;


}
