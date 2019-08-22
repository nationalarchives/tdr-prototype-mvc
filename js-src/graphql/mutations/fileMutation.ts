import { gql } from 'apollo-boost'

export default gql`
  mutation CreateFile($path: String!, $id: Int!) {
    createFile(path: $path, id: $id) {
        path,
        id,
        consignment {
            id,
            name
        } 
    }
  }
`