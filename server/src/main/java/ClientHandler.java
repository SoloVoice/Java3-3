import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    while (true) {
                        String str = in.readUTF();
                        // /auth login1 password1
                        String[] subStrings = str.split(" ");
                        if (subStrings.length >= 3 && subStrings[0].equals("/auth")) {
                            String nickFromDB = SQLHandler.getNickByLoginAndPassword(subStrings[1], subStrings[2]);
                            if (nickFromDB != null) {
                                sendMsg("/authok");
                                server.subscribe(this);
                                nickname = nickFromDB;
                                break;
                            }
                        }
                    }

                    while (true) {
                        String str = in.readUTF();
                        System.out.println("Сообщение от клиента: " + str);
                        if (str.startsWith("/end")) {
                            break;
                        } else if (str.startsWith("/w")) {
                            String[] msgDivide = str.split(" ");
                            String[] clearPrivateMsg = new String[msgDivide.length - 2];
                            System.arraycopy(msgDivide, 2, clearPrivateMsg, 0, msgDivide.length - 2);
                            String privateMsg = String.join(" ", clearPrivateMsg);
                            server.privatMsg(nickname, msgDivide[1], privateMsg);
                        } else if (str.startsWith("/cn")) {
                            String[] msgDivideForNewNick = str.split(" ");
                            String[] clearNewNick = new String[msgDivideForNewNick.length - 1];
                            System.arraycopy(msgDivideForNewNick, 1, clearNewNick, 0, msgDivideForNewNick.length - 1);
                            String newNick = String.join(" ", clearNewNick);
                            String newNickname = SQLHandler.changeNickInBD(newNick, nickname);
                            if (nickname.equals(newNickname)) {
                                server.privatMsg(nickname, "Server message: ", "Невозможно изменить ник на "+newNick+"");
                            } else {
                                server.privatMsg(nickname, "Server message: ", "Ваш ник изменен на "+newNick+"");
                            }
                            nickname = newNickname;
                        } else {
                            server.broadcastMsg(nickname + ": " + str);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    server.unsubscribe(this);
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }
}
