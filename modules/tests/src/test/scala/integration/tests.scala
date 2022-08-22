package hellosmithy4s
package tests
package integration

import cats.effect.kernel.Resource

object HelloTests extends BaseSuite with HelloSuite:
  override def sharedResource: Resource[cats.effect.IO, Res] =
    buildApp
