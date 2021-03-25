import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.net.*;
import java.io.*;
import java.util.*;


public class ClientHandler extends Thread {
    private Socket client;
    private PrintWriter toClient;
    private BufferedReader fromClient;
    private String ClientId;

    // List of Users
    private static List<String> users = new ArrayList<String>();

    //List of Channels
    private static List<Channel> channels = new ArrayList<Channel>();

    //File to be written to for persistence
    private static File filepath = new File("./lib/messages.txt");

    //File writer for appending to the file above
    FileWriter fileWriter = null;

    //
    public void readMessages() {
        if (filepath.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
                String line;
                System.out.println("Reading messages from last session.");
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (FileNotFoundException fe) {
                System.out.println("File not found.");
            } catch (IOException io) {
                System.out.println("Something went wrong with IO stream.");
                io.printStackTrace();
            }
            System.out.println("End of last sessions messages.");
        } else {
            System.out.println("No messages to read.");
        }
    }


    //constructor
    public ClientHandler(Socket socket, String clientnumber) throws IOException {
        client = socket;
        ClientId = clientnumber;
        toClient = new PrintWriter(client.getOutputStream(), true);
        fromClient = new BufferedReader(
                new InputStreamReader(client.getInputStream()));
    }

    public void run() {
        try {
            readMessages(); // attempt to read messages from file if server crashed

            int timestamp = 0; //every time a publish request is made add 1 to the timestamp

            String clientResponse;
            while ((clientResponse = fromClient.readLine()) != null) {

                // logging request to server console
                System.out.println(clientResponse);

                // decode request
                byte[] decodedBytes = Base64.getDecoder().decode(clientResponse);
                String decodedText = new String(decodedBytes);//decode

                Object json = JSONValue.parse(decodedText);
                JSONObject obj = (JSONObject) json; //parse

                String inputClass = (String) obj.get("_class");//extract class

                switch (inputClass) {

                    case Parameters.OPENCLASSNAME: //Open Request

                        String openName = (String) obj.get("identity");
                        if (!users.contains(openName)) {
                            synchronized (ClientHandler.class) {
                                users.add(openName);//check if user has already submitted an open request
                                System.out.println("User " + openName + " added.");
                            }
                            Channel newChannel = new Channel(ClientId, openName);//create channel for user
                            channels.add(newChannel);//add channel to shared list of channels
                        } else {
                            JSONObject publishError = new Response().errorResponse("User already exists.");
                        }

                        //create and encode success response
                        JSONObject openSuccess = new Response().successResponse();
                        byte[] encodedBytes = Base64.getEncoder().encode(openSuccess.toJSONString().getBytes());
                        String encodedText = new String(encodedBytes);

                        //return response
                        toClient.println(encodedText);
                        System.out.println("Open Request Complete.");

                        break;

                    case Parameters.REQUESTCLASSNAME: //Publish Request

                        String publishName = (String) obj.get("identity");
                        JSONObject publishMessage = (JSONObject) obj.get("message");
                        timestamp++;

                        //look through the channel list to see if the name matches, if so enter message to channel
                        for (Iterator<Channel> iter = channels.iterator(); iter.hasNext(); ) {
                            Channel channel = iter.next();
                            if (channel.getName().equals(publishName)) {
                                synchronized (ClientHandler.class) {
                                    if (publishMessage.values().size() > 999) {
                                        JSONObject publishError = new Response().errorResponse("Message too big.");
                                    } else {
                                        publishMessage.replace("when", timestamp);
                                        channel.enterMessages(publishMessage);
                                        // Constructing a file writer to append/write data for persistence upon a server restart/crash
                                        try {
                                            fileWriter = new FileWriter(filepath, true);
                                            fileWriter.write(publishMessage.toJSONString());
                                            fileWriter.write(System.getProperty("line.separator"));
                                        } catch (IOException io) {
                                            io.printStackTrace();
                                        } finally {
                                            fileWriter.close();
                                        }

                                        System.out.println(publishMessage);
                                        System.out.println("Message Sent.");
                                    }
                                }
                            }
                        }


                        // response acknowledging the publish request
                        JSONObject publishSuccess = new Response().successResponse();
                        byte[] encodedBytes1 = Base64.getEncoder().encode(publishSuccess.toJSONString().getBytes());
                        String encodedText1 = new String(encodedBytes1);

                        //return response
                        toClient.println(encodedText1);
                        System.out.println("Posted Successfully.");

                        break;

                    case Parameters.SUBSCRIBECLASSNAME: //Subscribe Request

                        String userName = (String) obj.get("identity");
                        String subscribeName = (String) obj.get("channel");

                        for (Iterator<Channel> iter = channels.iterator(); iter.hasNext(); ) {
                            Channel channel = iter.next();
                            if (channel.getName().equals(userName)) {
                                synchronized (ClientHandler.class) {
                                    List<String> subs = channel.getSubscriptions();
                                    for (String channel2 : subs) {
                                        if (subs.contains(subscribeName)) {
                                            System.out.println("You are already subscribed to this channel.");
                                        } else {
                                            channel.addSubscription(subscribeName);
                                            System.out.println("You have subscribed to " + subscribeName);
                                        }
                                    }
                                }
                            }
                        }

                        // response acknowledging the subscribe request
                        JSONObject subSuccess = new Response().successResponse();
                        byte[] encodedBytes2 = Base64.getEncoder().encode(subSuccess.toJSONString().getBytes());
                        String encodedText2 = new String(encodedBytes2);

                        //return response
                        toClient.println(encodedText2);
                        System.out.println("Subscribed Successfully.");

                        break;

                    case Parameters.UNSUBSCRIBECLASSNAME://Unsubscribe Request

                        String userName1 = (String) obj.get("identity");
                        String unsubscribeName = (String) obj.get("channel");

                        for (Iterator<Channel> iter = channels.iterator(); iter.hasNext(); ) {
                            Channel channel = iter.next();
                            if (channel.getName().equals(userName1)) {
                                synchronized (ClientHandler.class) {
                                    List<String> subs = channel.getSubscriptions();
                                    for (String channel2 : subs) {
                                        if (subs.contains(unsubscribeName)) {
                                            System.out.println("You are already subscribed to this channel.");
                                        } else {
                                            channel.addSubscription(unsubscribeName);
                                            System.out.println("You have subscribed to " + unsubscribeName);
                                        }
                                    }
                                }
                            }
                        }

                        // response acknowledging the unsubscribe request
                        JSONObject unsubSuccess = new Response().successResponse();
                        byte[] encodedBytes3 = Base64.getEncoder().encode(unsubSuccess.toJSONString().getBytes());
                        String encodedText3 = new String(encodedBytes3);

                        //return response
                        toClient.println(encodedText3);
                        System.out.println("Unsubscribed Successfully.");

                        break;

                    case Parameters.GETCLASSNAME://Get Messages Request
                        String userName3 = (String) obj.get("identity");
                        long ignored = (long) obj.get("after");
                        int afterInt = (int) ignored;
                        System.out.println("Wanting messages after " + afterInt);

                        synchronized (ClientHandler.class) {
                            //go through every channels messages and retrieve "after"
                            for (Iterator<Channel> iter = channels.iterator(); iter.hasNext(); ) {
                                Channel channel = iter.next();
                                if (channel.getName().equals(userName3)) {
                                    List<String> subsList = channel.getSubscriptions(); //channels subscriptions
                                    List<JSONObject> messageList = channel.getMessages(); //channels messages
                                    for (Iterator<JSONObject> iter2 = messageList.iterator(); iter2.hasNext(); ) {
                                        JSONObject msg = iter2.next(); //the message
                                        long ignored2 = (long) obj.get("after");
                                        int afterInt2 = (int) ignored2;

                                        if (afterInt2 >= afterInt) { //if after in request is greater than or equal to after in message, print the message
                                            System.out.println("Got Message " + msg);
                                        }
                                    }
                                }
                            }
                        }

                        // response acknowledging the get request
                        JSONObject getSuccess = new Response().successResponse();
                        byte[] encodedBytes4 = Base64.getEncoder().encode(getSuccess.toJSONString().getBytes());
                        String encodedText4 = new String(encodedBytes4);

                        //return response
                        toClient.println(encodedText4);
                        System.out.println("Get Request Completed Successfully.");

                        break;

                    default:
                        System.out.println("ILLEGAL REQUEST");
                }
            }
        } catch (IOException e) {
            System.out.println("Exception while connected");
            System.out.println(e.getMessage());
        }
    }
}