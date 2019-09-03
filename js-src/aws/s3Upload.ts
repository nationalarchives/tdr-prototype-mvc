import {getCurrentUser, getSession} from "./auth";
import {CognitoIdentityCredentials, config} from "aws-sdk";
import * as S3 from "aws-sdk/clients/s3";
import * as uuidv4 from "uuid";

declare var TDR_USER_POOL_ID: string;
declare var TDR_IDENTITY_POOL_ID: string;
declare var S3_UPLOAD_BUCKET: string;

export interface UploadableFile {
    id: string,
    file: File
}

export const uploadFiles = (files: UploadableFile[]) => {
    const currentUser = getCurrentUser();
    
    if(!currentUser) {
        // Placeholder error handling. In Beta, we should reauthenticate the user.
        return Promise.reject("No current user");        
    }

    return getSession(currentUser).then(session => {
        const cognitoLoginId = "cognito-idp.eu-west-2.amazonaws.com/" + TDR_USER_POOL_ID;

        config.region = "eu-west-2";
        config.credentials = new CognitoIdentityCredentials({
            IdentityPoolId: TDR_IDENTITY_POOL_ID,
            Logins: {
                [cognitoLoginId]: session.getIdToken().getJwtToken()
            }
        });        
       
        const userName = currentUser.getUsername();
        const bucket = S3_UPLOAD_BUCKET;
        // TODO: Use the consignment ID once known
        const parentFolder = `${userName}-${uuidv4()}`;
        const s3 = new S3({
            params: {
                Bucket: bucket
            }
        });

        const uploads = files.map(fileDetails => uploadFile(s3, bucket, fileDetails.id, parentFolder, fileDetails.file));

        // If any uploads fail, this will only return the first error. We should handle ALL errors, not just the first.
        return Promise.all(uploads);
    });
};

function uploadFile(s3: S3, bucket: string, name: string, parentFolder: string, content: File): Promise<void> {
    return new Promise((resolve, reject) => {
        s3.upload(
            {
                Key: `${parentFolder}/${name}`,
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
