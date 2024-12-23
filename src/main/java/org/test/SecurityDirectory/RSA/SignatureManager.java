package org.test.SecurityDirectory.RSA;

import javax.crypto.Cipher;
import java.security.*;
import java.util.Base64;

public class SignatureManager {
    Signature signature = null;

    public SignatureManager(){
        try {
            signature = Signature.getInstance("SHA256withRSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**서명을 위한 메소드 */
    public byte[] generateSignature(byte[] data,PrivateKey privateKey){
        try {
            signature.initSign(privateKey);
            signature.update(data);
            byte[] digitalSignature = signature.sign();
            return digitalSignature;

        } catch (SignatureException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
    /**서명 검증을 위한 메소드*/
    public boolean verifySignature(byte[] data, byte[] signatureData, PublicKey publicKey) {
        try {
            signature.initVerify(publicKey); // 공개 키 설정
            signature.update(data);         // 원본 데이터 설정
            return signature.verify(signatureData); // 서명 검증
        } catch (InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

}
