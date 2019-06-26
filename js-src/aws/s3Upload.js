import {getUserPool} from "./auth";
import AWS from "aws-sdk";

export const uploadFiles = (files) => {
    return getSession().then(session => {
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

        const uploads = Array.from(files).map(file => {
            uploadFile(s3, file.name, file)
        });

        // If any uploads fail, this will only return the first error. We should handle ALL errors, not just the first.
        return Promise.all(uploads);
    });
};

const getSession = () => {
    const userPool = getUserPool();

    const currentUser = userPool.getCurrentUser();

    return new Promise((resolve, reject) => {
        currentUser.getSession((err, session) => {
            if (err) {
                reject(err);
            } else {
                resolve(session)
            }
        });
    });
};

const uploadFile = (s3, name, content) => {
    return new Promise((resolve, reject) => {
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

    });
};