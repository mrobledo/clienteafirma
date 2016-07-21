package es.gob.afirma.standalone.ui.envelopes;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.swing.JOptionPane;

import es.gob.afirma.core.ciphers.AOCipherConfig;
import es.gob.afirma.core.ciphers.CipherConstants.AOCipherAlgorithm;
import es.gob.afirma.core.ui.AOUIFactory;
import es.gob.afirma.envelopers.cms.AOCMSEnveloper;
import es.gob.afirma.keystores.AOKeyStoreManager;
import es.gob.afirma.standalone.SimpleAfirmaMessages;

final class Enveloper {

	private static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$
	private static final Integer AES_MAX_KEY_SIZE = Integer.valueOf(256);

	private Enveloper() {
		// No instanciable
	}

    /** Genera el sobre digital.
     * @param dialog Di&aacute;logo padre del asistente.
     * @param senderPrivateKeyEntry Clave privada del remitente para la firma del sobre.
     * @param senderKeyStoreManager Almac&eacute;n de claves del remitente.
     * @return Devuelve <code>true</code> si se ha podido generar el sobre correctamente, <code>false</code> en caso contrario. */
	static boolean createEnvelope(final DigitalEnvelopePresentation dialog,
			                      final PrivateKeyEntry senderPrivateKeyEntry,
			                      final AOKeyStoreManager senderKeyStoreManager) {

		if (dialog == null) {
			throw new IllegalArgumentException(
				"El dialogo padre no puede ser nulo" //$NON-NLS-1$
			);
		}

		// Primero anadimos como destinatario al propio remitente
		dialog.getEnvelopeData().addCertificateRecipients(
			Collections.singletonList(
				new CertificateDestiny(
					"Remitente", //$NON-NLS-1$
					getPublicCypherCert(
						senderKeyStoreManager,
						senderPrivateKeyEntry.getCertificate()
					)
				)
			)
		);

        final AOCMSEnveloper enveloper = new AOCMSEnveloper();
        byte[] contentData = null;
        byte[] envelopedData = null;
        enveloper.setSignatureAlgorithm(
    		dialog.getEnvelopeData().getSignatureAlgorithm()
		);
        final AOCipherConfig cipherConfig = new AOCipherConfig(AOCipherAlgorithm.AES, null, null);

        try {
        	contentData = EnvelopesUtils.readFile(
    			dialog.getEnvelopeData().getFilePath()
			);
        }
        catch(final OutOfMemoryError e) {
        	LOGGER.severe("Error de memoria: " + e); //$NON-NLS-1$
        	AOUIFactory.showMessageDialog(
        		dialog,
        		SimpleAfirmaMessages.getString("DigitalEnvelopeSender.26"), //$NON-NLS-1$
        		SimpleAfirmaMessages.getString("DigitalEnvelopeSender.27"), //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
        	return false;
        }
        catch (final IOException e) {
        	LOGGER.severe("Error leyendo el fichero: " + e); //$NON-NLS-1$
        	AOUIFactory.showMessageDialog(
        		dialog,
        		SimpleAfirmaMessages.getString("DigitalEnvelopeSender.28"), //$NON-NLS-1$
        		SimpleAfirmaMessages.getString("DigitalEnvelopeSender.29"), //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
        	return false;
		}

        Integer keySize = null;
        final int aesMaxKeySize;
		try {
			aesMaxKeySize = Cipher.getMaxAllowedKeyLength(AOCipherAlgorithm.AES.getName());
			 if (aesMaxKeySize == Integer.MAX_VALUE && AOUIFactory.showConfirmDialog(
				dialog,
				SimpleAfirmaMessages.getString("DigitalEnvelopeSender.35"), //$NON-NLS-1$
				SimpleAfirmaMessages.getString("DigitalEnvelopeSender.36"), //$NON-NLS-1$
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE
			) == 0) {
	    		LOGGER.info("Se ha establecido la clave AES a " + Integer.toString(aesMaxKeySize) + " bits"); //$NON-NLS-1$ //$NON-NLS-2$
	    		keySize = AES_MAX_KEY_SIZE;
	        }
		}
		catch (final NoSuchAlgorithmException e1) {
			LOGGER.info("Error obteniendo el tamano maximo AES: " + e1); //$NON-NLS-1$
		}

        final X509Certificate[] certs = new X509Certificate[dialog.getEnvelopeData().getCertificateRecipientsList().size()];
        for (int i = 0; i < dialog.getEnvelopeData().getCertificateRecipientsList().size(); i++) {
            certs[i] = (X509Certificate) dialog.getEnvelopeData().getCertificateRecipientsList().get(i).getCertificate();
        }

       	try {
       		switch(dialog.getEnvelopeData().getEnvelopeType()) {
       			case AUTHENTICATED:
       				envelopedData = enveloper.createCMSAuthenticatedEnvelopedData(
   						contentData,
   						senderPrivateKeyEntry,
   						cipherConfig,
   						certs,
   						keySize
   					);
       				break;
       			case SIGNED:
       				envelopedData = enveloper.createCMSSignedAndEnvelopedData(
   						contentData,
   						senderPrivateKeyEntry,
   						cipherConfig,
   						certs,
   						keySize
   					);
       				break;
       			case SIMPLE:
       				envelopedData = enveloper.createCMSEnvelopedData(
   						contentData,
   						senderPrivateKeyEntry,
   						cipherConfig,
   						certs,
   						keySize
   					);
       				break;
   				default:
   					throw new IllegalStateException(
						"Tipo de sobre no soportado: " + dialog.getEnvelopeData().getEnvelopeType() //$NON-NLS-1$
					);
       		}
       	}
        catch (final Exception e) {
			LOGGER.severe("No se ha posido crear el sobre: " + e); //$NON-NLS-1$
			AOUIFactory.showMessageDialog(
        		dialog,
        		SimpleAfirmaMessages.getString("DigitalEnvelopeSender.30"), //$NON-NLS-1$
        		SimpleAfirmaMessages.getString("DigitalEnvelopeSender.31"), //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

       	final File savedFile;
		try {
			savedFile = AOUIFactory.getSaveDataToFile(
			    envelopedData,
			    SimpleAfirmaMessages.getString("DigitalEnvelopeSender.32"), //$NON-NLS-1$
			    null,
			    new File(
		    		dialog.getEnvelopeData().getFilePath()
	    		).getName() + ".enveloped", //$NON-NLS-1$
			    null,
			    null,
			    dialog
			);
		}
		catch (final IOException e) {
			LOGGER.severe("No se ha posido guardar el sobre: " + e); //$NON-NLS-1$
			AOUIFactory.showMessageDialog(
        		dialog,
        		SimpleAfirmaMessages.getString("DigitalEnvelopeSender.33"), //$NON-NLS-1$
        		SimpleAfirmaMessages.getString("DigitalEnvelopeSender.31"), //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
            return false;
		}
        // Si el usuario cancela el guardado de los datos, no nos desplazamos a la ultima pantalla
        if (savedFile == null) {
            return false;
        }
		return true;
	}


	private static X509Certificate getPublicCypherCert(final AOKeyStoreManager ksm, final Certificate signingCert) {
		return (X509Certificate) signingCert;
	}
}
