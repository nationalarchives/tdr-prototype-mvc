import Axios from "axios"


const updateFileStatuses: () => void = () => {
    const urlParams: URLSearchParams = new URLSearchParams(window.location.search);
    const consignmentId = urlParams.get('consignmentId');
    const checkStatus: () => void = () => {
        Axios.get(`${consignmentId}`)
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