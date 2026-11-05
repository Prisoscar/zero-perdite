import { html, LitElement } from 'lit';
import {customElement} from 'lit/decorators.js';

@customElement('super-layout')
export class IOOperationsLayer extends LitElement {

    declare $server: {
        requestToken: () =>  void;
        handleFileUploadUpdate: (rowUuid: string, uploadedBytes: number, totalBytes: number) => void;
        evaluateCanUpload: (currentDirectory: string) => void;
        addUploadRow: (filename: string, rowUuid: string, isDirectory: boolean, newTotalFiles: number) => void;
        handleSuccessUpload: (rowUuid: string, filename: string) => void;
        handleFailedUpload: (rowUuid: string, error: string, filename: string) => void;
        handleDirectoryFileUpload: (rowUuid: string, newFailures: number) => void;
        handleDirectoryUploadAborted: (rowUuid: string) => void;
        handleVerifyDirectory: (directoryName: string, targetDirectory: string, longestNameLength: number, directorySize: number, totalFiles: number, totalDirectories: number, totalReadErrors: number) => void;
        handleCreateDirectory: (filename: string, targetDirectory: string) => void;
        handleCreateEmpty: (filename: string, targetDirectory: string) => void;
        handleUploadChunk: () => void;
        handleUpdateZipJobStatus: () => void;
        handleZipJobCloseButtonClick: () => void;
        handleUploadCloseButtonClick: (filename: string, targetDirectory: string) => void;
      };

  processing = false;
  uploads : { entry: any, rowUuid: string, targetDirectory: string, report: {totalSize: number, longestPath: string, totalDirectories: number, totalFiles: number, readErrors: number} }[] = [];
  loadingZipJobSpan: any;
  loadingUploadSpan: any;
  uploadAborted: boolean = false;
  uploadIsDirectory: boolean = false;
  private currentFilename: string = "";
  private currentTargetDirectory: string = "";
  private currentUploadDirectoryRowUuid: string = "";

  createRenderRoot() {
    this.initializeViewerElements()
    // Do not use a shadow root
    return this;
  }

  render() {
    return html`
    <div id='cloud-io-global-container'></div>
    <vaadin-vertical-layout id="uploads-modal-layout"
          @dragover=${this.handleDragOver}
          @drop=${this.handleDrop}>
        <vaadin-horizontal-layout id="uploads-modal-header-layout"></vaadin-horizontal-layout>
        <vaadin-vertical-layout id="uploads-container"></vaadin-vertical-layout>
    </vaadin-vertical-layout>`;
  }

  handleDragOver(event:Event){
      event.preventDefault();
      event.stopPropagation();
  }

  handleDrop(event:any){
    event.preventDefault();
    event.stopPropagation();
    this.handleFiles(event.dataTransfer.items);
  }

  inputFileChange(event: any){
        const files = (event.target as HTMLInputElement).files;
        if(files == null) return;
        const entries = [];
        for(const file of files){
            entries.push({
                webkitGetAsEntry: function() {
                 return {isFile: true, isDirectory: false, name: file.name, file: function(successCallback: any) {successCallback(file)}};
                }
            });
        }
        this.handleFiles(entries);
  }


/*
    WEB-SOCKET RELATE FOR FILE UPLOAD SECTION START
*/
  private _resolveJwt!: (value: string) => void;
  private uploadSocket?: WebSocket;


  requestJwt(): Promise<string> {
    return new Promise(resolve => {
      this._resolveJwt = resolve;
      this.$server.requestToken();
    });
  }

  onTokenReceived(token: string) {
    this._resolveJwt(token);
  }

  openUploadSocket(jwt: string, path: string, filename: string, totalSize: number, totalChunks: number): Promise<WebSocket> {
    const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
    const wsUrl =
      `${protocol}://${location.host}/ws/upload` +
      `?token=${encodeURIComponent(jwt)}` +
      `&path=${encodeURIComponent(path)}` +
      `&filename=${encodeURIComponent(filename)}` +
      `&totalSize=${encodeURIComponent(totalSize)}` +
      `&totalChunks=${encodeURIComponent(totalChunks)}`;

    return new Promise<WebSocket>((resolve, reject) => {
        const socket = new WebSocket(wsUrl);
        socket.binaryType = 'arraybuffer';

        socket.onopen = () => {
          this.uploadSocket = socket;
          resolve(socket);
        };

        socket.onerror = (err) => {
          reject(new Error('WebSocket connection failed'));
        };

        socket.onclose = (e) => {
          if (!e.wasClean) {
            reject(new Error(e.reason || 'WebSocket closed unexpectedly'));
          }
        };
      });
  }

