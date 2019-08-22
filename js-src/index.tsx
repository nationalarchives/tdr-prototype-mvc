import * as React from "react";
import * as ReactDOM from "react-dom";

import { FileUpload } from "./components/FileUpload";

import ApolloClient from 'apollo-client'
import { ApolloProvider } from "react-apollo";
import { InMemoryCache } from 'apollo-cache-inmemory';
import { HttpLink } from 'apollo-link-http';

declare var APOLLO_CLIENT_URI: string;

const cache = new InMemoryCache();
const link = new HttpLink({
  uri: APOLLO_CLIENT_URI
});

export const apolloClient = new ApolloClient(
    {
        link: link,
        cache: cache,
        defaultOptions: {
            mutate: {
                errorPolicy: 'all'
            }
        }        
    }
);

window.onload = function () {
    const fileUploadContainer = document.querySelector('#file_upload_container');
    if (fileUploadContainer) {
        ReactDOM.render(
            <ApolloProvider client={ apolloClient }>
                <FileUpload />
            </ApolloProvider>,
             fileUploadContainer);
    }
};
