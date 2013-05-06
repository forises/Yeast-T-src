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
package org.ystsrv.transformer.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.ystsrv.ConfigurationException;
import org.ystsrv.TransformationException;
import org.ystsrv.Transformer;
import org.ystsrv.debug.Debug;
import org.ystsrv.transformer.Renderer;
import org.ystsrv.util.TextUtils;

/**
 * This class is specialized in the transformation of {@link DBQuery} objects.
 * The rows of the result of the <code>DBQuery</code> execution will be
 * transformed into JavaScript data structures, usually an array of objects or
 * several arrays of primitive data. Each member of the JavaScript array may
 * correspond to one row of the query result. The JavaScript structures may need
 * to be initialized using some JavaScript code. <code>DBQueryTransformer</code>
 * objects allow the specification of this initialization code by means of a
 * String parameter provided in the object construction (<code>header</code>
 * param). This parameter can be <code>null</code> or empty. In order to
 * transform each row of the query result it is necessary to provide a format
 * string, which will act as a template for the transformation. In that format
 * string you can insert, enclosed between curly braces ({}), references to the
 * data base columns or expressions included in the query. To refer to a certain
 * column of the result you must use its name or its position in the result (the
 * first column corresponds to the number 1). When the transformation is
 * performed, this transformer will substitute these references with the values
 * of the referred columns. Values are transformed following the same rules
 * specified for the class {@link org.ystsrv.transformer.BeanTransformer}.
 *
 * <p><code>DBQueryTransformer</code> will iterate over the result rows. If you
 * want to refer to the implicit counter associated to the iteration process,
 * you can use the predefined internal variable <code>#i</code> in the format
 * string (see the example below).
 *
 * <p> If the <code>DBQuery</code> object to be transformed is
 * <code>null</code>, then the resultant transformation will be an string with
 * the following JavaScript code: <code>// Null data skipped</code>.
 *
 * <p>Yeast templates must be configured to use <code>DBQueryTransformer</code>
 * for <code>DBQuery</code> objects transformation. The configuration details
 * are provided in the <a href="#config">Configuration in the section
 * <code>YSTConfig.xml</code> file</a>. You can also use a {@link
 * DBQueryHelper} object to avoid the use of configuration files, or to
 * share the same JDBC connection for several queries.
 *
 * <h3>Connection closing issues</h3> If the <code>DBQuery</code> object to be
 * transformed has to create a <code>java.sql.Connection</code> in order to
 * obtain the query result, then this <code>Connection</code> will be closed by
 * the  <code>DBQueryTransformer</code> once the transformation is finished.
 * Otherwise, that is, the <code>DBQuery</code> is build with an externally
 * created connection or <code>ResultSet</code>, the connection will remain as
 * it was provided, being its creator responsible for closing it.
 *
 * <h3>Example</h3> Consider we have a
 * relational data base where there is stored a table called <code>Book</code>,
 * which contains information about books. The table schema defines fields for
 * the id, title, author name (<code>a_name</code>) and surname
 * (<code>a_surname</code>), publisher (all of char type), price (number) and
 * publication date (<code>published</code>). We want to push the
 * <code>Book</code> table data into a Yeast template.
 *
 * <p> Imagine now that in the template’s design model a book is called
 * <code>Item</code>, and that they have the following attributes:
 * <code>author</code>, <code>title</code>, <code>price</code>, and
 * <code>pubDate</code>. You need to transform the table data into a JavaScript
 * array of <code>Item</code> objects named <code>books</code>.
 *
 * <p>In first place you need a SQL statement to retrieve the data: <pre>
 *   SELECT title, a_name+' '+a_surname, price, published
 *   FROM Book</pre>
 *
 * <p>You must build a <code>DBQuery</code> object with a connection to the data
 * base and the previous select statement as parameters: <pre>
 *   java.sql.Connection con = ...; // initialize the connection to the data base
 *   DBQuery dbq = new DBQuery(con, "SELECT title, a_name+' '+a_surname, price, published FROM Book");</pre>
 *
 * <p>Then, you can use the following format and header strings to obtain the
 * desired transformation: <pre>
 *   String header = "books = new Array();";
 *   String format = "books[{#i}] = new Item({#2}, {title}, {price}, {published});";</pre>
 * where <code>{#2}</code> refers to the second expression
 * included in the previous select (<code>a_name+' '+a_surname</code>).
 *
 * <p>When you transform <pre>
 *   DBQueryTransformer dbqt = new DBQueryTransformer(header, format);
 *   System.out.println(dbqt.transform(dbq));</pre>
 * you get <pre>
 *   books = new Array();
 *   books[0] = new Item('David Baldacci', 'The Camel Club', 8.49, new Date(1046732400000));
 *   books[1] = new Item('Danni Goodman', 'Dynamic HTML', 23.8, new Date(1032732000000));</pre>
 *
 * <p>You can also specify a different format for date representation like in
 * <pre>
 *   String format = "books[{#i}] = new Item({#2}, {title}, {price}, {published %dd-MM-yyyy%});";</pre>
 * In this case the transformation is: <pre>
 *   books = new Array();
 *   books[0] = new Item('David Baldacci', 'The Camel Club', 8.49, '04-03-2003');
 *   books[1] = new Item('Danni Goodman', 'Dynamic HTML', 23.8, '23-09-2002');</pre>
 *
 * <a name="config"><h3>Configuration in the <code>YSTConfig.xml</code> file</h3>
 * You can configure a Yeast template to use <code>DBQueryTransformer</code>. The
 * configuration must specify the values for the construction parameters using
 * the following syntax:
 * <ul>
 *   <li><code>header</code>: the header string</li>
 *   <li><code>format</code>: the format string</li>
 *   <li><code>datePattern</code>: the default date format that will be applied
 * if no specific pattern is provided</li>
 *   <li><code>localeLang</code>: the language code to be used as locale for
 * date rendering, which will either be the empty string or a lowercase ISO 639
 * code.</li>
 *   <li><code>localeCountry</code>: the country/region code used to complement
 * the locale defined with the previos parameter, which will either be the empty
 * string or an upercase ISO 3166 2-letter code.</li>
 * </ul>
 * For example, the following configuration excerpt is taken from a
 * <code>YSTConfig.xml</code> file where a <code>DBQueryTransformer</code> is
 * configured to be used with the rows of the previous example <code>Book</code>
 * table. The header string is <code>books = new Array();</code>, and the format
 * string is<code>books[{#i}] = new Item({#2}, {title}, {price}, {published});
 * </code>. And finally, a default pattern <code>dd-MM-yyyy</code> is provided.
 * <pre>
 *  &lt;template id="choose"&gt;
 *    &lt;location&gt;ChooseBooks.html&lt;/location&gt;
 *    &lt;transformer class="org.ystsrv.transformer.jdbc.DBQueryTransformer"&gt;
 *      &lt;param name="header"&gt; books = new Array();&lt;/param&gt;
 *      &lt;param name="format"&gt; books[{#i}] = new Item({#2}, {title}, {price}, {published});&lt;/param&gt;
 *      &lt;param name="datePattern"&gt;dd-MM-yyyy&lt;/param&gt;
 *    &lt;/transformer&gt;
 *  &lt;/template&gt;
 * </pre>
 *
 * @see org.ystsrv.transformer.jdbc.DBQueryTransformer
 * @author Francisco José García Izquierdo
 * @version 1.0
 */
