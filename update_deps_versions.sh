#!/usr/bin/env bash

set -o nounset

ramlParserV1Version="$1"
ramlParserV2Version="$2"
amfParserVersion="$3"

updatePropertiesVersion() {
  VERSION_TO_PROPERTY="$1"
  POM_PROPERTY_PATH="$2"

  # PROPERTIES argument should be passed as a literal "arrayName[@]" without $ because here using the ! it is double expanded
  # to obtiain the values and declare again the array.
  PROPERTIES=("${!3}")

  echo "Updating deps in pom: $POM_PROPERTY_PATH"

  for PROPERTY_NAME in "${PROPERTIES[@]}"
  do

      perl -0777 -i -pe "s/(<properties>.*<$PROPERTY_NAME)(.*)(\/$PROPERTY_NAME>.*<\/properties>)/\${1}>$VERSION_TO_PROPERTY<\${3}/s" "$POM_PROPERTY_PATH"
      echo "- Updating property '$PROPERTY_NAME' version to $VERSION_TO_PROPERTY"

  done
}


# Raml Parser V1 Version Property
propertiesRamlParserV1=(ramlParserV1Version)
VERSION_TO=$ramlParserV1Version
updatePropertiesVersion "$VERSION_TO" pom.xml propertiesRamlParserV1[@]

# Raml Parser V2 Version Property
propertiesRamlParserV2=(ramlParserV2Version)
VERSION_TO=$ramlParserV2Version
updatePropertiesVersion "$VERSION_TO" pom.xml propertiesRamlParserV2[@]

# AMF Parser Version Property
propertiesAmfParser=(amfVersion)
VERSION_TO=$amfParserVersion
updatePropertiesVersion "$VERSION_TO" pom.xml propertiesAmfParser[@]
