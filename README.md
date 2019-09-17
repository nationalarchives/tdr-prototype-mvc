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

### Prerequisites

You will need several configuration values:

- AWS configuration:
  - A [Cognito client app][cognito-app] ID and secret for login. The app should have the `aws.cognito.signin.user.admin`
    scope.
  - A separate Cognito client app ID for the upload client. This app is used client-side, so it should not have a client
    secret. It should have the `openid` and `profile` scopes but not `aws.cognito.signin.user.admin`.
  - An access key and secret key for an IAM user who has permission to view users of the Cognito user pool. For this
  prototype, you can use your developer keys
- Several keys to use for cryptographic signing. In development, these can be any string value. In deployed
  environments, they should be a unique random key.

[cognito-app]: https://docs.aws.amazon.com/cognito/latest/developerguide/user-pool-settings-client-apps.html

You will also need to setup a user in the userpool:
1. Go to the user pool where you have created the Cognito client app
2. Go to "General Settings" > "Users and groups" > "Create users"
3. Fill in the information on the dialog.
  * If you are not using a genuine email address for the user, you will need manually verify the user in AWS. See instructions below. 
4. Note down the user details, to enable login when you run the application.

#### Manually Verify User

1. If you used a dummy email address, you must confirm the user manually through the Cognito console.
2. From the AWS console, click Services then select Cognito under Security, Identity & Compliance.
3. Choose Manage your User Pools
4. Select the user pool you created the user under and click Users and groups in the left navigation bar.
5. You should see a user corresponding to the email address that you submitted through the registration page. Choose that username to view the user detail page.
6. Choose Confirm user to finalize the account creation process.

### Set up the database

Install the [AWS command line interface][aws-cli] and [configure your credentials][cli-config].

#### Download and Run Local DynamoDb

1. Download the [local version of Dynamo DB][localdb].
2. Create a folder on local machine called dynamo in a convenient location. Recommend the /usr/lib folder
3. Extract the contents of download to the dynamo folder
4. Ensure that you have permission to access the dynamo file in the folder. Run following command: 
```
$ sudo chown -R [user name]:[user name] dynamo/
```
5. Run the local Dynamo DB:

```
java -Djava.library.path=./DynamoDBLocal_lib -jar DynamoDBLocal.jar -sharedDb
```

Go into the scripts directory and create the user tables by running the create_user_db.sh script.

[localdb]: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.DownloadingAndRunning.html
[aws-cli]: https://aws.amazon.com/cli/
[cli-config]: https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-files.html


### Generate GraphQL Objects from GraphQL schema

Update conf/schema.graphl 

Create graphql queries or mutations in /home/ihoyle/git/tdr-prototype-mvc/app/graphql

Run sbt graphqlCodegen

Examples of use app/controllers/ViewCollectionsController.scala


### Build the JavaScript components

Run `npm install` the first time to install dependencies.
Run `npm run build` to compile the components, or run `npm run watch` to make webpack automatically rebuild the
components when any of the JavaScript source files change.

### Command line

Run sbt, filling in the environment variable values:

```
COGNITO_CLIENT_ID=some_client_id \
COGNITO_CLIENT_SECRET=some_client_secret \
COGNITO_UPLOAD_CLIENT_ID=client_id_for_upload_app \
AWS_ACCESS_KEY_ID=some_access_key \
AWS_SECRET_ACCESS_KEY=some_secret_key \
AUTHENTICATOR_SIGNER_KEY=changeme \
AUTHENTICATOR_CRYPTER_KEY=changeme \
CSRF_SIGNER_KEY=changeme \
SOCIAL_STATE_SIGNER_KEY=changeme \
TDR_AUTH_URL=https://tdr.auth.eu-west-2.amazoncognito.com \
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

Deployment requires a server such as an EC2 instance with:

- Java JRE 8
- A web server such as nginx, configured to proxy HTTP and/or HTTPS requests to whatever port you will be running TDR on 
- Ports 80 and/or 443 open

### Build a production distribution

On your dev machine, build the frontend, replacing the values of the environment variables:

```
TDR_BASE_URL=https://example.com \
  UPLOAD_APP_CLIENT_ID=some_cognito_app_id \
  npm run build
```

Build the Play app:

```
sbt clean dist
```

This should create a zip file in the target/universal directory.

### Deploy the application

scp the zip file from target/universal to the deployment machine.

ssh to the deployment machine and start the app, replacing the version number and environment variables:

```
unzip transfer-digital-records-<version>.zip
-DAWS_ACCESS_KEY_ID=some_access_key \
  -DAWS_SECRET_ACCESS_KEY=some_secret_key \
  transfer-digital-records-1.0-SNAPSHOT/bin/transfer-digital-records \
  -Dplay.http.secret.key=some_secret_key \
  -DAUTHENTICATOR_SIGNER_KEY=some_secret_key \
  -DAUTHENTICATOR_CRYPTER_KEY=some_secret_key \
  -DCSRF_SIGNER_KEY=some_secret_key \
  -DSOCIAL_STATE_SIGNER_KEY=some_secret_key \
  -DCOGNITO_CLIENT_ID=some_client_id \
  -DCOGNITO_CLIENT_SECRET=some_client_secret \
  -DCOGNITO_UPLOAD_CLIENT_ID=client_id_for_upload_app \
  -DUSER_DB_ENDPOINT=url_of_dynamo_db \
  -DUSER_DB_USERS_TABLE=dynamo_db_users_table \
  -DUSER_DB_TOKENS_TABLE=dynamo_db_tokens_table \
  -DTDR_BASE_URL=https://some-tdr-domain.com \
  -DTDR_AUTH_URL=https://tdr-{environment}.auth.eu-west-2.amazoncognito.com
  -DTDR_GRAPHQL_URI=URI for graphQl
  -Dhttp.port=8080
```
