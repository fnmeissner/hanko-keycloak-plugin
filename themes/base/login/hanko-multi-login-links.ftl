<ul>
    <#if loginMethod != "PASSWORD">
        <li>
            <form action="${url.loginAction}" style="display:hidden" class="${properties.kcFFormClass!}" method="post">
                <input class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!}"
                       type="submit" name="switch" id="button_cancel" value="Use Password"/>
                <input type="hidden" name="loginMethod" value="PASSWORD"/>
            </form>
        </li>
    </#if>
    <#if loginMethod != "UAF">
        <li>
            <form action="${url.loginAction}" style="display:hidden" class="${properties.kcFFormClass!}" method="post">
                <input class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!}"
                       type="submit" name="switch" id="button_cancel" value="Use Hanko Authenticator"/>
                <input type="hidden" name="loginMethod" value="UAF"/>
            </form>
        </li>
    </#if>
    <#if loginMethod != "ROAMING_AUTHENTICATOR">
        <li>
            <form action="${url.loginAction}" style="display:hidden" class="${properties.kcFFormClass!}" method="post">
                <input class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!}"
                       type="submit" name="switch" id="button_cancel" value="Use Roaming Authenticator"/>
                <input type="hidden" name="loginMethod" value="ROAMING_AUTHENTICATOR"/>
            </form>
        </li>
    </#if>
    <#if loginMethod != "PLATFORM_AUTHENTICATOR">
        <li>
            <form action="${url.loginAction}" style="display:hidden" class="${properties.kcFFormClass!}" method="post">
                <input class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!}"
                       type="submit" name="switch" id="button_cancel" value="Use Platform Authenticator"/>
                <input type="hidden" name="loginMethod" value="PLATFORM_AUTHENTICATOR"/>
            </form>
        </li>
    </#if>
</ul>