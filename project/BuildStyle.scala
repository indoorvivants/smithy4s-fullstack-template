import sbt.VirtualAxis

sealed abstract class BuildStyle(
    val idSuffix: String,
    val directorySuffix: String
) extends VirtualAxis.WeakAxis with Product with Serializable

object BuildStyle {
  case object SingleFile extends BuildStyle("-bundle", "bundle")
  case object Modules    extends BuildStyle("-modules", "modules")
}
// import sbt.VirtualAxis.ScalaVersionAxis

// import sbt.VirtualAxis

// object Settings {
//   case class CatsEffectAxis(idSuffix: String, directorySuffix: String)
//       extends VirtualAxis.WeakAxis

//   lazy val CatsEffect2Axis = CatsEffectAxis("_CE2", "ce2")
//   lazy val CatsEffect3Axis = CatsEffectAxis("_CE3", "ce3")
// }
