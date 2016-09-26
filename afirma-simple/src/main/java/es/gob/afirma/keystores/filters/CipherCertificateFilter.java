package es.gob.afirma.keystores.filters;

import java.security.cert.X509Certificate;

import es.gob.afirma.keystores.filters.rfc.KeyUsageFilter;

/** Filtro de certificados que muestra s&oacute; los certificados de cifrado de claves y datos. */
public final class CipherCertificateFilter extends CertificateFilter {

	/** Usos de clave permitidos en los certificados para la autenticaci&oacute;n de usuarios. */
    private static final Boolean[] AUTHENTICATION_CERT_USAGE = {
    	null, // digitalSignature
        null, // nonRepudiation
        Boolean.TRUE, // keyEncipherment
        Boolean.TRUE, // dataEncipherment
        null, // keyAgreement
        null, // keyCertSign
        null, // cRLSign
        null, // encipherOnly
        null  // decipherOnly
    };

	private static final KeyUsageFilter keyUsageFilter = new KeyUsageFilter(AUTHENTICATION_CERT_USAGE);

	/** {@inheritDoc} */
	@Override
	public boolean matches(final X509Certificate cert) {
		return isCipherCertificate(cert);
	}

	/**
	 * Comprueba si el certificado declara los keyusages de cifrado de datos y clave.
	 */
	private static boolean isCipherCertificate(final X509Certificate cert) {
		return keyUsageFilter.matches(cert);
	}
}
