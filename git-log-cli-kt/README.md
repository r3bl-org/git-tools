<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

- [Future tasks - Add a prettier UI](#future-tasks---add-a-prettier-ui)
- [git-log](#git-log)
- [Using Kotlin to create a self executing JAR file that can be run from command line](#using-kotlin-to-create-a-self-executing-jar-file-that-can-be-run-from-command-line)
- [What should the Main-Class point to?](#what-should-the-main-class-point-to)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# Future tasks - Add a prettier UI

[More info](https://github.com/nazmulidris/notes/issues/60)

# git-log

The classes in the `cli` package are a collection of command line utils to make it easy to deal w/ certain git workflows
related to merging and cherry picking between multiple branches in the same repo. Though this cli framework is
extensible and allows any new commands to be added quite simply by making an entry in `MainEntryPoint.kt` and going from
there.

A corresponding fish shell function has been added called `git-log-search` that will build this project if be (to get
the "fat" executable JAR to be able to run these commands).

# Using Kotlin to create a self executing JAR file that can be run from command line

The goal of this project is to create a simple command line program written in Kotlin that is meant to replace the use
of KTS files. For the most part, this is straightforward w/ using IDEA to create a Gradle & Java & Kotlin/JVM project
using this [tutorial](https://www.jetbrains.com/help/idea/getting-started-with-gradle.html). It walks you through
creating the project, adding sources, and tests, and then creating a self executing JAR file.

Sadly though when you run the self executing JAR file, it blows up w/ this exception:

```
java -jar build/libs/kotlin-gradle-executable-jar-1.0-SNAPSHOT.jar
Exception in thread "main" java.lang.NoClassDefFoundError: kotlin/jvm/internal/Intrinsics
	at processor.FizzBuzzProcessor.main(FizzBuzzProcessor.kt)
Caused by: java.lang.ClassNotFoundException: kotlin.jvm.internal.Intrinsics
	at java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:581)
	at java.base/jdk.internal.loader.ClassLoaders$AppClassLoader.loadClass(ClassLoaders.java:178)
	at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:522)
	... 1 more
Process finished with exit code 1
```

So, the problem is that the executable JAR files that is generated is "thin" but it needs to be "fat". The deps needed
for Kotlin and simply not packaged by gradle into this JAR file! Here are some great links:

1. [SO answer on using shadowJar](https://stackoverflow.com/a/32473606/2085356)
2. [ShadowJar](https://imperceptiblethoughts.com/shadow/getting-started/#default-java-groovy-tasks)

After adding ShadowJar, you can simply run the gradle task `shadowJar` in order to generate the "fat" JAR, which is
located here: `$PROJECT_ROOT/builds/libs/kotlin-gradle-executable-jar-1.0-SNAPSHOT-all.jar`. And you can just run it
from the command line using: `java -jar kotlin-gradle-executable-jar-1.0-SNAPSHOT-all.jar`

I've added following IDEA run tasks (and saved them to the project) that you can use to build and run this JAR.

1. `build fat jar` -> actually generates the fat JAR
2. `execute fat jar` -> runs the fat JAR directly as you would from the command line
3. `run all tests` -> runs all the tests
4. `clean` -> runs gradle clean task

# What should the Main-Class point to?

Kotlin files have the `Kt` string appended to them after compilation, for interop w/ Java code. This makes it a little
tricky to figure out what the entry point for the executable JAR file should be. There are 2 approaches to solving this.

1. Simply append `Kt` to the `Main-Class` manifest attribute in `build.gradle`.
2. Use the `@file:JvmName("FizzBuzzProcessor")` annotation to force the name of the Kotlin file to whatever you want it
   to be in Java. If you do this, then you have make sure that there's no top level main function defined (otherwise
   there will be a collision when you compile).
   1. Kotlin takes any main function defined and simply generates a class w/ that static function defined inside of it.
      And the collision is this generated class w/ the actual class that you've defined.
   2. Just move the file level main function into a companion object of the class itself (and mark it w/ `@JvmStatic`),
      and that solves the issue.
