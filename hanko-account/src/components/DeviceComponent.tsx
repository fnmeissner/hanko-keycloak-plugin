import * as React from 'react'
import { fetchApi } from '../utils/fetchApi'
import { Device } from '../models/Device'
import * as moment from 'moment'

type DeviceProps = {
  device: Device
  keycloak: Keycloak.KeycloakInstance
  deviceDeletedHandler: () => void
}

export class DeviceComponent extends React.Component<DeviceProps> {
  constructor(props: DeviceProps) {
    super(props)
  }

  deregister = () => {
    const { device, keycloak, deviceDeletedHandler } = this.props
    fetchApi(
      keycloak,
      `/hanko/devices/${device.typeId}/${device.deviceId}`,
      'DELETE'
    ).then(_ => {
      deviceDeletedHandler()
    })

    return false
  }

  render() {
    const { device } = this.props

    return (
      <tr>
        <td>{device.name}</td>
        <td>{device.type}</td>
        <td>{moment(device.createdAt).fromNow()}</td>
        <td>{moment(device.lastUsage).fromNow()}</td>
        <td>
          <a href="#" onClick={this.deregister}>
            deregister
          </a>
        </td>
      </tr>
    )
  }
}
