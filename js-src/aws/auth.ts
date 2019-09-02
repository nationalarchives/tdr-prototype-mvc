import {
    CognitoAccessToken,
    CognitoIdToken,
    CognitoRefreshToken, CognitoUser,
    CognitoUserPool,
    CognitoUserSession
} from "amazon-cognito-identity-js";

declare var TDR_BASE_URL: string;
declare var UPLOAD_APP_CLIENT_ID: string;
declare var TDR_AUTH_URL: string;
declare var TDR_USER_POOL_ID: string;

const poolData = {
    UserPoolId: TDR_USER_POOL_ID,
    ClientId: UPLOAD_APP_CLIENT_ID
};

export function getUserPool(): CognitoUserPool {
    return new CognitoUserPool(poolData)
}

export function getCurrentUser() {    
    return getUserPool().getCurrentUser();
}

export function getSession(currentUser: CognitoUser): Promise<CognitoUserSession> {
    return new Promise<CognitoUserSession>((resolve, reject) => {
        currentUser.getSession((err: any, session: CognitoUserSession) => {
            if (err) {
                reject(err);
            } else {
                resolve(session)
            }
        });
    });
}

export function authenticateUser(authenticationCode: string): Promise<void> {
    const userPool = getUserPool();

    const tokenEndpoint = TDR_AUTH_URL + "/oauth2/token";

    return fetch(tokenEndpoint, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: `grant_type=authorization_code&scope=profile&client_id=${UPLOAD_APP_CLIENT_ID}&redirect_uri=${TDR_BASE_URL}/upload&code=${authenticationCode}`
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