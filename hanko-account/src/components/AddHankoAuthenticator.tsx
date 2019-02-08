import * as React from 'react'
import { fetchApi } from '../utils/fetchApi'

type AddHankoAuthenticatorProps = {
  cancelHandler: () => void
  completionHandler: () => void
  keycloak: Keycloak.KeycloakInstance
}

type AddHankoAuthenticatorState = {
  qrCode: string | undefined
}

export class AddHankoAuthenticator extends React.Component<
  AddHankoAuthenticatorProps,
  AddHankoAuthenticatorState
> {
  constructor(props: AddHankoAuthenticatorProps) {
    super(props)
    this.state = { qrCode: undefined }
  }

  private timerId?: number

  componentDidMount() {
    this.requestQrCode()
  }

  componentWillUnmount() {
    clearTimeout(this.timerId)
  }

  requestQrCode = () => {
    const { keycloak } = this.props

    fetchApi(keycloak, '/hanko/register', 'POST').then(registrationRequest => {
      this.setState({ qrCode: registrationRequest.qrCode })
      this.updateState()
    })
  }

  cancel = () => {
    const { cancelHandler } = this.props
    cancelHandler()
  }

  updateState = () => {
    const { keycloak } = this.props

    fetchApi(keycloak, '/hanko/register/complete', 'POST').then(request => {
      switch (request.status) {
        case 'PENDING': {
          this.timerId = window.setTimeout(this.updateState, 1000)
          break
        }
        case 'OK': {
          this.setState({ qrCode: undefined })
          const { completionHandler } = this.props
          completionHandler()
          break
        }
        default: {
          this.setState({ qrCode: undefined })
          this.cancel()
          break
        }
      }
    })
  }

  render() {
    const { qrCode } = this.state

    return (
      <div className="flex column align-start">
        <h1>Register new Hanko Authenticator</h1>
        {qrCode ? (
          <div className="margin-bottom">
            <img src={`${qrCode}?fg=0&bg=ffffff`} className="fadein" />
          </div>
        ) : null}
        <div className="pull-right">
          <button onClick={this.cancel}>Cancel</button>
        </div>
      </div>
    )
  }
}
