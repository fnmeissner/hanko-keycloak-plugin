export type Device = {
  deviceId: string
  name: string
  type: string
  createdAt: string
  lastUsage: string
}

export const deviceFromJson = (device: any) => {
  return {
    name: device.keyName,
    type:
      device.authenticatorType == 'FIDO_UAF'
        ? 'Hanko Authenticator'
        : device.authenticatorType,
    createdAt: device.createdAt,
    lastUsage: device.lastUsage,
    deviceId: device.deviceId
  }
}
