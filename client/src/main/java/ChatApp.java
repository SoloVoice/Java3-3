import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

public class ChatApp extends JFrame {
    private ArrayList<JLabel> messages = new ArrayList<>();
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private JTextField loginField = new JTextField("", 14);
    private JTextField passField = new JTextField("", 14);
    private JButton submitButton = new JButton("Авторизоваться");
    private JTextField messageField = new JTextField();
    private JPanel chatWindow = new JPanel();
    private String userName;
    private JButton sendButton = new JButton("Отправить");
    private String loginNameforLogfile;
    private File logFile;
    private int showLogStartCounter = 1;


    public ChatApp() {

        setTitle("Chat App Window");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(200, 200, 500, 600);

        setLayout(new BorderLayout(10, 10));

        chatWindow.setLayout(new BoxLayout(chatWindow, BoxLayout.Y_AXIS));
        JScrollPane chatWindowScr = new JScrollPane(chatWindow);

        chatWindowScr.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        messageField.setHorizontalAlignment(JTextField.RIGHT);

        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMsg(messageField.getText());
//                if (userName == null) {
//                    setUserName();
//                } else {
//                    sendMsg();
//                }
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMsg(messageField.getText());
//                if (userName == null) {
//                    setUserName();
//                } else {
//                    sendMsg();
//                }
            }
        });

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendAuth();
            }
        });

        JPanel topAuthPanel = new JPanel();
        topAuthPanel.setLayout(new FlowLayout());
        add(topAuthPanel, BorderLayout.NORTH);
        topAuthPanel.add(loginField);
        topAuthPanel.add(passField);
        topAuthPanel.add(submitButton);

        JPanel bottomMessagePanel = new JPanel();
        bottomMessagePanel.setLayout(new BorderLayout());
        bottomMessagePanel.setPreferredSize(new Dimension(0, 30));

        add(bottomMessagePanel, BorderLayout.SOUTH);
        add(chatWindowScr, BorderLayout.CENTER);
        for (JLabel i : messages) {
            chatWindow.add(i);
        }
        bottomMessagePanel.add(sendButton, BorderLayout.EAST);
        bottomMessagePanel.add(messageField, BorderLayout.CENTER);

        setVisible(true);
    }

    public void connect() {
        try {
            socket = new Socket("localhost", 8389);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                while (true) {
                    try {
                        String msg = in.readUTF();
                        messages.add(new JLabel(msg));
                        createChatLog(msg);
                        chatLogging(msg);
                        for (JLabel i : messages) {
                            chatWindow.add(i);
                        }
                        setVisible(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
            messageField.setText("");
            messageField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public void setUserName() {
//        userName = messageField.getText();
//        messageField.setText("");
//        messageField.requestFocus();
//    }

    public void sendAuth() {
        loginNameforLogfile = loginField.getText();
        logFile = new File("client/src/main/java/", "/history_" + loginNameforLogfile + ".txt");
        connect();
        // /auth login1 pass1
        sendMsg("/auth " + loginField.getText() + " " + passField.getText());
        loginField.setText("");
        passField.setText("");
    }

    public void createChatLog(String msg) {
        try {
            if (msg.equals("/authok")) {
                if (logFile.exists()) {
                    showLast100();
                } else {
                    logFile.createNewFile();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void chatLogging(String msg) {
        try {
            FileOutputStream logOutStream = new FileOutputStream(logFile, true);
            try {
                byte[] outData = (msg + "\n").getBytes(StandardCharsets.UTF_8);
                logOutStream.write(outData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void showLast100() {
        ArrayList logArray = new ArrayList();
        try {
            FileReader fr = new FileReader(logFile);
            Scanner scan = new Scanner(fr);

            while (scan.hasNextLine()) {
                logArray.add(scan.nextLine());
            }

            try {
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println(logArray.size());
        System.out.println(logArray.size()-101);
        for (int i = (logArray.size()-101); i<logArray.size(); i++) {
            messages.add(new JLabel(String.valueOf(logArray.get(i))));
        }

    }
}

