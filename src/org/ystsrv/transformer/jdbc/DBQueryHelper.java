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
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.ystsrv.TransformationException;
import org.ystsrv.debug.Debug;
import org.ystsrv.servlet.YSTContext;

/**
 * This is a utility class designer to be used in the <code>handle</code> method
 * of {@link org.ystsrv.servlet.YSTServlet}s.
 *
 * <p>If you are using a Yeast template configured to use several {@link DBQuery}
 * objects, you will have noticed that if you want to share the JDBC connection
 * for those several <code>DBQuery</code> objects you have to externally build
 * the connection and provide the <code>DBQuery</code> objects with it. The
 * connection life cycle must be controlled by you.
 *
 * <p>The main use of <code>DBQueryHelper</code> is to provide a means for
 * sharing the same JDBC connection among several <code>DBQuery</code> objects,
 * without worrying about its creation or closing.
 *
 * <p>Moreover, for those of you that think that using configuration files is
 * tedious, <code>DBQueryHelper</code> is a convenience class that can be used
 * to simplify the use of <code>DBQuery</code> and {@link DBQueryTransformer}
 * objects. You can build <code>DBQueryHelper</code> in several ways, but you
 * have always to provide a query, a way of getting a JDBC connection to the
 * data base where the query is going to be executed, and a format for the
 * transformation of the query result. For the format specification you have to
 * use a {@link TransformationFormat} object. Another set of constructors allows
 * you to specify a set of queries as well as a set of corresponding
 * transformation formats. All of these queries will be executed through the
 * same connection.
 *
 * <p>This class methods are thread-safe. You can share
 * <code>DBQueryHelper</code> objects among servlet requests.
 *
 * <h3>Example</h3>
 *
 * Imagine that we have to develop a <code>YSTServlet</code> that returns a Yeast
 * Template representing a web page that shows information about a list of
 * books.
 *
 * Consider we have a relational data base where there is stored a table called
 * <code>Book</code>, which contains information about the books. The table
 * schema defines fields for the id, title, author name (<code>a_name</code>)
 * and surname (<code>a_surname</code>), publisher (all of char type), price
 * (number) and publication date (<code>published</code>). We want to push the
 * <code>Book</code> table data into the Yeast template.
 *
 * <p>Imagine now that in the template’s design model a book is called
 * <code>Item</code>, and that they have the following attributes:
 * <code>author</code>, <code>title</code>, <code>price</code>, and
 * <code>pubDate</code>. You need to transform the table data into a JavaScript
 * array of <code>Item</code> objects named <code>books</code>:<pre>
 *   books = new Array();
 *   books[0] = new Item('David Baldacci', 'The Camel Club', 8.49, '04-03-2003');
 *   books[1] = new Item('Danni Goodman', 'Dynamic HTML', 23.8, '23-09-2002');</pre>
 *
 * In order to make up a combo list, besides this data, we need an array with
 * the publisher names:<pre>
 *   pub = new Array();
 *   pub[0] = 'Pub XX & Sonns';
 *   pub[1] = 'Int. ABC Publishing Co.';</pre>
 *
 * <p>The YSTServlet will be as simple as: <pre>
 * import org.ystsrv.TransformationException;
 * import org.ystsrv.servlet.*;
 * import org.ystsrv.transformer.jdbc.DBQuery;
 *
 * public class YSTDemoDB extends YSTServlet {
 *   private static String BD_URL = "jdbc:odbc:JSTTest";
 *   private static String DRIVER_CLASS = "sun.jdbc.odbc.JdbcOdbcDriver";
 *   private static String USR = "", PWD = "";
 *
 *   // ... JDBC driver load details ommitted
 *
 *   protected String handle(YSTContext context) {
 *     String queries[] = {"SELECT title, a_name+' '+a_surname, price, published FROM Book",
 *                         "SELECT distinct publicher FROM Book"};
 *     TransformationFormat formats[] = {
 *         new TransformationFormat("books = new Array();",
 *                                  "books[{#i}] = new Item({#2}, {title}, {price}, {published});",
 *                                  "dd-MM-yyyy"),
 *         new TransformationFormat("publishers = new Array();","pub[{#i}] = {#1};")};
 *
 *     DBQueryHelper dbqh = new DBQueryHelper(BD_URL, USR, PWD, queries, formats);
 *     dbqh.toResponse(context);
 *     return "ChooseBooks";
 *   }
 * }</pre>
 *
 * <p>No extra configuration is needed for the template. Both queries will be
 * executed through the same database connection.
 *
 * <p>In this case, since the queries are constant, the
 * <code>DBQueryHelper</code> object could be declared and initialized as a
 * member attribute of the servlet. All requests will share the same
 * <code>DBQueryHelper</code>: <pre>
 * import org.ystsrv.TransformationException;
 * import org.ystsrv.servlet.*;
 * import org.ystsrv.transformer.jdbc.DBQuery;
 *
 * public class YSTDemoDB extends YSTServlet {
 *   private static String BD_URL = "jdbc:odbc:JSTTest";
 *   private static String DRIVER_CLASS = "sun.jdbc.odbc.JdbcOdbcDriver";
 *   private static String USR = "", PWD = "";
 *
 *   // ... JDBC driver load details ommitted
 *
 *   private DBQueryHelper dbqh;
 *
 *   public void init() {
 *     String queries[] = {"SELECT title, a_name+' '+a_surname, price, published FROM Book",
 *                         "SELECT distinct publicher FROM Book"};
 *     TransformationFormat formats[] = {
 *         new TransformationFormat("books = new Array();",
 *                                  "books[{#i}] = new Item({#2}, {title}, {price}, {published});",
 *                                  "dd-MM-yyyy"),
 *         new TransformationFormat("publishers = new Array();","pub[{#i}] = {#1};")};
 *
 *     dbqh = new DBQueryHelper(BD_URL, USR, PWD, queries, formats);
 *   }
 *
 *   protected String handle(YSTContext context) {
 *     dbqh.toResponse(context);
 *     return "ChooseBooks";
 *   }
 * }</pre>
 *
 * @see DBQuery
 * @see org.ystsrv.servlet.YSTServlet
 *
 * @author Francisco José García Izquierdo
 * @version 1.0
 */
