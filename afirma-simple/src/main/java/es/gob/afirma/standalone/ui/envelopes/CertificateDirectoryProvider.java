package es.gob.afirma.standalone.ui.envelopes;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/** Proveedor de acceso a directorio de usuarios para obtenci&oacute;n de certificados. */
public interface CertificateDirectoryProvider {

    /** Obtiene los usuarios que respondan a la cadena de b&uacute;squeda dada.
     * @param substring Cadena de b&uacute;squeda.
     * @return Lista de usuarios que respondan a la cadena de b&uacute;squeda dada. */
    Users[] getUsers(final String substring);

    /** Obtiene el certificado de un usuario.
     * @param uid Identificador del usuario.
     * @return Certificado del usuario cuyo identificador se ha proporcionado.
     * @throws IOException Si hay problemas obteniendo el certificado.
     * @throws CertificateException Si el certificado obtenido no es v&aacute;lido. */
    X509Certificate getCertificate(final String uid) throws IOException, CertificateException;

}