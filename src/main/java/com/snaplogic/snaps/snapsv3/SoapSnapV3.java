package com.snaplogic.snaps.snapsv3;

import com.google.inject.Inject;
import com.snaplogic.api.ConfigurationException;
import com.snaplogic.api.ExecutionException;
import com.snaplogic.api.Snap;
import com.snaplogic.common.properties.builders.PropertyBuilder;
import com.snaplogic.snap.api.*;
import com.snaplogic.snap.api.capabilities.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static String wsdlUrl;
    private static String serviceName;
    private static String endPoint;
    private static String operation;

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

    }

    @Override
    public void cleanup() throws ExecutionException {

    }
}
