import { Consignments } from "./Consignments";

/* tslint:disable */

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

  export interface FilesVariables{
    path: string
    id: number
  }