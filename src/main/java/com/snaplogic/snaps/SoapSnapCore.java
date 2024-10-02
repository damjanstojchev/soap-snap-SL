    package com.snaplogic.snaps;

import com.snaplogic.api.ConfigurationException;
import com.snaplogic.api.ExecutionException;
import com.snaplogic.api.Snap;
import com.snaplogic.common.SnapType;
import com.snaplogic.common.properties.builders.PropertyBuilder;
import com.snaplogic.snap.api.*;
import com.snaplogic.snap.api.capabilities.*;

import javax.inject.Inject;
import java.util.Collections;

@General(title = "Soap Snap Core", purpose = "Soap Requests", author = "Baselinked")
@Inputs(min = 0, max = 1, accepts = {ViewType.DOCUMENT})
@Outputs(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Errors(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Version(snap = 1)
@Category(snap = SnapCategory.READ)
public class SoapSnapCore implements Snap {
    private static final String WSDL_URL = "wsdl url";
    private static final String SERVICE_NAME = "Service Name"; // so suggest
    private static final String ENDPOINT = "Endpoint"; // so suggest
    private static final String OPERATION = "Operation"; // so suggest

    @Inject
    private DocumentUtility documentUtility;
    @Inject
    private OutputViews outputViews;
    @Inject
    private ErrorViews errorViews;

    @Override
    public void defineProperties(PropertyBuilder propertyBuilder) {
        propertyBuilder.describe(WSDL_URL, "Insert the wsdl url")
                .type(SnapType.STRING)
                .required()
                .add();

        propertyBuilder.describe(SERVICE_NAME, "Extract service name if needed")
                .withSuggestions((suggestionBuilder, propertyValues) -> {
                    String serviceName = propertyValues.get(PropertyCategory.SETTINGS, SERVICE_NAME);
                    if (serviceName != null && !serviceName.isEmpty()) {
                        suggestionBuilder.node(SERVICE_NAME).suggestions(String.valueOf(Collections.singletonList(serviceName)));
                    }
                })
                .required()
                .add();

        propertyBuilder.describe(ENDPOINT, "Select specific endpoint")
                .type(SnapType.STRING)
                .withSuggestions((suggestionBuilder, propertyValues) -> {
                    String endpoint = propertyValues.get(PropertyCategory.SETTINGS, ENDPOINT);
                if (endpoint != null && !endpoint.isEmpty()) {
                    suggestionBuilder.node(SERVICE_NAME).suggestions(String.valueOf(Collections.singletonList(endpoint)));
                    }
                })
                .required()
                .add();

        propertyBuilder.describe(OPERATION, "Select specific operation")
                .type(SnapType.STRING)
                .withSuggestions((suggestionBuilder, propertyValues) -> {
                    String operation = propertyValues.get(PropertyCategory.SETTINGS, ENDPOINT);
                    if (operation != null && !operation.isEmpty()) {
                        suggestionBuilder.node(OPERATION).suggestions(String.valueOf(Collections.singletonList(operation)));
                    }
                })
                .required()
                .add();
    }

    @Override
    public void configure(PropertyValues propertyValues) throws ConfigurationException {
    }

    @Override
    public void execute() throws ExecutionException {

    }

    @Override
    public void cleanup() throws ExecutionException {

    }
}
