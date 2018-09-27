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

        <form action="${url.loginAction}" style="display:hidden" class="${properties.kcFFormClass!}"
              id="kc-hanko-login-form"
              method="post">
            <input class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!}"
                   type="submit" name="cancel" id="button_cancel" value="${msg("doCancel")}"/>
        </form>

        <script>
            const awaitLoginComplete = () => {
                fetch('/auth/realms/${realm.name}/hanko/request/${requestId}')
                        .then(response => response.json())
                        .catch(error => console.error('Error:', error))
                        .then(response => {
                            if (response.status === "PENDING") {
                                setTimeout(function () {
                                    awaitLoginComplete();
                                }, 500);
                            } else {
                                document.getElementById('kc-hanko-login-form').submit();
                            }
                        });
            };

            window.onload = awaitLoginComplete;
        </script>
    </#if>
</@layout.registrationLayout>
