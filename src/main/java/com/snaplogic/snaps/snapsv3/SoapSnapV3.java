package com.snaplogic.snaps.snapsv3;

import com.google.inject.Inject;
import com.snaplogic.api.ConfigurationException;
import com.snaplogic.api.ExecutionException;
import com.snaplogic.api.Snap;
import com.snaplogic.common.properties.builders.PropertyBuilder;
import com.snaplogic.snap.api.*;
import com.snaplogic.snap.api.capabilities.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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

import java.util.*;

@General(title = "Soap Snap v3", purpose = "Soap Requests", author = "Baselinked")
@Inputs(min = 0, max = 1, accepts = {ViewType.DOCUMENT})
@Outputs(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Errors(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Version(snap = 1)
@Category(snap = SnapCategory.READ)
public class SoapSnapV3 implements Snap {

    private static final String WSDL_URL = "wsdl url";
    private static final String SERVICE_NAME = "Service Name";
    private static final String ENDPOINT = "Endpoint";
    private static final String OPERATION = "Operation";
    private static final String ENVELOPE = "Envelope";

    private static String wsdlUrl;
    private static String serviceName;
    private static String endPoint;
    private static String operation;
    private static String envelope;

    private static final XmlHandlerImpl xmlHandler = new XmlHandlerImpl();
    @Inject
    private DocumentUtility documentUtility;
    @Inject
    private OutputViews outputViews;
    @Inject
    private ErrorViews errorViews;

    @Override
    public void defineProperties(PropertyBuilder propertyBuilder) {
        propertyBuilder.describe(WSDL_URL, "Insert the WSDL URL")
                .required()
                .add();

        propertyBuilder.describe(SERVICE_NAME, "Extract service name if needed")
                .withSuggestions((suggestionBuilder, propertyValues) -> {
                    String wsdlUrl = propertyValues.get(WSDL_URL);
                    try {
                        Document document = xmlHandler.loadWsdl(wsdlUrl);
                        List<String> serviceNames = xmlHandler.extractServiceName(document);
                        suggestionBuilder.node(SERVICE_NAME).suggestions(serviceNames.toArray(new String[0]));
                    } catch (Exception e) {
                        throw new ConfigurationException(e, "Failed to load WSDL and extract service name");
                    }
                })
                .required()
                .add();

        propertyBuilder.describe(ENDPOINT, "Select specific endpoint")
                .withSuggestions((suggestionBuilder, propertyValues) -> {
                    String wsdlUrl = propertyValues.get(WSDL_URL);
                    try {
                        Document document = xmlHandler.loadWsdl(wsdlUrl);
                        List<String> endpoints = xmlHandler.extractEndpoints(document);
                        suggestionBuilder.node(ENDPOINT).suggestions(endpoints.toArray(new String[0]));
                    } catch (Exception e) {
                        throw new ConfigurationException(e, "Failed to load WSDL and extract endpoints");
                    }
                })
                .required()
                .add();

        propertyBuilder.describe(OPERATION, "Select specific operation")
                .withSuggestions((suggestionBuilder, propertyValues) -> {
                    String wsdlUrl = propertyValues.get(WSDL_URL);
                    String serviceName = propertyValues.get(SERVICE_NAME);
                    try {
                        Document document = xmlHandler.loadWsdl(wsdlUrl);
                        List<String> operations = xmlHandler.extractSpecificOperations(document, serviceName);
                        suggestionBuilder.node(OPERATION).suggestions(operations.toArray(new String[0]));
                    } catch (Exception e) {
                        throw new ConfigurationException(e, "Failed to load WSDL and extract operations");
                    }
                })
                .required()
                .add();
    }

    @Override
    public void configure(PropertyValues propertyValues) throws ConfigurationException {
        try {
            wsdlUrl = propertyValues.get(WSDL_URL);
            serviceName = propertyValues.get(SERVICE_NAME);
            operation = propertyValues.get(OPERATION);
            endPoint = propertyValues.get(ENDPOINT);

            Document document = xmlHandler.loadWsdl(wsdlUrl);

//            NodeList operationNodes = document.getElementsByTagName("wsdl:operation");
//            for (int i = 0; i < operationNodes.getLength(); i++) {
//                if (operationNodes.item(i).getAttributes().getNamedItem("name").getTextContent().equals(operation)) {
//                    operation = operationNodes.item(i).getAttributes().getNamedItem("name").getTextContent();
//                    break;
//                }
//            }
            List<String> x = xmlHandler.extractSpecificOperations(document, serviceName);

            if (x == null) {
                throw new SnapDataException("Selected operation '" + operation + "' not found in the WSDL document.");
            }

            NodeList policyNodes = document.getElementsByTagName("wsp:PolicyReference");
            List<String> uris = new ArrayList<>();
            for (int i = 0; i < policyNodes.getLength(); i++) {
                String uri = policyNodes.item(i).getAttributes().getNamedItem("URI").getTextContent();
                uris.add(uri);
            }

            Map<String, Object> output = new HashMap<>();
            output.put("Selected Operation", operation);
            output.put("Extracted URIs", uris);

            outputViews.write(documentUtility.newDocument(output));

        } catch (Exception e) {
            throw new SnapDataException(e, "The operation is null or non-existed " + e.getMessage());
        }
//        endPoint = propertyValues.get(PropertyCategory.SETTINGS, ENDPOINT);
    }

    @Override
    public void execute() throws ExecutionException, SnapDataException {
//        String username = "SAAS.IMP2";
//        String password = "welcome123";
//
//        try {
//            // Create the credentials provider
//            BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
//            credsProvider.setCredentials(
//                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
//                    new UsernamePasswordCredentials(username, password)
//            );
//
//            // Create HTTP client with credentials provider
//            CloseableHttpClient httpClient = HttpClients.custom()
//                    .setDefaultCredentialsProvider(credsProvider)
//                    .build();
//
//            HttpPost httpPost = new HttpPost(WSDL_URL);
//
//            // SOAP request body
//            String soapEnvelope = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
//                    + "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\""
//                    + " xmlns:pub=\"http://xmlns.oracle.com/oxp/service/PublicReportService\">"
//                    + "    <soap:Header/>"
//                    + "    <soap:Body>"
//                    + "        <pub:runReport>"
//                    + "            <pub:reportRequest>"
//                    + "                <pub:attributeFormat>csv</pub:attributeFormat>"
//                    + "                <pub:parameterNameValues>"
//                    + "                    <pub:item>"
//                    + "                        <pub:name>pPERSON_NUMBER</pub:name>"
//                    + "                        <pub:values>"
//                    + "                            <pub:item>7</pub:item>"
//                    + "                        </pub:values>"
//                    + "                    </pub:item>"
//                    + "                </pub:parameterNameValues>"
//                    + "                <pub:reportAbsolutePath>/Custom/Person Details/Person Detail Report.xdo</pub:reportAbsolutePath>"
//                    + "                <pub:sizeOfDataChunkDownload>-1</pub:sizeOfDataChunkDownload>"
//                    + "            </pub:reportRequest>"
//                    + "            <pub:appParams>?</pub:appParams>"
//                    + "        </pub:runReport>"
//                    + "    </soap:Body>"
//                    + "</soap:Envelope>";
//
//            // Headers
//            httpPost.setHeader("Content-Type", "application/soap+xml");
//
//            StringEntity entity = new StringEntity(soapEnvelope, "UTF-8");
//            httpPost.setEntity(entity);
//
//            // Execute the request
//            HttpResponse response = httpClient.execute(httpPost);
//
//            // Response status code
//            int statusCode = response.getStatusLine().getStatusCode();
//            System.out.println(statusCode);
//
//            // Get the response entity (the SOAP envelope)
//            HttpEntity responseEntity = response.getEntity();
//            if (responseEntity != null) {
//                String responseString = EntityUtils.toString(responseEntity);
//                //System.out.println(responseString); if the xml needs to be printed
//
//                // Convert the SOAP XML response to JSON
//                JSONObject jsonResponse = XML.toJSONObject(responseString);
//                System.out.println(jsonResponse.toString(4));
//
//                // Access specific fields in the JSON
//                JSONObject envelope = jsonResponse.getJSONObject("env:Envelope");
//                JSONObject body = envelope.getJSONObject("env:Body");
//                // Access the response data from the body
//                if (body.has("runReportResponse")) {
//                    JSONObject reportResponse = body.getJSONObject("runReportResponse");
//                    System.out.println("Report Response Data: " + reportResponse.toString());
//                }
//
//            }
//            httpClient.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        outputViews.write(documentUtility.newDocument("test123"));

         BasicCredentialsProvider credsProvider;
         CloseableHttpClient httpClient;
         HttpPost httpPost;
         String soapEnvelope;
         StringEntity entity;
         CloseableHttpResponse response;
         String responseString;

        try {
            // Credentials setup
            credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials("SAAS.IMP2", "welcome123")
            );

            // HTTP client setup
            httpClient = HttpClients.custom()
                    .setDefaultCredentialsProvider(credsProvider)
                    .build();

            // Prepare SOAP request
            httpPost = new HttpPost(endPoint);

            soapEnvelope = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\""
                    + " xmlns:pub=\"http://xmlns.oracle.com/oxp/service/PublicReportService\">"
                    + "    <soap:Header/>"
                    + "    <soap:Body>"
                    + "        <pub:runReport>"
                    + "            <pub:reportRequest>"
                    + "                <pub:attributeFormat>csv</pub:attributeFormat>"
                    + "                <pub:parameterNameValues>"
                    + "                    <pub:item>"
                    + "                        <pub:name>pPERSON_NUMBER</pub:name>"
                    + "                        <pub:values>"
                    + "                            <pub:item>7</pub:item>"
                    + "                        </pub:values>"
                    + "                    </pub:item>"
                    + "                </pub:parameterNameValues>"
                    + "                <pub:reportAbsolutePath>/Custom/Person Details/Person Detail Report.xdo</pub:reportAbsolutePath>"
                    + "                <pub:sizeOfDataChunkDownload>-1</pub:sizeOfDataChunkDownload>"
                    + "            </pub:reportRequest>"
                    + "            <pub:appParams>?</pub:appParams>"
                    + "        </pub:runReport>"
                    + "    </soap:Body>"
                    + "</soap:Envelope>";

            entity = new StringEntity(soapEnvelope, "UTF-8");
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/soap+xml");

            // Execute the SOAP request
            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("Response Status Code: " + statusCode);

            // Check if response is successful
            if (statusCode == 200) {
                // Process the response
                responseString = EntityUtils.toString(response.getEntity());

                // Convert the SOAP XML response to JSON
                JSONObject jsonResponse = XML.toJSONObject(responseString);

                // Map to store output data
                Map<String, String> data = new LinkedHashMap<>();
                data.put("status_code", String.valueOf(statusCode));
                data.put("response", jsonResponse.toString());
                outputViews.write(documentUtility.newDocument(data));

            } else {
                System.out.println("Failed to get a valid response. Status Code: " + statusCode);
            }
            httpClient.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup() throws ExecutionException {

    }
}
