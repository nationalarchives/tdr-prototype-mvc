docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD
docker build -t nationalarchives/prototype-play-app:$1 .
docker push nationalarchives/prototype-play-app

pip install --user awscli
export PATH=$PATH:$HOME/.local/bin
aws --version
aws ecs update-service --cluster tdr-prototype-ecs-dev --service tdr-application-service-dev --force-new-deployment
