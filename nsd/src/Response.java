import org.json.simple.*;

import java.io.*;

public class Response {

    public JSONObject successResponse() {
        JSONObject obj = new JSONObject();
        obj.put("_class", Parameters.SUCCESSCLASSNAME);
        return obj;
    }

    public JSONObject errorResponse(String error) {
        JSONObject obj = new JSONObject();
        obj.put("_class", Parameters.ERRORCLASSNAME);
        obj.put("error", error);
        return obj;
    }


}