public class DBQueryHelper {
  private static final String LOGGER_NAME = "ystsrv.transformer";

  private String URL;
  private String usr;
  private String pwd;

  private DataSource dataSource;

  private String[] queries;
  private TransformationFormat[] formats;

  /**
   * Creates a <code>DBQueryHelper</code> for a SQL query (<code>query</code>)
   * using the parameters <code>conURL</code>, <code>usr</code> and
   * <code>pwd</code> to build a JDBC connection through which the query is
   * executed. The <code>format</code> parameter specifies the transformation
   * pattern that must be applied to the query result. The associated
   * <code>java.sql.Connection</code> WILL BE CLOSED once the transformation
   * is finished.
   *
   * @param conURL URL for the connection to the data base
   * @param usr user name to be used in the connection to the data base
   * @param pwd password to be used in the connection to the data base
   * @param query SQL query to be executed
   * @param format <code>TransformationFormat</code> object that specifies the
   *   transformation format
   */
  public DBQueryHelper(String conURL, String usr, String pwd, String query,
                       TransformationFormat format) {
    if (conURL == null || conURL.trim().length() == 0)
      throw new IllegalArgumentException("Null string in connection URL not allowed");
    this.URL = conURL;
    this.usr = usr;
    this.pwd = pwd;

    if (format == null)
      throw new IllegalArgumentException("Null format not allowed");
    this.formats = new TransformationFormat[] {format};
    if (query == null || query.trim().length() == 0)
      throw new IllegalArgumentException("Null string in query not allowed");

    this.queries = new String[] {query};
  }

  /**
   * Creates a <code>DBQueryHelper</code> for a set of SQL queries
   * (<code>queries</code>) using the parameters <code>conURL</code>,
   * <code>usr</code> and <code>pwd</code> to build a JDBC connection through
   * which the queries are executed. The <code>formats</code> parameter
   * specifies the set of corresponding transformation patterns that must be
   * applied to the query results. The associated
   * <code>java.sql.Connection</code> WILL BE CLOSED once the all the
   * transformations are
   * finished.
   *
   * @param conURL URL for the connection to the data base
   * @param usr user name to be used in the connection to the data base
   * @param pwd password to be used in the connection to the data base
   * @param queries SQL queries to be executed
   * @param formats array of <code>TransformationFormat</code> objects that
   *   specifies the transformation format for each query.
   */
  public DBQueryHelper(String conURL, String usr, String pwd, String queries[],
                       TransformationFormat formats[]) {
    if (conURL == null || conURL.trim().length() == 0)
      throw new IllegalArgumentException("Null string in connection URL not allowed");
    this.URL = conURL;
    this.usr = usr;
    this.pwd = pwd;

    checkArray(formats, "formats");
    checkArray(queries, "queries");
    this.formats = formats;
    this.queries = queries;
  }

