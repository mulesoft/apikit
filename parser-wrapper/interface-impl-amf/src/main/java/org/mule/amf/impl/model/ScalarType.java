package org.mule.amf.impl.model;

import static org.mule.amf.impl.model.ScalarType.ScalarTypes.BOOLEAN_ID;
import static org.mule.amf.impl.model.ScalarType.ScalarTypes.DATE_ONLY_ID;
import static org.mule.amf.impl.model.ScalarType.ScalarTypes.DATE_TIME_ID;
import static org.mule.amf.impl.model.ScalarType.ScalarTypes.DATE_TIME_ONLY_ID;
import static org.mule.amf.impl.model.ScalarType.ScalarTypes.FLOAT_ID;
import static org.mule.amf.impl.model.ScalarType.ScalarTypes.INTEGER_ID;
import static org.mule.amf.impl.model.ScalarType.ScalarTypes.NUMBER_ID;
import static org.mule.amf.impl.model.ScalarType.ScalarTypes.STRING_ID;
import static org.mule.amf.impl.model.ScalarType.ScalarTypes.TIME_ID;

enum ScalarType {
  STRING(STRING_ID, "string"),
  BOOLEAN(BOOLEAN_ID, "boolean"),
  NUMBER(NUMBER_ID, "number"),
  FLOAT(FLOAT_ID, "float"),
  DATE_TIME_ONLY(DATE_TIME_ONLY_ID, "dateTimeOnly"),
  INTEGER(INTEGER_ID, "integer"),
  TIME(TIME_ID, "time"),
  DATE_TIME(DATE_TIME_ID, "dateTime"),
  DATE_ONLY(DATE_ONLY_ID, "date");

  private String id;
  private String name;

  ScalarType(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  static class ScalarTypes {

    static final String STRING_ID = "http://www.w3.org/2001/XMLSchema#string";
    static final String BOOLEAN_ID = "http://www.w3.org/2001/XMLSchema#boolean";
    static final String NUMBER_ID = "http://raml.org/vocabularies/shapes#number";
    static final String FLOAT_ID = "http://www.w3.org/2001/XMLSchema#float";
    static final String DATE_TIME_ONLY_ID = "http://raml.org/vocabularies/shapes#dateTimeOnly";
    static final String INTEGER_ID = "http://www.w3.org/2001/XMLSchema#integer";
    static final String TIME_ID = "http://www.w3.org/2001/XMLSchema#time";
    static final String DATE_TIME_ID = "http://www.w3.org/2001/XMLSchema#dateTime";
    static final String DATE_ONLY_ID = "http://www.w3.org/2001/XMLSchema#date";
  }
}
