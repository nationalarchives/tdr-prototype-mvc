docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD
docker build -t nationalarchives/prototype-play-app .
docker push nationalarchives/prototype-play-app

pip install awscli
aws --version