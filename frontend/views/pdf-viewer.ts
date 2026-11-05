import { html, LitElement } from 'lit';
import {customElement} from 'lit/decorators.js';
import * as pdfjsLib from 'pdfjs-dist';

@customElement('pdf-viewer-view')
export class PdfViewerView extends LitElement {

    declare $server: {
            setPageMaxAttribute: () => void;
            setCurrentPageNumber: () => void;
    };

    pdfDoc: any = null;
    currentPage: number = 1;


  createRenderRoot(){
    //Check here: https://mozilla.github.io/pdf.js/getting_started/
    //And here: https://cdnjs.com/libraries/pdf.js
//     pdfjsLib.GlobalWorkerOptions.workerSrc = 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/4.6.82/pdf.worker.mjs'
    pdfjsLib.GlobalWorkerOptions.workerSrc = "/io/get-pdf-worker";
    return this;
  }

  render() {
    return html`
    <div id="pdf-controls">
        <vaadin-button id="prev-page" class="action-button"></vaadin-button>
        <vaadin-button id="next-page"></vaadin-button>
        <vaadin-number-field id="page-input"></vaadin-number-field>
        <span id="page-number"></span>
        <vaadin-button id="go-to-page"></vaadin-button>
    </div>
    <div id="pdf-canvas-container"></div>
    `;
  }

  renderPDF(url: string) {
      // Check if the element is already in the DOM (optional for immediate execution)
      const targetElement = document.getElementById('pdf-canvas-container');
      if (targetElement) {
          // Your code here
          this.executeRenderPDF(url);
          return;
      }

      // Set up MutationObserver to detect when #my-id is added
      const observer = new MutationObserver(function (mutationsList, observer) {
          for (const mutation of mutationsList) {
              if (mutation.type === 'childList') {
                  const addedNodes = Array.from(mutation.addedNodes);
                  addedNodes.forEach(node => {
                      if (node instanceof HTMLElement && node.id === 'pdf-canvas-container') {
                          // Your code here

                          // Stop observing after the element is found
                          observer.disconnect();
                          const viewer: any = document.getElementById('pdf-viewer-container');
                          viewer.executeRenderPDF(url);
                      }
                  });
              }
          }
      });

      // Start observing the document's body for child element changes
      observer.observe(document.body, { childList: true, subtree: true });
  }

  executeRenderPDF(url: string) {
      this.setupScrollListener();
//         this.canvas = document.getElementById('pdf-canvas');
//         this.context = this.canvas.getContext('2d');
        const prevPage: any = document.getElementById('prev-page');
        prevPage.addEventListener('click-from-server', function () {
                var viewer: any = document.getElementById('pdf-viewer-container');
                if (viewer.currentPage > 1) {
                    viewer.goToPage(viewer.currentPage - 1);
                }
            });

            const nextPage: any = document.getElementById('next-page');
            nextPage.addEventListener('click-from-server', function () {
                var viewer: any = document.getElementById('pdf-viewer-container');
                if (viewer.currentPage < viewer.pdfDoc.numPages) {
                    viewer.goToPage(viewer.currentPage + 1);
                }
            });
            const goToPage: any = document.getElementById('go-to-page');
            goToPage.addEventListener('click-from-server', function () {
                var viewer: any = document.getElementById('pdf-viewer-container');
                var pageInput: any =  document.getElementById('page-input');
                const pageNumber = parseInt(pageInput.value, 10);
                viewer.goToPage(pageNumber);
            });
              // Ensure pdfjsLib is available
              if (typeof pdfjsLib === 'undefined') {
                  return;
              }


              pdfjsLib.getDocument({ url: url }).promise.then(function (pdf) {
                  const viewer: any = document.getElementById('pdf-viewer-container');
                  viewer.pdfDoc = pdf;
                  sessionStorage.setItem('pdf-total-page-number', '' + pdf.numPages);
                  viewer.$server.setPageMaxAttribute();
                    for (let pageNumber = 1; pageNumber <= pdf.numPages; pageNumber++) {
                    pdf.getPage(pageNumber).then(function (page: any) {
                    // Create a new canvas for each page
                    var canvas: any = document.createElement('canvas');
                    canvas.id = `pdf-canvas-${pageNumber}`;
                    canvas.classList.add('preview-pdf-canvas');
                    const container: any = document.getElementById('pdf-canvas-container');
                    container.appendChild(canvas);

                    // Set up scale and viewport
                    var scale = 1.5;
                    var viewport = page.getViewport({ scale: scale });

                    // Set canvas dimensions to match the PDF page
                    var context = canvas.getContext('2d');
                    canvas.height = viewport.height;
                    canvas.width = viewport.width;

                    // Render PDF page into canvas context
                    var renderContext = {
                        canvasContext: context,
                        viewport: viewport
                    };

                    // Render the page
                    page.render(renderContext).promise.then(() => {
                    });
                });
            }
              }).catch(function (error) {
                  console.error('Error loading PDF');
              });
          }

       scrollToElementInContainer(targetId: string) {
          const container: any = document.getElementById('pdf-canvas-container');
          const targetElement: any = document.getElementById(targetId);

          if (container && targetElement) {
              container.scrollTo({
                  top: targetElement.offsetTop - container.offsetTop//,
//                   behavior: 'smooth'
              });
          }
      }

  setupScrollListener() {
      const container = document.getElementById('pdf-canvas-container');

      if (container) {
          container.addEventListener('scroll', function () {
              const viewer: any = document.getElementById('pdf-viewer-container');
              viewer.observePDFPages();
          });
      }
  }


  observePDFPages() {
      const pdfPages = document.querySelectorAll('.preview-pdf-canvas');  // Assuming each page has a class 'pdf-page'

      // Create the Intersection Observer
      const observer = new IntersectionObserver((entries) => {
          entries.forEach((entry: any) => {
              if (entry.isIntersecting) {
                  const currentPage: number = entry.target.id.split('-').pop() - 0;
                  const viewer: any = document.getElementById('pdf-viewer-container');
                  if(currentPage != viewer.currentPage){
                      sessionStorage.setItem('pdf-current-page-number', '' + currentPage);
                      viewer.currentPage = currentPage;
                      viewer.$server.setCurrentPageNumber();
                  }
              }
          });
      }, {
          root: null,
          threshold: 0.1  // Trigger when 10% of the page is visible
      });

      // Observe each PDF page
      pdfPages.forEach(page => {
          observer.observe(page);
      });
  }

      goToPage(pageNumber: number) {
          if (pageNumber >= 1 && pageNumber <= this.pdfDoc.numPages) {
              this.currentPage = pageNumber;
              this.scrollToElementInContainer(`pdf-canvas-${pageNumber}`)
              sessionStorage.setItem('pdf-current-page-number', '' + pageNumber);
              this.$server.setCurrentPageNumber();
              this.dispatchEvent(new CustomEvent('page-number-update-event'));
          }
      }
}
