FROM openjdk:8-slim
WORKDIR play
COPY target/universal/transfer-digital-records-1.0.1-SNAPSHOT.zip .
RUN apt-get update && apt-get install unzip && unzip -qq transfer-digital-records-1.0.1-SNAPSHOT.zip
CMD AWS_ACCESS_KEY_ID=$ACCESS_KEY_ID \
      AWS_SECRET_ACCESS_KEY=$SECRET_ACCESS_KEY \
      transfer-digital-records-1.0.1-SNAPSHOT/bin/transfer-digital-records \
      -Dplay.http.secret.key=$PLAY_SECRET_KEY \
      -DAUTHENTICATOR_SIGNER_KEY=$AUTHENTICATOR_SIGNER_KEY \
      -DAUTHENTICATOR_CRYPTER_KEY=$AUTHENTICATOR_CRYPTER_KEY \
      -DCSRF_SIGNER_KEY=$CSRF_SIGNER_KEY \
      -DSOCIAL_STATE_SIGNER_KEY=$SOCIAL_STATE_SIGNER_KEY \
      -DCOGNITO_CLIENT_ID=$COGNITO_CLIENT_ID \
      -DCOGNITO_CLIENT_SECRET=$COGNITO_CLIENT_SECRET \
      -DCOGNITO_UPLOAD_CLIENT_ID=$COGNITO_UPLOAD_CLIENT_ID \
      -DUSER_DB_ENDPOINT=$USER_DB_ENDPOINT \
      -DUSER_DB_USERS_TABLE=$USER_DB_USERS_TABLE \
      -DUSER_DB_TOKENS_TABLE=$USER_DB_TOKENS_TABLE \
      -DTDR_BASE_URL=$TDR_BASE_PATH \
      -DTDR_AUTH_URL=$TDR_AUTH_URL \
      -Dhttp.port=9000
