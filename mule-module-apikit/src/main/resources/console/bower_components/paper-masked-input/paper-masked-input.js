Polymer({
  is: 'paper-masked-input',

  properties: {
    /**
     * The label for this input.
     */
    label: String,
    //The value for this input.
    value: {
      type: String,
      notify: true
    },
    // Set to true to show the text in the input field.
    visible: {
      type: Boolean,
      value: false,
      notify: true
    },

    // Icon name to display
    icon: {
      type: String,
      value: 'arc:visibility',
      readOnly: true
    },

    // Current input field type
    type: {
      type: String,
      value: 'password',
      readOnly: true
    },

    // Icon's alt label
    alt: {
      type: String,
      computed: '_computeAlt(title)'
    },

    // Icon's title label
    title: {
      type: String,
      value: 'Show password',
      readOnly: true
    },
    // The error message to display when the input is invalid.
    errorMessage: String,
    /**
     * Returns true if the value is invalid.
     * If `autoValidate` is true, the `invalid` attribute is managed automatically,
     * which can clobber attempts to manage it manually.
     */
    invalid: {
      type: Boolean,
      notify: true
    },
    // Set to true to prevent the user from entering invalid input.
    preventInvalidInput: Boolean,
    // Set this to specify the pattern allowed by `preventInvalidInput`.
    allowedPattern: String,
    // Name of the validator to use.
    validator: String,
    // A pattern to validate the `input` with.
    pattern: String,
    // `<input>`'s autocomplete property
    autocomplete: {
      type: String,
      value: 'off'
    },
    // Set to true to mark the input as required.
    required: {
      type: Boolean,
      value: false
    },
    // Binds to `<input>`'s `autofocus` property
    autofocus: Boolean,
    // Binds to `<input>`'s `inputmode` property
    inputmode: String,
    // The minimum length of the input value.
    minlength: Number,
    // The maximum length of the input value.
    maxlength: Number,
    // Binds to `<input>`'s `name` property
    name: String,
    /**
     * A placeholder string in addition to the label. If this is set, the label will always float.
     */
    placeholder: {
      type: String,
      value: ''
    },
    // Binds to `<input>`'s `readonly` property
    readonly: {
      type: Boolean,
      value: false
    },
    // Binds to `<input>`'s `size` property
    size: Number,
    // Binds to `<input>`'s `autocapitalize` property
    autocapitalize: {
      type: String,
      value: 'none'
    },
    // Binds to `<input>`'s `autocorrect` property
    autocorrect: {
      type: String,
      value: 'off'
    },
    // Set to true to disable this input.
    disabled: {
      type: Boolean,
      value: false
    },
    // Set to true to disable the floating label.
    noLabelFloat: {
      type: Boolean,
      value: false
    },
    // Set to true to always float the label.
    alwaysFloatLabel: {
      type: Boolean,
      value: false
    },
    // Set to true to auto-validate the input value.
    autoValidate: {
      type: Boolean,
      value: false
    }
  },

  hostAttributes: {
    tabindex: 0
  },

  observers: [
    '_visibleChanged(visible)'
  ],
  // Toggle password visibility.
  toggle: function() {
    this.set('visible', !this.visible);
  },

  _visibleChanged: function(visible) {
    if (visible) {
      this._setType('text');
      this._setIcon('arc:visibility-off');
      this._setTitle('Hide password');
    } else {
      this._setType('password');
      this._setIcon('arc:visibility');
      this._setTitle('Show password');
    }
  },

  _computeAlt: function(title) {
    return title + ' icon';
  },

  // Clears the value of the field.
  clear: function() {
    this.set('value', '');
  },

  validate: function() {
    this.$.input.validate();
  },

  get inputElement() {
    return this.$.input;
  }
});
