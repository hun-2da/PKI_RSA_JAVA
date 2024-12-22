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
    public boolean verifySignature(byte[] SignatureData,PublicKey publicKey){
        try {
            signature.initVerify(publicKey);
            signature.update(SignatureData);
            boolean isVerified = signature.verify(SignatureData);
            return isVerified;

        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        }

    }
}
