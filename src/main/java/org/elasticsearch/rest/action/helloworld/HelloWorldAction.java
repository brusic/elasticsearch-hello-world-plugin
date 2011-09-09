package org.elasticsearch.rest.action.helloworld;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.get.GetField;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentBuilderString;
import org.elasticsearch.rest.*;

import java.io.IOException;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestStatus.NOT_FOUND;
import static org.elasticsearch.rest.RestStatus.OK;
import static org.elasticsearch.rest.action.support.RestXContentBuilder.restContentBuilder;

public class HelloWorldAction extends BaseRestHandler {

    public static String INDEX = "example";
    public static String TYPE = "person";

    @Inject public HelloWorldAction(Settings settings, Client client, RestController controller) {
        super(settings, client);

        // Define REST endpoints
        controller.registerHandler(GET, "/_hello/", this);
        controller.registerHandler(GET, "/_hello/{name}", this);
    }

    public void handleRequest(final RestRequest request, final RestChannel channel) {
        logger.debug("HelloWorldAction.handleRequest called");

        final String name = request.hasParam("name") ? request.param("name") : "world";

        final GetRequest getRequest = new GetRequest(INDEX, TYPE, name);
        getRequest.listenerThreaded(false);
        getRequest.operationThreaded(true);

        String[] fields = {"msg"};
        getRequest.fields(fields);

        client.get(getRequest, new ActionListener<GetResponse>() {
            @Override public void onResponse(GetResponse response) {

                try {
                    XContentBuilder builder = restContentBuilder(request);
                    GetField field = response.field("msg");
                    String greeting = (field!=null) ? (String)field.values().get(0) : "Sorry, do I know you?";
                    builder
                        .startObject()
                        .field(new XContentBuilderString("hello"), name)
                        .field(new XContentBuilderString("greeting"), greeting)
                        .endObject();

                    if (!response.exists()) {
                        channel.sendResponse(new XContentRestResponse(request, NOT_FOUND, builder));
                    } else {
                        channel.sendResponse(new XContentRestResponse(request, OK, builder));
                    }
                } catch (Exception e) {
                    onFailure(e);
                }
            }

            @Override public void onFailure(Throwable e) {
                try {
                    channel.sendResponse(new XContentThrowableRestResponse(request, e));
                } catch (IOException e1) {
                    logger.error("Failed to send failure response", e1);
                }
            }
        });
    }
}
