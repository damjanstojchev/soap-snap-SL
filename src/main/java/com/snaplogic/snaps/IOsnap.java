package com.snaplogic.snaps;

import com.snaplogic.api.ConfigurationException;
import com.snaplogic.api.ExecutionException;
import com.snaplogic.api.Snap;
import com.snaplogic.common.SnapType;
import com.snaplogic.common.properties.builders.PropertyBuilder;
import com.snaplogic.snap.api.*;
import com.snaplogic.snap.api.capabilities.*;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;

@General(title = "SnapD", purpose = "Generates documents based on the configuration",
        author = "Your Company Name", docLink = "http://yourdocslinkhere.com")
@Inputs(min = 0, max = 1, accepts = {ViewType.DOCUMENT})
@Outputs(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Errors(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Version(snap = 1)
@Category(snap = SnapCategory.READ)
public class IOsnap implements Snap {
    private static final String COUNT = "Count";
    private static final String WORD = "Word";
    private static final Document NO_DOCUMENT = null;

    @Inject
    private DocumentUtility documentUtility;
    @Inject
    private OutputViews outputViews;
    @Inject
    private ErrorViews errorViews;

    private int count;
    private String word;

    @Override
    public void defineProperties(PropertyBuilder propertyBuilder) {
        propertyBuilder.describe(COUNT, "Number of times to repeat the word")
                .type(SnapType.STRING)
                .required()
                .add();

        propertyBuilder.describe(WORD, "The word to repeat")
                .type(SnapType.STRING)
                .required()
                .add();
    }

    @Override
    public void configure(PropertyValues propertyValues) throws ConfigurationException {
        String countValueExp = propertyValues.get(COUNT);
        try {
            count = Integer.parseInt(countValueExp);
        } catch (NumberFormatException e) {
            throw new ConfigurationException(e, "Invalid number format for count: " + countValueExp);
        }

        word = propertyValues.get(WORD).toString();
    }

    @Override
    public void execute() throws ExecutionException {
        if (count < 0) {
            SnapDataException snapDataException =
                    new SnapDataException(String.format("Invalid count value %d", count))
                            .withReason("Value of count property cannot be negative")
                            .withResolution("Ensure the count property is greater than or equal to zero");
            errorViews.write(snapDataException);
            return;
        }

        for (int i = 0; i < count; i++) {
            Map<String, String> data = new LinkedHashMap<>();
            data.put("key", word + (i + 1));
            outputViews.write(documentUtility.newDocument(data));
        }
    }

    @Override
    public void cleanup() throws ExecutionException {
    }
}
