import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.visa.mvisa.generator.InputInvalidException;
import com.visa.mvisa.generator.MerchantQrDataRequest;
import com.visa.mvisa.generator.QrCodeDataGenerator;

public class QRCode
{

	public static Integer WIDTH;
	public static Integer HEIGHT;
	public static Integer fileNameColumnNumber;
	public static String finalImageFormat;
	public static String logoPath;
	public static String framePath;
	public static String fileFieldSeperator;
	public static String outputDir;
	public static String inputDir;
	public static String csvFileName;

	public static void main(String[] args)
	{
		String configFile = args[0];
		System.out.println(configFile);

		Properties properties = new Properties();

		try
		{
			InputStream inputStream = new QRCode().getClass().getClassLoader().getResourceAsStream(configFile);
			if (inputStream != null)
			{
				properties.load(inputStream);
			}
			else
			{
				throw new FileNotFoundException("property file '" + configFile + "' not found in the classpath");
			}

		}
		catch (Exception e)
		{
			System.out.println(e);
		}

		Map<String, String> propertyMap = new HashMap<String, String>();
		for (final Entry<Object, Object> entry : properties.entrySet())
		{
			propertyMap.put((String) entry.getKey(), (String) entry.getValue());
		}

		WIDTH = Integer.parseInt(propertyMap.get("width"));
		HEIGHT = Integer.parseInt(propertyMap.get("height"));

		//fileNameColumnNumber = Integer.parseInt(propertyMap.get("fileNameColumn"));

		finalImageFormat = propertyMap.get("finalImageFormat");

		//logoPath = propertyMap.get("logoPath");
		framePath = propertyMap.get("framePath");
		outputDir = propertyMap.get("outputDir");
		inputDir = propertyMap.get("inputDir");
		csvFileName = propertyMap.get("csvFileName");
		fileFieldSeperator = propertyMap.get("fileFieldSeperator");

		System.out.println("Going to process file input[" + inputDir + "], output[" + outputDir + "], processfile[" + csvFileName + "],logoPath[" + logoPath + "],WIDTH[" + WIDTH + "],HEIGHT" + HEIGHT
				+ "]," + "finalFormat[" + finalImageFormat + "]");

		processFile(inputDir, outputDir, csvFileName, framePath, WIDTH, HEIGHT, finalImageFormat);
	}

