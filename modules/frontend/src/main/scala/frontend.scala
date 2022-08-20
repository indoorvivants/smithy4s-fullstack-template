package hellosmithy4s

import com.raquo.laminar.api.L.*
import com.raquo.waypoint.Router
import org.scalajs.dom

@main def frontend =
  given Router[Page] = Page.router
  // given Api = Api.create()

  val app = div(
    "howdy"
  )

  documentEvents.onDomContentLoaded.foreach { _ =>
    import scalacss.ProdDefaults.*

    val sty = styleTag(Styles.render[String], `type` := "text/css")
    dom.document.querySelector("head").appendChild(sty.ref)

    render(dom.document.getElementById("appContainer"), app)
  }(unsafeWindowOwner)
end frontend
