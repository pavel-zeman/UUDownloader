package cz.pavel.uudownloader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import cz.pavel.uudownloader.utils.Configuration;
import cz.pavel.uudownloader.utils.HtmlParser;
import cz.pavel.uudownloader.utils.LogUtils;

public class UUManager {
	
	private static Logger log = LogUtils.getLogger();
	private static final String UIS_BASE_URL = "https://uu.unicornuniverse.eu";
	
	private DefaultHttpClient httpClient;
	
	
	/** Http connection timeout (in ms) */
	private static final int HTTP_CONNECTION_TIMEOUT = 10000;
	/** Http socket read timeout (in ms) */
	private static final int HTTP_SO_TIMEOUT = 10000;
	
	// total bytes read from server
	private int totalBytes;
	
	
	public int getTotalBytes() {
		return totalBytes;
	}
	
	private static String fixUrl(String url) {
		url = url.replace("&amp;", "&");
		url = url.replace("|", "%7C");
		return url;
	}
	
	private String doGet(String url) throws ClientProtocolException, IOException {
		url = fixUrl(url);
		HttpGet httpGet = new HttpGet(UIS_BASE_URL + url);
        log.debug("Sending GET request to " + httpGet.getURI());
        HttpResponse response = httpClient.execute(httpGet);
        response.getHeaders("Content-Disposition");
        HttpEntity entity = response.getEntity();
        String data = HtmlParser.getContents(entity.getContent());
        return data;
	}

	public String goToArtifact(String pageData, String artifact) throws ClientProtocolException, IOException {
		String url = fixUrl(HtmlParser.extractRegExp(pageData, "<form onsubmit=\"return is_iphone \\|\\| is_android;\" method=\"post\" action=\"([^\"]*)\" name=\"quickfindform\" id=\"quickfindform\""));
        HttpPost httpost = new HttpPost(UIS_BASE_URL + url);
        
        List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        nvps.add(new BasicNameValuePair("ues-core-quickfind", artifact));
        httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
        
        HttpResponse response = httpClient.execute(httpost);
        HttpEntity entity = response.getEntity();
        String data = HtmlParser.getContents(entity.getContent());
        return data;
	}
	
	public String goToAttachments(String data) throws ClientProtocolException, IOException {
		String url = HtmlParser.extractRegExp(data, "<a href=\"([^\"]*UC104000)\">");
		return doGet(url);
	}
	
	private void copyStream(InputStream input, OutputStream output) throws IOException {
		byte [] buffer = new byte[1024 * 1024];
        int read = input.read(buffer);
        while (read > 0) {
        	output.write(buffer, 0, read);
        	read = input.read(buffer);
        }
	}
	
	public void downloadAttachments(String data, File targetDir) throws ClientProtocolException, IOException {
		String id = HtmlParser.extractRegExp(data, "<SPAN class=\"last-child\" id=\"([^\"]*)\">");
		String url = fixUrl(HtmlParser.extractRegExp(data, "href:\"([^\"]*)\",pushed:false,disabled:false},\"" + id + "\""));
		HttpGet httpGet = new HttpGet(UIS_BASE_URL + url);
        log.debug("Sending GET request to " + httpGet.getURI());
        HttpResponse response = httpClient.execute(httpGet);
        Header[] headers = response.getHeaders("Content-Disposition");
        String headerValue = headers[0].getValue();
        String file = URLDecoder.decode(HtmlParser.extractRegExp(headerValue, "attachment; filename\\*=UTF-8''(.*)$"), "UTF-8");
        
        // read file ...
        File downloadedFile = new File(targetDir, file);
        FileOutputStream fos = new FileOutputStream(downloadedFile);
        HttpEntity entity = response.getEntity();
        BufferedInputStream bis = new BufferedInputStream(entity.getContent());
        copyStream(bis, fos);
        fos.close();
        bis.close();
        
        // unzip it ...
        ZipFile zipFile = new ZipFile(downloadedFile);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
        	ZipEntry entry = entries.nextElement();
        	fos = new FileOutputStream(new File(targetDir, entry.getName()));
        	InputStream zipInputStream = zipFile.getInputStream(entry); 
        	copyStream(zipInputStream, fos);
        	zipInputStream.close();
        	fos.close();
        }
        zipFile.close();
        
