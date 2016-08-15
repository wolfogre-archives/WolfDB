package com.wolfogre.wolfdb;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by wolfogre on 8/14/16.
 */
public class Test {
    public static void main(String[] args) throws FileNotFoundException {
        System.out.println("Please input sqlite file path:");
        Scanner scanner = new Scanner(System.in);
        FileDb fileDb = new FileDb(scanner.next());
        System.out.println("Please input upload file path:");
        InputStream inputStream = new FileInputStream(scanner.next());
        try {
            fileDb.open();
            String id = fileDb.putFile(inputStream);
            URL url = fileDb.getFileUrl(id, 60);
            System.out.println(url.toString());
            fileDb.close();
        } catch (FileDbException e) {
            e.printStackTrace();
        }

    }
}
