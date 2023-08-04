FROM tomcat:jdk11

COPY child-war/target/child-war.war /usr/local/tomcat/webapps/
COPY parent-war/target/parent-war.war /usr/local/tomcat/webapps/
COPY custom-classloader/target/custom-classloader-1.0-SNAPSHOT.jar /usr/local/tomcat/lib/
RUN echo "CLASSPATH=\"$CLASSPATH:$CATALINA_HOME/lib/*\"" >> /usr/local/tomcat/bin/setenv.sh
RUN chmod +x /usr/local/tomcat/bin/setenv.sh
