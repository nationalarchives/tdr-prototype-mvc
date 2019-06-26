import React, { Component } from "react";
import {
    CognitoUserPool,
    CognitoUserSession,
    CognitoIdToken,
    CognitoRefreshToken,
    CognitoAccessToken,
    CognitoUser
} from "amazon-cognito-identity-js";

import AWS from "aws-sdk";
import CognitoIdentityServiceProvider from "aws-sdk/clients/cognitoidentityserviceprovider";

class FileUpload extends Component {
    constructor(props) {
        super(props);
        this.state = {};
    }

    componentDidMount() {
        const url = window.location.href;
        const codeRegex = /.*?code\=([\w-]+)/;
        const awsCode = codeRegex.exec(url)[1];

        const poolData = {
            UserPoolId: "eu-west-2_6Mn0M2i9C",
            ClientId: "2u2clbhcqnjaj3fn0jaid078ao"
        };
        const userPool = new CognitoUserPool(poolData);

        const tokenEndpoint = "https://tdr.auth.eu-west-2.amazoncognito.com/oauth2/token";

        fetch(tokenEndpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: `grant_type=authorization_code&scope=profile&client_id=2u2clbhcqnjaj3fn0jaid078ao&redirect_uri=http://localhost:9000/upload&code=${awsCode}`
        })
            .then(res => {
                return res.json();
            })
            .then(json => {
                const idToken = new CognitoIdToken({ IdToken: json.id_token });
                const decoded = idToken.decodePayload();
                // If we decide to use this auth mechanism in production, we should verify that this username matches
                // the user that logged into the app.
                const userName = decoded["cognito:username"];

                const session = new CognitoUserSession({
                    IdToken: new CognitoIdToken({ IdToken: json.id_token }),
                    RefreshToken: new CognitoRefreshToken({ RefreshToken: json.refresh_token }),
                    AccessToken: new CognitoAccessToken({ AccessToken: json.access_token })
                });

                const user = new CognitoUser({
                    Username: userName,
                    Pool: userPool
                });
                user.setSignInUserSession(session);

                const currentUser = userPool.getCurrentUser();

                currentUser.getSession((err, session) => {
                    if (err) {
                        console.log("Error getting user session");
                        console.log(err);
                    } else {
                        console.log("Got session");

                        const cognitoLoginId = "cognito-idp.eu-west-2.amazonaws.com/eu-west-2_6Mn0M2i9C";

                        console.log("Configuring AWS creds");
                        console.log("JWT token:");
                        console.log(session.getIdToken().getJwtToken());

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
                                Key: filename,
                                Body: "placeholder content",
                                Bucket: "tdr-files"
                            },
                            {},
                            function(err) {
                                console.log("Error uploading files to S3");
                                console.log(err);
                            }
                        );
                    }
                });

                this.setState({
                    awsCode: awsCode
                });
            })
            .catch(error => {
                console.log("Error fetching token");
                console.log(error);
            });
    }

    render() {
        return `In the file upload component. AWS code: '${this.state.awsCode}'`;
    }
}

export default FileUpload;