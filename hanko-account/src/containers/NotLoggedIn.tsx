import * as React from 'react'

type NotLoggedInProps = {
  keycloak: Keycloak.KeycloakInstance
}

export const NotLoggedIn = ({ keycloak }: NotLoggedInProps) => {
  const login = () => {
    keycloak.login()
  }

  return (
    <div>
      <h1>Login failed</h1>
      <button onClick={login}>Try again</button>
    </div>
  )
}
