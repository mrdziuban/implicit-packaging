package example

trait Instances1 {
  implicit val testInt: TypeClass[Int] = new TypeClass[Int] {}
}
