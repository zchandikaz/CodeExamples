# stop the running tomcat(docker container) if running
docker ps |grep war-dependency-example_tomcat| awk '{print $1}'|xargs -i$ docker kill $
docker images |grep war-dependency-example_tomcat| awk '{print $3}'|xargs -i$ docker rmi $ -f

mvn clean install -f custom-classloader/pom.xml
mvn clean install -f parent-war/pom.xml -P parent-jar
mvn package -f parent-war/pom.xml -P parent-war
mvn clean package -f child-war/pom.xml
docker-compose up &

echo "wait until tomcat is ready"
until $(curl --output /dev/null --silent --head --fail http://localhost:8081/child-war/); do
    printf '.'
    sleep 5
done
echo
echo "tomcat is ready"
echo "------------------------------------------------------"
echo
echo "1st request to the parent war"
curl -X POST http://127.0.0.1:8081/parent-war/
echo
echo
echo "1st request to the child war"
curl -X POST http://127.0.0.1:8081/child-war/
echo
echo
echo "2nd request to the parent war"
curl -X POST http://127.0.0.1:8081/parent-war/
echo
echo
echo "2nd request to the child war"
curl -X POST http://127.0.0.1:8081/child-war/
echo