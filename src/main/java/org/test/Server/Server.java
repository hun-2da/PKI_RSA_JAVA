package org.test.Server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class Server {
    private static int ClientCount = 0;
    private static final int PORT = 12345;

    /**client List*/
    private static final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    //  client handler 를 관리할 리스트 _ 단일 스레드만 접근할 수 있게끔 안전하게 관리

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("서버 실행 중. 포트: " + PORT);

            for (ClientCount=0;ClientCount<2;ClientCount++) {
                // 반복 client를 대기 후 추가 _ 핸들러 객체에게 socket 이후 핸들러 객체를 List로 관리

                Socket clientSocket = serverSocket.accept();
                System.out.println("클라이언트 연결됨: " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                clientHandler.start(); // 각 클라이언트를 별도 스레드에서 처리

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**메시지를 모든 클라이언트에게 브로드캐스트*/
    public static void broadcast(String message, ClientHandler sender) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != sender) { // 메시지를 보낸 클라이언트를 제외하고 전송
                    client.sendMessage(message);
                }
            }
        }
    }

    // 클라이언트 제거
    public static void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        System.out.println("클라이언트 연결 해제");
        //ClientCount--;
    }

}
