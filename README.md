JBoss CapeDwarf
===============

JBoss CapeDwarf Blue is JBoss' attempt to implement Google App Engine API on top of JBoss' technology / frameworks / libraries.
This way making the GAE to WildFly switch seamless.

At JBoss.org
------------

http://www.jboss.org/capedwarf

How to build CapeDwarf environment?
-----------------------------------

(1) Build WildFly 8.1.0.CR1 from WildFly repo --> JBOSS_HOME

    https://github.com/wildfly/wildfly

    git checkout 8.1.0.CR1

    mvn clean install -DskipTests -Prelease

Or you can grab it from WildFly downloads.

    http://wildfly.org/downloads/

(2) Build CapeDwarf Shared ("master" branch)

    https://github.com/capedwarf/capedwarf-shared

    mvn clean install

(3) Build CapeDwarf Blue ("master" branch)

    https://github.com/capedwarf/capedwarf-blue

    mvn clean install

(4) Build CapeDwarf WildFly ("master" branch)

    https://github.com/capedwarf/capedwarf-jboss-as

    mvn clean install -Djboss.dir=${JBOSS_HOME} -Pupdate-as

This will install CapeDwarf Subsystem into previous WildFly 8.1.0.CR1

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

(2) Run tests automatically with CapeDwarf Testsuite ("master" branch)

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

Any example apps I can deploy?
------------------------------

Simply start a CapeDwarf instance and drop any GAE .war into {JBOSS_HOME}/standalone/deployment or deploy the .war via WildFly's management console.

    If your app requires to be accessible under "/" context root, deploy it as ROOT.war.
    Otherwise it will be accessible under "/<app name>" context.

Existing examples / demos:

(1) GAE SDK comes with a bunch of examples, and we make sure they work on CapeDwarf as well. (at least the one's that are 100% portable)

    https://github.com/GoogleCloudPlatform/

(2) A SimpleChat app, exposing Channel API

    https://github.com/alesj/simplechat

(3) A ToDo list app

    https://github.com/capedwarf/todolist
