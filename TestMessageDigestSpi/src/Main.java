import javax.xml.crypto.Data;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by wolfogre on 8/14/16.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        MessageDigest mdSHA;
        MessageDigest mdMD5;
        try {
            mdSHA = MessageDigest.getInstance("SHA");
            mdMD5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return;
        }
        String str = "asdfasdfasdfasdfasdfasdf";
        FileInputStream fileInputStream = new FileInputStream("/home/wolfogre/Pictures/Android-Studio-Desktop.png");
        byte[] bytes = new byte[fileInputStream.available()];
        fileInputStream.read(bytes);

        System.out.println(new Date().getTime());
        System.out.println(toHexString(mdSHA.digest(str.getBytes())));
        System.out.println(new Date().getTime());
        System.out.println(toHexString(mdMD5.digest(str.getBytes())));
        System.out.println(new Date().getTime());
        
        System.out.println(toHexString(mdSHA.digest(bytes)));
        System.out.println(new Date().getTime());
        System.out.println(toHexString(mdMD5.digest(bytes)));
        System.out.println(new Date().getTime());
    }

    public static String toHexString(byte[] bytes){
        StringBuilder stringBuilder = new StringBuilder();
        for(byte b : bytes){
            stringBuilder.append(Integer.toHexString((b >> 4) & 15));
            stringBuilder.append(Integer.toHexString(b & 15));
        }
        return stringBuilder.toString();
    }
}
