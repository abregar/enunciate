package org.codehaus.enunciate.modules.docs;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.node.ValueNode;

import java.io.IOException;

/**
 * This singleton value class is used to contain a raw value (for documentation purposes).
 */
public class RawValueNode extends ValueNode {

  private final String rawValue;

  public RawValueNode(String rawValue) {
    this.rawValue = rawValue;
  }

  @Override
  public boolean isNull() {
    return false;
  }

  public String getValueAsText() {
    return asText();
  }

  @Override
  public String asText() {
    return this.rawValue;
  }

  @Override
  public final void serialize(JsonGenerator jg, SerializerProvider provider)
    throws IOException, JsonProcessingException {
    jg.writeRawValue(this.rawValue);
  }

  public boolean equals(Object o) {
    return (o == this);
  }

  @Override
  public JsonToken asToken() {
    return JsonToken.VALUE_EMBEDDED_OBJECT;
  }
}