package es.gob.afirma.local.triphase.signer.processors;

import java.util.Properties;
import java.util.UUID;

/** Operaciones trif&aacute;sicas.
 * @author Sergio Mart&iacute;nez Rico. */
public final class TriPhaseUtil {

	private static final String PROP_ID = "SignatureId"; //$NON-NLS-1$

	private TriPhaseUtil() {
		// No instanciable
	}

	/**
	 * Recupera de los par&aacute;metros de configuraci&oacute;n el identificado de la firma
	 * que se est&aacute; procesando y, en caso de no estar reflejado, genera un identificador
	 * y lo agrega al extraParams.
	 * @param extraParams Par&aacute;metro de configuraci&oacute;n de la firma.
	 * @return Identificador de la firma.
	 */
	public static String getSignatureId(final Properties extraParams) {
		if (extraParams == null || !extraParams.containsKey(PROP_ID)) {
			return UUID.randomUUID().toString();
		}
		return extraParams.getProperty(PROP_ID);
	}

}