  /**
   * Similar to {@link #DBQueryHelper(String conURL, String usr, String pwd,
   * String query, TransformationFormat format)} but, in this case the JDBC
   * connection is obtained by means of a <code>javax.sql.DataSource</code>
   * object (<code>dataSource</code>). The associated
   * <code>java.sql.Connection</code> WILL BE CLOSED once the transformation
   * is finished.
   *
   * @param dataSource provider of JDBC connections to the data base
   * @param query SQL query to be executed
   * @param format <code>TransformationFormat</code> object that specifies the
   *   transformation format
   */
  public DBQueryHelper(DataSource dataSource, String query, TransformationFormat format) {
    if (dataSource == null)throw new IllegalArgumentException("Null dataSource not allowed");
    this.dataSource = dataSource;

    if (format == null)throw new IllegalArgumentException("Null format not allowed");
    if (query == null || query.trim().length() == 0)throw new IllegalArgumentException(
        "Null string in query not allowed");

    this.formats = new TransformationFormat[] {format};
    this.queries = new String[] {query};
  }

  /**
   * Similar to {@link #DBQueryHelper(String conURL, String usr, String pwd,
   * String [] queries, TransformationFormat [] formats)} but, in this case the
   * JDBC connection is obtained by means of a <code>javax.sql.DataSource</code>
   * object (<code>dataSource</code>). All the queries are executed through the
   * same connection. The associated <code>java.sql.Connection</code> WILL BE
   * CLOSED once the transformation is
   * finished.
   *
   * @param dataSource provider of JDBC connections to the data base
   * @param queries SQL queries to be executed
   * @param formats array of <code>TransformationFormat</code> objects that
   *   specifies the transformation format for each query.
   */
  public DBQueryHelper(DataSource dataSource, String queries[], TransformationFormat formats[]) {
    if (dataSource == null)throw new IllegalArgumentException("Null dataSource not allowed");
    this.dataSource = dataSource;

    checkArray(formats, "formats");
    checkArray(queries, "queries");
    this.formats = formats;
    this.queries = queries;
  }

  /**
   * Similar to {@link #DBQueryHelper(String conURL, String usr, String pwd,
   * String query, TransformationFormat format)} but, in this case the JDBC
   * connection is obtained by means of a <code>javax.sql.DataSource</code>
   * object. This data source is obtained from the <code>ctx</code>
   * (<code>javax.naming.Context</code>) using the <code>dsName</code> name. The
   * associated <code>java.sql.Connection</code> WILL BE CLOSED once the
   * transformation is finished.
   *
   * @param dsName name of the data source
   * @param ctx <code>javax.naming.Context</code> from which the DataSource is
   *   obtained
   * @param query SQL query to be executed
   * @param format <code>TransformationFormat</code> object that specifies the
   *   transformation format
   * @throws NamingException
   */
  public DBQueryHelper(String dsName, Context ctx, String query, TransformationFormat format) throws
      NamingException {
    if (dsName == null || dsName.trim().length() == 0)
      throw new IllegalArgumentException("Null string in dataSource name not allowed");
    if (ctx == null) {
      ctx = new InitialContext();
    }
    this.dataSource = (DataSource)ctx.lookup(dsName);

    if (format == null)throw new IllegalArgumentException("Null format not allowed");
    if (query == null || query.trim().length() == 0)throw new IllegalArgumentException(
        "Null string in query not allowed");
    this.formats = new TransformationFormat[] {format};
    this.queries = new String[] {query};
  }

  /**
   * Similar to {@link #DBQueryHelper(String conURL, String usr, String pwd,
   * String [] queries, TransformationFormat [] formats)} but, in this case the
   * JDBC connection is obtained by means of a <code>javax.sql.DataSource</code>
   * object. This data source is obtained from the <code>ctx</code>
   * (<code>javax.naming.Context</code>) using the <code>dsName</code> name. All
   * the queries are executed through the same connection. The associated
   * <code>java.sql.Connection</code> WILL BE CLOSED once the transformation
   * is finished.
   *
   * @param dsName name of the data source
   * @param ctx <code>javax.naming.Context</code> from which the DataSource is
   *   obtained
   * @param queries SQL queries to be executed
   * @param formats array of <code>TransformationFormat</code> objects that
   *   specifies the transformation format for each query.
   * @throws NamingException
   */
  public DBQueryHelper(String dsName, Context ctx, String queries[], TransformationFormat formats[]) throws
      NamingException {
    if (dsName == null || dsName.trim().length() == 0)
      throw new IllegalArgumentException("Null string in dataSource name not allowed");
    if (ctx == null) {
      ctx = new InitialContext();
    }
    this.dataSource = (DataSource)ctx.lookup(dsName);

    checkArray(formats, "formats");
    checkArray(queries, "queries");
    this.formats = formats;
    this.queries = queries;

  }

