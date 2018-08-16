<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "title">
        Sign in with HANKO Authenticator
    <#elseif section = "header">
        Sign in with HANKO Authenticator
    <#elseif section = "form">
        <p>Please confirm your authentication with the HANKO Authenticator you registered with your account. We have
            sent a notification to your device.</p>

        <img src="${url.resourcesPath}/img/login-hanko.png" width="120" style="display: block; margin: 50px auto">

        <p>Currently we don't support cancellation of the login process, but you can use the back-button of your browser
            to return to the login with password page.</p>

        <form action="${url.loginAction}" style="display:hidden" class="${properties.kcFFormClass!}"
              id="kc-hanko-login-form"
              method="post">
        </form>

        <script>
            window.onload = function () {
                document.getElementById('kc-hanko-login-form').submit();
            }
        </script>
    </#if>
</@layout.registrationLayout>
