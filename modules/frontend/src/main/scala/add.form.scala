package hellosmithy4s

import com.raquo.laminar.api.L.*
import hellosmithy4s.spec.*
import cats.syntax.all.*

class AddKeyForm private (val node: Element)
object AddKeyForm:
  def apply(obs: Observer[Pair]) =

    val keyVar   = Var(Option.empty[String])
    val valueVar = Var(0)

    val node =
      form(
        onSubmit.preventDefault --> { _ =>
          keyVar.now().foreach { key =>
            obs.onNext(Pair(Key(key), Value(valueVar.now())))
            keyVar.set(None)
            valueVar.set(0)
          }
        },
        div(
          Styles.addForm,
          input(
            tpe         := "text",
            placeholder := "key",
            value <-- keyVar.signal.map(_.getOrElse("")),
            onInput.mapToValue
              .map(_.trim)
              .map(s => Option.when(s.nonEmpty)(s)) --> keyVar.writer,
            Styles.inp
          ),
          input(
            tpe         := "text",
            placeholder := "value",
            value <-- valueVar.signal.map(_.toString),
            onInput.mapToValue
              .map(_.toIntOption)
              .map(_.getOrElse(0)) --> valueVar.writer,
            Styles.inp
          ),
          button(tpe := "submit", "add", Styles.btn)
        )
      )

    new AddKeyForm(node)
  end apply
end AddKeyForm
