import React, {Component} from "react";

class FileForm extends Component {
    render() {
        return (
            <div className="govuk-form-group">
                <label className="govuk-label" htmlFor="upload-files">
                    Upload a file
                </label>
                <input className="govuk-file-upload" id="upload-files" name="upload-files" type="file" webkitdirectory="" />
            </div>
        );
    }
}

export default FileForm;