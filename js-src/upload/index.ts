import Axios from "axios";
import * as S3 from "aws-sdk/clients/s3";

interface HTMLInputTarget extends EventTarget {
    files?: InputElement;
}

interface InputElement {
    files?: TdrFile[];
}

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

export const generateHash: (file: File) => Promise<string> = file => {
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

const upload: () => void = () => {
    const uploadForm: HTMLFormElement | null = document.querySelector(
        "#file-upload-form"
    );
    if (uploadForm) {
        uploadForm.addEventListener("submit", ev => {
            ev.preventDefault();
            const target: HTMLInputTarget | null = ev.currentTarget;
            const files: TdrFile[] = target!.files!.files!;
            processFiles(files)
                .then(() =>
                    uploadForm.submit()
                )
                .catch(err => {
                    console.log(err);
                    const error: HTMLParagraphElement | null = document.querySelector(
                        ".error"
                    );
                    error!.innerText = "There has been an error";
                });
        });
    }
    const dragAndDrop: HTMLDivElement | null = document.querySelector(
        ".govuk-file-drop"
    );
    if (dragAndDrop) {
        dragAndDrop.ondragover = onDragOver;
        dragAndDrop.ondragleave = () => setIsDragging(false);
        dragAndDrop.ondrop = onDrop;
    }
};

const setIsDragging: (isDragging: boolean) => void = isDragging => {
    let currentVal = ".govuk-file-drop";
    let fileDrop: HTMLDivElement | null = document.querySelector(
        ".govuk-file-drop"
    );
    if (!fileDrop) {
        currentVal = ".govuk-file-drop-drag";
        fileDrop = document.querySelector(currentVal);
    }
    const targetVal = isDragging ? ".govuk-file-drop-drag" : ".govuk-file-drop";
    const fileDropLabel: HTMLSpanElement | null = document.querySelector(
        ".draganddrop"
    );
    const labelText = isDragging ? "Drop now" : "Drag folder here";
    fileDropLabel!.innerText = labelText;
    fileDrop!.classList.replace(currentVal, targetVal);
};

const onDragOver: (e: DragEvent) => void = e => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(true);
};

const getFileFromEntry: (entry: IWebkitEntry) => Promise<TdrFile> = entry => {
    return new Promise<TdrFile>(resolve => {
        entry.file(f => {
            resolve(<TdrFile>f);
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
    fileInfoInput: TdrFile[]
) => Promise<TdrFile[]> = async (entry, fileInfoInput) => {
    const reader: IReader = entry.createReader();
    const entries: IWebkitEntry[] = await getEntriesFromReader(reader);
    for (const entry of entries) {
        if (entry.isDirectory) {
            await getAllFiles(entry, fileInfoInput);
        } else {
            fileInfoInput.push(await getFileFromEntry(entry));
        }
    }
    return fileInfoInput;
};

const onDrop: (e: DragEvent) => void = async e => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
    const dataTransferItems: DataTransferItemList = e.dataTransfer!.items;

    //Assume one file in the drag and drop for now
    const files: TdrFile[] = await getAllFiles(
        dataTransferItems[0].webkitGetAsEntry(),
        []
    );
    processFiles(files);
};

const getFileInfo: (
    tdrFile: TdrFile
) => Promise<CreateFileInput> = async tdrFile => {
    const clientSideChecksum = await generateHash(tdrFile);
    const urlParams: URLSearchParams = new URLSearchParams(
        window.location.search
    );
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
};

export { upload };

interface Credentials {
    accessKeyId: string;
    secretAccessKey: string;
    sessionToken: string;
}

interface AxiosResponse {
    data: IFileData;
}

interface IFileData {
    pathMap: { [key: string]: string };
    credentials: Credentials;
    bucketName: string;
}

const fileDataUploadBatchSize = 250;

function createBatches(files: CreateFileInput[], batchSize: number) {
    const batches: CreateFileInput[][] = [];

    for (var index = 0; index < files.length; index += batchSize) {
        batches.push(files.slice(index, index + batchSize));
    }

    return batches;
}

function uploadFileData(batches: CreateFileInput[][], consignmentId: number) {
    const responses = batches.map(async function (value) {
        return await Axios.post<{}, AxiosResponse>(
            `/filedata?consignmentId=${consignmentId}`,
            { data: value }
        );
    });
    return Promise.all(responses);
}

async function processFiles(files: TdrFile[]) {
    const fileInfoList: CreateFileInput[] = [];
    const urlParams: URLSearchParams = new URLSearchParams(
        window.location.search
    );
    const consignmentId = parseInt(urlParams.get("consignmentId")!, 10);
    if (files) {
        const filePathToFile: { [key: string]: File } = {}

        const fileInfoStart = new Date().getTime();
        for (var tdrFile of files) {
            const fileInfo: CreateFileInput = await getFileInfo(tdrFile);

            fileInfoList.push(fileInfo);
            filePathToFile[fileInfo.path!] = tdrFile
        }
        const fileInfoEnd = new Date().getTime();
        console.log(`Got info for ${files.length} files in ${fileInfoEnd - fileInfoStart} ms`);

        const fileDataBatches = createBatches(
            fileInfoList,
            fileDataUploadBatchSize
        );

        const fileDataUploadStart = new Date().getTime();
        const responses = await uploadFileData(fileDataBatches, consignmentId);
        const fileDataUploadEnd = new Date().getTime();
        console.log(`Uploaded data for ${files.length} files in ${fileDataBatches.length} batches in ${fileDataUploadEnd - fileDataUploadStart} ms`);

        for (const response of responses) {
            const fileData = response!.data;
            const {
                accessKeyId,
                secretAccessKey,
                sessionToken
            } = response!.data.credentials;
            const region = "eu-west-2";
            var s3 = new S3({
                accessKeyId,
                secretAccessKey,
                sessionToken,
                region
            });

            const s3UploadStart = new Date().getTime();
            for (const path of Object.keys(response.data!.pathMap)) {
                const file = filePathToFile[path];
                const id = fileData.pathMap[path];

                await uploadToS3(
                    s3,
                    `${consignmentId}/${id}`,
                    fileData.bucketName,
                    file
                );
            }
            const s3UploadEnd = new Date().getTime();
            console.log(`Uploaded ${files.length} files in ${s3UploadEnd - s3UploadStart} ms`);

        }
    }
}
function uploadToS3(s3: S3, key: string, bucketName: string, file: File) {
    return new Promise((resolve, reject) => {
        s3.upload(
            {
                Key: key,
                Bucket: bucketName,
                Body: file
            },
            function (err, data) {
                if (err) {
                    reject(err);
                }
                resolve(data);
            }
        );
    });
}
