import FileUpload from "./components/FileUpload.jsx"
import React from "react";
import ReactDOM from "react-dom";

window.onload = function () {
    const fileUploadContainer = document.querySelector('#file_upload_container');
    if (fileUploadContainer) {
        ReactDOM.render(<FileUpload/>, fileUploadContainer);
    }
};
