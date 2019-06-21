# Transfer Digital Records: MVC prototype

## Purpose

Prototype project to experiment with the basic features of the Transfer Digital Records project in a Play MVC app.

It is based on the [Play Hello World Scala seed project][hello-world].

[hello-world]: https://github.com/playframework/play-samples/tree/2.7.x/play-scala-hello-world-tutorial

## Run the project

You will need several configuration values from AWS:

- A [Cognito client app][cognito-app] ID and secret
- An access key and secret key for an IAM user who has permission to view users of the Cognito user pool. For this
  prototype, you can use your developer keys

[cognito-app]: https://docs.aws.amazon.com/cognito/latest/developerguide/user-pool-settings-client-apps.html

Run sbt, filling in the environment variable values:

```
COGNITO_CLIENT_ID=some_client_id \
COGNITO_CLIENT_SECRET=some_client_secret \
AWS_ACCESS_KEY_ID=some_access_key \
AWS_SECRET_ACCESS_KEY=some_secret_key
sbt run
```

Then visit <http://localhost:9000>
