<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="robots" content="noindex, nofollow">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Boat App — Sign in</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link href="${url.resourcesPath}/css/login.css" rel="stylesheet"/>
</head>
<body>

<div class="login-page">
    <div class="card">

        <!-- ── Brand section ─────────────────────────────────────────── -->
        <div class="brand-section">
            <div class="brand-badge">
                <span class="material-icons brand-icon">directions_boat</span>
            </div>
            <h1 class="app-title">Boat App</h1>
            <p class="app-tagline">Manage your fleet</p>
        </div>

        <!-- ── Global error / warning message ───────────────────────── -->
        <#if message?has_content && (message.type = 'error' || message.type = 'warning')>
        <div class="alert-msg alert-${message.type}">
            ${kcSanitize(message.summary)?no_esc}
        </div>
        </#if>

        <!-- ── Login form ────────────────────────────────────────────── -->
        <form id="kc-form-login" class="login-form"
              action="${url.loginAction}" method="post" novalidate>

            <!-- Username / email -->
            <div class="form-field">
                <label for="username">
                    <#if !realm.loginWithEmailAllowed>
                        ${msg("username")}
                    <#elseif !realm.registrationEmailAsUsername>
                        ${msg("usernameOrEmail")}
                    <#else>
                        ${msg("email")}
                    </#if>
                </label>
                <div class="input-wrapper <#if messagesPerField.existsError('username')>input-error</#if>">
                    <span class="material-icons field-icon">person</span>
                    <input tabindex="1"
                           id="username"
                           name="username"
                           type="text"
                           autocomplete="username"
                           value="${(login.username!'')}"
                           autofocus/>
                </div>
                <#if messagesPerField.existsError('username')>
                <span class="field-error">${kcSanitize(messagesPerField.get('username'))?no_esc}</span>
                </#if>
            </div>

            <!-- Password -->
            <div class="form-field">
                <label for="password">${msg("password")}</label>
                <div class="input-wrapper <#if messagesPerField.existsError('password')>input-error</#if>">
                    <span class="material-icons field-icon">lock</span>
                    <input tabindex="2"
                           id="password"
                           name="password"
                           type="password"
                           autocomplete="current-password"/>
                </div>
                <#if messagesPerField.existsError('password')>
                <span class="field-error">${kcSanitize(messagesPerField.get('password'))?no_esc}</span>
                </#if>
            </div>

            <!-- Hidden credential id required by Keycloak -->
            <input type="hidden" id="id-hidden-input" name="credentialId"
                   <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>

            <!-- Submit -->
            <button tabindex="4" type="submit" class="submit-btn">
                ${msg("doLogIn")}
            </button>

        </form>
    </div>
</div>

</body>
</html>





