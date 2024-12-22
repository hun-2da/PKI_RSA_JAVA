package org.test.SecurityDirectory.RSA;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;

/**RSA 암호화 및 복호화 클레스*/
public class CryptoHandler {
    Cipher cipher = null;

    public CryptoHandler(){
        try {
            cipher = Cipher.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    /**key 암호화를 위한 메소드
     * 입력 데이터(String) 와 public key로 암호화한다.*/
    public byte[] Message_Encryption(String Message, PublicKey publicKey){
        try {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedData = cipher.doFinal(Message.getBytes());
            return encryptedData;
            //System.out.println("Encrypted Data: " + new String(encryptedData));

        }catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        //return null;
    }
    /**key 복호화를 위한 메소드
     * 암호화 데이터(byte[])와 private Key로 복호화 진행*/
    public byte[] Message_Decryption(byte[] encryptedData, PrivateKey privateKey){
        try {
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedData = cipher.doFinal(encryptedData);
            return decryptedData;

        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }
}
