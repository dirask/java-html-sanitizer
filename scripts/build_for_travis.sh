#!/bin/bash

set -e

# Invoked from .travis.yml to verify the build.

export COMMON_FLAGS="-Dgpg.skip=true -B -V"

if echo $TRAVIS_JDK_VERSION | egrep -q 'jdk[67]'; then
    # The main library only uses jdk6 incompatible dependencies,
    # and older versions of javadoc barf on -Xdoclint flags used
    # to configure the maven-javadoc-plugin.
    exec mvn verify -Dguava.version=20.0 -Dmaven.javadoc.skip=true $COMMON_FLAGS
else
    # Build the whole kit-n-kaboodle.
    mvn                             -f aggregate/pom.xml       source:jar javadoc:jar verify $COMMON_FLAGS \
    && mvn -Dguava.version=27.1-jre -f aggregate/pom.xml clean source:jar javadoc:jar verify $COMMON_FLAGS \
    && mvn jacoco:report coveralls:report \
    && mvn org.sonatype.ossindex.maven:ossindex-maven-plugin:audit -f aggregate
fi
