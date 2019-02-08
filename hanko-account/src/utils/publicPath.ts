console.log('baseUrl: ', window.resourceBaseUrl)

declare var __webpack_public_path__: string | undefined

if (window.resourceBaseUrl) {
  console.log('setting baseUrl to:', window.resourceBaseUrl)
  window.__webpack_public_path__ = window.resourceBaseUrl
  __webpack_public_path__ = window.resourceBaseUrl
}
