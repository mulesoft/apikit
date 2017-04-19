<a name="2.0.14"></a>
## [2.0.14](https://github.com/advanced-rest-client/auth-methods/compare/2.0.13...v2.0.14) (2017-03-28)




<a name="2.0.13"></a>
## [2.0.13](https://github.com/advanced-rest-client/auth-methods/compare/2.0.11...v2.0.13) (2017-03-28)


### Fix

* tokenValue in settings object is now available when settings change event fires ([d187627748f18c4184fed2cbd149f63c32c922c7](https://github.com/advanced-rest-client/auth-methods/commit/d187627748f18c4184fed2cbd149f63c32c922c7))



<a name="2.0.12"></a>
## [2.0.12](https://github.com/advanced-rest-client/auth-methods/compare/2.0.11...v2.0.12) (2017-03-28)




<a name="2.0.11"></a>
## [2.0.11](https://github.com/advanced-rest-client/auth-methods/compare/2.0.10...v2.0.11) (2017-03-28)




<a name="2.0.10"></a>
## [2.0.10](https://github.com/advanced-rest-client/auth-methods/compare/2.0.8...v2.0.10) (2017-03-28)


### Update

* Added event listeners for the auth-settings-changed event so changes in one component will be reflected in any other component of the same type ([277ae396dfb8a882e776bd67579a5dd973eed42c](https://github.com/advanced-rest-client/auth-methods/commit/277ae396dfb8a882e776bd67579a5dd973eed42c))



<a name="2.0.9"></a>
## [2.0.9](https://github.com/advanced-rest-client/auth-methods/compare/2.0.8...v2.0.9) (2017-03-28)




<a name="2.0.8"></a>
## [2.0.8](https://github.com/advanced-rest-client/auth-methods/compare/2.0.7...v2.0.8) (2017-03-25)




<a name="2.0.7"></a>
## [2.0.7](https://github.com/advanced-rest-client/auth-methods/compare/2.0.5...v2.0.7) (2017-03-25)


### Update

* removed reference to paper-toggle-button ([8479bf4f54066da3e495521101460dba9ce544f2](https://github.com/advanced-rest-client/auth-methods/commit/8479bf4f54066da3e495521101460dba9ce544f2))



<a name="2.0.6"></a>
## [2.0.6](https://github.com/advanced-rest-client/auth-methods/compare/2.0.5...v2.0.6) (2017-03-25)




<a name="2.0.5"></a>
## [2.0.5](https://github.com/advanced-rest-client/auth-methods/compare/2.0.1...v2.0.5) (2017-03-25)


### Docs

* Updated docs for digest method ([17772e33622298637daf4a0a8474087631457c0c](https://github.com/advanced-rest-client/auth-methods/commit/17772e33622298637daf4a0a8474087631457c0c))

### New

* Added digest auth method form ([cc23a5aa887ca43d600eec117f2d73f6bc674488](https://github.com/advanced-rest-client/auth-methods/commit/cc23a5aa887ca43d600eec117f2d73f6bc674488))

### Update

* now settings getter will also return token value in the settings object ([207fe882e5ff4ff9fd5a5f5461205764769ac080](https://github.com/advanced-rest-client/auth-methods/commit/207fe882e5ff4ff9fd5a5f5461205764769ac080))
* Updated code and removed ES6 reference ([5cb672f8867d578d7030540495a588af5646c286](https://github.com/advanced-rest-client/auth-methods/commit/5cb672f8867d578d7030540495a588af5646c286))
* Updated test browsers list for Sauce ([414782d4459190523e9bed972b69ae602870ce5c](https://github.com/advanced-rest-client/auth-methods/commit/414782d4459190523e9bed972b69ae602870ce5c))



<a name="2.0.4"></a>
## [2.0.4](https://github.com/advanced-rest-client/auth-methods/compare/2.0.1...v2.0.4) (2017-03-14)


### Update

* Updated code and removed ES6 reference ([5cb672f8867d578d7030540495a588af5646c286](https://github.com/advanced-rest-client/auth-methods/commit/5cb672f8867d578d7030540495a588af5646c286))
* Updated test browsers list for Sauce ([414782d4459190523e9bed972b69ae602870ce5c](https://github.com/advanced-rest-client/auth-methods/commit/414782d4459190523e9bed972b69ae602870ce5c))



<a name="2.0.3"></a>
## [2.0.3](https://github.com/advanced-rest-client/auth-methods/compare/2.0.1...v2.0.3) (2017-03-03)




<a name="2.0.2"></a>
## [2.0.2](https://github.com/advanced-rest-client/auth-methods/compare/2.0.1...v2.0.2) (2017-03-03)




<a name="2.0.1"></a>
## [2.0.1](https://github.com/advanced-rest-client/auth-methods/compare/1.0.3...v2.0.1) (2017-03-02)


### Breaking

* updated elements styles. Changed events fired by the elements. ([b11d5153f32ca550a6a68abfc5d14c1be84652ce](https://github.com/advanced-rest-client/auth-methods/commit/b11d5153f32ca550a6a68abfc5d14c1be84652ce))

### Fix

* Added a polyfill for the Array.from method so the IE11 will be happy again ☺ ([08c2e4a9389d4a6c593e740c433fcf1247a3225c](https://github.com/advanced-rest-client/auth-methods/commit/08c2e4a9389d4a6c593e740c433fcf1247a3225c))
* Fixes #1 - the OAuth will be enabled automatically when the auth token is ready ([5211090dc45cc4537e173ebd90f451f6b6425afc](https://github.com/advanced-rest-client/auth-methods/commit/5211090dc45cc4537e173ebd90f451f6b6425afc)), closes [#1](https://github.com/advanced-rest-client/auth-methods/issues/1)

### New

* Added test cases for the NTLM panel ([35eebd6e649bc0ce862417a21f2a8da135696ed3](https://github.com/advanced-rest-client/auth-methods/commit/35eebd6e649bc0ce862417a21f2a8da135696ed3))
* Added test cases for the OAuth2 panel ([785819dd00e0fcfebccbe4616b38c10120bc88b1](https://github.com/advanced-rest-client/auth-methods/commit/785819dd00e0fcfebccbe4616b38c10120bc88b1))

### Update

* Added tabindex on the redirecy URL field ([d90bc03e3a3f83852402b63389c9c6204dc3d72c](https://github.com/advanced-rest-client/auth-methods/commit/d90bc03e3a3f83852402b63389c9c6204dc3d72c))
* Updated travis configuration ([11141c1d4b9987805afa3565bb19bb8a20b7e494](https://github.com/advanced-rest-client/auth-methods/commit/11141c1d4b9987805afa3565bb19bb8a20b7e494))



<a name="1.0.5"></a>
## [1.0.5](https://github.com/advanced-rest-client/auth-methods/compare/1.0.3...v1.0.5) (2017-01-30)


### Fix

* Added a polyfill for the Array.from method so the IE11 will be happy again ☺ ([08c2e4a9389d4a6c593e740c433fcf1247a3225c](https://github.com/advanced-rest-client/auth-methods/commit/08c2e4a9389d4a6c593e740c433fcf1247a3225c))
* Fixes #1 - the OAuth will be enabled automatically when the auth token is ready ([5211090dc45cc4537e173ebd90f451f6b6425afc](https://github.com/advanced-rest-client/auth-methods/commit/5211090dc45cc4537e173ebd90f451f6b6425afc)), closes [#1](https://github.com/advanced-rest-client/auth-methods/issues/1)

### New

* Added test cases for the NTLM panel ([35eebd6e649bc0ce862417a21f2a8da135696ed3](https://github.com/advanced-rest-client/auth-methods/commit/35eebd6e649bc0ce862417a21f2a8da135696ed3))
* Added test cases for the OAuth2 panel ([785819dd00e0fcfebccbe4616b38c10120bc88b1](https://github.com/advanced-rest-client/auth-methods/commit/785819dd00e0fcfebccbe4616b38c10120bc88b1))

### Update

* Added tabindex on the redirecy URL field ([d90bc03e3a3f83852402b63389c9c6204dc3d72c](https://github.com/advanced-rest-client/auth-methods/commit/d90bc03e3a3f83852402b63389c9c6204dc3d72c))
* Updated travis configuration ([11141c1d4b9987805afa3565bb19bb8a20b7e494](https://github.com/advanced-rest-client/auth-methods/commit/11141c1d4b9987805afa3565bb19bb8a20b7e494))



<a name="1.0.4"></a>
## [1.0.4](https://github.com/advanced-rest-client/auth-methods/compare/1.0.3...v1.0.4) (2017-01-09)


### Fix

* Added a polyfill for the Array.from method so the IE11 will be happy again ☺ ([08c2e4a9389d4a6c593e740c433fcf1247a3225c](https://github.com/advanced-rest-client/auth-methods/commit/08c2e4a9389d4a6c593e740c433fcf1247a3225c))
* Fixes #1 - the OAuth will be enabled automatically when the auth token is ready ([5211090dc45cc4537e173ebd90f451f6b6425afc](https://github.com/advanced-rest-client/auth-methods/commit/5211090dc45cc4537e173ebd90f451f6b6425afc)), closes [#1](https://github.com/advanced-rest-client/auth-methods/issues/1)

### New

* Added test cases for the NTLM panel ([35eebd6e649bc0ce862417a21f2a8da135696ed3](https://github.com/advanced-rest-client/auth-methods/commit/35eebd6e649bc0ce862417a21f2a8da135696ed3))
* Added test cases for the OAuth2 panel ([785819dd00e0fcfebccbe4616b38c10120bc88b1](https://github.com/advanced-rest-client/auth-methods/commit/785819dd00e0fcfebccbe4616b38c10120bc88b1))

### Update

* Updated travis configuration ([11141c1d4b9987805afa3565bb19bb8a20b7e494](https://github.com/advanced-rest-client/auth-methods/commit/11141c1d4b9987805afa3565bb19bb8a20b7e494))



<a name="1.0.3"></a>
## [1.0.3](https://github.com/advanced-rest-client/auth-methods/compare/1.0.2...v1.0.3) (2016-12-23)


### Breaking

* Chnaged event parameters structure. Added raml-settings attribute ([333844a90e8633a00cf123a505d11066d2dee332](https://github.com/advanced-rest-client/auth-methods/commit/333844a90e8633a00cf123a505d11066d2dee332))



<a name="0.0.5"></a>
## [0.0.5](https://github.com/advanced-rest-client/auth-methods/compare/0.0.4...v0.0.5) (2016-11-29)


<a name="1.0.2"></a>
## [1.0.2](https://github.com/advanced-rest-client/auth-methods/compare/0.0.4...v1.0.2) (2016-11-29)


### Docs

* Updated documentation and demos  for the elements ([fa24c14cebe77ba5923180452df8deb9bc01fa06](https://github.com/advanced-rest-client/auth-methods/commit/fa24c14cebe77ba5923180452df8deb9bc01fa06))

### New

* Added validation methods ([94cd4d0ea1b5703e26bd385e5a7a2c30a9b3b210](https://github.com/advanced-rest-client/auth-methods/commit/94cd4d0ea1b5703e26bd385e5a7a2c30a9b3b210))

### Update

* Bumping version to stable ([6c52d8a4326da5bda627942215ecc7675228b7c2](https://github.com/advanced-rest-client/auth-methods/commit/6c52d8a4326da5bda627942215ecc7675228b7c2))



<a name="1.0.1"></a>
## [1.0.1](https://github.com/advanced-rest-client/auth-methods/compare/0.0.4...v1.0.1) (2016-11-29)


### Docs

* Updated documentation and demos  for the elements ([fa24c14cebe77ba5923180452df8deb9bc01fa06](https://github.com/advanced-rest-client/auth-methods/commit/fa24c14cebe77ba5923180452df8deb9bc01fa06))

### New

* Added validation methods ([94cd4d0ea1b5703e26bd385e5a7a2c30a9b3b210](https://github.com/advanced-rest-client/auth-methods/commit/94cd4d0ea1b5703e26bd385e5a7a2c30a9b3b210))

### Update

* Bumping version to stable ([6c52d8a4326da5bda627942215ecc7675228b7c2](https://github.com/advanced-rest-client/auth-methods/commit/6c52d8a4326da5bda627942215ecc7675228b7c2))


<a name="0.0.4"></a>
## [0.0.4](https://github.com/advanced-rest-client/auth-methods/compare/0.0.3...v0.0.4) (2016-11-28)




<a name="0.0.3"></a>
## [0.0.3](https://github.com/advanced-rest-client/auth-methods/compare/0.0.2...v0.0.3) (2016-11-28)




<a name="0.0.2"></a>
## 0.0.2 (2016-11-25)




