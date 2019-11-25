# Scala implicit packaging

This repo demonstrates a pattern for including implicit type class instances in
the companion object of a type class but still allowing for them to be defined
in separate files. The pattern involves defining the typeclass in one file and a
trait with instances of the type class in another file. Then the trait can be
extended by the type class' companion object to bring the instances into scope,
e.g.

#### `TypeClass.scala`

```scala
trait TypeClass[A]
object TypeClass extends Instances1
```

#### `Instances1.scala`

```scala
trait Instances1 {
  implicit val tcInt: TypeClass[Int] = new TypeClass[Int] {}
}
```

When adding new instances, you can define another trait and add it to the
parents of the companion object, e.g.

#### `TypeClass.scala`

```diff
trait TypeClass[A]
-object TypeClass extends Instances1
+object TypeClass extends Instances1 with Instances2
```

#### `Instances2.scala`

```scala
trait Instances2 {
  implicit val tcStr: TypeClass[String] = new TypeClass[String] {}
}
```

## Problem with this approach

From what I can tell, when changes are made to one of the instances traits,
SBT/zinc's incremental compilation first invalidates and recompiles the trait
itself, but then also invalidates and recompiles the `TypeClass` companion
object and any other instances traits that it extends. For example, when running
`~compile` in this project, commenting out the `implicit val` in
`Instances2.scala` triggers the following output:

```
[info] Build triggered by implicit-packaging/src/main/scala/example/Instances2.scala. Running 'compile'.
[info] Compiling 1 Scala source to implicit-packaging/target/scala-2.12/classes ...
[info] Compiling 3 Scala sources to implicit-packaging/target/scala-2.12/classes ...
```

Adding `Instances3.scala` causes the second line to say `4 scala sources` --
the `TypeClass` companion object and the three `Instances` traits.

Given the above, the issue can be generalized to say that given `N` traits
defining instances, a change to one of them will trigger `N + 1` sources to be
recompiled.
