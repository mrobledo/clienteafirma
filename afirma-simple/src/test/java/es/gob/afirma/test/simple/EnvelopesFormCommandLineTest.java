package es.gob.afirma.test.simple;

import org.junit.Test;

import es.gob.afirma.standalone.SimpleAfirma;

public class EnvelopesFormCommandLineTest {

	/** Prueba apertura de sobres digitales. */
	@Test
	public void testOpenEnvelope() {
		SimpleAfirma.main(
			new String[] {
				"openenvelope", "-i", "C:\\Users\\A621916\\Desktop\\Pruebas\\xml_with_ids.xml.enveloped"  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		);
	}

	/** Prueba crear sobres digitales. */
	@Test
	public void testCreateEnvelope() {
		SimpleAfirma.main(
			new String[] {
				"createenvelope", "-i", "C:\\Users\\A621916\\Desktop\\Pruebas\\xml_with_ids.xml.enveloped"  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		);
	}

}
