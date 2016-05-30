package es.gob.afirma.keyone;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import com.aowagie.text.pdf.PdfReader;
import com.aowagie.text.pdf.PdfStamper;

import es.gob.afirma.cert.signvalidation.SignValiderFactory;
import es.gob.afirma.cert.signvalidation.SignValidity.SIGN_DETAIL_TYPE;
import es.gob.afirma.core.AOCancelledOperationException;
import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.signers.AOSignConstants;
import es.gob.afirma.core.signers.AOSigner;
import es.gob.afirma.core.signers.AOSignerFactory;
import es.gob.afirma.keystores.AOCertificatesNotFoundException;
import es.gob.afirma.keystores.AOKeyStoreDialog;
import es.gob.afirma.keystores.AOKeyStoreManager;
import es.gob.afirma.keystores.AOKeyStoreManagerException;
import es.gob.afirma.signers.pades.BadPdfPasswordException;
import es.gob.afirma.signers.pades.PdfHasUnregisteredSignaturesException;
import es.gob.afirma.signers.pades.PdfIsCertifiedException;
import es.gob.afirma.signers.pades.PdfUtil;
import es.gob.afirma.signers.pades.PdfUtil.SignatureField;
import es.gob.afirma.standalone.SimpleKeyStoreManager;
import es.gob.afirma.standalone.ui.preferences.PreferencesManager;

public class KeyOne {

	private static final String SEPARATOR = ","; //$NON-NLS-1$
	private static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$


