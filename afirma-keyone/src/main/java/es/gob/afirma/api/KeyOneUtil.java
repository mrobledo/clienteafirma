package es.gob.afirma.api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.CertificateEncodingException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import com.aowagie.text.DocumentException;
import com.aowagie.text.Rectangle;
import com.aowagie.text.pdf.PdfAnnotation;
import com.aowagie.text.pdf.PdfFormField;
import com.aowagie.text.pdf.PdfName;
import com.aowagie.text.pdf.PdfReader;
import com.aowagie.text.pdf.PdfStamper;
import com.aowagie.text.pdf.PdfString;
import es.gob.afirma.cert.signvalidation.SignValiderFactory;
import es.gob.afirma.cert.signvalidation.SignValidity.SIGN_DETAIL_TYPE;
import es.gob.afirma.core.AOCancelledOperationException;
import es.gob.afirma.core.AOException;
import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.misc.Base64;
import es.gob.afirma.core.signers.AOSignConstants;
import es.gob.afirma.core.signers.AOSigner;
import es.gob.afirma.core.signers.AOSignerFactory;
import es.gob.afirma.core.ui.AOUIFactory;
import es.gob.afirma.keystores.AOCertificatesNotFoundException;
import es.gob.afirma.keystores.AOKeyStore;
import es.gob.afirma.keystores.AOKeyStoreDialog;
import es.gob.afirma.keystores.AOKeyStoreManager;
import es.gob.afirma.keystores.AOKeyStoreManagerException;
import es.gob.afirma.keystores.AOKeyStoreManagerFactory;
import es.gob.afirma.keystores.AOKeystoreAlternativeException;
import es.gob.afirma.keystores.AggregatedKeyStoreManager;
import es.gob.afirma.keystores.SmartCardException;
import es.gob.afirma.keystores.filters.CertificateFilter;
import es.gob.afirma.keystores.filters.PolicyIdFilter;
import es.gob.afirma.local.BatchSigner;
import es.gob.afirma.signers.pades.BadPdfPasswordException;
import es.gob.afirma.signers.pades.PdfHasUnregisteredSignaturesException;
import es.gob.afirma.signers.pades.PdfIsCertifiedException;
import es.gob.afirma.signers.pades.PdfUtil;
import es.gob.afirma.signers.pades.PdfUtil.SignatureField;
import es.gob.afirma.standalone.SimpleAfirmaMessages;
import es.gob.afirma.standalone.SimpleKeyStoreManager;
import es.gob.afirma.standalone.ui.preferences.PreferencesManager;

/** API con funcionalidades básicas sobre documentos PDF.
 * @author Sergio Mart&iacute;nez Rico. */
public final class KeyOneUtil {

	private static final String SEPARATOR = ","; //$NON-NLS-1$
	private static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	/** Obtiene los nombres de los campos de firma de un documento pdf.
	 * @param filePath Ruta del documento pdf.
	 * @return Nombres de los campos de firma de un documento pdf.
	 * @throws PdfException Error al gestionar el documento pdf.
	 */
	public static String enumSignatureFieldNames(final String filePath) throws PdfException {
		final StringBuilder sb = new StringBuilder();
        try ( final InputStream fis = new FileInputStream(new File(filePath)) ) {
        	final byte[] data = AOUtil.getDataFromInputStream(fis);
        	final List<SignatureField> fields = PdfUtil.getPdfEmptySignatureFields(data);
        	for (final SignatureField field : fields) {
        		sb.append(field.getName());
        		sb.append(SEPARATOR);
        	}
        	return sb.toString();
        }
        catch (final Exception e) {
        	LOGGER.severe("Error recuperando los nombres de campos de firma del PDF: " + e); //$NON-NLS-1$
        	throw new PdfException("Error recuperando los nombres de campos de firma del pdf: " + e, e); //$NON-NLS-1$
        }
	}

