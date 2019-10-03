# Transfer Digital Records: MVC prototype
[![Build Status](https://travis-ci.org/nationalarchives/tdr-prototype-mvc.svg?branch=master)](https://travis-ci.org/nationalarchives/tdr-prototype-mvc)

## Purpose

Prototype project to experiment with the basic features of the Transfer Digital Records project in a Play MVC app.

It is based on the [Play Hello World Scala seed project][hello-world].

[hello-world]: https://github.com/playframework/play-samples/tree/2.7.x/play-scala-hello-world-tutorial

## Design

This project uses the [GOV.UK Design System][govuk-design] to lay out the pages and style components.

[govuk-design]: https://design-system.service.gov.uk/

## Run the project

### Generate GraphQL Objects from GraphQL schema

Update conf/schema.graphl 

Create graphql queries or mutations in /app/graphql

Run sbt graphqlCodegen

Examples of use app/controllers/ViewCollectionsController.scala


### Build the JavaScript components

Run `npm install` the first time to install dependencies.
Run `npm run build` to compile the components, or run `npm run watch` to make webpack automatically rebuild the
components when any of the JavaScript source files change.

### Command line

Run sbt, filling in the environment variable values:

```
AWS_ACCESS_KEY_ID=some_access_key \
AWS_SECRET_ACCESS_KEY=some_secret_key \
sbt run
```

Then visit <http://localhost:9000>

### IntelliJ

Add a new sbt configuration:

- Set the task to `run`
- Uncheck "Use SBT shell" so that you can edit the environment variables
- Add an environment variable for each of the values in the command line section above
- Save the configuration, and hit Run or Debug

Then visit <http://localhost:9000>

Note: Intellij may fail to build the project successfully. If this occurs compile the project first from the command line. Then try from Intellij again.

# Deployment

## Automated deployment

Currently, Travis automatically deploys the master branch by running the scripts/deploy.sh script.

## Manual deployment

### Prerequisites

Deployment is via a docker image running on ECS. The docker image is self contained and defined in the Dockerfile.

### Build a production distribution

On your dev machine, build the frontend

```
npm run build
```

Build the Play app:

```
sbt clean dist
```

Build and push the docker image
```
docker build -t nationalarchives/prototype-play-app:dev .
docker push nationalarchives/prototype-play-app:dev
```

Deploy to ECS
```
aws ecs update-service --service tdr-application-service-dev --cluster tdr-prototype-ecs-dev --force-new-deployment
```

