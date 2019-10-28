import { updateFileStatuses } from "./fileStatus";
import {ChecksumCalculator, upload} from "./upload";

const wasmSupported = (() => {
    try {
        if (
            typeof WebAssembly === "object" &&
            typeof WebAssembly.instantiate === "function"
        ) {
            const module = new WebAssembly.Module(
                Uint8Array.of(0x0, 0x61, 0x73, 0x6d, 0x01, 0x00, 0x00, 0x00)
            );
            if (module instanceof WebAssembly.Module)
                return (
                    new WebAssembly.Instance(module) instanceof
                    WebAssembly.Instance
                );
        }
    } catch (e) {}
    return false;
})();

window.onload = function() {
    if (wasmSupported) {
        // @ts-ignore
        import("@nationalarchives/checksum-calculator")
            .then(checksumModule => {
                renderModules(checksumModule);
            })
            .catch(e => {
                console.error("Error importing checksum module:", e);
                renderModules()
            });
    } else {
        renderModules();
    }
};

const renderModules = (checksumCalculator?: ChecksumCalculator) => {
    const uploadContainer: HTMLDivElement | null = document.querySelector(".upload-form");
    if (uploadContainer) {
        upload(checksumCalculator);
    }
    const fileStatusContainer: HTMLDivElement | null = document.querySelector(".file-status");
    if (fileStatusContainer) {
        updateFileStatuses();
    }
};
