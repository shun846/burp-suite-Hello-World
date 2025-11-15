package example.helloworld;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.http.handler.HttpHandler;
import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.http.handler.RequestToBeSentAction;
import burp.api.montoya.http.handler.ResponseReceivedAction;
import burp.api.montoya.core.Annotations;
import burp.api.montoya.core.HighlightColor;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.params.HttpParameter;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.Http;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.responses.HttpResponse;





import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;


public class HelloWorld implements BurpExtension {

    @Override
    public void initialize(MontoyaApi api) {
        // set extension name
        api.extension().setName("Hello world First extension");
        api.http().registerHttpHandler(new HelloWorldExample(api));
    }
}

class HelloWorldExample implements HttpHandler {
    private final Logging logging;
    private final Http http; 
    private boolean flag=false;
    private String beforeUrl = null;
    private String afterUrl = null;
    
    public HelloWorldExample(MontoyaApi api) {
        this.logging = api.logging();
        this.http=api.http();
    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
        
        if(flag){
        logging.logToOutput("変更後");
        afterUrl = requestToBeSent.url().toString();
        logging.logToOutput("URL: " + afterUrl);

        }
        else{
            logging.logToOutput("変更前");
            beforeUrl = requestToBeSent.url().toString();
            logging.logToOutput("URL: " + beforeUrl);

        }
        
        // contains 使用
        if (requestToBeSent.contains("DVWA", false)) {
            logging.logToOutput("This message contain DVWA");
        }

        HttpRequest modified = requestToBeSent;


        
        // URL パラメータを追加
        // HttpParameter urlPara = HttpParameter.urlParameter("password", "password");
        // modified = modified.withParameter(urlPara);
        // logging.logToOutput("Add urlParameter: " + modified.toString());

        // URL パラメータの編集
        List<ParsedHttpParameter> now_parame = requestToBeSent.parameters();
        logging.logToOutput("Now parameter: " + now_parame.toString());
        

        List<HttpParameter> updateParame = new ArrayList<>();  
        for (ParsedHttpParameter p : now_parame) {
    
            if ("URL".equals(p.type().name()) && "id".equals(p.name())) {
        updateParame.add(HttpParameter.urlParameter("id", "admin"));
        
            } else {
        updateParame.add(HttpParameter.parameter(p.name(), p.value(), p.type()));
            }
        }

        // logging.logToOutput("変更後URL: " + modified.url());

        modified = modified.withUpdatedParameters(updateParame);
        logging.logToOutput("copy parameter: " + updateParame.toString());

        if(flag){
              return RequestToBeSentAction.continueWith(modified);
        }

        
        flag=true;
         HttpRequestResponse res=http.sendRequest(modified);
        HttpResponse rr=res.response();
        // logging.logToOutput(res.toString());
        // logging.logToOutput(rr.toString());  
        flag=false;

        compareUrl();
        // 特定パスならハイライトを付与してそのまま継続
        // if (urlString.contains("DVWA")) {
        //     Annotations ann = requestToBeSent.annotations().withHighlightColor(HighlightColor.RED);
        //     return RequestToBeSentAction.continueWith(modified);
        // }
        logging.logToOutput("");
        logging.logToOutput("");
        return RequestToBeSentAction.continueWith(modified);
    }


 private void compareUrl() {
        if (beforeUrl != null && afterUrl != null) {
            if (!beforeUrl.equals(afterUrl)) {
                logging.logToOutput("URLの変更があります");
            } else {
                logging.logToOutput("URLは変更されていません");
            }
        }
 }
    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        String urlString = responseReceived.initiatingRequest().url().toString();
        // logging.logToOutput("response URL: " + urlString);
        // logging.logToOutput("");
        String GREEN = "\u001B[32m";
        String RESET = "\u001B[0m";

        if (urlString.contains("csrf")) {
            String target = "http://localhost/DVWA/security.php";

            short status = 302;
            var modified = responseReceived
                    .withStatusCode(status)
                    .withAddedHeader("Location", target)
                    .withBody(ByteArray.byteArray(""));

            logging.logToOutput(GREEN+"response with 302 -> " + target+RESET+"\n");
            
            return ResponseReceivedAction.continueWith(modified);
        }

        return ResponseReceivedAction.continueWith(responseReceived);
    }
}


