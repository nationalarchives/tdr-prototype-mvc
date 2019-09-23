import Axios from "axios";

interface CreateFileInput {
    consignmentId: number;
    path: string | null;
    fileSize: number;
    lastModifiedDate: Date;
    clientSideChecksum: unknown;
    fileName: string;
}

interface TdrFile extends File {
    webkitRelativePath: string;
}

interface FilePathMap {
    path: string;
    file: File
}

export interface IReader {
    readEntries: (callbackFunction: (entry: IWebkitEntry[]) => void) => void;
}

export interface IWebkitEntry extends DataTransferItem {
    createReader: () => IReader;
    isFile: boolean;
    isDirectory: boolean;
    fullPath: string;
    file: (success: (file: File) => void) => void;
}



const hexString = (buffer: ArrayBuffer) => {
    const byteArray = new Uint8Array(buffer);
    const hexCodes = [...byteArray].map(value => {
        const hexCode = value.toString(16);
        return hexCode.padStart(2, "0");
    });
    return hexCodes.join("");
};

export const generateHash: (file: File) => Promise<string> = (file) => {
    const crypto = self.crypto.subtle;
    const fileReader = new FileReader();
    fileReader.readAsArrayBuffer(file);
    return new Promise(resolve => {
        fileReader.onload = async function () {
            const fileReaderResult = fileReader.result;
            if (fileReaderResult instanceof ArrayBuffer) {
                const buffer = await crypto.digest("SHA-256", fileReaderResult);
                resolve(hexString(buffer));
            }
        };
    });
};


const upload: () => void = async () => {
    const uploadInput: HTMLInputElement | null = document.querySelector("#file-upload")
    const uploadForm: HTMLFormElement | null = document.querySelector("#upload-submit");
    if (uploadForm && uploadInput) {
        uploadForm.addEventListener("click", async () => {
            const files: FileList | null = uploadInput.files
            await processFiles(files);
        })
    }
    const dragAndDrop: HTMLDivElement | null = document.querySelector(".govuk-file-drop");
    if (dragAndDrop) {
        dragAndDrop.ondragover = onDragOver
        dragAndDrop.ondragleave = () => setIsDragging(false)
        dragAndDrop.ondrop = onDrop
    }
}

const setIsDragging: (isDragging: boolean) => void = (isDragging) => {
    let currentVal = ".govuk-file-drop"
    let fileDrop: HTMLDivElement | null = document.querySelector(".govuk-file-drop")
    if (!fileDrop) {
        currentVal = ".govuk-file-drop-drag"
        fileDrop = document.querySelector(currentVal)

    }
    const targetVal = isDragging ? ".govuk-file-drop-drag" : ".govuk-file-drop"
    const fileDropLabel: HTMLSpanElement | null = document.querySelector(".draganddrop")
    const labelText = isDragging ? "Drop now" : "Drag folder here"
    fileDropLabel!.innerText = labelText;
    fileDrop!.classList.replace(currentVal, targetVal)

}

const onDragOver: (e: DragEvent) => void = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(true)
}

const getFileFromEntry: (entry: IWebkitEntry) => Promise<File> = entry => {
    return new Promise<File>(resolve => {
        entry.file(f => {
            resolve(f);
        });
    });
};

const getEntriesFromReader: (
    reader: IReader
) => Promise<IWebkitEntry[]> = reader => {
    return new Promise<IWebkitEntry[]>(resolve => {
        reader.readEntries(entries => {
            resolve(entries);
        });
    });
};

const getAllFiles: (
    entry: IWebkitEntry,
    fileInfoInput: File[]
) => Promise<File[]> = async (entry, fileInfoInput) => {
    const reader: IReader = entry.createReader();
    const entries: IWebkitEntry[] = await getEntriesFromReader(reader);
    for (const entry of entries) {
        if (entry.isDirectory) {
            await getAllFiles(entry, fileInfoInput);
        } else {
            fileInfoInput.push(
                await getFileFromEntry(entry)
            );
        }
    }
    return fileInfoInput;
};

const onDrop: (e: DragEvent) => void = async e => {
    e.preventDefault()
    e.stopPropagation()
    setIsDragging(false)
    const dataTransferItems: DataTransferItemList = e.dataTransfer!.items

    //Assume one file in the drag and drop for now
    const files: File[] = await getAllFiles(dataTransferItems[0].webkitGetAsEntry(), [])
    processFiles(files)
}

const getFileInfo: (tdrFile: TdrFile) => Promise<CreateFileInput> = async (tdrFile) => {
    const clientSideChecksum = await generateHash(tdrFile);
    const urlParams: URLSearchParams = new URLSearchParams(window.location.search);
    const consignmentId = parseInt(urlParams.get("consignmentId")!, 10);
    const fileInfo: CreateFileInput = {
        consignmentId,
        clientSideChecksum,
        fileSize: tdrFile.size,
        path: tdrFile.webkitRelativePath,
        lastModifiedDate: new Date(tdrFile.lastModified),
        fileName: tdrFile.name
    };
    return fileInfo;
}

export { upload }

async function processFiles(files: File[] | FileList | null) {
    const fileInfoList: CreateFileInput[] = [];
    if (files) {
        const filePathToFile: FilePathMap[] = [];
        for (var i = 0; i < files.length; i++) {
            const tdrFile = <TdrFile>files[i];
            const fileInfo: CreateFileInput = await getFileInfo(tdrFile);

            fileInfoList.push(fileInfo);
            filePathToFile.push({ path: fileInfo.path!, file: tdrFile });
        }
        Axios.post("/presignedUrls", { data: fileInfoList }).then(async data => {
            for (const file of filePathToFile) {
                const url = data.data[file.path];
                const formData = new FormData();
                formData.append("file", file.file);
                await Axios.put(url, formData)
            }
            const urlParams: URLSearchParams = new URLSearchParams(window.location.search);
            const consignmentId = parseInt(urlParams.get("consignmentId")!, 10);
            window.location.href = `/fileStatus?consignmentId=${consignmentId}`
        }).catch(err => {
            const error: HTMLParagraphElement | null = document.querySelector('.error')
            error!.innerText = "There has been an error"
        })

    }
}
