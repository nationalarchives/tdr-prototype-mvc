import ApolloClient from 'apollo-boost';
import gql from 'graphql-tag';
import { getCurrentUser, getSession } from '../aws/auth'

declare var APOLLO_CLIENT_URI: string;

const client = new ApolloClient({
    uri: APOLLO_CLIENT_URI,
    request: async operation => {
        const currentUser = getCurrentUser();
        if (!currentUser) {
            // Placeholder error handling. In Beta, we should reauthenticate the user.
            throw "Cannot call GraphQL because there is no logged-in Cognito user"
        }
        const session = await getSession(currentUser);
        const token = session.getIdToken().getJwtToken();
        operation.setContext({
            headers: {
                authorization: token
            }
        });
    }
});

interface Data {
    getFileChecksStatus: FileStatus
}

interface FileStatus {
    totalComplete: number,
    totalFiles: number,
    error: boolean
}

const updateFileStatuses: () => void = () => {
    const urlParams: URLSearchParams = new URLSearchParams(window.location.search);
    const consignmentId = urlParams.get('consignmentId');
    const checkStatus: () => void = () => {
        client.query<Data>({
            fetchPolicy: "no-cache",
            query: gql`
    query {
    getFileChecksStatus(id: ${consignmentId}) {
        totalComplete
        totalFiles
        error
    }
}
  `,
        })
            .then(data => {
                console.log(data.data.getFileChecksStatus)
                const statusProgress: HTMLProgressElement | null = document.querySelector(".status-progress")
                const statusProgressLabel: HTMLProgressElement | null = document.querySelector(".status-progress-label")
                const { totalComplete, totalFiles } = data.data.getFileChecksStatus
                if (statusProgress !== null && statusProgressLabel !== null) {
                    statusProgress.value = totalComplete
                    statusProgressLabel.innerText = `File ${totalComplete} of ${totalFiles}`
                }
                if (totalComplete === totalFiles) {
                    const progressContainer: HTMLDivElement | null = document.querySelector(".progress-container")
                    const progressCompleteContainer: HTMLDivElement | null = document.querySelector(".progress-complete-container")
                    if (progressContainer && progressCompleteContainer) {
                        progressCompleteContainer.classList.remove('hide')
                        progressContainer.classList.add('hide')
                    }
                    clearInterval(pollingInterval)
                }
            })
            .catch(error => console.error(error));
    }
    const pollingInterval = setInterval(checkStatus, 3000);
}

export { updateFileStatuses }