	/** Obtiene el n&uacute;mero de p&aacute;ginas del documento pdf.
	 * @param filePath Ruta del documento pdf.
	 * @return N&uacute;mero de p&aacute;ginas del documento pdf.
	 * @throws PdfException Error al gestionar el documento pdf.
	 */
	public static int getPdfPageNumber(final String filePath) throws PdfException {
        PdfReader pdfReader;
		try {
			pdfReader = new PdfReader(filePath);
		} catch (IOException e) {
			LOGGER.severe("Error anadiendo pagina en blanco al PDF: " + e); //$NON-NLS-1$
			throw new PdfException("Error obteniendo el n&uacute;mero de p&aacute;ginas del documento PDF: " + e, e); //$NON-NLS-1$
		}
        return pdfReader.getNumberOfPages();
	}
	
	/** Agrega una p&aacute;gina en blanco al final del documento pdf.
	 * @param filePath Ruta del fichero pdf.
	 * @throws PdfException Error al gestionar el fichero pdf.
	 */
	public static void addBlankPage(final String filePath) throws PdfException {
        try ( 
        		final ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        		final FileOutputStream os = new FileOutputStream(new File(filePath))
        				) {
        	final PdfReader pdfReader = new PdfReader(filePath);
        	final Calendar cal = Calendar.getInstance();
        	final PdfStamper stp = new PdfStamper(pdfReader, baos, cal);
        	stp.insertPage(pdfReader.getNumberOfPages() + 1, pdfReader.getPageSizeWithRotation(1));
        	stp.close(cal);
        	pdfReader.close();
        	os.write(baos.toByteArray());
        	os.close();
        }
        catch(final Exception e) {
        	LOGGER.severe("Error anadiendo pagina en blanco al PDF: " + e); //$NON-NLS-1$
        	throw new PdfException("Error a&ntilde;adiendo pagina en blanco al documento PDF: " + e, e); //$NON-NLS-1$
        }
	}
	
