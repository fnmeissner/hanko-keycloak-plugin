import * as React from 'react'
import { fetchApi } from '../utils/fetchApi'
import { convertToBinary, arrayBufferToBase64 } from '../utils/conversion'

type AddWebAuthnProps = {
  children: React.ReactNode
  keycloak: Keycloak.KeycloakInstance
  refetch: () => void
  type: 'roaming' | 'platform'
}

type AddWebAuthnState = {}

export class AddWebAuthn extends React.Component<
  AddWebAuthnProps,
  AddWebAuthnState
> {
  constructor(props: AddWebAuthnProps) {
    super(props)
    this.state = {}
  }

  addThisDevice = () => {
    const { keycloak, refetch, type } = this.props

    const authenticatorSelection =
      type == 'roaming'
        ? {
            requireResidentKey: false,
            userVerification: 'preferred',
            authenticatorAttachment: 'cross-platform'
          }
        : {
            requireResidentKey: true,
            userVerification: 'required',
            authenticatorAttachment: 'platform'
          }

    // fetch request
    fetchApi(keycloak, '/hanko/registerType/WEB_AUTHN', 'POST').then(
      registrationRequest => {
        console.log(registrationRequest)
        const fidoRequest = JSON.parse(registrationRequest.request)
        console.log(fidoRequest)
        const challenge = convertToBinary(fidoRequest.challenge)

        const pubKey = {
          pubKeyCredParams: [
            {
              alg: -7,
              type: 'public-key'
            },
            {
              alg: -257,
              type: 'public-key'
            }
          ],
          rp: {
            name: fidoRequest.rpName
          },
          user: {
            id: challenge,
            name: fidoRequest.displayName,
            displayName: fidoRequest.displayName
          },
          authenticatorSelection: authenticatorSelection,
          timeout: 50000,
          challenge: challenge,
          excludeCredentials: [],
          attestation: 'none'
        }

        const s = navigator as any
        console.log(pubKey)
        s.credentials
          .create({ publicKey: pubKey })
          .then((result: any) => {
            console.log('Creating credential yielded following result:')
            console.log(result)

            const attestationString = arrayBufferToBase64(
              result.response.attestationObject
            )

            const clientDataString = arrayBufferToBase64(
              result.response.clientDataJSON
            )

            var response = {
              credID: result.id.replace(/\//g, '_').replace(/\+/g, '-'),
              publicKey: attestationString,
              challenge: fidoRequest.challenge,
              clientData: clientDataString
            }

            console.log('response')
            console.log(response)

            fetchApi(
              keycloak,
              '/hanko/request/verify/webauthn',
              'POST',
              response
            ).then(result => {
              console.log(result)
              refetch()
            })
          })
          .catch((reason: any) => {
            console.log(reason)
          })
      }
    )
  }

  render() {
    const { children } = this.props

    return <button onClick={this.addThisDevice}>{children}</button>
  }
}
