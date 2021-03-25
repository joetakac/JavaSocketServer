import org.json.simple.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Channel {

    String clientID;
    String name;

    private List<JSONObject> messageList = new ArrayList<JSONObject>();
    private List<String> Subscriptions = new ArrayList<String>();

    public Channel(String clientID, String name) {
        this.clientID = clientID;
        this.name = name;
        System.out.println("Channel Created.");
    }

    public String getClientID() {
        return clientID;
    }

    public String getName() {
        return name;
    }

    public void enterMessages(JSONObject obj) {
        messageList.add(obj);
    }

    public void addSubscription(String name) {
        Subscriptions.add(name);
    }

    public void removeSubscription(String name) {
        Subscriptions.remove(name);
    }

    public List<JSONObject> getMessages() {
        return messageList;
    }

    public List<String> getSubscriptions() {
        return Subscriptions;
    }

}
