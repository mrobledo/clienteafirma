package es.gob.afirma.local.signers.batch.server;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

import es.gob.afirma.core.signers.TriphaseData;
import es.gob.afirma.local.signers.batch.SignBatch;

/** Realiza la tercera (y &uacute;ltima) fase de un proceso de firma por lote.
 * Servlet implementation class BatchPostsigner
 */
public final class BatchPostsigner {

	private static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	private static final String BATCH_XML_PARAM = "xml"; //$NON-NLS-1$
	private static final String BATCH_CRT_PARAM = "certs"; //$NON-NLS-1$
	private static final String BATCH_TRI_PARAM = "tridata"; //$NON-NLS-1$

	/** Realiza la tercera y &uacute;ltima fase de un proceso de firma por lote.
	 * Debe recibir la definici&oacute;n XML (<a href="../doc-files/batch-scheme.html">descripci&oacute;n
	 * del formato</a>) del lote (exactamente la misma enviada para la primera fase)
	 * en un XML pero convertido a Base64 (puede ser en formato <i>URL Safe</i>) y la cadena de
	 * certificados del firmante (exactamente la misma que la enviada en la primera fase),
	 * convertidos a Base64 (puede ser <i>URL Safe</i>) y separados por punto y coma (<code>;</code>).<br>
	 * Devuelve un XML de resumen de resultado (<a href="../doc-files/resultlog-scheme.html">descripci&oacute;n
	 * del formato</a>). 
	 * @param xml Ruta del fichero XML.
	 * @param certListUrlSafeBase64 Certificados en Base64.
	 * @param triphaseDataAsUrlSafeBase64 Datos trif&aacute;sicos en Base64.
	 * @return Resultado de la postfirma.
	 * @throws IOException */
	public static String doBatchPostSign(final String xml,
			               			 final String certListUrlSafeBase64,
			               			 final String triphaseDataAsUrlSafeBase64) throws IOException {
		if (xml == null) {
			LOGGER.severe("No se ha recibido una definicion de lote en el parametro " + BATCH_XML_PARAM); //$NON-NLS-1$
			throw new IllegalArgumentException("No se ha recibido una definicion de lote en el parametro " + BATCH_XML_PARAM); //$NON-NLS-1$
		}

		final SignBatch batch;
		try {
			batch = BatchServerUtil.getSignBatch(xml);
		}
		catch(final Exception e) {
			LOGGER.severe("La definicion de lote es invalida: " + e); //$NON-NLS-1$
			return null;
		}

		if (certListUrlSafeBase64 == null) {
			LOGGER.severe("No se ha recibido la cadena de certificados del firmante en el parametro " + BATCH_CRT_PARAM); //$NON-NLS-1$
			return null;
		}

		final X509Certificate[] certs;
		try {
			certs = BatchServerUtil.getCertificates(certListUrlSafeBase64);
		}
		catch (final Exception e) {
			LOGGER.severe("La cadena de certificados del firmante es invalida: " + e); //$NON-NLS-1$
			return null;
		}

		if (triphaseDataAsUrlSafeBase64 == null) {
			LOGGER.severe("No se ha recibido el resultado de las firmas cliente en el parametro " + BATCH_TRI_PARAM); //$NON-NLS-1$
			return null;
		}

		final TriphaseData td;
		try {
			td = BatchServerUtil.getTriphaseData(triphaseDataAsUrlSafeBase64);
		}
		catch(final Exception e) {
			LOGGER.severe("El XML de firmas cliente es invalido: " + e); //$NON-NLS-1$
			return null;
		}

		final String ret;
		try {
			ret = batch.doPostBatch(certs, td);
		}
		catch (final Exception e) {
			LOGGER.severe("Error en el postproceso del lote: " + e); //$NON-NLS-1$
			return null;
		}

		return ret;
	}

}
