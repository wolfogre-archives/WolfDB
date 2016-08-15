package com.wolfogre.wolfdb;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.DownloadFileRequest;
import com.aliyun.oss.model.DownloadFileResult;
import com.aliyun.oss.model.OSSObject;

import java.io.*;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;


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


    public String putFile(InputStream inputStream) throws FileDbException {
        byte[] bytes;
        try {
            bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            inputStream.close();
        } catch (IOException e) {
            throw new FileDbException("Can not read local file");
        }

        String code = getCode(bytes);
        FileInfo fileInfo;

        fileInfo = FileInfo.get(code, statement);
        if(fileInfo != null)
            return fileInfo.getCode();

        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd/");
        String path = simpleDateFormat.format(date) + code;
        try {
            ossClient.putObject(bucket, path, new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            throw new FileDbException("Upload fail", e);
        }
        FileInfo.put(code, path, statement);

        return code;
    }

    public URL getFileUrl(String fileId, int seconds) throws FileDbException {
        FileInfo fileInfo =FileInfo.get(fileId, statement);

        if(fileInfo == null)
            throw new FileDbException("Can not get file whose id is " + fileId);

        return ossClient.generatePresignedUrl(
                bucket,
                fileInfo.getPath(),
                new Date(new Date().getTime() + seconds * 1000)
        );

    }

    public InputStream getFile(String fileId) throws FileDbException {
        FileInfo fileInfo = FileInfo.get(fileId, statement);
        if(fileInfo == null)
            throw new FileDbException("Can not get file whose id is " + fileId);
        try{
            OSSObject ossObject = ossClient.getObject(bucket, fileInfo.getPath());
            return ossObject.getObjectContent();
        } catch (Exception e){
            throw new FileDbException("Get file input stream error whose id is ", e);
        }

    }

    public void deleteFile(String fileId) throws FileDbException {
        FileInfo fileInfo = FileInfo.get(fileId, statement);
        if(fileInfo == null)
            throw new FileDbException("Can not get file whose id is " + fileId);
        try{
            ossClient.deleteObject(bucket, fileInfo.getPath());
        } catch (Exception e){
            throw new FileDbException("Get file input stream error whose id is ", e);
        }
    }

    private String getCode(byte[] bytes) throws FileDbException {
        MessageDigest mdSHA;
        try {
            mdSHA = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            throw new FileDbException("Can not load SHA", e);
        }
        byte[] result = mdSHA.digest(bytes);
        StringBuilder stringBuilder = new StringBuilder();
        for(byte b : result){
            stringBuilder.append(Integer.toHexString((b >> 4) & 15));
            stringBuilder.append(Integer.toHexString(b & 15));
        }
        return stringBuilder.toString();
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
