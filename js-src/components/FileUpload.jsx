import React, {Component} from "react";

import {authenticateUser, getUserPool} from "../aws/auth";

import AWS from "aws-sdk";

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

        const userPool = getUserPool();

        authenticateUser(awsCode).then(() => {
            const currentUser = userPool.getCurrentUser();

            currentUser.getSession((err, session) => {
                if (err) {
                    console.log("Error getting user session");
                    console.log(err);
                } else {
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

                    const filename = "tmp-file-" + new Date().getTime();

                    s3.upload(
                        {
                            Key: `tmp-play-app/${filename}`,
                            Body: "placeholder content",
                            Bucket: "tdr-files"
                        },
                        {},
                        function(err) {
                            if (err) {
                                console.log("Error uploading files to S3");
                                console.log(err);
                            } else {
                                component.setState({
                                    uploadedFile: filename
                                });
                            }
                        }
                    );
                }
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