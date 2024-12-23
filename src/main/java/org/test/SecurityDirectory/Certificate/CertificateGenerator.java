package org.test.SecurityDirectory.Certificate;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import javax.security.auth.x500.X500Principal;

public class CertificateGenerator {

    // 인증서 생성 메서드
//    private static Certificate sendCertificateToClient(KeyPair keyPair) throws Exception {
//        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
//        certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
//        certGen.setIssuerDN(new X500Principal("CN=Server Certificate"));
//        certGen.setSubjectDN(new X500Principal("CN=Client Certificate"));
//        certGen.setNotBefore(new Date(System.currentTimeMillis()));
//        certGen.setNotAfter(new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000)); // 1년 유효기간
//        certGen.setPublicKey(keyPair.getPublic());
//        certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
//
//        // 개인 키로 서명하여 인증서 생성
//        return certGen.generate(keyPair.getPrivate(), "BC");
//    }

    public static Certificate sendCertificateToClient(PublicKey userPublicKey,PrivateKey ServerPrivateKey) throws Exception {

        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
        certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        certGen.setIssuerDN(new X500Principal("CN=Server CA")); // 서버 정보
        certGen.setSubjectDN(new X500Principal("CN=User PublicKey")); // 유저 정보
        certGen.setNotBefore(new Date(System.currentTimeMillis())); // 유효 시작일
        certGen.setNotAfter(new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000)); // 1년 유효
        certGen.setPublicKey(userPublicKey); // 유저의 퍼블릭 키 포함
        certGen.setSignatureAlgorithm("SHA256withRSA");

        // 4. 서버 CA의 Private Key로 인증서 서명
        X509Certificate userCertificate = certGen.generate(ServerPrivateKey, "BC");
        return userCertificate;
    }


//    // 클라이언트에게 키와 인증서 전송
//    private static void sendKeysAndCertificate(Socket socket, KeyPair keyPair, Certificate certificate) throws IOException {
//        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//        // 인증서 전송
//        out.writeObject(certificate);
//        out.flush();
//
//        System.out.println("Keys and certificate sent to client.");
//        socket.close();
//    }


}
