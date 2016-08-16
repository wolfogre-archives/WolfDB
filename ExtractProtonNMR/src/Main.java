import com.mongodb.MongoClient;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by wolfogre on 8/16/16.
 */
public class Main {
    static MongoClient mongoClient = new MongoClient();
    static MongoDatabase db = mongoClient.getDatabase("ProtonNMR");
    public static void main(String[] args) throws IOException {
        //ExtractBasicData();
        ExtractOligo();

    }

    static void ExtractOligo() throws IOException {
        ArrayList<String> oligos = new ArrayList<>();
        DistinctIterable<String> result = db.getCollection("BasicData").distinct("Oligo", String.class);
        for(String str : result){
            oligos.add(str);
        }
        // https://www.ccrc.uga.edu/world/xgnmr/showseq.php?seq=XGol

    }

    static void ExtractBasicData() throws IOException {
        ArrayList<File> files = new ArrayList<>();
        files.add(new File("/home/wolfogre/IdeaProjects/WolfDB/ExtractProtonNMR/data/D-Glcol.html"));
        files.add(new File("/home/wolfogre/IdeaProjects/WolfDB/ExtractProtonNMR/data/α-D-Xylp.html"));
        files.add(new File("/home/wolfogre/IdeaProjects/WolfDB/ExtractProtonNMR/data/α-L-Araf.html"));
        files.add(new File("/home/wolfogre/IdeaProjects/WolfDB/ExtractProtonNMR/data/α-L-Fucp.html"));
        files.add(new File("/home/wolfogre/IdeaProjects/WolfDB/ExtractProtonNMR/data/β-D-Galp.html"));
        files.add(new File("/home/wolfogre/IdeaProjects/WolfDB/ExtractProtonNMR/data/β-D-Glcp.html"));
        files.add(new File("/home/wolfogre/IdeaProjects/WolfDB/ExtractProtonNMR/data/β-D-Xylp.html"));
        files.add(new File("/home/wolfogre/IdeaProjects/WolfDB/ExtractProtonNMR/data/β-L-Araf.html"));
        for(File f : files)
            Extract(f);
    }

    static void Extract(File input) throws IOException {
        org.jsoup.nodes.Document doc = Jsoup.parse(input, "UTF-8");
        Element table = doc.select("table").first();
        Elements ths = table.select("tr").first().select("th");
        ArrayList<String> titleHead = ths.stream().map(Element::text).collect(Collectors.toCollection(ArrayList::new));
        Elements trs = table.select("tr");
        trs.remove(0);
        for(Element e : trs){
            int index = 0;
            Document d = new Document();
            for(Element e1 : e.select("td")){
                d.append(titleHead.get(index++), e1.text().trim());
            }
            db.getCollection("BasicData").insertOne(d);
        }
    }
}
