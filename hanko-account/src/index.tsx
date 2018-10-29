import * as React from 'react'
import * as ReactDOM from 'react-dom'
import { App } from './containers/App'

import * as Keycloak from 'keycloak-js'

import { NotLoggedIn } from './containers/NotLoggedIn'

require('./styles/main.scss')

const keycloak = Keycloak({
  url: `${process.env.KEYCLOAK_URL}/auth`,
  realm: `${process.env.KEYCLOAK_REALM}`,
  clientId: 'hanko-account'
})

keycloak
  .init({ onLoad: 'login-required' })
  .success(isLoggedId => {
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
