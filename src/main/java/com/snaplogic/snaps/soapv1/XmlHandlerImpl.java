package com.snaplogic.snaps.soapv1;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public class XmlHandlerImpl implements XmlHandler {
    Document wsdlDocument = null;

    @Override
    public Document loadWsdl(String wsdlUrl) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        URL url = new URL(wsdlUrl);
        return builder.parse(url.openStream());
    }

    @Override
    public List<String> extractServiceName(Document wsdlDocument, NodeList nodeList) {
        nodeList = wsdlDocument.getElementsByTagName("wsdl:service");
        return Collections.singletonList(nodeList.item(0).getAttributes().getNamedItem("name").getTextContent());
    }

    @Override
    public List<String> extractEndpoints(NodeList nodeList) {
        nodeList = wsdlDocument.getElementsByTagName("soap12:address");
        //NodeList portNodes = wsdlDocument.getElementsByTagNameNS("*","address");
        System.out.println(nodeList.getLength());
        if (nodeList.item(0) != null) {
            String endpoint = nodeList.item(0).getAttributes().getNamedItem("location").getTextContent();
            System.out.println(endpoint);
        }
        return Collections.singletonList("test123");
    }

    @Override
    public List<String> extractSpecificOperation(NodeList nodeList) {
        nodeList = wsdlDocument.getElementsByTagName("soap12:address");

        for (int i = 0; i < nodeList.getLength(); i++) {
            String endpoint = nodeList.item(i).getAttributes().getNamedItem("location").getTextContent();
            System.out.println(endpoint);
        }
        return Collections.singletonList("test123");
    }
}
