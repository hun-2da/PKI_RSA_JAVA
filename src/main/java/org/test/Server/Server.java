package org.test.Server;


import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.test.SecurityDirectory.Certificate.CertificateGenerator;
import org.test.SecurityDirectory.RSA.CryptoHandler;
import org.test.SecurityDirectory.RSA.KeyGeneration;
import org.test.SecurityDirectory.RSA.SignatureManager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.*;


public class Server {

    private static  KeyGeneration ServerKey = null;
    private static CryptoHandler cryptoHandler = null;
    private static SignatureManager signatureManager = null;

    static{
        Security.addProvider(new BouncyCastleProvider());
        ServerKey = new KeyGeneration();
        cryptoHandler = new CryptoHandler();
        signatureManager = new SignatureManager();

    }

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

            Thread.sleep(3000);
            sendCertificate();




        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**클라이언트에 보내기전 서명*/
    public static void ClientKeySignature(byte[] Key,ClientHandler sender){
        byte[] signature_key = signatureManager.generateSignature(Key,ServerKey.getUserPrivateKey());
        sender.transferKey(Key,signature_key);
    }


//    /**메시지를 모든 클라이언트에게 브로드캐스트*/
//    public static void broadcast(String message, ClientHandler sender) {
//        synchronized (clients) {
//            for (ClientHandler client : clients) {
//                if (client != sender) { // 메시지를 보낸 클라이언트를 제외하고 전송
//                    client.sendMessage(message);
//                }
//            }
//        }
//    }

    /** 메시지를 모든 클라이언트에게 브로드캐스트 */
    public static void broadcast(byte[] message, ClientHandler sender) {
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

    /**서버의 public key _ 해당 퍼블릭 키가 client 에 pre-installed 되었다 가정*/
    public static PublicKey getServerPublicKey(){
        return ServerKey.getUserPublicKey();
    }

    //    private static void sendCertificate() throws Exception {
//        clients.get(0).getOut().println(
//                CertificateGenerator.sendCertificateToClient(
//                        clients.get(1).getPublicKey(),ServerKey.getUserPrivateKey()
//                )
//        );
//        clients.get(1).getOut().println(
//                CertificateGenerator.sendCertificateToClient(
//                        clients.get(0).getPublicKey(),ServerKey.getUserPrivateKey()
//                )
//        );
//
//    }
    private static void sendCertificate() throws Exception {
        // 바이너리 데이터 전송을 위한 스트림 사용
        DataOutputStream out1 = new DataOutputStream(clients.get(0).getSocket().getOutputStream());
        DataOutputStream out2 = new DataOutputStream(clients.get(1).getSocket().getOutputStream());

        // 1. 첫 번째 클라이언트에게 두 번째 클라이언트의 인증서 전송
        Certificate cert1 = CertificateGenerator.sendCertificateToClient(
                clients.get(1).getPublicKey(), ServerKey.getUserPrivateKey()
        );
        byte[] certBytes1 = cert1.getEncoded(); // 인증서 직렬화
        out1.writeInt(certBytes1.length);       // 길이 전송
        out1.write(certBytes1);                 // 데이터 전송
        out1.flush();

        // 2. 두 번째 클라이언트에게 첫 번째 클라이언트의 인증서 전송
        Certificate cert2 = CertificateGenerator.sendCertificateToClient(
                clients.get(0).getPublicKey(), ServerKey.getUserPrivateKey()
        );
        byte[] certBytes2 = cert2.getEncoded(); // 인증서 직렬화
        out2.writeInt(certBytes2.length);       // 길이 전송
        out2.write(certBytes2);                 // 데이터 전송
        out2.flush();
    }
}
