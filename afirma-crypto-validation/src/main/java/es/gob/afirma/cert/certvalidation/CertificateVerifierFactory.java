/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 11/01/11
 * You may contact the copyright holder at: soporte.afirma5@mpt.es
 */

package es.gob.afirma.cert.certvalidation;

import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.zip.CRC32;


/** Factor&iacute;a para la obtenci&oacute;n de un validador de certificados.
 * Clase cedida por <a href="http://www.yohago.com/">YoHago</a>.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s */
public final class CertificateVerifierFactory {

	private static Properties p = null;

	private static final String FACTORY_CONFIGURATION = "/validationfactory.properties"; //$NON-NLS-1$

	private static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	private CertificateVerifierFactory() {
		// No permitimos la instanciacion
	}

	private static String getIssuerIdentifier(final X509Certificate cert) {
		// Es el CRC del emisor lo que le identifica
		final CRC32 issuerCrc = new CRC32();
		issuerCrc.update(cert.getIssuerX500Principal().getEncoded());
		final long intermediateCrc = issuerCrc.getValue();
		issuerCrc.reset();
		issuerCrc.update(cert.getSigAlgName().getBytes());
		return Long.toHexString(intermediateCrc + issuerCrc.getValue());
	}

	/** Obtiene un validador para el certificado proporcionado.
	 * @param cert Certificado a validar.
	 * @return Validador para el certificado proporcionado. */
	public static CertificateVerificable getCertificateVerifier(final X509Certificate cert) {
		if (cert == null) {
			throw new IllegalArgumentException("El certificado no puede ser nulo"); //$NON-NLS-1$
		}
		if (p == null) {
			p = new Properties();
			try {
				p.load(CertificateVerifierFactory.class.getResourceAsStream(FACTORY_CONFIGURATION));
			}
			catch (final Exception e) {
				p = null;
				throw new IllegalStateException(
					"No se ha podido cargar la configuracion de la factoria: " + e, e //$NON-NLS-1$
				);
			}
		}

		String crc = getIssuerIdentifier(cert);
		LOGGER.info("Identificador del emisor del certificado: " + crc); //$NON-NLS-1$

		if (p.getProperty(crc + ".validation.properties") == null) { //$NON-NLS-1$
			crc = "default"; //$NON-NLS-1$
		}

		final String validationProperties = p.getProperty(crc + ".validation.properties"); //$NON-NLS-1$
		final String validationClass = p.getProperty(crc + ".validation.type"); //$NON-NLS-1$
		try {
			final Class<?> certVerifierClass = Class.forName(validationClass);
			final CertificateVerificable certVerif = (CertificateVerificable) certVerifierClass.getConstructor().newInstance();
			certVerif.setValidationProperties(validationProperties);
			certVerif.setSubjectCert(cert);
			return certVerif;
		}
		catch (final ClassNotFoundException e) {
			LOGGER.warning("No se encuentran la clase validadora " + validationClass + ": " + e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch (final Exception e) {
			LOGGER.warning("No se ha podido instanciar el verificador del certificado " + validationClass + ": " + e); //$NON-NLS-1$ //$NON-NLS-2$
		}

		throw new IllegalStateException(
			"No se soporta el medio de validacion: " + validationClass //$NON-NLS-1$
		);
	}

}
