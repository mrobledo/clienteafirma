package es.gob.afirma.standalone.certvalidation;

import java.security.cert.X509Certificate;

import es.gob.afirma.cert.certvalidation.CertificateVerifier;
import es.gob.afirma.cert.certvalidation.ValidationResult;

public class MdefCertificateVerifier extends CertificateVerifier {

	@Override
	public ValidationResult verifyRevocation(X509Certificate cert) {
		// TODO verificar con servicio web
		return ValidationResult.REVOKED;
	}
	
	@Override
	public ValidationResult validateCertificate(final X509Certificate cert) {
		return ValidationResult.VALID;
		
	}

}
