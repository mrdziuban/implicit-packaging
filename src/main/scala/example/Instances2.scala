package example

trait Instances2 {
  implicit val testString: TypeClass[String] = new TypeClass[String] {}
}
