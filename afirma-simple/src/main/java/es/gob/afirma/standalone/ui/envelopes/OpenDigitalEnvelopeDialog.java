package es.gob.afirma.standalone.ui.envelopes;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListModel;

import es.gob.afirma.core.AOCancelledOperationException;
import es.gob.afirma.core.misc.Platform;
import es.gob.afirma.core.ui.AOUIFactory;
import es.gob.afirma.envelopers.cms.AOCMSEnveloper;
import es.gob.afirma.keystores.AOCertificatesNotFoundException;
import es.gob.afirma.keystores.AOKeyStore;
import es.gob.afirma.keystores.AOKeyStoreDialog;
import es.gob.afirma.keystores.AOKeyStoreManager;
import es.gob.afirma.keystores.AOKeyStoreManagerException;
import es.gob.afirma.keystores.AOKeyStoreManagerFactory;
import es.gob.afirma.keystores.AOKeystoreAlternativeException;
import es.gob.afirma.keystores.KeyStoreConfiguration;
import es.gob.afirma.standalone.AutoFirmaUtil;
import es.gob.afirma.standalone.SimpleAfirmaMessages;
import es.gob.afirma.standalone.ui.preferences.PreferencesManager;

/**
 * @author Mariano Mart&iacute;nez
 * Di&aacute;logo para abrir sobres digitales.
 */
public class OpenDigitalEnvelopeDialog extends JDialog implements KeyListener {

	private static final long serialVersionUID = -5949140119173965513L;
	static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$
	private static final int PREFERRED_WIDTH = 600;
	private static final int PREFERRED_HEIGHT = 200;

	private final JTextField selectedFilePath = new JTextField();
	void setSelectedFilePath(final String path) {
		this.selectedFilePath.setText(path);
	}

	private final JComboBox<KeyStoreConfiguration> comboBoxRecipients = new JComboBox<>();
	private final JTextField senderTextField = new JTextField();

	private final JButton examineFileButton = new JButton(SimpleAfirmaMessages.getString("OpenDigitalEnvelope.2")); //$NON-NLS-1$

	private final JButton openButton = new JButton(SimpleAfirmaMessages.getString("OpenDigitalEnvelope.3")); //$NON-NLS-1$
	private final JButton cancelButton = new JButton(SimpleAfirmaMessages.getString("OpenDigitalEnvelope.4")); //$NON-NLS-1$

	private boolean certificateDialogOpenned = false;

	/** Crea el di&aacute;logo y lo hace visible.
	 * @param parent Frame padre del di&aacute;logo.
	 * @param filePath Ruta hacia el sobre digital.
	 * @param sa Instancia de SimpleAfirma para utilizar el almac&eacute;n de la aplicaci&oacute;n.
	 */
	public static void startOpenDigitalEnvelopeDialog(final Frame parent,													  final String filePath) {
		final OpenDigitalEnvelopeDialog ode = new OpenDigitalEnvelopeDialog(parent, filePath);
		ode.setSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
		ode.setResizable(false);
		ode.setLocationRelativeTo(parent);
		ode.setVisible(true);
	}

	/** Crea el panel de apertura de un sobre digital.
	 * @param parent Componente padre del di&aacute;logo.
	 * @param filePath Ruta hacia el sobre digital.
	 * @param sa Instancia de SimpleAfirma para utilizar el almac&eacute;n de la aplicaci&oacute;n.
	 **/
	public OpenDigitalEnvelopeDialog(final Frame parent, final String filePath) {
		super(parent);

		if (filePath != null) {
			setSelectedFilePath(filePath);
		}
		createUI();
	}

	private void createUI() {

		setTitle(SimpleAfirmaMessages.getString("OpenDigitalEnvelope.0")); //$NON-NLS-1$

		getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("OpenDigitalEnvelope.2") //$NON-NLS-1$
		);

		// Icono de la ventana
		setIconImage(AutoFirmaUtil.getDefaultDialogsIcon());

		// Eleccion fichero a desensobrar
		final JLabel envelopeFilesLabel = new JLabel(
			SimpleAfirmaMessages.getString("OpenDigitalEnvelope.6") //$NON-NLS-1$
		);
		envelopeFilesLabel.setLabelFor(this.selectedFilePath);
		this.selectedFilePath.setEditable(false);
		this.selectedFilePath.setFocusable(false);

