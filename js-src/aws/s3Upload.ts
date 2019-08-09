import {getUserPool} from "./auth";
import {CognitoIdentityCredentials, config} from "aws-sdk";
import {CognitoUserSession} from "amazon-cognito-identity-js";
import {FileList, SelectedFile} from "../models/File";
import * as S3 from "aws-sdk/clients/s3";
import * as uuidv4 from "uuid";

declare var TDR_USER_POOL_ID: string;
declare var TDR_IDENTITY_POOL_ID: string;
declare var S3_UPLOAD_BUCKET: string;

export const uploadFiles = (files: FileList) => {
    return getSession().then(session => {
        const cognitoLoginId = "cognito-idp.eu-west-2.amazonaws.com/" + TDR_USER_POOL_ID;

        config.region = "eu-west-2";
        config.credentials = new CognitoIdentityCredentials({
            IdentityPoolId: TDR_IDENTITY_POOL_ID,
            Logins: {
                [cognitoLoginId]: session.getIdToken().getJwtToken()
            }
        });

        const currentUser = getCurrentUser();

        var userName = "noUser";

        if(currentUser)
        {
            userName = currentUser.getUsername();
        }

        const bucket = S3_UPLOAD_BUCKET;
        const s3Uploadkey = `${userName}-` + uuidv4();
        const s3 = new S3({
            params: {
                Bucket: bucket
            }
        });

        const uploads = Array.from(files).map(file => uploadFile(s3, bucket, file.name, s3Uploadkey, file));

        // If any uploads fail, this will only return the first error. We should handle ALL errors, not just the first.
        return Promise.all(uploads);
    });
};

function getCurrentUser() {
    const userPool = getUserPool();

    const currentUser = userPool.getCurrentUser();

    return currentUser;
};

function getSession(): Promise<CognitoUserSession> {
    const userPool = getUserPool();

    const currentUser = userPool.getCurrentUser();

    if (!currentUser) {
        // Placeholder error handling. In Beta, we should reauthenticate the user.
        return Promise.reject("No current user");
    }

    return new Promise<CognitoUserSession>((resolve, reject) => {
        currentUser.getSession((err: any, session: CognitoUserSession) => {
            if (err) {
                reject(err);
            } else {
                resolve(session)
            }
        });
    });
}

function uploadFile(s3: S3, bucket: string, name: string, s3UploadKey: string, content: SelectedFile): Promise<void> {
    return new Promise((resolve, reject) => {
        s3.upload(
            {
                Key: `${s3UploadKey}/${name}`,
                Bucket: bucket,
                Body: content
            },
            {},
            function(err: any) {
                if (err) {
                    reject(err);
                } else {
                    resolve();
                }
            }
        );
    });
}
