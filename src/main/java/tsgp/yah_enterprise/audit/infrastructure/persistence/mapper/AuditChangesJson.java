package tsgp.yah_enterprise.audit.infrastructure.persistence.mapper;

import tsgp.yah_enterprise.audit.domain.valueobject.AuditChange;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes the JSON array of changes: [{"fieldName":"..","oldValue":".."|null,"newValue":".."|null}].
 * Small and self-contained on purpose: the shape is fixed, so no JSON library has to guess a mapping.
 */
public final class AuditChangesJson {

    private AuditChangesJson() { }

    /** Null when there are no changes (the column stays NULL). */
    public static String write(List<AuditChange> changes) {
        if (changes == null || changes.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < changes.size(); i++) {
            AuditChange c = changes.get(i);
            if (i > 0) sb.append(',');
            sb.append("{\"fieldName\":");
            string(sb, c.fieldName());
            sb.append(",\"oldValue\":");
            string(sb, c.oldValue());
            sb.append(",\"newValue\":");
            string(sb, c.newValue());
            sb.append('}');
        }
        return sb.append(']').toString();
    }

    public static List<AuditChange> read(String json) {
        List<AuditChange> out = new ArrayList<>();
        if (json == null || json.isBlank()) {
            return out;
        }
        Parser p = new Parser(json);
        p.expect('[');
        p.skip();
        if (p.peek() == ']') {
            return out;
        }
        while (true) {
            p.expect('{');
            String field = null;
            String oldValue = null;
            String newValue = null;
            while (true) {
                String key = p.readString();
                p.expect(':');
                String value = p.readStringOrNull();
                switch (key) {
                    case "fieldName" -> field = value;
                    case "oldValue" -> oldValue = value;
                    case "newValue" -> newValue = value;
                    default -> { /* unknown key: ignored */ }
                }
                p.skip();
                if (p.peek() == ',') {
                    p.expect(',');
                    continue;
                }
                break;
            }
            p.expect('}');
            out.add(new AuditChange(field, oldValue, newValue));
            p.skip();
            if (p.peek() == ',') {
                p.expect(',');
                continue;
            }
            break;
        }
        p.expect(']');
        return out;
    }

    private static void string(StringBuilder sb, String value) {
        if (value == null) {
            sb.append("null");
            return;
        }
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (ch < 0x20) {
                        sb.append(String.format("\\u%04x", (int) ch));
                    } else {
                        sb.append(ch);
                    }
                }
            }
        }
        sb.append('"');
    }

    private static final class Parser {
        private final String s;
        private int pos = 0;

        Parser(String s) {
            this.s = s;
        }

        void skip() {
            while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++;
        }

        char peek() {
            skip();
            if (pos >= s.length()) throw new IllegalArgumentException("Unexpected end of the changes JSON");
            return s.charAt(pos);
        }

        void expect(char c) {
            if (peek() != c) {
                throw new IllegalArgumentException("Expected '" + c + "' at " + pos + " in the changes JSON");
            }
            pos++;
        }

        String readStringOrNull() {
            if (peek() == 'n') {
                if (!s.startsWith("null", pos)) throw new IllegalArgumentException("Bad value at " + pos);
                pos += 4;
                return null;
            }
            return readString();
        }

        String readString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                if (pos >= s.length()) throw new IllegalArgumentException("Unterminated string in the changes JSON");
                char ch = s.charAt(pos++);
                if (ch == '"') return sb.toString();
                if (ch != '\\') {
                    sb.append(ch);
                    continue;
                }
                char esc = s.charAt(pos++);
                switch (esc) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        sb.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16));
                        pos += 4;
                    }
                    default -> throw new IllegalArgumentException("Bad escape at " + pos);
                }
            }
        }
    }
}
