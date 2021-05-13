package test.azure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.specialized.BlockBlobClient;

import reactor.core.publisher.Mono;


public class Main {

	public static final String storageConnectionString =
		    "DefaultEndpointsProtocol=http;" +
		    "AccountName=your_storage_account;" +
		    "AccountKey=your_storage_account_key";
	
	public static final String containername="your_container_name";
	public static final long UNKNOWN_LNEGTH = -1;
	public static final String username = "your_username";
	public static final String password = "your_password";
	public static final String FROCR_ENDPOINT_URL = "https://af-hrb-complete-it/api/ExtractFomfields/";
	
	public static void main(String[] args) throws Exception {
		
		InputStream stream = null;
		String fileName = "dummy";
		createContainer();
		String url = uploadBlobToContainerAndGetBlobURL(stream, fileName);
		String responseCode = callGETAPIFOROCR(url); //callFROCRAPI(url);
	}
	
	
	private static void createContainer() {
		BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(storageConnectionString)
			    .buildClient();
		
		BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containername);
		if(!blobContainerClient.exists())
			blobContainerClient.create();
	}

	private static String uploadBlobToContainerAndGetBlobURL(InputStream stream, String fileName) {
		BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(storageConnectionString)
			    .buildClient();
		
		BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containername);
		
		String myBlobName = generateFileName(fileName);
		BlockBlobClient blockBlobClient = blobContainerClient.getBlobClient(myBlobName).getBlockBlobClient();
		blockBlobClient.upload(stream,UNKNOWN_LNEGTH);
		String url = blockBlobClient.getBlobUrl();
		
		
		return url;
	}

	public static int callFROCRAPI(String url) throws Exception  {

		HttpPost post = new HttpPost(FROCR_ENDPOINT_URL);

        // add request parameter, form parameters
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", username));
        urlParameters.add(new BasicNameValuePair("password", password));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {

        	return response.getStatusLine().getStatusCode();
            
        }
	}
	private static String callGETAPIFOROCR(String url) throws IOException {
		URL obj = new URL(FROCR_ENDPOINT_URL+"?BlobUrl="+url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		int responseCode = con.getResponseCode();
		System.out.println("GET Response Code :: " + responseCode);
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			// This is just to extract the response not really required 
			// IF 200 comes you can do what next we need to do
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			// print result
			System.out.println(response.toString());
			return response.toString();
		} else {
			System.out.println("GET request not worked");
		}
		return null;

	}
	/*
	 * Here we need to write a logic used in your application to generate file name
	 */
	private static String generateFileName(String fileName) {
		String name = fileName +".pdf";
		return name;
	}
}
