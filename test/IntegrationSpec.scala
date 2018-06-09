import org.specs2.runner._
import org.junit.runner._
import play.api.test._

@RunWith(classOf[JUnitRunner])
class IntegrationSpec extends PlaySpecification {

  "Application" should {

    "transfer from non-empty account should success" in new WithApplication {
      val request = FakeRequest(POST, "/xfer").withFormUrlEncodedBody("accfrom" -> "1", "accto" -> "2", "amt" -> "50")
      val Some(xfer) = route(app, request)
      contentAsString(xfer) must equalTo("""{"res":"ok"}""")
    }

    "transfer resting money from non-empty account should success" in new WithApplication {
      val request = FakeRequest(POST, "/xfer").withFormUrlEncodedBody("accfrom" -> "1", "accto" -> "2", "amt" -> "50")
      val Some(xfer) = route(app, request)
      contentAsString(xfer) must equalTo("""{"res":"ok"}""")
    }

    "overdraft transfer from non-empty account should fail" in new WithApplication {
      val request = FakeRequest(POST, "/xfer").withFormUrlEncodedBody("accfrom" -> "2", "accto" -> "1", "amt" -> "201")
      val Some(xfer) = route(app, request)
      contentAsString(xfer) must equalTo("""{"error":"computer says no"}""")
    }

    "transfer from empty account should fail" in new WithApplication {
      val request = FakeRequest(POST, "/xfer").withFormUrlEncodedBody("accfrom" -> "3", "accto" -> "1", "amt" -> "1")
      val Some(xfer) = route(app, request)
      contentAsString(xfer) must equalTo("""{"error":"computer says no"}""")
    }

    "transfer from non-existing account should fail" in new WithApplication {
      val request = FakeRequest(POST, "/xfer").withFormUrlEncodedBody("accfrom" -> "4", "accto" -> "1", "amt" -> "50")
      val Some(xfer) = route(app, request)
      contentAsString(xfer) must equalTo("""{"error":"computer says no"}""")
    }

    "transfer to non-existing account should fail" in new WithApplication {
      val request = FakeRequest(POST, "/xfer").withFormUrlEncodedBody("accfrom" -> "2", "accto" -> "4", "amt" -> "50")
      val Some(xfer) = route(app, request)
      contentAsString(xfer) must equalTo("""{"error":"computer says no"}""")
    }

    "dump should success" in new WithApplication {
      val request = FakeRequest(GET, "/dump")
      val Some(dump) = route(app, request)
      contentAsString(dump) must equalTo("""[{"account":1,"balance":0},{"account":2,"balance":200},{"account":3,"balance":0}]""")
    }

  }
}