public class DBQueryTransformer extends Transformer {
  private static final String LOGGER_NAME = "ystsrv.transformer";
  private List tokens;
  private String header;
  private String format;
  private String defaultDatePattern;
  private Locale locale;
  private boolean lexed = false;

  /**
   * Creates a <code>DBQueryTransformer</code>.
   * @param header it can be null
   * @param format String specifying the format to be applied in the
   *   transformation. It can not be null
   */
  public DBQueryTransformer(String header, String format) {
    this(header,format,null);
  }

  /**
   * Creates a <code>DBQueryTransformer</code>.
   * @param header it can be null
   * @param format String specifying the format to be applied in the
   *   transformation. It can not be null
   * @param defaultDatePattern default date pattern for date rendering. It will
   * be applied for the rendering of a certain query column if no specific
   * pattern is supplied. It can be null
   */
  public DBQueryTransformer(String header, String format, String defaultDatePattern) {
    this(header, format, defaultDatePattern, null);
  }

  /**
   * Creates a <code>DBQueryTransformer</code>.
   *
   * @param header it can be null
   * @param format String specifying the format to be applied in the
   *   transformation. It can not be null
   * @param defaultDatePattern default date pattern for date rendering. It will
   *   be applied for the rendering of a certain query column if no specific
   *   pattern is supplied. It can be null
   * @param locale Locale for date representation (it can be null)
   */
  public DBQueryTransformer(String header, String format, String defaultDatePattern, Locale locale) {
    if (format == null || format.trim().length() == 0)
      throw new ConfigurationException("Impossible to build DBQueryTransformer without format. " +
                                       "Check the ystsrv configuration file. Param format required");

    this.header = header;
    this.format = format;
    this.defaultDatePattern = defaultDatePattern;
    this.locale = locale;
    this.tokens = new ArrayList();
  }

