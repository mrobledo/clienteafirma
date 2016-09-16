package es.gob.afirma.cert.certvalidation;

import java.security.cert.X509Certificate;

/** Clase que valida los certificados que no tienen un mecanismo particular. Este validador
 * considera todos los certificados como no v&aacute;lidos. */
public class DefaultCertificateVerifier extends CertificateVerifier {

	@Override
	public ValidationResult verifyRevocation(final X509Certificate cert) {
		return ValidationResult.REVOKED;
	}

	@Override
	public ValidationResult validateCertificate(final X509Certificate cert) {
		return ValidationResult.CA_NOT_SUPPORTED;
	}
}
