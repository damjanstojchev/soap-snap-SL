package com.snaplogic.snaps.snapsv3;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.XML;
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

    @Override
    public JSONObject executeSoapRequestWithAuth(String soapEnvelope, String endPoint) throws Exception {
        BasicCredentialsProvider credsProvider;
        CloseableHttpClient httpClient;
        HttpPost httpPost;
        StringEntity entity;
        CloseableHttpResponse response;
        String responseString = null;
        JSONObject jsonResponse = null;

        try {
            // Set up Basic Authentication
            credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials("TEST.IMP1", "welcome123")  // Replace with your username and password
            );
            httpClient = HttpClients.custom()
                    .setDefaultCredentialsProvider(credsProvider)
                    .build();

            // Set up the HTTP POST request
            httpPost = new HttpPost(endPoint);
            entity = new StringEntity(soapEnvelope, "UTF-8");
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/soap+xml");

            // Execute the request and get the response
            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("Response Status Code: " + statusCode);

            if (statusCode == 200) {
                responseString = EntityUtils.toString(response.getEntity());

                // Convert the XML response to JSONObject
                jsonResponse = XML.toJSONObject(responseString);
            } else {
                throw new Exception("Failed to get a valid response. Status Code: " + statusCode);
            }

            httpClient.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error while sending the SOAP request envelope: " + e.getMessage());
        }

        // Return the JSON response
        return jsonResponse;
    }
}
