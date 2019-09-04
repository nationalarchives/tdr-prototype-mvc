// ====================================================
// GraphQL query operation: Files
// ====================================================

export interface Files_files {
    __typename: "File";
    /**
     * The ID of an object
     */
    id: string;
    /**
     * The path of this File
     */
    path: string | null;
    /**
     * The consignment of this File
     */
    consignmentId: number;
  }
  
  export interface Files {
    createMultipleFiles: (Files_files)[];
  }

  export interface CreateFileInput {    
    consignmentId: number;    
    path: string | null;
    fileSize: number;
    lastModifiedDate: string;
    clientSideChecksum: unknown;
    fileName: string;
  }

  export interface MultipleFilesVariables {
    inputs: CreateFileInput[]
  }