  /**
   * Creates a <code>DBQueryTransformer</code>. Gets the required
   * constructor parameter <code>format</code> from
   * the <code>params</code> map. The rest of parametera are optional.
   *
   * @param params Map with parameters names and values. It MUST contain
   *   <code>format</code> parameter and their respective value;
   *   <code>header</code>, <code>datePattern</code> <code>localeLang</code>
   *   <code>localeCountry</code> are optional.
   * @throws ConfigurationException if the given <code>params</code> map does
   *   not contain an entry for the format string of this object (entry of key
   *   <code>format</code>), or the entry is null or the empty String or it has
   *   curly braces mismatches
   */
  public DBQueryTransformer(Map params) throws ConfigurationException {
    this.header = (String)params.get("header");
    this.format = (String)params.get("format");
    if (format == null || format.trim().length() == 0)
      throw new ConfigurationException("Impossible to build DBQueryTransformer " +
                                       "without format. Check the ystsrv configuration file." +
                                       " Param format required");
    this.defaultDatePattern = (String)params.get("datePattern");
    this.tokens = new ArrayList();

    this.locale = null;
    String localeLang = (String)params.get("localeLang");
    if (localeLang != null && localeLang.trim().length() != 0) {
      String localeCountry =  (String)params.get("localeCountry");
      if (localeCountry != null && localeCountry.trim().length() != 0)
        this.locale = new Locale(localeLang, localeCountry);
      else
        this.locale = new Locale(localeLang);
    }
  }

