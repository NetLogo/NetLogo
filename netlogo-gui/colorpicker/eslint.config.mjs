import eslint from '@eslint/js';
import tseslint from 'typescript-eslint';

export default tseslint.config({
  plugins: {
    '@typescript-eslint': tseslint.plugin
  }
, languageOptions: {
    parser: tseslint.parser
  , parserOptions: {
      project: true
    }
  }
, files: ["src/**/*.ts"]
, rules: {
    "@typescript-eslint/adjacent-overload-signatures":                 "off"
  , "@typescript-eslint/array-type":                                   ["error", { "default": "generic" }]
  , "@typescript-eslint/await-thenable":                               "error"
  , "@typescript-eslint/ban-ts-comment":                               "error"
  , "@typescript-eslint/ban-tslint-comment":                           "off"
  , "@typescript-eslint/class-literal-property-style":                 ["error", "getters"]
  , "@typescript-eslint/class-methods-use-this":                       "error"
  , "@typescript-eslint/consistent-generic-constructors":              ["error", "constructor"]
  , "@typescript-eslint/consistent-indexed-object-style":              ["error", "record"]
  , "@typescript-eslint/consistent-return":                            ["error", { treatUndefinedAsUnspecified: false }]
  , "@typescript-eslint/consistent-type-assertions":                   ["error", { assertionStyle: "as", objectLiteralTypeAssertions: "allow" }]
  , "@typescript-eslint/consistent-type-definitions":                  ["error", "type"]
  , "@typescript-eslint/consistent-type-exports":                      "error"
  , "@typescript-eslint/consistent-type-imports":                      "error"
  , "@typescript-eslint/default-param-last":                           "error"
  , "@typescript-eslint/dot-notation":                                 ["error", { allowKeywords: true }]
  , "@typescript-eslint/explicit-function-return-type":                "error"
  , "@typescript-eslint/explicit-member-accessibility":                "error"
  , "@typescript-eslint/explicit-module-boundary-types":               "error"
  , "@typescript-eslint/init-declarations":                            ["error", "always"]
  , "@typescript-eslint/member-ordering":                              "off"
  , "@typescript-eslint/method-signature-style":                       ["error", "property"]
  , "@typescript-eslint/naming-convention":                            ["error", { "selector": ["enumMember"], "format": ["PascalCase"] }]
  , "@typescript-eslint/no-array-delete":                              "error"
  , "@typescript-eslint/no-base-to-string":                            "error"
  , "@typescript-eslint/no-confusing-non-null-assertion":              "error"
  , "@typescript-eslint/no-confusing-void-expression":                 "error"
  , "@typescript-eslint/no-duplicate-enum-values":                     "error"
  , "@typescript-eslint/no-duplicate-type-constituents":               "error"
  , "@typescript-eslint/no-dynamic-delete":                            "error"
  , "@typescript-eslint/no-empty-object-type":                         "error"
  , "@typescript-eslint/no-explicit-any":                              "error"
  , "@typescript-eslint/no-extraneous-class":                          "error"
  , "@typescript-eslint/no-extra-non-null-assertion":                  "error"
  , "@typescript-eslint/no-floating-promises":                         "error"
  , "@typescript-eslint/no-for-in-array":                              "error"
  , "@typescript-eslint/no-implied-eval":                              "error"
  , "@typescript-eslint/no-import-type-side-effects":                  "error"
  , "@typescript-eslint/no-inferrable-types":                          "off"
  , "@typescript-eslint/no-invalid-void-type":                         "error"
  , "@typescript-eslint/no-loop-func":                                 "error"
  , "@typescript-eslint/no-meaningless-void-operator":                 "error"
  , "@typescript-eslint/no-misused-new":                               "error"
  , "@typescript-eslint/no-misused-promises":                          "error"
  , "@typescript-eslint/no-mixed-enums":                               "error"
  , "@typescript-eslint/no-namespace":                                 "error"
  , "@typescript-eslint/non-nullable-type-assertion-style":            "error"
  , "@typescript-eslint/no-non-null-asserted-nullish-coalescing":      "error"
  , "@typescript-eslint/no-non-null-asserted-optional-chain":          "error"
  , "@typescript-eslint/no-non-null-assertion":                        "error"
  , "@typescript-eslint/no-redundant-type-constituents":               "error"
  , "@typescript-eslint/no-require-imports":                           "error"
  , "@typescript-eslint/no-restricted-types":                          "off"
  , "@typescript-eslint/no-shadow":                                    ["error", { "builtinGlobals": false, "hoist": "all" }]
  , "@typescript-eslint/no-this-alias":                                "error"
  , "@typescript-eslint/no-unnecessary-boolean-literal-compare":       "error"
  , "@typescript-eslint/no-unnecessary-condition":                     "error"
  , "@typescript-eslint/no-unnecessary-parameter-property-assignment": "error"
  , "@typescript-eslint/no-unnecessary-qualifier":                     "error"
  , "@typescript-eslint/no-unnecessary-template-expression":           "error"
  , "@typescript-eslint/no-unnecessary-type-arguments":                "error"
  , "@typescript-eslint/no-unnecessary-type-assertion":                "error"
  , "@typescript-eslint/no-unnecessary-type-constraint":               "error"
  , "@typescript-eslint/no-unnecessary-type-parameters":               "error"
  , "@typescript-eslint/no-unsafe-argument":                           "error"
  , "@typescript-eslint/no-unsafe-assignment":                         "error"
  , "@typescript-eslint/no-unsafe-call":                               "error"
  , "@typescript-eslint/no-unsafe-declaration-merging":                "error"
  , "@typescript-eslint/no-unsafe-enum-comparison":                    "error"
  , "@typescript-eslint/no-unsafe-function-type":                      "error"
  , "@typescript-eslint/no-unsafe-member-access":                      "error"
  , "@typescript-eslint/no-unsafe-return":                             "error"
  , "@typescript-eslint/no-unsafe-unary-minus":                        "error"
  , "@typescript-eslint/no-unused-expressions":                        ["error", {}]
  , "@typescript-eslint/no-unused-vars":                               ["error", { "varsIgnorePattern": "^_\d?$" }]
  , "@typescript-eslint/no-useless-empty-export":                      "error"
  , "@typescript-eslint/no-wrapper-object-types":                      "error"
  , "@typescript-eslint/parameter-properties":                         "off"
  , "@typescript-eslint/prefer-as-const":                              "error"
  , "@typescript-eslint/prefer-enum-initializers":                     "off"
  , "@typescript-eslint/prefer-find":                                  "error"
  , "@typescript-eslint/prefer-for-of":                                "error"
  , "@typescript-eslint/prefer-function-type":                         "error"
  , "@typescript-eslint/prefer-includes":                              "error"
  , "@typescript-eslint/prefer-literal-enum-member":                   "error"
  , "@typescript-eslint/prefer-namespace-keyword":                     "off"
  , "@typescript-eslint/prefer-nullish-coalescing":                    "error"
  , "@typescript-eslint/prefer-optional-chain":                        "error"
  , "@typescript-eslint/prefer-promise-reject-errors":                 "error"
  , "@typescript-eslint/prefer-readonly":                              "error"
  , "@typescript-eslint/prefer-readonly-parameter-types":              "off"
  , "@typescript-eslint/prefer-reduce-type-parameter":                 "error"
  , "@typescript-eslint/prefer-regexp-exec":                           "off"
  , "@typescript-eslint/prefer-return-this-type":                      "off"
  , "@typescript-eslint/prefer-string-starts-ends-with":               "error"
  , "@typescript-eslint/promise-function-async":                       "error"
  , "@typescript-eslint/require-array-sort-compare":                   "error"
  , "@typescript-eslint/restrict-plus-operands":                       ["error", { allowAny: false, allowBoolean: false, allowNullish: false, allowNumberAndString: false, allowRegExp: false }]
  , "@typescript-eslint/restrict-template-expressions":                "off"
  , "@typescript-eslint/strict-boolean-expressions":                   "error"
  , "@typescript-eslint/switch-exhaustiveness-check":                  "error"
  , "@typescript-eslint/triple-slash-reference":                       "error"
  , "@typescript-eslint/typedef":                                      ["off", { arrayDestructuring: false, arrowParameter: true, memberVariableDeclaration: true, objectDestructuring: false, parameter: true, propertyDeclaration: true, variableDeclaration: false, variableDeclarationIgnoreFunction: false }]
  , "@typescript-eslint/unbound-method":                               "error"
  , "@typescript-eslint/unified-signatures":                           "error"
  , "@typescript-eslint/use-unknown-in-catch-callback-variable":       "error"
  }
});
