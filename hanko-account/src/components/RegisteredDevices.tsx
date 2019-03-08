import * as React from 'react'
import { Device } from '../models/Device'
import { DeviceComponent } from './DeviceComponent'
import { FormattedMessage } from 'react-intl'

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
    const confirmDeregistration = window.requires2fa
      ? window.requires2fa === 'true' && devices.length === 1
      : false
    // const { keycloak, deviceDeletedHandler } = this.props
    // const devices: Device[] = []

    if (devices.length === 0)
      return window.requires2fa === 'true' ? (
        <div className="warning">
          <FormattedMessage
            id="RegisteredDevices.noDeviceWarningHeader"
            defaultMessage="Please register a 2nd factor now!"
          >
            {content => <h3>{content}</h3>}
          </FormattedMessage>
          <FormattedMessage
            id="RegisteredDevices.noDeviceWarningMessage"
            defaultMessage="You don't have any 2nd factor device configured. If you logout now, you cannot login anymore."
          >
            {content => <p>{content}</p>}
          </FormattedMessage>
        </div>
      ) : (
        <FormattedMessage
          id="RegisteredDevices.noDeviceMessage"
          defaultMessage="No devices registered."
        >
          {content => <div>{content}</div>}
        </FormattedMessage>
      )

    return (
      <table>
        <thead>
          <tr>
            <FormattedMessage
              id="RegisteredDevices.deviceNameLabel"
              defaultMessage="Name"
            >
              {content => <th>{content}</th>}
            </FormattedMessage>
            <FormattedMessage
              id="RegisteredDevices.deviceNameType"
              defaultMessage="Type"
            >
              {content => <th>{content}</th>}
            </FormattedMessage>
            <FormattedMessage
              id="RegisteredDevices.deviceNameCreatedAt"
              defaultMessage="Created At"
            >
              {content => <th>{content}</th>}
            </FormattedMessage>
            <FormattedMessage
              id="RegisteredDevices.deviceNameLastActivity"
              defaultMessage="Last Activity"
            >
              {content => <th>{content}</th>}
            </FormattedMessage>
            <FormattedMessage
              id="RegisteredDevices.deviceNameManage"
              defaultMessage="Manage"
            >
              {content => <th>{content}</th>}
            </FormattedMessage>
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
