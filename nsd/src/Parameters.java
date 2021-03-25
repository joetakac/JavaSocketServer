//This is to store all parameters to be used as constant
public class Parameters
{

    public static String SERVERHOST = null;
    public static int SERVERPORT = 12345;
    public static boolean DEBUG = false;
    public static String LOAD = null;

    public static String LOW = "Low";
    public static String HIGH = "High";
    public static String REGULAR = "Routine";

    public static final String OPENCLASSNAME = "OpenRequest";
    public static final String REQUESTCLASSNAME = "PublishRequest";
    public static final String SUBSCRIBECLASSNAME = "SubscribeRequest";
    public static final String UNSUBSCRIBECLASSNAME = "UnsubscribeRequest";
    public static final String GETCLASSNAME = "GetRequest";
    public static final String MESSAGECLASSNAME = "Message";

    public static String SUCCESSCLASSNAME = "SuccessResponse";
    public static String ERRORCLASSNAME = "ErrorResponse";
    public static String ERRORNAME = "error";



    public static void printAll()
    {
        if (!DEBUG) System.out.println("I will not print as you do not want me to ");

        else
        {
            System.out.println("Server is at "+ SERVERHOST + " listening to "+ SERVERPORT);
            System.out.println(" Throughput is "+ LOAD);
            System.out.println( " I will show you the proceedigns ");
        }
    }

    public static void debug(String message)
    {
        if (DEBUG) System.out.println(message);
    }
}