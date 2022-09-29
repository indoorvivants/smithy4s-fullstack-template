// object asd{
//   println("Hello, world!")
// }

@main def hello =
  println(getClass.getResourceAsStream("assets/main.js").readAllBytes())
  println(getClass.getResourceAsStream("assets/test.txt").readAllBytes)
  println(System.getProperty("java.class.path"))
