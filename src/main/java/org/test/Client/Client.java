package org.test.Client;

import org.test.SecurityDirectory.RSA.CryptoHandler;
import org.test.SecurityDirectory.RSA.SignatureManager;
import org.test.Server.Server;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;

public class Client {
    final public static int PublicKey = 0;
    final public static int SignaturePublicKey = 1;
    final public static int PrivateKey = 2;
    final public static int SignaturePrivateKey = 3;
    public static ArrayList<byte[]> KeyList = new ArrayList<>();

    private static PublicKey myPublicKey = null;
    private static PrivateKey myPrivateKey = null;

    private static PublicKey partnerPublicKey = null;


    public static void main(String[] args) {
        String host = "127.0.0.1"; // 서버 IP 주소 (로컬)
        int port = 12345; // 서버 포트 번호

        //try-with-resources _ try 블록이 끝나거나 예외가 발생하더라도 자동으로 자원을 닫아주는(해제하는) 역할
        try (Socket socket = new Socket(host, port);
             // 소켓 생성 _ 해당 소켓을 통해 송수신
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             // 입력 스트림 _ 서버가 전송한 데이터를 byte -> character _-> Line 단위로 읽기 위함
             DataInputStream MessageIn = new DataInputStream(socket.getInputStream());


             //PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             // 출력 스트림 _  서버로 데이터를 byte로 _ 자동으로 버퍼 지우기 true
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());


             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {
            //사용장 입력 스트림

            System.out.println("서버에 연결되었습니다.");
            CryptoHandler cryptoHandler = new CryptoHandler();//암호 및 복호화를 위한 메소드


            // 서버로부터 메시지를 받는 스레드
            Thread receiver = new Thread(() -> {
                try {

                    for (int i = 0; i < 8; i++) { // 8번 반복 (4개의 데이터 + 각 데이터 길이)
                        // 1. 길이 정보 수신

                        int length = MessageIn.readInt(); // 데이터 길이 수신
                        if (length <= 0) throw new IOException("수신된 데이터 길이 오류!");

                        // 2. 데이터 수신
                        byte[] data = new byte[length];
                        MessageIn.readFully(data); // 데이터 수신

                        if(i%2 == 1)
                            KeyList.add(data); // KeyList에 저장
                    }

                    checkKeyValidity();

                    int length2 = MessageIn.readInt(); // 데이터 길이 수신
                    if (length2 <= 0) {
                        throw new IOException("수신된 인증서 길이가 잘못되었습니다!");
                    }

                    // 2. 데이터 수신
                    byte[] certBytes = new byte[length2]; // 길이에 맞게 배열 생성
                    MessageIn.readFully(certBytes);

                    retrieveCertificate(certBytes);

//                    String serverMessage;
//                    while ((serverMessage = in.readLine()) != null) {
//                        /* 메시지를 기다리는 코드.
//                        메시지가 오면 serverMessage에 저장
//                        */
//
//
//                        System.out.println("메시지 수신: " + serverMessage);
//                    }
                    while (true) { // 메시지를 계속 수신
                        // 2. 데이터 길이 수신 (4바이트 정수)
                        int length = MessageIn.readInt(); // 데이터 길이 수신
                        if (length <= 0) break; // 길이가 0 이하이면 종료

                        // 3. 암호화된 데이터 수신
                        byte[] encryptedMessage = new byte[length]; // 길이에 맞게 배열 생성
                        MessageIn.readFully(encryptedMessage); // 정확한 길이만큼 데이터 수신

                        // 4. 복호화
                        String decryptedMessage = cryptoHandler.Message_Decryption(encryptedMessage, myPrivateKey);

                        // 5. 메시지 출력
                        System.out.println("복호화된 메시지: " + decryptedMessage);

                        // 6. 종료 조건 확인
                        if (decryptedMessage.equalsIgnoreCase("끝")) { // 복호화된 메시지로 종료 확인
                            System.out.println("서버 연결 종료.");
                            break;
                        }
                    }
                } catch (IOException e) {
                    System.err.println("서버로부터 메시지 수신 중 오류 발생: " + e.getMessage());
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                } catch (InvalidKeySpecException e) {
                    throw new RuntimeException(e);
                } catch (CertificateException e) {
                    throw new RuntimeException(e);
                } catch (SignatureException e) {
                    throw new RuntimeException(e);
                } catch (InvalidKeyException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchProviderException e) {
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            receiver.start();



            // 사용자 입력을 서버로 전송
            String userMessage;
            while ((userMessage = userInput.readLine()) != null) {

                byte[] encryptedMessage = cryptoHandler.Message_Encryption(userMessage,partnerPublicKey);
                //out.println(userMessage); // 서버로 메시지 전송
                // 1. 데이터 길이 전송 (4바이트 정수)
                out.writeInt(encryptedMessage.length);
                // 2. 암호화된 메시지 전송
                out.write(encryptedMessage);
                // 전송 완료
                out.flush();


                if (userMessage.equalsIgnoreCase("끝")) {
                    System.out.println("연결 종료");
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private static byte[] convertStringToBytes(String key){
//        return Base64.getDecoder().decode(key);
//    }
    private static void checkKeyValidity() throws NoSuchAlgorithmException, InvalidKeySpecException {
        SignatureManager signatureManager = new SignatureManager();
        boolean isValid = signatureManager.verifySignature(KeyList.get(PublicKey), KeyList.get(SignaturePublicKey), Server.getServerPublicKey());
        if (isValid) {
            System.out.println("서명 검증 성공! 공개 키 복원 중...");
            //  공개 키 복원
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            myPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(KeyList.get(PublicKey)));

        } else {
            System.out.println("서명 검증 실패! 키 사용 불가.");
        }

        boolean isValid2 = signatureManager.verifySignature(KeyList.get(PrivateKey), KeyList.get(SignaturePrivateKey), Server.getServerPublicKey());
        if (isValid2) {
            System.out.println("서명 검증 성공! 개인 키 복원 중...");
            // 개인 키 복원
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            myPrivateKey = keyFactory.generatePrivate(new X509EncodedKeySpec(KeyList.get(PrivateKey)));

        } else {
            System.out.println("서명 검증 실패! 키 사용 불가.");
        }
    }
    private static void retrieveCertificate(  byte[] certBytes) throws Exception {

        // 3. 인증서 복원
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certBytes));

        // 4. 서명 검증
        certificate.verify(Server.getServerPublicKey()); // 서버의 공개 키로 서명 검증
        System.out.println("서명 검증 성공!");

        // 5. 공개 키 복원
        partnerPublicKey = certificate.getPublicKey(); // 유저의 퍼블릭 키 복원
        if (partnerPublicKey == null) {
            throw new IllegalStateException("복원된 공개 키가 null 상태입니다!");
        }
        System.out.println("복원된 유저 퍼블릭 키: " + partnerPublicKey);
    }

//    private static void retrieveCertificate(String Certificate) throws Exception {
//        byte[] certBytes = Base64.getDecoder().decode(Certificate);
//        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
//        X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certBytes));
//
//        // 인증서 정보 출력
//        //System.out.println("Subject: " + certificate.getSubjectDN());
//        //System.out.println("Issuer: " + certificate.getIssuerDN());
//
//        // CA의 퍼블릭 키로 서명 검증
//        //PublicKey caPublicKey = getCAPublicKey(); // 사전에 등록된 CA 키 사용
//        certificate.verify(Server.getServerPublicKey());
//        //System.out.println("서명 검증 성공!");
//
//        // 6. 유저의 퍼블릭 키 복원
//        partnerPublicKey = certificate.getPublicKey();
//        if(partnerPublicKey==null) System.out.println("키 문제");
//
//        //System.out.println("복원된 유저 퍼블릭 키: " + userPublicKey);
//    }
//
//    private static PublicKey getCAPublicKey() throws Exception {
//
//        String base64CAPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFA...";
//        byte[] keyBytes = Base64.getDecoder().decode(base64CAPublicKey);
//        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//        return keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
//    }


}