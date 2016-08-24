package es.gob.afirma.cert.certvalidation;

import java.security.cert.X509Certificate;

/** Clase que valida los certificados de Defensa.
 * @author Sergio Mart&iacute;nez Rico. */
public class MdefCertificateVerifier extends CertificateVerifier {

	@Override
	public ValidationResult verifyRevocation(final X509Certificate cert) {
		// TODO verificar con servicio web
		return ValidationResult.REVOKED;
	}

	@Override
	public ValidationResult validateCertificate(final X509Certificate cert) {
		return ValidationResult.VALID;
	}

}
