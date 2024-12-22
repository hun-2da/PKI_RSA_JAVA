package org.test.SecurityDirectory.RSA;

import java.security.*;

public class KeyGeneration {
    private KeyPair User_RSA_Key;

    public void User_RSAKey_Registration() {
        // 1. RSA KeyPairGenerator 생성
        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        keyGen.initialize(2048); // RSA 키 크기 설정
        // 2. 키 쌍 생성
        User_RSA_Key = keyGen.generateKeyPair();
    }

    public PublicKey getUserPublicKey(){
        return User_RSA_Key.getPublic();
    }
    public PrivateKey getUserPrivateKey(){
        return User_RSA_Key.getPrivate();
    }
}
