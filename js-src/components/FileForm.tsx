import * as React from "react";
import {ChangeEvent, FormEvent} from "react";
import {FileList} from "../models/File";

export interface FileFormProps {
    onUpload: (files: FileList) => void
}

interface FileFormState {
    files?: FileList
}

function DirectoryInput(inputProps: React.DetailedHTMLProps<React.InputHTMLAttributes<HTMLInputElement>, HTMLInputElement>) {
    const directoryProps = {
        type: "file",
        webkitdirectory: "",
        ...inputProps
    };
    return <input {...directoryProps} />;
}

class FileForm extends React.Component<FileFormProps, FileFormState> {

    constructor(props: FileFormProps) {
        super(props);

        this.state = {};

        this.handleFileSelect = this.handleFileSelect.bind(this);
        this.handleUpload = this.handleUpload.bind(this);
    }

    handleFileSelect(event: ChangeEvent) {
        const files = (event.target as HTMLInputElement).files;

        this.setState({ files });
    }

    handleUpload(event: FormEvent) {
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
                    <DirectoryInput
                        className="govuk-file-upload"
                        id="upload-files"
                        name="upload-files"
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