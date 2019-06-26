import React, {Component} from "react";

import {authenticateUser} from "../aws/auth";

import {uploadFile} from "../aws/s3Upload";

class FileUpload extends Component {
    constructor(props) {
        super(props);
        this.state = {};
    }

    componentDidMount() {
        const url = window.location.href;
        const codeRegex = /.*?code\=([\w-]+)/;
        const awsCode = codeRegex.exec(url)[1];

        const component = this;

        authenticateUser(awsCode).then(() => {

            const fileName = "tmp-file-" + new Date().getTime();
            const fileBody = "placeholder content";

            uploadFile(fileName, fileBody).then(() => {
                component.setState({
                    uploadedFile: fileName
                });
            }).catch(error => {
                console.log("Error uploading file");
                console.log(error)
            });
        }).catch(error => {
            console.log("Error authenticating user");
            console.log(error);
        });
    }

    render() {
        return `In the file upload component. Uploaded file: '${this.state.uploadedFile}'`;
    }
}

export default FileUpload;