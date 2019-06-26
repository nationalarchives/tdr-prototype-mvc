import React, {Component} from "react";

class FileForm extends Component {

    constructor(props) {
        super(props);

        this.handleFileSelect = this.handleFileSelect.bind(this);
    }

    handleFileSelect(event) {
        const files = event.target.files;
        console.log("Files were selected");
        console.log(files);
    }

    render() {
        return (
            <div className="govuk-form-group">
                <label className="govuk-label" htmlFor="upload-files">
                    Upload a file
                </label>
                <input
                    className="govuk-file-upload"
                    id="upload-files"
                    name="upload-files"
                    type="file"
                    webkitdirectory=""
                    onChange={this.handleFileSelect}
                />
            </div>
        );
    }
}

export default FileForm;