        // and delete it 
        downloadedFile.delete(); // if the deletion fails, false is returned (there is no exception), but we can ignore it here
	}
	

	/**
	 * Initializes Apache http client.
	 */
	public void initHttpClient() {
		// if the client was not initialized, do it now
		if (httpClient == null) {
			// initialize client and set timeouts
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, HTTP_CONNECTION_TIMEOUT);
			HttpConnectionParams.setSoTimeout(httpParams, HTTP_SO_TIMEOUT);
			httpClient = new DefaultHttpClient(httpParams);
			
			// add support for gzip compression (request header)
			httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
	            @Override
				public void process(final HttpRequest request, final HttpContext context) {
	                if (!request.containsHeader("Accept-Encoding")) {
	                    request.addHeader("Accept-Encoding", "gzip");
	                }
	            }
	        });
			
			// add support for gzip compression (response interceptor)
			httpClient.addResponseInterceptor(new HttpResponseInterceptor() {
                @Override
				public void process(final HttpResponse response, final HttpContext context) {
                    HttpEntity entity = response.getEntity();
                    totalBytes += entity.getContentLength();
                    Header ceheader = entity.getContentEncoding();
                    if (ceheader != null) {
                        HeaderElement[] codecs = ceheader.getElements();
                        for (int i = 0; i < codecs.length; i++) {
                            if (codecs[i].getName().equalsIgnoreCase("gzip")) {
                                response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                                return;
                            }
                        }
                    }
                }
            });
		}
	}
	
	/**
	 * Logs in the UU using the access codes from configuration.
	 * @return Data of the first page after login.
	 * @throws ClientProtocolException Thrown by Apache httpclient.
	 * @throws IOException Thrown by Apache httpclient.
	 */
	public String logIn() throws ClientProtocolException, IOException {
		return logIn(
				Configuration.getEncryptedString(Configuration.Parameters.UU_ACCESS_CODE1), 
				Configuration.getEncryptedString(Configuration.Parameters.UU_ACCESS_CODE2)
				);		
	}
	
	/**
	 * Logs in to Unicorn Universe.
	 * 
	 * @return Contents of the first HTML page after successful logon.
	 */
	private String logIn(String accessCode1, String accessCode2) throws ClientProtocolException, IOException {
        HttpPost httpost = new HttpPost(UIS_BASE_URL + "/ues/sesm");
        
        List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        nvps.add(new BasicNameValuePair("UES_AccessCode1", accessCode1));
        nvps.add(new BasicNameValuePair("UES_AccessCode2", accessCode2));
        nvps.add(new BasicNameValuePair("UES_SecurityRealm", "unicornuniverse.eu"));
        nvps.add(new BasicNameValuePair("loginURL", "http://unicornuniverse.eu"));
        httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
        
        HttpResponse response = httpClient.execute(httpost);
        HttpEntity entity = response.getEntity();
        
        log.debug("Sending POST request to " + httpost.getURI());
        String result = HtmlParser.getContents(entity.getContent());
        if (result.indexOf("\"a_toolBar-system-back\"") < 0) {
        	log.error("Invalid page after login, incorrect access code 1 or 2?");
        	throw new RuntimeException("Probably incorrect UU access code 1 or 2");
        }
        
        return result;
	}
	
	/**
	 * Verifies, whether access codes are valid by trying to log in. Throws RuntimeException,
	 * if they are not valid.
	 * @param accessCode1 access code 1
	 * @param accessCode2 access code 2
	 * @throws ClientProtocolException Thrown by Apache httpclient
	 * @throws IOException Thrown by Apache httpclient
	 */
	public void checkAccessCodes(String accessCode1, String accessCode2) throws ClientProtocolException, IOException {
		initHttpClient();
		logIn(accessCode1, accessCode2);
	}
	
	

}
