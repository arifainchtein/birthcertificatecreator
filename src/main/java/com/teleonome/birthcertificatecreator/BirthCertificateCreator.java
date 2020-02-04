package com.teleonome.birthcertificatecreator;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONException;
import org.json.JSONObject;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.DocumentException;
import com.teleonome.framework.tools.SendOneCommandToArduino;
import com.teleonome.framework.utils.Utils;
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
	private static final String QR_CODE_TOTP_KEY_PATH= "TOTP.png";

	public BirthCertificateCreator() {
		String fileName =  Utils.getLocalDirectory() + "lib/Log4J.properties";
		PropertyConfigurator.configure(fileName);
		logger = Logger.getLogger(getClass());
	}
	public void create(String teleonomeName, String totpSharedSecret, String  base32Secret, String  piUserPasswrd, String wifipwd, String postgresqlPassword, int postgresqlPort) {
		
		//
		//read the file
		//
		
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

//				String url = "https://httpbin.org/post";
//				String urlParameters = "name="+name+"&CurrentOrganismUniqueIdentifier="+currentOrganismUniqueIdentifier+"&dataSharing="+dataSharing+"&structureSharing=" + structureSharing;
//				byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
//
//				URL myurl = new URL(url);
//				con = (HttpURLConnection) myurl.openConnection();
//
//				con.setDoOutput(true);
//				con.setRequestMethod("POST");
//				con.setRequestProperty("User-Agent", "Java client");
//				con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//
//				try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
//					wr.write(postData);
//				}
//
//				InputStream stream = con.getInputStream();
//				byte[] data = new byte[200000];
//				int count = stream.read(data);
//
//				FileUtils.writeByteArrayToFile(new File("DigitalGeppettoSecret.png"), data);
//				 
				
				String creator="DigitalGeppetto";
				String email=teleonomeName + "@digitalgeppetto.com";
				//                      "QR-Code:otpauth://totp/Example.com:alice@example.com?algorithm=SHA1&digits=6&issuer=Example.com&period=30&secret=K3XT7VEUS7JFJVCX
				
				//https://www.google.com/chart?chs=200x200&chld=M|0&cht=qr&chl=otpauth://totp/Example%3Aalice%40google.com%3Fsecret%3DJBSWY3DPEHPK3PXP%26issuer%3DExample
					
				//String base32QRText = "otpauth://totp/"+teleonomeName+":"+email +"?digits=6&period=30&secret="+ totpSharedSecret+"&issuer=" + teleonomeName;
				String base32QRText = "otpauth://totp/"+teleonomeName+":"+email +"?digits=6&period=30&secret="+ base32Secret+"&issuer=" + teleonomeName;
				System.out.println(base32QRText);	
				generateQRCodeImage(base32QRText, 150, 150, QR_CODE_TOTP_KEY_PATH);
//				
//				generateQRCodeImage(FileUtils.readFileToString(new File(birthCertificateJSONObject.getString("SSHPrivateKey"))), 350, 350, QR_CODE_SSH_PRIVATE_KEY_PATH);
//				generateQRCodeImage(FileUtils.readFileToString(new File(birthCertificateJSONObject.getString("SSHPublicKey"))), 350, 350, QR_CODE_SSH_PUBLIC_KEY_PATH);
//
//				generateQRCodeImage(FileUtils.readFileToString(new File(birthCertificateJSONObject.getString("SSHDigitalGeppettoPrivateKey"))), 350, 350, QR_CODE_SSH_DIGITAL_GEPPETTO_PRIVATE_KEY_PATH);
//				generateQRCodeImage(FileUtils.readFileToString(new File(birthCertificateJSONObject.getString("SSHDigitalGeppettoPublicKey"))), 350, 350, QR_CODE_SSH_DIGITAL_GEPPETTO_PUBLIC_KEY_PATH);

			} catch (IOException e) {
				logger.warn(Utils.getStringException(e));;
			} catch (WriterException e) {
				// TODO Auto-generated catch block
				logger.warn(Utils.getStringException(e));
			}
			
			String htmlText = generateHTMLFile( teleonomeName, totpSharedSecret,base32Secret, piUserPasswrd,wifipwd, postgresqlPassword,  postgresqlPort );
			File htmlFile = new File("BC.html");
			File birtCertificateFile = new File("BirthCertificate.pdf");
			FileUtils.writeStringToFile(htmlFile, htmlText.toString());

