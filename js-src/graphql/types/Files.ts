import { Consignments } from "./Consignments";

// ====================================================
// GraphQL query operation: Files
// ====================================================

export interface Files_files {
    __typename: "File";
    /**
     * The ID of an object
     */
    id: number;
    /**
     * The path of this File
     */
    path: string | null;
    /**
     * The consignment of this File
     */
    consignment: Consignments 
  }
  
  export interface Files {
    files: (Files_files | null)[] | null;
  }

  export interface CreateFileInput {    
    consignmentId: number;    
    path: string | null;
    fileSize: number;
    lastModifiedDate: string;
    clientSideChecksum: unknown;
    fileName: string;
  }

  export interface FilesVariables {    
    input: CreateFileInput
  }

  export interface MultipleFilesVariables {
    inputs: CreateFileInput[]
  }