import * as React from 'react'
import * as ReactDOM from 'react-dom'
import { App } from './containers/App'

import * as Keycloak from 'keycloak-js'

import { NotLoggedIn } from './containers/NotLoggedIn'

require('./styles/main.scss')

const keycloakUrl =
  (window as any).keycloakUrl !== undefined
    ? (window as any).keycloakUrl
    : `${process.env.KEYCLOAK_URL}/auth`

const realm =
  (window as any).realmId !== undefined
    ? (window as any).realmId
    : `${process.env.KEYCLOAK_REALM}`

console.log('(window as any).realmId = ' + (window as any).realmId)
console.log('(window as any).keycloakUrl = ' + (window as any).keycloakUrl)

console.log('${process.env.KEYCLOAK_URL} = ' + process.env.KEYCLOAK_URL)
console.log('${process.env.KEYCLOAK_REALM} = ' + process.env.KEYCLOAK_REALM)

const keycloak = Keycloak({
  url: keycloakUrl,
  realm: realm,
  clientId: 'hanko-account'
})

keycloak
  .init({ onLoad: 'login-required' })
  .success(_ => {
    ReactDOM.render(<App keycloak={keycloak} />, document.getElementById(
      'root'
    ) as HTMLElement)
  })
  .error(() => {
    ReactDOM.render(
      <NotLoggedIn keycloak={keycloak} />,
      document.getElementById('root') as HTMLElement
    )
  })
