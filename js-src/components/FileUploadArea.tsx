import * as React from "react";
//import "Gov.css";
import { IWebkitEntry } from "../utils/files";
import { useGetFileInfo } from "../utils/useGetFileInfo";

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

interface IFileUploadAreaProps {
  onFilesProcessed: (fileInfo: IUpdateFile[]) => void;
  setIsLoading: React.Dispatch<boolean>;
}

const FileUploadArea: React.FunctionComponent<IFileUploadAreaProps> = props => {
  const [dataTransferItems, setDataTransferItems]: [
    IWebkitEntry[],
    React.Dispatch<IWebkitEntry[]>
  ] = React.useState<IWebkitEntry[]>([]);

  const [isDragging, setIsDragging]: [
    boolean,
    React.Dispatch<boolean>
  ] = React.useState<boolean>(false);

  const onDrop: (event: React.DragEvent<HTMLDivElement>) => void = event => {
    console.log("on drop");
    event.preventDefault();
    event.stopPropagation();
    setIsDragging(false);
    props.setIsLoading(true);

    const dataTransferItemList: IWebkitEntry[] = [];

    for (let index = 0; index < event.dataTransfer.items.length; index++) {
      const element = event.dataTransfer.items[index];
      dataTransferItemList.push(element.webkitGetAsEntry());
    }
    setDataTransferItems(dataTransferItemList);

  };

  const onDragOver: ( event: React.DragEvent<HTMLDivElement>) => void = event => {
    console.log("on drag over");
    event.preventDefault();
    event.stopPropagation();
    setIsDragging(true);
  };

  const onDragLeave: () => void = () => {
      console.log("on drag leave");
    setIsDragging(false);
  };
  const files: IUpdateFile[] = useGetFileInfo(dataTransferItems);
  if (files.length > 0) {
    props.onFilesProcessed(files);
  }

  return (
    <div
      className={`govuk-file-drop${isDragging ? "-drag" : ""}`}
      onDragOver={onDragOver}
      onDragLeave={onDragLeave}
      onDrop={onDrop}
    >
      Drop files ...
    </div>
  );
};

export { FileUploadArea };