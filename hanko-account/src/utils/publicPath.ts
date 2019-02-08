console.log('baseUrl: ', window.resourceBaseUrl)

if (window.resourceBaseUrl) {
  console.log('setting baseUrl to:', window.resourceBaseUrl)
  window.__webpack_public_path__ = window.resourceBaseUrl
}
