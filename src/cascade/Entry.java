package cascade;

import cascade.core.CascadeServer;
import protocol.connection.AuthenticationType;

public class Entry {

    public static void main(String[] args) {

        // entry point. parse input.
        if (args.length >= 2) {
            // first parse the port.
            try {
                int port = Integer.parseInt(args[0]);
                AuthenticationType authenticationType = AuthenticationType.valueOf(args[1].toUpperCase());

                if (args.length >= 3) {
                    CascadeServer.start(port, authenticationType, args[2]);
                    Runtime.getRuntime().addShutdownHook(new Thread(CascadeServer::stop));
                } else {
                    CascadeServer.start(port, authenticationType);
                    Runtime.getRuntime().addShutdownHook(new Thread(CascadeServer::stop));
                }

            } catch (IllegalArgumentException exception) {
                System.out.println("Invalid argument! <port> <auth-type(basic, password, none)> <password>(OPTIONAL)");
            }
        } else {
            System.out.println("Invalid argument! <port> <auth-type(basic, password, none)> <password>(OPTIONAL)");
        }

    }

}
