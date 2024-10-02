package com.snaplogic.snaps.snapsv2;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.ArrayList;
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
    public List<String> extractServiceName(Document wsdlDocument) {
        List<String> serviceNamesList = new ArrayList<>();
        NodeList serviceNodes = wsdlDocument.getElementsByTagName("wsdl:service");
        String serviceName = serviceNodes.item(0).getAttributes().getNamedItem("name").getTextContent();
        serviceNamesList.add(serviceName);
        return serviceNamesList;
    }

    @Override
    public List<String> extractEndpoints(Document wsdlDocument) {
        List<String> endpointsList = new ArrayList<>();
        NodeList portNodes = wsdlDocument.getElementsByTagName("soap12:address");

        for (int i = 0; i < portNodes.getLength(); i++) {
            String endpoint = portNodes.item(i).getAttributes().getNamedItem("location").getTextContent();
            endpointsList.add(endpoint);
        }

        return endpointsList;
    }

    @Override
    public List<String> extractSpecificOperations(Document wsdlDocument, String serviceName) {
        List<String> operationsList = new ArrayList<>();
        NodeList operationNodes = wsdlDocument.getElementsByTagName("wsdl:operation");

        for (int i = 0; i < operationNodes.getLength(); i++) {
            String operationName = operationNodes.item(i).getAttributes().getNamedItem("name").getTextContent();
            operationsList.add(operationName);
        }

        return operationsList;
    }
}
