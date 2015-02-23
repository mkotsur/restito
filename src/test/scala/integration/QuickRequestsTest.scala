package integration

import akka.actor.ActorSystem
import com.xebialabs.restito.builder.stub.StubHttp._
import com.xebialabs.restito.builder.verify.VerifyHttp._
import com.xebialabs.restito.semantics.Action._
import com.xebialabs.restito.semantics.Condition._
import com.xebialabs.restito.server.StubServer
import org.glassfish.grizzly.http.util.HttpStatus
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSpec, Matchers}
import spray.client.pipelining._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

@RunWith(classOf[JUnitRunner])
class QuickRequestsTest extends FunSpec with BeforeAndAfterAll with  Matchers {

  implicit val system: ActorSystem = ActorSystem()

  describe("restito server") {
    it("should register quick requests happening in parallel") {
      val server = new StubServer().run

      whenHttp(server)
        .`match`(startsWithUri("/test"))
        .then(status(HttpStatus.OK_200))

      val pipeline = sendReceive
      val req = Get(s"http://localhost:${server.getPort}/test")


      Await.result(Future.sequence(Seq(
        pipeline(req),
        pipeline(req),
        pipeline(req),
        pipeline(req),
        pipeline(req)
      )), 10 second)

      verifyHttp(server)
      .times(5, get("/test"))

    }
  }

}
