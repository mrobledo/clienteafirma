package es.gob.afirma.standalone.ui.envelopes;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;

import es.gob.afirma.core.AOException;
import es.gob.afirma.envelopers.cms.AOCMSEnveloper;

/**
 * Clase de utilidad con funciones de sobrado y desensobrado.
 */
public class DigitalEnvelopWrapper {

	static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	/**
	 * Extrae los datos de un sobre digital y la informaci&oacute;n del propio sobre.
	 * @param envelop Sobre que se desea abrir.
	 * @param pke Clave privada para la apertura del sobre.
	 * @return Informacion del sobre.
	 * @throws InvalidKeyException Cuando no se puede abrir el sobre por no ser uno de los destinatarios o indicarse una clave invalida.
	 * @throws DataFormatException Cuando no se indique un tipo de envoltorio valido.
	 * @throws IOException Error de lectura de los datos.
	 * @throws AOException Cuando se produce un error desconocido al desenvolver los datos.
	 */
	public static EnvelopInfo unwrap(final byte[] envelop, final PrivateKeyEntry pke) throws InvalidKeyException, AOException, IOException, DataFormatException {

		final AOCMSEnveloper enveloper = new AOCMSEnveloper();

		byte[] data;
		try {
			data = enveloper.recoverData(envelop, pke);
		} catch (final InvalidKeySpecException e) {
			throw new InvalidKeyException("Error en la especificacion de la clave", e); //$NON-NLS-1$
		}

		final EnvelopInfo info = new EnvelopInfo(getWrapType(enveloper.getProcessedEnvelopType()));
		info.setData(data);


		return info;
	}

	/**
	 * Calcula el tipo de sobre digital en base al tipo de contenido CMS indicado.
	 * @param cmsContentType Tipo de contenido CMS.
	 * @return Tipo de sobre o {@code null} si no es un tipo reconocido.
	 */
	private static WrapType getWrapType(final String cmsContentType) {
		switch (cmsContentType) {
		case AOCMSEnveloper.CMS_CONTENTTYPE_ENVELOPEDDATA:
			return WrapType.SIMPLE;
		case AOCMSEnveloper.CMS_CONTENTTYPE_SIGNEDANDENVELOPEDDATA:
			return WrapType.SIGNED;
		case AOCMSEnveloper.CMS_CONTENTTYPE_AUTHENVELOPEDDATA:
			return WrapType.AUTHENTICATED;
		default:
			return null;
		}
	}
}
