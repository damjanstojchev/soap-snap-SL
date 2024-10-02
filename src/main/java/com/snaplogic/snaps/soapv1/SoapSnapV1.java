package com.snaplogic.snaps.soapv1;

import com.snaplogic.api.ConfigurationException;
import com.snaplogic.api.ExecutionException;
import com.snaplogic.api.Snap;
import com.snaplogic.common.properties.builders.PropertyBuilder;
import com.snaplogic.snap.api.*;
import com.snaplogic.snap.api.capabilities.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.inject.Inject;

@General(title = "Soap Snap v1", purpose = "Soap Requests", author = "Baselinked")
@Inputs(min = 0, max = 1, accepts = {ViewType.DOCUMENT})
@Outputs(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Errors(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Version(snap = 1)
@Category(snap = SnapCategory.READ)
public class SoapSnapV1 implements Snap {

    private static final String WSDL_URL = "wsdl url";
    private static final String SERVICE_NAME = "Service Name"; // so suggest
    private static final String ENDPOINT = "Endpoint"; // so suggest
    private static final String OPERATION = "Operation"; // so suggest

    private static NodeList nodeList;
    private static XmlHandlerImpl xmlHandler = new XmlHandlerImpl();

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
                    Document document = propertyValues.get(PropertyCategory.SETTINGS, WSDL_URL);
                    try {
                        //suggestionBuilder.node(SERVICE_NAME).suggestions(xmlHandler.loadWsdl());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .required()
                .add();

        propertyBuilder.describe(ENDPOINT, "Select specific endpoint")
                .withSuggestions((suggestionBuilder, propertyValues) -> {
                    String wsdlUrl = propertyValues.get(PropertyCategory.SETTINGS, SERVICE_NAME);
                    try {
                        //suggestionBuilder.node(ENDPOINT).suggestions(xmlHandler.extractEndpoints(wsdlUrl));
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to load WSDL and extract service name", e);
                    }
                })
                .required()
                .add();

        propertyBuilder.describe(OPERATION, "Select specific operation")
                .withSuggestions((suggestionBuilder, propertyValues) -> {
                    String operation = propertyValues.get(PropertyCategory.SETTINGS, ENDPOINT);
                    if (operation != null && !operation.isEmpty()) {
                        suggestionBuilder.node(OPERATION).suggestions(operation);
                    }
                })
                .required()
                .add();
    }

    @Override
    public void configure(PropertyValues propertyValues) throws ConfigurationException {

    }

    @Override
    public void execute() throws ExecutionException, SnapDataException {

    }

    @Override
    public void cleanup() throws ExecutionException {

    }
}
