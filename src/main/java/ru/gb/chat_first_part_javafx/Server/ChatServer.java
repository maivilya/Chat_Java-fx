package ru.gb.chat_first_part_javafx.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    private final List<ClientHandler> clients;
    private final AuthService authService;

    public ChatServer() {
        this.clients = new ArrayList<>();
        authService = new InMemoryAuthService();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            while (true) {
                System.out.println("ожидаю подключения . . .");
                final Socket socket = serverSocket.accept();
                new ClientHandler(socket, this, authService);
                System.out.println("клиент подключился.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void broadcastForOne(String nick, String message){
        for (ClientHandler client : clients) {
            if (client.getNickname().equals(nick)){
                client.sendMessage(message);
            }
        }
    }

    public void subscribe(ClientHandler client) {
        clients.add(client);
    }

    public boolean isNickBusy(String nickname) {
        for (ClientHandler client : clients) {
            if (nickname.equals(client.getNickname())) {
                return true;
            }
        }
        return false;
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
    }
}
