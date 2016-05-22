package integration

import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured.given
import com.xebialabs.restito.builder.stub.StubHttp._
import com.xebialabs.restito.semantics.Action._
import com.xebialabs.restito.semantics.Condition
import com.xebialabs.restito.server.StubServer
import org.glassfish.grizzly.http.util.HttpStatus
import org.glassfish.grizzly.http.util.HttpStatus.ACCEPTED_202
import org.hamcrest.Matchers.equalTo
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSpec, Matchers}


@RunWith(classOf[JUnitRunner])
class ActionSequencesTest extends FunSpec with BeforeAndAfterAll with Matchers {

  describe("Sequence action builder") {

    it("should add actions to the stub via 'withSequence'") {

      val server = new StubServer().run
      RestAssured.port = server.getPort

      whenHttp(server).
        `match`(Condition.get("/demo")).
        `then`(status(HttpStatus.ACCEPTED_202)).
        withSequence(
          composite(stringContent("This is 1")),
          composite(stringContent("This is 2"))
        )

      given().get("/demo").`then`().assertThat()
        .statusCode(ACCEPTED_202.getStatusCode)
        .body(equalTo("This is 1"))

      given().get("/demo").`then`().assertThat()
        .statusCode(ACCEPTED_202.getStatusCode)
        .body(equalTo("This is 2"))
    }
  }

}
