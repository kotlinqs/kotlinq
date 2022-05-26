### What is it

Kotlinq is a compiler plugin which parses lambda expressions and adds their AST presentation into code, so it could be parsed in runtime. Like that.

```kotlin

import io.github.kotlinq.expression

// Marks that method must be processed by Kotlinq
@Kotlinq
fun main() {
    val lambda: (Int) -> Int = { it * 3 }
    println(lambda.expression)
}
```

Output:
```
Multiply([LambdaArgument(number=0), Value(value=3)])
```

Modified source file
```kotlin
package io
import io.github.kotlinq.*
import io.github.kotlinq.expression.node.*

import io.github.kotlinq.Kotlinq
import io.github.kotlinq.expression

@Kotlinq
fun main() {
    val lambda: (Int) -> Int = (withExpression1({ it * 3 }, { Multiply(LambdaArgument(0),Value(3)) }))
    println(lambda.expression)
}
```

### Queryable<T>

Also Kotlinq provides interface `Queryable<T>` which looks like LINQ's Queryable
and allows to write same code to select data from memory and from DB.

Take a look at JOOQ example: TBD link

### How to use

1. Add kotlinq plugin to your `build.gradle`
   ```groovy
   buildscript {
      repositories {
         maven("https://jitpack.io")
      }
      dependencies {
         classpath("com.github.kotlinqs.kotlinq:com.github.kotlinqs.gradle.plugin:0.1-SNAPSHOT")
      }
   }
   
   apply(plugin="com.github.kotlinqs")
   ```
3. Add configuration into your `build.gradle` (optional)
   ```groovy
   kotlinq {
        /// If set to true then patched sources are stored in temp folder
        /// like this on windows: "C:\Users\~username~\AppData\Local\kotlin\daemon\build\temp"
        debug = true
   
        /// If set to true then symbols with first upper letter are recognized as constructor calls
        /// and with lowercase letter - as method calls
        upperCaseIsClassName = true
   
        /// Adds package to processing. All lambdas in that package will be processed.
        package("io.github")

        /// MyRecord will be recognized as class name, even it is started with lowercase letter 
        constructors("myRecord")
   
        /// Kotlinq will not try to use references to methods/functions for those symbols
        ignore("listOf", "toInt")
   }
   ``` 
4. If you not specified packages in config then add `@Kotlinq` annotation for classes/functions you'd like to have lambdas parsed to expressions.
   If you specified packages, then all lambdas in that packages will be parsed. To ignore some classes add `@Kotlinq(off=true)` annotation.
5. Write method which accepts lambda, pass lambda to it. Inside method call `println(lambda.expression)`, build, run and see what happens.

