package dockerenv

import _root_.oracle.jdbc.pool.OracleDataSource

import java.sql.Connection

case class RichConnect(conn: Connection) extends AutoCloseable {
  override def close(): Unit = conn.close()

  def metadata = conn.getMetaData()

}

case class OracleConf(url: String = "jdbc:oracle:thin:@localhost:1521:xe", username: String = "system", password: String = "oracle") {

  def connect = {
    val ods = new OracleDataSource()
    //        ods.setURL("jdbc:oracle:thin:scott/tiger@host:port/service")
    //        ods.setPortNumber(1521)
    ods.setURL(url)
    ods.setUser(username)
    ods.setPassword(password)
    RichConnect(ods.getConnection())
  }
}