  /**
   * {@inheritDoc}
   */
  public String transform(Object data) throws TransformationException {
    if (data == null) {
      Debug.warning(LOGGER_NAME, "TransformerGroup.transform: skiping null data to transform");
      return "// Null data skipped";
    }

    DBQuery dbq = (DBQuery)data;
    ResultSet rs = null;
    try {
      rs = dbq.getResultSet();
      if (data != null && rs.next()) {
        synchronized (this.tokens) {
          if (!lexed) {
            Debug.fine(LOGGER_NAME, "Lexe the format string for the first time");
            this.lexeFormat(this.format, rs);
          }
        }
        return this.format(rs);
      } else
        return (header != null ? header : "");
    } catch (SQLException ex) {
      throw new TransformationException("Exception transforming " + data, ex);
    } finally {
      if (rs != null) {
        try {
          Statement stm = rs.getStatement();
          if (stm != null) {
            Connection con = rs.getStatement().getConnection();
            if (con != null) {
              if (dbq.getMustCloseConnection()) {
                rs.close();
                stm.close();
                con.close();
                Debug.info(LOGGER_NAME, "DB connection for "+data+" closed");
              } else if (dbq.getMustCloseStatement()) {
                rs.close();
                stm.close();
                Debug.info(LOGGER_NAME, "DB statement for "+data+" closed");
              }
            }
          }
        } catch (SQLException ex1) {
          Debug.error(LOGGER_NAME, "Cannot close DB connection for "+data, ex1);
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public Class transformedClass() {
    return DBQuery.class;
  }

  private String format(ResultSet rs) throws SQLException, TransformationException {
    StringBuffer sb = new StringBuffer( (header != null ? header : "") + "\n");
    int i = 0;
    for (; ; ) {
      sb.append(this.formatRow(i++, rs));
      if (rs.next())sb.append("\n");
      else break;
    }
    return sb.toString();
  }

  private String formatRow(int order, ResultSet rs) throws TransformationException,
      ConfigurationException {
    StringBuffer formatedValue = new StringBuffer();

    Iterator iter = this.tokens.iterator();
    while (iter.hasNext()) {
      Object token = iter.next();
      if (token instanceof PropertyHolder) {
        PropertyHolder p = (PropertyHolder)token;
        if (p.name.equals("#i")) {
          formatedValue.append(order);
        } else {
          try {
            int type = p.type;
            int colNum = p.colNum;

            switch (type) {
              case Types.BOOLEAN:
                boolean b = rs.getBoolean(colNum);
                if (!rs.wasNull())
                  formatedValue.append(b);
                else
                  formatedValue.append("null");
                break;
              case Types.DATE:
              case Types.TIME:
              case Types.TIMESTAMP:
                Timestamp ts = rs.getTimestamp(colNum);
                if (ts != null) {
                  String df = (p.format != null ? p.format : this.defaultDatePattern);
                  formatedValue.append(Renderer.renderDate(new Date(ts.getTime()), df, this.locale));
                } else {
                  formatedValue.append("null");
                }
                break;
              case Types.BIGINT:
              case Types.BIT:
              case Types.INTEGER:
              case Types.SMALLINT:
              case Types.TINYINT:
                long l = rs.getLong(colNum);
                if (!rs.wasNull())
                  formatedValue.append(l);
                else
                  formatedValue.append("null");
                break;
              case Types.DECIMAL:
              case Types.DOUBLE:
              case Types.FLOAT:
              case Types.NUMERIC:
              case Types.REAL:
                double d = rs.getDouble(colNum);
                if (!rs.wasNull())
                  formatedValue.append(d);
                else
                  formatedValue.append("null");
                break;
              default:
                String s = rs.getString(colNum);
                if (!rs.wasNull())
                  formatedValue.append("'" + TextUtils.escape(s) + "'");
                else
                  formatedValue.append("null");
            }
          } catch (SQLException ex) {
            throw new ConfigurationException(
                "Impossible to transform the column " + p.name + ". Problems accesing to the DB.",
                ex);
          }
        }
      } else {
        formatedValue.append(token);
      }
    }
    return formatedValue.toString();
  }

  private static int getColNum(String propName) throws NumberFormatException {
    try {
      return Integer.parseInt(propName.substring(1));
    } catch (NumberFormatException ex) {
      throw new ConfigurationException(
          "Property " + propName + " bad specified in format string");
    }
  }

  private static int getType(ResultSetMetaData rsmd, int col) throws SQLException {
    return rsmd.getColumnType(col);
  }

  private void lexeFormat(String format, ResultSet rs) throws SQLException {
    // Example: name[{#i}] = new YSTTypeName('{prop1}', {prop2}, '{prop3}');
    ResultSetMetaData rsMd = rs.getMetaData();
    int i = 0;
    StringBuffer token = new StringBuffer();
    int len = format.length();
    while (i < len) {
      if (format.charAt(i) == '\\' && (i + 1) < len &&
          (format.charAt(i + 1) == '{' || format.charAt(i + 1) == '}')) {
        i++;
      }
      if (format.charAt(i) == '{') {
        // Comienza propiedad. copia token a tokens
        if (i == 0 || i > 0 && format.charAt(i - 1) != '\\') {
          if (token.length() != 0)tokens.add(token.toString()); // Añado el token anterior
          token = new StringBuffer();
          StringBuffer prop = new StringBuffer();
          StringBuffer form = null;
          int init = i;
          i++;

          // Quita posibles espacios en blanco antes del nombre de la propiedad
          while (i < len && format.charAt(i) == ' ')i++;

          boolean propStarted = (format.charAt(i) != '%');

          while (i < len && format.charAt(i) != '}') {
            if (format.charAt(i) == '%') {
              // Comienzo de especificación de formato de propiedad
              form = new StringBuffer();
              if (!propStarted)
                throw new ConfigurationException("Column cannot begin with % in position " + (i + 1));

              i++;
              while (i < len && format.charAt(i) != '%') {
                if (format.charAt(i) == '\\' && (i + 1) < len && format.charAt(i + 1) == '%') {
                  // % escapado
                  i++;
                }
                // Cojo formato
                form.append(format.charAt(i));
                i++;
              }
              if (i == len) { // agotada cadena sin encontrar pareja %
                throw new ConfigurationException(
                    "Impossible to transform the column. Format specification not finished in position " +
                    (init + 1));
              }
            } else if (format.charAt(i) == '{') {
              throw new ConfigurationException(
                  "Impossible to transform the column. Nested { in position " + (i + 1));
            } else {
              if (format.charAt(i) != ' ' && form != null  && form.length() > 0)
                throw new ConfigurationException(
                    "Column bad specified. No text after format specification " + form +
                    " is allowed " + (init + 1));
              prop.append(format.charAt(i));
            }
            i++;
          }
          if (i == len) { // agotada cadena sin encontrar pareja }
            throw new ConfigurationException(
                "Impossible to transform the column. Curly brace mismatch for { in position " +
                (init + 1));
          } else { // Se acabó la propiedad
            // Se acabó la propiedad
            String propName = prop.toString().trim();
            if (propName.length() != 0) {
              if (propName.equals("#i")) {
                tokens.add(new PropertyHolder(propName, null));
              } else {
                int colNum;
                if (propName.charAt(0) == '#') {
                  colNum = getColNum(propName);
                } else {
                  try {
                    colNum = rs.findColumn(propName);
                  } catch (SQLException ex) {
                    throw new ConfigurationException(
                        "Impossible to transform the column " + propName +
                        ". Column does not exists.",
                        ex);
                  }
                }
                int type = getType(rsMd, colNum);
                // Oracle devuelve para todos sus tipos de numero NUMERIC
                // Miramos si tiene o no decimales para tratarlo como entero
                // SI no tiene escala ni precision (Numeric) es tb float
                if (type == Types.NUMERIC && rsMd.getPrecision(colNum) != 0 &&
                    rsMd.getScale(colNum) == 0) {
                  type = Types.BIGINT;
                }
                tokens.add(new PropertyHolder(propName, (form != null ? form.toString():null), type, colNum));
              }
            }
          }
        } else { // era una { escapada con \
          token.append(format.charAt(i));
        }
      } else if (format.charAt(i) == '}') { // Es una llave de cierre descolocada
        if (i == 0 || i > 0 && format.charAt(i - 1) != '\\') {
          throw new ConfigurationException(
              "Impossible to transform the column. Curly brace mismatch for } in position " +
              (i + 1));
        } else { // Una llave de cierre escapada con  \
          token.append(format.charAt(i));
        }
      } else { // texto normal
        token.append(format.charAt(i));
      }
      i++;
    }
    if (token.length() != 0) {
      tokens.add(token.toString());
    }
    this.lexed = true;
  }


  private class PropertyHolder {
    String name;
    String format;
    int type;
    int colNum;

    PropertyHolder(String name, String format, int type, int colNum) {
      this.name = name;
      this.type = type;
      this.colNum = colNum;
        this.format = format;
    }

    PropertyHolder(String name, String format) {
      this.name = name.trim();
        this.format = format;
    }
  }
}
