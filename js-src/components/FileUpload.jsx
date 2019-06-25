import React, { Component } from "react";
import {
    IAuthenticationDetailsData,
    AuthenticationDetails,
    CognitoUserPool,
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

        const tokenEndpoint = "https://tdr.auth.eu-west-2.amazoncognito.com/oauth2/token";

        fetch(tokenEndpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: `grant_type=authorization_code&client_id=2u2clbhcqnjaj3fn0jaid078ao&redirect_uri=http://localhost:9000/upload&code=${awsCode}`
        })
            .then(res => {
                console.log("Got response");
                console.log(res);
            })
            .catch(error => {
                console.log("Error fetching token");
                console.log(error);
            });

        const poolData = {
            UserPoolId: "eu-west-2_6Mn0M2i9C",
            ClientId: "2u2clbhcqnjaj3fn0jaid078ao"
        };
        const userPool = new CognitoUserPool(poolData);

        const user = userPool.getCurrentUser();

        console.log("Current user:");
        console.log(user);

        this.setState({
           awsCode: awsCode
        });
    }

    render() {
        return `In the file upload component. AWS code: '${this.state.awsCode}'`;
    }
}

export default FileUpload;