docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD
docker build -t nationalarchives/prototype-play-app:$1 .
docker push nationalarchives/prototype-play-app:$1

pip install --user awscli
export PATH=$PATH:$HOME/.local/bin
aws ecs update-service --cluster tdr-prototype-ecs-$1 --service tdr-application-service-$1 --force-new-deployment
