export const fetchApi = (
  keycloak: Keycloak.KeycloakInstance,
  path: string,
  method: string | undefined = undefined,
  body: any | undefined = undefined
) => {
  return new Promise<any>((success, fail) => {
    keycloak.updateToken(30).success(_ => {
      const url = `${process.env.KEYCLOAK_URL}/auth/realms/${
        process.env.KEYCLOAK_REALM
      }${path}`

      const headers = (body
        ? {
            authorization: `Bearer ${keycloak.token}`,
            'Content-Type': 'application/json'
          }
        : {
            authorization: `Bearer ${keycloak.token}`
          }) as any

      const options = {
        method: method ? method : 'GET',
        headers: headers,
        body: body ? JSON.stringify(body) : null
      }

      fetch(url, options)
        .then(response => {
          return response.json()
        })
        .then(json => success(json))
        .catch(_ => success(undefined))
    })
  })
}
