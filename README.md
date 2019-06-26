# Transfer Digital Records: MVC prototype

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
  - A [Cognito client app][cognito-app] ID and secret
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

### Build the JavaScript components

Run `npm run build` to compile the components, or run `npm run watch` to make webpack automatically rebuild the
components when any of the JavaScript source files change.

### Command line

Run sbt, filling in the environment variable values:

```
COGNITO_CLIENT_ID=some_client_id \
COGNITO_CLIENT_SECRET=some_client_secret \
AWS_ACCESS_KEY_ID=some_access_key \
AWS_SECRET_ACCESS_KEY=some_secret_key \
AUTHENTICATOR_SIGNER_KEY=changeme \
AUTHENTICATOR_CRYPTER_KEY=changeme \
CSRF_SIGNER_KEY=changeme \
SOCIAL_STATE_SIGNER_KEY=changeme \
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
