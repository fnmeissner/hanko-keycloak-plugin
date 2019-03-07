import * as React from 'react'
import { fetchApi } from '../utils/fetchApi'
import { Device } from '../models/Device'
import * as moment from 'moment'
import { PopupDialog } from './PopupDialog'

type DeviceProps = {
  device: Device
  keycloak: Keycloak.KeycloakInstance
  deviceDeletedHandler: () => void
  confirmDeregistration: boolean
}

type DeviceState = {
  showConfirmationDialog: boolean
}

export class DeviceComponent extends React.Component<DeviceProps, DeviceState> {
  constructor(props: DeviceProps) {
    super(props)
    this.state = { showConfirmationDialog: false }
  }

  showConformationDialog = () => {
    this.setState({ showConfirmationDialog: true })
  }

  hideConfirmationDialog = () => {
    this.setState({ showConfirmationDialog: false })
  }

  confirmOrDeregister = () => {
    if (this.props.confirmDeregistration) {
      this.showConformationDialog()
    } else {
      this.deregister()
    }
  }

  deregister = () => {
    const { device, keycloak, deviceDeletedHandler } = this.props

    this.hideConfirmationDialog()

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
    const { showConfirmationDialog } = this.state

    return (
      <tr>
        <td>{device.name}</td>
        <td>{device.type}</td>
        <td>{moment(device.createdAt).fromNow()}</td>
        <td>{moment(device.lastUsage).fromNow()}</td>
        <td>
          <a href="#" onClick={this.confirmOrDeregister}>
            delete
          </a>
          {showConfirmationDialog ? (
            <PopupDialog>
              <h3>Warning</h3>
              <p>
                You are about to delete your last device. You will not be able
                to login unless you register another device.
              </p>
              <div className="button-list pull-right">
                <button className="small" onClick={this.hideConfirmationDialog}>
                  cancel
                </button>
                <button className="small" onClick={this.deregister}>
                  confirm
                </button>
              </div>
            </PopupDialog>
          ) : null}
        </td>
      </tr>
    )
  }
}
