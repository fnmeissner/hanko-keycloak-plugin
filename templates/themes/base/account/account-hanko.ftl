<!doctype html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="robots" content="noindex, nofollow">

    <title>Hanko Authentication Settings</title>
    <link rel="icon" href="${url.resourcesPath}/img/favicon.ico">
    <script src="/auth/js/keycloak.js"></script>
    <#if properties.styles?has_content>
        <#list properties.styles?split(' ') as style>
            <link href="${url.resourcesPath}/${style}" rel="stylesheet"/>
        </#list>
    </#if>
    <#if properties.scripts?has_content>
        <#list properties.scripts?split(' ') as script>
            <script type="text/javascript" src="${url.resourcesPath}/${script}"></script>
        </#list>
    </#if>
</head>
<body class="admin-console user">

<header class="navbar navbar-default navbar-pf navbar-main header">
    <nav class="navbar" role="navigation">
        <div class="navbar-header">
            <div class="container">
                <h1 class="navbar-title">Keycloak</h1>
            </div>
        </div>
        <div class="navbar-collapse navbar-collapse-1">
            <div class="container">
                <ul class="nav navbar-nav navbar-utility">
                        <#if realm.internationalizationEnabled>
                            <li>
                                <div class="kc-dropdown" id="kc-locale-dropdown">
                                    <a href="#" id="kc-current-locale-link">${locale.current}</a>
                                    <ul>
                                        <#list locale.supported as l>
                                            <li class="kc-dropdown-item"><a href="${l.url}">${l.label}</a></li>
                                        </#list>
                                    </ul>
                                </div>
                            <li>
                        </#if>
                        <#if referrer?has_content && referrer.url?has_content>
                            <li><a href="${referrer.url}" id="referrer">Back to ${referrer.name}</a></li></#if>
                    <li><a href="${url.logoutUrl}">Sign Out</a></li>
                </ul>
            </div>
        </div>
    </nav>
</header>

<div class="container">
    <div class="col-sm-12 content-area">
            <#if message?has_content>
                <div class="alert alert-${message.type}">
                    <#if message.type=='success' ><span class="pficon pficon-ok"></span></#if>
                    <#if message.type=='error' ><span class="pficon pficon-error-octagon"></span><span
                            class="pficon pficon-error-exclamation"></span></#if>
                    ${message.summary?no_esc}
                </div>
            </#if>
        <h2>Hanko Authenticator</h2>
        <hr/>
        <div id="register-hanko" style="display:none">
            <a href='https://play.google.com/store/apps/details?id=io.hanko.authenticator&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'>
                <img width="150em" alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png'/>
            </a>
            <ol>
                <li>Install Hanko Authenticator on your mobile</li>
                <li>Request a QrCode</li>
                <li>Scan the QrCode with Hanko Authenticator</li>
                <li>Confirm your registration</li>
            </ol>
            <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                   type="button" value="Request QrCode" id="enableButton"/>
            <div id="qrcodeDiv">
            </div>
        </div>
        <div id="deregister-hanko" style="display:none">
            <ol>
                <li>Click on deregister to deaktivate Hanko Authentication for your Account.</li>
            </ol>
            <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                   type="button" value="Deregister" id="disableButton"/>
        </div>
    </div>
</div>
<script>
    window.onload = function () {
        const registerHankoDiv = document.getElementById('register-hanko');
        const deregisterHankoDiv = document.getElementById('deregister-hanko');
        const enableButton = document.getElementById('enableButton');
        const disableButton = document.getElementById('disableButton');
        const qrcode = document.getElementById('qrcodeDiv');

        const keycloakConfig = {
            "url": '${keycloakUrl}',
            "realm": '${keycloakRealm}',
            "clientId": '${keycloakClientId}',
            "public-client": true
        };

        const keycloak = Keycloak(keycloakConfig);

        const runAfterRenewal = (func) => {
            keycloak.updateToken(30).then(function () {
                func();
            }).catch(function () {
                alert('Failed to refresh token');
            });
        };


        const updateIsHankoEnabled = () => {
            runAfterRenewal(function () {
                fetch(keycloak.authServerUrl + 'realms/${keycloakRealm}/hanko',
                        {
                            method: "GET",
                            headers: {
                                "Accept": "application/json",
                                "authorization": "Bearer " + keycloak.token,
                            }
                        }).then(response => response.json())
                        .catch(error => console.error('Error:', error))
                        .then(res => {
                            console.log('response: ', res);
                            if (res.isPasswordlessActive) {
                                registerHankoDiv.style.display = 'none';
                                deregisterHankoDiv.style.display = 'block';
                            } else {
                                registerHankoDiv.style.display = 'block';
                                deregisterHankoDiv.style.display = 'none';
                            }
                        });
            });
        };


        const requestRegistration = () => {
            runAfterRenewal(function () {

                fetch(keycloak.authServerUrl + 'realms/${keycloakRealm}/hanko/register',
                        {
                            method: "POST",
                            headers: {
                                "Accept": "application/json",
                                "authorization": "Bearer " + keycloak.token,
                            }
                        }).then(response => response.json())
                        .catch(error => console.error('Error:', error))
                        .then(res => {
                            console.log('response: ', res);
                            qrcode.innerHTML = "<img src=\"" + res.qrCode + "\" />";
                            awaitRegistrationComplete();
                        });
            });
        };

        const awaitRegistrationComplete = () => {
            runAfterRenewal(function () {
                fetch(keycloak.authServerUrl + 'realms/${keycloakRealm}/hanko/register/complete',
                        {
                            method: "POST",
                            headers: {
                                "Accept": "application/json",
                                "authorization": "Bearer " + keycloak.token,
                            }
                        }).then(response => response.json())
                        .catch(error => console.error('Error:', error))
                        .then(res => {
                            console.log('response: ', res);
                            qrcode.innerHTML = "";
                            updateIsHankoEnabled();
                        });
            });
        };

        const disableHanko = () => {
            runAfterRenewal(function () {
                fetch(keycloak.authServerUrl + 'realms/${keycloakRealm}/hanko/deregister',
                        {
                            method: "POST",
                            headers: {
                                "Accept": "application/json",
                                "authorization": "Bearer " + keycloak.token,
                            }
                        }).then(response => response.json())
                        .catch(error => console.error('Error:', error))
                        .then(res => {
                            console.log('response: ', res);
                            qrcode.innerHTML = "";
                            updateIsHankoEnabled();
                        });
            });
        };

        keycloak.init({onLoad: 'login-required'}).then((authenticated) => {

            if (authenticated) {
                updateIsHankoEnabled();
                enableButton.onclick = requestRegistration;
                disableButton.onclick = disableHanko;
            }

        }).catch(function (err) {
            console.log('failed to initialize');
            console.log("error:", err);
        });
    }
</script>
</body>
</html>