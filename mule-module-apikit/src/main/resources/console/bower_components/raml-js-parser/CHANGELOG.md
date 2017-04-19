<a name="2.0.1"></a>
## [2.0.1](https://github.com/advanced-rest-client/raml-js-parser/compare/1.1.7...v2.0.1) (2017-03-30)


### Breaking

* The parser will not longer use raml2object to normalise the JSON output. The raml2object and data types expansion has been moved to the raml-json-enhance element. The element only parses RAML and reports the parser's output to the app ([5ef93c8057cb6e55d31e5369b5f402a1fcbc80d8](https://github.com/advanced-rest-client/raml-js-parser/commit/5ef93c8057cb6e55d31e5369b5f402a1fcbc80d8))

### Update

* Added performance analysis to the parser ([cbb54d1d37b85834cbd066307ee4ba39a6108729](https://github.com/advanced-rest-client/raml-js-parser/commit/cbb54d1d37b85834cbd066307ee4ba39a6108729))



<a name="1.1.7"></a>
## [1.1.7](https://github.com/advanced-rest-client/raml-js-parser/compare/1.1.6...v1.1.7) (2017-03-09)


### Fix

* Fixed >import location< script (localizes the element URL for workers import location) and therefore fixing the parser to work in Safari 9 ([0e515b5eee6317312936b45a7ae99bfa61b2a9d3](https://github.com/advanced-rest-client/raml-js-parser/commit/0e515b5eee6317312936b45a7ae99bfa61b2a9d3))

### Update

* Working on a failing test for Safari 9 ([1997a639241cd8724f75cedf401723ba47542421](https://github.com/advanced-rest-client/raml-js-parser/commit/1997a639241cd8724f75cedf401723ba47542421))



<a name="1.1.6"></a>
## [1.1.6](https://github.com/advanced-rest-client/raml-js-parser/compare/1.1.2...v1.1.6) (2017-01-13)


### Breaking

* Made the parser normalizer to work in web worker. It doesn't change anything in the API and how to use the element. However it changes the build process. See readme for more information ([de79a4800df12dafbeca527515581583ad91f308](https://github.com/advanced-rest-client/raml-js-parser/commit/de79a4800df12dafbeca527515581583ad91f308))
* Now the parser output will contain security schemes applied to methods and resource. Also the resourceUrl was removed and requestUri is not the only one showing the absolute URL ([7568efa3fa7f2ceba0b39df69b93c708708c958c](https://github.com/advanced-rest-client/raml-js-parser/commit/7568efa3fa7f2ceba0b39df69b93c708708c958c))

### Fix

* Fixed import location - added last slash to the path ([7ab2236eaf2c30505e66d83c8b62c617de6bf967](https://github.com/advanced-rest-client/raml-js-parser/commit/7ab2236eaf2c30505e66d83c8b62c617de6bf967))
* Fixed import location path so the IE11 could det it correctly ([c12cd4d72cac4c6bfb40eb2a4bab413fc7e35f3f](https://github.com/advanced-rest-client/raml-js-parser/commit/c12cd4d72cac4c6bfb40eb2a4bab413fc7e35f3f))

### Update

* updated import script colcation so it work in IE when testing other elements ([0d5a02acaff79e4b165ef471bf6fdf3622b16b0a](https://github.com/advanced-rest-client/raml-js-parser/commit/0d5a02acaff79e4b165ef471bf6fdf3622b16b0a))
* Updated safari version to 10 in test cases ([d29d4fa3b6a34edc7be8fd06b1fbd04eec6404e2](https://github.com/advanced-rest-client/raml-js-parser/commit/d29d4fa3b6a34edc7be8fd06b1fbd04eec6404e2))



<a name="1.1.5"></a>
## [1.1.5](https://github.com/advanced-rest-client/raml-js-parser/compare/1.1.2...v1.1.5) (2017-01-13)


### Breaking

* Made the parser normalizer to work in web worker. It doesn't change anything in the API and how to use the element. However it changes the build process. See readme for more information ([de79a4800df12dafbeca527515581583ad91f308](https://github.com/advanced-rest-client/raml-js-parser/commit/de79a4800df12dafbeca527515581583ad91f308))
* Now the parser output will contain security schemes applied to methods and resource. Also the resourceUrl was removed and requestUri is not the only one showing the absolute URL ([7568efa3fa7f2ceba0b39df69b93c708708c958c](https://github.com/advanced-rest-client/raml-js-parser/commit/7568efa3fa7f2ceba0b39df69b93c708708c958c))

### Fix

* Fixed import location - added last slash to the path ([7ab2236eaf2c30505e66d83c8b62c617de6bf967](https://github.com/advanced-rest-client/raml-js-parser/commit/7ab2236eaf2c30505e66d83c8b62c617de6bf967))
* Fixed import location path so the IE11 could det it correctly ([c12cd4d72cac4c6bfb40eb2a4bab413fc7e35f3f](https://github.com/advanced-rest-client/raml-js-parser/commit/c12cd4d72cac4c6bfb40eb2a4bab413fc7e35f3f))

### Update

* Updated safari version to 10 in test cases ([d29d4fa3b6a34edc7be8fd06b1fbd04eec6404e2](https://github.com/advanced-rest-client/raml-js-parser/commit/d29d4fa3b6a34edc7be8fd06b1fbd04eec6404e2))



<a name="1.1.4"></a>
## [1.1.4](https://github.com/advanced-rest-client/raml-js-parser/compare/1.1.2...v1.1.4) (2017-01-13)


### Breaking

* Made the parser normalizer to work in web worker. It doesn't change anything in the API and how to use the element. However it changes the build process. See readme for more information ([de79a4800df12dafbeca527515581583ad91f308](https://github.com/advanced-rest-client/raml-js-parser/commit/de79a4800df12dafbeca527515581583ad91f308))
* Now the parser output will contain security schemes applied to methods and resource. Also the resourceUrl was removed and requestUri is not the only one showing the absolute URL ([7568efa3fa7f2ceba0b39df69b93c708708c958c](https://github.com/advanced-rest-client/raml-js-parser/commit/7568efa3fa7f2ceba0b39df69b93c708708c958c))

### Fix

* Fixed import location - added last slash to the path ([7ab2236eaf2c30505e66d83c8b62c617de6bf967](https://github.com/advanced-rest-client/raml-js-parser/commit/7ab2236eaf2c30505e66d83c8b62c617de6bf967))
* Fixed import location path so the IE11 could det it correctly ([c12cd4d72cac4c6bfb40eb2a4bab413fc7e35f3f](https://github.com/advanced-rest-client/raml-js-parser/commit/c12cd4d72cac4c6bfb40eb2a4bab413fc7e35f3f))

### Update

* Updated safari version to 10 in test cases ([d29d4fa3b6a34edc7be8fd06b1fbd04eec6404e2](https://github.com/advanced-rest-client/raml-js-parser/commit/d29d4fa3b6a34edc7be8fd06b1fbd04eec6404e2))



<a name="1.1.2"></a>
## [1.1.2](https://github.com/advanced-rest-client/raml-js-parser/compare/1.1.1...v1.1.2) (2016-12-23)


### Update

* Updated raml2obj.js file where type error on baseUrl has been fixed. ([3b1dc4932c5d9f70ab631474f803876d70e64035](https://github.com/advanced-rest-client/raml-js-parser/commit/3b1dc4932c5d9f70ab631474f803876d70e64035))



<a name="1.1.1"></a>
## [1.1.1](https://github.com/advanced-rest-client/raml-js-parser/compare/1.0.16...v1.1.1) (2016-12-19)


### Docs

* Bumped minor version number after refactoring and adding polyfills. Updated docs ([aba681b06fa5c4dc24d4e3d53fb6084ea3956e91](https://github.com/advanced-rest-client/raml-js-parser/commit/aba681b06fa5c4dc24d4e3d53fb6084ea3956e91))



<a name="1.0.17"></a>
## [1.0.17](https://github.com/advanced-rest-client/raml-js-parser/compare/1.0.16...v1.0.17) (2016-12-08)




<a name="1.0.16"></a>
## [1.0.16](https://github.com/advanced-rest-client/raml-js-parser/compare/1.0.15...v1.0.16) (2016-12-08)




<a name="1.0.15"></a>
## [1.0.15](https://github.com/advanced-rest-client/raml-js-parser/compare/1.0.14...v1.0.15) (2016-12-08)




<a name="1.0.14"></a>
## [1.0.14](https://github.com/advanced-rest-client/raml-js-parser/compare/1.0.13...v1.0.14) (2016-12-05)


### Update

* Removed own Normalizer and added Mulesoft's raml-parser-toolbelt to normalize the output ([e5cca5f2c9a46b8a10ff8a3711b9b44dbfd988a3](https://github.com/advanced-rest-client/raml-js-parser/commit/e5cca5f2c9a46b8a10ff8a3711b9b44dbfd988a3))



<a name="1.0.13"></a>
## [1.0.13](https://github.com/advanced-rest-client/raml-js-parser/compare/1.0.12...v1.0.13) (2016-12-05)




<a name="1.0.12"></a>
## [1.0.12](https://github.com/advanced-rest-client/raml-js-parser/compare/1.0.11...v1.0.12) (2016-12-05)




<a name="1.0.11"></a>
## [1.0.11](https://github.com/advanced-rest-client/raml-js-parser/compare/1.0.10...v1.0.11) (2016-12-05)




<a name="1.0.10"></a>
## [1.0.10](https://github.com/advanced-rest-client/raml-js-parser/compare/1.0.9...v1.0.10) (2016-12-05)




<a name="1.0.9"></a>
## [1.0.9](https://github.com/advanced-rest-client/raml-js-parser/compare/1.0.8...v1.0.9) (2016-12-03)




<a name="1.0.8"></a>
## [1.0.8](https://github.com/advanced-rest-client/raml-js-parser/compare/1.0.7...v1.0.8) (2016-10-06)


### New

* Added dependencyci ([f8332c156c2c0115a283ea23bdda6852a635c7ef](https://github.com/advanced-rest-client/raml-js-parser/commit/f8332c156c2c0115a283ea23bdda6852a635c7ef))
* Added hero image ([d83d94dfcdc60264cb604825d8aef29bf35222a1](https://github.com/advanced-rest-client/raml-js-parser/commit/d83d94dfcdc60264cb604825d8aef29bf35222a1))



<a name="1.0.7"></a>
## [1.0.7](https://github.com/advanced-rest-client/raml-js-parser/compare/1.0.6...v1.0.7) (2016-10-03)


### Docs

* Updated demo page. It now uses the <raml-main-entry-lookup> element to search for the main entry point of the RAML api ([d9a4ff54ea706707c77f5cafb875dfd0fb54c658](https://github.com/advanced-rest-client/raml-js-parser/commit/d9a4ff54ea706707c77f5cafb875dfd0fb54c658))

### Update

* Updated component, now the event handler will not override the files array if the files parameter is not available and it can be set directly onthe element. Also updated element inline documentation and fixed formatting ([c4df38071df1eb8a90780565d992129b1d494678](https://github.com/advanced-rest-client/raml-js-parser/commit/c4df38071df1eb8a90780565d992129b1d494678))



<a name="1.0.6"></a>
## [1.0.6](https://github.com/advanced-rest-client/raml-js-parser/compare/1.0.2...v1.0.6) (2016-10-02)


### Fix

* Fixed content file path resolution ([1c909b1be2c50d56c67cf7229258a7f8a83d9226](https://github.com/advanced-rest-client/raml-js-parser/commit/1c909b1be2c50d56c67cf7229258a7f8a83d9226))
* Fixing travis file per missing directory while building ([1cc013484a5bc9fa1925b6f7bb183a9567e6fdc3](https://github.com/advanced-rest-client/raml-js-parser/commit/1cc013484a5bc9fa1925b6f7bb183a9567e6fdc3))
* Fixing travis file per missing directory while building ([42d0a9ac50f97b8d9562814a8566393059ea00a4](https://github.com/advanced-rest-client/raml-js-parser/commit/42d0a9ac50f97b8d9562814a8566393059ea00a4))
* Fixing travis file with ssh keys ([8e53b7d39fa694e8fb6ede4d6b3a54601e0babbf](https://github.com/advanced-rest-client/raml-js-parser/commit/8e53b7d39fa694e8fb6ede4d6b3a54601e0babbf))
* Fixing travis file with ssh keys ([be9610d29c4a5b6394ffc492c342ffa3d717f391](https://github.com/advanced-rest-client/raml-js-parser/commit/be9610d29c4a5b6394ffc492c342ffa3d717f391))
* Fixing travis file with ssh keys ([6088f5fdc63b0492757298ec4f0488bd891bd133](https://github.com/advanced-rest-client/raml-js-parser/commit/6088f5fdc63b0492757298ec4f0488bd891bd133))
* Fixing travis file with ssh keys ([49d522efc8227a3015b87b62989dc78b14a345ce](https://github.com/advanced-rest-client/raml-js-parser/commit/49d522efc8227a3015b87b62989dc78b14a345ce))

### New

* Added cache directive to the travis.yaml file ([4c230fec236353a754db48a3191b398623c51a22](https://github.com/advanced-rest-client/raml-js-parser/commit/4c230fec236353a754db48a3191b398623c51a22))

### Update

* cleared demo and removed console.log from the component page ([02d848342e3656451338ba2e3f205f5640c5ee07](https://github.com/advanced-rest-client/raml-js-parser/commit/02d848342e3656451338ba2e3f205f5640c5ee07))
* Removed console logging from the element file and demo page ([e4e98f291ea310d5c984fe009a02c43afc1ad646](https://github.com/advanced-rest-client/raml-js-parser/commit/e4e98f291ea310d5c984fe009a02c43afc1ad646))
* Updated travis integration ([e6c07b50b87f8b1ca49c824fe08d6cce09100cfc](https://github.com/advanced-rest-client/raml-js-parser/commit/e6c07b50b87f8b1ca49c824fe08d6cce09100cfc))



<a name="1.0.5"></a>
## [1.0.5](https://github.com/advanced-rest-client/raml-js-parser/compare/1.0.2...v1.0.5) (2016-10-02)


### Fix

* Fixed content file path resolution ([1c909b1be2c50d56c67cf7229258a7f8a83d9226](https://github.com/advanced-rest-client/raml-js-parser/commit/1c909b1be2c50d56c67cf7229258a7f8a83d9226))
* Fixing travis file per missing directory while building ([1cc013484a5bc9fa1925b6f7bb183a9567e6fdc3](https://github.com/advanced-rest-client/raml-js-parser/commit/1cc013484a5bc9fa1925b6f7bb183a9567e6fdc3))
* Fixing travis file per missing directory while building ([42d0a9ac50f97b8d9562814a8566393059ea00a4](https://github.com/advanced-rest-client/raml-js-parser/commit/42d0a9ac50f97b8d9562814a8566393059ea00a4))
* Fixing travis file with ssh keys ([8e53b7d39fa694e8fb6ede4d6b3a54601e0babbf](https://github.com/advanced-rest-client/raml-js-parser/commit/8e53b7d39fa694e8fb6ede4d6b3a54601e0babbf))
* Fixing travis file with ssh keys ([be9610d29c4a5b6394ffc492c342ffa3d717f391](https://github.com/advanced-rest-client/raml-js-parser/commit/be9610d29c4a5b6394ffc492c342ffa3d717f391))
* Fixing travis file with ssh keys ([6088f5fdc63b0492757298ec4f0488bd891bd133](https://github.com/advanced-rest-client/raml-js-parser/commit/6088f5fdc63b0492757298ec4f0488bd891bd133))
* Fixing travis file with ssh keys ([49d522efc8227a3015b87b62989dc78b14a345ce](https://github.com/advanced-rest-client/raml-js-parser/commit/49d522efc8227a3015b87b62989dc78b14a345ce))

### New

* Added cache directive to the travis.yaml file ([4c230fec236353a754db48a3191b398623c51a22](https://github.com/advanced-rest-client/raml-js-parser/commit/4c230fec236353a754db48a3191b398623c51a22))

### Update

* Updated travis integration ([e6c07b50b87f8b1ca49c824fe08d6cce09100cfc](https://github.com/advanced-rest-client/raml-js-parser/commit/e6c07b50b87f8b1ca49c824fe08d6cce09100cfc))



<a name="1.0.4"></a>
## [1.0.4](https://github.com/advanced-rest-client/raml-js-parser/compare/1.0.2...v1.0.4) (2016-09-28)


### Fix

* Fixing travis file per missing directory while building ([1cc013484a5bc9fa1925b6f7bb183a9567e6fdc3](https://github.com/advanced-rest-client/raml-js-parser/commit/1cc013484a5bc9fa1925b6f7bb183a9567e6fdc3))
* Fixing travis file per missing directory while building ([42d0a9ac50f97b8d9562814a8566393059ea00a4](https://github.com/advanced-rest-client/raml-js-parser/commit/42d0a9ac50f97b8d9562814a8566393059ea00a4))
* Fixing travis file with ssh keys ([8e53b7d39fa694e8fb6ede4d6b3a54601e0babbf](https://github.com/advanced-rest-client/raml-js-parser/commit/8e53b7d39fa694e8fb6ede4d6b3a54601e0babbf))
* Fixing travis file with ssh keys ([be9610d29c4a5b6394ffc492c342ffa3d717f391](https://github.com/advanced-rest-client/raml-js-parser/commit/be9610d29c4a5b6394ffc492c342ffa3d717f391))
* Fixing travis file with ssh keys ([6088f5fdc63b0492757298ec4f0488bd891bd133](https://github.com/advanced-rest-client/raml-js-parser/commit/6088f5fdc63b0492757298ec4f0488bd891bd133))
* Fixing travis file with ssh keys ([49d522efc8227a3015b87b62989dc78b14a345ce](https://github.com/advanced-rest-client/raml-js-parser/commit/49d522efc8227a3015b87b62989dc78b14a345ce))

### New

* Added cache directive to the travis.yaml file ([4c230fec236353a754db48a3191b398623c51a22](https://github.com/advanced-rest-client/raml-js-parser/commit/4c230fec236353a754db48a3191b398623c51a22))

### Update

* Updated travis integration ([e6c07b50b87f8b1ca49c824fe08d6cce09100cfc](https://github.com/advanced-rest-client/raml-js-parser/commit/e6c07b50b87f8b1ca49c824fe08d6cce09100cfc))



<a name="1.0.3"></a>
## [1.0.3](https://github.com/advanced-rest-client/raml-js-parser/compare/1.0.2...v1.0.3) (2016-09-27)


### Fix

* Fixing travis file per missing directory while building ([1cc013484a5bc9fa1925b6f7bb183a9567e6fdc3](https://github.com/advanced-rest-client/raml-js-parser/commit/1cc013484a5bc9fa1925b6f7bb183a9567e6fdc3))
* Fixing travis file per missing directory while building ([42d0a9ac50f97b8d9562814a8566393059ea00a4](https://github.com/advanced-rest-client/raml-js-parser/commit/42d0a9ac50f97b8d9562814a8566393059ea00a4))
* Fixing travis file with ssh keys ([8e53b7d39fa694e8fb6ede4d6b3a54601e0babbf](https://github.com/advanced-rest-client/raml-js-parser/commit/8e53b7d39fa694e8fb6ede4d6b3a54601e0babbf))
* Fixing travis file with ssh keys ([be9610d29c4a5b6394ffc492c342ffa3d717f391](https://github.com/advanced-rest-client/raml-js-parser/commit/be9610d29c4a5b6394ffc492c342ffa3d717f391))
* Fixing travis file with ssh keys ([6088f5fdc63b0492757298ec4f0488bd891bd133](https://github.com/advanced-rest-client/raml-js-parser/commit/6088f5fdc63b0492757298ec4f0488bd891bd133))
* Fixing travis file with ssh keys ([49d522efc8227a3015b87b62989dc78b14a345ce](https://github.com/advanced-rest-client/raml-js-parser/commit/49d522efc8227a3015b87b62989dc78b14a345ce))

### New

* Added cache directive to the travis.yaml file ([4c230fec236353a754db48a3191b398623c51a22](https://github.com/advanced-rest-client/raml-js-parser/commit/4c230fec236353a754db48a3191b398623c51a22))



<a name="1.0.3"></a>
## [1.0.3](https://github.com/advanced-rest-client/raml-js-parser/compare/1.0.2...v1.0.3) (2016-09-27)


### Fix

* Fixing travis file per missing directory while building ([1cc013484a5bc9fa1925b6f7bb183a9567e6fdc3](https://github.com/advanced-rest-client/raml-js-parser/commit/1cc013484a5bc9fa1925b6f7bb183a9567e6fdc3))
* Fixing travis file per missing directory while building ([42d0a9ac50f97b8d9562814a8566393059ea00a4](https://github.com/advanced-rest-client/raml-js-parser/commit/42d0a9ac50f97b8d9562814a8566393059ea00a4))
* Fixing travis file with ssh keys ([8e53b7d39fa694e8fb6ede4d6b3a54601e0babbf](https://github.com/advanced-rest-client/raml-js-parser/commit/8e53b7d39fa694e8fb6ede4d6b3a54601e0babbf))
* Fixing travis file with ssh keys ([be9610d29c4a5b6394ffc492c342ffa3d717f391](https://github.com/advanced-rest-client/raml-js-parser/commit/be9610d29c4a5b6394ffc492c342ffa3d717f391))
* Fixing travis file with ssh keys ([6088f5fdc63b0492757298ec4f0488bd891bd133](https://github.com/advanced-rest-client/raml-js-parser/commit/6088f5fdc63b0492757298ec4f0488bd891bd133))
* Fixing travis file with ssh keys ([49d522efc8227a3015b87b62989dc78b14a345ce](https://github.com/advanced-rest-client/raml-js-parser/commit/49d522efc8227a3015b87b62989dc78b14a345ce))

### New

* Added cache directive to the travis.yaml file ([4c230fec236353a754db48a3191b398623c51a22](https://github.com/advanced-rest-client/raml-js-parser/commit/4c230fec236353a754db48a3191b398623c51a22))



<a name="1.0.2"></a>
## [1.0.2](https://github.com/advanced-rest-client/raml-js-parser/compare/1.0.1...v1.0.2) (2016-09-23)


### Fix

* fixed dependency - moved file-drop element to dev dependencies ([6518d8587f52918bdef4aa874d4ce56adf12b2ee](https://github.com/advanced-rest-client/raml-js-parser/commit/6518d8587f52918bdef4aa874d4ce56adf12b2ee))



<a name="1.0.1"></a>
## 1.0.1 (2016-09-23)


### Docs

* Updated documentation ([365f2dd6a47eaf8a567d31eebcc74f272386ed14](https://github.com/advanced-rest-client/raml-js-parser/commit/365f2dd6a47eaf8a567d31eebcc74f272386ed14))

### Fix

* fixes in demo page ([8832d0a6cf52c06889c8797dcabc7b815a3a131e](https://github.com/advanced-rest-client/raml-js-parser/commit/8832d0a6cf52c06889c8797dcabc7b815a3a131e))

### New

* Added tests ([e51cc29ebedbcb5414abf6f676b5d1bd43e209cb](https://github.com/advanced-rest-client/raml-js-parser/commit/e51cc29ebedbcb5414abf6f676b5d1bd43e209cb))
* initial commit ([664c54c45f4ae0cfefba6aa6d198e264b491cdc8](https://github.com/advanced-rest-client/raml-js-parser/commit/664c54c45f4ae0cfefba6aa6d198e264b491cdc8))

### Release

* Finished API for the first release ([000875b39d43767d6ea48d94bc3ed131efd04c7d](https://github.com/advanced-rest-client/raml-js-parser/commit/000875b39d43767d6ea48d94bc3ed131efd04c7d))

### Update

* Updated demo pages ([8471422573e802cbe7fc3b19943dbf1b41d8802b](https://github.com/advanced-rest-client/raml-js-parser/commit/8471422573e802cbe7fc3b19943dbf1b41d8802b))



