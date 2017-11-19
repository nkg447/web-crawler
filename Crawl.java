import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class Crawl{

    static List<String> urls=new LinkedList<>();
    static Document doc;
    static String filepath = "Sites.xml";

    static void crawlAndStore(String url)throws Exception{
        //System.out.println(url);
        addUrl(url);
        URLConnection con=(new URL(url)).openConnection();
        Scanner sc=new Scanner(con.getInputStream());
        String line;
        int index;
        int start,end;
        String link;
        while(sc.hasNext()){
            line=sc.nextLine();
            index=line.indexOf("http", 0);
            if(index!=-1){
                if(line.charAt(index-1)=='"'){
                    start=index;
                    end=line.indexOf('"', index);
                    if(end==-1) continue;
                    link=line.substring(start, end);
                    urls.add(link);
                }
            }
            
        }
    }

    static void addUrl(String url)throws Exception{
        NodeList seeds=doc.getElementsByTagName("seed");
        int len=seeds.getLength();
        Node seed=seeds.item(len-1);
        Node nou=doc.getElementsByTagName("noofurl").item(len-1);
        int temp=Integer.parseInt(nou.getTextContent())+1;
        nou.setTextContent(temp+"");

        Node urllist=doc.getElementsByTagName("urllist").item(len-1);
        Node urlNode=doc.createElement("url");
        urlNode.setTextContent(url);
        urllist.appendChild(urlNode);
        completetransform();
    }

    static void completetransform()throws Exception{
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty("indent", "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(filepath));
		transformer.transform(source, result);
    }

    static void addSeedToXML(String seedUrl)throws Exception{
        Node nos = doc.getElementsByTagName("noofseed").item(0);
        int noOfSeed = Integer.parseInt(nos.getTextContent());
        noOfSeed++;
        nos.setTextContent(noOfSeed+"");

        Node webc=doc.getElementsByTagName("webcrawler").item(0);

        Element seed = doc.createElement("seed");
        seed.setAttribute("seedurl", seedUrl);
        Element nou = doc.createElement("noofurl");
        nou.setTextContent("0");

        Element urllist = doc.createElement("urllist");
        seed.appendChild(nou);
        seed.appendChild(urllist);
        webc.appendChild(seed);

        completetransform();
    }

    public static void main(String[] args)throws Exception {
        
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        doc = docBuilder.parse(filepath);
        
        String seed1="https://www.google.com";
        String seed2="https://stackoverflow.com/questions/11087163/how-to-get-url-html-contents-to-string-in-java";
        String seed3="http://192.168.1.1/cgi-bin/webproc?getpage=html/index.html&var:menu=status&var:page=deviceinfo";
        addSeedToXML(seed1);
        crawlAndStore(seed1);
        int count=0;
        while(!urls.isEmpty() && count<1000){
            crawlAndStore(urls.get(0));
            urls.remove(0);
            count++;
        }
    }
}