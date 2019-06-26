import React, {Component} from "react";

class FileForm extends Component {

    constructor(props) {
        super(props);

        this.state = {
            files: []
        };

        this.handleFileSelect = this.handleFileSelect.bind(this);
    }

    handleFileSelect(event) {
        const files = event.target.files;
        console.log("Files were selected");
        console.log(files);

        this.setState({ files });
    }

    render() {
        return (
            <div>
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
                <p>You have selected {this.state.files.length} files.</p>
                <button type="submit" className="govuk-button">
                    Upload
                </button>
            </div>
        );
    }
}

export default FileForm;