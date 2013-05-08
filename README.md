JBoss CapeDwarf
===============

JBoss CapeDwarf Blue is JBoss' attempt to implement GoogleAppEngine API on top of JBoss' technology / frameworks / libraries.
This way making the GAE to JBossAS switch seamless.

At JBoss.org
------------

http://www.jboss.org/capedwarf

How to build CapeDwarf environment?
-----------------------------------

(1) Build JBossAS 7.2.0.Final tag --> JBOSS_HOME

    https://github.com/jbossas/jboss-as/

(2) Build CapeDwarf Shared

    https://github.com/capedwarf/capedwarf-shared

(3) Build CapeDwarf Blue

    https://github.com/capedwarf/capedwarf-blue

(4) Build CapeDwarf AS

    https://github.com/capedwarf/capedwarf-jboss-as

    mvn clean install -Djboss.dir=${JBOSS_HOME} -Pupdate-as

This will install CapeDwarf Subsystem into previous AS 7.2.0.Final

(5) Start CapeDwarf

    cd ${JBOSS_HOME}/bin

    ./capedwarf.sh

or the long version

    ./standalone.sh -c standalone-capedwarf.xml

**Voila!**

How to test CapeDwarf environment?
-----------------------------------

There are multiple ways to test it:

(1) Run Blue's tests against **running** CapeDwarf AS instance

Goto CapeDwarf Blue and simply run

    mvn clean install -Premote

(2) Run tests automatically with CapeDwarf Testsuite

    https://github.com/capedwarf/capedwarf-testsuite

where you then simply do

    mvn clean install

Note: you need to manually build "AS 7.2.0.Final" with its .zip distribution.

This will grab AS .zip distribution, overlay it with CapeDwarf extension, and run tests via managed Arquillian AS container.

(3) Run different versions of GAE API jar against CapeDwarf

    https://github.com/capedwarf/capedwarf-versions

(4) Run some benchmarks against CapeDwarf

    https://github.com/capedwarf/capedwarf-benchmark
