import * as React from "react";
import {ChangeEvent, FormEvent} from "react";
import {FileUploadArea} from './FileUploadArea';


export interface FileFormProps {
    onUpload: (files: FileList) => void
}

interface FileFormState {
    files?: FileList
}


interface IUpdateFileInfo {
    id: string;
    checksum: string;
    size: string;
    path: string;
    lastModifiedDate: string;
    fileName: string;
  }

  export interface IUpdateFile extends IUpdateFileInfo {
    file: File;
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
        console.log("handle file select");
        const files = (event.target as HTMLInputElement).files;

        if (files) {
            this.setState({ files });
        }
    }

    handleUpload(event: FormEvent) {
        console.log("handle file upload");
        event.preventDefault();

        const currentFiles = this.state.files;
        if (currentFiles) {
            this.props.onUpload(currentFiles);
        }
    }

    onFilesProcessed(fileInfo: IUpdateFile[]) {
        console.log("files processed", fileInfo);
    }

    setIsLoading(data: any) {
        console.log("setIsLoading", data);
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

              <FileUploadArea
                onFilesProcessed={this.onFilesProcessed}
                setIsLoading={this.setIsLoading}
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
