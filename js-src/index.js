import FileUpload from "./components/FileUpload.jsx"
import React from "react";
import ReactDOM from "react-dom";

window.onload = function () {
    console.log("Hello world!");

    const domContainer = document.querySelector('#file_upload_container');
    if (domContainer) {
        console.log("Rendering FileUpload");
        ReactDOM.render(<FileUpload/>, domContainer);
    } else {
        console.log("No DOM container in which to render")
    }
}
