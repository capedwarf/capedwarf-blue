JBoss CapeDwarf
===============

JBoss CapeDwarf Blue is JBoss' attempt to implement GoogleAppEngine API on top of JBoss' technology / frameworks / libraries.
This way making the GAE to JBossAS switch seamless.


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

mvn clean install -Djboss.dir=JBOSS_HOME -Pupdate-as

This will install CapeDwarf Subsystem into previous AS 7.2.0.Final

(5) Start CapeDwarf

cd JBOSS_HOME/bin

./capedwarf.sh

or the long version

./standalone.sh -c standalone-capedwarf.xml

**Voila!**
