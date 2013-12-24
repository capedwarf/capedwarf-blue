JBoss CapeDwarf
===============

JBoss CapeDwarf Blue is JBoss' attempt to implement Google App Engine API on top of JBoss' technology / frameworks / libraries.
This way making the GAE to WildFly switch seamless.

At JBoss.org
------------

http://www.jboss.org/capedwarf

How to build CapeDwarf environment?
-----------------------------------

(1) Build WildFly 8.0.0.CR1 tag --> JBOSS_HOME

    https://github.com/wildfly/wildfly

    mvn clean install -DskipTests -Prelease

(2) Build CapeDwarf Shared

    https://github.com/capedwarf/capedwarf-shared

(3) Build CapeDwarf Blue

    https://github.com/capedwarf/capedwarf-blue

(4) Build CapeDwarf WildFly

    https://github.com/capedwarf/capedwarf-jboss-as

    mvn clean install -Djboss.dir=${JBOSS_HOME} -Pupdate-as

This will install CapeDwarf Subsystem into previous WildFly 8.0.0.CR1

(5) Start CapeDwarf

    cd ${JBOSS_HOME}/bin

    ./capedwarf.sh

or the long version

    ./standalone.sh -c standalone-capedwarf.xml

and with Modules support

    ./standalone.sh -c standalone-capedwarf-modules.xml

**Voila!**

How to test CapeDwarf environment?
-----------------------------------

There are multiple ways to test it:

(1) Run Blue's tests against **running** CapeDwarf WildFly instance

Goto CapeDwarf Blue and simply run

    mvn clean install -Premote

(2) Run tests automatically with CapeDwarf Testsuite

    https://github.com/capedwarf/capedwarf-testsuite

where you then simply do

    mvn clean install -Dcapedwarf.xmpp.password=<PASSWORD> -Djboss.mail.host=<SMTP HOST>

This will grab WildFly .zip distribution, overlay it with CapeDwarf extension, and run tests via managed Arquillian WildFly container.

(3) Run different versions of GAE API jar against CapeDwarf

    https://github.com/capedwarf/capedwarf-versions

(4) Run some benchmarks against CapeDwarf

    https://github.com/capedwarf/capedwarf-benchmark

(5) Run GAE TCK against CapeDwarf

    https://github.com/GoogleCloudPlatform/appengine-tck

    mvn clean install -Pcapedwarf
