package es.gob.afirma.standalone.ui.cipher;

import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_CIPHER_ALGORITHM;

import java.awt.Container;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import es.gob.afirma.core.AOCancelledOperationException;
import es.gob.afirma.core.ciphers.CipherConstants;
import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.misc.Platform;
import es.gob.afirma.core.ui.AOUIFactory;
import es.gob.afirma.standalone.AutoFirmaUtil;
import es.gob.afirma.standalone.SimpleAfirmaMessages;
import es.gob.afirma.standalone.ui.preferences.PreferencesManager;

/** Di&aacute;logo para el cifrado de ficheros.
 * @author Mariano Mart&iacute;nez. */
public final class CipherDialog extends JDialog implements KeyListener{

	private static final long serialVersionUID = -9133887916481572642L;
	static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	private static final String[] CIPHER_ALGOS = new String[] {
		CipherConstants.AOCipherAlgorithm.PBEWITHSHA1ANDDESEDE.getName(),
        CipherConstants.AOCipherAlgorithm.PBEWITHSHA1ANDRC2_40.getName(),
        CipherConstants.AOCipherAlgorithm.PBEWITHMD5ANDDES.getName()
	};

	private final JComboBox<String> cipherAlgorithms = new JComboBox<>(CIPHER_ALGOS);
	String getSelectedCipherAlgorithm() {
		return this.cipherAlgorithms.getSelectedItem().toString();
	}

	private final JButton cipherButton = new JButton(
			SimpleAfirmaMessages.getString("CipherDialog.5") //$NON-NLS-1$
	);

	JButton getCipherButton() {
		return this.cipherButton;
	}

	/** Clave de cifrado */
    private Key cipherKey;

	private final JTextField textFieldData = new JTextField();

	private final JPasswordField passwordField = new JPasswordField(14);
	private char[] password;

	JPasswordField getPasswordField() {
		return this.passwordField;
	}

	void setPassword(final char[] text) {
		this.password = text;
	}

	char[] getPassword() {
		return this.password;
	}

	void setTextFieldDataText(final String text) {
		this.textFieldData.setText(text);
	}
	String getTextFieldDataText() {
		return this.textFieldData.getText();
	}

	/** Inicia el proceso de cifrado del fichero.
	 * @param parent Componente padre para la modalidad. */
	public static void startCipher(final Frame parent) {
		final CipherDialog cd = new CipherDialog(parent);
		cd.setSize(600, 320);
		cd.setResizable(false);
		cd.setLocationRelativeTo(parent);
		cd.setVisible(true);
	}

	/** Crea un di&aacute;logo para el cifrado de ficheros.
	 * @param parent Componente padre para la modalidad. */
	private CipherDialog(final Frame parent) {
		super(parent);
		setTitle(SimpleAfirmaMessages.getString("CipherDialog.0")); //$NON-NLS-1$
		setModalityType(ModalityType.APPLICATION_MODAL);
		createUI();
	}