	public static void processFile(String inputDir, String outputDir, String csvFileName, String framePath, int width, int height, String fileImageFormat)
	{
		String line = "";

		String processFile = inputDir + "/" + csvFileName;
		String newFileWithData = inputDir + "/processed/" + csvFileName;

		try (BufferedReader br = new BufferedReader(new FileReader(processFile)))
		{

			PrintWriter writer = new PrintWriter(newFileWithData);

			while ((line = br.readLine()) != null)
			{
				System.out.println(fileFieldSeperator);
				String saveDir = outputDir + "/";

				String[] entry = line.split(fileFieldSeperator);
				String data = getBharatQr(entry);
				System.out.println("Going to generate QR for data " + data);
				saveDir += entry[11] + "." + fileImageFormat;
				// writing in new file along with data
				writer.println(line + fileFieldSeperator + data);

				//generateBharatQR(data, framePath, saveDir, fileImageFormat, entry[7], entry[2]);
				generateOnlyBharatQR(data, saveDir, fileImageFormat);

			}
			writer.close();

			/*
			File oldFile = new File(processFile);
			File newFile = new File(newFileWithData);
			newFile.renameTo(oldFile);*/

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static String getMVisaData(String[] entry, int fileNameColumnNumber, String saveDir, int i, String fileImageFormat)
	{
		StringBuffer dataBuff = new StringBuffer();

		// last column is the file name of image so no need to add it as data
		if (i == fileNameColumnNumber - 1)
		{
			saveDir = saveDir + entry[i] + "." + fileImageFormat;
		}
		else
		{
			// append the column number, the length of fieldData, the fieldData
			dataBuff.append(getLength(i)).append(getLength(entry[i].length())).append(entry[i]);
		}

		return dataBuff.toString();
	}

	public static String getBharatQr(String[] entry)
	{
		//entry = "1,12,4006252000238801,5812,356,IN,PH-Basant Lok,Delhi,TRUE,P1000,***,Basant Lok P1000".split(",");
		MerchantQrDataRequest mqD = new MerchantQrDataRequest();

		mqD.setPayloadFormatIndicator(entry[0]);

		mqD.setPointOfInitiation(entry[1]);

		mqD.setmVisaMerchantId(entry[2]);

		mqD.setMerchantCategoryCode(entry[3]);

		mqD.setCurrencyCode(entry[4]);

		mqD.setCountryCode(entry[5]);

		mqD.setMerchantName(entry[6]);

		mqD.setCityName(entry[7]);

		if ("TRUE".equalsIgnoreCase(entry[8]))
		{
			mqD.setTag62Present(true);
		}
		mqD.setBillId(entry[9]);

		mqD.setMobileNumber(entry[10]);

		String data = null;
		try
		{
			data = QrCodeDataGenerator.generateQrCodeData(mqD);
		}
		catch (InputInvalidException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}

	/**
	 * Returns length as len if len is sigle digit else it returns A, B, C
	 * depending on the double digit len value
	 */
	public static String getLength(Integer len)
	{
		if (len < 10)
		{
			return len.toString();
		}
		else
		{
			char init = 'A';
			char character = (char) (init + (len - 10));
			return String.valueOf(character);

		}
	}

	/*
		public static void generateQR(String data, String logoPath, String generationPath, String finalImageFormat, int WIDTH, int HEIGHT)
		{
			Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
			hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
	
			QRCodeWriter qrWriter = new QRCodeWriter();
	
			BitMatrix matrix = null;
			try
			{
				matrix = qrWriter.encode(data, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);
			}
			catch (WriterException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	
			BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(matrix);
	
			BufferedImage logoImage = null;
			try
			{
				logoImage = ImageIO.read(new File(logoPath));
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
			// Calculate the delta height and width between QR code and logo
			int deltaHeight = qrImage.getHeight() - logoImage.getHeight();
			int deltaWidth = qrImage.getWidth() - logoImage.getWidth();
	
			// Initialize combined image
			BufferedImage combined = new BufferedImage(qrImage.getHeight(), qrImage.getWidth(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D) combined.getGraphics();
	
			//Graphics2D g = (Graphics2D) qrImage.getGraphics();
			// Write QR code to new image at position 0/0
			g.drawImage(qrImage, 0, 0, null);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
	
			// Write logo into combine image at position (deltaWidth / 2) and
			// (deltaHeight / 2). Background: Left/Right and Top/Bottom must be
			// the same space for the logo to be centered
			g.drawImage(logoImage, (int) Math.round(deltaWidth / 4), (int) Math.round(deltaHeight / 4), null);
	
			// Write combined image as PNG to OutputStream
			File imageFile = new File(generationPath);
	
			try
			{
				ImageIO.write(combined, finalImageFormat, imageFile);
				//ImageIO.write(qrImage, finalImageFormat, imageFile);
				System.out.println("File created for data at " + generationPath);
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	*/
	public static void generateBharatQR(String data, String framePath, String generationPath, String finalImageFormat, String location, String merchantId)
	{
		BufferedImage frameImage = null;
		try
		{
			frameImage = ImageIO.read(new File(framePath));
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Calculate Height and width of qr to be generated based on the size of frame
		int WIDT = (Math.round(frameImage.getWidth() / 2) - Math.round(frameImage.getWidth() / 7));
		System.out.println(WIDT + " " + frameImage.getWidth());

		int HEIGH = (Math.round(frameImage.getHeight() * 3 / 4) - Math.round(frameImage.getHeight() * 2 / 5));
		System.out.println(HEIGH + " " + frameImage.getHeight());
		System.out.println((int) Math.round(frameImage.getWidth() / 7) + " " + (int) Math.round(frameImage.getHeight() * 2 / 5));
		Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

		QRCodeWriter qrWriter = new QRCodeWriter();

		BitMatrix matrix = null;
		try
		{
			matrix = qrWriter.encode(data, BarcodeFormat.QR_CODE, WIDT, HEIGH, hints);
		}
		catch (WriterException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Write qr to bufferimage
		BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(matrix);

		// Initialize combined image with the frame size
		BufferedImage combined = new BufferedImage(frameImage.getHeight(), frameImage.getWidth(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) combined.getGraphics();

		//Graphics2D g = (Graphics2D) qrImage.getGraphics();
		// Write frame to the position 0,0
		g.drawImage(frameImage, 0, 0, null);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

		// Write qr starting at width 1/7th of the frame width, and height 2/5th of the frames height
		g.drawImage(qrImage, (int) Math.round(frameImage.getWidth() / 7), (int) Math.round(frameImage.getHeight() * 2 / 5), null);

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

		Font font = new Font("Gotham-Thin", Font.BOLD, 16);
		g.setFont(font);
		g.setColor(Color.BLACK);
		int mIdStartX = Math.round(frameImage.getWidth() / 7) + 30;
		int mIdStartY = (Math.round(frameImage.getHeight() * 3 / 4) + 10);
		g.drawString(merchantId, mIdStartX, mIdStartY);

		font = new Font("Gotham-Bold", Font.PLAIN, 16);
		g.setFont(font);
		g.setColor(Color.BLACK);
		g.drawString(location, mIdStartX + (WIDT / 2) - ((location.length() * 17) / 2), mIdStartY + 25);

		g.setFont(font);

		// Write combined image as PNG to OutputStream
		File imageFile = new File(generationPath);
		try
		{
			ImageIO.write(combined, finalImageFormat, imageFile);
			//ImageIO.write(qrImage, finalImageFormat, imageFile);
			System.out.println("File created for data at " + generationPath);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void generateOnlyBharatQR(String data, String generationPath, String finalImageFormat)
	{
		// Calculate Height and width of qr to be generated based on the size of frame
		Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

		QRCodeWriter qrWriter = new QRCodeWriter();

		BitMatrix matrix = null;
		try
		{
			matrix = qrWriter.encode(data, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);
		}
		catch (WriterException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Write qr to bufferimage
		BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(matrix);

		// Initialize combined image with the frame size
		Graphics2D g = (Graphics2D) qrImage.getGraphics();

		//Graphics2D g = (Graphics2D) qrImage.getGraphics();
		// Write frame to the position 0,0
		g.drawImage(qrImage, 0, 0, null);
		// Write combined image as PNG to OutputStream
		File imageFile = new File(generationPath);

		try
		{
			ImageIO.write(qrImage, finalImageFormat, imageFile);
			//ImageIO.write(qrImage, finalImageFormat, imageFile);
			System.out.println("File created for data at " + generationPath);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
