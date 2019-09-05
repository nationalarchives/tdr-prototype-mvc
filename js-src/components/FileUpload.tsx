import * as React from "react";

import {authenticateUser} from "../aws/auth";

import {uploadFiles} from "../aws/s3Upload";
import {uploadFileMetadata} from "../aws/MetadataUpload"
import FileForm from "./FileForm";

export interface FileUploadProps {}

interface FileUploadState {
    userAuthenticated: boolean,
    metadataFileCount: number,
    uploadedFileCount: number,
    totalFiles: number,
    uploadProgress: UploadProgress,
    uploadError?: any
}

enum UploadProgress {
    NotStarted,
    InProgress,
    Finished
}

export class FileUpload extends React.Component<FileUploadProps, FileUploadState> {
    constructor(props: FileUploadProps) {
        super(props);

        this.state = {
            userAuthenticated: false,
            uploadProgress: UploadProgress.NotStarted,
            totalFiles: 0,
            metadataFileCount: 0,
            uploadedFileCount: 0
        };

        this.handleUpload = this.handleUpload.bind(this);
        this.incrementMetadataCount = this.incrementMetadataCount.bind(this);
        this.incrementUploadCount = this.incrementUploadCount.bind(this);
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

    incrementMetadataCount(): void {
        this.setState(prevState => {
           return {
               metadataFileCount: prevState.metadataFileCount + 1
           }
        });
    }

    incrementUploadCount(): void {
        this.setState(prevState => {
            return {
                uploadedFileCount: prevState.uploadedFileCount + 1
            }
        });
    }

    handleUpload(files: File[]) {
        this.setState({
            uploadProgress: UploadProgress.InProgress,
            totalFiles: files.length
        });

        uploadFileMetadata(files, () => this.incrementMetadataCount()).then((filesWithIds) => {
            return uploadFiles(filesWithIds, () => this.incrementUploadCount());
        }).then(() => {
            this.setState({
                uploadProgress: UploadProgress.Finished,
                uploadedFileCount: files.length
            });
        }).catch((error: any) => {
            this.setState({uploadError: error});
            console.log("Error uploading file");
            console.log(error);
        });
    }

    render() {
        if (!this.state.userAuthenticated) {
            return "Authenticating user...";
        } else if (this.state.uploadProgress === UploadProgress.NotStarted) {
            return <FileForm onUpload={this.handleUpload} />
        } else if (this.state.uploadError) {
            return "Error uploading files";
        }

        const progressMessage = (this.state.uploadProgress === UploadProgress.Finished) ? "Upload finished" : "Upload in progress";

        return (
          <div>
              <p>{progressMessage}</p>
              <p>Metadata extraction: {this.state.metadataFileCount} of {this.state.totalFiles}</p>
              <p>Uploaded files: {this.state.uploadedFileCount} of {this.state.totalFiles}</p>
          </div>
        );
    }
}