	private static String enumSignatureFieldNames(final String filePath) {
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
        	LOGGER.warning("Error recuperando los nombres de campos de firma del PDF: " + e); //$NON-NLS-1$
        	//throw new Exception("Error recuperando los nombres de campos de firma del pdf: " + e);
        	return null;
        }
	}

	private static boolean addBlankPage(final String filePath) {
        try ( final ByteArrayOutputStream baos = new ByteArrayOutputStream() ) {
        	final PdfReader pdfReader = new PdfReader(filePath);
        	final Calendar cal = Calendar.getInstance();
        	final PdfStamper stp = new PdfStamper(pdfReader, baos, cal);
        	stp.insertPage(pdfReader.getNumberOfPages() + 1, pdfReader.getPageSizeWithRotation(1));
        	stp.close(cal);
        	pdfReader.close();
        	final FileOutputStream os = new FileOutputStream(new File(filePath));
        	os.write(baos.toByteArray());
        	os.close();
        	return true;
        }
        catch(final Exception e) {
        	LOGGER.warning("Error anadiendo pagina en blanco al PDF: " + e); //$NON-NLS-1$
        	return false;
        }
	}

	private static boolean pdfSign(final String originalPath,
								   final String destinyPath,
								   final String policyIdentifier,
								   final String fieldName,
								   final String tsaName,
								   final String xmlLook) {

		final AOSigner signer = AOSignerFactory.getSigner(AOSignConstants.SIGN_FORMAT_PADES);

		byte[] encoded = null;
		try {
			encoded = Files.readAllBytes(Paths.get("C:\\Users\\A621916\\Desktop\\Pruebas\\defensa\\aparienciaPrueba.xml"));
		} catch (final IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		final String xml = new String(encoded);
		final Properties p = new XMLLookParser(xml).parse();
		//final Properties p = new Properties();
		if (fieldName != null && !fieldName.isEmpty()) {
			p.setProperty("signatureField", fieldName); //$NON-NLS-1$
		}
		if (policyIdentifier != null && !policyIdentifier.isEmpty()) {
			p.setProperty("policyIdentifier", policyIdentifier);
		}
		if (tsaName != null && !tsaName.isEmpty()) {
			p.setProperty("tsaPolicy", tsaName);
		}

		final PrivateKeyEntry pke;
        try {
            pke = getPrivateKeyEntry();
        }
        catch (final AOCancelledOperationException e) {
            return false;
        }
        catch(final AOCertificatesNotFoundException e) {
        	LOGGER.severe("El almacen no contiene ningun certificado que se pueda usar para firmar: " + e); //$NON-NLS-1$
        	return false;
        }
        catch (final Exception e) {
        	LOGGER.severe("Ocurrio un error al extraer la clave privada del certificiado seleccionado: " + e); //$NON-NLS-1$
        	return false;
    	}

        final String signatureAlgorithm = PreferencesManager.get(
    		PreferencesManager.PREFERENCE_GENERAL_SIGNATURE_ALGORITHM, "SHA512withRSA" //$NON-NLS-1$
		);

        LOGGER.info("prop: " + p);
        final byte[] signResult;
        try ( final InputStream fis = new FileInputStream(new File(originalPath)); ) {
        	final byte[] data = AOUtil.getDataFromInputStream(fis);
            signResult = signer.sign(
        		data,
        		signatureAlgorithm,
        		pke.getPrivateKey(),
                pke.getCertificateChain(),
                p
            );
            final FileOutputStream os = new FileOutputStream(new File(destinyPath));
        	os.write(signResult);
        	os.close();
        	return true;
        }
        catch(final AOCancelledOperationException e) {
            return false;
        }
        catch(final PdfIsCertifiedException e) {
        	LOGGER.warning("PDF no firmado por estar certificado: " + e); //$NON-NLS-1$
            return false;
        }
        catch(final BadPdfPasswordException e) {
        	LOGGER.warning("PDF protegido con contrasena mal proporcionada: " + e); //$NON-NLS-1$
            return false;
        }
        catch(final PdfHasUnregisteredSignaturesException e) {
        	LOGGER.warning("PDF con firmas no registradas: " + e); //$NON-NLS-1$
            return false;
        }
        catch(final Exception e) {
            LOGGER.severe("Error durante el proceso de firma: " + e); //$NON-NLS-1$
            return false;
        }
        catch(final OutOfMemoryError ooe) {
            LOGGER.severe("Falta de memoria en el proceso de firma: " + ooe); //$NON-NLS-1$
            return false;
        }
	}

	private static boolean verifySignature(final String filePath) {
		byte[] sign = null;
		try ( final FileInputStream fis = new FileInputStream(new File(filePath)) ) {
			sign = AOUtil.getDataFromInputStream(fis);
			return SignValiderFactory.getSignValider(sign).validate(sign).getValidity().equals(SIGN_DETAIL_TYPE.OK);
		}
		catch(final Exception e) {
			LOGGER.warning("Error validando la firma del PDF: " + e); //$NON-NLS-1$
			return false;
		}
	}

	private static PrivateKeyEntry getPrivateKeyEntry() throws UnrecoverableEntryException,
																AOKeyStoreManagerException,
																AOCertificatesNotFoundException,
																KeyStoreException,
																NoSuchAlgorithmException
	{
		final AOKeyStoreManager ksm = SimpleKeyStoreManager.getKeyStore(false, null);
		final AOKeyStoreDialog dialog = new AOKeyStoreDialog(
				ksm,
				null,
				true,             // Comprobar claves privadas
				false,            // Mostrar certificados caducados
				true,             // Comprobar validez temporal del certificado
				null, 				// Filtros
				false             // mandatoryCertificate
			);
	    	dialog.show();
	    	ksm.setParentComponent(null);
	    	return ksm.getKeyEntry(
				dialog.getSelectedAlias()
			);
	}


	//TODO
	public static void main(final String[] args) {
		//System.out.println("verify: " + verifySignature("C:\\Users\\A621916\\Desktop\\Pruebas\\defensa\\aparienciaPrueba.xml_signed.xsig")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println("verify: " + pdfSign( //$NON-NLS-1$
			"C:\\Users\\A621916\\Desktop\\Pruebas\\empty_signature_field.pdf", //$NON-NLS-1$
			"C:\\Users\\A621916\\Desktop\\Pruebas\\empty_signature_field_signed2.pdf", //$NON-NLS-1$
			null,
			null,
			null,
			null
			)
		);
	}

}
