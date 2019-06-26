import React, {Component} from "react";

import {authenticateUser} from "../aws/auth";

import {uploadFile} from "../aws/s3Upload";
import FileForm from "./FileForm.jsx";

class FileUpload extends Component {
    constructor(props) {
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
        const awsCode = codeRegex.exec(url)[1];

        authenticateUser(awsCode).then(() => {
            this.setState({ userAuthenticated: true })
        }).catch(error => {
            console.log("Error authenticating user");
            console.log(error);
        });
    }

    handleUpload(files) {
        console.log("Handling upload of files");
        console.log(files);

        // TODO: Upload all files
        const file = files[0];

        uploadFile(file.name, file).then(() => {
            this.setState({ uploadedFileCount: files.length })
        }).catch(error => {
            console.log("Error uploading file");
            console.log(error);
        });
    }

    render() {
        if (!this.state.userAuthenticated) {
            return "Authenticating user..."
        } else if (this.state.uploadedFileCount > 0) {
            return `Uploaded ${this.state.uploadedFileCount} files`;
        }

        return <FileForm onUpload={this.handleUpload} />
    }
}

export default FileUpload;