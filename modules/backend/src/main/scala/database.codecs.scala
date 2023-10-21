package hellosmithy4s

import skunk.Codec
import skunk.codec.all.*
import smithy4s.Newtype
import hellosmithy4s.spec.*

object codecs:
  extension [T](c: Codec[T])
    def as(obj: Newtype[T]): Codec[obj.Type] =
      c.imap(obj.apply(_))(_.value)

  val userId: Codec[Key]       = varchar(50).as(Key)
  val value: Codec[Value]       = int4.as(Value)


end codecs