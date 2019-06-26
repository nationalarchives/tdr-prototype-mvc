import React, { Component } from "react";
import {
    IAuthenticationDetailsData,
    AuthenticationDetails,
    CognitoUserPool,
    CognitoUserSession,
    CognitoIdToken,
    CognitoRefreshToken,
    CognitoAccessToken,
    ICognitoUserData,
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
            body: `grant_type=authorization_code&scope=openid+profile&client_id=2u2clbhcqnjaj3fn0jaid078ao&redirect_uri=http://localhost:9000/upload&code=${awsCode}`
        })
            .then(res => {
                return res.json();
            })
            .then(json => {
                const session = new CognitoUserSession({
                    IdToken: new CognitoIdToken({ IdToken: json.id_token }),
                    RefreshToken: new CognitoRefreshToken({ RefreshToken: json.refresh_token }),
                    AccessToken: new CognitoAccessToken({ AccessToken: json.access_token })
                });

                const user = new CognitoUser({
                    // TODO: Get programatically
                    Username: "play-test-user",
                    Pool: userPool
                });
                user.setSignInUserSession(session);

                console.log("User:");
                console.log(user);

                const currentUser = userPool.getCurrentUser();

                console.log("Current user:");
                console.log(currentUser);

                currentUser.getSession((err, session) => {
                    if (err) {
                        console.log("Error getting user session");
                        console.log(err);
                    } else {
                        console.log("Got session");
                        currentUser.getUserAttributes((err, userData) => {
                            if (err) {
                                console.log("Error getting user attributes");
                                console.log(err);
                            } else {
                                console.log("Got user attributes");
                                console.log(userData);
                            }
                        });
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