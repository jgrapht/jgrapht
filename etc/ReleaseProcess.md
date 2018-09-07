# JGraphT Release Process

1. Let other developers on [jgrapht-dev](https://groups.google.com/forum/#!forum/jgrapht-dev) know that you're starting on the release and ask them to hold off on merging changes until the release is complete.
1. Review the README.md, HISTORY.md, CONTRIBUTORS.md, and update:
    * Version
    * Dependencies
    * Release notes
    * Contributors
    * Copyright year
1. Review/update github issues to make sure they reflect the current state.  If there were important bug/feature changes, it is worth mentioning them in the README.md release notes.
1. Run `mvn javadoc:aggregate` to build the javadoc and make sure it is generated without errors/warnings. Fix where necessary. Make sure Eclipse build is warning-free.
1. Run all the JUnit tests via `mvn test`. Fix where necessary.
1. Reformat all code [using Eclipse](codeFormatter.sh). 
1. Commit all work and push to github.
1. Run `mvn -Dmaven.artifact.threads=1 clean deploy` to push the latest snapshot to Sonatype.
1. Run `mvn source:jar; mvn javadoc:jar; mvn release:prepare; mvn release:perform` to create the Maven artifacts and push them to Maven Central
1. Publish the release [using the Sonatype UI](http://central.sonatype.org/pages/releasing-the-deployment.html).
1. Before continuing, restart from a fresh clone to make sure your workspace is clean.  Otherwise, if you have old files lying around that are hidden by `.gitignore`, they may get accidentally included in the release archive.
1. Run `mvn javadoc:aggregate; mvn install` from the new release branch to produce the release archive distribution
1. Upload the release archive distribution to sourceforge using the File Release System.
1. Add the javadocs for the new release to the [javadoc repository](https://github.com/jgrapht/jgrapht-javadoc).  To do this, push a commit which replaces the contents of the existing javadoc directory, and also [adds an identical copy](https://github.com/jgrapht/jgrapht/wiki/Website-Deployment#javadoc) under a new javadoc-x.y.z directory.
1. Update [the website](../docs) with links to the new downloads, version numbers, etc.  Be sure to push this commit **after** the javadoc update from the previous step; this will make sure that the new javadoc gets released to the website at the same time.
1. Announce the new version in the mailing lists: jgrapht-users@lists.sourceforge.net, jgrapht-announce@lists.sourceforge.net
1. Update and commit the version number in HISTORY.md to reflect the beginning of development for the next version.  Finally, remove all existing deprecated methods.

## Notes
* The release artifacts are signed with private keys. In order to sign this release, you'll need to make sure you've already [created and published your own key](http://blog.sonatype.com/2010/01/how-to-generate-pgp-signatures-with-maven).
* To rebuild the full release package after it has been pushed to github, you can run `git checkout jgrapht-x.y.z` (the tag you published for the release), and then run `mvn clean; mvn javadoc:aggregate; mvn package`
