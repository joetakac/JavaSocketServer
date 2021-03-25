//This will execute the runs and will contain all logic
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.simple.*;  // required for JSON encoding and decoding


public class Executor
{


    //This is the entry method for this class. this will delegate to the appropriate methods
    public void execute()
    {
        if (Parameters.DEBUG) System.out.println("Connection Success.");


        if (Parameters.LOAD.equals(Parameters.REGULAR)) routineRun();
        else if (Parameters.LOAD.equals(Parameters.LOW)) lowThroughputRun();
        else if (Parameters.LOAD.equals(Parameters.HIGH)) highThroughputRun();



    }

    //This will just run one server session
    private void routineRun()
    {
        Parameters.debug("Regular Session");
        MessageSender sender = new MessageSender(1,0,1);
        sender.start();


    }

    //This will run 3 sessions from 3 different clients in parallel. Situation is 2 or 3 clients publish and read 10 messages. The clients are rate-limited to at most 1 request per second
    private void lowThroughputRun()
    {
        Parameters.debug("Low Throughput Session");

        //create 3 threads
        MessageSender sender1 = new MessageSender(10,1000,1);
        MessageSender sender2 = new MessageSender(10,1000,2);
        MessageSender sender3 = new MessageSender(10,1000,3);

        //run 3 threads
        sender1.start();
        sender2.start();
        sender3.start();


    }

    //This will run 10 sessions from 10 different clients in parallel. Situation is 10 clients publish and read at least 1000 messages at an unlimited rate (i.e. they issues request as fast as they can).
    private void highThroughputRun()
    {
        Parameters.debug("High Throughput Session");

        //create 10 threads
        MessageSender sender1 = new MessageSender(1000,0,1);
        MessageSender sender2 = new MessageSender(1000,0,2);
        MessageSender sender3 = new MessageSender(1000,0,3);
        MessageSender sender4 = new MessageSender(1000,0,4);
        MessageSender sender5 = new MessageSender(1000,0,5);
        MessageSender sender6 = new MessageSender(1000,0,6);
        MessageSender sender7 = new MessageSender(1000,0,7);
        MessageSender sender8 = new MessageSender(1000,0,8);
        MessageSender sender9 = new MessageSender(1000,0,9);
        MessageSender sender10 = new MessageSender(1000,0,10);

        //run 10 threads
        sender1.start();
        sender2.start();
        sender3.start();
        sender4.start();
        sender5.start();
        sender6.start();
        sender7.start();
        sender8.start();
        sender9.start();
        sender10.start();
    }


}
//This class will manage the actual message sending and will be instantiated many times based on the load requirement.
class MessageSender extends Thread
{
    private int numberOfMessage;
    private int delay;
    private int identifier;



    public MessageSender(int number, int period, int id)
    {
        numberOfMessage = number;
        delay = period;
        identifier = id;
    }



    public void run()
    {
        try (
                Socket messageSocket = new Socket(Parameters.SERVERHOST,Parameters.SERVERPORT);
                PrintWriter out = new PrintWriter(messageSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(messageSocket.getInputStream()));
        )
        {
            // open a channel
            String openRequest = getOpenRequest("NSD Client"+identifier);

            out.println(openRequest);
            // read success or error
            readResult(in);

            //subscribe
            String subscribeRequest = getSubscribeRequest("NSD Client"+identifier,"NSD Client"+identifier);
            //Parameters.debug("Sending "+subscribeRequest);
            out.println(subscribeRequest);
            // read success or error
            readResult(in);

            // publish message(s)
            String publishRequest;
            for (int i=1; i<=numberOfMessage; i++)
            {
                // wait if applicable
                if (delay > 0)
                {
                    try { Thread.sleep(delay); } catch (InterruptedException e) { Parameters.debug("Not expecting any exception here"); }
                }

                publishRequest = getPublishRequest("NSD Client"+identifier,"NSD Client"+identifier, "Hello Message "+i,i);
                Parameters.debug("Sending "+publishRequest);
                out.println(publishRequest);
                // read success or error
                readResult(in);

            }

            // get message to get all messages
            String getRequest = getGetRequest("NSD Client"+identifier,0);
            //Parameters.debug("Sending "+getRequest);
            out.println(getRequest);
            // read messages
            readMessages(in);

            //unsubscribe
            String unsubscribeRequest = getUnsubscribeRequest("NSD Client"+identifier,"NSD Client"+identifier);
            //out.println(unsubscribeRequest);
            // read success or error
            readResult(in);



        } catch (UnknownHostException e)
        {
            System.err.println("Don't know about host " + Parameters.SERVERHOST);
            System.exit(1);
        }
        catch (IOException e)
        {
            System.err.println("Couldn't get I/O for the connection to " +	Parameters.SERVERHOST);
            System.exit(1);
        }
        catch (Exception e)
        {
            System.err.println("improper behaviour from " +	Parameters.SERVERHOST);
            System.exit(1);
        }

    }

