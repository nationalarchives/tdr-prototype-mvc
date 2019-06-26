import React, {Component} from "react";

class FileForm extends Component {

    constructor(props) {
        super(props);

        this.state = {
            files: []
        };

        this.handleFileSelect = this.handleFileSelect.bind(this);
        this.handleUpload = this.handleUpload.bind(this);
    }

    handleFileSelect(event) {
        const files = event.target.files;

        this.setState({ files });
    }

    handleUpload(event) {
        event.preventDefault();

        this.props.onUpload(this.state.files);
    }

    render() {
        return (
            <form onSubmit={this.handleUpload}>
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
                <button type="submit" className="govuk-button">
                    Upload
                </button>
            </form>
        );
    }
}

export default FileForm;