import Axios from "axios";
import * as S3 from "aws-sdk/clients/s3";
import { ChecksumCalculator } from "./checksum";

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

const upload: (checksumCalculator: ChecksumCalculator) => void = (checksumCalculator) => {
    const uploadForm: HTMLFormElement | null = document.querySelector(
        "#file-upload-form"
    );
    const commenceUploadForm: HTMLFormElement | null = document.querySelector(
        "#commence-upload-form"
    );

    if (uploadForm) {
        uploadForm.addEventListener("submit", ev => {
            ev.preventDefault();
            const target: HTMLInputTarget | null = ev.currentTarget;
            const files: TdrFile[] = target!.files!.files!;
            processFiles(files, checksumCalculator)
                .then(() => {
                    if (commenceUploadForm) {
                        commenceUploadForm.submit();
                    }
                })
                .catch(err => {
                    console.log(err);
                    const error: HTMLDivElement | null = document.querySelector(
                        ".govuk-error-summary"
                    );
                    const displayError: HTMLParagraphElement | null = document.querySelector(
                        ".errorMessage"
                    );
                    displayError!.innerText = err;
                    error!.classList.remove("hide");
                });
        });
    }

    const onDrop: (e: DragEvent) => void = async e => {
        e.preventDefault();
        e.stopPropagation();
        setIsDragging(false);
        const dataTransferItems: DataTransferItemList = e.dataTransfer!.items;

        //Assume one folder in the drag and drop for now
        const files: TdrFile[] = await getAllFiles(
            dataTransferItems[0].webkitGetAsEntry(),
            []
        );
        processFiles(files, checksumCalculator);
    };

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
) => Promise<IWebkitEntry[]> = async reader => {
    let allEntries: IWebkitEntry[] = [];

    let nextBatch = await getEntryBatch(reader);

    while (nextBatch.length > 0) {
        allEntries = allEntries.concat(nextBatch);
        nextBatch = await getEntryBatch(reader);
    }

    return allEntries;
};

const getEntryBatch: (reader: IReader) => Promise<IWebkitEntry[]> = reader => {
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

const getFileInfo: (
    tdrFile: TdrFile,
    useJsForChecksums: boolean,
    checksumCalculator: ChecksumCalculator
) => Promise<CreateFileInput> = async (tdrFile, useJsForChecksums, checksumCalculator) => {
    const progress: (percentage: number) => void = percentage => {
        if (tdrFile.size > 10000000) {
            console.log(`Progress: ${percentage}%`);
        }
    };
    const clientSideChecksum = await checksumCalculator.calculateChecksum(tdrFile, useJsForChecksums, progress);
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

// Temporary override to allow us to switch between WebAssembly and JavaScript checksums for performance testing.
const useJsForChecksums: () => boolean = () => {
    return window.localStorage.getItem("useJsForChecksums") === "true";
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

async function uploadFileData(batches: CreateFileInput[][], consignmentId: number) {
    let responses: AxiosResponse[] = [];

    for (const batch of batches) {
        const response = await Axios.post<{}, AxiosResponse>(
            `/filedata?consignmentId=${consignmentId}`,
            {
                data: batch
            }
        );
        responses = responses.concat(response);
    }

    return responses;
}

async function processFiles(files: TdrFile[], checksumCalculator: ChecksumCalculator) {
    const fileInfoList: CreateFileInput[] = [];
    const urlParams: URLSearchParams = new URLSearchParams(
        window.location.search
    );
    const consignmentId = parseInt(urlParams.get("consignmentId")!, 10);
    if (files) {
        const filePathToFile: { [key: string]: File } = {};

        const fileInfoStart = new Date().getTime();

        let fileInfoCount = 0;

        const jsChecksumOverride = useJsForChecksums();

        for (var tdrFile of files) {
            if (fileInfoCount % 100 == 0) {
                console.log(`Got file info for ${fileInfoCount} files`);
            }

            const fileInfo: CreateFileInput = await getFileInfo(tdrFile, jsChecksumOverride, checksumCalculator);

            fileInfoList.push(fileInfo);
            filePathToFile[fileInfo.path!] = tdrFile;
            fileInfoCount++;
        }
        const fileInfoEnd = new Date().getTime();
        console.log(
            `Got info for ${files.length} files in ${fileInfoEnd -
                fileInfoStart} ms`
        );

        const fileDataBatches = createBatches(
            fileInfoList,
            fileDataUploadBatchSize
        );

        const fileDataUploadStart = new Date().getTime();
        const responses = await uploadFileData(fileDataBatches, consignmentId);
        const fileDataUploadEnd = new Date().getTime();
        console.log(
            `Uploaded data for ${files.length} files in ${
                fileDataBatches.length
            } batches in ${fileDataUploadEnd - fileDataUploadStart} ms`
        );

        const s3UploadStart = new Date().getTime();

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
        }

        const s3UploadEnd = new Date().getTime();
        console.log(
            `Uploaded ${files.length} files in ${s3UploadEnd -
                s3UploadStart} ms`
        );
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
            function(err, data) {
                if (err) {
                    reject(err);
                }
                resolve(data);
            }
        );
    });
}
