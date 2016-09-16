package es.gob.afirma.standalone.ui.envelopes;

import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

import javax.security.auth.callback.PasswordCallback;

import org.junit.Test;

import es.gob.afirma.core.ciphers.AOCipherConfig;
import es.gob.afirma.core.ciphers.CipherConstants.AOCipherAlgorithm;
import es.gob.afirma.envelopers.cms.AOCMSEnveloper;
import es.gob.afirma.keystores.AOKeyStore;
import es.gob.afirma.keystores.AOKeyStoreManager;
import es.gob.afirma.keystores.AOKeyStoreManagerFactory;
import es.gob.afirma.keystores.callbacks.CachePasswordCallback;

public class TestAperturaSobres {

	@Test
	public void testEnsobrarP11DesensobrarWindows() throws Exception {

		// ========== ENVOLVEMOS

		Logger.getLogger("es.gob.afirma").info("ENVOLVEMOS"); //$NON-NLS-1$ //$NON-NLS-2$

		final PasswordCallback pssCallback = new CachePasswordCallback("A111111a".toCharArray()); //$NON-NLS-1$

		final AOKeyStoreManager ksm = AOKeyStoreManagerFactory.getAOKeyStoreManager(
				AOKeyStore.TEMD,
				null,
				null,
				pssCallback,
				null);

		X509Certificate dest = null;

		for (final String alias : ksm.getAliases()) {
			final X509Certificate cert = ksm.getCertificate(alias);
			if (cert.getKeyUsage()[2] == true) {
				dest = ksm.getCertificate(alias);
				break;
			}
		}

		assert dest != null;

		for (final boolean use : dest.getKeyUsage()) {
			System.out.print(", " + use); //$NON-NLS-1$
		}
		System.out.println();

		final AOCipherConfig cipherConfig = new AOCipherConfig(AOCipherAlgorithm.AES, null, null);

		final AOCMSEnveloper enveloper = new AOCMSEnveloper();
		final byte[] envelop = enveloper.createCMSEnvelopedData(
				"Hola Mundo".getBytes(), //$NON-NLS-1$
				null,
				cipherConfig,
				new X509Certificate[] { dest },
				Integer.valueOf(128));

		// =========== DESENVOLVEMOS

		Logger.getLogger("es.gob.afirma").info("DESENVOLVEMOS"); //$NON-NLS-1$ //$NON-NLS-2$

		final AOKeyStoreManager ksm2 = AOKeyStoreManagerFactory.getAOKeyStoreManager(
				AOKeyStore.WINDOWS,
				null,
				null,
				pssCallback,
				null);

		PrivateKeyEntry pke = null;

		for (final String alias : ksm2.getAliases()) {
			final X509Certificate cert = ksm2.getCertificate(alias);
			if (cert.getSerialNumber().equals(dest.getSerialNumber())) {
				pke = ksm2.getKeyEntry(alias);
				break;
			}
		}

		final byte[] data = enveloper.recoverData(
				envelop,
				pke);

		System.out.println(new String(data));
	}
}
