package es.gob.afirma.standalone.ui.envelopes;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public final class LDAPMDEFManager implements LDAPMDEF {

	public LDAPMDEFManager() {
		//vacio
	}

	@Override
	public Users[] getUsers(final String substring) {
		final List<Users> users = new ArrayList<>();
		users.add(new Users("Usuario Prueba", "usuarioprueba@atos.net", "1234"));
		users.add(new Users("Usuario Prueba2", "usuarioprueba2@atos.net", "4321"));
		return users.toArray(new Users[0]);
	}

	@Override
	public X509Certificate getCertificate(final String uid) {
		 InputStream inStream = null;
		 X509Certificate cert = null;
		 String path;
		 if (uid.equals("1234")) {
			 path = "C:\\Users\\A621916\\Desktop\\Pruebas\\Certificadopruebacer.cer";
		 }
		 else {
			 path = "C:\\Users\\A621916\\Desktop\\Pruebas\\FisicoActivo.cer";
		 }
		 try {
		     inStream = new FileInputStream(path);
		     final CertificateFactory cf = CertificateFactory.getInstance("X.509");
		     cert = (X509Certificate)cf.generateCertificate(inStream);
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
		     if (inStream != null) {
		         try {
					inStream.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
		     }
		 }
		return cert;
	}

}
