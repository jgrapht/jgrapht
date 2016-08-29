# JGraphT

Released: April 3, 2016</p>

Written by [Barak Naveh](mailto:barak_naveh@users.sourceforge.net)  and Contributors

(C) Copyright 2003-2016, by Barak Naveh and Contributors. All rights
reserved.

Please address all contributions, suggestions, and inquiries to the [user mailing list](https://lists.sourceforge.net/lists/listinfo/jgrapht-users)

## Introduction ##

JGraphT is a free Java class library that provides mathematical graph-theory objects and algorithms. It runs on Java 2 Platform (requires JDK 1.8 or later starting with JGraphT 1.0.0).

JGraphT may be used under the terms of either the

 * GNU Lesser General Public License (LGPL) 2.1
   http://www.gnu.org/licenses/lgpl-2.1.html

or the

 * Eclipse Public License (EPL)
   http://www.eclipse.org/org/documents/epl-v10.php

As a recipient of JGraphT, you may choose
which license to receive the code under.

For a detailed information on the dual license approach, see https://github.com/jgrapht/jgrapht/wiki/Relicensing.

A copy of the [EPL license](license-EPL.txt) and the [LPGL license](license-LGPL.txt) is included in the download.

Please note that JGraphT is distributed WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

Please refer to the license for details.

## Contents ##

- `README.md` this file
- `CONTRIBUTORS.md` list of contributors
- `HISTORY.md` changelog
- `license-EPL.txt` Eclipse Public License 1.0
- `license-LGPL.txt` GNU Lesser General Public License 2.1
- `javadoc/` Javadoc documentation
- `lib/` JGraphT libraries:
- `jgrapht-core-x.y.z.jar` core library
- `jgrapht-demo-x.y.z.jar` demo classes
- `jgrapht-ext-x.y.z.jar` extensions
- `jgrapht-ext-x.y.z-uber.jar` all libraries rolled into one
- `jgraph-a.b.c.jar` JGraph dependency library
- `jgraphx-a.b.c.jar` JGraphX dependency library
- `source/` complete source tree used to build this release

## Getting Started ##

The package `org.jgrapht.demo` includes small demo applications to help you get started. If you spawn your own demo app and think others can use it, please send it to us and we will add it to that package.

To run the graph visualization demo from the downloaded release, try executing this command in the lib directory:

    java -jar jgrapht-demo-x.y.z.jar

## Using via Maven

Starting from 0.9.0, every JGraphT release is published to the Maven Central Repository.  You can add a dependency from your project as follows:

```
  <groupId>org.jgrapht</groupId>
  <artifactId>jgrapht-core</artifactId>
  <version>0.9.2</version>
```

## Upgrading Versions ##

To help upgrading, JGraphT maintains a one-version-backwards compatibility. While this compatibility is not a hard promise, it is generally respected. (This policy was not followed for the jump from `0.6.0` to `0.7.0` due to the pervasive changes required for generics.) You can upgrade via:

- **The safe way** : compile your app with the JGraphT version that immediately follows your existing version and follow the deprecation notes, if they exist, and modify your application accordingly. Then move to the next version, and on, until you're current.
- **The fast way** : go to the latest JGraphT right away - if it works, you're done.
  
Reading the [change history](HISTORY.md) is always recommended.

## Documentation ##

A local copy of the Javadoc HTML files is included in this distribution. The latest version of these files is also available [on-line](http://www.jgrapht.org/javadoc).

## Dependencies ##

- JGraphT requires JDK 1.8 or later to build starting with version 1.0.0.
- [JUnit](http://www.junit.org) is a unit testing framework. You need JUnit only if you want to run the unit tests.  JUnit is licensed under the terms of the IBM Common Public License.  The JUnit tests included with JGraphT have been created using JUnit `3.8.1`.
- [XMLUnit](http://xmlunit.sourceforge.net) extends JUnit with XML capabilities. You need XMLUnit only if you want to run the unit tests.  XMLUnit is licensed under the terms of the BSD
    License.
- [JGraph](http://sourceforge.net/projects/jgraph) is a graph visualization and editing component. You need JGraph only if you want to create graph visualizations using the JGraphT-to-JGraph adapter. JGraph is licensed     under the terms of the GNU Lesser General Public License (LGPL). 
- [JGraphX](http://www.jgraph.com/jgraph.html) is the successor to JGraph. You need JGraphX only if you want to use the JGraphXAdapter to visualize the JGraphT graph interactively via JGraphX. JGraphX is licensed under the terms of the BSD license.
- [Touchgraph](http://sourceforge.net/projects/touchgraph) is a graph visualization and layout component. You need Touchgraph only if you want to create graph visualizations using the JGraphT-to-Touchgraph converter. Touchgraph is licensed under the terms of an Apache-style License.
- [ANTLR](http://www.antlr.org) is a parser generator.  It is used for reading text files containing graph representations, and is only required by the jgrapht-ext module.  ANTLR v4 is licensed under the terms of the [BSD license](http://www.antlr.org/license.html).

## Online Resources ##

The JGraphT website is at [http://www.jgrapht.org](http://www.jgrapht.org). You can use this site to:

- **Obtain the latest version**: latest version and all previous versions of JGraphT are available online.
- **Report bugs**: if you have any comments, suggestions or bugs you want to report.
- **Get support**: if you have questions or need help with JGraphT.

There is also a [wiki](https://github.com/jgrapht/jgrapht/wiki) set up for everyone in the JGraphT community to share information about the project.

Source code is hosted on [github](https://github.com/jgrapht/jgrapht). You can send contributions as pull requests there.

## Your Improvements ##

If you add improvements to JGraphT please send them to us as pull requests on github. We will add them to the next release so that everyone can enjoy them. You might also benefit from it: others may fix bugs in your source files or may continue to enhance them.

## Thanks ##

With regards from

[Barak Naveh](mailto:barak_naveh@users.sourceforge.net), JGraphT Project Creator

[John Sichi](mailto:perfecthash@users.sourceforge.net), JGraphT Project Administrator

[Joris Kinable](https://github.com/jkinable), JGraphtT Project Reviewer/Committer
