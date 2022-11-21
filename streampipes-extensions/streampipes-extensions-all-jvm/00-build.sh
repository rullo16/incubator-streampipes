docker login 
docker build -t fouo/gft-custom-extensions .
docker push fouo/gft-custom-extensions
docker rmi eclipse-temurin:11-jre-focal
