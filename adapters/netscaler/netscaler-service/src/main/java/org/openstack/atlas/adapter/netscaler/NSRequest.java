package org.openstack.atlas.adapter.netscaler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.jersey.api.client.*;
import javax.ws.rs.core.Response.Status;

public class NSRequest
{
    private static final String GET = "GET";
    private static final String PUT = "PUT";
    private static final String DELETE = "DELETE";
    private static final String POST = "POST";
    
    public static Log LOG = LogFactory.getLog(NetScalerAdapterImpl.class.getName());

    static String perform_request(String method, String urlStr, Map<String,String> headers, String body)
    throws IOException
    {
    	
    	ClientResponse response;
    	
        // sigh.  openConnection() doesn't actually open the connection,
        // just gives you a URLConnection.  connect() will open the connection.

        LOG.debug("[issuing request: " + method + " " + urlStr + "]");

        Client client = Client.create();
        WebResource  resource = client.resource(urlStr);

        WebResource.Builder resourceBuilder = resource.getRequestBuilder();
        
       
        URL url = new URL(urlStr);
        
        //HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        //connection.setRequestMethod(method);
        
        LOG.debug("Before writing headers...");

        
        LOG.debug("writing headers");
        // write  headers
        for (Map.Entry<String,String> header : headers.entrySet())
        {
        	LOG.debug(header.getKey() +  ":" + header.getValue());
        	//connection.setRequestProperty(header.getKey(), header.getValue());
            resourceBuilder.header(header.getKey(), header.getValue());
        }

        LOG.debug("Before writing body...");
        
        /*
         
        // write body if we're doing POST or PUT
        byte buffer[] = new byte[8192];
        int read = 0;

        if (body != null)
        {
            LOG.debug("Before getting output stream...");

            connection.setDoOutput(true);

            LOG.debug("After setting output...");
          
             
			InputStream body;

			try {
				request = new ByteArrayInputStream(body.getBytes("UTF-8"));
			} catch (Exception e) {
				throw new RemoteException(
						"UTF-8 is not recognized as a valid charset encoding !!");
			}

			OutputStream output;

			try {
				output = connection.getOutputStream();
			} catch (Exception e) {
				LOG.debug("Exception thrown: " + e.getMessage());
				throw new IOException(e.getMessage());
			}

			LOG.debug("Got output stream...");

			while ((read = request.read(buffer)) != -1) {
				LOG.debug("Before writing buffer to output stream...");

				output.write(buffer, 0, read);
				LOG.debug("After writing buffer to output stream...");

			}
        }
        */
        
        response = null;
        
        if (method.toUpperCase() == "GET")
        {
        	LOG.debug("Doing a GET request...");
        	
        	response = resourceBuilder.get(ClientResponse.class);
        }
        
        if (method.toUpperCase() == "POST")
        {
        	LOG.debug("Doing a POST request...");
        	response = resourceBuilder.post(ClientResponse.class, body);
        }
        
        
        if (method.toUpperCase() == "PUT")
        {
        	LOG.debug("Doing a PUT request...");
        	response = resourceBuilder.put(ClientResponse.class, body);
        }
        
        if (method.toUpperCase() == "DELETE")
        {
        	LOG.debug("Doing a DELETE request...");
        	response = resourceBuilder.delete(ClientResponse.class);
        }
        
        String resp_body;
        
        if (response != null)
        {
        	int statuscode = response.getStatus();
        	

        	LOG.debug("Status code of response is: " + statuscode);
        
        	resp_body = response.getEntity(String.class);
        	LOG.debug("Response body: " + resp_body);
        	
        	if (statuscode != Status.ACCEPTED.getStatusCode() && statuscode != Status.CREATED.getStatusCode())
        	{
        		throw new IOException("Error : " + resp_body);
        	}
        	
        } else {
        	LOG.debug("response was set to null");
        	resp_body = null;
        }
        
        /*
        // do request
        long time = System.currentTimeMillis();
        LOG.debug("Before connecting...");
        //connection.connect();
        LOG.debug("After connecting...");        
        
        //InputStream responseBodyStream;
        
        try {
           //responseBodyStream = connection.getInputStream();
        } catch (IOException e)
        {
        	LOG.debug("Received exception while reading input stream: " + e.getMessage());
        	throw e;
        }
        
        StringBuffer responseBody = new StringBuffer();
        while ((read = responseBodyStream.read(buffer)) != -1)
        {
           responseBody.append(new String(buffer, 0, read));
        }
        
        connection.disconnect();
        time = System.currentTimeMillis() - time;
        
        // start printing output

        LOG.debug("[read " + responseBody.length() + " chars in " + time + "ms]");
        
        // look at headers
        // the 0th header has a null key, and the value is the response line ("HTTP/1.1 200 OK" or whatever)

        String header = null;
        String headerValue = null;
        
        int index = 0;
        
        while ((headerValue = connection.getHeaderField(index)) != null)
        {
            header = connection.getHeaderFieldKey(index);
            
            if (header == null)
                LOG.debug("header value:" + headerValue);
            else
                LOG.debug("header  " + header + ": " + headerValue);
            
            index++;
        }
        
        LOG.debug("");
        LOG.debug(responseBody);   


        return responseBody.toString();
        */
        
        return resp_body;
    }
}
