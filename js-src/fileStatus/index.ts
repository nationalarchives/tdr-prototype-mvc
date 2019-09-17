const updateFileStatuses: (fileStatusContainer: HTMLDivElement) => void = (fileStatusContainer) => {
    fileStatusContainer.addEventListener("click", () => {
        const total: HTMLParagraphElement | null = document.querySelector(".total")
        if (total) {
            total.innerHTML = 'Done'
        }

    })
}

export { updateFileStatuses }