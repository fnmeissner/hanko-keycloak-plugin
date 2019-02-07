interface Window {
  PublicKeyCredential: PublicKeyCredential | undefined
}

interface PublicKeyCredential {
  isUserVerifyingPlatformAuthenticatorAvailable: () => Promise<boolean>
}

interface Navigator {
  userLanguage: string | undefined
  language: string | undefined
}
