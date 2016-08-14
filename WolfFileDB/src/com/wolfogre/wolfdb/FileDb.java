package com.wolfogre.wolfdb;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.DownloadFileRequest;
import com.aliyun.oss.model.DownloadFileResult;
import com.aliyun.oss.model.OSSObject;

import java.io.*;
import java.security.MessageDigest;
import java.sql.*;

/**
 * Created by wolfogre on 8/14/16.
 */
public class FileDb {
    private String sqlitePath;
    private Connection connection;
    private OSSClient ossClient;
    private Statement statement;
    private String bucket;

    public FileDb(String sqlitePath){
        this.sqlitePath = sqlitePath;
    }

    public void open() throws FileDbException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new FileDbException("Can not find sqlite driver org.sqlite.JDBC", e);
        }

        File file = new File(sqlitePath);
        if (!file.exists()) {
            throw new FileDbException("Can not find sqlite file " + sqlitePath);
        }

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + sqlitePath);
        } catch (SQLException e) {
            throw new FileDbException("Can not open sqlite " + sqlitePath, e);
        }

        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            close();
            throw new FileDbException("Can not open sqlite statement " + sqlitePath, e);
        }

        String endpoint;
        String accessKeyId;
        String accessKeySecret;

        try {
            endpoint = statement.executeQuery("SELECT value FROM config WHERE key = 'endpoint'").getString(1);
            accessKeyId = statement.executeQuery("SELECT value FROM config WHERE key = 'accessKeyId'").getString(1);
            accessKeySecret = statement.executeQuery("SELECT value FROM config WHERE key = 'accessKeySecret'").getString(1);
            bucket = statement.executeQuery("SELECT value FROM config WHERE key = 'bucket'").getString(1);
        } catch (SQLException e) {
            close();
            throw new FileDbException("Can not get endpoint, accessKeyId or accessKeySecret from " + sqlitePath, e);
        }

        try{
            ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);

            //To test ossClient
            OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
            OSSObject ossObject = ossClient.getObject(bucket, "wolfdb");
            BufferedReader reader = new BufferedReader(new InputStreamReader(ossObject.getObjectContent()));
            String line = reader.readLine();
            reader.close();
            if (line == null)
                throw new Exception("Can not read oss file wolfdb");
        } catch (Throwable throwable) {
            close();
            throw new FileDbException("Can not login aliyun oss", throwable);
        }

    }

    public void close() throws FileDbException {
        try {
            if(connection != null && !connection.isClosed())
                connection.close();
        } catch (SQLException e) {
            throw new FileDbException("Can not close sqlite " + sqlitePath, e);
        }
        if(ossClient != null)
            ossClient.shutdown();
    }


    public String putFile(InputStream inputStream){
        return "";
    }

    public String getFileUrl(String fileId, int seconds){
        return "";
    }

    public OutputStream getFile(String fileId){
        return new ByteArrayOutputStream();
    }

    public void deleteFile(String fileId){
        return;
    }


    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
