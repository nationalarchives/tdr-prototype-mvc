import { gql } from 'apollo-boost'

export const CreateFile = gql`
  mutation CreateFile($input: CreateFileInput!) {
    createFile(createFileInput: $input) {
        path,
        id,
        consignmentId 
    }
  }
`
export const CreateMultipleFiles =  gql`
  mutation CreateMultipleFiles($inputs: [CreateFileInput!]!) {
    createMultipleFiles(createFileInputs: $inputs) {
        path,
        id,
        consignmentId 
    }
  }
`