		// Boton de examinar
		this.examineFileButton.setMnemonic('X');
		this.examineFileButton.getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("OpenDigitalEnvelope.7") //$NON-NLS-1$
		);
		this.examineFileButton.addActionListener(
			new ActionListener() {
				/** {@inheritDoc} */
				@Override
				public void actionPerformed(final ActionEvent ae) {
					final File envFile;
					try {
						envFile = AOUIFactory.getLoadFiles(
							SimpleAfirmaMessages.getString("OpenDigitalEnvelope.11"), //$NON-NLS-1$
							null,
							null,
							new String[] {SimpleAfirmaMessages.getString("OpenDigitalEnvelope.8")}, //$NON-NLS-1$
							SimpleAfirmaMessages.getString("OpenDigitalEnvelope.8"), //$NON-NLS-1$
							false,
							false,
							null,
							OpenDigitalEnvelopeDialog.this
						)[0];
					}
					catch (final AOCancelledOperationException e) {
						LOGGER.warning(
							"Operacion cancelada por el usuario: " + e //$NON-NLS-1$
						);
						return;
					}
					if (!envFile.canRead()) {
						LOGGER.warning(
							"No ha podido cargarse el fichero para envolver: " //$NON-NLS-1$
						);
						AOUIFactory.showErrorMessage(
							OpenDigitalEnvelopeDialog.this,
							SimpleAfirmaMessages.getString("OpenDigitalEnvelope.12"), //$NON-NLS-1$
							SimpleAfirmaMessages.getString("OpenDigitalEnvelope.13"), //$NON-NLS-1$
							JOptionPane.ERROR_MESSAGE
						);
						return;
					}
					setSelectedFilePath(envFile.getAbsolutePath());
					enableOpenbutton();
				}
			}
		);
		this.examineFileButton.setEnabled(true);
		this.examineFileButton.addKeyListener(this);

		// Boton abrir
		this.openButton.setMnemonic('A');
		this.openButton.getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("OpenDigitalEnvelope.9") //$NON-NLS-1$
		);
		this.openButton.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent arg0) {
					if (open()) {
						setVisible(false);
						dispose();
					}
				}
			}
		);
		this.openButton.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(final KeyEvent ke) {
				OpenDigitalEnvelopeDialog.this.keyTyped(ke);
			}

			@Override
			public void keyReleased(final KeyEvent ke) {
				if (isCertificateDialogOpenned()) {
					setCertificateDialogOpenned(false);
				}
				else {
					OpenDigitalEnvelopeDialog.this.keyReleased(ke);
				}
			}

			@Override
			public void keyPressed(final KeyEvent ke) {
				OpenDigitalEnvelopeDialog.this.keyPressed(ke);
			}
		});

        // Eleccion del certificado
        final JLabel labelCombo = new JLabel(SimpleAfirmaMessages.getString("OpenDigitalEnvelope.24")); //$NON-NLS-1$
        labelCombo.setLabelFor(this.comboBoxRecipients);
        this.comboBoxRecipients.setModel(new DefaultComboBoxModel<>(EnvelopesUtils.getKeyStoresToUnwrap()));
 		this.comboBoxRecipients.addKeyListener(this);

		// Area de texto con el alias del certificado seleccionado
		final JLabel labelText = new JLabel(SimpleAfirmaMessages.getString("DigitalEnvelopeSender.18")); //$NON-NLS-1$
		labelText.setLabelFor(this.senderTextField);
        this.senderTextField.setFont(new java.awt.Font ("Century Schoolbook L", 0, 13)); //$NON-NLS-1$
        this.senderTextField.setFocusable(false);

		this.cancelButton.setMnemonic('C');
		this.cancelButton.getAccessibleContext().setAccessibleDescription(
 			SimpleAfirmaMessages.getString("OpenDigitalEnvelope.10") //$NON-NLS-1$
		);
		this.cancelButton.addActionListener(
			new ActionListener() {
				/** {@inheritDoc} */
				@Override
				public void actionPerformed(final ActionEvent ae) {
					setVisible(false);
					dispose();
				}
			}
		);
		this.cancelButton.addKeyListener(this);

		final JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		// En Mac OS X el orden de los botones es distinto
		if (Platform.OS.MACOSX.equals(Platform.getOS())) {
			panel.add(this.cancelButton);
			panel.add(this.openButton);
		}
		else {
			panel.add(this.openButton);
			panel.add(this.cancelButton);
		}

		setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 0;
		c.gridx = 0;
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(5, 11, 0, 11);
        add(envelopeFilesLabel, c);
        c.gridy++;
        c.gridwidth = 1;
        c.insets = new Insets(5, 11, 0, 0);
        add(this.selectedFilePath, c);
        c.gridx++;
        c.weightx = 0.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = new Insets(5, 11, 0, 11);
        add(this.examineFileButton, c);
        c.gridx = 0;
        c.gridy++;
        c.insets = new Insets(11, 11, 0, 11);
		add(labelCombo, c);
		c.gridy++;
		c.insets = new Insets(5, 10, 0, 11);
		c.weightx = 1.0;
		add(this.comboBoxRecipients, c);
		c.gridy++;
		c.insets = new Insets(11, 11, 0, 11);
        c.anchor = GridBagConstraints.PAGE_END;
        add(panel, c);

 		loadConfiguration();

        enableOpenbutton();
	}

	void enableOpenbutton() {
		if (!this.selectedFilePath.getText().trim().isEmpty()) {
			this.openButton.setEnabled(true);
		}
		else {
			this.openButton.setEnabled(false);
		}
	}

	/**
	 * Abre el sobre digital seleccionado si es posible.
	 * @return Devuelve <code>true</code> si se ha podido abrir el sobre correctamente, <code>false</code> en caso contrario.
	 */
	public boolean open() {

		saveConfiguration();

		final PrivateKeyEntry pke;
        try {
            pke = getPrivateKeyEntry();
        }
        catch (final UnrecoverableEntryException e) {
        	LOGGER.warning("Error de contrasena: " + e); //$NON-NLS-1$
            // Control de la excepcion generada al introducir mal la contrasena para el certificado
            AOUIFactory.showMessageDialog(
        		this,
        		SimpleAfirmaMessages.getString("OpenDigitalEnvelope.20"), //$NON-NLS-1$
        		SimpleAfirmaMessages.getString("OpenDigitalEnvelope.21"),  //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        catch (final AOCancelledOperationException e) {
        	LOGGER.info("Operacion cancelada por el usuario: " + e); //$NON-NLS-1$
        	return false;
        }
    	catch (final AOCertificatesNotFoundException e) {
        	LOGGER.warning("No se han encontrado certificados validos en el almacen: " + e); //$NON-NLS-1$
            AOUIFactory.showMessageDialog(
        		this,
        		SimpleAfirmaMessages.getString("OpenDigitalEnvelope.22"), //$NON-NLS-1$
        		SimpleAfirmaMessages.getString("OpenDigitalEnvelope.21"),  //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
        	return false;
        }
    	catch (final Exception e) {
        	LOGGER.log(Level.SEVERE, "Error recuperando la clave privada: " + e, e); //$NON-NLS-1$
            AOUIFactory.showMessageDialog(
        		this,
        		SimpleAfirmaMessages.getString("OpenDigitalEnvelope.23"), //$NON-NLS-1$
        		SimpleAfirmaMessages.getString("OpenDigitalEnvelope.21"),  //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
        	return false;
        }

		byte[] data = null;

        final AOCMSEnveloper enveloper = new AOCMSEnveloper();
        try {
			data = enveloper.recoverData(
				EnvelopesUtils.readFile(this.selectedFilePath.getText()),
				pke
			);
		}
        catch (final InvalidKeyException e1) {
			LOGGER.severe("La clave indicada no pertenece a ninguno de los destinatarios del envoltorio" + e1); //$NON-NLS-1$
        	AOUIFactory.showErrorMessage(
                this,
                SimpleAfirmaMessages.getString("OpenDigitalEnvelope.17"), //$NON-NLS-1$
                SimpleAfirmaMessages.getString("OpenDigitalEnvelope.15"), //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
        	return false;
		}
        catch (final Exception e1) {
			LOGGER.severe("Error desensobrando el fichero: " + e1); //$NON-NLS-1$
        	AOUIFactory.showErrorMessage(
                this,
                SimpleAfirmaMessages.getString("OpenDigitalEnvelope.18"), //$NON-NLS-1$
                SimpleAfirmaMessages.getString("OpenDigitalEnvelope.15"), //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
        	return false;
		}

		try {
			AOUIFactory.getSaveDataToFile(
			    data,
			    SimpleAfirmaMessages.getString("DigitalEnvelopeSender.32"), //$NON-NLS-1$
			    null,
			    new File(this.selectedFilePath.getText()).getName().split(".enveloped")[0], //$NON-NLS-1$
			    null,
			    null,
			    this
			);
		}
		catch (final IOException e) {
			LOGGER.severe("No se ha posido guardar el sobre: " + e); //$NON-NLS-1$
			AOUIFactory.showMessageDialog(
        		this,
        		SimpleAfirmaMessages.getString("OpenDigitalEnvelope.19"), //$NON-NLS-1$
        		SimpleAfirmaMessages.getString("OpenDigitalEnvelope.15"), //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
		}
		catch (final AOCancelledOperationException e) {
			LOGGER.info("Operacion de almacenamiento del fichero descifrado cancelada por el usuario: " + e); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	/**
	 * Recupera la entrada de un certificado seleccionado por el usuario para la apertura del sobre.
	 * @return Entrada con el certificado y la referencia a su clave privada.
	 * @throws AOCertificatesNotFoundException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws UnrecoverableEntryException
	 * @throws AOKeyStoreManagerException
	 * @throws AOKeystoreAlternativeException
	 * @throws IOException
	 */
	private PrivateKeyEntry getPrivateKeyEntry() throws AOCertificatesNotFoundException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, AOKeyStoreManagerException, AOKeystoreAlternativeException, IOException {

		setCertificateDialogOpenned(true);

    	String[] filter;
    	final AOKeyStoreManager keyStoreManager;
    	final KeyStoreConfiguration kc = (KeyStoreConfiguration) this.comboBoxRecipients.getSelectedItem();
    	final AOKeyStore ao = kc.getType();
    	String lib = null;
    	if (ao == AOKeyStore.PKCS12 || ao == AOKeyStore.SINGLE) {
    		if (ao == AOKeyStore.PKCS12) {
    			filter = new String[] {"p12", "pfx"};  //$NON-NLS-1$//$NON-NLS-2$
    		}
    		else {
    			filter = new String[] { "cer", "p7b", "p7s"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    		}
    		final File keystorePath = EnvelopesUtils.addFileSelected(filter, this.comboBoxRecipients, this);
    		if (keystorePath == null) {
    			throw new AOCancelledOperationException();
    		}
    		lib = keystorePath.getAbsolutePath();
    	}
    	else if (ao == AOKeyStore.PKCS11) {
    		filter = new String[] {"dll", "so"};  //$NON-NLS-1$//$NON-NLS-2$
    		final File keystorePath = EnvelopesUtils.addFileSelected(filter, this.comboBoxRecipients, this);
    		if (keystorePath == null) {
    			throw new AOCancelledOperationException();
    		}
    		lib = keystorePath.getAbsolutePath();
    	}

    	try {
    	keyStoreManager = AOKeyStoreManagerFactory.getAOKeyStoreManager(
    			ao,
    			lib,
    			"default", //$NON-NLS-1$
    			ao.getStorePasswordCallback(this),
    			this
    			);
    	}
    	catch (final IOException e) {
    		throw new UnrecoverableEntryException("No se pudo abrir el almacen: " + e); //$NON-NLS-1$
    	}

    	final AOKeyStoreDialog dialog = new AOKeyStoreDialog(
			keyStoreManager,
			this,
			true,             // Comprobar claves privadas
			false,            // Mostrar certificados caducados
			true,             // Comprobar validez temporal del certificado
			null, 				// Filtros
			false             // mandatoryCertificate
		);
    	dialog.show();

    	keyStoreManager.setParentComponent(this);
    	return keyStoreManager.getKeyEntry(
			dialog.getSelectedAlias()
		);
	}

	/** {@inheritDoc} */
	@Override
	public void keyTyped(final KeyEvent e) { /* Vacio */ }

	/** {@inheritDoc} */
	@Override
	public void keyPressed(final KeyEvent e) { /* Vacio */ }

	/** {@inheritDoc} */
	@Override
	public void keyReleased(final KeyEvent ke) {
		// En Mac no cerramos los dialogos con Escape
		if (ke != null && ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
			this.setVisible(false);
			dispose();
		}
	}

	public boolean isCertificateDialogOpenned() {
		return this.certificateDialogOpenned;
	}

	public void setCertificateDialogOpenned(final boolean certificateDialogOpenned) {
		this.certificateDialogOpenned = certificateDialogOpenned;
	}

	/**
	 * Guarda el tipo de almac&eacute;n configurado para futuros usos.
	 */
	private void saveConfiguration() {

		final AOKeyStore ks = ((KeyStoreConfiguration) this.comboBoxRecipients.getSelectedItem()).getType();
		PreferencesManager.put(PreferencesManager.PREFERENCE_ENVELOP_PRIVATE_KEYSTORE, ks.getName());
	}

	/**
	 * Carga el tipo de almac&eacute;n previamente configurado.
	 */
	private void loadConfiguration() {

		final String ksName = PreferencesManager.get(PreferencesManager.PREFERENCE_ENVELOP_PRIVATE_KEYSTORE, null);

		if (ksName != null) {
			final int itemIndex = -1;
			final ListModel<KeyStoreConfiguration> model = this.comboBoxRecipients.getModel();
			for (int i = 0; i < model.getSize() && itemIndex < 0; i++) {
				if (ksName.equals(model.getElementAt(i).getType().getName())) {
					this.comboBoxRecipients.setSelectedIndex(i);
					break;
				}
			}
		}
	}

	public static void main(final String[] args) {
		OpenDigitalEnvelopeDialog.startOpenDigitalEnvelopeDialog(null, null);
	}
}