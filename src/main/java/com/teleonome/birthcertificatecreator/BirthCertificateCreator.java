package com.teleonome.birthcertificatecreator;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.fit.pdfdom.PDFDomTree;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.teleonome.framework.tools.SendOneCommandToArduino;
import com.teleonome.framework.utils.Utils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
/**
 * Hello world!
 *
 */
public class BirthCertificateCreator 
{
	private static String buildNumber="";
	Logger logger;
	private static final String QR_CODE_SSH_PRIVATE_KEY_PATH = "./sshPrivateKey.png";
	private static final String QR_CODE_SSH_PUBLIC_KEY_PATH = "./sshPulicKey.png";
	private static final String QR_CODE_SSH_DIGITAL_GEPPETTO_PRIVATE_KEY_PATH = "./digitalGeppettoPrivateKey.png";
	private static final String QR_CODE_SSH_DIGITAL_GEPPETTO_PUBLIC_KEY_PATH = "./digitalGeppettoPulicKey.png";


	public BirthCertificateCreator() {
	}
	public void create(String teleonomeName, String secret, String  base32secret, String  pwd, String   wifipwd, String postgresqlPassword, int postgresqlPort) {
		logger = Logger.getLogger(getClass());
		//
		//read the file
		try {
			
			try {
				//
				// Get byte array that represents the qrcode for the secret to publish to digital geppetto
				//

				String name = "";
				String currentOrganismUniqueIdentifier = "";
				String dataSharing ="";
				String structureSharing = "";
				HttpURLConnection con;

				String url = "https://httpbin.org/post";
				String urlParameters = "name="+name+"&CurrentOrganismUniqueIdentifier="+currentOrganismUniqueIdentifier+"&dataSharing="+dataSharing+"&structureSharing=" + structureSharing;
				byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

				URL myurl = new URL(url);
				con = (HttpURLConnection) myurl.openConnection();

				con.setDoOutput(true);
				con.setRequestMethod("POST");
				con.setRequestProperty("User-Agent", "Java client");
				con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

				try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
					wr.write(postData);
				}

				InputStream stream = con.getInputStream();
				byte[] data = new byte[200000];
				int count = stream.read(data);

				FileUtils.writeByteArrayToFile(new File("DigitalGeppettoSecret.png"), data);

				//generateQRCodeImage(FileUtils.readFileToString(new File(birthCertificateJSONObject.getString("SSHPrivateKey"))), 350, 350, QR_CODE_SSH_PRIVATE_KEY_PATH);
				//generateQRCodeImage(FileUtils.readFileToString(new File(birthCertificateJSONObject.getString("SSHPublicKey"))), 350, 350, QR_CODE_SSH_PUBLIC_KEY_PATH);

				//generateQRCodeImage(FileUtils.readFileToString(new File(birthCertificateJSONObject.getString("SSHDigitalGeppettoPrivateKey"))), 350, 350, QR_CODE_SSH_DIGITAL_GEPPETTO_PRIVATE_KEY_PATH);
				//generateQRCodeImage(FileUtils.readFileToString(new File(birthCertificateJSONObject.getString("SSHDigitalGeppettoPublicKey"))), 350, 350, QR_CODE_SSH_DIGITAL_GEPPETTO_PUBLIC_KEY_PATH);

			} catch (IOException e) {
				logger.warn(Utils.getStringException(e));;
			}

//			String teleonomeName = birthCertificateJSONObject.getString("TeleonomeName");
//			String creator = birthCertificateJSONObject.getString("Creator");
//			String piUserPasswrd = birthCertificateJSONObject.getString("Pi User Password");
//			String totpSharedSecret = birthCertificateJSONObject.getString("TOTP Shared Secret");
			
			
			
			String htmlText = generateHTMLFile( teleonomeName,  secret,   base32secret,   pwd,    wifipwd );
			File htmlFile = new File("BC.html");
			File birtCertificateFile = new File("BirthCertificate.pdf");
			FileUtils.writeStringToFile(htmlFile, htmlText.toString());

			PDDocument pdf = PDDocument.load(htmlFile);
			Writer output = new PrintWriter("./BirthCertificate.pdf", "utf-8");
			new PDFDomTree().writeText(pdf, output);
			output.close();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			logger.warn(Utils.getStringException(e));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.warn(Utils.getStringException(e));
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			logger.warn(Utils.getStringException(e));
		}

	}


	private String generateHTMLFile(String teleonomeName, String secret,String base32secret, String piUserPasswrd, String wifipwd ) {
		StringBuffer htmlText = new StringBuffer("<html><body><center><H3>Birth Certificate</h3><br><h1>MonoNanny3</h1>");
		htmlText.append(teleonomeName);
		//htmlText.append("<br><br>Created by ");
		//htmlText.append(creator); 
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat  sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		sdf.setTimeZone(TimeZone.getTimeZone("CET"));
		String now = sdf.format(date);
		htmlText.append("on " + now ); 
		htmlText.append("<br> <br> <br> <table cellpadding=\"20\">");
		htmlText.append("<tr  ><td width=\"20%\">Pi User Password</td><td align=\"center\" width=\"80%\"><b>");
		htmlText.append(piUserPasswrd);
		htmlText.append("</b></td></tr>");
		htmlText.append("<tr  ><td width=\"20%\">Wifi Password</td><td align=\"center\" width=\"80%\"><b>");
		htmlText.append(wifipwd);
		htmlText.append("</b></td></tr>");
		htmlText.append("<tr><td width=\"20%\">TOTP Shared Secret</td><td align=\"center\" width=\"80%\"><b>");
		htmlText.append(secret);
		htmlText.append("</b></td></tr>");
		htmlText.append("<tr><td width=\"20%\">TOTP Shared Secret Base32</td><td align=\"center\" width=\"80%\"><b>");
		htmlText.append(base32secret);
		htmlText.append("</b></td></tr>");
		//htmlText.append("<tr><td>SSH Private Key</td><td><img src=\""+ QR_CODE_SSH_PRIVATE_KEY_PATH+"\" /></td></tr>");
		//htmlText.append("<tr><td>SSH Public Key</td><td><img src=\""+ QR_CODE_SSH_PUBLIC_KEY_PATH+"\" /></td></tr>");

		//htmlText.append("<tr><td>Digital Geppetto SSH Private Key</td><td><img src=\""+ QR_CODE_SSH_DIGITAL_GEPPETTO_PRIVATE_KEY_PATH+"\" /></td></tr>");
		//htmlText.append("<tr><td>Digital Geppetto SSH Public Key</td><td><img src=\""+ QR_CODE_SSH_DIGITAL_GEPPETTO_PUBLIC_KEY_PATH+"\" /></td></tr>");

		htmlText.append("<table></center></body></html>");
		return htmlText.toString();
	}

	private static void generateQRCodeImage(String text, int width, int height, String filePath)
			throws WriterException, IOException {
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

		Path path = FileSystems.getDefault().getPath(filePath);
		MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
	}

	///
	// java -jar BirthCertificateGenerator.jar Cleotilde  ZmViOWRhZGNkYz LJWVM2KPK5JGQWSHJZVVS6Q= YmYyYWE4OW YjgwMmQ3 ZTYwMTFmOT 29923
	
	public static void main( String[] args )
	{

		// $secret   $base32secret  $pwd   $wifipwd 

		if(args.length==7) {
			String teleonomeName=args[0];
			String secret=args[1];
			String base32secret=args[2];
			String pwd=args[3];
			String wifipwd=args[4];
			String postgresqlPassword = args[5];
			int postgresqlPort = Integer.parseInt(args[6]);
			
			BirthCertificateCreator b = new BirthCertificateCreator();
			b.create(teleonomeName, secret,  base32secret,  pwd   ,wifipwd, postgresqlPassword, postgresqlPort);
		}else {
			System.out.println("Usage sudo sh CreateBirthCertificate.sh teleonomeName secret  base32secret  pwd   wifipwd ");
			System.exit(0);
		}


	}
}
