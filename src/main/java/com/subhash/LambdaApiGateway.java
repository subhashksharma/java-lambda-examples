package com.subhash;

import com.amazonaws.util.StringUtils;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpStatus;

public class LambdaApiGateway implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {


        final LambdaLogger  logger = context.getLogger();
        logger.log(" API full log event "+ apiGatewayProxyRequestEvent.toString());

        String body = apiGatewayProxyRequestEvent.getBody();
        final User user = gson.fromJson(body, User.class);

        if(StringUtils.isNullOrEmpty(user.getUsername()) || StringUtils.isNullOrEmpty(String.valueOf(user.getId()))){
            //throw new RuntimeException(" UserDetails are not correct or may be empty");
           return apiResponseMethod(HttpStatus.SC_BAD_REQUEST, Constants.CLIENT_ERROR_CODE,
            "Username was blank", "request body was not valid", logger );
        }
        //server error
        try {
            performOperation(user);
        } catch (Exception e){
            return apiResponseMethod(HttpStatus.SC_INTERNAL_SERVER_ERROR, "DB Call failed",
                    Constants.SERVER_ERROR_MESSAGE + user.getUsername(), Constants.SERVER_ERROR_CODE, logger);
        }
        //success reponse
        return apiResponseMethod(HttpStatus.SC_OK, "SUCCESS", null, null, logger);
    }

    void performOperation(User user){
        //DB operation or calling a new API
        if(user.getId() == 101){
            //error
            throw new RuntimeException("User is not valid");
        }
    }

    public APIGatewayProxyResponseEvent apiResponseMethod(int status, String errorCode,
                                                          String errorMessage, String responseBody,

                                                          LambdaLogger logger) {

        Error error = new Error();
        error.setErrorCode(errorCode);
        error.setErrorMessage(errorMessage);
        APIGatewayProxyResponseEvent returnResponse = new APIGatewayProxyResponseEvent()
                                    .withStatusCode(status)
                                    .withBody(gson.toJson(new Response<String>(status, responseBody, error )));

        logger.log(returnResponse.toString());
        return  returnResponse;

    }
}