	void createUI() {

		final Container c = getContentPane();
		final GridBagLayout gbl = new GridBagLayout();
		c.setLayout(gbl);
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10,15,0,10);
        setIconImage(
			AutoFirmaUtil.getDefaultDialogsIcon()
		);
        getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("CipherDialog.1") //$NON-NLS-1$
		);

		final JLabel cipherAlgorithmsLabels = new JLabel(
				SimpleAfirmaMessages.getString("CipherDialog.2") //$NON-NLS-1$
		);
		cipherAlgorithmsLabels.addKeyListener(this);
		cipherAlgorithmsLabels.setLabelFor(this.cipherAlgorithms);

		this.cipherAlgorithms.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					PreferencesManager.put(
							PREFERENCE_CIPHER_ALGORITHM,
						getSelectedCipherAlgorithm()
					);
				}
			}
		);
		this.cipherAlgorithms.setSelectedItem(
			PreferencesManager.get(
					PREFERENCE_CIPHER_ALGORITHM,
					getSelectedCipherAlgorithm()
			)
		);
		this.cipherAlgorithms.addKeyListener(this);

		final JLabel fileTextFieldLabel = new JLabel(
				SimpleAfirmaMessages.getString("CipherDialog.3") //$NON-NLS-1$
		);
		fileTextFieldLabel.addKeyListener(this);
		fileTextFieldLabel.setLabelFor(this.textFieldData);
		this.textFieldData.addKeyListener(this);
		this.textFieldData.setEditable(false);
		this.textFieldData.setFocusable(false);
		this.textFieldData.setColumns(10);

		final JButton textFieldDataButton =  new JButton(SimpleAfirmaMessages.getString("CheckHashDialog.12")); //$NON-NLS-1$
		textFieldDataButton.addKeyListener(this);
		textFieldDataButton.setMnemonic('E');
		textFieldDataButton.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent ae) {
					try {
						setTextFieldDataText(
							AOUIFactory.getLoadFiles(
								SimpleAfirmaMessages.getString("CipherDialog.6"), //$NON-NLS-1$
								null,
								null,
								null,
								SimpleAfirmaMessages.getString("CipherDialog.11"), //$NON-NLS-1$
								false,
								false,
								AutoFirmaUtil.getDefaultDialogsIcon(),
								CipherDialog.this
							)[0].getAbsolutePath()
						);
						final String cipherFile = getTextFieldDataText();
						setPassword(getPasswordField().getPassword());
						final String pass = String.valueOf(getPassword());
						if (!(cipherFile == null) && !cipherFile.trim().isEmpty() && pass != null && !pass.trim().isEmpty()) {
							getCipherButton().setEnabled(true);
							getCipherButton().requestFocus();
						}
					}
					catch(final AOCancelledOperationException ex) {
						// Operacion cancelada por el usuario
					}
				}
			}
		);
		textFieldDataButton.getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("CipherDialog.7") //$NON-NLS-1$
		);
		textFieldDataButton.addKeyListener(this);

		final JLabel jlabelPassword = new JLabel(SimpleAfirmaMessages.getString("CipherDialog.8")); //$NON-NLS-1$
		jlabelPassword.addKeyListener(this);
		jlabelPassword.setLabelFor(this.passwordField);
		this.passwordField.setEchoChar('*');
		this.passwordField.addKeyListener(this);
		this.passwordField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final String cipherFile = getTextFieldDataText();
				setPassword(getPasswordField().getPassword());
				final String pass = String.valueOf(getPassword());
				if (!(cipherFile == null) && !cipherFile.trim().isEmpty() && pass != null && !pass.trim().isEmpty()) {
					getCipherButton().setEnabled(true);
					getCipherButton().requestFocus();
				}
			}
		});

		this.cipherButton.addKeyListener(this);
		this.cipherButton.setEnabled(false);
		this.cipherButton.setMnemonic('C');
		this.cipherButton.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					setPassword(getPasswordField().getPassword());
					final String pass = String.valueOf(getPassword());
					if (pass != null && !pass.trim().isEmpty()){
						if (cipherFile()) {
							CipherDialog.this.setVisible(false);
							CipherDialog.this.dispose();
						}
					}
				}
			}
		);
		this.cipherButton.getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("CipherDialog.9") //$NON-NLS-1$
		);

		final JButton cancelButton = new JButton(
				SimpleAfirmaMessages.getString("CipherDialog.4") //$NON-NLS-1$
		);

		cancelButton.setMnemonic('A');
		cancelButton.addActionListener( new ActionListener () {
			@Override
			public void actionPerformed( final ActionEvent e ) {
				CipherDialog.this.setVisible(false);
				CipherDialog.this.dispose();
			}
		});
		cancelButton.getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("CipherDialog.10") //$NON-NLS-1$
		);
		cancelButton.addKeyListener(this);

		final JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		// En Mac OS X el orden de los botones es distinto
		if (Platform.OS.MACOSX.equals(Platform.getOS())) {
			panel.add(cancelButton);
			panel.add(this.cipherButton);
		}
		else {
			panel.add(this.cipherButton);
			panel.add(cancelButton);
		}

		c.add(fileTextFieldLabel, gbc);
		gbc.insets = new Insets(5,10,0,10);
		gbc.weightx = 1.0;
		gbc.gridy++;
		c.add(this.textFieldData, gbc);
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.LINE_END;
		c.add(textFieldDataButton, gbc);
		gbc.insets = new Insets(30,15,0,10);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy++;
		c.add(cipherAlgorithmsLabels, gbc);
		gbc.insets = new Insets(5,10,0,10);
		gbc.gridy++;
		c.add(this.cipherAlgorithms, gbc);
		gbc.insets = new Insets(30,15,0,10);
		gbc.gridy++;
		c.add(jlabelPassword, gbc);
		gbc.insets = new Insets(5,10,0,10);
		gbc.gridy++;
		c.add(this.passwordField, gbc);
		gbc.insets = new Insets(30,10,0,10);
		gbc.gridy++;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		c.add(panel, gbc);
		pack();
	}

	@Override
	public void keyTyped(final KeyEvent e) { /* Vacio */ }

	@Override
	public void keyPressed(final KeyEvent e) { /* Vacio */ }

	@Override
	public void keyReleased(final KeyEvent ke) {
		// En Mac no cerramos los dialogos con Escape
		if (ke != null && ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
			CipherDialog.this.setVisible(false);
			CipherDialog.this.dispose();
		}
		else{
			final String cipherFile = getTextFieldDataText();
			setPassword(getPasswordField().getPassword());
			final String pass = String.valueOf(getPassword());
			if (!(cipherFile == null) && !cipherFile.isEmpty() && pass != null && !pass.isEmpty()) {
				getCipherButton().setEnabled(true);
			}
		}
	}

	boolean cipherFile() {
		if (getTextFieldDataText() == null) {
            LOGGER.warning("No se ha indicado un fichero de datos"); //$NON-NLS-1$
            AOUIFactory.showMessageDialog(
        		this,
                SimpleAfirmaMessages.getString("CipherDialog.12"), //$NON-NLS-1$
                SimpleAfirmaMessages.getString("CipherDialog.0"), //$NON-NLS-1$
                JOptionPane.WARNING_MESSAGE
            );
            return false;
    	}
		final CipherConfig cipherConfig = new CipherConfig(getSelectedCipherAlgorithm());
        // Generamos la clave necesaria para el cifrado
        try {
            this.cipherKey = cipherConfig.getCipher().decodePassphrase(
        		getPassword(),
        		cipherConfig.getConfig(),
        		null
    		);
        }
        catch (final Exception ex) {
            LOGGER.severe("Error durante el proceso de generacion de claves: " + ex); //$NON-NLS-1$
            AOUIFactory.showErrorMessage(
            	this,
            	SimpleAfirmaMessages.getString("CipherDialog.14"), //$NON-NLS-1$
                SimpleAfirmaMessages.getString("CipherDialog.13"), //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        // Leemos el fichero de datos
        final byte[] fileContent;
        try ( final InputStream fis = new FileInputStream(new File(getTextFieldDataText())); ) {
            fileContent = AOUtil.getDataFromInputStream(fis);
        }
        catch (final Exception ex) {
            LOGGER.warning("Error al leer el fichero: " + ex); //$NON-NLS-1$
            AOUIFactory.showErrorMessage(
            	this,
            	SimpleAfirmaMessages.getString("CipherDialog.15"), //$NON-NLS-1$
                SimpleAfirmaMessages.getString("CipherDialog.13"), //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        catch(final OutOfMemoryError e) {
        	AOUIFactory.showErrorMessage(
    			this,
    			SimpleAfirmaMessages.getString("CipherDialog.16"), //$NON-NLS-1$
                SimpleAfirmaMessages.getString("CipherDialog.13"), //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
        	return false;
        }

        final byte[] result;
        try {
            result = cipherConfig.getCipher().cipher(fileContent, cipherConfig.getConfig(), this.cipherKey);
        }
        catch (final InvalidKeyException ex) {
            LOGGER.severe("No se cumplen con los requisitos de contrasena del algoritmo: " + ex); //$NON-NLS-1$
            AOUIFactory.showErrorMessage(
            	this,
            	SimpleAfirmaMessages.getString("CipherDialog.17"), //$NON-NLS-1$
                SimpleAfirmaMessages.getString("CipherDialog.13"), //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        catch (final Exception ex) {
            LOGGER.warning("Error al cifrar: " + ex); //$NON-NLS-1$
            AOUIFactory.showErrorMessage(
            	this,
            	SimpleAfirmaMessages.getString("CipherDialog.18"), //$NON-NLS-1$
                SimpleAfirmaMessages.getString("CipherDialog.13"), //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        if (result == null) {
        	AOUIFactory.showErrorMessage(
        		this,
        		SimpleAfirmaMessages.getString("CipherDialog.19"), //$NON-NLS-1$
                SimpleAfirmaMessages.getString("CipherDialog.13"), //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
        	);
        }
        else {
        	try {
	            // Almacenamos el fichero de salida de la operacion
	            final File savedFile =
	            		AOUIFactory.getSaveDataToFile(
	                        result,
	            			SimpleAfirmaMessages.getString("CipherDialog.20"), //$NON-NLS-1$
	            			null,
	            			AutoFirmaUtil.getCanonicalFile(new File(getTextFieldDataText())).getName() + ".cifrado", //$NON-NLS-1$
	                        null,
	                        null,
	                        this
	            );
	            if (savedFile == null) {
	                return false;
	            }
        	}
            catch(final IOException e) {
                LOGGER.severe(
                    "No se ha podido guardar el resultado del cifrado: " + e //$NON-NLS-1$
                );
                AOUIFactory.showErrorMessage(
                      this,
                      SimpleAfirmaMessages.getString("CipherDialog.21"), //$NON-NLS-1$
                      SimpleAfirmaMessages.getString("CipherDialog.20"), //$NON-NLS-1$
                      JOptionPane.ERROR_MESSAGE
                );
                return false;
            }
            catch(final AOCancelledOperationException e) {
            	return false;
            }
        }
        return true;
	}
}
