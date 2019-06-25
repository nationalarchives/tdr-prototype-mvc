'use strict';

class FileUpload extends React.Component {
    constructor(props) {
        super(props);
        this.state = {};
    }

    componentDidMount() {
        const url = window.location.href;
        const codeRegex = /.*?code\=([\w-]+)/;
        const awsCode = codeRegex.exec(url)[1];

        this.setState({
           awsCode: awsCode
        });
    }

    render() {
        return `In the file upload component. AWS code: '${this.state.awsCode}'`;
    }
}

const domContainer = document.querySelector('#file_upload_container');
if (domContainer) {
    ReactDOM.render(<FileUpload/>, domContainer);
}
