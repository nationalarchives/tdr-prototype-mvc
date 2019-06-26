import {getUserPool} from "./auth";
import AWS from "aws-sdk";

export const uploadFile = (name, content) => {
    const userPool = getUserPool();

    const currentUser = userPool.getCurrentUser();

    return new Promise((resolve, reject) => {
        currentUser.getSession((err, session) => {
            if (err) {
                reject(err);
            } else {
                const cognitoLoginId = "cognito-idp.eu-west-2.amazonaws.com/eu-west-2_6Mn0M2i9C";

                AWS.config.region = "eu-west-2";
                AWS.config.credentials = new AWS.CognitoIdentityCredentials({
                    IdentityPoolId: "eu-west-2:4b26364a-3070-4f98-8e86-1e33a1b54d85",
                    Logins: {
                        [cognitoLoginId]: session.getIdToken().getJwtToken()
                    }
                });

                const s3 = new AWS.S3({
                    params: {
                        Bucket: "tdr-files"
                    }
                });

                s3.upload(
                    {
                        Key: `tmp-play-app/${name}`,
                        Body: content,
                        Bucket: "tdr-files"
                    },
                    {},
                    function(err) {
                        if (err) {
                            reject(err);
                        } else {
                            resolve();
                        }
                    }
                );
            }
        });
    });
};