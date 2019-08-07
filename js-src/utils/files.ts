export interface IFileInfo {
    entry: IWebkitEntry;
    shaHash: string;
    file: File;
  }

  export interface IReader {
    readEntries: (callbackFunction: (entry: IWebkitEntry[]) => void) => void;
  }

  export interface IWebkitEntry extends DataTransferItem {
    createReader: () => IReader;
    isFile: boolean;
    isDirectory: boolean;
    fullPath: string;
    file: (success: (file: File) => void) => void;
  }