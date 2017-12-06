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


import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@WebServlet(
        name = "MainServlet",
        urlPatterns = {"/"}
    )
public class MainPage extends HttpServlet{

    private static final String CLIENT_ID = System.getenv("SALESFORCE_CLIENT_ID");
    private static final String CLIENT_SECRET = System.getenv("SALESFORCE_CLIENT_SECRET");
    private static final String MODE = System.getenv("SALESFORCE_MODE");
    private static final String COMMUNITY = System.getenv("SALESFORCE_COMMUNITY");
    private static final String FORGOT_PASSWORD = System.getenv("SALESFORCE_FORGOT_PASSWORD_ENABLED");
    private static final String SELF_REGISTER = System.getenv("SALESFORCE_SELF_REGISTER_ENABLED");
    private static final String HEROKUAPP_URL = System.getenv("SALESFORCE_HEROKUAPP_URL");
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws 
        ServletException, IOException {

        String code = request.getParameter("code");
        if (code != null) {
            code = URLDecoder.decode(code, "UTF-8");
        }
        String startURL = request.getParameter("state");
        if (startURL != null) {
            startURL = URLDecoder.decode(startURL, "UTF-8");
        }

        response.setContentType("text/html; charset=utf-8");
        PrintWriter out = response.getWriter();

        String outputStr =  "<html><head>\n" +
		   "<meta charset=\"utf-8\">
		    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">
		    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">
		    <title>FIX, curated coffee components</title>";
		outputStr  +="
		    <link href="reset.css" rel="stylesheet">
		    <link href="//fonts.googleapis.com/css?family=Source+Sans+Pro:200,300,600" type="text/css" rel="stylesheet">
		    <link href="main.css" rel="stylesheet">"
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