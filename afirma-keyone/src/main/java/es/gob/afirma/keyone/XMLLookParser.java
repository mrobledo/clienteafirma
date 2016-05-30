package es.gob.afirma.keyone;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import es.gob.afirma.core.misc.Base64;
import es.gob.afirma.standalone.ui.pdf.ColorResource;

public class XMLLookParser {

	static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$
	private final String xml;
	private final Properties prop;
	public Properties getProperties() {
		return this.prop;
	}

	public XMLLookParser(final String xml) {
		this.xml = xml;
		this.prop = new Properties();
	}

	public Properties parse() {
		if (this.xml == null) {
			return null;
		}

		try(final InputStream in = new ByteArrayInputStream(this.xml.getBytes(StandardCharsets.UTF_8))) {
			final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			final Document doc = docBuilder.parse(in);
			final Node rect = doc.getElementsByTagName("Rect").item(0); //$NON-NLS-1$
			if (rect != null) {
				setRectProp(rect);
			}
			final Node background = doc.getElementsByTagName("Backgorund").item(0); //$NON-NLS-1$
			if (background != null) {
				setBackgroundImage(background);
			}
			final Node foreground = doc.getElementsByTagName("Foreground").item(0); //$NON-NLS-1$
			if (foreground != null) {
				setForeground(foreground);
			}


			return this.prop;
		}
		catch (final Exception e) {
			LOGGER.severe("Error analizando el xml: " + e); //$NON-NLS-1$
			return null;
		}
	}

	private void setRectProp(final Node rect) {
		final NamedNodeMap rectAttrib = rect.getAttributes();
		if (rectAttrib != null && rectAttrib.getLength() == 4) {
			this.prop.setProperty("imagePositionOnPageLowerLeftX" , rectAttrib.getNamedItem("x0").getTextContent()); //$NON-NLS-1$ //$NON-NLS-2$
			this.prop.setProperty("imagePositionOnPageLowerLeftY" , rectAttrib.getNamedItem("y0").getTextContent()); //$NON-NLS-1$ //$NON-NLS-2$
			this.prop.setProperty("imagePositionOnPageUpperRightX" , rectAttrib.getNamedItem("x1").getTextContent()); //$NON-NLS-1$ //$NON-NLS-2$
			this.prop.setProperty("imagePositionOnPageUpperRightY" , rectAttrib.getNamedItem("y1").getTextContent()); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private void setBackgroundImage(final Node backgorund) {
		final Node backImage = backgorund.getChildNodes().item(0);
		if (backImage != null && backImage.getNodeName().equals("image")) { //$NON-NLS-1$
			final NodeList list = backgorund.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				final Node node = list.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					final Element element = (Element) node;
		        	if (element.getNodeName().equals("data")) { //$NON-NLS-1$
		        		try {
							this.prop.setProperty("image", getImageBase64(ImageIO.read(new File(element.getTextContent())))); //$NON-NLS-1$
						} catch (final Exception e) {
							LOGGER.severe("Error extrayendo propiedades del fondo: " + e); //$NON-NLS-1$
						}
		        	}
				}
			}
		}
	}

	private void setForeground(final Node foreground) {
		final Node foreImage = foreground.getChildNodes().item(0);
		if (foreImage != null && foreImage.getNodeName().equals("image")) { //$NON-NLS-1$
			final NodeList list = foreground.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				final Node node = list.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					final Element element = (Element) node;
		        	if (element.getNodeName().equals("data")) { //$NON-NLS-1$
		        		try {
							this.prop.setProperty(
								"signatureRubricImage", //$NON-NLS-1$
								getImageBase64(ImageIO.read(new File(element.getTextContent())))
							);
						} catch (final Exception e) {
							e.printStackTrace();
						}
		        	}
		        	else if (element.getNodeName().equals("text")) { //$NON-NLS-1$
		        		setTextProperties(element);
		        	}
				}
			}
		}

	}

	private void setTextProperties(final Node text) {
		final NodeList list = text.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			final Node node = list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				final Element element = (Element) node;
	        	if (element.getNodeName().equals("properties")) { //$NON-NLS-1$
	        		final NamedNodeMap textPropAttrib = element.getAttributes();
	        		if (textPropAttrib != null && textPropAttrib.getLength() == 2) {
	        			setTextColorProperties(textPropAttrib);
	        		}
	        	}
	        	else if (element.getNodeName().equals("position")) { //$NON-NLS-1$

	        	}
			}
		}
	}

	private void setTextColorProperties(final NamedNodeMap textPropAttrib) {
		final int[] rgb = new int[3];
		final String rgbString[] = textPropAttrib.getNamedItem("color").getTextContent().split(" "); //$NON-NLS-1$ //$NON-NLS-2$
		for (int j = 0 ; j < 3 ; j++) {
			rgb[j] = Integer.parseInt(rgbString[j]);
		}
		final Color col = new Color(rgb[0], rgb[1], rgb[2]);
		for (final ColorResource color : ColorResource.getAllColorResources()) {
			if (color.getColor().equals(col)) {
				this.prop.setProperty("layer2FontColor", color.getPdfColorKey()); //$NON-NLS-1$
			}
		}
		this.prop.setProperty("layer2FontSize" , textPropAttrib.getNamedItem("fontSize").getTextContent()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static String getImageBase64(final BufferedImage image) {
		try (final ByteArrayOutputStream osImage = new ByteArrayOutputStream()) {
			ImageIO.write(image, "jpg", osImage); //$NON-NLS-1$
			return Base64.encode(osImage.toByteArray());
		}
        catch (final Exception e) {
        	LOGGER.severe("No ha sido posible pasar la imagen a JPG: " + e); //$NON-NLS-1$
		}
		return null;
	}
}
