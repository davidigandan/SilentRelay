package silentrelay;
public class Client {

    // Initialise host, port and uuid
    private static String host;
    private static int port;
    private static String uuid;


    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java Client <host> <port> <uuid>");
            System.out.println("There's something wrong with the number of arguments. Exiting...");
            System.exit(1);
        }

        host = args[0];
        port = Integer.parseInt(args[1]);
        uuid = args[2];



        
    }
}
