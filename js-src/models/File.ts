export interface SelectedFile {
    name: string
}

export interface FileList {
    readonly length: number;
    item(index: number): SelectedFile | null;
    [index: number]: SelectedFile;
}