import {
    CognitoAccessToken,
    CognitoIdToken,
    CognitoRefreshToken, CognitoUser,
    CognitoUserPool,
    CognitoUserSession
} from "amazon-cognito-identity-js";

export const authenticateUser = authenticationCode => {
    const poolData = {
        UserPoolId: "eu-west-2_6Mn0M2i9C",
        ClientId: "2u2clbhcqnjaj3fn0jaid078ao"
    };
    const userPool = new CognitoUserPool(poolData);

    const tokenEndpoint = "https://tdr.auth.eu-west-2.amazoncognito.com/oauth2/token";

    return fetch(tokenEndpoint, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: `grant_type=authorization_code&scope=profile&client_id=2u2clbhcqnjaj3fn0jaid078ao&redirect_uri=http://localhost:9000/upload&code=${authenticationCode}`
    }).then(res => {
        return res.json();
    }).then(json => {
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
    });
}