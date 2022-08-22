package hellosmithy4s
package tests
package stub

import cats.effect.kernel.Resource

object HelloTests extends BaseSuite with HelloSuite:
  override def sharedResource = buildApp
