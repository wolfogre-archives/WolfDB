import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.Bucket;
import org.dom4j.DocumentException;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;

/**
 * Created by wolfogre on 8/14/16.
 */
public class Main {
    public static void main(String [] args){
        AccessKey accessKey;
        try {
            accessKey = new AccessKey();
        } catch (DocumentException e) {
            e.printStackTrace();
            return;
        }
        // 创建ClientConfiguration实例，按照您的需要修改默认参数
        ClientConfiguration conf = new ClientConfiguration();
        // 开启支持CNAME选项
        conf.setSupportCname(true);
        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(accessKey.getEndpoint(), accessKey.getId(), accessKey.getSecret(), conf);
        // 使用访问OSS

        // 上传字符串
        //String content = "Hello OSS";
        //ossClient.putObject("wolfogre-wolfdb", "test/hello.txt", new ByteArrayInputStream(content.getBytes()));

        // 设置URL过期时间
        Date expiration = new Date(new Date().getTime() + 60 * 1000);

        // 生成URL
        URL url = ossClient.generatePresignedUrl("wolfogre-wolfdb", "test/hello.txt", expiration);
        System.out.println(url.toString());

        // 关闭client
        ossClient.shutdown();
    }
}
