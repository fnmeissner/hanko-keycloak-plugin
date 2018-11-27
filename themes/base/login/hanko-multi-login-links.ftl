<ul>
    <#if loginMethod != "PASSWORD">
        <li>
            <form action="${url.loginAction}" style="display:hidden" class="${properties.kcFFormClass!}" method="post">
                <input class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!}"
                       type="submit" name="switch" id="button_cancel" value="Password"/>
                <input type="hidden" name="loginMethod" value="PASSWORD"/>
            </form>
        </li>
    </#if>
    <#if loginMethod != "UAF">
        <#if hasUaf == true>
            <li>
                <form action="${url.loginAction}" style="display:hidden" class="${properties.kcFFormClass!}" method="post">
                    <input class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!}"
                           type="submit" name="switch" id="button_cancel" value="Authenticator"/>
                    <input type="hidden" name="loginMethod" value="UAF"/>
                </form>
            </li>
        </#if>
    </#if>
    <#if loginMethod != "WEBAUTHN">
        <#if hasWebAuthn == true>
            <li>
                <form action="${url.loginAction}" style="display:hidden" class="${properties.kcFFormClass!}" method="post">
                    <input class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!}"
                           type="submit" name="switch" id="button_cancel" value="WebAuthn"/>
                    <input type="hidden" name="loginMethod" value="WEBAUTHN"/>
                </form>
            </li>
        </#if>
    </#if>
</ul>