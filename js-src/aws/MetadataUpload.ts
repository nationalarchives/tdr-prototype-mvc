import { CreateMultipleFiles } from '../graphql/mutations/fileMutation'
import { Files, CreateFileInput, MultipleFilesVariables } from '../graphql/types/Files'

import ApolloClient from 'apollo-boost'
import {getCurrentUser, getSession}  from "./auth";

interface TdrFile extends File {
    webkitRelativePath: string;
}

declare var APOLLO_CLIENT_URI: string;

//Creating new client to get mutation to work as useMutation hook does not appear to work
const apolloClient = new ApolloClient(
    {
        uri: APOLLO_CLIENT_URI,
        request: async operation => {
            const currentUser = getCurrentUser();
            if(!currentUser) {
                // Placeholder error handling. In Beta, we should reauthenticate the user.
                throw "Cannot call GraphQL because there is no logged-in Cognito user"
            }
            const session = await getSession(currentUser);
            const token = session.getIdToken().getJwtToken();
            operation.setContext({
                headers: {
                    authorization: token
                }
            });
        }
    }
);

function AddFiles(fileInputs: CreateFileInput[]) {
    return apolloClient.mutate<Files, MultipleFilesVariables>(
        {
            variables: {
                inputs: fileInputs
            },
            mutation: CreateMultipleFiles
        }        
    )
};

export const uploadFileMetadata = async (files:File[]) => {
    //Retrieve the necessary file info
    const filesInfo = files.map(
        async file => {
            return await getFileInfo(<TdrFile>file)
        }
    )
    const p = await Promise.all(filesInfo);
    return AddFiles(p)
};

const hexString = (buffer:ArrayBuffer) => {
    const byteArray = new Uint8Array(buffer);
    const hexCodes = [...byteArray].map(value => {
        const hexCode = value.toString(16);
        return hexCode.padStart(2, "0");
     });
    return hexCodes.join("");
};

export const generateHash = (file:File) => {
    const crypto = self.crypto.subtle;
    const fileReader = new FileReader();
    fileReader.readAsArrayBuffer(file);
    return new Promise(resolve => {
        fileReader.onload = async function () {
            const fileReaderResult = fileReader.result;
            if (fileReaderResult instanceof ArrayBuffer) {
                const buffer = await crypto.digest("SHA-256", fileReaderResult);
                resolve(hexString(buffer));
            }
        };
    });
};

const getFileInfo = async (file:TdrFile): Promise<CreateFileInput> => {
     const checksum = await generateHash(file);
     const fileInfo: CreateFileInput = {
        //Hardcoded consignment id
        consignmentId: 1,
        clientSideChecksum: checksum,
        fileSize: file.size,
        path:file.webkitRelativePath,
        //Need consistent format for storing date information
        lastModifiedDate: file.lastModified.toString(),
        fileName:file.name
    };

    return fileInfo
};