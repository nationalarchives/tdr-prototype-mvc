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

export const uploadFiles = (files: UploadableFile[], incrementFileCount: () => void) => {
    const currentUser = getCurrentUser();
    
    if(!currentUser) {
        // Placeholder error handling. In Beta, we should reauthenticate the user.
        return Promise.reject("No current user");        
    }

    return getSession(currentUser).then(async session => {
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

        for (const fileDetails of files) {
            // Await so that files are uploaded one-by-one, rather than in parallel. Parallel uploads crash the browser
            // when the files are very large.
            await uploadFile(s3, bucket, fileDetails.id, parentFolder, fileDetails.file);

            incrementFileCount();
        }
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
            // Throttle upload of parts of large files. The AWS S3 SDK splits large files into smaller parts, and
            // uploads each part separately. The 'queueSize' option controls how many parts are uploaded in parallel.
            // The default is 4. This often causes Firefox to crash when uploading very large files. Reducing it to 1
            // seems to prevent the crashes.
            // TODO: Tune the 'queueSize' and 'partSize' options to see if we can improve the performance without
            // causing browser crashes.
            {
                queueSize: 1
            },
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
