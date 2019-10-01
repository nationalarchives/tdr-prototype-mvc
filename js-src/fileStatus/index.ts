import Axios from "axios"


const updateFileStatuses: () => void = () => {
    const urlParams: URLSearchParams = new URLSearchParams(window.location.search);
    const consignmentId = urlParams.get('consignmentId');
    const checkStatus: () => void = () => {
        Axios.get(`/fileStatusApi?consignmentId=${consignmentId}`)
            .then(data => {
                console.log(data.data)
                const statusProgress: HTMLProgressElement | null = document.querySelector(".status-progress")
                const statusProgressLabel: HTMLProgressElement | null = document.querySelector(".status-progress-label")
                const { totalComplete, totalFiles, error } = data.data
                if (statusProgress !== null && statusProgressLabel !== null) {
                    statusProgress.value = totalComplete
                    statusProgressLabel.innerText = `File ${totalComplete} of ${totalFiles}`
                }
                const progressContainer: HTMLDivElement | null = document.querySelector(".progress-container")
                const errorContainer: HTMLDivElement | null = document.querySelector(".error")
                if (totalComplete === totalFiles && !error) {
                    const progressCompleteContainer: HTMLDivElement | null = document.querySelector(".progress-complete-container")
                    const completeMessage: HTMLSpanElement | null = document.querySelector(".complete-message")
                    if (progressContainer && progressCompleteContainer) {
                        progressCompleteContainer.classList.remove('hide')
                        progressContainer.classList.add('hide')
                    }
                    if (errorContainer) {
                        errorContainer.classList.add("hide")
                    }
                    completeMessage!.innerText = `${totalComplete} files have been successfully uploaded`
                    clearInterval(pollingInterval)
                }
                if (error && errorContainer && progressContainer) {
                    errorContainer.classList.remove("hide");
                    progressContainer.classList.add("hide");
                    clearInterval(pollingInterval)
                }
            })
            .catch(error => console.error(error));
    }
    const pollingInterval = setInterval(checkStatus, 3000);
}

export { updateFileStatuses }