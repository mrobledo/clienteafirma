package es.gob.afirma.standalone.ui.envelopes;

import java.security.cert.X509Certificate;

public interface LDAPMDEF {

    Users[] getUsers(final String substring);

    X509Certificate getCertificate(final String uid);

}