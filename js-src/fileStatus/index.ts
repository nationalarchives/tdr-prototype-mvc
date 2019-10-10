import Axios from "axios"

interface IResponse {
    data: IDataResponse
}

interface IDataResponse {
    percentage: number;
    totalFiles: number;
    virusErrors: string[]
    checksumErrors: string[]
}

const updateFileStatuses: () => void = () => {
    const urlParams: URLSearchParams = new URLSearchParams(window.location.search);
    const consignmentId = urlParams.get('consignmentId');
    const checkStatus: () => void = () => {
        Axios.get<any, IResponse>(`/fileStatusApi?consignmentId=${consignmentId}`)
            .then(data => {
                console.log(data.data)
                const statusProgress: HTMLProgressElement | null = document.querySelector(".status-progress")
                const statusProgressLabel: HTMLProgressElement | null = document.querySelector(".status-progress-label")
                const { percentage, totalFiles, virusErrors, checksumErrors } = data.data
                const error = virusErrors.length || checksumErrors.length
                if (statusProgress !== null && statusProgressLabel !== null) {
                    statusProgress.value = percentage
                    statusProgressLabel.innerText = `${percentage}%`
                }
                const progressContainer: HTMLDivElement | null = document.querySelector(".progress-container")
                const errorContainer: HTMLDivElement | null = document.querySelector(".error")
                if (percentage === 100 && !error) {
                    const progressCompleteContainer: HTMLDivElement | null = document.querySelector(".progress-complete-container")
                    const completeMessage: HTMLSpanElement | null = document.querySelector(".complete-message")
                    if (progressContainer && progressCompleteContainer) {
                        progressCompleteContainer.classList.remove('hide')
                        progressContainer.classList.add('hide')
                    }
                    if (errorContainer) {
                        errorContainer.classList.add("hide")
                    }
                    completeMessage!.innerText = `${totalFiles} files have been successfully uploaded`
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