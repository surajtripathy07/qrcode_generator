import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QRCode
{
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

		int WIDTH = Integer.parseInt(propertyMap.get("width"));
		int HEIGHT = Integer.parseInt(propertyMap.get("height"));

		int fileNameColumnNumber = Integer.parseInt(propertyMap.get("fileNameColumn"));

		String finalImageFormat = propertyMap.get("finalImageFormat");

		String logoPath = propertyMap.get("logoPath");
		String outputDir = propertyMap.get("outputDir");
		String inputDir = propertyMap.get("inputDir");
		String csvFileName = propertyMap.get("csvFileName");
		System.out.println("Going to process file input["+inputDir +"], output["+ outputDir+"], processfile["+ csvFileName+"],columnNumber["+ fileNameColumnNumber+"],logoPath["+ logoPath+"],WIDTH["+ WIDTH+"],HEIGHT"+ HEIGHT+"],"+ "finalFormat["+finalImageFormat+"]");
		processFile(inputDir, outputDir, csvFileName, fileNameColumnNumber, logoPath, WIDTH, HEIGHT, finalImageFormat);
	}

	public static void processFile(String inputDir, String outputDir, String csvFileName, int fileNameColumnNumber, String logoPath, int width, int height, String fileImageFormat)
	{
		String line = "";
		String cvsSplitBy = ",";
		StringBuffer dataBuff = new StringBuffer("");

		try (BufferedReader br = new BufferedReader(new FileReader(inputDir + "/" + csvFileName)))
		{

			while ((line = br.readLine()) != null)
			{
				String saveDir = outputDir + "/";
				// use comma as separator
				String[] entry = line.split(cvsSplitBy);
				// clearing previous buffer

				dataBuff.setLength(0);
				System.out.println(entry.length);
				for (int i = 0; i < entry.length; i++)
				{
					if (entry[i] == null || entry[i].trim().isEmpty())
					{
						continue;
					}

					if (i == fileNameColumnNumber-1)
					{
						saveDir = saveDir + entry[i] +"." + fileImageFormat;
					}
					else
					{
						dataBuff.append(i).append(getLength(entry[i])).append(entry[i]);
					}
				}
				System.out.println("Going to generate QR for data " + dataBuff.toString());
				generateQR(dataBuff.toString(), logoPath, saveDir, fileImageFormat, width, height);

			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static String getLength(String param)
	{
		Integer len = param.length();

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

		// Write QR code to new image at position 0/0
		g.drawImage(qrImage, 0, 0, null);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

		// Write logo into combine image at position (deltaWidth / 2) and
		// (deltaHeight / 2). Background: Left/Right and Top/Bottom must be
		// the same space for the logo to be centered
		g.drawImage(logoImage, (int) Math.round(deltaWidth / 2), (int) Math.round(deltaHeight / 2), null);

		// Write combined image as PNG to OutputStream
		File imageFile = new File(generationPath);

		try
		{
			ImageIO.write(combined, finalImageFormat, imageFile);
			System.out.println("File created for data at "+generationPath);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
