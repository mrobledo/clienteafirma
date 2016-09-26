package es.gob.afirma.standalone.ui.envelopes;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import es.gob.afirma.core.AOCancelledOperationException;
import es.gob.afirma.core.ciphers.AOCipherConfig;
import es.gob.afirma.core.ciphers.CipherConstants.AOCipherAlgorithm;
import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.ui.AOUIFactory;
import es.gob.afirma.envelopers.cms.AOCMSEnveloper;
import es.gob.afirma.keystores.AOKeyStoreManager;
import es.gob.afirma.keystores.filters.CertificateFilter;
import es.gob.afirma.keystores.filters.CipherCertificateFilter;
import es.gob.afirma.standalone.SimpleAfirmaMessages;

final class Enveloper {

	private static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$
	private static final Integer AES_KEY_SIZE = Integer.valueOf(256);

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
		dialog.getEnvelopeData().getCertificateRecipientsList().add(
			new CertificateDestiny(
				"Remitente", //$NON-NLS-1$
				getPublicCipherCert(
					senderKeyStoreManager,
					(X509Certificate) senderPrivateKeyEntry.getCertificate()
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


        final X509Certificate[] certs = new X509Certificate[dialog.getEnvelopeData().getCertificateRecipientsList().size()];
        for (int i = 0; i < dialog.getEnvelopeData().getCertificateRecipientsList().size(); i++) {
            certs[i] = (X509Certificate) dialog.getEnvelopeData().getCertificateRecipientsList().get(i).getCertificate();

            System.out.println("CN: " + AOUtil.getCN(certs[i]));
            for (final boolean b : certs[i].getKeyUsage()) {
            	System.out.print(", " + b);
            }
            System.out.println();
        }

        try {
       		switch(dialog.getEnvelopeData().getEnvelopeType()) {
       			case SIGNED:
       				envelopedData = enveloper.createCMSSignedAndEnvelopedData(
   						contentData,
   						senderPrivateKeyEntry,
   						cipherConfig,
   						certs,
   						AES_KEY_SIZE
   					);
       				break;
       			case SIMPLE:
       				envelopedData = enveloper.createCMSEnvelopedData(
   						contentData,
   						senderPrivateKeyEntry,
   						cipherConfig,
   						certs,
   						AES_KEY_SIZE
   					);
       				break;
   				default:
   					throw new IllegalStateException(
						"Tipo de sobre no soportado: " + dialog.getEnvelopeData().getEnvelopeType() //$NON-NLS-1$
					);
       		}
       	}
        catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No se ha posido crear el sobre: " + e, e); //$NON-NLS-1$
			AOUIFactory.showMessageDialog(
        		dialog,
        		SimpleAfirmaMessages.getString("DigitalEnvelopeSender.30"), //$NON-NLS-1$
        		SimpleAfirmaMessages.getString("DigitalEnvelopeSender.31"), //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

       	File savedFile;
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
		catch(final AOCancelledOperationException e) {
			savedFile = null;
		}
        // Si el usuario cancela el guardado de los datos, no nos desplazamos a la ultima pantalla
        if (savedFile == null) {
            return false;
        }
		return true;
	}

	/**
	 * Obtiene del almac&eacute;n de claves el certificado parejo de cifrado.
	 * @param ksm Almacen de claves.
	 * @param signingCert Certificado de firma.
	 * @return Certificado de cifrado asociado al de firma o el propio certificado de firma
	 * si no se encuentra.
	 */
	private static X509Certificate getPublicCipherCert(final AOKeyStoreManager ksm, final X509Certificate signingCert) {
		final CertificateFilter ccf = new CipherCertificateFilter();
		if (ccf.matches(signingCert)) {
			return signingCert;
		}
		final String[] cipherAliases = ccf.matches(ksm.getAliases(), ksm);
		if (cipherAliases != null) {
			for (final String alias : cipherAliases) {
				final X509Certificate tmpCert = ksm.getCertificate(alias);
				if (
					tmpCert.getIssuerX500Principal().equals(signingCert.getIssuerX500Principal()) &&
					tmpCert.getNotAfter().equals(signingCert.getNotAfter())
				) {
					LOGGER.info(
						"Se ha anadido como destinatario del sobre el certificado: " + AOUtil.getCN(tmpCert) //$NON-NLS-1$
					);
					return tmpCert;
				}
			}
		}
		LOGGER.warning(
			"No se ha encontrado un certificado de cifrado para el remitente del sobre, se anadira como destinatario el propio de firma" //$NON-NLS-1$
		);
		return signingCert;
	}
}
