package hellosmithy4s
package tests
package stub

import cats.effect.kernel.Resource

def buildApp: Resource[cats.effect.IO, Probe] =
  for
    db <- Resource.eval(InMemoryDatabase.create)
    logger = scribe.cats.io
    probe  <- Probe.build(logger, db)
    routes <- Routes.build(probe.api)
  yield probe
