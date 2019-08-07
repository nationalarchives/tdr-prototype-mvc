import * as React from "react";
import { IWebkitEntry, IFileInfo, IReader } from "../utils/files";
import { IUpdateFile } from "../components/FileUploadArea";

function useGetFileInfo(dataItems: IWebkitEntry[]) {
  const [fileInfo, setFileInfo]: [
    IUpdateFile[],
    React.Dispatch<IUpdateFile[]>
  ] = React.useState<IUpdateFile[]>([]);

  const getFileFromEntry: (entry: IWebkitEntry) => Promise<File> = entry => {
    return new Promise<File>(resolve => {
      entry.file(f => {
        resolve(f);
      });
    });
  };

  const getEntriesFromReader: (
    reader: IReader
  ) => Promise<IWebkitEntry[]> = reader => {
    return new Promise<IWebkitEntry[]>(resolve => {
      reader.readEntries(entries => {
        resolve(entries);
      });
    });
  };

  const getAllFiles: (
    entry: IWebkitEntry,
    fileInfoInput: IFileInfo[]
  ) => Promise<IFileInfo[]> = async (entry, fileInfoInput) => {
    const reader: IReader = entry.createReader();
    const entries: IWebkitEntry[] = await getEntriesFromReader(reader);
    for (const entry of entries) {
      if (entry.isDirectory) {
        await getAllFiles(entry, fileInfoInput);
      } else {
        fileInfoInput.push({
          entry: entry,
          file: await getFileFromEntry(entry),
          shaHash: "0"
        });
      }
    }
    return fileInfoInput;
  };

  React.useEffect(() => {
    const getFileInfo: () => void = async () => {
      let allFileInfo: IFileInfo[] = [];
      for (const item of dataItems) {
        const allFiles: IFileInfo[] = await getAllFiles(item, []);
        allFileInfo = allFileInfo.concat(allFiles);
      }

      const updateFiles: IUpdateFile[] = allFileInfo.map(fileInfo => ({
        id: "UUID",
        checksum: fileInfo.shaHash,
        size: fileInfo.file.size.toString(),
        path: fileInfo.entry.fullPath,
        lastModifiedDate: fileInfo.file.lastModified.toString(),
        file: fileInfo.file,
        fileName: fileInfo.file.name
      }));
      setFileInfo(updateFiles);
    };
    getFileInfo();
  });

  return fileInfo;
}

export { useGetFileInfo };