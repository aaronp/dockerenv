package dockerenv

import _root_.oracle.jdbc.pool.OracleDataSource
import scalikejdbc.{DBSession, WrappedResultSet}
import scalikejdbc._

import java.sql.Connection

object WideTable {
  def createSql = """CREATE TABLE Persons (
                    |    PersonID int,
                    |    LastName varchar(255),
                    |    FirstName varchar(255),
                    |    Address varchar(255),
                    |    City varchar(255)
                    |);""".stripMargin.linesIterator.mkString(" ")

  def create(implicit session: DBSession): Boolean = {
//    println(createSql)
//    sql"$createSql".execute().apply()
    sql"CREATE TABLE Persons (PersonID int, LastName varchar(255), FirstName varchar(255), Address varchar(255), City varchar(255) );".execute().apply()
  }
}

case class TableDefn(owner: String, name: String)
object TableDefn {
  def apply(rs: WrappedResultSet): TableDefn = {
    new TableDefn(rs.string("owner"), rs.string("table_name"))
  }
}

case class RichConnect(conn: Connection) extends AutoCloseable {
  override def close(): Unit = conn.close()

  implicit lazy val session: DBSession = DBSession(conn)

  def metadata = conn.getMetaData()

  def listTables(implicit session: DBSession): List[TableDefn] = {
    sql"SELECT table_name, owner FROM user_tables ORDER BY owner, table_name".map(TableDefn.apply).list().apply()
  }
}

/**
  * https://hub.docker.com/r/oracleinanutshell/oracle-xe-11g
  *
  * @param url
  * @param username
  * @param password
  */
case class OracleConf(url: String = "jdbc:oracle:thin:@localhost:1521:xe", username: String = "system", password: String = "oracle") {

  def connect: RichConnect = {
    val ods = new OracleDataSource()
    //        ods.setURL("jdbc:oracle:thin:scott/tiger@host:port/service")
    //        ods.setPortNumber(1521)
    ods.setURL(url)
    ods.setUser(username)
    ods.setPassword(password)
    RichConnect(ods.getConnection())
  }
}
