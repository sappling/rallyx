RallyX
======

RallyX is a tool for working with [Rally (or now CA Agile Central)](https://www.ca.com/us/products/ca-agile-central.html).
RallyX can export information to [XMind](http://www.xmind.net) or MS Excel.
I have found that we typically use Rally for development projects organized
by the Rally Portfolio Items of Initiatives and Features.  So our
projects are in a hierarchy of Initiative -> Feature -> User Story -> User Story...

Many of the reports and ways to visualize the work in Rally did not appropriately
group by initiative, or even track features well.  This tool helps.

Personally, I like to use mind maps to organize. I have found it useful to
view the User Stories in this manner, which is what originally prompted
me to write this.

How To Build
------------
This project include a [gradle](http://gradle.org) build file and wrapper.
Use the provided gradlew file to create a project for your IDE or a
command line runner.  You will need to have a Java 8 SDK installed and your
JAVA_HOME environment variable set.

Run "gradlew idea" to generate a project for [IntelliJ Idea](https://www.jetbrains.com/idea/).
Run "gradlew eclipse" to generate a project for [Eclipse](https://eclipse.org/ide/).

Run "gradlew installDist" to build a runnable sample in the build/install/rallyx
directory.  From that directory you may run the tool using **bin/rallyx**.

Running
-------
RallyX requires some environment variables and various command line arguments.
It will search in Rally beneath your default Rally "project".

#### Environment Variables
|Env Variable  | Description    |
|--------------|----------------|
|JAVA_HOME     | Path to your Java JRE or JDK (min ver 1.8)|
|RALLY_KEY     | Set to the Rally API Key - **REQUIRED**|
|PROXYURL      | URL of proxy (if needed) like http://myproxy.my.com:8080 |
|PROXYUSER     | username of authenticated proxy |
|PROXYPASS     | password for authenticated proxy |

See [Rally Help](https://help.rallydev.com/rally-application-manager)
for a description of how to get an API Key.

#### Command Line Arguments

```
rallyx -i <id>  -r <name> [-f <filename>] [-noproxy] [-type <filetype>] [-help]
 -i,--initiative <id>      Initiative ID (like I203)
 -r,--release <name>       Release (like "some release") - REQUIRED
 -f,--file <filename>      output filename
 -noproxy                  disable proxy use even if env var set
 -type,--type <filetype>   type of output (xmind, excel, word)
 -help                     display help
```


Output Formats
--------------

#### xmind

 The xmind output format should be used with a file name ending in .xmind.
 It generates a mind map with the Initiative as the
 root topic.  User Stories that have been completed will have a check
 mark in front of them.  User Stories which are not part of the specified
 release will be marked through.  User Stories that are in the specified
 release, but not beneath the initiative will be on a separate "orphans"
 sheet.

#### excel
 The excel output format should be used with a file name ending in
 .xlsx.  It generates a spreadsheet with the union of all the stories
 from beneath the initiative and the specified release.

License
-------

This application is licensed under the
 [Eclipse Public License (EPL) v1.0](http://www.eclipse.org/legal/epl-v10.html).
