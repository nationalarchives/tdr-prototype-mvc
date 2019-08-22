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

  // Dropping the folder 
  const onDrop: (event: React.DragEvent<HTMLDivElement>) => void = async (event )=> {
    event.preventDefault();
    event.stopPropagation();
    setIsDragging(false);
    props.setIsLoading(true);

    const items: File[] = await getAllFileEntries(event.dataTransfer.items);
    items.forEach(item => {
      console.log(item.name)
    });

      if (items.length > 0) {
          props.onFilesProcessed(items);
      }

  };

  // Dragging folder in the upload area
  const onDragOver: ( event: React.DragEvent<HTMLDivElement>) => void = event => {
    event.preventDefault();
    event.stopPropagation();
    setIsDragging(true);
  };

  // Dragging folder out of the upload area
  const onDragLeave: () => void = () => {
     setIsDragging(false);
  };

  return (
    <div className={`govuk-file-drop${isDragging ? "-drag" : ""}`}
      onDragOver={onDragOver}
      onDragLeave={onDragLeave}
      onDrop={onDrop}>

      <svg width="50" height="43" viewBox="0 0 50 43">
        <path d="M48.4 26.5c-.9 0-1.7.7-1.7 1.7v11.6h-43.3v-11.6c0-.9-.7-1.7-1.7-1.7s-1.7.7-1.7 1.7v13.2c0 .9.7 1.7 1.7 1.7h46.7c.9 0 1.7-.7 1.7-1.7v-13.2c0-1-.7-1.7-1.7-1.7zm-24.5 6.1c.3.3.8.5 1.2.5.4 0 .9-.2 1.2-.5l10-11.6c.7-.7.7-1.7 0-2.4s-1.7-.7-2.4 0l-7.1 8.3v-25.3c0-.9-.7-1.7-1.7-1.7s-1.7.7-1.7 1.7v25.3l-7.1-8.3c-.7-.7-1.7-.7-2.4 0s-.7 1.7 0 2.4l10 11.6z"></path>
      </svg>
      <span className={"draganddrop"}>{isDragging ? "Drop now" : "Drag folder here"}</span>

    </div>
  );
};

export { FileUploadArea };