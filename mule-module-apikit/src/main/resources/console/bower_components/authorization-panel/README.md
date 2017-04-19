[![Build Status](https://travis-ci.org/advanced-rest-client/authorization-panel.svg?branch=stage)](https://travis-ci.org/advanced-rest-client/authorization-panel)  

# authorization-panel

`<authorization-panel>` The authorization panel used in the request panel.
It is a set of forms that allow set up the authorization method for a HTTP request.

It renders a set of forms. But it or it's children do not perform authorization.
In case of `basic` method the app should insert the authorization header automatically when
running the request.
When enabled authorization type is `ntlm` then username, password and domain should be passed
to transport (XHR, socket) and there perform the authorization.

Oauth 2 form sends the `oauth2-token-requested` with the OAuth settings provided with the form.
Any element / app can handle this event and perform authorization.
ARC provides the `<oauth2-authorization>` element (from the `oauth-authorization` repo) that can
be placed anywhere in the DOM (from current element where `authorization-panel` is attached up to
the body) and perform OAuth athorization. However it can be done by any other element / app  or
even server. See `<oauth2-authorization>` for detailed documentation.

Note: OAuth 2.0 server flow probably will not work in regular browser environment because
main providers aren't setting CORS headers and therefore the request will be canceled by the
browser.
To make it work, handle the `oauth2-token-requested` fired from the inside of this element.
If it's browser flow type (implicit) then the `oauth2-authorization` element can be used.
If other type, then cancel the event and use server to handle token exchange.

Oauth 1a is not currently supported. Though the form is ready and available, there's no
authorization method in the ARC components set.

### Example
```
<authorization-panel></authorization-panel>
```

### Styling
`<authorization-panel>` provides the following custom properties and mixins for styling:

Custom property | Description | Default
----------------|-------------|----------
`--authorization-panel` | Mixin applied to the element | `{}`



### Events
| Name | Description | Params |
| --- | --- | --- |
| authorization-settings-changed | Fired when auth settings change.  It will be fired when any of types is currently selected and any value of any property has changed. | settings **Object** - Current auth settings. It depends on enabled `type`. |
type **String** - Enabled auth type. For example `basic`, `ntlm` or `oauth2`. |
| authorization-type-changed | Fired when the authorization type changed. Note that the `settings` property may not be updated at the moment of of firing the event. | type **String** - Current auth type |
| query-parameter-changed | Fired when the query param changed and all listeners should update parameters / URL value. | name **String** - Name of the header that has changed |
value **String** - Header new value |
| request-header-changed | Fired when the request header changed and all listeners should update header value. | name **String** - Name of the header that has changed |
value **String** - Header new value |
