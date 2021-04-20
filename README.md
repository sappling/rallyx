RallyX
======

RallyX is a tool for working with [Rally (or now CA Agile Central)](https://www.ca.com/us/products/ca-agile-central.html).
RallyX can export information to [XMind](http://www.xmind.net), MS Word, MS Excel,
or [Miro](https://miro.com).
I have found that we typically use Rally for development projects organized
by the Rally Portfolio Items of Initiatives and Features.  So our
projects are in a hierarchy of Initiative -> Feature -> User Story -> User Story...

Many of the reports and ways to visualize the work in Rally did not appropriately
group by initiative, or even track features well.  This tool helps.

Personally, I like to use mind maps to organize. I have found it useful to
view the User Stories in this manner, which is what originally prompted
me to write this.

The "check" output type is also handy to check for common errors in stories.

Download
--------
A zip of the latest released version is [here](https://github.com/sappling/rallyx/releases).

How To Build
------------
This project include a [gradle](http://gradle.org) build file and wrapper.
Use the provided gradlew file to create a project for your IDE or a
command line runner.  You will need to have a Java 8 SDK installed and your
JAVA_HOME environment variable set.

Run "gradlew idea" to generate a project for [IntelliJ Idea](https://www.jetbrains.com/idea/).
Run "gradlew eclipse" to generate a project for [Eclipse](https://eclipse.org/ide/).

Run "gradlew dist" to build a runnable sample in the build/install/rallyx
directory and a zip in build/distributions.

Running
-------
Use the script bin/rallyx.bat or bin/rallyx to run the tool.
RallyX requires some environment variables and various command line arguments.
It will search in Rally beneath your default Rally "project".

#### Environment Variables
While some of the values may be specified either as environment variables
or in a properties file (see below), **JAVA_HOME** MUST be set as an
environment variable in order to run.

|Env Variable  | Description    |
|--------------|----------------|
|JAVA_HOME     | Path to your Java JRE or JDK (min ver 1.8) **REQUIRED**|
|RALLY_KEY     | Set the Rally API Key|
|PROXYURL      | URL of proxy (if needed) like http://myproxy.my.com:8080 |
|PROXYUSER     | username of authenticated proxy |
|PROXYPASS     | password for authenticated proxy |

See [Rally Help](https://rally1.rallydev.com/slm/doc/webservice/authentication.jsp)
for a description of how to get an API Key.

#### Command Line Arguments

```
usage: rallyx [-f <filename>] [-help] [-i <id>] [-incomplete] [-noproxy] [-p <propfile>] [-project <projectName>] [-r <name>] [-t <filetype>]
 -f,--file <filename>         output filename
 -help                        display help
 -i,--initiative <id>         Initiative ID (like I203)
 -incomplete                  Only use incomplete stories
 -noproxy                     disable proxy use even if env var set
 -p,--properties <propfile>   properties file with options
 -project <projectName>       only use User Stories in this project
 -r,--release <name>          Release (like "some release") - REQUIRED
 -t,--type <filetype>         type of output (xmind, excel, word, check, miro)
```

#### Property Files
You may also specify any of the environment variables and most of the
command line options in a property file.  In this case, you would just
use the -p propfile command line parameter to specify the properties
file to use.  This is helpful when you want to run the same report
frequently and just want to save all the options.  A sample properties
file is included.  All of the lines of this sample file start with
a '#' to comment out the line.  Remove this '#' from any option you wish
to set in the file.

Property files override any value set in an environment variable and
command line parameters override any values set in property files.

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

#### word
The word output format creates an MS Word docx file with the contents
of all the User Stories in hierarchical form.

#### check
The check output format creates an excel spreadsheet containing the
results of a user story error check.  These checks include:

|Severity  | Description  |
|----------|--------------|
| Error    | Story beneath the Initiative, but not in any release |
| Error    | Story with no estimate |
| Warning  | In current sprint for > 2 days with no tasks |
| Warning  | Story with no description |
| Warning  | Story not assigned to a team (not at a leaf project) |
| Warning  | Incomplete story in old Sprint ( ended > 2 weeks ago)|
| Warning  | Story not beneath the Initiative |

#### miro
This output format adds sticky notes and cards into the online whiteboard tool,
[Miro](https://miro.com).  Currently this is used to support remote PI Planning events
and works in a manner needed for my use.  Features or User Stories that have the tag "MMF"
are added as a blue sticky note.  Other user stories are added as a Card.

To use the miro option, you must use a properties file and set 2 properties:
* miro.token - The OAuth Token to authorize use of the board.  While I have created an app to do this, I have not yet
published it on the Miro Marketplace.  To use this, you will have to create your own app.  You can go to your 
Miro team's Profile Settings, under the "API, SDK & Embed" tab, and "create an app".  
You must specify at least the "boards:write" scope.  You must use an OAuth 2.0 flow to get a token.  Development
instructions are [here](https://developers.miro.com/reference#authorization-and-authentication).  I have
a Java Web App that will support this, but it is not yet published as Open Source (hopefully coming soon).

* miro.link - This option adds content inside a Miro frame.  Select theFrame, click the "..." and pick "Copy Link".   
It will be formatted like: ```https://miro.com/app/board/<miroBoard>=/?moveToWidget=<miroframe>&cot=13```

* miro.update.link - This is an optional property.  If specified, it should be a frame who's contents will be updated, 
but which will not have any new widgets added.  This may be used where one frame (miro.link) represents a backlog
of work brought into a miro board, and another frame (miro.update.link) is where content is moved from the backlog
and organized.

* miro.card.show - This is a comma separated list of options
  * size - show the estimated size in points for a user story 
  * iteration - show the scheduled iteration
  * team - show the team (project)
  * feature - show the parent feature ID on a user story
  * MMF - show the parent MMF on a user story
  * notinrelease - adds a note a marks the card in red if not in the specified release
  * unassigned - adds a dark red note to the card if not scheduled for an iteration
  * compactMMF - lays out the MMF cards in a column as long as they don't have any children

License
-------

This application is licensed under the
 [Eclipse Public License (EPL) v1.0](http://www.eclipse.org/legal/epl-v10.html).
