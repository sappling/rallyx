RallyX
======

Rallyx is a tool for working with [Rally (or now CA Agile Central)](https://www.ca.com/us/products/ca-agile-central.html).
Rally can export information to [XMind](http://www.xmind.net), MS Excel, or MS Word.

How To Build
------------
These samples include a gradle build file and wrapper.  Use the provided
gradlew file to create a project for your IDE or a command line runner
for the samples.  You will need to have a Java 8 SDK installed and your
JAVA_HOME environment variable set.

Run "gradlew idea" to generate a project for [IntelliJ Idea](https://www.jetbrains.com/idea/).
Run "gradlew eclipse" to generate a project for [Eclipse](https://eclipse.org/ide/).

Run "gradlew installDist" to build a runnable sample in the build/install/rallyx
directory.  From that directory you may run the tool using **bin/xmindapi**.
