package com.snaplogic.snaps.soapv1;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.List;

public interface XmlHandler {
    List<String> extractSpecificOperation(NodeList nodeList);

    List<String> extractEndpoints(NodeList nodeList);

    List<String> extractServiceName(Document wsdlDocument, NodeList nodeList);

    Document loadWsdl(String wsdlUrl) throws Exception;
}