  /*
      WEB-SOCKET RELATED FOR FILE UPLOAD SECTION END
  */


  async handleFiles(entries: any){
    let canUpload: boolean =  await new Promise(resolve => {
      (async () => {
        this._resolveEvaluateCanUpload = resolve;
        this.$server.evaluateCanUpload(await this.getCookie('directory'));
      })();
    });
    if (!canUpload) return;

    for (let entry of entries) {
        const file  = entry.webkitGetAsEntry();
        if(file == null) continue;
        const rowUuid = this.generateUUID();
        let report = { totalSize: 1, longestPath: '/', totalDirectories: 0, totalFiles: 1, readErrors: 0 };
        if(file.isDirectory) report = await this.updateDirectoryDetails(file);
        this.$server.addUploadRow(file.name, rowUuid, file.isDirectory, report.totalFiles);
        await this.waitForAddUploadRow();
        this.uploads.push({entry: file, rowUuid: rowUuid, targetDirectory: await this.getCookie('directory'), report: report});
    }
    if (!this.processing) this.processFiles();
  }

  async processFiles(){
    this.processing = true;
    while (this.uploads.length > 0){
        this.uploadAborted = false;
        const upload = (this.uploads.shift());
        if (upload !== undefined) {
            //HANDLE FILE UPLOAD
            if(upload.entry.isFile) {
                this.uploadIsDirectory = false;
                const fileUploadResult = await this.processFile(upload.entry, upload.targetDirectory, upload.rowUuid);
                if (fileUploadResult.code === 200){
                    this.$server.handleSuccessUpload(upload.rowUuid, fileUploadResult.response);
                    await this.waitForFinishedUpload();
                } else {
                    let error: string;
                    if(fileUploadResult.code === 500) error = fileUploadResult.response;
                    else error = 'generic-error';
                    this.$server.handleFailedUpload(upload.rowUuid, error, upload.entry.name);
                    await this.waitForFinishedUpload();
                }
            }
            //HANDLE DIRECTORY UPLOAD
            else {
                this.uploadIsDirectory = true;
                await this.processDirectory(upload.entry, upload.rowUuid, upload.targetDirectory, upload.report);
            }
        }
    }
    this.processing = false;
  }

  async processDirectory(entry: any, rowUuid: string, targetDirectory: string, report: {totalSize: number, longestPath: string, totalDirectories: number, totalFiles: number, readErrors: number}){

    const verificationResult = await this.verifyDirectoryForUpload(entry.name, targetDirectory, report);
    if (verificationResult.code != 200){
      let error: string;
      if(verificationResult.code === 500) error = verificationResult.response;
      else error = 'generic-error';
      this.$server.handleFailedUpload(rowUuid, error, entry.name);
      await this.waitForFinishedUpload();
      return;
    }
    const createParentResult = await this.createDirectory(entry.name, targetDirectory);
    const parsedResponse = typeof createParentResult.response === 'string' ? JSON.parse(createParentResult.response) : createParentResult.response;
    if (createParentResult.code != 200){
          let error: string;
          if(createParentResult.code === 500) error = parsedResponse.error;
          else error = 'generic-error';
          this.$server.handleFailedUpload(rowUuid, error, entry.name);
          await this.waitForFinishedUpload();
          return;
        }
    this.stackExit = false;
    await this.uploadChildren(entry, rowUuid, parsedResponse.id, false);
    //handle success
    this.$server.handleSuccessUpload(rowUuid, parsedResponse.filename);
    await this.waitForFinishedUpload();
  }

  async updateDirectoryDetails(entry: any, report: {totalSize: number, longestPath: string, totalDirectories: number, totalFiles: number, readErrors: number} = { totalSize: 0, longestPath: '', totalDirectories: 0, totalFiles: 0, readErrors: 0 }) : Promise<{totalSize: number, longestPath: string, totalDirectories: number, totalFiles: number, readErrors: number}>{
    let dirReader = entry.createReader();

    try {
        // Await the result of readEntries()
        const results = await new Promise<any[]>((resolve, reject) => {
            dirReader.readEntries(resolve, reject);
        });

        for(let entry of results){
            if(entry.fullPath.length > report.longestPath.length) report.longestPath = entry.fullPath;
            if (entry.isFile) {
                await new Promise<void>((resolve, reject) => {
                    entry.file(async (file: File) => {
                        report.totalFiles++;
                        report.totalSize += file.size;
                        resolve();
                    });
                });
            } else {
                report.totalDirectories++;
                await this.updateDirectoryDetails(entry, report);
            }
        }
    } catch (error) {
        /* handle error — error is a FileError object */
        report.readErrors++;
    }
    return report;
  }

