package es.gob.afirma.standalone.trayicon;

import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_KEYSTORE_DEFAULT_STORE;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_KEYSTORE_PRIORITARY_STORE;

import java.awt.AWTException;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import es.gob.afirma.core.misc.Platform;
import es.gob.afirma.core.ui.AOUIFactory;
import es.gob.afirma.keystores.AOKeyStore;
import es.gob.afirma.keystores.AOKeyStoreDialog;
import es.gob.afirma.keystores.AOKeyStoreManager;
import es.gob.afirma.keystores.AOKeyStoreManagerFactory;
import es.gob.afirma.keystores.temd.TemdKeyStoreManager;
import es.gob.afirma.standalone.AutoFirmaUtil;
import es.gob.afirma.standalone.LookAndFeelManager;
import es.gob.afirma.standalone.SimpleAfirma;
import es.gob.afirma.standalone.SimpleKeyStoreManager;
import es.gob.afirma.standalone.ui.MainMenu;
import es.gob.afirma.standalone.ui.preferences.PreferencesDialog;
import es.gob.afirma.standalone.ui.preferences.PreferencesManager;
import es.gob.afirma.ui.core.jse.certificateselection.CertificateSelectionDialog;

/** Clase que genera el TrayIcon de la configuraci&oacute;n de AutoFirma. */
public final class AutoFirmaTrayIcon {

	static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	private static final String AUTOFIRMA_APPLICATION_FILENAME = "Autofirma.exe"; //$NON-NLS-1$

	/** Directorio de datos de la aplicaci&oacute;n. */
	public static final String APPLICATION_HOME = Platform.getUserHome() + File.separator + ".afirma" + File.separator + "AutoFirmaTray"; //$NON-NLS-1$ //$NON-NLS-2$

	private static final Image ICON = Toolkit.getDefaultToolkit().getImage(
		AutoFirmaTrayIcon.class.getResource("/logo_cliente_128.png") //$NON-NLS-1$
	);

	private static final Image ICON_SMALL = Toolkit.getDefaultToolkit().getImage(
		AutoFirmaTrayIcon.class.getResource("/logo_cliente_16.png") //$NON-NLS-1$
	);

	private static final String VERSION = "AutoFirma "+ SimpleAfirma.getVersion(); //$NON-NLS-1$
	final static Properties properties = new Properties();

	static final AOKeyStore[] DEFAULT_STORES;
	static {
		final List<AOKeyStore> stores = new ArrayList<>();
		final Platform.OS os = Platform.getOS();
		if (Platform.OS.WINDOWS.equals(os)) {
			stores.add(AOKeyStore.WINDOWS);
		}
		else if (Platform.OS.MACOSX.equals(os)) {
			stores.add(AOKeyStore.APPLE);
		}
		else {
			stores.add(AOKeyStore.SHARED_NSS);
		}
		if (SimpleKeyStoreManager.isFirefoxAvailable()) {
			stores.add(AOKeyStore.MOZ_UNI);
		}
		DEFAULT_STORES = stores.toArray(new AOKeyStore[0]);
	}

	static final String[] PRIORITY_STORES = {
			AOKeyStore.TEMD.toString(),
			AOKeyStore.DNIEJAVA.toString(),
			AOKeyStore.CERES.toString(),
			TrayIconMessages.getString("AutoFirmaTrayIcon.3") // Ninguno //$NON-NLS-1$
	};

	static final Frame HIDDEN_FRAME = new JFrame();
	static {
		HIDDEN_FRAME.setIconImage(ICON);
		HIDDEN_FRAME.setLayout(null);
	}

	/** Muestra el panel de "acerca de AutoFirma". */
	static void aboutAutofirma(){
		MainMenu.showAbout(null);
	}

