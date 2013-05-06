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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Objects of this class represent queries to a data base whose result can be
 * directly included in a Yeast template following a transformation procedure
 * similar to the one performed for a bean collection. Each row of the query
 * result will be transformed following a specified format. The {@link
 * DBQueryTransformer} class must be used for this transformation.
 *
 * <p>You can build a <code>DBQuery</code> object in several ways. The final
 * objective is to get a <code>java.sql.ResultSet</code> object containing the
 * query result. You can directly provide this <code>ResultSet</code> or you can
 * give a SQL query and a connection to the data base
 * (<code>java.sql.Connection</code>). This connection may be provided in
 * several ways: directly, giving the connection url, or giving a
 * <code>javax.sql.DataSource</code> from which the connection can be obtained.
 *
 * <p>A <code>DBQuery</code> object must be transformed by a
 * <code>DBQueryTransformer</code> transformer. This transformer will take the
 * encapsulated <code>java.sql.ResultSet</code> and it will transform every row
 * in it. Once the <code>java.sql.ResultSet</code> is completely transformed,
 * then the connection to the data base should be closed.
 *
 * <p><b>Note about the JDBC connection closing</b>: if the <code>DBQuery</code>
 * object is supplied with an externally built JDBC connection, then the
 * decision about the connection closing must be taken externally also. If the
 * JDBC connection is internally built, then the connection is always closed by
 * the <code>DBQueryTransformer</code>.
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
 * <p>The YSTServlet will be as simple as: <pre>
 * import org.ystsrv.servlet.*;
 * import org.ystsrv.transformer.jdbc.*;
 *
 * public class YSTDemoDB extends YSTServlet {
 *   private static String BD_URL = "jdbc:odbc:JSTTest";
 *   private static String DRIVER_CLASS = "sun.jdbc.odbc.JdbcOdbcDriver";
 *   private static String USR = "", PWD = "";
 *
 *   // ... JDBC driver load details ommitted
 *
 *   protected String handle(YSTContext context) {
 *     DBQuery q = new DBQuery(BD_URL, USR, PWD,
 *                             "SELECT title, a_name+' '+a_surname, price, published FROM Book");
 *     context.toResponse(q);
 *     return "Choose";
 *   }
 * }</pre>
 *
 * <p>To be used in such a way, the template must be configured to use a
 * properly configured <code>DBQueryTransformer</code>. This configuration is
 * performed in the <code>YSTConfig.xml</code> file. In this case, for the
 * "ChooseBooks" template the configuration element should be:
 * <pre>
 *  &lt;template id="Choose"&gt;
 *    &lt;location&gt;ChooseBooks.html&lt;/location&gt;
 *    &lt;transformer class="org.ystsrv.transformer.jdbc.DBQueryTransformer"&gt;
 *      &lt;param name="header"&gt; books = new Array();&lt;/param&gt;
 *      &lt;param name="format"&gt; books[{#i}] = new Item({#2}, {title}, {price}, {published});&lt;/param&gt;
 *      &lt;param name="datePattern"&gt;dd-MM-yyyy&lt;/param&gt;
 *    &lt;/transformer&gt;
 *  &lt;/template&gt;</pre>
 * For more details about the configuration of <code>DBQueryTransformer</code>
 * see {@link DBQueryTransformer}
 *
 * @see DBQueryTransformer
 * @see DBQueryHelper
 * @see org.ystsrv.servlet.YSTServlet
 * @author Francisco José García Izquierdo
 * @version 1.0
 */
public class DBQuery {
  private static final String LOGGER_NAME = "ystsrv.transformer";

  private String URL;
  private String usr;
  private String pwd;

  private Connection connection;

  private DataSource dataSource;

  private String query;

  private ResultSet resultSet;

  private boolean forceCloseConnection;
  private boolean forceCloseStatement;

  private String DBQuery2Str;

  /**
   * Creates a <code>DBQuery</code> object with a
   * <code>java.sql.ResultSet</code> object containing the query result. This
   * <code>java.sql.ResultSet</code>, as well as its associated
   * <code>java.sql.Statement</code> and <code>java.sql.Connection</code> WILL
   * NOT BE CLOSED once the transformation performed by a {@link
   * DBQueryTransformer} is finished.
   *
   * @param resultSet <code>java.sql.ResultSet</code> object containing the
   *   query result
   */
  public DBQuery(ResultSet resultSet) {
    if (resultSet == null)
      throw new IllegalArgumentException("Null resultSet not allowed");

    this.resultSet = resultSet;
    // Me lo pasan de fuera --> que decidan fuera
    this.forceCloseConnection = false;
    this.forceCloseStatement = false;
    this.DBQuery2Str = "DBQuery object for already executed query";
  }

  /**
   * Creates a <code>DBQuery</code> object with a string containing the SQL
   * query, and a <code>java.sql.Connection</code> to the data base where the
   * query must be executed. Since the connection is externally created, the
   * associated <code>java.sql.Connection</code> WILL NOT BE CLOSED once the
   * transformation performed by a {@link DBQueryTransformer} is finished.
   *
   * @param con Connection to the data base
   * @param query SQL query to be executed
   */
  public DBQuery(Connection con, String query) {
    if (con == null)
      throw new IllegalArgumentException("Null connection not allowed");
    if (query == null || query.trim().length() == 0)
      throw new IllegalArgumentException("Null string in query not allowed");

    this.connection = con;
    // Me lo pasan de fuera --> que decidan fuera
    this.forceCloseConnection = false;
    this.forceCloseStatement = true;
    this.query = query;
    this.DBQuery2Str = "DBQuery object for query " + query;
    try {
      String c = con.getCatalog();
      this.DBQuery2Str += " through connection " + (c != null ? c : "");
    } catch (SQLException ex) {
    }
  }

  /**
   * Creates a <code>DBQuery</code> object with a string containing the SQL
   * query, and the URL, user name and password that will allow the
   * establishment of a connection to the data base where the query must be
   * executed. The associated <code>java.sql.Connection</code> WILL BE CLOSED
   * once the transformation performed by a {@link DBQueryTransformer} is
   * finished.
   *
   * @param conURL URL for the connection to the data base
   * @param usr user name
   * @param pwd password
   * @param query SQL query to be executed
   */
  public DBQuery(String conURL, String usr, String pwd, String query) {
    if (conURL == null || conURL.trim().length() == 0)
      throw new IllegalArgumentException("Null string in connection URL not allowed");
    if (query == null || query.trim().length() == 0)
      throw new IllegalArgumentException("Null string in query not allowed");

    this.URL = conURL;
    this.usr = usr;
    this.pwd = pwd;
    this.forceCloseConnection = true;
    this.query = query;
    this.DBQuery2Str = "DBQuery object for query " + query + " through connection " + conURL + ":" +
        usr + ":" + pwd;
  }

  /**
   * Creates a <code>DBQuery</code> object with a string containing the SQL
   * query, and a <code>javax.sql.DataSource</code> object that will allow the
   * establishment of a connection to the data base where the query must be
   * executed. The associated <code>java.sql.Connection</code> WILL BE CLOSED
   * once the transformation performed by a {@link DBQueryTransformer}
   * is finished.
   *
   * @param dataSource dataSource configured to provide data base connections
   * @param query SQL query to be executed
   */
  public DBQuery(DataSource dataSource, String query) {
    if (dataSource == null)throw new IllegalArgumentException("Null dataSource not allowed");
    if (query == null || query.trim().length() == 0)
      throw new IllegalArgumentException("Null string in query not allowed");

    this.dataSource = dataSource;
    this.forceCloseConnection = true;
    this.query = query;
    this.DBQuery2Str = "DBQuery object for query " + query;
    try {
      String c = dataSource.getConnection().getCatalog();
      this.DBQuery2Str += " through dataSource " + (c != null ? c : "");
    } catch (SQLException ex) {
    }
  }

  /**
   * Creates a <code>DBQuery</code> object with a string containing the SQL
   * query, and the name of a <code>DataSource</code> and a
   * <code>javax.naming.Context</code> object that will allow the establishment
   * of a connection to the data base where the query must be executed. The
   * associated <code>java.sql.Connection</code> WILL BE CLOSED once the
   * transformation performed by a {@link DBQueryTransformer} is finished.
   *
   * @param dsName name of the <code>DataSource</code> configured to provide
   *   data base connections
   * @param ctx Context that will e used to look up the <code>DataSource</code>
   *   object
   * @param query SQL query to be executed
   * @throws NamingException
   */
  public DBQuery(String dsName, Context ctx, String query) throws NamingException {
    if (dsName == null || dsName.trim().length() == 0)
      throw new IllegalArgumentException("Null string in dataSource name not allowed");
    if (query == null || query.trim().length() == 0)
      throw new IllegalArgumentException("Null string in query not allowed");

    if (ctx == null) {
      ctx = new InitialContext();
    }
    this.dataSource = (DataSource)ctx.lookup(dsName);
    this.forceCloseConnection = true;
    this.query = query;
    this.DBQuery2Str = "DBQuery object for query " + query + " through dataSource " + dsName;
  }

  /**
   * Creates a <code>DBQuery</code> object with a string containing the SQL
   * query, and the name of a <code>DataSource</code> that will allow the
   * establishment of a connection to the data base where the query must be
   * executed. The <code>InitialContext</code> is used to look up the
   * <code>DataSource</code> object. The associated
   * <code>java.sql.Connection</code> WILL BE CLOSED once the transformation
   * performed by a {@link DBQueryTransformer} is finished.
   *
   * @param dsName name of the <code>DataSource</code> configured to provide
   *   data base connections
   * @param query SQL query to be executed
   * @throws NamingException
   */
  public DBQuery(String dsName, String query) throws NamingException {
    this(dsName, null, query);
  }

  /**
   * Returns a boolean indicating if the data base connection used to retrieve
   * the query data must be closed or not once the <code>DBQuery</code> object
   * transformation is finished. See {@link #setMustCloseConnection} to understand the
   * connection closing details.
   *
   * @return boolean
   */
  boolean getMustCloseConnection() {
    return /*this.mustCloseConnection || */this.forceCloseConnection;
  }

  boolean getMustCloseStatement() {
    return this.forceCloseStatement || this.forceCloseConnection;
  }

  ResultSet getResultSet() throws SQLException {
    Connection con;
    if (this.resultSet != null) {
      return this.resultSet;
    } else if (this.connection != null) {
      // ya tengo la conexion
      con = this.connection;
    } else if (this.dataSource != null) {
      // coger la conexion del DS
      con = this.dataSource.getConnection();
    } else {
      con = DriverManager.getConnection(this.URL, this.usr, this.pwd);
    }
    // Aqui tengo la conexión
    PreparedStatement ps = con.prepareStatement(this.query);
    return ps.executeQuery();
  }

  String getQuery () {
    return query;
  }

  public String toString() {
    return this.DBQuery2Str;
  }
}
