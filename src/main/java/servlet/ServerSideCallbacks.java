package servlet;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import org.json.JSONObject;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@WebServlet(
        name = "CallbackServlet2",
        urlPatterns = {"/_callback"}
    )
public class ServerSideCallbacks extends HttpServlet{

// Client ID
    private static final String CLIENT_ID = System.getenv("SALESFORCE_CLIENT_ID");

// client secret
    private static final String CLIENT_SECRET = System.getenv("SALESFORCE_CLIENT_SECRET");
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws 
        ServletException, IOException {

    		System.setProperty("javax.net.ssl.trustStore",".jdk/jre/lib/security/cacerts");
    		System.setProperty("javax.net.ssl.trustStorePassword", System.getenv("CACERTS_PASSWORD"));
        String code = request.getParameter("code");
        if (code != null) {
            code = URLDecoder.decode(code, "UTF-8");
        }
        String startURL = request.getParameter("state");
        if (startURL != null) {
            startURL = URLDecoder.decode(startURL, "UTF-8");
        }
                
        String tokenResponse = null;
        String communityUrl = null;
        HttpClient httpclient = new HttpClient();
        
        try {
            // community_url parameter passed from redirect uri.
            communityUrl = request.getParameter("sfdc_community_url");
            // Token endpoint : communityUrl + "/services/oauth2/token";
            PostMethod post = new PostMethod(communityUrl+"/services/oauth2/token");
            post.addParameter("code",code);
            post.addParameter("grant_type","authorization_code");
            // Consumer key of the Connected App.
            post.addParameter("client_id", CLIENT_ID);
            // Consumer Secret of the Connected App.
            post.addParameter("client_secret",CLIENT_SECRET);
            
            // Callback URL of the Connected App.
            post.addParameter("redirect_uri", "https://" +  System.getenv("SALESFORCE_HEROKUAPP_URL") + "/_callback");
            
    			System.out.println("Attempting to POST to token endpoint: " + post.getPath());	
            httpclient.executeMethod(post);
            tokenResponse = post.getResponseBodyAsString();
            post.releaseConnection();
 
            System.err.println("tokenResponse: " + tokenResponse);
        } catch (Exception e) {
        		throw new ServletException(e);
        }

        JSONObject identityJSON = null;
        try {
        		System.out.println("Attempting to parse token response: " + tokenResponse);
            JSONObject token = new JSONObject(tokenResponse);
            String accessToken = token.getString("access_token");
            String identity = token.getString("id");
            httpclient = new HttpClient();
            GetMethod get = new GetMethod(identity + "?version=latest");
            get.setFollowRedirects(true);
            get.addRequestHeader("Authorization", "Bearer " + accessToken);
            httpclient.executeMethod(get);
            String identityResponse = get.getResponseBodyAsString();
            get.releaseConnection();
            identityJSON = new JSONObject(identityResponse);
            identityJSON.put("access_token", accessToken);
        } catch (Exception e) {
            throw new ServletException(e);
        }
        

        response.setContentType("text/html; charset=utf-8");
        PrintWriter out = response.getWriter();

        // Mode passed from redirect-uri
       //  Notice that we’re using base64 encoded
        String outputStr =  "<html><head>\n" +
                "<meta name=\"salesforce-community\" content=\""+ communityUrl +"\">\n" +
//                "<meta name=\"salesforce-mode\" content=\""+ request.getParameter("mode") +"-callback\">\n" +
				"<meta name=\"salesforce-mode\" content=\"" + System.getenv("SALESFORCE_MODE") + "-callback\">\n" +
                "<meta name=\"salesforce-server-callback\" content=\"true\">\n" +
                "<meta name=\"salesforce-server-response\" content='" + 
                Base64.getEncoder().encodeToString(identityJSON.toString().getBytes(StandardCharsets.UTF_8))+"'>\n" +
                "<meta name=\"salesforce-server-starturl\" content='" + startURL +"'>\n" +
                "<meta name=\"salesforce-target\" content= \"#salesforce-login\">\n"+
                "<meta name=\"salesforce-allowed-domains\" content=\"" + System.getenv("SALESFORCE_HEROKUAPP_URL") + "\">\n" +
                "<script src=\""+ communityUrl +"/servlet/servlet.loginwidgetcontroller?type=javascript_widget\"" +
                " async defer></script>\n" +
                "</head><body></body></html>";
        out.write(outputStr);
    }

}