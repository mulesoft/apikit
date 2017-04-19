[![Build Status](https://travis-ci.org/advanced-rest-client/raml-main-entry-lookup.svg?branch=master)](https://travis-ci.org/advanced-rest-client/raml-main-entry-lookup)  

# raml-main-entry-lookup

The `<raml-main-entry-lookup>` is an element that lookup for a main RAML file
in the web filesystem structure.
It is a helper for RAML parser to indetify the main RAML file.

It is looking for a signle RAML file in the highest directory in the structure
and moving down to subdirectories looking for other RAML files.
The file that is lokated on top ove the directory structure wins.
If the element find more than one file all of them will be returned and the program
should ask the user to choose an entry that is the main file.

### Example
```html
<raml-main-entry-lookup
  files="[[webFileSystem]]"
  entry="{{ramlEntry}}"
  on-entry="_entryPointFound"></raml-main-entry-lookup>
```
```javascript
this._entryPointFound: function(e) {
  var file = e.detail.entry;
  if (!file) {
    return this.notifyLackOfEntryPoint();
  }
  if (file instanceof Array) {
    this.askUserAboutMainFile(file);
  } else {
    this.processRAML(file);
  }
}
```
Tou can pass a single file as the `files` attribute or the web filesystem structure.



### Events
| Name | Description | Params |
| --- | --- | --- |
| entry | Fired when the entry was found. | file **FileEntry** - Can be FileEntry, Array of file entries or null if not found at all. |
