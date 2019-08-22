//import { useMutation } from '@apollo/react-hooks';
import fileMutation from '../graphql/mutations/fileMutation'
import { Files, FilesVariables } from '../graphql/types/Files'

import ApolloClient from 'apollo-boost'

interface TdrFile extends File {
    webkitRelativePath: string;
}

declare var APOLLO_CLIENT_URI: string;

//Creating new client to get mutation to work as useMutation hook does not appear to work
const apolloClient = new ApolloClient(
    {
        uri: APOLLO_CLIENT_URI
    }
);

//Hardcoded consignment id
function AddFile(path: String) {
    try {        
        apolloClient.mutate<Files, FilesVariables>(
            {
                variables: {
                    path: `${path}`,
                    id: 1
                },
                mutation: fileMutation                
            }
        )
        
        //useMutation hook does not fire off the request to the graphql server
        /* const [, {data}] = useMutation<Files, FilesVariables>(
            fileMutation, {variables: {path: "a/test/filepath/file1.txt", id: 1}}
        ); */
        
        console.log("Complete")
            
    } catch(err){
       return (err.message)
    }
}

// Not uploading data just logging data to prove File api works
export const uploadFileMetadata = (files:File[]) => {

    const uploads = Array.from(files).map(async file => {
        await getFileMetadata( file.name, file)
    });

    return Promise.all(uploads);
};

const getFileMetadata =  async ( name:String, content:File) => {
    const fileInfo = await getFileInfo(<TdrFile>content);
    console.log(fileInfo);
    return fileInfo
};

const hexString = (buffer:ArrayBuffer) => {
    const byteArray = new Uint8Array(buffer);

    const hexCodes = [...byteArray].map(value => {
        const hexCode = value.toString(16);
        return hexCode.padStart(2, "0");
     });

    return hexCodes.join("");
};


const generateHash = (file:File) => {
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

const getFileInfo = async (file:TdrFile) => {
    const checksum = await generateHash(file);
    const fileInfo = {
        checksum:checksum,
        size: file.size,
        path:file.webkitRelativePath,
        lastModifiedDate: file.lastModified,
        fileName:file.name
    };

    AddFile(file.webkitRelativePath)

    return new Promise(resolve =>{
        resolve(fileInfo)
    });
};


