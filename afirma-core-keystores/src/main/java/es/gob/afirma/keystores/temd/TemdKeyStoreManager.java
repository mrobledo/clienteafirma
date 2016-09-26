package es.gob.afirma.keystores.temd;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.logging.Logger;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;

import es.gob.afirma.core.misc.Platform;
import es.gob.afirma.keystores.AOKeyStore;
import es.gob.afirma.keystores.AOKeyStoreManager;
import es.gob.afirma.keystores.AOKeyStoreManagerException;
import es.gob.afirma.keystores.AOKeyStoreManagerFactory;
import es.gob.afirma.keystores.AOKeystoreAlternativeException;
import es.gob.afirma.keystores.AggregatedKeyStoreManager;
import es.gob.afirma.keystores.AutoCloseableStore;
import es.gob.afirma.keystores.KeyStoreMessages;
import es.gob.afirma.keystores.KeyStoreUtilities;

/** Almac&eacute;n TEMD en tarjeta.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class TemdKeyStoreManager extends AggregatedKeyStoreManager implements AutoCloseableStore {

    /** Tiempo (en segundos) que se mantiene abierto el almacen de la TEMD. */
    private static final int DEFAULT_TIME_TO_CLOSE_KEYSTORE = 24 * 3600; // 1 dia

	static Logger getLogger() {
		return LOGGER;
	}

	private TimedPersistentCachePasswordCallback pwc = new TimedPersistentCachePasswordCallback(
			KeyStoreMessages.getString("TemdKeyStoreManager.0"), //$NON-NLS-1$
			DEFAULT_TIME_TO_CLOSE_KEYSTORE,
			null);

	/** Construye un almac&eacute;n TEMD en tarjeta.
	 * @throws AOKeyStoreManagerException Si no puede construirse el almac&eacute;n. */
	public TemdKeyStoreManager() throws AOKeyStoreManagerException {
		System.setProperty("es.gob.afirma.keystores.DoNotReusePkcs11Provider", Boolean.TRUE.toString()); //$NON-NLS-1$
		setKeyStoreType(AOKeyStore.TEMD);
		addKeyStoreManager(getTemdPkcs11KeyStoreManager());
	}

	/**
	 *
	 * @throws AOKeyStoreManagerException
	 */
	public TemdKeyStoreManager(final TimedPersistentCachePasswordCallback pwc) throws AOKeyStoreManagerException {
		this.pwc = pwc;
		System.setProperty("es.gob.afirma.keystores.DoNotReusePkcs11Provider", Boolean.TRUE.toString()); //$NON-NLS-1$
		setKeyStoreType(AOKeyStore.TEMD);
		addKeyStoreManager(getTemdPkcs11KeyStoreManager());
	}


	@Override
	public boolean isKeyEntry(final String alias) throws KeyStoreException {
		if (lacksKeyStores()) {
			try {
				refresh();
			}
			catch (final IOException e) {
				throw new KeyStoreException(e);
			}
		}
		return super.isKeyEntry(alias);
	}

	@Override
	public String[] getAliases() {
		resetTimer();
		if (lacksKeyStores()) {
			try {
				refresh();
			}
			catch (final IOException e) {
				LOGGER.severe(
					"No ha podido recargarse el almacen: " + e //$NON-NLS-1$
				);
			}
			return new String[0];
		}
		return super.getAliases();
	}

    @Override
	public X509Certificate getCertificate(final String alias) {
    	resetTimer();
		if (lacksKeyStores()) {
			try {
				refresh();
			}
			catch (final IOException e) {
				LOGGER.severe(
					"No ha podido recargarse el almacen: " + e //$NON-NLS-1$
				);
			}
			return null;
		}
    	return super.getCertificate(alias);
    }

    @Override
	public X509Certificate[] getCertificateChain(final String alias) {
    	resetTimer();
		if (lacksKeyStores()) {
			try {
				refresh();
			}
			catch (final IOException e) {
				LOGGER.severe(
					"No ha podido recargarse el almacen: " + e //$NON-NLS-1$
				);
			}
			return new X509Certificate[0];
		}
    	return super.getCertificateChain(alias);
    }

    @Override
	public KeyStore.PrivateKeyEntry getKeyEntry(final String alias) throws KeyStoreException,
                                                                     NoSuchAlgorithmException,
                                                                     UnrecoverableEntryException {
    	resetTimer();
		if (lacksKeyStores()) {
			try {
				refresh();
			}
			catch (final IOException e) {
				LOGGER.severe(
					"No ha podido recargarse el almacen: " + e //$NON-NLS-1$
				);
			}
			return null;
		}
    	return super.getKeyEntry(alias);
    }

	@Override
	public void refresh() throws IOException {
		removeAll();
		try {
			addKeyStoreManager(getTemdPkcs11KeyStoreManager());
		}
		catch (final AOKeyStoreManagerException e) {
			throw new IOException(e);
		}
		resetTimer();
	}

	enum TEMD_CARD {

		MMAR(
			"PKCS11_TEMD", //$NON-NLS-1$
			new Atr(
				new byte[] {
			        (byte) 0x3B, (byte) 0x7F, (byte) 0x94, (byte) 0x00, (byte) 0x00, (byte) 0x80, (byte) 0x31, (byte) 0x80, (byte) 0x71, (byte) 0x90,
			        (byte) 0x67, (byte) 0x54, (byte) 0x45, (byte) 0x4D, (byte) 0x44, (byte) 0x31, (byte) 0x2E, (byte) 0x30, (byte) 0x90, (byte) 0x00
			    },
				new byte[] {
			        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
			        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
			    }
			),
			new byte[] {
		        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
		        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
		    },
			"Tarjeta del Ministerio de Defensa (MMAR)" //$NON-NLS-1$
		),
		FNMT(
			getFNMTLibrary(),
			new Atr(
				new byte[] {
					(byte)0x3B, (byte)0x7F, (byte)0x96, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x6A, (byte)0x46, (byte)0x4E, (byte)0x4D,
					(byte)0x54, (byte)0x03, (byte)0x04, (byte)0x11, (byte)0x43, (byte)0x04, (byte)0x30, (byte)0x03, (byte)0x90, (byte)0x00
				},
				new byte[] {
			        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
			        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
			    }
			),
			new byte[] {
		        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
		        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
		    },
			"Tarjeta del Ministerio de Defensa (FNMT)" //$NON-NLS-1$
		);

		private final String lib;
		private final Atr atr;
		private final String description;
		private final byte[] atrMask;

		TEMD_CARD(final String libName, final Atr answerToReset, final byte[] answerToResetMask, final String desc) {
			this.lib = libName;
			this.atr = answerToReset;
			this.description = desc;
			this.atrMask = answerToResetMask;
		}

		boolean itsMe(final byte[] atrBytes) {
			return this.atr.equals(
				new Atr(
					atrBytes,
					this.atrMask
				)
			);
		}

		static TEMD_CARD getTemdCard(final byte[] atr) {
			if (atr == null) {
				return null;
			}
			if (MMAR.itsMe(atr)) {
				return MMAR;
			}
			else if (FNMT.itsMe(atr)) {
				return FNMT;
			}
			return null;
		}

		@Override
		public String toString() {
			return this.description;
		}

		String getLibName() {
			return this.lib;
		}

		String getLibPath() {
			return KeyStoreUtilities.searchPathForFile(
        		new String[] {
    				System.mapLibraryName(this.lib),
        		}
    		);
		}

		boolean libLoads() {
			try {
				System.loadLibrary(this.lib);
			}
			catch(final Exception e) {
				getLogger().warning(
					"No se ha podido cargar la biblioteca PKCS#11 '" + this.lib + "': "  + e //$NON-NLS-1$ //$NON-NLS-2$
				);
				return false;
			}
			catch(final Error e) {
				getLogger().warning(
					"No se ha podido cargar la biblioteca PKCS#11 '" + this.lib + "': "  + e //$NON-NLS-1$ //$NON-NLS-2$
				);
				return false;
			}
			return true;
		}
	}

	private void resetTimer() {
		this.pwc.resetTimer();
	}

	/** Obtiene la versi&oacute;n de Java para determinar el nombre de la librer&iacute;a de las tarjetas de FNMT.
	 * @return Nombre de la librer&iacute;a para las tarjetas de FNMT.
	 */
	public static String getFNMTLibrary() {
		final String javaArch = Platform.getJavaArch();
		if ("32".equals(javaArch)) { //$NON-NLS-1$
			return "FNMT_P11"; //$NON-NLS-1$
		}
		return "FNMT_P11_x64"; //$NON-NLS-1$
	}

	@Override
	public void closeIn(final int seconds) {
		if (seconds < 1) {
			throw new IllegalArgumentException(
				"El numero de segundos debe ser mayor que cero, y se ha especificado: " + seconds //$NON-NLS-1$
			);
		}
		this.pwc.setSecondsToClose(seconds);
	}

	@Override
	public void close() {
		this.pwc.clearPassword();
	}

	/**
	 * Cierra la instancia unica que puede existir de este almac&eacute;n.
	 */
	public static void closeKeyStore() {
		TimedPersistentCachePasswordCallback.clear();
	}

	@Override
	public boolean isOpen() {
		return !this.pwc.isObjectExpired();
	}

    private static TEMD_CARD getInsertedTemd() {

    	List<CardTerminal> terminales;
		try {
			terminales = TerminalFactory.getDefault().terminals().list();
		}
		catch (final CardException e) {
			LOGGER.warning("No se ha podido obtener la lista de lectores del sistema: " + e); //$NON-NLS-1$
			return null;
		}

    	for (final CardTerminal cardTerminal : terminales) {
			try {
				if (cardTerminal.isCardPresent()) {
					final Card card = cardTerminal.connect("*"); //$NON-NLS-1$
					final byte[] currentAtr = card.getATR().getBytes();
					if (TEMD_CARD.FNMT.itsMe(currentAtr)) {
						card.disconnect(true);
						return TEMD_CARD.FNMT;
					}
					else if(TEMD_CARD.MMAR.itsMe(currentAtr)) {
						card.disconnect(true);
						return TEMD_CARD.MMAR;
					}

				}
			}
			catch (final CardException e) {
				LOGGER.warning(
					"Error comprobando la presencia de una tarjeta: " + e //$NON-NLS-1$
				);
				continue;
			}
    	}

    	return null;
    }

    @Override
	public void setParentComponent(final Object p) {
		super.setParentComponent(p);
		this.pwc.setParent(p);
	}

	private AOKeyStoreManager getTemdPkcs11KeyStoreManager() throws AOKeyStoreManagerException {

		final TEMD_CARD card = getInsertedTemd();

		if (card == null) {
			throw new AOKeyStoreManagerException("No hay ninguna TEMD insertada"); //$NON-NLS-1$
		}

		if (!card.libLoads()) {
			throw new AOKeyStoreManagerException("No se puede cargar el PKCS#11 de la TEMD insertada"); //$NON-NLS-1$
		}

		try {
			return AOKeyStoreManagerFactory.getAOKeyStoreManager(
				AOKeyStore.PKCS11,
				card.getLibPath(),
				card.toString(),
				this.// Pedimos el PIN para almacenarlo (peticion expresa del Ministerio de Defensa pese a lo
				// inseguro de la practica).
				pwc,
				getParentComponent()
			);
		}
		catch (AOKeystoreAlternativeException | IOException e) {
			this.pwc.clearPassword();
			throw new AOKeyStoreManagerException(e);
		}
	}
}
