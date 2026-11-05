import { html, LitElement } from 'lit';
import {customElement} from 'lit/decorators.js';
import * as pdfjsLib from 'pdfjs-dist';
import pdfjsWorker from 'pdfjs-dist';
// import { showNotification } from '@vaadin/flow-frontend/a-notification';

@customElement('folder-view')
export class FolderView extends LitElement {

  declare $server: {
          handleTitleNavigation: (title: string) => void;
          refreshFiles: () => void;
          handleSelectUploadOnGrid: (filename: string) => void;
  };

  ioOperationsLayerElement: any = document.getElementById('cloud-io-operations-layer');

  createRenderRoot() {
    return this;
  }

  render() {
    return html`
    <vaadin-grid
      id="main-files-grid" draggable="true"
      @dragover=${this.handleDragOver}
      @drop=${this.handleDrop}
    >
    </vaadin-grid>
    <vaadin-grid
      id="main-files-mobile-grid" draggable="true"
      @dragover=${this.handleDragOver}
      @drop=${this.handleDrop}
    >
    </vaadin-grid>
    <input type="file" id="main-upload-files-input" multiple @change=${this.inputFileChange}>
    <!--input type="file" id="folderInput" webkitdirectory directory multiple-->
    <!--vaadin-vertical-layout id="uploads-modal-layout">
        <vaadin-horizontal-layout id="uploads-modal-header-layout"></vaadin-horizontal-layout>
        <vaadin-vertical-layout id="uploads-container"></vaadin-vertical-layout>
    </vaadin-vertical-layout-->`;
  }

  handleDragOver(event:Event){
      event.preventDefault();
      event.stopPropagation();
  }

  handleDrop(event:any){
    event.preventDefault();
    event.stopPropagation();
    this.ioOperationsLayerElement.handleFiles(event.dataTransfer.items);
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
        this.ioOperationsLayerElement.handleFiles(entries);
  }

  async handleTitleNavigationClick(navigateTo: string){
//     await this.ioOperationsLayerElement.setSessionCookie('navigate-to', navigateTo);
//     this.dispatchEvent(new CustomEvent('title-directory-click'));
    this.$server.handleTitleNavigation(navigateTo);
  }
}
