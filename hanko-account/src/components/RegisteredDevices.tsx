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

    if (devices.length === 0) return <div>no devices registered</div>

    return (
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Created At</th>
            <th>Last Activity</th>
            <th />
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
              />
            )
          })}
        </tbody>
      </table>
    )
  }
}
