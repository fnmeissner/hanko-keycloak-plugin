import * as React from 'react'
import { fetchApi } from '../utils/fetchApi'
import { FormattedMessage } from 'react-intl'

type ChangePasswordComponentProps = {
  keycloak: Keycloak.KeycloakInstance
}

type ChangePasswordComponentState = {
  password: string
  passwordValidation: string
}

export class ChangePasswordComponent extends React.Component<
  ChangePasswordComponentProps,
  ChangePasswordComponentState
> {
  constructor(props: ChangePasswordComponentProps) {
    super(props)
    this.state = {
      password: '',
      passwordValidation: ''
    }
  }

  passwordChanged = (event: React.ChangeEvent<HTMLInputElement>) => {
    this.setState({ password: event.target.value })
  }

  passwordValidationChanged = (event: React.ChangeEvent<HTMLInputElement>) => {
    this.setState({ passwordValidation: event.target.value })
  }

  changePassword = () => {
    const { keycloak } = this.props
    const { password, passwordValidation } = this.state

    if (!(password && password.length > 0)) {
      alert('password must not be empty')
    } else if (
      !(
        passwordValidation &&
        passwordValidation.length > 0 &&
        password === passwordValidation
      )
    ) {
      alert('passwords do not match')
      return
    } else {
      fetchApi(keycloak, '/hanko/password', 'POST', {
        newPassword: password
      })
        .then(_ => {
          this.setState({ password: '', passwordValidation: '' })
          alert('Password successfully changed')
        })
        .catch(reason => {
          console.error(reason)
          alert('Oops, your password could not be updated.')
        })
    }
  }

  render() {
    const { password, passwordValidation } = this.state
    return (
      <div className="container">
        <FormattedMessage
          id="ChangePassword.changePasswordHeader"
          defaultMessage="Change Password"
        >
          {content => <h1>{content}</h1>}
        </FormattedMessage>

        <div className="formfield">
          <FormattedMessage
            id="ChangePassword.passwordLabel"
            defaultMessage="Password"
          >
            {content => <label>{content}</label>}
          </FormattedMessage>
          <input
            type="password"
            value={password}
            onChange={this.passwordChanged}
          />
        </div>
        <div className="formfield">
          <FormattedMessage
            id="ChangePassword.repeatPasswordLabel"
            defaultMessage="Password (repeat)"
          >
            {content => <label>{content}</label>}
          </FormattedMessage>
          <input
            type="password"
            value={passwordValidation}
            onChange={this.passwordValidationChanged}
          />
        </div>
        <div className="pull-right">
          <FormattedMessage
            id="ChangePassword.changeButton"
            defaultMessage="Change"
          >
            {content => (
              <button onClick={this.changePassword}>{content}</button>
            )}
          </FormattedMessage>
        </div>
      </div>
    )
  }
}
