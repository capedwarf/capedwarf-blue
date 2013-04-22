How do we run this cluster tests?
---------------------------------

Running this tests requires to **manually** start 2 JBossAS nodes:

${JBOSS_HOME}/bin/standalone.sh -c standalone-capedwarf.xml -Djboss.node.name=nodeA -Djboss.server.data.dir=${JBOSS_HOME}/standalone/dataA -Djboss.server.temp.dir=${JBOSS_HOME}/standalone/tmpA

${JBOSS_HOME}/bin/standalone.sh -c standalone-capedwarf.xml -Djboss.node.name=nodeB -Djboss.server.data.dir=${JBOSS_HOME}/standalone/dataB -Djboss.server.temp.dir=${JBOSS_HOME}/standalone/tmpB -Djboss.socket.binding.port-offset=100
