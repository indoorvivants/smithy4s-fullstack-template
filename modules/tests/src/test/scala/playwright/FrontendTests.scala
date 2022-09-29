package hellosmithy4s
package tests
package playwright

import com.indoorvivants.weaver.playwright.*

import cats.syntax.all.*
import cats.effect.IO
import org.http4s.Uri
import java.nio.file.Paths
import scala.jdk.CollectionConverters.*

import scala.concurrent.duration.*

case class Resources(
    probe: Probe,
    baseUri: Uri,
    pw: PlaywrightRuntime
)

object FrontendTests extends weaver.IOSuite with PlaywrightIntegration:
  override type Res = Resources
  override def sharedResource =
    integration.buildApp.parProduct(PlaywrightRuntime.create()).map {
      case ((probe, server), pw) =>
        Resources(probe, server.baseUri, pw)
    }

  override def getPlaywright(res: Res): PlaywrightRuntime = res.pw
  override def retryPolicy: PlaywrightRetry =
    PlaywrightRetry.linear(20, 250.millis) // 5 seconds max

  test("basics") { pb =>
    getPageContext(pb).use { pc =>
      for
        _ <- pc.page(_.setDefaultTimeout(3000))
        _ <- pc.page(_.navigate(pb.baseUri.toString))
        _ <- eventually(pc.page(_.title)) { title =>
          expect.same(title, "Hello from Smithy4s!")
        }
      yield success
    }
  }

  test("adding") { pb =>
    val keyName = "new key!"
    getPageContext(pb).use { pc =>
      val pf = PageFragments(pc)
      for
        _   <- pc.page(_.setDefaultTimeout(10000))
        _   <- pc.page(_.navigate(pb.baseUri.toString))
        _   <- pf.createKey(keyName, 211)
        idx <- pf.waitUntilKeyAppears(keyName)
        value = pc
          .locator(s".item-value >> nth=$idx")
          .map(_.first().textContent())
        _ <- eventually(value) { text =>
          expect(text == "211")
        }
      yield success
      end for
    }
  }

  test("deleting") { pb =>
    val keyName = "key-for-deletion"
    getPageContext(pb).use { pc =>
      val pf = PageFragments(pc)
      for
        _   <- pc.page(_.setDefaultTimeout(10000))
        _   <- pc.page(_.navigate(pb.baseUri.toString))
        _   <- pf.createKey(keyName, 211)
        idx <- pf.waitUntilKeyAppears(keyName)
        _ <- pc
          .locator(s".item-delete-button >> nth=$idx")
          .map(_.first().click())
        _ <- eventually(pf.keysOnPage) { keys =>
          expect(!keys.contains(keyName))
        }
      yield success
      end for
    }
  }

  test("incrementing/decrementing") { pb =>
    val keyName  = "inc-dec-key"
    val keyValue = 257
    getPageContext(pb).use { pc =>
      val pf = PageFragments(pc)

      for
        _   <- pc.page(_.setDefaultTimeout(10000))
        _   <- pc.page(_.navigate(pb.baseUri.toString))
        _   <- pf.createKey(keyName, keyValue)
        idx <- pf.waitUntilKeyAppears(keyName)
        valuesOnPage = pc.locator(".item-value").map(_.allInnerTexts().asScala)
        inc <- pc.locator(s".item-increment-button >> nth=$idx")
        dec <- pc.locator(s".item-decrement-button >> nth=$idx")

        _ <- IO(inc.click())
        _ <- eventually(valuesOnPage) { values =>
          expect(values.contains(s"${keyValue + 1}"))
        }

        _ <- IO(dec.click())
        _ <- eventually(valuesOnPage) { values =>
          expect(values.contains(s"${keyValue}"))
        }

        _ <- IO(dec.click())
        _ <- eventually(valuesOnPage) { values =>
          expect(values.contains(s"${keyValue - 1}"))
        }
      yield success
      end for
    }
  }

  private case class PageFragments(pc: PageContext):
    def keysOnPage = pc.locator(".item-key").map(_.allInnerTexts().asScala)

    def createKey(keyName: String, keyValue: Int) =
      for
        key    <- pc.locator("#input-key").map(_.first().fill(keyName))
        value  <- pc.locator("#input-value").map(_.first().fill(s"$keyValue"))
        button <- pc.locator("#input-submit").map(_.first().click())
      yield ()

    def waitUntilKeyAppears(keyName: String) =
      for
        _ <- eventually(keysOnPage) { keys =>
          expect(keys.contains(keyName))
        }
        idx <- keysOnPage.map(_.indexOf(keyName))
      yield idx
  end PageFragments
end FrontendTests