    //Read the messages from a request. Does not do anything except printing if the result is ok else throws exception leading to system exit
    private void readMessages(BufferedReader in) throws Exception
    {
        String result = in.readLine();
        //check for status

        Parameters.debug("Read Result is "+result);

    }

    //Read the result from a request. Does not do anything if the result is ok else throws exception leading to system exit
    private void readResult(BufferedReader in) throws Exception
    {
        String result = in.readLine();

        if (result == null) throw new Exception("unhandled error");

        Parameters.debug("got back "+result);

        byte[] decodedBytes =  Base64.getDecoder().decode(result);
        String decodedText = new String(decodedBytes);
        Parameters.debug("Decoded Request is "+decodedText);

        // parse raw response to JSON value
        Object json = JSONValue.parse(decodedText);

        //check for status

        JSONObject obj = (JSONObject)json;

        Parameters.debug("The class is "+obj.get("_class"));

        // check for _class field matching class name
        if (Parameters.SUCCESSCLASSNAME.equals(obj.get("_class")))
        {
            Parameters.debug("Response is ok");
        }
        else if (Parameters.ERRORCLASSNAME.equals(obj.get("_class")))
        {
            String error = (String)obj.get(Parameters.ERRORNAME);
            Parameters.debug("Error is "+error);
            throw new Exception("error");
        }
        else
        {

            throw new Exception("unhandled error");
        }

    }

    //This is the format. {"_class":"OpenRequest", "identity":"Alice"}.
    @SuppressWarnings("unchecked")
    private String getOpenRequest(String channel)
    {
        //
        // create JSON Object
        JSONObject obj = new JSONObject();
        obj.put("_class",Parameters.OPENCLASSNAME);
        obj.put("identity", channel);


        byte[] encodedBytes =  Base64.getEncoder().encode(obj.toJSONString().getBytes());
        String encodedText = new String(encodedBytes);
        return encodedText;
    }

    //This is the format. {"_class":"PublishRequest", "identity":"Alice", "message":{"_class":"Message", "from":"Bob", "when":0, "body":"Hello again!"}}.
    @SuppressWarnings("unchecked")
    private String getPublishRequest(String channel,String sender, String message, int time)
    {
        // create message Object first
        JSONObject msg = new JSONObject();
        msg.put("_class",Parameters.MESSAGECLASSNAME);
        msg.put("from", sender);
        msg.put("when", time);
        msg.put("body", message);

        String messageString = msg.toJSONString();

        // create request Object
        JSONObject obj = new JSONObject();
        obj.put("_class",Parameters.REQUESTCLASSNAME);
        obj.put("identity", channel);
        obj.put("message", msg);


        byte[] encodedBytes =  Base64.getEncoder().encode(obj.toJSONString().getBytes());
        String encodedText = new String(encodedBytes);
        return encodedText;
    }

    //This is the format. {"_class":"SubscribeRequest", "identity":"Alice", "channel":"Bob"}.
    @SuppressWarnings("unchecked")
    private String getSubscribeRequest(String channel,String user)
    {


        // create request Object
        JSONObject obj = new JSONObject();
        obj.put("_class",Parameters.SUBSCRIBECLASSNAME);
        obj.put("identity", user);
        obj.put("channel", channel);

        //return obj.toJSONString();
        byte[] encodedBytes =  Base64.getEncoder().encode(obj.toJSONString().getBytes());
        String encodedText = new String(encodedBytes);
        return encodedText;
    }

    //This is the format.{"_class":"UnsubscribeRequest", "identity":"Alice", "channel":"Bob"}
    @SuppressWarnings("unchecked")
    private String getUnsubscribeRequest(String channel,String user)
    {


        // create request Object
        JSONObject obj = new JSONObject();
        obj.put("_class",Parameters.UNSUBSCRIBECLASSNAME);
        obj.put("identity", user);
        obj.put("channel", channel);


        byte[] encodedBytes =  Base64.getEncoder().encode(obj.toJSONString().getBytes());
        String encodedText = new String(encodedBytes);
        return encodedText;
    }

    //This is the format {"_class":"GetRequest", "identity":"Alice", "after":42}
    @SuppressWarnings("unchecked")
    private String getGetRequest(String channel,int time)
    {


        // create request Object
        JSONObject obj = new JSONObject();
        obj.put("_class",Parameters.GETCLASSNAME);
        obj.put("identity", channel);
        obj.put("after", time);



        byte[] encodedBytes =  Base64.getEncoder().encode(obj.toJSONString().getBytes());
        String encodedText = new String(encodedBytes);
        return encodedText;
    }
}