  async processResults(results: any[], report: any) {
      for (let entry of results) {
          if (entry.fullPath.length > report.longestPath.length) {
              report.longestPath = entry.fullPath;
          }
          if (entry.isFile) {
              await new Promise<void>((resolve, reject) => {
                  entry.file(async (file: File) => {
                      report.totalFiles++;
                      report.totalSize += file.size;
                      resolve();
                  });
              });
          } else {
              report.totalDirectories++;
              await this.updateDirectoryDetails(entry, report);
          }
      }
  }

  async verifyDirectoryForUpload(directoryName: string, targetDirectory: string, report: {totalSize: number, longestPath: string, totalDirectories: number, totalFiles: number, readErrors: number}) : Promise<{ code: number, response: string }>{
      return await new Promise(resolve => {
            this._resolveVerifyDirectory = resolve;
            this.$server.handleVerifyDirectory(directoryName, targetDirectory, report.longestPath.length, report.totalSize, report.totalFiles, report.totalDirectories, report.readErrors);
          });
    }

  async createDirectory(name: string, targetDirectory: string) : Promise<{ code: number, response: any }> {
    return await new Promise(resolve => {
      this._resolveCreateDirectory = resolve;
      this.$server.handleCreateDirectory(name, targetDirectory);
    });
  }

  async processFile(entry: any, targetDirectory: string, rowUuid: string) : Promise<{ code: number, response: string }>{
    return await new Promise<{ code: number, response: string }>((resolve, reject) => {
            entry.file(async (file: File) => {
                if (file.size === 0) resolve(this.createEmptyFile(file, targetDirectory));
                else resolve(await this.uploadFile(file, targetDirectory, rowUuid));
            });
        });
  }

  async createEmptyFile(file: File, targetDirectory: string) : Promise<{ code: number, response: string }>{
    return await new Promise(resolve => {
      this._resolveCreateEmpty = resolve;
      this.$server.handleCreateEmpty(file.name, targetDirectory);
    });
  }

  async uploadChildren(entry: any, rowUuid: string, currentDirectory: string, handleSelf: boolean = true){
    if(entry.isFile){
        if(this.stackExit) return;
        if(!handleSelf) return;
        let fileUploadResult: any;
        this.currentUploadDirectoryRowUuid = rowUuid;
        if(this.uploadAborted === true && this.uploadIsDirectory === true) {
            await this.interruptDirectoryUpload(rowUuid);
            return;
        }

        else {
            fileUploadResult = await this.processFile(entry, currentDirectory, rowUuid);
            }
        let newFailures: number;
        if (fileUploadResult.code === 200) newFailures = 0;
        else newFailures = 1;
        this.$server.handleDirectoryFileUpload(this.currentUploadDirectoryRowUuid, newFailures);
        await this.waitForDirectoryUpload();
    } else {
        if(handleSelf){
            const createParentResult = await this.createDirectory(entry.name, currentDirectory);
            const parsedResponse = typeof createParentResult.response === 'string' ? JSON.parse(createParentResult.response) : createParentResult.response;
            if (createParentResult.code != 200) {
                const report = await this.updateDirectoryDetails(entry);
                this.currentUploadDirectoryRowUuid = rowUuid;
                this.$server.handleDirectoryFileUpload(rowUuid, report.totalFiles);
                await this.waitForDirectoryUpload();
                return;
            } else {
                currentDirectory =  parsedResponse.id;
            }
        }
        let dirReader = entry.createReader();
        const results = await new Promise<any[]>((resolve, reject) => {
            dirReader.readEntries(resolve, reject);
        });

        for(let nestedEntry of results){
            await this.uploadChildren(nestedEntry, rowUuid, currentDirectory);
        }
    }
  }

  stackExit: boolean = false;

