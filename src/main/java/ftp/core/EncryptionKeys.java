package ftp.core;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

public class EncryptionKeys {
    String encryptedPasswordString;
    String passwordString;
    byte[] encryptedPasswordRaw;

    EncryptionKeys(){}
    EncryptionKeys(String password) throws Exception {
        SecretKey key = generateKey("AES");
        Cipher cipher = Cipher.getInstance("AES");

        byte[] encryptedData = encryptPassword(password, key, cipher);
        this.encryptedPasswordRaw = encryptedData;
        this.encryptedPasswordString = new String(encryptedData, "UTF8");
        String decryptedData = decryptPassword(encryptedData, key, cipher);
        this.passwordString = decryptedData;
    }

    public String getEncryptedPasswordString() {
        return encryptedPasswordString;
    }

    public void setEncryptedPasswordString(String encryptedPasswordString) {
        this.encryptedPasswordString = encryptedPasswordString;
    }

    public String getPasswordString() {
        return this.passwordString;
    }

    public void setPasswordString(String passwordString) {
        this.passwordString = passwordString;
    }

    public byte[] getEncryptedPasswordRaw() {
        return encryptedPasswordRaw;
    }

    public void setEncryptedPasswordRaw(byte[] encryptedPasswordRaw) {
        this.encryptedPasswordRaw = encryptedPasswordRaw;
    }

    public SecretKey generateKey(String encryptionType){
        try{
            KeyGenerator keyGenerator = KeyGenerator.getInstance(encryptionType);
            SecretKey myKey =  keyGenerator.generateKey();
            return myKey;
        } catch(Exception e) {
            return null;
        }
    }

    public byte[] encryptPassword(String Password, SecretKey key, Cipher cipher) throws Exception {
        try{
            byte[] text = Password.getBytes(StandardCharsets.UTF_8);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] textEncrypted = cipher.doFinal(text);
            return textEncrypted;
        } catch (Exception e){
            return  null;
        }
    }

    public String decryptPassword(byte[] encryptedPassword, SecretKey key, Cipher cipher) throws Exception {
        try{
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] textDecrypt = cipher.doFinal(encryptedPassword);
            String result = new String(textDecrypt, "UTF8");
            return  result;
        } catch (Exception e){
            return null;
        }
    }
}
