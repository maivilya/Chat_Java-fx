package ru.gb.chat_first_part_javafx.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

    private Socket socket;
    private ChatServer server;
    private AuthService authService;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;

    public ClientHandler(Socket socket, ChatServer server, AuthService authService) {
        try {
            this.socket = socket;
            this.server = server;
            this.authService = authService;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    authenticate();
                    readMessages();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void authenticate() {
        while (true) {
            try {
                final String message = in.readUTF();
                if (message.startsWith("/auth")) {
                    final String[] split = message.split("\\p{Blank}+");
                    final String login = split[1];
                    final String password = split[2];
                    final String nickname = authService.getNickByLoginAndPassword(login, password);
                    if (nickname != null) {
                        if (server.isNickBusy(nickname)) {
                            sendMessage("такой пользователь уже авторизован");
                            continue;
                        }
                        sendMessage("/authok " + nickname);
                        this.nickname = nickname;
                        server.broadcast("пользователь " + nickname + " подключился к чату");
                        server.subscribe(this);
                        break;
                    } else {
                        sendMessage("пароль или логин неверные");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeConnection() {
        sendMessage("/end");
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            server.unsubscribe(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    private void readMessages() {
        while (true) {
            try {
                final String message = in.readUTF();
                if ("/end".equals(message)) {
                    break;
                }
                server.broadcast(nickname + ": " + message);
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }

    public String getNickname() {
        return nickname;
    }
}
