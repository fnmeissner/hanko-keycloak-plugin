import * as React from 'react'
import { RegisteredDevices } from '../components/RegisteredDevices'
import { AddHankoAuthenticator } from '../components/AddHankoAuthenticator'
import { fetchApi } from '../utils/fetchApi'
import { deviceFromJson, Device } from '../models/Device'
import { ChangePasswordComponent } from '../components/ChangePasswordComponent'

type AppState = {
  showAddHankoAuthenticator: boolean
  devices: Device[] | undefined
}

type AppProps = {
  keycloak: Keycloak.KeycloakInstance
}

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

    return (
      <div>
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
      </div>
    )
  }
}
