How do we run this cluster tests?
---------------------------------

Running this tests requires to **manually** start 2 JBossAS nodes:

<JBOSS_HOME>/bin/standalone.sh -c standalone-capedwarf.xml -Djboss.node.name=node-a -Djboss.server.data.dir=<JBOSS_HOME>/standalone/data1 -Djboss.server.temp.dir=<JBOSS_HOME>/standalone/tmp1

<JBOSS_HOME>/bin/standalone.sh -c standalone-capedwarf.xml -Djboss.node.name=node-b -Djboss.socket.binding.port-offset=100 -Djboss.server.data.dir=<JBOSS_HOME>/standalone/data2 -Djboss.server.temp.dir=<JBOSS_HOME>/standalone/tmp2

