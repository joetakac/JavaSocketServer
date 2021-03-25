import org.json.simple.*;

import java.io.*;

public class Message {
    private String from;
    private int when;
    private String body;

    public Message() {
        JSONObject obj = new JSONObject();
        obj.put("_class", Parameters.MESSAGECLASSNAME);
        obj.put("from", from);
        obj.put("when", when);
        obj.put("body", body);
    }

    public String getFrom() {
        return from;
    }

    public int getWhen() {
        return when;
    }

    public String getBody() {
        return body;
    }


}
