package cn.iocoder.yudao.server.quote;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import java.io.IOException;

/** 数量必须是真正的整数；拒绝把 1.9 悄悄截成 1，避免少报数量。 */
public final class StrictQuantityDeserializer extends JsonDeserializer<Integer> {
    @Override public Integer deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (!parser.hasToken(JsonToken.VALUE_NUMBER_INT))
            throw com.fasterxml.jackson.databind.exc.InvalidFormatException.from(parser, "数量必须为整数", parser.getText(), Integer.class);
        return parser.getIntValue();
    }
}
