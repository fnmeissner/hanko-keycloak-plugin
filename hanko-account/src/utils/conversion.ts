export const convertToBinary = (dataURI: string) => {
  var raw = window.atob(dataURI)
  var rawLength = raw.length
  var array = new Uint8Array(new ArrayBuffer(rawLength))

  for (let i = 0; i < rawLength; i++) {
    array[i] = raw.charCodeAt(i)
  }
  return array
}

export const arrayBufferToBase64 = (buf: ArrayBuffer) => {
  var binary = ''
  var bytes = new Uint8Array(buf)
  var len = bytes.byteLength
  for (var i = 0; i < len; i++) {
    binary += String.fromCharCode(bytes[i])
  }
  return window
    .btoa(binary)
    .replace(/\//g, '_')
    .replace(/\+/g, '-')
}
