package dockerenv

object MongoEnv {

  def listUsers(env: DockerEnv.Instance) = env.runInScriptDir("mongo.sh", "listUsers.js")

  def createUser(env: DockerEnv.Instance) = env.runInScriptDir("mongo.sh", "createUser.js")

}
