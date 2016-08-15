package com.wolfogre.wolfdb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by wolfogre on 8/15/16.
 */
public class FileInfo {
    private String code;
    private String path;

    public static FileInfo get(String code, Statement statement) throws FileDbException {
        ResultSet result = null;
        try {
            result = statement.executeQuery("SELECT path FROM file WHERE code = '" + code + "'");
            if(!result.next())
                return null;
            return new FileInfo(code, result.getString(1));
        } catch (SQLException e) {
            throw new FileDbException("SQL error", e);
        }
    }

    public static void put(String code, String path, Statement statement) throws FileDbException {
        if(get(code, statement) != null)
            return;
        try {
            statement.executeUpdate("INSERT INTO file (code, path) VALUES ('" + code + "','" + path +  "')");
        } catch (SQLException e) {
            throw new FileDbException("SQL error", e);
        }
    }

    public String getCode() {
        return code;
    }

    public String getPath() {
        return path;
    }

    private FileInfo(String code, String path) {
        this.code = code;
        this.path = path;
    }
}