  async interruptDirectoryUpload(rowUuid: string){
    this.stackExit = true;
    this.$server.handleDirectoryUploadAborted(this.currentUploadDirectoryRowUuid);
    await this.waitForInterruptDirectoryUpload();
  }

  async uploadFile(file: File, targetDirectory: string, rowUuid: string) : Promise<{ code: number, response: string }>{

    this.currentFilename = file.name;
    this.currentTargetDirectory = targetDirectory;

    const CHUNK_SIZE = 1024 * 1024; // 1 MB
    const totalBytes = file.size;
    const totalChunks = Math.ceil(totalBytes / CHUNK_SIZE);
    let uploadedBytes = 0;
    let offset = 0;

    const jwt = await this.requestJwt();

    const socket = await this.openUploadSocket(jwt, targetDirectory, file.name, totalBytes, totalChunks);

    return new Promise((resolve) => {

      (async () => {

          const cleanup = () => {
            socket.removeEventListener('message', onMessage);
            socket.removeEventListener('error', onError);
          };

          const onMessage = async (event: MessageEvent) => {
            try {
              const data = JSON.parse(event.data);
              if (data.type === 'UPLOADED') {

                //send next chunk
                const chunk = file.slice(offset, offset + CHUNK_SIZE);
                const buffer = await chunk.arrayBuffer();
                if (socket.readyState !== WebSocket.OPEN) {
                  this.closeCurrentUpload();
                  resolve({ code: 500, response: 'generic-error' });
                }
                socket.send(buffer);

                offset += CHUNK_SIZE;
                uploadedBytes += buffer.byteLength;

                this.$server.handleFileUploadUpdate(
                  rowUuid,
                  uploadedBytes,
                  totalBytes
                );

              }
              if (data.code && data.response) {
                cleanup();
                socket.close();
                if(data.response == "operation-aborted") this.closeCurrentUpload();
                resolve({
                  code: parseInt(data.code, 10),
                  response: data.response
                });
              }
            } catch {
              // ignore non JSON messages
            }
          };

          const onError = () => {
            cleanup();
            socket.close();
            this.closeCurrentUpload();
            resolve({ code: 500, response: 'generic-error' });
          };

          socket.addEventListener('message', onMessage);
          socket.addEventListener('error', onError);

          //send first chunk
          const chunk = file.slice(offset, offset + CHUNK_SIZE);
          const buffer = await chunk.arrayBuffer();
          if (socket.readyState !== WebSocket.OPEN) {
            this.closeCurrentUpload();
            resolve({ code: 500, response: 'generic-error' });
          }
          socket.send(buffer);

          offset += CHUNK_SIZE;
          uploadedBytes += buffer.byteLength;

          this.$server.handleFileUploadUpdate(
            rowUuid,
            uploadedBytes,
            totalBytes
          );
      })();
    });
  }

  async bufferToBase64(blob: Blob) {
    // use a FileReader to generate a base64 data URI:
    const base64url: any = await new Promise(r => {
      const reader = new FileReader()
      reader.onload = () => r(reader.result)
      reader.readAsDataURL(blob)
    });
    // remove the `data:...;base64,` part from the start
    return base64url.slice(base64url.indexOf(',') + 1);
  }

  async streamToUtf8String(stream: ReadableStream<Uint8Array>): Promise<string> {
        const decoder = new TextDecoder('utf-8');
        const reader = stream.getReader();
        let result = '';
        while (true) {
            const { done, value } = await reader.read();
            if (done) {
                break;
            }
            result += decoder.decode(value, { stream: true });
        }
        // Decode the remaining bytes in the stream (if any)
        result += decoder.decode();
        return result;
    }

    currentZipJobLoadingSpan: any;
    currentUploadLoadingSpan: any;
    viewerInitialized = false;

