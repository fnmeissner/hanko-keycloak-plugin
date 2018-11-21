import * as React from 'react'
import { RegisteredDevices } from '../components/RegisteredDevices'
import { AddHankoAuthenticator } from '../components/AddHankoAuthenticator'
import { fetchApi } from '../utils/fetchApi'
import { deviceFromJson, Device } from '../models/Device'
import { ChangePasswordComponent } from '../components/ChangePasswordComponent'
import glamorous from 'glamorous'

type AppState = {
  showAddHankoAuthenticator: boolean
  devices: Device[] | undefined
}

type AppProps = {
  keycloak: Keycloak.KeycloakInstance
}

const ContentWrapper = glamorous.div({
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  flex: 1
})

export class App extends React.Component<AppProps, AppState> {
  constructor(props: AppProps) {
    super(props)
    this.state = {
      showAddHankoAuthenticator: false,
      devices: undefined
    }
  }

  componentDidMount() {
    this.fetchDevices()
  }

  showAddHankoAuthenticator = () => {
    this.setState({ showAddHankoAuthenticator: true })
  }

  hideAddHankoAuthenticator = () => {
    this.setState({ showAddHankoAuthenticator: false })
  }

  completionHandler = () => {
    this.hideAddHankoAuthenticator()
    this.setState({ devices: undefined })
    this.fetchDevices()
  }

  fetchDevices = () => {
    const { keycloak } = this.props

    fetchApi(keycloak, '/hanko/devices').then(jsonArray => {
      const devices = (jsonArray as any[]).map(json => deviceFromJson(json))
      this.setState({ devices: devices })
    })
  }

  render() {
    const { keycloak } = this.props
    const { showAddHankoAuthenticator, devices } = this.state

    const urlParams = new URLSearchParams(window.location.search)
    const redirectParam = urlParams.get('redirectUrl')
    const redirectNameParam = urlParams.get('redirectName')
    const redirectLinkText = redirectNameParam ? redirectNameParam : 'return'

    return (
      <ContentWrapper>
        <div className="navigation-bar">
          {redirectParam ? (
            <a className="navigation-bar-link" href={redirectParam}>
              {redirectLinkText}
            </a>
          ) : null}
        </div>
        <div id="content">
          <div className="center column">
            <div className="container">
              <h1>Registered Devices</h1>
              {devices === undefined ? (
                <div>Loading...</div>
              ) : (
                <RegisteredDevices
                  keycloak={keycloak}
                  devices={devices}
                  deviceDeletedHandler={this.fetchDevices}
                />
              )}

              <div className="margin-bottom" />

              {showAddHankoAuthenticator ? (
                <AddHankoAuthenticator
                  keycloak={keycloak}
                  cancelHandler={this.hideAddHankoAuthenticator}
                  completionHandler={this.completionHandler}
                />
              ) : (
                <div>
                  <button onClick={this.showAddHankoAuthenticator}>
                    Add Authenticator
                  </button>
                </div>
              )}
            </div>
            <ChangePasswordComponent keycloak={keycloak} />
          </div>
        </div>
      </ContentWrapper>
    )
  }
}
