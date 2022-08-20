package hellosmithy4s

import scalacss.ProdDefaults.*
import scalacss.internal.StyleA

val Styles = new StyleSheet.Inline:
  import dsl.*

  val darkBlue  = rgb(0, 20, 24)
  val lightGray = grey(240)

  val Standalone = new StyleSheet.Standalone:
    import dsl.*
    "body" - (
      fontFamily :=! "'Wotfard',Futura,-apple-system,sans-serif"
    )
    "html" - (
      height := 100.%%
    )
  end Standalone

  val btn = style(
    padding(4.px),
    backgroundColor := lightGray,
    fontSize(1.5.rem)
  )
