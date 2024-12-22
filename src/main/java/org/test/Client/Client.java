package org.test.Client;

import org.test.Server.Server;

import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        String host = "127.0.0.1"; // 서버 IP 주소 (로컬)
        int port = 12345; // 서버 포트 번호

        //try-with-resources _ try 블록이 끝나거나 예외가 발생하더라도 자동으로 자원을 닫아주는(해제하는) 역할
        try (Socket socket = new Socket(host, port);
             // 소켓 생성 _ 해당 소켓을 통해 송수신
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             // 입력 스트림 _ 서버가 전송한 데이터를 byte -> character _-> Line 단위로 읽기 위함

             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             // 출력 스트림 _  서버로 데이터를 byte로 _ 자동으로 버퍼 지우기 true

             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {
            //사용장 입력 스트림

            System.out.println("서버에 연결되었습니다.");

            // 서버로부터 메시지를 받는 스레드
            Thread receiver = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        /* 메시지를 기다리는 코드.
                        메시지가 오면 serverMessage에 저장
                        */
                        System.out.println("메시지 수신: " + serverMessage);
                    }
                } catch (IOException e) {
                    System.err.println("서버로부터 메시지 수신 중 오류 발생: " + e.getMessage());
                }
            });
            receiver.start();



            // 사용자 입력을 서버로 전송
            String userMessage;
            while ((userMessage = userInput.readLine()) != null) {
                out.println(userMessage); // 서버로 메시지 전송
                if (userMessage.equalsIgnoreCase("끝")) {
                    System.out.println("연결 종료");
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}//