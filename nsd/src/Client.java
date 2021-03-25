//This is a boundary client class. It will have the main method and it will delegate the activity to another class

public class Client{


    public static void main(String[] args)
    {
        // read command line arguments and set the parameters
        if (args.length != 4) {
            System.err.println(
                    "Usage: java Client <host name> <port number> <debug 0/1> <throughput R/L/H>");
            System.exit(1);
        }

        Parameters.SERVERHOST = args[0];
        Parameters.SERVERPORT = Integer.parseInt(args[1]);

        if(args[2].equals("1")) Parameters.DEBUG = true;


        if(args[3].equals("R")) Parameters.LOAD = Parameters.REGULAR;
        else if(args[3].equals("L")) Parameters.LOAD = Parameters.LOW;
        else if(args[3].equals("H")) Parameters.LOAD = Parameters.HIGH;


        //Print parameters
        Parameters.printAll();

        //delegate the execution
        new Executor().execute();


    }
}