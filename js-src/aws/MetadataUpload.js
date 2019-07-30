import uuid4 from "uuid";

export const uploadFileMetadata = (files) => {

    const uploads = Array.from(files).map(async file => {
        await uploadMetadata( file.name, file)
    });

    return Promise.all(uploads);

};

const uploadMetadata =  async ( name, content) => {
    const fileInfo = await getFileInfo(content);
    console.log(fileInfo);
    return fileInfo

};

const hexString = buffer => {
    const byteArray = new Uint8Array(buffer);

    const hexCodes = [...byteArray].map(value => {
        const hexCode = value.toString(16);
        return hexCode.padStart(2, "0");
     });

    return hexCodes.join("");
};


const generateHash = file => {
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

const getFileInfo = async file => {
    const fileId = uuid4();
    const checksum = await generateHash(file);
    const fileInfo = {
        id:fileId,
        checksum:checksum,
        size: file.size,
        path:file.webkitRelativePath,
        latModifiedDate: file.lastModified,
        fileName:file.name
    };

    return new Promise(resolve =>{
        resolve(fileInfo)
    });
};