    initializeViewerElements(){
        if (this.viewerInitialized) return;
        this.loadingZipJobSpan = document.getElementById('loading-zip-job-span');
        this.loadingUploadSpan = document.getElementById('loading-upload-span');
        this.currentZipJobGlobalViewer = document.getElementById('current-zip-job-viewer');
        this.currentZipJobCloseButton = document.getElementById('zip-job-viewer-close-button');
        this.currentUploadGlobalViewer = document.getElementById('current-upload-viewer');
        this.currentUploadCloseButton = document.getElementById('upload-viewer-close-button');
        this.currentZipJobLoadingSpan = document.getElementById('loading-zip-job-span');
        this.currentUploadLoadingSpan = document.getElementById('loading-upload-span');
        //initialize close buttons
        this.currentZipJobCloseButton.addEventListener('click', async function() {
            const ioOperationsElement: any = document.getElementById('cloud-io-operations-layer');
            if(ioOperationsElement == null) return;
            ioOperationsElement.closeCurrentZipJob();
            ioOperationsElement.$server.handleZipJobCloseButtonClick();
        });
        this.currentUploadCloseButton.addEventListener('click', async function() {
            const ioOperationsElement: any = document.getElementById('cloud-io-operations-layer');
            if(ioOperationsElement == null) return;
            ioOperationsElement.closeCurrentUpload();
            ioOperationsElement.$server.handleUploadCloseButtonClick(ioOperationsElement.currentFilename, ioOperationsElement.currentTargetDirectory);
        });
        this.viewerInitialized = true;
    }

    interval: any;

    //used from java class
    async startZipJob(zipJobUuid: string){
        const ioOperationsElement: any = document.getElementById('cloud-io-operations-layer');
        if(ioOperationsElement == null) return;
        await this.setSessionCookie('current-zip-job-uuid', zipJobUuid);
        this.interval = setInterval(async function() {
//           ioOperationsElement.dispatchEvent(new CustomEvent('update-zip-job-status'));
          ioOperationsElement.$server.handleUpdateZipJobStatus();
        }, 750);
    }

    currentZipJobGlobalViewer: any;
    currentZipJobCloseButton: any;

    updateCurrentZipJobView(zipJobPreview: string, zipJobUpdateStatus: string){
        if(this.currentZipJobGlobalViewer == null) return;
        this.currentZipJobGlobalViewer.classList.remove('hidden-element');
        this.currentZipJobLoadingSpan.classList.remove('hidden-element');
        const zipJobViewerStatusDescriptionParagraph = document.getElementById('zip-job-viewer-status-description-paragraph');
        if (zipJobViewerStatusDescriptionParagraph !== null) {
            zipJobViewerStatusDescriptionParagraph.textContent = zipJobPreview;
        }
        const zipJobViewerStatusUpdateParagraph = document.getElementById('zip-job-viewer-status-update-paragraph');
        if (zipJobViewerStatusUpdateParagraph !== null) {
            zipJobViewerStatusUpdateParagraph.textContent = zipJobUpdateStatus;
        }
    }

    currentUploadGlobalViewer: any;
    currentUploadCloseButton: any;

    updateCurrentUploadView(uploadPreview: string, uploadUpdateStatus: string){
        if(this.uploadAborted === true) return;
        if(this.currentUploadGlobalViewer == null) return;
        this.currentUploadLoadingSpan.classList.remove('hidden-element');
        this.currentUploadGlobalViewer.classList.remove('hidden-element');
        const uploadViewerStatusDescriptionParagraph = document.getElementById('upload-viewer-status-description-paragraph');
        if (uploadViewerStatusDescriptionParagraph !== null) {
            uploadViewerStatusDescriptionParagraph.textContent = uploadPreview;
        }
        const uploadViewerStatusUpdateParagraph = document.getElementById('upload-viewer-status-update-paragraph');
        if (uploadViewerStatusUpdateParagraph !== null) {
            uploadViewerStatusUpdateParagraph.textContent = uploadUpdateStatus;
        }
    }

    closeCurrentZipJob(){
        this.endCurrentZipJob();
        if(this.currentZipJobGlobalViewer != null) this.currentZipJobGlobalViewer.classList.add('hidden-element');
    }

    endCurrentZipJob(){
        if(this.loadingZipJobSpan != null) this.loadingZipJobSpan.classList.add('hidden-element');
        clearInterval(this.interval);
    }

    closeCurrentUpload(){
        this.endCurrentUpload();
        if(this.currentUploadGlobalViewer != null) this.currentUploadGlobalViewer.classList.add('hidden-element');
        this.uploadAborted = true;
    }

    endCurrentUpload(){
        if(this.loadingUploadSpan != null) this.loadingUploadSpan.classList.add('hidden-element');
    }

    removeUploadByUuid(rowUuid: string): void {
        this.uploads = this.uploads.filter(upload => upload.rowUuid !== rowUuid);
    }

  async getStorageAttribute(name: string): Promise<any>{
    var returned = sessionStorage.getItem(name);
    return returned;
  }

