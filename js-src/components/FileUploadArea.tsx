import * as React from "react";
import { getAllFileEntries } from "../utils/FileInfo";

interface IFileUploadAreaProps {
  onFilesProcessed: (files: File[]) => void;
  setIsLoading: React.Dispatch<boolean>;
}

const FileUploadArea: React.FunctionComponent<IFileUploadAreaProps> = props => {

  const [isDragging, setIsDragging]: [
    boolean,
    React.Dispatch<boolean>
  ] = React.useState<boolean>(false);

  const onDrop: (event: React.DragEvent<HTMLDivElement>) => void = async (event )=> {
    console.log("on drop! ");
    event.preventDefault();
    event.stopPropagation();
    setIsDragging(false);
    props.setIsLoading(true);

    //const dataTransferItemList: IWebkitEntry[] = [];

    const items: File[] = await getAllFileEntries(event.dataTransfer.items);
    console.log("items " + items);

      if (items.length > 0) {
          props.onFilesProcessed(items);
      }

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