package com.wolfogre.wolfdb;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObject;

import java.io.*;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


/**
 * Created by wolfogre on 8/14/16.
 */
public class FileDb {
    private String sqlitePath;
    private Connection connection;
    private OSSClient ossClient;
    private Statement statement;
    private String bucket;
    private Random random;

    public FileDb(String sqlitePath){
        this.sqlitePath = sqlitePath;
        random = new Random();
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
            endpoint = executeQuery("SELECT value FROM config WHERE key = 'endpoint'");
            accessKeyId = executeQuery("SELECT value FROM config WHERE key = 'accessKeyId'");
            accessKeySecret = executeQuery("SELECT value FROM config WHERE key = 'accessKeySecret'");
            bucket = executeQuery("SELECT value FROM config WHERE key = 'bucket'");
        } catch (Exception e) {
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
        if(!executeExists("SELECT EXISTS (SELECT * FROM file WHERE code = '" + code + "')")){
            Date date = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd/");
            String path = simpleDateFormat.format(date) + code;
            try {
                ossClient.putObject(bucket, path, new ByteArrayInputStream(bytes));
            } catch (Exception e) {
                throw new FileDbException("Upload fail", e);
            }
            executeUpdate("INSERT INTO file (code, path) VALUES ('" + code + "','" + path +  "')");
        }
        String handle = createHandle();
        executeUpdate("UPDATE reference SET code = '" + code + "' WHERE handle = '" + handle + "'");
        executeUpdate("UPDATE file SET count = count + 1 WHERE code = '" + code + "'");

        return handle;
    }

    public URL getFileUrl(String handle, int seconds) throws FileDbException {

        if(!executeExists("SELECT EXISTS (SELECT * FROM reference WHERE handle = '" + handle + "')"))
            throw new FileDbException("Can not get file whose handle is " + handle);

        String code = executeQuery("SELECT code FROM reference WHERE handle = '" + handle + "'");

        return ossClient.generatePresignedUrl(
                bucket,
                executeQuery("SELECT path FROM file WHERE code = '" + code + "'"),
                new Date(new Date().getTime() + seconds * 1000)
        );

    }

    public InputStream getFile(String handle) throws FileDbException {
        if(!executeExists("SELECT EXISTS (SELECT * FROM reference WHERE handle = '" + handle + "')"))
            throw new FileDbException("Can not get file whose handle is " + handle);
        String code = executeQuery("SELECT code FROM reference WHERE handle = '" + handle + "'");
        try{
            OSSObject ossObject = ossClient.getObject(bucket, executeQuery("SELECT path FROM file WHERE code = '" + code + "'"));
            return ossObject.getObjectContent();
        } catch (Exception e){
            throw new FileDbException("Get file input stream error whose handle is ", e);
        }

    }

    public void deleteFile(String handle) throws FileDbException {
        if(!executeExists("SELECT EXISTS (SELECT * FROM reference WHERE handle = '" + handle + "')"))
            throw new FileDbException("Can not get file whose handle is " + handle);
        String code = executeQuery("SELECT code FROM reference WHERE handle = '" + handle + "'");
        executeUpdate("DELETE reference WHERE handle = '" + handle + "'");
        executeUpdate("UPDATE file SET count = count - 1 WHERE code = '" + code + "'");
        // TODO:原本这里应该判断 count 是否等于0，删除 OSS 里的资源和 file 表里对应的记录，但代价很大，考虑使用统一的垃圾回收机制
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

    private String createHandle() throws FileDbException {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 50; ++i)
            sb.append((char)('A' + random.nextInt(26)));
        String handle = sb.toString();
        if(executeExists("SELECT EXISTS (SELECT * FROM reference WHERE handle = '" + handle + "')"))
            return createHandle();
        executeUpdate("INSERT INTO reference (handle) VALUES ('" + handle + "')");
        return sb.toString();
    }

    private int executeUpdate(String sql) throws FileDbException {
        try {
            return statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new FileDbException("SQL error", e);
        }
    }

    private String executeQuery(String sql) throws FileDbException {
        try {
            return statement.executeQuery(sql).getString(1);
        } catch (SQLException e) {
            throw new FileDbException("SQL error", e);
        }
    }

    private boolean executeExists(String sql) throws FileDbException {
        try {
            return statement.executeQuery(sql).getBoolean(1);
        } catch (SQLException e) {
            throw new FileDbException("SQL error", e);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
