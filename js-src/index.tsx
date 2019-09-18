import * as React from "react";
import * as ReactDOM from "react-dom";

import { FileUpload } from "./components/FileUpload";
import { updateFileStatuses } from "./fileStatus"

window.onload = function () {
    const fileUploadContainer = document.querySelector('#file_upload_container');
    if (fileUploadContainer) {
        ReactDOM.render(<FileUpload />, fileUploadContainer);
    }
    const fileStatusContainer: HTMLDivElement | null = document.querySelector(".file-status")
    if (fileStatusContainer) {
        updateFileStatuses()
    }
};
