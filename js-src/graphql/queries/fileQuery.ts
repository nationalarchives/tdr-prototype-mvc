import { gql } from 'apollo-boost'

export default gql`
  query GetFiles {
    getFiles {
        path,
        id,
        consignment {
            id,
            name
        }
    }
  }
`