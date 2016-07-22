package es.gob.afirma.standalone.ui.envelopes;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

final class FakeLDAPMDEFManager implements CertificateDirectoryProvider {

	FakeLDAPMDEFManager() {
		//vacio
	}

	@Override
	public Users[] getUsers(final String substring) {
		final List<Users> users = new ArrayList<>();
		users.add(new Users("Usuario Prueba", "usuarioprueba@atos.net", "1234")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		users.add(new Users("Usuario Prueba2", "usuarioprueba2@atos.net", "4321")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return users.toArray(new Users[0]);
	}

	@Override
	public X509Certificate getCertificate(final String uid) throws IOException, CertificateException {
		 String path;
		 if (uid.equals("1234")) { //$NON-NLS-1$
			 path = "C:\\Users\\A621916\\Desktop\\Pruebas\\Certificadopruebacer.cer"; //$NON-NLS-1$
		 }
		 else {
			 path = "/cert_test_fnmt.cer"; //$NON-NLS-1$
		 }
		 try ( final InputStream inStream = FakeLDAPMDEFManager.class.getResourceAsStream(path); ) {
		     final CertificateFactory cf = CertificateFactory.getInstance("X.509"); //$NON-NLS-1$
		     return (X509Certificate)cf.generateCertificate(inStream);
		 }
	}

}
