import { Sha256, bytes_to_hex } from "asmcrypto.js";

const FILE_CHUNK_SIZE_BYTES = 10000000;

export const generateHash: (file: File) => Promise<string> = async file => {
    const hashStart = new Date().getTime();

    const fileReader = new FileReader();

    const fileSize = file.size;
    const chunkCount = Math.ceil(fileSize / FILE_CHUNK_SIZE_BYTES);

    const sha256 = new Sha256();

    for (let i = 0; i < chunkCount; i += 1) {
        const start = i * FILE_CHUNK_SIZE_BYTES;
        const end = start + FILE_CHUNK_SIZE_BYTES;

        let slice = file.slice(start, end);

        fileReader.readAsArrayBuffer(slice);

        await new Promise(resolve => {
            fileReader.onload = async function() {
                const result = fileReader.result;

                if (result instanceof ArrayBuffer) {
                    sha256.process(new Uint8Array(result));
                    resolve();
                }
            };
        });
    }

    const result = sha256.finish().result!;

    if (file.size > 1000000) {
        const hashEnd = new Date().getTime();
        console.log(`Calculated hash for ${file.size} byte file ${hashEnd - hashStart} ms`);
    }

    return bytes_to_hex(result);
};
