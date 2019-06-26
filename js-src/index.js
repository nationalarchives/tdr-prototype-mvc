import FileUpload from "./components/FileUpload.jsx"
import FileForm from "./components/FileForm.jsx"
import React from "react";
import ReactDOM from "react-dom";

window.onload = function () {
    const fileUploadContainer = document.querySelector('#file_upload_container');
    if (fileUploadContainer) {
        ReactDOM.render(<FileUpload/>, fileUploadContainer);
    }

    // TODO: Move into fileUploadContainer
    const tmpFileFormContainer = document.querySelector('#file_form_container');
    if (tmpFileFormContainer) {
        ReactDOM.render(<FileForm/>, tmpFileFormContainer);
    }
}
