import * as React from "react";

import {authenticateUser} from "../aws/auth";

import {uploadFiles} from "../aws/s3Upload";
import {uploadFileMetadata} from "../aws/MetadataUpload"
import FileForm from "./FileForm";

import ApolloClient from 'apollo-boost'
import { ApolloProvider } from "react-apollo";
import pokemonQuery from '../qraphql/queries/pokemonQuery'
import { Pokemons, PokemonsVariables } from '../qraphql/queries/types/Pokemons'
import { useQuery } from '@apollo/react-hooks';


const clienta = new ApolloClient({
    uri: 'https://graphql-pokemon.now.sh/'
});




export interface FileUploadProps {}

interface FileUploadState {
    userAuthenticated: boolean,
    uploadedFileCount: number,
    uploadError?: any
}

function  Data () {
    try {
        const {data} = useQuery<Pokemons, PokemonsVariables>(
            pokemonQuery, {variables: {first: 10}}
        );
     return (<h2>hello {JSON.stringify(data, null, 2)}</h2>)
    } catch(err){
       return <h2> {err.message} </h2>
    }
}


export class FileUpload extends React.Component<FileUploadProps, FileUploadState> {
    constructor(props: FileUploadProps) {
        super(props);

        this.state = {
            userAuthenticated: false,
            uploadedFileCount: 0
        };

        this.handleUpload = this.handleUpload.bind(this);
    }

    componentDidMount() {
        const url = window.location.href;
        const codeRegex = /.*?code\=([\w-]+)/;
        let matches = codeRegex.exec(url);

        // Remove authentication code from page URL
        window.history.replaceState(null, "File upload", window.location.pathname);

        if (matches && matches[1]) {
            const awsCode = matches[1];

            authenticateUser(awsCode).then(() => {
                this.setState({ userAuthenticated: true })
            }).catch(error => {
                console.log("Error authenticating user");
                console.log(error);
            });
        }
    }


    handleUpload(files: File[]) {
        uploadFileMetadata(files).then(() => {
            return uploadFiles(files)
        })
            .then(() => {
                this.setState({uploadedFileCount: files.length})
            }).catch((error: any) => {
            this.setState({uploadError: error});
            console.log("Error uploading file");
            console.log(error);
        });

    }



    render() {

        if (!this.state.userAuthenticated) {
            return "Authenticating user...";
        } else if (this.state.uploadError) {
            return "Error uploading files";
        } else if (this.state.uploadedFileCount > 0) {
            // PL TODO: Add the uploaded filenames? Add button to return to dashboard?
            return (
                    <div>
                         <Data/>
                        <p>Thank you!<br/>{this.state.uploadedFileCount} files were uploaded</p>
                        <form>
                            <button type="submit" className="govuk-button">Dashboard</button>
                        </form>
                    </div>
                    );
        }

      return(
          <ApolloProvider client={clienta}>
           <FileForm onUpload={this.handleUpload} />
         </ApolloProvider>)

    }
}
