import org.specs2.runner._
import org.junit.runner._
import play.api.test._
import play.api.db.DBApi
import play.api.db.evolutions.Evolutions

@RunWith(classOf[JUnitRunner])
class IntegrationSpec extends PlaySpecification {

  "Application" should {

    "transfer should not deadlock" in new WithApplication {
      val request12 = FakeRequest(POST, "/xfer").withFormUrlEncodedBody("accfrom" -> "1", "accto" -> "2", "amt" -> "1")
      val request21 = FakeRequest(POST, "/xfer").withFormUrlEncodedBody("accfrom" -> "2", "accto" -> "1", "amt" -> "1")

      def deadlockAttempt = {
        val (Some(xfer12), Some(xfer21)) = route(app, request12) -> route(app, request21)
        List(xfer12, xfer21)
      }

      val xfers = (1 to 50).flatMap(_ => deadlockAttempt)
      xfers.foreach(r => contentAsString(r) must equalTo("""{"res":"ok"}"""))

      val request = FakeRequest(GET, "/dump")
      val Some(dump) = route(app, request)
      contentAsString(dump) must equalTo(
        """[{"account":1,"balance":10000},{"account":2,"balance":10000},{"account":3,"balance":0}]""")
    }

    "transfer from non-empty account should success" in new WithApplication {
      val request = FakeRequest(POST, "/xfer").withFormUrlEncodedBody("accfrom" -> "1", "accto" -> "2", "amt" -> "5000")
      val Some(xfer) = route(app, request)
      contentAsString(xfer) must equalTo("""{"res":"ok"}""")
    }

    "transfer resting money from non-empty account should success" in new WithApplication {
      val request = FakeRequest(POST, "/xfer").withFormUrlEncodedBody("accfrom" -> "1", "accto" -> "2", "amt" -> "5000")
      val Some(xfer) = route(app, request)
      contentAsString(xfer) must equalTo("""{"res":"ok"}""")
    }

    "transfer zero amount should fail" in new WithApplication {
      val request = FakeRequest(POST, "/xfer").withFormUrlEncodedBody("accfrom" -> "3", "accto" -> "1", "amt" -> "0")
      val Some(xfer) = route(app, request)
      contentAsString(xfer) must equalTo("""{"error":"computer says no"}""")
    }

    "transfer to the same non-empty account should fail" in new WithApplication {
      val request =
        FakeRequest(POST, "/xfer").withFormUrlEncodedBody("accfrom" -> "2", "accto" -> "2", "amt" -> "20000")
      val Some(xfer) = route(app, request)
      contentAsString(xfer) must equalTo("""{"error":"computer says no"}""")
    }

    "overdraft transfer to the same non-empty account should fail" in new WithApplication {
      val request =
        FakeRequest(POST, "/xfer").withFormUrlEncodedBody("accfrom" -> "2", "accto" -> "2", "amt" -> "20001")
      val Some(xfer) = route(app, request)
      contentAsString(xfer) must equalTo("""{"error":"computer says no"}""")
    }

    "overdraft transfer from non-empty account should fail" in new WithApplication {
      val request =
        FakeRequest(POST, "/xfer").withFormUrlEncodedBody("accfrom" -> "2", "accto" -> "1", "amt" -> "20001")
      val Some(xfer) = route(app, request)
      contentAsString(xfer) must equalTo("""{"error":"computer says no"}""")
    }

    "transfer from empty account should fail" in new WithApplication {
      val request = FakeRequest(POST, "/xfer").withFormUrlEncodedBody("accfrom" -> "3", "accto" -> "1", "amt" -> "1")
      val Some(xfer) = route(app, request)
      contentAsString(xfer) must equalTo("""{"error":"computer says no"}""")
    }

    "transfer from non-existing account should fail" in new WithApplication {
      val request = FakeRequest(POST, "/xfer").withFormUrlEncodedBody("accfrom" -> "4", "accto" -> "1", "amt" -> "5000")
      val Some(xfer) = route(app, request)
      contentAsString(xfer) must equalTo("""{"error":"computer says no"}""")
    }

    "transfer to non-existing account should fail" in new WithApplication {
      val request = FakeRequest(POST, "/xfer").withFormUrlEncodedBody("accfrom" -> "2", "accto" -> "4", "amt" -> "5000")
      val Some(xfer) = route(app, request)
      contentAsString(xfer) must equalTo("""{"error":"computer says no"}""")
    }

    "transfer negative amount should fail" in new WithApplication {
      val request =
        FakeRequest(POST, "/xfer").withFormUrlEncodedBody("accfrom" -> "2", "accto" -> "3", "amt" -> "-5000")
      val Some(xfer) = route(app, request)
      contentAsString(xfer) must equalTo("""{"error":{"amt":["Must be greater or equal to 0"]}}""")
    }

    "dump should success" in new WithApplication {
      val request = FakeRequest(GET, "/dump")
      val Some(dump) = route(app, request)
      contentAsString(dump) must equalTo(
        """[{"account":1,"balance":0},{"account":2,"balance":20000},{"account":3,"balance":0}]""")
    }

    "cleanup" in new WithApplication {
      // reset test data
      val db = app.injector.instanceOf(classOf[DBApi]).database("default")
      Evolutions.cleanupEvolutions(db)
      Evolutions.applyEvolutions(db)
    }

  }
}
