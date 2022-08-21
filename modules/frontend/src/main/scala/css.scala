package hellosmithy4s

import scalacss.ProdDefaults.*
import scalacss.internal.StyleA
import com.raquo.laminar.api.L.*
import scalacss.internal.Attrs.direction

trait Common extends scalacss.internal.mutable.StyleSheet.Base:
  import dsl.*

  val darkBlue  = rgb(0, 20, 24)
  val lightGray = grey(240)

object Styles extends StyleSheet.Inline, Common:
  import dsl.*

  val btn = style(
    padding(10.px),
    fontSize(2.rem)
  )

  val inp = style(
    padding(10.px),
    fontSize(2.rem)
  )

  val rows = style(
    display.flex,
    flexDirection.column,
    gap(20.px),
    margin.auto,
    maxWidth(500.px)
  )

  val row = style(
    display.flex,
    flexDirection.row,
    justifyContent.spaceBetween,
    gap(15.px),
    borderBottom(1.px, dashed, white)
  )

  val addForm = style(
    display.flex,
    flexDirection.row,
    justifyContent.center,
    gap(15.px),
    border(2.px, solid, white),
    borderRadius(15.px),
    padding(10.px),
    marginBottom(50.px)
  )

  val key = style(
    width(200.px)
  )
  val value = style(
    width(50.px)
  )

  val bigRedCross = style(
    color.red,
    textDecorationLine.none,
    &.hover - (
      textDecorationLine.underline
    )
  )
end Styles

object GlobalStyles extends StyleSheet.Standalone, Common:
  import dsl.*
  import Styles.*
  "body" - (
    backgroundColor := darkBlue,
    color.white,
    fontSize(1.8.rem),
    fontFamily :=! "'Wotfard',Futura,-apple-system,sans-serif"
  )
  "html" - (
    height := 100.%%
  )
end GlobalStyles

given styleBinder: Conversion[StyleA, Modifier[HtmlElement]] with
  def apply(st: StyleA): Modifier[HtmlElement] = (cls := st.htmlClass)
