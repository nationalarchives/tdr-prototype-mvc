
interface TdrFile extends File {
    webkitRelativePath: string;
}
// Not uploading data just logging data to prove File api works
export const uploadFileMetadata = (files:FileList) => {

    const uploads = Array.from(files).map(async file => {
        await getFileMetadata( file.name, file)
    });

    return Promise.all(uploads);

};

const getFileMetadata =  async ( name:String, content:File) => {
    const fileInfo = await getFileInfo(<TdrFile>content);
    console.log(fileInfo);
    return fileInfo

};

const hexString = (buffer:ArrayBuffer) => {
    const byteArray = new Uint8Array(buffer);

    const hexCodes = [...byteArray].map(value => {
        const hexCode = value.toString(16);
        return hexCode.padStart(2, "0");
     });

    return hexCodes.join("");
};


const generateHash = (file:File) => {
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

const getFileInfo = async (file:TdrFile) => {
    const checksum = await generateHash(file);
    const fileInfo = {
        checksum:checksum,
        size: file.size,
        path:file.webkitRelativePath,
        lastModifiedDate: file.lastModified,
        fileName:file.name
    };

    return new Promise(resolve =>{
        resolve(fileInfo)
    });
};


