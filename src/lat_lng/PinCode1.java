package lat_lng;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class PinCode1 {
	//private static final String GEOCODE_REQUEST_URL = "https://maps.googleapis.com/maps/api/geocode/xml?sensor=false&key=AIzaSyDHCFYArAuNywfxLW2esTlFg3GojS1j86Q";
	private static final String GEOCODE_REQUEST_URL = "http://maps.googleapis.com/maps/api/geocode/xml?sensor=false&";
	private static HttpClient httpClient = new HttpClient(
			new MultiThreadedHttpConnectionManager());

	public static void process(String input, String output, int indexAdd1,
			int indexAdd2) throws Exception {
		// example code

		BufferedReader br = new BufferedReader(new FileReader(new File(input)));
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(output),
				true));
		String line = null;
		// DoubleMetaphone doubleMetaphone = new DoubleMetaphone();
		int cnt = 0;
		while ((line = br.readLine()) != null) {
			String result = "";
			
			if (StringUtils.isNotBlank(line)) {   
				try {
					
					JSONObject json=null;
					
					  try  {
					  
						System.out.print(line);
					    json = readJsonFromUrl("http://maps.googleapis.com/maps/api/geocode/json?address="+line+"&sensor=true");
					    if(json.getJSONArray("results").getJSONObject(0).has("postcode_localities")) {
					    	
					    	JSONArray postocodeLocalities = json.getJSONArray("results").getJSONObject(0).getJSONArray("postcode_localities");
						    System.out.print(postocodeLocalities.length());
						    String[] cityNames = new String[postocodeLocalities.length()];
						    for(int i=0;i<postocodeLocalities.length();i++)
						    	cityNames[i] = (String) postocodeLocalities.get(i);
						    
						    for(int i=0;i<cityNames.length;i++) {
						    	
						    	System.out.print(cityNames[i]);
						    	bw.write(line + "|" + cityNames[i]);
						    	bw.newLine();
							    
						    }
						    
						    
					    } else {
					    	
					    	json = readJsonFromUrl("http://maps.googleapis.com/maps/api/geocode/json?address="+line+"&sensor=true");
						    
						    String cityName = json.getJSONArray("results").getJSONObject(0).getJSONArray("address_components").getJSONObject(1).getString("long_name");
						    bw.write(line + "|" + cityName);
					    	bw.newLine();
						    
						    
					    }
					    
					  }
					  
					  catch (JSONException e)  {
						  
					     e.printStackTrace();
					    
					  } 
					

					// System.out.println(line);
				} catch (Exception e) {
					e.printStackTrace();
					result = "null";
					System.out.println(line);
				}
				
			}
		}

		bw.flush();
		br.close();
		bw.close();

	}
	
	 private static String readAll(Reader rd) throws IOException 
	 {
	    StringBuilder sb = new StringBuilder();
	    int cp;
	    while ((cp = rd.read()) != -1) 
	    {
	      sb.append((char) cp);
	    }
	    return sb.toString();
	 }
	 
	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException 
	 {
	    InputStream is = new URL(url).openStream();
	    try 
	    {
	      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
	      String jsonText = readAll(rd);
	      JSONObject json = new JSONObject(jsonText);
	      return json;
	    }
	    finally 
	    {
	      is.close();
	    }
	 }

	public static void main(String[] args) {
		try {
			process("/Users/RajnishKumar/Documents/Misc/CHM/Input/input_try.txt",
					"/Users/RajnishKumar/Documents/Misc/CHM/Output/Output_1e.txt",
					1, 4);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main1(String[] args) throws Exception {
		LongLatService1 tDirectionService = new LongLatService1();
		System.out.println(tDirectionService
				.getLongitudeLatitude("Baigni Bihar JEHANABAD"));
		
	}

	public String getLongitudeLatitude(String address) {
		String res = "";
		try {
			StringBuilder urlBuilder = new StringBuilder(GEOCODE_REQUEST_URL);
			if (StringUtils.isNotBlank(address)) {
				urlBuilder.append("&address=").append(
						URLEncoder.encode(address, "UTF-8"));
			}

			final GetMethod getMethod = new GetMethod(urlBuilder.toString());
			try {
				httpClient.executeMethod(getMethod);
				Reader reader = new InputStreamReader(
						getMethod.getResponseBodyAsStream(),
						getMethod.getResponseCharSet());

				int data = reader.read();
				char[] buffer = new char[1024];
				Writer writer = new StringWriter();
				while ((data = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, data);
				}

				String result = writer.toString();
				// System.out.println(result.toString());

				DocumentBuilderFactory dbf = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				InputSource is = new InputSource();
				is.setCharacterStream(new StringReader("<"
						+ writer.toString().trim()));
				Document doc = db.parse(is);

				String strLatitude = getXpathValue(doc,
						"//GeocodeResponse/result/geometry/location/lat/text()");
				// System.out.println("Latitude:" + strLatitude);

				String strLongtitude = getXpathValue(doc,
						"//GeocodeResponse/result/geometry/location/lng/text()");
				// System.out.println("Longitude:" + strLongtitude);
				System.out.println("Pincode:" + getXpathValue(doc,"//GeocodeResponse/result/address_component[type/text()='postal_code']/long_name/text()"));
				res = strLatitude + "|" + strLongtitude  ;
                res=getXpathValue(doc,"//GeocodeResponse/result/address_component[type/text()='postal_code']/long_name/text()");

			} finally {
				getMethod.releaseConnection();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	private String getXpathValue(Document doc, String strXpath)
			throws XPathExpressionException {
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = xPath.compile(strXpath);
		String resultData = null;
		Object result4 = expr.evaluate(doc, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result4;
		for (int i = 0; i < nodes.getLength(); i++) {
			resultData = nodes.item(i).getNodeValue();
		}
		return resultData;
	}

}