  /**
   * Similar to {@link #DBQueryHelper(String conURL, String usr, String pwd,
   * String query, TransformationFormat format)} but, in this case the JDBC
   * connection is obtained by means of a <code>javax.sql.DataSource</code>
   * object. This data source is obtained from the default
   * <code>javax.naming.Context</code> using the <code>dsName</code> name. All
   * the queries are executed through the same connection. The associated
   * <code>java.sql.Connection</code> WILL BE CLOSED once the transformation
   * is finished.
   *
   * @param dsName name of the data source in the default context
   * @param query SQL query to be executed
   * @param format <code>TransformationFormat</code> object that specifies the
   *   transformation format
   * @throws NamingException
   */
  public DBQueryHelper(String dsName, String query, TransformationFormat format) throws
      NamingException {
    this(dsName, null, query, format);
  }

  /**
   * Similar to {@link #DBQueryHelper(String conURL, String usr, String pwd,
   * String query, TransformationFormat format)} but, in this case the JDBC
   * connection is obtained by means of a <code>javax.sql.DataSource</code>
   * object. This data source is obtained from the default
   * <code>javax.naming.Context</code> using the <code>dsName</code> name. The
   * associated <code>java.sql.Connection</code> WILL BE CLOSED once the
   * transformation is finished.
   *
   * @param dsName name of the data source in the default context
   * @param queries SQL queries to be executed
   * @param formats array of <code>TransformationFormat</code> objects that
   *   specifies the transformation format for each query.
   * @throws NamingException
   */
  public DBQueryHelper(String dsName, String queries[], TransformationFormat formats[]) throws
      NamingException {
    this(dsName, null, queries, formats);
  }

  /**
   * Transforms the result of the encapsulated queries using the corresponding
   * formats, and writes the transformation result in the Yeast template through
   * the supplied <code>YSTContext</code>. Once the transformation is finished,
   * the used JDBC connection is closed.
   *
   * @param context YSTContext
   * @throws TransformationException encapsulating any transformation problem,
   *   e.g. accessing to the data base.
   */
  public void toResponse(YSTContext context) throws TransformationException {
    Connection con = null;
    try {
      con = getConnection();
      for (int i = 0; i < queries.length; i++) {
        DBQueryTransformer t = DBQueryTransformerCache.getTransformer(this.formats[i].getHeader(),
            this.formats[i].getFormat(), this.formats[i].getDefaultDatePattern(),
            this.formats[i].getLocale(), this.queries[i]);
        DBQuery q = new DBQuery(con, queries[i]);
        context.toResponse(t.transform(q));
      }
    } catch (SQLException ex) {
      Debug.info(LOGGER_NAME, "Error connecting or reading the DB", ex);
      throw new TransformationException(ex);
    } finally {
      if (con != null)
        try {
          con.close();
          Debug.info(LOGGER_NAME, "Closed DB connection for DBHelper");
        } catch (SQLException ex1) {
          Debug.info(LOGGER_NAME, "I cannot close the DB connection", ex1);
        }
    }
  }

  private Connection getConnection() throws SQLException {
    Connection con;
    if (this.dataSource != null) {
      // coger la conexion del DS
      con = this.dataSource.getConnection();
    } else {
      con = DriverManager.getConnection(this.URL, this.usr, this.pwd);
    }
    return con;
  }

  private void checkArray(String[] array, String arrayName) {
    if (array == null || array.length == 0)
      throw new IllegalArgumentException("Param " + arrayName + " can not be null");
    for (int i = 0; i < array.length; i++) {
      if (array[i] == null || array[i].trim().length() == 0)
        throw new IllegalArgumentException("Component " + i + " of param " + arrayName +
                                           " can not be null nor empty");
    }
  }

  private void checkArray(Object[] array, String arrayName) {
    if (array == null || array.length == 0)
      throw new IllegalArgumentException("Param " + arrayName + " can not be null");
    for (int i = 0; i < array.length; i++) {
      if (array[i] == null)
        throw new IllegalArgumentException("Component " + i + " of param " + arrayName +
                                           " can not be null");
    }
  }
}
