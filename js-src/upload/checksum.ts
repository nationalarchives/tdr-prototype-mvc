import { Sha256, bytes_to_hex } from "asmcrypto.js";

const FILE_CHUNK_SIZE_BYTES = 10000000;

export interface WebAssemblyChecksumCalculator {
    generate_checksum(blob: any, callback: (percentage: number) => void): any;
}

export class ChecksumCalculator {
    wasmChecksumModule?: WebAssemblyChecksumCalculator;

    constructor(wasmChecksumModule?: WebAssemblyChecksumCalculator) {
        this.wasmChecksumModule = wasmChecksumModule;
    }

    calculateChecksum(file: File, useJsForChecksums: boolean, handleProgress: (percentage: number) => void) {
        if (this.wasmChecksumModule && !useJsForChecksums) {
            return this.wasmChecksumModule.generate_checksum(file, handleProgress);
        } else {
            return generateHash(file, handleProgress);
        }
    }
}

const generateHash: (file: File, handleProgress: (percentage: number) => void) => Promise<string> = async (file, handleProgress) => {
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

                    const progressPercent = 100 * (i + 1) / chunkCount;
                    handleProgress(progressPercent);

                    resolve();
                }
            };
        });
    }

    const result = sha256.finish().result!;
    return bytes_to_hex(result);
};
