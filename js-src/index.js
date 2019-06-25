import FileUpload from "./components/FileUpload.jsx"
import React from "react";
import ReactDOM from "react-dom";

window.onload = function () {
    const domContainer = document.querySelector('#file_upload_container');

    if (domContainer) {
        ReactDOM.render(<FileUpload/>, domContainer);
    }
}
