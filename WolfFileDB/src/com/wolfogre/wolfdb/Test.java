package com.wolfogre.wolfdb;

import java.util.Scanner;

/**
 * Created by wolfogre on 8/14/16.
 */
public class Test {
    public static void main(String[] args){
        System.out.println("Please input sqlite file path:");
        Scanner scanner = new Scanner(System.in);
        FileDb fileDb = new FileDb(scanner.next());
        try {
            fileDb.open();
            fileDb.close();
        } catch (FileDbException e) {
            e.printStackTrace();
        }

    }
}
