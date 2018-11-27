<div class="login-methods">
    <#if hasLoginMethods == true>
        <h3>Or sign in using:</h3>
    </#if>
    <div class="stretched-row">
    <#if loginMethod != "PASSWORD">
        <form action="${url.loginAction}" style="display:hidden" class="${properties.kcFFormClass!}" method="post">
            <input class="${properties.kcButtonClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!} btn-login-method"
                   type="submit" name="switch" id="button_cancel" value="Password"/>
            <input type="hidden" name="loginMethod" value="PASSWORD"/>
        </form>
    </#if>
    <#if loginMethod != "UAF">
        <#if hasUaf == true>
            <form action="${url.loginAction}" style="display:hidden" class="${properties.kcFFormClass!}" method="post">
                <input class="${properties.kcButtonClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!} btn-login-method"
                       type="submit" name="switch" id="button_cancel" value="Authenticator"/>
                <input type="hidden" name="loginMethod" value="UAF"/>
            </form>
        </#if>
    </#if>
    <#if loginMethod != "WEBAUTHN">
        <#if hasWebAuthn == true>
            <form action="${url.loginAction}" style="display:hidden" class="${properties.kcFFormClass!}" method="post">
                <input class="${properties.kcButtonClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!} btn-login-method"
                       type="submit" name="switch" id="button_cancel" value="WebAuthn"/>
                <input type="hidden" name="loginMethod" value="WEBAUTHN"/>
            </form>
        </#if>
    </#if>
    </div>
</div>