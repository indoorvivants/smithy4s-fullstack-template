package hellosmithy4s

import hellosmithy4s.spec.*
import com.raquo.laminar.api.L
import com.raquo.laminar.api.L.*
import com.raquo.waypoint.*
import java.util.UUID

import io.circe.{*, given}
import io.circe.syntax.*
import smithy4s.Newtype
import scala.scalajs.js.JSON

def codec[A: Decoder: Encoder](nt: Newtype[A]): Codec[nt.Type] =
  val decT = summon[Decoder[A]].map(nt.apply)
  val encT = summon[Encoder[A]].contramap(nt.value)

  Codec.from(decT, encT)

given Codec[Key]   = codec(Key)
given Codec[Value] = codec(Value)

sealed trait Page derives Codec.AsObject
object Page:
  case object Index              extends Page
  case class KeySummary(id: Key) extends Page

  val mainRoute = Route.static(Page.Index, root / endOfSegments)

  val helloRoute = Route(
    encode = (stp: KeySummary) => stp.id.value,
    decode = (arg: String) => KeySummary(Key(arg)),
    pattern = root / "key-summary" / segment[String] / endOfSegments
  )

  val router = new Router[Page](
    routes = List(
      mainRoute,
      helloRoute
    ),
    getPageTitle = {
      case Index          => "Hello from Smithy4s!"
      case KeySummary(id) => s"Summary for key ${id.value}"
    },
    serializePage = pg => pg.asJson.noSpaces,
    deserializePage = str =>
      io.circe.scalajs.decodeJs[Page](JSON.parse(str)).fold(throw _, identity)
  )(
    popStateEvents = windowEvents(_.onPopState),
    owner = L.unsafeWindowOwner
  )
end Page

def redirectTo(pg: Page)(using router: Router[Page]) =
  router.pushState(pg)

def forceRedirectTo(pg: Page)(using router: Router[Page]) =
  router.replaceState(pg)

def navigateTo(page: Page)(using router: Router[Page]): Binder[HtmlElement] =
  Binder { el =>
    import org.scalajs.dom

    val isLinkElement = el.ref.isInstanceOf[dom.html.Anchor]

    if isLinkElement then el.amend(href(router.absoluteUrlForPage(page)))

    (onClick
      .filter(ev =>
        !(isLinkElement && (ev.ctrlKey || ev.metaKey || ev.shiftKey || ev.altKey))
      )
      .preventDefault
      --> (_ => redirectTo(page))).bind(el)
  }