	/** Genera el PopupMenu del TrayIcon.
	 * @return Men&uacute; del TrayIcon. */
	private static PopupMenu createMenu(){
		final PopupMenu menu = new PopupMenu();
		menu.getAccessibleContext().setAccessibleDescription(TrayIconMessages.getString("TrayIcon.0")); //$NON-NLS-1$

		// create menu item for the default action
		final MenuItem messageItem = new MenuItem(TrayIconMessages.getString("TrayIcon.5")); //$NON-NLS-1$
		messageItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				 PreferencesDialog.show(HIDDEN_FRAME, true);
			}
		});

		messageItem.getAccessibleContext().setAccessibleDescription(TrayIconMessages.getString("TrayIcon.1")); //$NON-NLS-1$

		final MenuItem closeItem = new MenuItem(TrayIconMessages.getString("TrayIcon.6")); //$NON-NLS-1$
		closeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				System.exit(0);
			}
		});
		closeItem.getAccessibleContext().setAccessibleDescription(TrayIconMessages.getString("TrayIcon.2")); //$NON-NLS-1$


		final MenuItem about = new MenuItem(TrayIconMessages.getString("TrayIcon.7")); //$NON-NLS-1$
		about.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				aboutAutofirma();
			}
		});
		about.getAccessibleContext().setAccessibleDescription(TrayIconMessages.getString("TrayIcon.3")); //$NON-NLS-1$

		final Menu displayMenu = new Menu(TrayIconMessages.getString("TrayIcon.9")); //$NON-NLS-1$

		final MenuItem selectPriorityKeyStore = new MenuItem(TrayIconMessages.getString("TrayIcon.10")); //$NON-NLS-1$
		selectPriorityKeyStore.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {

				final String keyStore = (String) AOUIFactory.showInputDialog(
					HIDDEN_FRAME,
    				TrayIconMessages.getString("TrayIcon.22"), //$NON-NLS-1$
    				TrayIconMessages.getString("TrayIcon.21"), //$NON-NLS-1$
    				JOptionPane.QUESTION_MESSAGE,
    				AutoFirmaUtil.getDefaultDialogsIcon(),
    				PRIORITY_STORES,
    				SimpleKeyStoreManager.getDefaultKeyStoreType()
    			);
				if (keyStore != null && !keyStore.trim().isEmpty()) {
					PreferencesManager.put(
						PREFERENCE_KEYSTORE_PRIORITARY_STORE,
						keyStore
					);
				}
			}
		});
		selectPriorityKeyStore.getAccessibleContext().setAccessibleDescription(TrayIconMessages.getString("TrayIcon.11")); //$NON-NLS-1$

		final MenuItem selecDefaultKeyStore = new MenuItem(TrayIconMessages.getString("TrayIcon.16")); //$NON-NLS-1$
		selecDefaultKeyStore.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {

					final AOKeyStore keyStore = (AOKeyStore) AOUIFactory.showInputDialog(
						HIDDEN_FRAME,
	    				null,
	    				TrayIconMessages.getString("TrayIcon.20"), //$NON-NLS-1$
	    				JOptionPane.QUESTION_MESSAGE,
	    				AutoFirmaUtil.getDefaultDialogsIcon(),
	    				DEFAULT_STORES,
	    				SimpleKeyStoreManager.getDefaultKeyStoreType()
	    			);
					if (keyStore != null) {
						PreferencesManager.put(
							PREFERENCE_KEYSTORE_DEFAULT_STORE,
							keyStore.toString()
						);
					}
				}
			}
		);
		selecDefaultKeyStore.getAccessibleContext().setAccessibleDescription(TrayIconMessages.getString("TrayIcon.17")); //$NON-NLS-1$

		final MenuItem showCertificates = new MenuItem(TrayIconMessages.getString("TrayIcon.12")); //$NON-NLS-1$
		showCertificates.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final AOKeyStoreManager ksm;
				try {
					ksm = AOKeyStoreManagerFactory.getAOKeyStoreManager(
							SimpleKeyStoreManager.getDefaultKeyStoreType(),
						null,
						"default", //$NON-NLS-1$
						SimpleKeyStoreManager.getDefaultKeyStoreType().getStorePasswordCallback(this),
						this
					);

					final CertificateSelectionDialog csd = new CertificateSelectionDialog(
						HIDDEN_FRAME,
						new AOKeyStoreDialog(
							ksm,
							this,
							true,
							true,
							false
						),
						TrayIconMessages.getString(
							"AutoFirmaTrayIcon.0", //$NON-NLS-1$
							SimpleKeyStoreManager.getDefaultKeyStoreType().toString()
						),
						TrayIconMessages.getString(
							"AutoFirmaTrayIcon.2", //$NON-NLS-1$
							SimpleKeyStoreManager.getDefaultKeyStoreType().toString()
						),
						false,
						true
					);
					csd.showDialog();
				}
				catch (final Exception e1) {
					AOUIFactory.showErrorMessage(
						this,
						TrayIconMessages.getString("AutoFirmaTrayIcon.1"), //$NON-NLS-1$
						TrayIconMessages.getString("AutoFirmaTrayIcon.0", SimpleKeyStoreManager.getDefaultKeyStoreType().toString()), //$NON-NLS-1$
						JOptionPane.ERROR_MESSAGE
					);
					Logger.getLogger("es.gob.afirma").warning("Error al recuperar el almacen por defecto seleccionado: " + e1); //$NON-NLS-1$ //$NON-NLS-2$
				}

			}
		});
		showCertificates.getAccessibleContext().setAccessibleDescription(TrayIconMessages.getString("TrayIcon.13")); //$NON-NLS-1$

		// Menu para el cierre del almacen en tarjeta
		final MenuItem closeKeyStoreMenuItem = new MenuItem(TrayIconMessages.getString("AutoFirmaTrayIcon.7")); //$NON-NLS-1$
		closeKeyStoreMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				TemdKeyStoreManager.closeKeyStore();
			}
		});
		closeKeyStoreMenuItem.getAccessibleContext().setAccessibleDescription(TrayIconMessages.getString("AutoFirmaTrayIcon.9")); //$NON-NLS-1$

		// add menu items to menu
		displayMenu.add(selectPriorityKeyStore);
		displayMenu.add(selecDefaultKeyStore);
		displayMenu.add(showCertificates);
		displayMenu.add(closeKeyStoreMenuItem);

		menu.add(displayMenu);
		menu.addSeparator();
		menu.add(messageItem);
		menu.addSeparator();
		menu.add(about);
		menu.addSeparator();
		menu.add(closeItem);
		return menu;
	}

	/** Crea el TrayIcon y lo ejecuta. */
	private static void initialize (){
		LookAndFeelManager.applyLookAndFeel();

		if (!SystemTray.isSupported()) {
			LOGGER.severe("SystemTray is not supported"); //$NON-NLS-1$
			throw new IllegalStateException("SystemTray is not supported"); //$NON-NLS-1$
		}

		final PopupMenu menu = createMenu();

		final SystemTray systemTray = SystemTray.getSystemTray();

		final TrayIcon icon = new TrayIcon(ICON_SMALL, VERSION, menu);

		icon.setImageAutoSize(true);

		// Si se se hace clic sobre el TrayIcon, se muestra la pantalla
		// acerca de. Si se hace doble clic, se abre la aplicacion.
		icon.addMouseListener(new ClickListener() {

			@Override
			public void doubleClick(final MouseEvent e) {

				// Ejecutamos AutoFirma

				final File appDir;
				try {
					appDir = new File(AutoFirmaTrayIcon.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile();
				}
				catch (final Exception ex) {
					LOGGER.severe("Error al identificar el directorio de la aplicacion: " + e); //$NON-NLS-1$
					AOUIFactory.showMessageDialog(
							null,
							TrayIconMessages.getString("AutoFirmaTrayIcon.4"), //$NON-NLS-1$
							TrayIconMessages.getString("AutoFirmaTrayIcon.5"), //$NON-NLS-1$
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				final File appFile = new File(appDir, AUTOFIRMA_APPLICATION_FILENAME);
				if (!appFile.exists() || !appFile.isFile() || !appFile.canExecute()) {
					LOGGER.severe("No se encuentra o no se tienen permisos de ejecucion sobre AutoFirma: " + e); //$NON-NLS-1$
					AOUIFactory.showMessageDialog(
							null,
							TrayIconMessages.getString("AutoFirmaTrayIcon.6"), //$NON-NLS-1$
							TrayIconMessages.getString("AutoFirmaTrayIcon.5"), //$NON-NLS-1$
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				try {
					new ProcessBuilder(appFile.getAbsolutePath()).start();
				} catch (final IOException e1) {
					LOGGER.severe("Error al ejecutar AutoFirma desde el TrayIcon: " + e); //$NON-NLS-1$
					AOUIFactory.showMessageDialog(
							null,
							TrayIconMessages.getString("AutoFirmaTrayIcon.8"), //$NON-NLS-1$
							TrayIconMessages.getString("AutoFirmaTrayIcon.5"), //$NON-NLS-1$
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		try {
			systemTray.add(icon);
		}
		catch (final AWTException e) {
			LOGGER.severe("Ha ocurrido un error al ejecutar el TrayIcon: " + e); //$NON-NLS-1$
		}

	}

	@SuppressWarnings("resource")
	private static boolean isAfirmaTrayIconAlreadyRunning() {
    	final File appDir = new File(APPLICATION_HOME);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        try {
            final File file = new File(APPLICATION_HOME + File.separator + ".lock"); //$NON-NLS-1$
            final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw"); //$NON-NLS-1$
            final FileLock fileLock = randomAccessFile.getChannel().tryLock();
            if (fileLock != null) {
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
					public void run() {
                        try {
                            fileLock.release();
                            randomAccessFile.close();
                            file.delete();
                        }
                        catch (final Exception e) {
                            LOGGER.warning("No se ha podido eliminar el bloqueo de instancia: " + e); //$NON-NLS-1$
                        }
                    }
                });
                return false;
            }
            return true;
        }
        catch (final Exception e) {
        	LOGGER.warning("No se ha podido comprobar el bloqueo de instancia: " + e); //$NON-NLS-1$
            return false;
        }
    }

	/** Lanza la ejecuci&oacute;n de las opciones de configuraci&oacute;n de AutoFirma en el &aacute;rea de notificaciones.
	 * @param args No se usa. */
	public static void main(final String[] args) {
		if (isAfirmaTrayIconAlreadyRunning()) {
			System.exit(0);
		}
		else {
			initialize();
		}
	}
}