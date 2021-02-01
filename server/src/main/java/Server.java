import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
    private Vector<ClientHandler> clients;

    public Server() {
        try {
            SQLHandler.connect();
            ServerSocket serverSocket = new ServerSocket(8389);
            clients = new Vector<>();
            while (true) {
                System.out.println("Ждем подключения клиента");
                Socket socket = serverSocket.accept();
                ClientHandler c = new ClientHandler(this, socket);
                for (ClientHandler i : clients) {
                    System.out.println(i.getNickname());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            SQLHandler.disconnect();
        }
    }

    public void subscribe(ClientHandler client) {
        clients.add(client);
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
    }

    public void broadcastMsg(String msg) {
        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }

    public void privatMsg(String nick, String name, String msg) {
        for (ClientHandler c : clients) {
            if (c.getNickname().equals(name)) {
                c.sendMsg(nick+": "+msg);
            }
        }
        for (ClientHandler c : clients) {
            if (c.getNickname().equals(nick)) {
                c.sendMsg(nick+": "+msg);
            }
        }
    }
}