	/** Realiza la firma en lotes sobre los documentos definidos en un fichero XML.
	 * @param xmlPath Ruta del fichero XML.
	 * @param alias Alias del certificado a usar en la firma.
	 * @param password Password para obtener la clave privada de firma.
	 * @return Registro del resultado general del proceso por lote en un XML
	 * @throws Exception Error en la firma por lotes
	 */
	public static String doBatchSign(final String xmlPath, String alias, String password) throws Exception {
		// Conversion del fichero XML a bytes
		byte [] xmlBytes = null;
		File f = new File(xmlPath);
		xmlBytes = new byte[(int)f.length()]; 
		try (
				final FileInputStream fis = new FileInputStream(f); 
				){
			
			fis.read(xmlBytes);  
			if (xmlBytes.length < 1) {
				throw new IllegalArgumentException(
					"El XML de definicion de lote de firmas no puede ser nulo ni vacio" //$NON-NLS-1$
				);
			}
			
			AggregatedKeyStoreManager aksm;
		
			//Se obtiene el almacen de Windows (posibilidad de inicializar otro almacen)
			 aksm = AOKeyStoreManagerFactory.getAOKeyStoreManager(
					 AOKeyStore.WINDOWS, 
					 null, 
					 null, 
					 null, 
					 null
					);
			 
			 if(AOUIFactory.showConfirmDialog(
     				null,
     				SimpleAfirmaMessages.getString("Api.1"), //$NON-NLS-1$
     				SimpleAfirmaMessages.getString("Api.0"), //$NON-NLS-1$
     				JOptionPane.YES_NO_OPTION,
     				JOptionPane.WARNING_MESSAGE
     			) == 0) {
				 //Se obtiene la clave privada del almacen
				 PrivateKeyEntry pke = aksm.getKeyEntry(alias);
			 
				 //Se firma con la clave privada
				 return BatchSigner.sign(
						Base64.encode(xmlBytes), 
						pke.getCertificateChain(), 
						pke.getPrivateKey()
					);
			 }
		} catch (CertificateEncodingException | AOException e) {
			LOGGER.severe("Error durante la firma por lotes: " + e); //$NON-NLS-1$
			throw e;
		}
		catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException e) {
			LOGGER.severe("No se ha encontrado el alias en el almacen: " + e); //$NON-NLS-1$
			throw e;
		}
		catch (AOKeystoreAlternativeException e) {
			LOGGER.severe("No de ha podido inicializar el almacen de windows: " + e); //$NON-NLS-1$
			throw e;
		}
		throw new AOCancelledOperationException("Acceso a la clave privada no permitido"); //$NON-NLS-1$
	}

	/** Firma del fichero pdf.
	 * @param originalPath Ruta de origen.
	 * @param destinyPath Ruta de destino.
	 * @param policyIdentifier Identificador de pol&iacute;ticas.
	 * @param fieldName Nombre del campo.
	 * @param tsaName Nombre tsa.
	 * @param xmlLook Apariencia del XML.
	 * @throws PdfException Error en la lectua del pdf.
	 * @throws XMLException Errir en la lectura del XML.
	 * @throws AOCertificatesNotFoundException No se han encontrado certficados.
	 * @throws BadPdfPasswordException Password incorrecta.
	 * @throws PdfIsCertifiedException Pdf no certificado.
	 * @throws PdfHasUnregisteredSignaturesException Firma del pdf no registrada.
	 */
	public static void pdfSign(final String originalPath,
								   final String destinyPath,
								   final String policyIdentifier,
								   final String fieldName,
								   final String tsaName,
								   final String xmlLook) throws PdfException,
																XMLException,
																AOCertificatesNotFoundException,
																BadPdfPasswordException,
																PdfIsCertifiedException,
																PdfHasUnregisteredSignaturesException {

		final AOSigner signer = AOSignerFactory.getSigner(AOSignConstants.SIGN_FORMAT_PADES);

		byte[] data = null;
		try ( final InputStream fis = new FileInputStream(new File(originalPath)); ) {
        	data = AOUtil.getDataFromInputStream(fis);
		}
		catch (final Exception e) {
			LOGGER.severe("Error leyendo fichero de entrada: " + e); //$NON-NLS-1$
			throw new PdfException("Error leyendo fichero de entrada: " + e, e); //$NON-NLS-1$
		}
		SignatureField field = null;
		if (fieldName != null && !fieldName.isEmpty()) {
			final List<SignatureField> list = PdfUtil.getPdfEmptySignatureFields(data);
			for (final SignatureField sf : list) {
				if (sf.getName().equals(fieldName)) {
					field = sf;
				}
			}
		}

		final Properties p = new Properties();

		PolicyIdFilter policyFilter = null;
		if (policyIdentifier != null && !policyIdentifier.isEmpty()) {
			policyFilter = new PolicyIdFilter(policyIdentifier);
			p.setProperty("policyIdentifier", policyIdentifier); //$NON-NLS-1$
		}
		if (tsaName != null && !tsaName.isEmpty()) {
			p.setProperty("tsaPolicy", tsaName); //$NON-NLS-1$
		}

		ArrayList<CertificateFilter> filters = null;
		if (policyFilter != null) {
			filters = new ArrayList<>();
			filters.add(policyFilter);
		}
		final PrivateKeyEntry pke;
        try {
            pke = getPrivateKeyEntry(filters);
        }
        catch (final AOCancelledOperationException e) {
        	throw e; 
        }
        catch(final AOCertificatesNotFoundException e) {
        	LOGGER.severe("El almacen no contiene ningun certificado que se pueda usar para firmar: " + e); //$NON-NLS-1$
        	throw e;
        }
        catch (final Exception e) {
        	LOGGER.severe("Ocurrio un error al extraer la clave privada del certificiado seleccionado: " + e); //$NON-NLS-1$
        	throw new PdfException("Ocurrio un error al extraer la clave privada del certificiado seleccionado: " + e, e); //$NON-NLS-1$
    	}

        final String signatureAlgorithm = PreferencesManager.get(
    		PreferencesManager.PREFERENCE_GENERAL_SIGNATURE_ALGORITHM, "SHA512withRSA" //$NON-NLS-1$
		);

        new XMLLookParser(xmlLook, field, p, pke).parse();

        final byte[] signResult;
        try (
        		final FileOutputStream os = new FileOutputStream(new File(destinyPath))
        		){
            signResult = signer.sign(
        		data,
        		signatureAlgorithm,
        		pke.getPrivateKey(),
                pke.getCertificateChain(),
                p
            );
        	os.write(signResult);
        	os.close();
        }
        catch(final AOCancelledOperationException e) {
        	throw new AOCancelledOperationException("Cancelado por el usuario: " + e, e); //$NON-NLS-1$
        }
        catch(final PdfIsCertifiedException e) {
        	LOGGER.severe("PDF no firmado por estar certificado: " + e); //$NON-NLS-1$
        	throw e;
        }
        catch(final BadPdfPasswordException e) {
        	LOGGER.severe("PDF protegido con contrasena mal proporcionada: " + e); //$NON-NLS-1$
        	throw e;
        }
        catch(final PdfHasUnregisteredSignaturesException e) {
        	LOGGER.severe("PDF con firmas no registradas: " + e); //$NON-NLS-1$
        	throw e;
        }
        catch(final OutOfMemoryError ooe) {
            LOGGER.severe("Falta de memoria en el proceso de firma: " + ooe); //$NON-NLS-1$
            throw new OutOfMemoryError("Falta de memoria en el proceso de firma: " + ooe); //$NON-NLS-1$
        }
        catch(final Exception e) {
            LOGGER.severe("Error durante el proceso de firma: " + e); //$NON-NLS-1$
            throw new PdfException("Error durante el proceso de firma: " + e, e); //$NON-NLS-1$
        }
	}

	/** Verifica la firma correcta de un fichero.
	 * @param filePath Ruta del fichero firmado.
	 * @return True si la verificaci&oacute;n es correcta.
	 * @throws PdfException Error en la apertura o en el acceso al fichero.
	 */
	public static boolean verifySignature(final String filePath) throws PdfException {
		byte[] sign = null;
		try ( final FileInputStream fis = new FileInputStream(new File(filePath)) ) {
			sign = AOUtil.getDataFromInputStream(fis);
			return SignValiderFactory.getSignValider(sign).validate(sign).getValidity().equals(SIGN_DETAIL_TYPE.OK);
		}
		catch(final Exception e) {
			LOGGER.severe("Error validando la firma del PDF: " + e); //$NON-NLS-1$
			throw new PdfException("Error validando la firma del PDF: " + e, e); //$NON-NLS-1$
		}
	}

	/** Obtiene la clave privada tras aplicar los filtros.
	 * @param filters Filtros para la obtenci&oacute;n de la clave.
	 * @return Clave privada.
	 * @throws UnrecoverableEntryException Error en la inicializaci&oacute;n del almac&eacute;n.
	 * @throws AOCertificatesNotFoundException No se han encontrados certificados.
	 * @throws AOKeyStoreManagerException Error en la inicializaci&oacute;n del almac&eacute;n.
	 * @throws KeyStoreException Error en la inicializaci&oacute;n del almac&eacute;n.
	 * @throws NoSuchAlgorithmException No existe el algoritmo.
	 */
	public static PrivateKeyEntry getPrivateKeyEntry(final List<? extends CertificateFilter> filters) throws UnrecoverableEntryException, AOCertificatesNotFoundException, AOKeyStoreManagerException, KeyStoreException, NoSuchAlgorithmException
	{
		final AOKeyStoreManager ksm = SimpleKeyStoreManager.getKeyStore(false, null);
		final AOKeyStoreDialog dialog = new AOKeyStoreDialog(
				ksm,
				null,
				true,             // Comprobar claves privadas
				false,            // Mostrar certificados caducados
				true,             // Comprobar validez temporal del certificado
				filters, 				// Filtros
				false             // mandatoryCertificate
			);
    	dialog.show();
    	ksm.setParentComponent(null);
    	return ksm.getKeyEntry(
			dialog.getSelectedAlias()
		);
	}

	/** Se crea un campo vac&iacute;o de firma para una p&aacute;gina y posici&oacute;n concretos.
	 *  La p&aacute;gina puede ser menor a 0 para comenzar a contar desde la &uacute;ltima p&aacute;gina
	 *  (-1 = &uacute;ltima, -2 = pen&uacute;ltima, etc). 
	 *  En caso de ser 0 el campo se crea en la primera p&aacute;gina.
	 * @param filePath Fichero pdf sobre el que incluir el campo de firma.
	 * @param page N&uacute;mero de p&aacute;gina.
	 * @param leftX Coordinada izquierda X de la posici&oacute;n del campo.
	 * @param leftY Coordinada izquierda Y de la posici&oacute;n del campo.
	 * @param rightX Coordinada derecha X de la posici&oacute;n del campo.
	 * @param rightY Coordinada izquierda Y de la posici&oacute;n del campo.
	 * @throws DocumentException Error al abrir el documento.
	 * @throws IOException Error al encontrar el fichero.
	 * @throws PdfException Error al obtener el n&uacute;mero de p&aacute;ginas.
	 * @throws IllegalArgumentException N&uacute;mero de p&aacute;gina fuera de los l&iacute;mites.
	 */
	public static void addSignField(final String filePath, final int page, final int leftX, final int leftY, final int rightX, final int rightY) throws DocumentException, IOException, PdfException {
		int pageNbr = getPdfPageNumber(filePath);
		if(page > pageNbr || page < pageNbr*-1) {
			throw new IllegalArgumentException("El numero de pagina no puede ser superior al numero total"); //$NON-NLS-1$
		}
		final PdfReader reader = new PdfReader(filePath);
		try (
				FileOutputStream fos = new FileOutputStream(filePath)
				) {
			PdfStamper stamper = new PdfStamper(reader, fos, new GregorianCalendar());
			PdfFormField sig = PdfFormField.createSignature(stamper.getWriter()); 
			sig.setWidget(new Rectangle(leftX, leftY, rightX, rightY), null); 
			sig.setFlags(PdfAnnotation.FLAGS_PRINT); 
			sig.put(PdfName.DA, new PdfString("/Helv 0 Tf 0 g"));  //$NON-NLS-1$
			sig.setFieldName("SIGNATURE");  //$NON-NLS-1$
			int finalPage;
			if(page > 0) {
				finalPage = page;
			}
			else if(page < 0) {
				finalPage = pageNbr + page + 1;
			}
			else {
				finalPage = 1;
			}
			sig.setPage(finalPage); 
			stamper.addAnnotation(sig, finalPage); 
			stamper.close(new GregorianCalendar()); 
		}
		catch (IOException e) {
			throw new IOException("El fichero " + filePath + " no existe: " + e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	/** Obtiene el CN del certificado cargado de una tarjeta.
	 * @return CN de la tarjeta.
	 * @throws SmartCardException Error en la inicializaci&oacute;n de la tarjeta
	 */
	public static String cnTarjeta() throws SmartCardException {
		AOKeyStoreManager ksm = null;
		try {
			ksm = AOKeyStoreManagerFactory.getAOKeyStoreManager(
			    AOKeyStore.TEMD, // Store
			    null, // Lib
			    "TEMD (Tarjeta del Ministerio de Defensa)", // Description //$NON-NLS-1$
				null, // PasswordCallback
				null // Parent
			);
		}
		catch (final Exception e) {
			LOGGER.severe("Error recuperando el almacen de tarjetas del Ministerio de Defensa: " + e); //$NON-NLS-1$
			throw new SmartCardException("Error recuperando el almacen de tarjetas del Ministerio de Defensa: " + e, e); //$NON-NLS-1$
		}
		final String[] aliases = ksm.getAliases();
		for (final String al : aliases) {
			System.out.println(al);
		}
		if (aliases.length != 1) {
			LOGGER.severe("Hay mas de una tarjeta de defensa insertada"); //$NON-NLS-1$
			throw new SmartCardException("Hay mas de una tarjeta de defensa insertada"); //$NON-NLS-1$
		}
		return AOUtil.getCN(ksm.getCertificate(aliases[0]));
	}

}
