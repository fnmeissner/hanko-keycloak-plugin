import * as React from 'react'
import { FormattedMessage } from 'react-intl'

type NotLoggedInProps = {
  keycloak: Keycloak.KeycloakInstance
}

export const NotLoggedIn = ({ keycloak }: NotLoggedInProps) => {
  const login = () => {
    keycloak.login()
  }

  return (
    <div>
      <FormattedMessage
        id="NotLoggedIn.loginFailedHeader"
        defaultMessage="Login failed"
      >
        {content => <h1>{content}</h1>}
      </FormattedMessage>

      <FormattedMessage
        id="NotLoggedIn.tryAgainButton"
        defaultMessage="Try again"
      >
        {content => <button onClick={login}>{content}</button>}
      </FormattedMessage>
    </div>
  )
}
