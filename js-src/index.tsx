import * as React from "react";
import * as ReactDOM from "react-dom";

import { FileUpload } from "./components/FileUpload";

window.onload = function () {
    const fileUploadContainer = document.querySelector('#file_upload_container');
    if (fileUploadContainer) {
        ReactDOM.render(<FileUpload />, fileUploadContainer);
    }
};
