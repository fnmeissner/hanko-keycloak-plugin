interface Window {
  PublicKeyCredential: PublicKeyCredential | undefined
  __webpack_public_path__: string | undefined
  resourceBaseUrl: string | undefined
}

interface PublicKeyCredential {
  isUserVerifyingPlatformAuthenticatorAvailable: () => Promise<boolean>
}

interface Navigator {
  userLanguage: string | undefined
  language: string | undefined
}
