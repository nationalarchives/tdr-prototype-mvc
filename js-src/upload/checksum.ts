export const generateHash: (file: File) => Promise<string> = file => {
    const hashStart = new Date().getTime();

    const crypto = self.crypto.subtle;
    const fileReader = new FileReader();
    fileReader.readAsArrayBuffer(file);
    return new Promise(resolve => {
        fileReader.onload = async function() {
            const fileReaderResult = fileReader.result;
            if (fileReaderResult instanceof ArrayBuffer) {
                const buffer = await crypto.digest("SHA-256", fileReaderResult);

                if (file.size > 1000000) {
                    const hashEnd = new Date().getTime();
                    console.log(
                        `Calculated hash for ${file.size} byte file ${hashEnd -
                        hashStart} ms`
                    );
                }

                resolve(hexString(buffer));
            }
        };
    });
};

const hexString = (buffer: ArrayBuffer) => {
    const byteArray = new Uint8Array(buffer);
    const hexCodes = [...byteArray].map(value => {
        const hexCode = value.toString(16);
        return hexCode.padStart(2, "0");
    });
    return hexCodes.join("");
};