package org.test.Server;


import org.test.Client.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**각 클라이언트 객체들을 관리하기 위한 핸들러 _
 * 클라이언트들이 연결될때 마다 해당 객체가 생성 및 관리*/
public class ClientHandler extends Thread {
    private Socket socket;

    /**메시지 출력을 위한 스트림
     * (서버->클라이언트 )*/
    private PrintWriter out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out = new PrintWriter(socket.getOutputStream(), true);

            String message;

            while ((message = in.readLine()) != null) { // 각 클라이언트들의 메시지 (
                System.out.println("클라이언트 메시지: " + message);
                Server.broadcast(message, this); // 받은 메시지를 다른 클라이언트들에게 전송
            }

        } catch (IOException e) {
            System.err.println("클라이언트 처리 중 오류 발생: " + e.getMessage());
        } finally {
            Server.removeClient(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}
