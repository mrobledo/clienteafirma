package es.gob.afirma.keystores.temd;

import org.junit.Test;

import es.gob.afirma.core.keystores.AutoCloseable;
import es.gob.afirma.keystores.AOKeyStoreManager;

/** Pruebas de TEMD.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class TestTemd {

	/** Prueba de creaci&oacute;n de almac&eacute;n TEMD.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void testTemdCreation() throws Exception {
		final AOKeyStoreManager ksm = new TemdKeyStoreManager(null);
		if (ksm instanceof AutoCloseable) {
			((AutoCloseable)ksm).closeIn(15);
		}
		System.out.println("OK - 1"); //$NON-NLS-1$
		for (final String alias : ksm.getAliases()) {
			System.out.println(alias);
		}
		Thread.sleep(20001);
		System.out.println("OK - 2"); //$NON-NLS-1$
		for (final String alias : ksm.getAliases()) {
			System.out.println(alias);
		}
		System.out.println("OK - 3"); //$NON-NLS-1$
	}

}
