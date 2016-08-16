package com.wolfogre.wolfdb;

import java.io.*;
import java.net.URL;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * Created by wolfogre on 8/14/16.
 */
public class Test {
    public static void main(String[] args) throws FileNotFoundException {
        System.out.println("Please input sqlite file path:");
        Scanner scanner = new Scanner(System.in);
        FileDb fileDb = new FileDb(scanner.next());
        try {
            fileDb.open();
            boolean flag = true;
            LinkedList<String> record = new LinkedList<>();

            while(flag && scanner.hasNext()){
                switch (scanner.next()){
                    case "help":
                        System.out.println("help");
                        System.out.println("list");
                        System.out.println("upload");
                        System.out.println("geturl");
                        System.out.println("download");
                        System.out.println("exit");
                    case "list":
                        record.forEach(System.out::println);
                        break;
                    case "upload":
                        String handle = fileDb.putFile(new FileInputStream(scanner.next()));
                        System.out.println("File ID: " + handle);
                        for(String str : record){
                            if(str.equals(handle))
                                break;
                        }
                        record.add(handle);
                        break;
                    case "geturl":
                        System.out.println(fileDb.getFileUrl(scanner.next(), scanner.nextInt()).toString());
                        break;
                    case "download":
                        InputStream is = fileDb.getFile(scanner.next());
                        FileOutputStream fileOutputStream = new FileOutputStream(new File(scanner.next()));
                        int read;
                        while((read = is.read()) != -1){
                            fileOutputStream.write(read);
                        }
                        fileOutputStream.close();
                        break;
                    case "exit":
                        flag = false;
                        break;
                    default:
                        System.out.println("Input error");
                        continue;
                }

                System.out.println("Done !");
            }

            fileDb.close();
        } catch (FileDbException | IOException e) {
            e.printStackTrace();
        }

    }
}
