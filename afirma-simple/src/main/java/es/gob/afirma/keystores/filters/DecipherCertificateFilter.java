package es.gob.afirma.keystores.filters;

import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import es.gob.afirma.core.keystores.KeyStoreManager;
import es.gob.afirma.keystores.filters.rfc.KeyUsageFilter;

/** Filtro de certificados que muestra todos los certificados excepto aquellos para los que existe
 * alguno equivalente expresamente preparado para el cifrado de claves y datos. Por ejemplo, si nos
 * encontramos el certificado de firma del DNIe, se mostrar&aacute;; pero si nos encontramos el de
 * firma o el de autenticaci&oacute;n de la tarjeta de Defensa, no se mostrar&aacute; porque existe
 * uno de cifrado equivalente. El certificado de cifrado en cuesti&oacute;n s&iacute; se mostrar&aacute;.  */
public final class DecipherCertificateFilter extends CertificateFilter {

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

	/** {@inheritDoc} */
	@Override
	public String[] matches(final String[] aliases, final KeyStoreManager ksm) {

		X509Certificate cert;
		final Set<String> filteredCerts = new HashSet<>();
		for (final String aliase : aliases) {
			cert = ksm.getCertificate(aliase);
			if (cert == null) {
				Logger.getLogger("es.gob.afirma").warning( //$NON-NLS-1$
						"No se pudo recuperar el certificado: " + aliase); //$NON-NLS-1$
				continue;
			}
			try {
				// Si es de cifrado
				if (this.matches(cert)) {
					filteredCerts.add(aliase);
				}
				// Si no es de cifrado y no tiene certificado parejo lo agregamos al listado, si no, lo omitimos
				else if (!hasQualifiedCipherCertificate(cert, ksm, aliases)) {
					filteredCerts.add(aliase);
				}
			}
			catch (final Exception e) {
				Logger.getLogger("es.gob.afirma").warning( //$NON-NLS-1$
						"Error en la verificacion del certificado '" + //$NON-NLS-1$
						cert.getSerialNumber() + "': " + e);  //$NON-NLS-1$
			}
		}
		return filteredCerts.toArray(new String[filteredCerts.size()]);
	}

	/**
	 * Comprueba si el certificado declara los keyusages de cifrado de datos y clave.
	 */
	private static boolean isCipherCertificate(final X509Certificate cert) {
		return keyUsageFilter.matches(cert);
	}

	/** Recupera el certificado qualificado de cifrado pareja del certificado indicado.
	 * @param cert Certificado del que buscar la pareja de firma.
	 * @param ksm Gestor del almac&eacute;n de certificados.
	 * @param aliases Alias de los certificados candidatos.
	 * @return Alias del certificado pareja del indicado o {@code null} si no se ha
	 * localizado una pareja satisfactoria. */
	private static boolean hasQualifiedCipherCertificate(final X509Certificate cert, final KeyStoreManager ksm, final String[] aliases) {
		X509Certificate cert2;
		for (final String aliase : aliases) {
			cert2 = ksm.getCertificate(aliase);
			if (!cert.getSerialNumber().equals(cert2.getSerialNumber())) {
				final boolean sameIssuer = cert.getIssuerDN() == null ?
						cert2.getIssuerDN() == null :
							cert.getIssuerDN().equals(cert2.getIssuerDN());
				final boolean sameSubjectSN = FilterUtils.getSubjectSN(cert) == null ?
					FilterUtils.getSubjectSN(cert2) == null :
						FilterUtils.getSubjectSN(cert).equals(FilterUtils.getSubjectSN(cert2));
				final boolean sameExpiredData = getExpiredDate(cert) == null ?
						getExpiredDate(cert2) == null : getExpiredDate(cert).equals(getExpiredDate(cert2));
				if (isCipherCertificate(cert2) && sameIssuer && sameSubjectSN && sameExpiredData) {
					return true;
				}
			}
		}
		return false;
	}

	/** Recupera la fecha de expiraci&oacute;n del certificado en formato "yyyy-MM-dd".
	 * @param cert Certificado.
	 * @return Fecha de caducidad. */
	private static String getExpiredDate(final X509Certificate cert) {
		return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cert.getNotAfter()); //$NON-NLS-1$
	}
}
