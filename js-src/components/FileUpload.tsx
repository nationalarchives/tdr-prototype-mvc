import * as React from "react";

import {authenticateUser} from "../aws/auth";

import {uploadFiles} from "../aws/s3Upload";
import {uploadFileMetadata} from "../aws/MetadataUpload"
import FileForm from "./FileForm";


export interface FileUploadProps {}

interface FileUploadState {
    userAuthenticated: boolean,
    uploadedFileCount: number,
    uploadError?: any
}

export class FileUpload extends React.Component<FileUploadProps, FileUploadState> {
    constructor(props: FileUploadProps) {
        super(props);

        this.state = {
            userAuthenticated: false,
            uploadedFileCount: 0
        };

        this.handleUpload = this.handleUpload.bind(this);
    }

    componentDidMount() {
        const url = window.location.href;
        const codeRegex = /.*?code\=([\w-]+)/;
        let matches = codeRegex.exec(url);

        // Remove authentication code from page URL
        window.history.replaceState(null, "File upload", window.location.pathname);

        if (matches && matches[1]) {
            const awsCode = matches[1];

            authenticateUser(awsCode).then(() => {
                this.setState({ userAuthenticated: true })
            }).catch(error => {
                console.log("Error authenticating user");
                console.log(error);
            });
        }
    }


    handleUpload(files: FileList) {
        uploadFileMetadata(files).then(() => {
            return uploadFiles(files)
        })
            .then(() => {
                this.setState({uploadedFileCount: files.length})
            }).catch((error: any) => {
            this.setState({uploadError: error});
            console.log("Error uploading file");
            console.log(error);
        });

    }

    render() {
        if (!this.state.userAuthenticated) {
            return "Authenticating user...";
        } else if (this.state.uploadError) {
            return "Error uploading files";
        } else if (this.state.uploadedFileCount > 0) {
            return `Uploaded ${this.state.uploadedFileCount} files`;
        }

        return <FileForm onUpload={this.handleUpload} />
    }
}
