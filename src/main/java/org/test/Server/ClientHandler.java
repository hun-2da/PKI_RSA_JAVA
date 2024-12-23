package org.test.Server;


import org.test.Client.Client;
import org.test.SecurityDirectory.RSA.KeyGeneration;

import java.io.*;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

/**각 클라이언트 객체들을 관리하기 위한 핸들러 _
 * 클라이언트들이 연결될때 마다 해당 객체가 생성 및 관리*/
public class ClientHandler extends Thread {
    private Socket socket;
    private PublicKey ClientPublicKey = null;
    //private PrivateKey ClientPrivateKey = null;
    private DataOutputStream out;

    /**메시지 출력을 위한 스트림
     * (서버->클라이언트 )*/
    //private PrintWriter out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            //out = new PrintWriter(socket.getOutputStream(), true);
            out = new DataOutputStream(socket.getOutputStream());
            DataInputStream MessageIn = new DataInputStream(socket.getInputStream());
            sendKeyPairToClient();

//            String message;
//
//            while ((message = in.readLine()) != null) { // 각 클라이언트들의 메시지 (
//                System.out.println("클라이언트 메시지: " + message);
//                Server.broadcast(message, this); // 받은 메시지를 다른 클라이언트들에게 전송
//            }
            while (true) {
                try {
                    // 데이터 길이 수신
                    int length = MessageIn.readInt();
                    if (length <= 0) break; // 종료 조건

                    // 데이터 수신
                    byte[] messageBytes = new byte[length];
                    MessageIn.readFully(messageBytes);

                    // 메시지를 다른 클라이언트에게 브로드캐스트
                    Server.broadcast(messageBytes, this);

                } catch (IOException e) {
                    System.err.println("클라이언트 메시지 수신 오류: " + e.getMessage());
                    break;
                }
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

    //    public void sendMessage(String message) {
//        if (out != null)
//            out.println(message);
//    }
    public void sendMessage(byte[] data) {
        try {
            //byte[] data = message.getBytes();
            out.writeInt(data.length); // 길이 전송
            out.write(data);           // 데이터 전송
            out.flush();
        } catch (IOException e) {
            System.err.println("메시지 전송 오류: " + e.getMessage());
        }
    }


    /**Client에게 key값을 보내기 위해
     * Public , Private 키를 만들고 서명 후 전송하는 메소드 */
    private void sendKeyPairToClient(){
        KeyGeneration ClientKey = new KeyGeneration();

        ClientPublicKey = ClientKey.getUserPublicKey();

        byte[] publicKeyBytes = ClientPublicKey.getEncoded();
        Server.ClientKeySignature(publicKeyBytes,this);


        byte[] privateKeyBytes = ClientKey.getUserPrivateKey().getEncoded();
        Server.ClientKeySignature(privateKeyBytes,this);
    }


    //    /**키를 전송하기 위한 메소드*/
//    public void transferKey(byte[] key,byte[] Signature_key){
//        if(Signature_key != null){
//            out.println(Base64.getEncoder().encodeToString(key));
//            out.println(Base64.getEncoder().encodeToString(Signature_key));
//        }
//    }
    public void transferKey(byte[] key, byte[] Signature_key){
        try {
            if(Signature_key != null){
                // 데이터 길이 전송
                out.writeInt(key.length);
                out.write(key);
                out.writeInt(Signature_key.length);
                out.write(Signature_key);
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("키 전송 중 오류 발생: " + e.getMessage());
        }
    }

    public PublicKey getPublicKey(){
        return ClientPublicKey;
    }
    public Socket getSocket(){
        return socket;
    }
//    public PrintWriter getOut(){
//        return out;
//    }

}
