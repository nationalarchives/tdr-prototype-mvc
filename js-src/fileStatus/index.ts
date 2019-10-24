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
                const { percentage, virusErrors, checksumErrors } = data.data
                const error = virusErrors.length || checksumErrors.length
                if (statusProgress !== null && statusProgressLabel !== null) {
                    statusProgress.value = percentage
                    statusProgressLabel.innerText = `${percentage}%`
                }
                const progressContainer: HTMLDivElement | null = document.querySelector(".progress-container")
                const errorContainer: HTMLDivElement | null = document.querySelector(".error")
                if (percentage === 100 && !error) {
                    const progressCompleteContainer: HTMLDivElement | null = document.querySelector(".progress-complete-container")
                    if (progressContainer && progressCompleteContainer) {
                        progressCompleteContainer.classList.remove('hide')
                        progressContainer.classList.add('hide')
                    }
                    if (errorContainer) {
                        errorContainer.classList.add("hide")
                    }
                    clearInterval(pollingInterval)
                }
                if (error && errorContainer && progressContainer) {
                    const virusErrorMessage: HTMLDivElement | null = document.querySelector("#virus-errors");
                    const checksumErrorMessage: HTMLDivElement | null = document.querySelector("#checksum-errors");
                    const errorDiv: HTMLDivElement | null = document.querySelector(".govuk-error-summary__body");
                    if (virusErrors.length && virusErrorMessage && !virusErrorMessage.children.length) {
                        virusErrorMessage.classList.remove("hide");
                        checksumErrorMessage!.classList.add("hide")
                        for(const error of virusErrors) {
                            const pElement = document.createElement("p")
                            const textNode = document.createTextNode(error);
                            pElement.appendChild(textNode)
                            errorDiv!.append(pElement)
                            
                        }
                    }
                    if(checksumErrors.length && checksumErrorMessage && !checksumErrorMessage.children.length) {
                        checksumErrorMessage.classList.remove("hide");
                        virusErrorMessage!.classList.add("hide")
                        for(const error of checksumErrors) {
                            const pElement = document.createElement("p")
                            const textNode = document.createTextNode(error);
                            pElement.appendChild(textNode)
                            checksumErrorMessage!.appendChild(pElement)
                            errorDiv!.append(pElement)
                        }
                    }
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