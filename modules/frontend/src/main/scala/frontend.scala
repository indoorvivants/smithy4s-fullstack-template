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

  renderOnDomContentLoaded(
    dom.document.getElementById("root"),
    app
  )

end frontend

def renderPage(using
    router: Router[Page]
)(using Api, EventBus[Event]): Signal[HtmlElement] =
  SplitRender[Page, HtmlElement](router.currentPageSignal)
    .collectStatic(Page.Index)(indexPage)
    .signal

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
      cls := "rows",
      children <--
        events.toObservable.startWithNone.flatMapSwitch { _ =>
          api
            .stream(_.hello.getAll())
            .map(_.pairs)
            .map { pairs =>
              pairs.map { pair =>
                div(
                  cls := "row",
                  div(pair.key.value, className := "item-key"),
                  div(
                    pair.value.value,
                    className := "item-value"
                  ),
                  button(
                    "+",
                    increment(pair.key),
                    className := "item-increment-button"
                  ),
                  button(
                    "-",
                    decrement(pair.key),
                    className := "item-decrement-button"
                  ),
                  a(
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
