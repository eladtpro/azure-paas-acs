package com.function;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.OnlineMeeting;
import com.microsoft.graph.models.Request;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.serializer.OffsetDateTimeSerializer;
import com.azure.identity.DeviceCodeCredential;
import com.azure.identity.DeviceCodeCredentialBuilder;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class CreateTeamsMeeting {
    /**
     * This function listens at endpoint "/api/CreateTeamsMeeting". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/CreateTeamsMeeting
     * 2. curl {your host}/api/CreateTeamsMeeting?name=HTTP%20Query
     */
    @FunctionName("CreateTeamsMeeting")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        String qClient = request.getQueryParameters().get("client");
        String client = request.getBody().orElse(qClient);
        String qTenant = request.getQueryParameters().get("tenant");
        String tenant = request.getBody().orElse(qTenant);

        final String clientId = client;
        final String tenantId = tenant; // or "common" for multi-tenant apps
        final List<String> scopes = Arrays.asList("User.Read");

        final DeviceCodeCredential credential = new DeviceCodeCredentialBuilder()
            .clientId(clientId).tenantId(tenantId).challengeConsumer(challenge -> {
                // Display challenge to the user
                System.out.println(challenge.getMessage());
            }).build();

        if (null == scopes || null == credential) {
            throw new Exception("Unexpected error");
        }
        final TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(
            scopes, credential);


        final GraphServiceClient<Request> graphClient = GraphServiceClient.builder()
            .authenticationProvider(authProvider)
            .buildClient();


        OnlineMeeting onlineMeeting = new OnlineMeeting();
        onlineMeeting.startDateTime = OffsetDateTimeSerializer.deserialize("2019-07-12T21:30:34.2444915+00:00");
        onlineMeeting.endDateTime = OffsetDateTimeSerializer.deserialize("2019-07-12T22:00:34.2464912+00:00");
        onlineMeeting.subject = "User Token Meeting";

        graphClient.me().onlineMeetings()
            .buildRequest()
            .post(onlineMeeting);


        
        if (client == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
        } else {
            return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + client).build();
        }
    }
}
