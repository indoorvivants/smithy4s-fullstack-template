package hellosmithy4s

import com.raquo.laminar.api.L.*
import com.raquo.waypoint.Router
import org.scalajs.dom
import com.raquo.waypoint.SplitRender

import spec.*

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

enum Event:
  case KeyUpdated(i: Int)

@main def frontend =
  given Router[Page]    = Page.router
  given Api             = Api.create()
  given EventBus[Event] = new EventBus[Event]

  val app = div(
    child <-- renderPage
  )

  documentEvents.onDomContentLoaded.foreach { _ =>
    import scalacss.ProdDefaults.*

    val sty = styleTag(Styles.render[String], `type` := "text/css")
    dom.document.querySelector("head").appendChild(sty.ref)

    val sty1 = styleTag(GlobalStyles.render[String], `type` := "text/css")
    dom.document.querySelector("head").appendChild(sty1.ref)

    render(dom.document.getElementById("appContainer"), app)
  }(unsafeWindowOwner)
end frontend

def renderPage(using
    router: Router[Page]
)(using Api, EventBus[Event]): Signal[HtmlElement] =
  SplitRender[Page, HtmlElement](router.$currentPage)
    .collectStatic(Page.Index)(indexPage)
    .$view

def indexPage(using
    Router[Page]
)(using api: Api, events: EventBus[Event], ec: ExecutionContext) =
  inline def notification =
    events.emit(Event.KeyUpdated(scala.util.Random().nextInt()))

  inline def increment(inline k: Key) = onClick.preventDefault --> { _ =>
    api.future(_.hello.inc(k)).onComplete(_ => notification)
  }
  inline def decrement(inline k: Key) = onClick.preventDefault --> { _ =>
    api.future(_.hello.dec(k)).onComplete(_ => notification)
  }

  inline def delete(inline k: Key) = onClick.preventDefault --> { _ =>
    api
      .future(_.hello.delete(k))
      .onComplete(_ => notification)
  }

  val addForm = AddKeyForm(
    Observer(pair =>
      api
        .future(_.hello.create(pair.key, Some(pair.value)))
        .onComplete(_ => notification)
    )
  )

  div(
    addForm.node,
    div(
      Styles.rows,
      children <--
        events.toObservable.startWithNone.flatMap { _ =>
          api
            .stream(_.hello.getAll())
            .map(_.pairs)
            .map { pairs =>
              pairs.map { pair =>
                div(
                  Styles.row,
                  div(Styles.key, pair.key.value, className := "item-key"),
                  div(
                    Styles.value,
                    pair.value.value,
                    className := "item-value"
                  ),
                  button(
                    "+",
                    Styles.btn,
                    increment(pair.key),
                    className := "item-increment-button"
                  ),
                  button(
                    "-",
                    Styles.btn,
                    decrement(pair.key),
                    className := "item-decrement-button"
                  ),
                  a(
                    Styles.bigRedCross,
                    "X",
                    href := "#",
                    delete(pair.key),
                    className := "item-delete-button"
                  )
                )
              }
            }
        }
    )
  )
end indexPage
