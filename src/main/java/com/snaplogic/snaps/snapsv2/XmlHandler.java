package com.snaplogic.snaps.snapsv2;

import org.w3c.dom.Document;

import java.util.List;

public interface XmlHandler {
    List<String> extractSpecificOperations(Document wsdlDocument, String serviceName);

    List<String> extractEndpoints(Document wsdlDocument);

    List<String> extractServiceName(Document wsdlDocument);

    Document loadWsdl(String wsdlUrl) throws Exception;
}