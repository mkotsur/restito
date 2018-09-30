package integration

import io.restassured.RestAssured
import io.restassured.RestAssured.given
import com.xebialabs.restito.builder.stub.StubHttp._
import com.xebialabs.restito.semantics.Action._
import com.xebialabs.restito.semantics.Condition
import com.xebialabs.restito.server.StubServer
import org.glassfish.grizzly.http.util.HttpStatus
import org.glassfish.grizzly.http.util.HttpStatus.{ACCEPTED_202, NON_AUTHORATIVE_INFORMATION_203, OK_200}
import org.hamcrest.Matchers.equalTo
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSpec, Matchers}


@RunWith(classOf[JUnitRunner])
class ActionSequencesTest extends FunSpec with BeforeAndAfterAll with Matchers {

  val server = new StubServer()

  override def beforeAll() = {
    super.beforeAll()
    server.run
    RestAssured.port = server.getPort
  }

  override protected def afterAll() = {
    super.afterAll()
    server.stop()
  }

  describe("Sequence action builder") {

    it("should add actions to the stub via 'withSequence' with an empty `then()`") {
      whenHttp(server).
        `match`(Condition.get("/demo")).
        `then`().
        withSequence(
          composite(stringContent("This is 1"), status(OK_200)),
          composite(stringContent("This is 2"), status(OK_200))
        )

      given().get("/demo").`then`().assertThat()
        .statusCode(OK_200.getStatusCode)
        .body(equalTo("This is 1"))

      given().get("/demo").`then`().assertThat()
        .statusCode(OK_200.getStatusCode)
        .body(equalTo("This is 2"))
    }

    it("should add actions to the stub via 'withSequence'") {
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

    it("should respond with a special action for all exceeding requests") {
      whenHttp(server).
        `match`(Condition.get("/demo")).
        `then`(status(HttpStatus.ACCEPTED_202)).
        withSequence(
          composite(stringContent("This is 1")),
          composite(stringContent("This is 2"))
        ).whenExceeded(status(NON_AUTHORATIVE_INFORMATION_203))

      given().get("/demo").`then`().assertThat()
        .statusCode(ACCEPTED_202.getStatusCode)
        .body(equalTo("This is 1"))

      given().get("/demo").`then`().assertThat()
        .statusCode(ACCEPTED_202.getStatusCode)
        .body(equalTo("This is 2"))

      given().get("/demo").`then`().assertThat()
        .statusCode(NON_AUTHORATIVE_INFORMATION_203.getStatusCode)
    }
  }
}