  // Function to get a cookie by name
  async getCookie(name: string): Promise<string> {
      const cookies = document.cookie.split(';');
      for (let cookie of cookies) {
          cookie = cookie.replace('=', '~')
          const [cookieName, cookieValue] = cookie.split('~');
          if (cookieName.trim() === name) {
              return decodeURIComponent(cookieValue);
          }
      }
      return '';
  }

  setSessionCookie(cookieName: string, cookieValue: string) {
  const encodedCookie = encodeURIComponent(cookieValue);
  document.cookie = `${cookieName}=${encodedCookie}; SameSite=Lax; path=/`;
  }

  generateUUID(): string {
    const crypto = window.crypto || (window as any).msCrypto;
    if (crypto) {
      const buf = new Uint16Array(8);
      crypto.getRandomValues(buf);
      return (
        this.pad4(buf[0]) +
        this.pad4(buf[1]) +
        '-' +
        this.pad4(buf[2]) +
        '-' +
        this.pad4(buf[3]) +
        '-' +
        this.pad4(buf[4]) +
        '-' +
        this.pad4(buf[5]) +
        this.pad4(buf[6]) +
        this.pad4(buf[7])
      );
    } else {
      // Fallback to Math.random
      return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (
        c
      ) {
        var r = (Math.random() * 16) | 0,
          v = c == 'x' ? r : (r & 0x3) | 0x8;
        return v.toString(16);
      });
    }
  }

  pad4(num: number): string {
    let ret = num.toString(16);
    while (ret.length < 4) {
      ret = '0' + ret;
    }
    return ret;
  }


  //ASYNC RESPONSES FROM SERVER

  //evaluate can upload
    private _resolveEvaluateCanUpload!: (canUpload: boolean) => void;

    onEvaluateCanUploadResponse(canUpload: boolean) {
      this._resolveEvaluateCanUpload(canUpload);
    }

  //add upload row
  private _resolveAddUploadRow!: () => void;

  waitForAddUploadRow(): Promise<void> {
    return new Promise(resolve => {
      this._resolveAddUploadRow = resolve;
    });
  }

  onAddUploadRowAck() {
    if (this._resolveAddUploadRow) {
      this._resolveAddUploadRow();
      this._resolveAddUploadRow = undefined!;
    }
  }

  //create new directory
  private _resolveVerifyDirectory!: (r: { code: number; response: string }) => void;

  onVerifyDirectoryResponse(code: number, status: string) {
    this._resolveVerifyDirectory({
      code: code,
      response: status
    });
  }

  //create empty file
  private _resolveCreateEmpty!: (r: { code: number; response: string }) => void;

  onCreateEmptyResponse(code: number, status: string) {
    this._resolveCreateEmpty({
      code: code,
      response: status
    });
  }

  //create new directory
  private _resolveCreateDirectory!: (r: { code: number; response: string }) => void;

  onCreateDirectoryResponse(code: number, status: string) {
    this._resolveCreateDirectory({
      code: code,
      response: status
    });
  }

  //Success-fail upload row
  private _resolveFinishedUpload!: () => void;

  waitForFinishedUpload(): Promise<void> {
    return new Promise(resolve => {
      this._resolveFinishedUpload = resolve;
    });
  }

  onFinishedUploadAck() {
    if (this._resolveFinishedUpload) {
      this._resolveFinishedUpload();
      this._resolveFinishedUpload = undefined!;
    }
  }

  //interrupt directory upload
  private _resolveInterruptDirectoryUpload!: () => void;

  waitForInterruptDirectoryUpload(): Promise<void> {
    return new Promise(resolve => {
      this._resolveInterruptDirectoryUpload = resolve;
    });
  }

  onInterruptDirectoryUploadAck() {
    if (this._resolveInterruptDirectoryUpload) {
      this._resolveInterruptDirectoryUpload();
      this._resolveInterruptDirectoryUpload = undefined!;
    }
  }

  //interrupt directory upload
  private _resolveDirectoryUpload!: () => void;

  waitForDirectoryUpload(): Promise<void> {
    return new Promise(resolve => {
      this._resolveDirectoryUpload = resolve;
    });
  }

  onDirectoryUploadAck() {
    if (this._resolveDirectoryUpload) {
      this._resolveDirectoryUpload();
      this._resolveDirectoryUpload = undefined!;
    }
  }
}
