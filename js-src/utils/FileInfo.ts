// Drop handler function to get all files
import {IReader, IWebkitEntry} from "./files";


const getFileFromEntry: (entry: IWebkitEntry) => Promise<File> = entry => {
    return new Promise<File>(resolve => {
        entry.file(f => {
            resolve(f);
        });
    });
};

async function getAllFileEntries(dataTransferItemList: DataTransferItemList) {
    let fileEntries:File[] = [];
    // Use BFS to traverse entire directory/file structure
    let queue = [];
    // Unfortunately dataTransferItemList is not iterable i.e. no forEach
    for (let i = 0; i < dataTransferItemList.length; i++) {
        queue.push(dataTransferItemList[i].webkitGetAsEntry());
    }
    while (queue.length > 0) {
        let entry:IWebkitEntry = queue.shift();
        if (entry.isFile) {
            fileEntries.push(await getFileFromEntry(entry));
        } else if (entry.isDirectory) {
            let reader = entry.createReader();
            queue.push(...await readAllDirectoryEntries(reader));
        }
    }
    return fileEntries;
}

// Get all the entries (files or sub-directories) in a directory by calling readEntries until it returns empty array
async function readAllDirectoryEntries(directoryReader :IReader) {
    let entries = [];
    let readEntries:any = await readEntriesPromise(directoryReader);
    while (readEntries.length > 0) {
        entries.push(...readEntries);
        readEntries = await readEntriesPromise(directoryReader);
    }
    return entries;
}

// Wrap readEntries in a promise to make working with readEntries easier
async function readEntriesPromise(directoryReader:IReader) {
    try {
        return await new Promise((resolve, reject) => {
            directoryReader.readEntries(resolve);
        });
    } catch (err) {
        console.log(err);
    }
}

export { getAllFileEntries };