package lat_lng;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URLEncoder;

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
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class LongLatService1 {

	private static final String GEOCODE_REQUEST_URL = "http://maps.googleapis.com/maps/api/geocode/xml?sensor=false&";
	private static HttpClient httpClient = new HttpClient(
			new MultiThreadedHttpConnectionManager());

	public static void process(String input, String output, int indexAdd1,
			int indexAdd2) throws Exception {
		// example code

		BufferedReader br = new BufferedReader(new FileReader(new File(input)));
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(output)));
		String line = null;
		// DoubleMetaphone doubleMetaphone = new DoubleMetaphone();
		int cnt = 0;
		while ((line = br.readLine()) != null) {
			String result = "";
			cnt++;
			if (cnt % 500 == 0) {
				System.out.println("Count :" + cnt);
			}
			if (StringUtils.isNotBlank(line)) {
				try {
					String arr[] = StringUtils
							.splitPreserveAllTokens(line, "|");
					String res = "";
					for (int i = indexAdd1; i <= indexAdd2; i++) {
						res = res + "," + arr[i];
					}
					LongLatService1 tDirectionService = new LongLatService1();
					result = tDirectionService.getLongitudeLatitude(res);
					for (int i = 0; i < arr.length; i++) {
						bw.write(arr[i] + "|");
					}

//					System.out.println(line);
				} catch (Exception e) {
					e.printStackTrace();
					result = "null";
					System.out.println(line);
				}
				bw.write(result + "" /*
									 * + "|" + key2.toUpperCase().trim()
									 */);
				bw.newLine();
			}
		}

		bw.flush();
		br.close();
		bw.close();

	}

	public static void main(String[] args) {
		try {
			process("/Users/RajnishKumar/Documents/Misc/CHM/Input/input_try.txt",
					"/Users/RajnishKumar/Documents/Misc/CHM/Output/Output_1c.txt",
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
				//System.out.println("Latitude:" + strLatitude);

				String strLongtitude = getXpathValue(doc,
						"//GeocodeResponse/result/geometry/location/lng/text()");
				//System.out.println("Longitude:" + strLongtitude);
				//res = strLatitude + "|" + strLongtitude + "|" + getXpathValue(doc,"//GeocodeResponse/result/address_component[type/text()='postal_code']/long_name/text()");
				res = getXpathValue(doc,"//GeocodeResponse/result/address_component[type/text()='postal_code']/long_name/text()");

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