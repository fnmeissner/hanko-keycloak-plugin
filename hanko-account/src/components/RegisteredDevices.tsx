import * as React from 'react'
import { Device } from '../models/Device'
import { DeviceComponent } from './DeviceComponent'

type RegisteredDevicesProps = {
  keycloak: Keycloak.KeycloakInstance
  devices: Device[]
  deviceDeletedHandler: () => void
}

export class RegisteredDevices extends React.Component<RegisteredDevicesProps> {
  constructor(props: RegisteredDevicesProps) {
    super(props)

    this.state = { devices: undefined }
  }

  render() {
    const { devices, keycloak, deviceDeletedHandler } = this.props
    const confirmDeregistration = devices.length === 1
    // const { keycloak, deviceDeletedHandler } = this.props
    // const devices: Device[] = []

    if (devices.length === 0)
      return (
        <div className="warning">
          <h3>Please register a 2nd factor now!</h3>
          <p>
            You don't have any 2nd factor device configured. If you logout now,
            you cannot login anymore.
          </p>
        </div>
      )

    return (
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Created At</th>
            <th>Last Activity</th>
            <th>Manage</th>
          </tr>
        </thead>
        <tbody>
          {devices.map((device, index) => {
            return (
              <DeviceComponent
                device={device}
                key={index}
                keycloak={keycloak}
                deviceDeletedHandler={deviceDeletedHandler}
                confirmDeregistration={confirmDeregistration}
              />
            )
          })}
        </tbody>
      </table>
    )
  }
}
