package hellosmithy4s
package tests

import weaver.*
import spec.*
import cats.effect.IO

trait HelloSuite:
  self: BaseSuite =>

  probeTest("Creating") { probe =>
    import probe.api.hello.*
    val key = Key("creating")

    for
      _   <- create(key)
      lst <- getAll()
    yield expect(lst.pairs.contains(Pair(key, Value(0))))
  }

  probeTest("Creating with Value") { probe =>
    import probe.api.hello.*
    val key = Key("creating with value")

    for
      _   <- create(key, Some(Value(42069)))
      lst <- getAll()
    yield expect(lst.pairs.contains(Pair(key, Value(42069))))
  }

  probeTest("Getting") { probe =>
    import probe.api.hello.*
    val key = Key("getting")

    for
      _ <- create(key, Some(Value(42069)))
      k <- get(key)
    yield expect(k.value == Value(42069))
  }

  probeTest("Deleting") { probe =>
    import probe.api.hello.*
    val key = Key("deleting")

    for
      _   <- create(key)
      _   <- delete(key)
      lst <- getAll()
    yield expect(!lst.pairs.exists(_._1 == key))
  }

  probeTest("Incrementing") { probe =>
    import probe.api.hello.*
    val key = Key("incrementing")

    for
      _   <- create(key)
      _   <- inc(key)
      _   <- inc(key)
      lst <- getAll()
    yield expect(lst.pairs.contains(Pair(key, Value(2))))
  }

  probeTest("Decrementing") { probe =>
    import probe.api.hello.*
    val key = Key("decrementing")

    for
      _   <- create(key)
      _   <- dec(key)
      _   <- dec(key)
      lst <- getAll()
    yield expect(lst.pairs.contains(Pair(key, Value(-2))))
  }

  probeTest("Updating") { probe =>
    import probe.api.hello.*
    val key = Key("updating")

    for
      _   <- create(key)
      _   <- update(key, Value(42069))
      lst <- getAll()
    yield expect(lst.pairs.contains(Pair(key, Value(42069))))
  }

  probeTest("Error conditions") { probe =>
    import probe.api.hello.*
    val key = Key("blabla")

    for
      _              <- create(key)
      doubleCreation <- create(key).attempt
      _ <- expect(doubleCreation == Left(KeyAlreadyExists())).failFast

      _              <- delete(key)
      doubleDeletion <- delete(key).attempt
      _              <- expect(doubleDeletion == Left(KeyNotFound())).failFast

      getMissing <- get(key).attempt
      _          <- expect(getMissing == Left(KeyNotFound())).failFast

      incMissing <- inc(key).attempt
      _ <- IO.println(incMissing)
      _          <- expect(incMissing == Left(KeyNotFound())).failFast

      decMissing <- dec(key).attempt
      _          <- expect(decMissing == Left(KeyNotFound())).failFast

      updateMissing <- update(key, Value(42069)).attempt
      _             <- expect(updateMissing == Left(KeyNotFound())).failFast
    yield success
    end for
  }
end HelloSuite
