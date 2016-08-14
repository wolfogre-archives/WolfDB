import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 * Created by wolfogre on 8/14/16.
 */
public class AccessKey {
    private String endpoint;
    private String id;
    private String secret;

    public AccessKey() throws DocumentException {
        super();
        String path = this.getClass().getResource("/").getPath() + "access-key.cfg.xml";
        SAXReader reader = new SAXReader();
        Document document = reader.read(path);
        Element rootElement = document.getRootElement();

        endpoint = rootElement.element("endpoint").getTextTrim();
        id = rootElement.element("id").getTextTrim();
        secret = rootElement.element("secret").getTextTrim();
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getId() {
        return id;
    }

    public String getSecret() {
        return secret;
    }
}