//			PDDocument pdf = PDDocument.load(htmlFile);
//			Writer output = new PrintWriter("BirthCertificate.pdf", "utf-8");
//			new PDFDomTree().writeText(pdf, output);
//			output.close();

			 try {
			        String url = htmlFile.toURI().toURL().toString();
			        System.out.println("URL: " + url);

			        OutputStream out = new FileOutputStream("BirthCertificate.pdf");

			        //Flying Saucer part
			        ITextRenderer renderer = new ITextRenderer();

			        renderer.setDocument(url);
			        renderer.layout();
			        renderer.createPDF(out);

			        out.close();
			    } catch (DocumentException | IOException e) {
			        // TODO Auto-generated catch block
			        e.printStackTrace();
			    }

		      
		      
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			logger.warn(Utils.getStringException(e));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(Utils.getStringException(e));
			logger.warn(Utils.getStringException(e));
		}

	}
	private String generateHTMLFile(String teleonomeName, String secret,String base32secret, String piUserPasswrd, String wifipwd,String postgresqlPassword, int postgresqlPort ) {
		StringBuffer htmlText = new StringBuffer("<html><head><style></style></head><body><center><h6>Birth Certificate</h6><h3>"+teleonomeName+"</h3>");
		//htmlText.append(teleonomeName);
		//htmlText.append(creator); 
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat  sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		sdf.setTimeZone(TimeZone.getTimeZone("CET"));
		String now = sdf.format(date);
		htmlText.append("Created on " + now ); 
		htmlText.append("<br />");
		
		htmlText.append("<table cellpadding=\"2\"><tr>");
			htmlText.append("<td><img src=\""+ QR_CODE_TOTP_KEY_PATH+"\" /><br></br>TOTP Secret QR Code</td><td></td><td></td>");
		htmlText.append("<td>");
		
		htmlText.append("<table border=\"2\" cellpadding=\"2\">");
		htmlText.append("<tr><td>Pi User Password</td><td align=\"center\"><b>");
		htmlText.append(piUserPasswrd);
		htmlText.append("</b></td></tr>");
		htmlText.append("<tr><td >Wifi Password</td><td align=\"center\"><b>");
		htmlText.append(wifipwd);
		htmlText.append("</b></td></tr>");
		htmlText.append("<tr><td >TOTP Shared Secret</td><td align=\"center\"><b>");
		htmlText.append(secret);
		htmlText.append("</b></td></tr>");
		
		htmlText.append("<tr><td >TOTP Shared Secret Base32</td><td align=\"center\"><b>");
		htmlText.append(base32secret);
		htmlText.append("</b></td></tr>");
//		
		htmlText.append("<tr><td >Postgresql Password</td><td align=\"center\"><b>");
		htmlText.append(postgresqlPassword);
		htmlText.append("</b></td></tr>");
//		
		htmlText.append("<tr><td >Postgresql Port</td><td align=\"center\"><b>");
		htmlText.append(postgresqlPort);
		htmlText.append("</b></td></tr>");
		htmlText.append("</table>");
		htmlText.append("</td>");;
		
		htmlText.append("</tr></table></center></body></html>");
		

		System.out.println(htmlText);
		return htmlText.toString();
	}

	private String generateHTMLFileA4(String teleonomeName, String secret,String base32secret, String piUserPasswrd, String wifipwd,String postgresqlPassword, int postgresqlPort ) {
		StringBuffer htmlText = new StringBuffer("<html><head><style></style></head><body><center><h3>Birth Certificate</h3><br></br><h1>"+teleonomeName+"</h1>");
		//htmlText.append(teleonomeName);
		htmlText.append("<br></br><br></br>Created by ");
		//htmlText.append(creator); 
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat  sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		sdf.setTimeZone(TimeZone.getTimeZone("CET"));
		String now = sdf.format(date);
		htmlText.append("on " + now ); 
		htmlText.append("<br></br> <br></br> <br></br>");
		htmlText.append("<table cellpadding=\"20\">");
		htmlText.append("<tr><td width=\"20%\">Pi User Password</td><td align=\"center\" width=\"80%\"><b>");
		htmlText.append(piUserPasswrd);
		htmlText.append("</b></td><td></td></tr>");
		htmlText.append("<tr><td width=\"20%\">Wifi Password</td><td align=\"center\" width=\"80%\"><b>");
		htmlText.append(wifipwd);
		htmlText.append("</b></td><td></td></tr>");
		htmlText.append("<tr><td width=\"20%\">TOTP Shared Secret</td><td align=\"center\" width=\"80%\"><b>");
		htmlText.append(secret);
		htmlText.append("</b></td><td></td></tr>");
		
		htmlText.append("<tr><td width=\"20%\">TOTP Shared Secret Base32</td><td align=\"center\" width=\"70%\"><b>");
		htmlText.append(base32secret);
		htmlText.append("</b></td><td width=\"10%\"></td></tr>");
//		
		htmlText.append("<tr><td width=\"20%\">Postgresql Password</td><td align=\"center\" width=\"70%\"><b>");
		htmlText.append(postgresqlPassword);
		htmlText.append("</b></td><td width=\"10%\"></td></tr>");
//		
		htmlText.append("<tr><td width=\"20%\">Postgresql Port</td><td align=\"center\" width=\"70%\"><b>");
		htmlText.append(postgresqlPort);
		htmlText.append("</b></td><td width=\"10%\"></td></tr>");
		
		htmlText.append("<tr>");
		htmlText.append("<td width=\"98%\"><img src=\""+ QR_CODE_TOTP_KEY_PATH+"\" /><br></br>TOTP Secret QR Code</td><td></td><td></td></tr>");
		htmlText.append("</table></center></body></html>");
		System.out.println(htmlText);
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
	// java -jar BirthCertificateCreator.jar Cleotilde  M2RjZGZiZWIxYT  JUZFE2S2I5NGSWSXJF4FSVAK= ZDA1NDE1ZG ZDA1NDE1 ZTYwMTFmOT 26907
	//sudo java -jar BirthCertificateCreator.jar $teleonomeName $secret   $base32secret  $pwd   $wifipwd $newPostgresqlUserPassword $postgresqlPort

	public static void main( String[] args )
	{

		// $secret   $base32secret  $pwd   $wifipwd 
		//BirthCertificateCreator b2 = new BirthCertificateCreator();
	//	b2.create("Cleotilde", "M2RjZGZiZWIxYT",  "M2RjZGZiZWIxYT=",  "YmYyYWE4OW"   ,"YjgwMmQ3", "ZTYwMTFmOT", 29923);
//		
		if(args.length==7) {
			String teleonomeName=args[0];
			String secret=args[1];
			String base32secret=args[2];
			String pwd=args[3];
			String wifipwd=args[4];
			String postgresqlPassword = args[5];
			int postgresqlPort = Integer.parseInt(args[6]);
			
			BirthCertificateCreator b = new BirthCertificateCreator();
			b.create(teleonomeName, secret,  base32secret,  pwd  ,wifipwd, postgresqlPassword, postgresqlPort);
		}else {
			for(int i=0;i<args.length;i++) {
				System.out.println("i=" +i + " "  + args[i]);
				
				
			}
			System.out.println("Usage sudo sh CreateBirthCertificate.sh teleonomeName secret  base32secret  pwd   wifipwd postgresqlPassword, postgresqlPort");
			System.exit(0);
		}


	}
}
