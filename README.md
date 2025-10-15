[![JGrapht Master build](https://github.com/jgrapht/jgrapht/actions/workflows/master-workflow.yaml/badge.svg)](https://github.com/jgrapht/jgrapht/actions/workflows/master-workflow.yaml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jgrapht/jgrapht/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22jgrapht%22)
[![Snapshot](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Forg%2Fjgrapht%2Fjgrapht-core%2Fmaven-metadata.xml&style=flat-square&label=snapshots&color=%2315252D)](https://central.sonatype.com/repository/maven-snapshots/org/jgrapht/jgrapht/maven-metadata.xml)
[![License](https://img.shields.io/badge/license-LGPL%202.1-blue.svg)](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html)
[![License](https://img.shields.io/badge/license-EPL%202.0-blue.svg)](https://www.eclipse.org/legal/epl-2.0)
[![Language](http://img.shields.io/badge/language-java-brightgreen.svg)](https://www.java.com/)

# JGraphT

<img src="https://raw.githubusercontent.com/jgrapht/jgrapht/master/etc/logo/jgrapht-logo-transparent-cropped.png" width="361" height="200" align="right" />

Released: May 2, 2023</p>

Written by [Barak Naveh](mailto:barak_naveh@users.sourceforge.net) and Contributors

(C) Copyright 2003-2023, by Barak Naveh and Contributors. All rights
reserved.

Please address all contributions, suggestions, and inquiries to the [user mailing list](https://lists.sourceforge.net/lists/listinfo/jgrapht-users)

## Introduction

JGraphT is a free Java class library that provides mathematical graph-theory objects and algorithms. It runs on Java 2 Platform (requires JDK 11 or later starting with JGraphT 1.5.0).

JGraphT may be used under the terms of either the

 * GNU Lesser General Public License (LGPL) 2.1
   <https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html>

or the

 * Eclipse Public License (EPL)
   <https://www.eclipse.org/legal/epl-2.0/>

As a recipient of JGraphT, you may choose which license to receive the code under.

For detailed information on the dual license approach, see <https://github.com/jgrapht/jgrapht/wiki/Users:-Relicensing>.

A copy of the [EPL license](license-EPL.txt) and the [LPGL license](license-LGPL.txt) is included in the download.

Please note that JGraphT is distributed WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

Please refer to the license for details.

    SPDX-License-Identifier: LGPL-2.1-or-later OR EPL-2.0

## Release Contents

The files below make up the table of contents for a release distribution archive (produced by `mvn package`):

- `README.md` this file
- `CONTRIBUTORS.md` list of contributors
- `HISTORY.md` changelog
- `license-EPL.txt` Eclipse Public License 2.0
- `license-LGPL.txt` GNU Lesser General Public License 2.1
- `javadoc/` Javadoc documentation
- `lib/` JGraphT libraries and dependencies:
    - `jgrapht-core-x.y.z.jar` core library
    - `jgrapht-demo-x.y.z.jar` demo classes
    - `jgrapht-opt-x.y.z.jar` optimized graph implementations
    - `jgrapht-ext-x.y.z.jar` extensions
    - `jgrapht-io-x.y.z.jar` Importers/Exporters for various graph formats
    - `jgrapht-guava-x.y.z.jar` Adapter classes for the Guava library
    - `jgrapht-unimi-dsi-x.y.z.jar` Webgraph adapter and succinct graph implementations
    - `jgraphx-a.b.c.jar` JGraphX dependency library
    - `jheaps-x.y.jar` JHeaps library 
    - `antlr4-runtime-x.y.jar` ANTLR parser runtime
    - `commons-lang3-x.y.z.jar` Apache Commons Lang library
    - `commons-text-x.y.jar` Apache Commons Text library
    - `fastutil-x.y.z.jar` Fastutil library
    - `guava-x.y-jre.jar` Guava library
    - `jsap-x.y.jar` Jsap library
    - `logback-classic-x.y.z.jar` Logger
    - `logback-core-x.y.z.jar` Logger
    - `slf4j-api-x.y.z.jar` Logger api
    - `sux4j-x.y.z.jar` Sux4j library
    - `webgraph-x.y.z.jar` Webgraph library
    - `webgraph-big-z.y.z.jar` Webgraph big library
    - `apfloat-x.x.x.jar` Apfloat library

- `source/` complete source tree used to build this release

## Getting Started ##

The JGraphT [wiki](https://github.com/jgrapht/jgrapht/wiki) provides various helpful pages for new users, including a [How to use JGraphT in your projects](https://github.com/jgrapht/jgrapht/wiki/Users:-How-to-use-JGraphT-as-a-dependency-in-your-projects) page.
The package `org.jgrapht.demo` includes small demo applications to help you get started. If you spawn your own demo app and think others can use it, please send it to us and we will add it to that package.

To run the graph visualization demo from the downloaded release, try executing this command in the lib directory:

    java -jar jgrapht-demo-x.y.z.jar
 
More information can be found on the [user pages](https://github.com/jgrapht/jgrapht/wiki#user-pages) of our wiki. Finally, all classes come with corresponding test classes. These test classes contain many examples.

To help us understand how you use JGraphT, and which features are important to you, [tell](https://github.com/jgrapht/jgrapht/wiki/Users:-Projects-Using-JGraphT) us how you are using JGraphT, and [cite](https://github.com/jgrapht/jgrapht/wiki/Users:-How-to-cite-JGraphT) the usage of JGraphT in your book, paper, website, or technical report.

## Using via Maven

Starting from 0.9.0, every JGraphT release is published to the Maven Central Repository.  You can add a dependency from your project as follows:

```xml
<groupId>org.jgrapht</groupId>
<artifactId>jgrapht-core</artifactId>
<version>1.5.2</version>
```

We have also started auto-publishing SNAPSHOT builds for every successful commit to master.  To use the bleeding edge:

```xml
<groupId>org.jgrapht</groupId>
<artifactId>jgrapht-core</artifactId>
<version>1.5.3-SNAPSHOT</version>
```

and make sure the snapshot repository is enabled:

```xml
<repositories>
  <repository>
    <name>Central Portal Snapshots</name>
    <id>central-portal-snapshots</id>
    <url>https://central.sonatype.com/repository/maven-snapshots</url>
    <releases>
      <enabled>false</enabled>
    </releases>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
  </repository>
</repositories>
```

## Upgrading Versions

To help upgrading, JGraphT maintains a one-version-backwards compatibility. While this compatibility is not a hard promise, it is generally respected. (This policy was not followed for the jump from `0.6.0` to `0.7.0` due to the pervasive changes required for generics.) You can upgrade via:

- **The safe way**: compile your app with the JGraphT version that immediately follows your existing version and follow the deprecation notes, if they exist, and modify your application accordingly. Then move to the next version, and on, until you're current.
- **The fast way**: go to the latest JGraphT right away - if it works, you're done.
  
Reading the [change history](HISTORY.md) is always recommended.

## Documentation

A local copy of the Javadoc HTML files is included in the distribution. The latest version of these files is also available [online](http://www.jgrapht.org/javadoc).

## Dependencies

- JGraphT requires JDK 11 or later to build starting with version 1.5.0.
- [JHeaps](https://www.jheaps.org/) is a library with priority queues. JHeaps is licensed under the terms of the Apache License, Version 2.0.
- [JUnit](https://www.junit.org) is a unit testing framework. You need JUnit only if you want to run the unit tests.  JUnit is licensed under the terms of the Eclipse Public License - v 2.0. The JUnit tests included with JGraphT have been created using JUnit 5.
- [XMLUnit](https://www.xmlunit.org/) extends JUnit with XML capabilities. You need XMLUnit only if you want to run the unit tests.  XMLUnit is licensed under the terms of the BSD License.
- [JGraphX](https://github.com/jgraph/jgraphx) is a graph visualizations and editing component (the successor to the older JGraph library). You need JGraphX only if you want to use the JGraphXAdapter to visualize the JGraphT graph interactively via JGraphX. JGraphX is licensed under the terms of the BSD license.
- [ANTLR](https://www.antlr.org) is a parser generator.  It is used for reading text files containing graph representations, and is only required by the jgrapht-io module.  ANTLR v4 is licensed under the terms of the [BSD license](https://www.antlr.org/license.html).
- [Guava](https://github.com/google/guava) is Google's core libraries for Java. You need Guava only if you are already using Guava's graph data-structures and wish to use our adapter classes in order to execute JGraphT's algorithms. Only required by the [jgrapht-guava](jgrapht-guava) module.
- [Apache Commons Proper](https://commons.apache.org/components.html) is an Apache project containing reusable Java components. The packages [commons-text](https://commons.apache.org/proper/commons-text/) and [commons-lang3.](https://commons.apache.org/proper/commons-lang/) which provide additional utilities for String manipulation are only required by the jgrapht-io module. The package [commons-math](https://commons.apache.org/proper/commons-text/) is only required by the jgrapht-unimi-dsi module.
- [fastutil](http://fastutil.di.unimi.it/) provides a collection of type-specific maps, sets, lists and queues with a small memory footprint and fast access and insertion. Fastutil is only required by the jgrapht-opt module.
- [webgraph](https://webgraph.di.unimi.it/) provides a framework for graph compression enabling management of very large graphs. Webgraph is only required by the jgrapht-unimi-dsi module.
- [sux4j](https://sux.di.unimi.it/) provides implementations of basic succinct data structures. Sux4j is only required by the jgrapht-unimi-dsi module.
- [jsap](https://www.martiansoftware.com/jsap/) provides a simple argument parser. Jsap is only required by the jgrapht-unimi-dsi module.
- [apfloat](https://www.apfloat.org/apfloat_java/) provides support for high performance arbitrary precision arithmetic. Apfloat is licensed under the terms of the MIT license.

## Online Resources

The JGraphT website is at <https://www.jgrapht.org>. You can use this site to:

- **Obtain the latest version**: latest version and all previous versions of JGraphT are available online.
- **Report bugs**: if you have any comments, suggestions or bugs you want to report.
- **Get support**: if you have questions or need help with JGraphT.

There is also a [wiki](https://github.com/jgrapht/jgrapht/wiki) set up for everyone in the JGraphT community to share information about the project. For support, refer to our [support page](https://github.com/jgrapht/jgrapht/wiki/Users:-Getting-Support)

Source code is hosted on [github](https://github.com/jgrapht/jgrapht). You can send contributions as pull requests there.

## Your Improvements

If you add improvements to JGraphT please send them to us as [pull requests on github](https://github.com/jgrapht/jgrapht/wiki/Dev-guide:-How-to-make-your-first-(code)-contribution). We will add them to the next release so that everyone can enjoy them. You might also benefit from it: others may fix bugs in your source files or may continue to enhance them.

## Thanks

With regards from

[Barak Naveh](mailto:barak_naveh@users.sourceforge.net), JGraphT Project Creator

[John Sichi](mailto:perfecthash@users.sourceforge.net), JGraphT Project Administrator

[Joris Kinable](https://github.com/jkinable), JGraphtT Project Reviewer/Committer and Release Manager

[Dimitrios Michail](https://github.com/d-michail), JGraphT Project Reviewer/Committer


Me gustan las lentejas mucho, pero es que no puedo vivir sin ellas.
