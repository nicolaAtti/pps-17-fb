import scala.concurrent.Future
import org.mongodb.scala._
import org.mongodb.scala.bson.BsonValue
import org.mongodb.scala.model.Filters
import org.mongodb.scala.result.DeleteResult

object AsyncDbManager {

  val mongoClient = MongoClient("mongodb://login-guest-service:pps-17-fb@ds039291.mlab.com:39291/heroku_3bppsqjk")
  val database = mongoClient.getDatabase("heroku_3bppsqjk")
  val collection = database.getCollection("casual-queue")
  val documentId = "casual-queue"

  val filter = Filters.equal("_id", value = documentId)

  def findPlayerInQueue: Future[(String, Seq[String], String)] = {
    collection
      .findOneAndDelete(filter)
      .map(document =>
        (document("playerName").asString().getValue, document("teamMembers").asArray().toArray.toSeq.collect {
          case str: BsonValue => str.asString().getValue
        }, document("replyToQueue").asString().getValue))
      .head()
  }

  def putPlayerInQueue(playerName: String, teamMembers: Seq[String], replyTo: String): Future[Completed] = {
    println("poroo")
    collection
      .insertOne(
        Document("_id" -> documentId,
                 "playerName" -> playerName,
                 "teamMembers" -> teamMembers,
                 "replyToQueue" -> replyTo))
      .head()
  }

  def removeQueuedPlayer(playerName: String): Future[DeleteResult] = {
    collection.deleteOne(Filters.equal("playerName", playerName)).head()
  }

}