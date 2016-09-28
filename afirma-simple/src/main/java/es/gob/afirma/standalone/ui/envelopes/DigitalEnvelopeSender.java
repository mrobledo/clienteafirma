package es.gob.afirma.standalone.ui.envelopes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.UnrecoverableEntryException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import es.gob.afirma.core.AOCancelledOperationException;
import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.misc.Platform;
import es.gob.afirma.core.ui.AOUIFactory;
import es.gob.afirma.keystores.AOCertificatesNotFoundException;
import es.gob.afirma.keystores.AOKeyStoreDialog;
import es.gob.afirma.keystores.AOKeyStoreManager;
import es.gob.afirma.keystores.filters.CertificateFilter;
import es.gob.afirma.keystores.filters.rfc.KeyUsageFilter;
import es.gob.afirma.standalone.SimpleAfirma;
import es.gob.afirma.standalone.SimpleAfirmaMessages;

/**
 * Panel para seleccionar el remitente que se quiere incluir en el sobre digital.
 * @author Juliana Marulanda
 */
public class DigitalEnvelopeSender extends JPanel {

	private static final long serialVersionUID = 7169956308231498090L;

	private static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	private final DigitalEnvelopePresentation dialog;
	DigitalEnvelopePresentation getDialog() {
		return this.dialog;
	}

	private final JButton addButton = new JButton(SimpleAfirmaMessages.getString("DigitalEnvelopeSender.17")); //$NON-NLS-1$
	private final JButton nextButton = new JButton(SimpleAfirmaMessages.getString("DigitalEnvelopePresentation.3")); //$NON-NLS-1$
	private final JButton cancelButton = new JButton(SimpleAfirmaMessages.getString("DigitalEnvelopePresentation.4")); //$NON-NLS-1$
	private final JButton backButton = new JButton(SimpleAfirmaMessages.getString("DigitalEnvelopePresentation.5") ); //$NON-NLS-1$

	JButton getBackButton() {
		return this.backButton;
	}

	private final JTextField senderTextField = new JTextField();
	JTextField getSenderTextField() {
		return this.senderTextField;
	}

	final JPanel panel = new JPanel();
	JPanel getPanel() {
		return this.panel;
	}

	private PrivateKeyEntry senderPrivateKeyEntry = null;
	PrivateKeyEntry getSenderPrivateKeyEntry() {
		return this.senderPrivateKeyEntry;
	}

	private AOKeyStoreManager senderKeyStoreManager = null;
	AOKeyStoreManager getSenderKeyStoreManager() {
		return this.senderKeyStoreManager;
	}

	private boolean certificateDialogOpenned = false;
	public boolean isCertificateDialogOpenned() {
		return this.certificateDialogOpenned;
	}

	public void setCertificateDialogOpenned(final boolean certificateDialogOpenned) {
		this.certificateDialogOpenned = certificateDialogOpenned;
	}

	/** Crea el panel de remitentes del asistente.
	 * @param parent Di&aacute;logo del asistente de ensobrado. */
	public DigitalEnvelopeSender(final DigitalEnvelopePresentation parent) {
		this.dialog = parent;

		if (this.dialog != null && this.dialog.getEnvelopeData().getFilePath() != null) {
			this.senderKeyStoreManager = this.dialog.getEnvelopeData().getSenderKeyStoreManager();
			this.senderPrivateKeyEntry = this.dialog.getEnvelopeData().getSenderPrivateKeyEntry();

			// Mostramos el CN/alias del certificado seleccionado en el cuadro de texto
			showSenderCertAlias();
		}

		createUI();
	}

	/**
	 * Muestra el CN o alias del certificado seleccionado para firmar la petici&oacute;n.
	 */
	private void showSenderCertAlias() {
		if (this.senderPrivateKeyEntry != null) {
			String senderText = null;
			if (this.senderPrivateKeyEntry.getCertificate() instanceof X509Certificate) {
				senderText = AOUtil.getCN((X509Certificate) this.senderPrivateKeyEntry.getCertificate());
			}
			this.senderTextField.setText(
					senderText != null ?
							senderText.trim() : this.dialog.getEnvelopeData().getSenderCertificateAlias().trim());
		}
	}

	void createUI() {

		getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("DigitalEnvelopeSender.32") //$NON-NLS-1$
		);

        // Panel con el contenido
        final JPanel panelCentral = new JPanel();
        final GridBagLayout gbLayout = new GridBagLayout();
        panelCentral.setBackground(Color.WHITE);
        panelCentral.setLayout(gbLayout);
        panelCentral.setBorder(BorderFactory.createEmptyBorder());

        // Etiqueta con el texto "Remitente..."
        final JLabel label = new JLabel(
    		SimpleAfirmaMessages.getString("DigitalEnvelopeSender.0"), //$NON-NLS-1$
    		SwingConstants.CENTER
        );
        label.setFont(new java.awt.Font ("Century Schoolbook L", 0, 13)); //$NON-NLS-1$

        // Eleccion del remitente
        final JLabel labelCombo = new JLabel(SimpleAfirmaMessages.getString("DigitalEnvelopeSender.12")); //$NON-NLS-1$
        labelCombo.setLabelFor(this.senderTextField);
        this.senderTextField.setFont(new java.awt.Font ("Century Schoolbook L", 0, 13)); //$NON-NLS-1$
        this.senderTextField.setFocusable(false);

 		// Boton de anadir
		this.addButton.setMnemonic('D');
		this.addButton.getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("DigitalEnvelopeSender.20") //$NON-NLS-1$
		);
		this.addButton.addActionListener(
			new ActionListener() {
				/** {@inheritDoc} */
				@Override
				public void actionPerformed(final ActionEvent ae) {
					setSender();
				}
			}
		);
		this.addButton.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(final KeyEvent ke) {
				getDialog().keyTyped(ke);
			}

			@Override
			public void keyReleased(final KeyEvent ke) {
				if (isCertificateDialogOpenned()) {
					setCertificateDialogOpenned(false);
				}
				else {
					getDialog().keyReleased(ke);
				}
			}

			@Override
			public void keyPressed(final KeyEvent ke) {
				getDialog().keyPressed(ke);
			}
		});

		 // Boton de siguiente
 		this.nextButton.setMnemonic('S');
 		this.nextButton.getAccessibleContext().setAccessibleDescription(
 			SimpleAfirmaMessages.getString("DigitalEnvelopePresentation.6") //$NON-NLS-1$
		);
 		this.nextButton.addActionListener(
 			new ActionListener() {
 				/** {@inheritDoc} */
 				@Override
 				public void actionPerformed(final ActionEvent ae) {
 					if (
						Enveloper.createEnvelope(
 							getDialog(),
 							DigitalEnvelopeSender.this.getSenderPrivateKeyEntry(),
 							DigitalEnvelopeSender.this.getSenderKeyStoreManager()
						)
					) {
 						getDialog().remove(panelCentral);
 						getDialog().remove(getPanel());
 						getDialog().remove(getDialog().getSendersPanel());
 		 				getDialog().add(new DigitalEnvelopeEnd(getDialog()), BorderLayout.CENTER);
						getDialog().revalidate();
						getDialog().repaint();
 					}
 				}
 			}
 		);
 		this.nextButton.addKeyListener(this.dialog);

 		// Boton cancelar
		this.cancelButton.setMnemonic('C');
		this.cancelButton.getAccessibleContext().setAccessibleDescription(
 			SimpleAfirmaMessages.getString("DigitalEnvelopePresentation.7") //$NON-NLS-1$
		);
		this.cancelButton.addActionListener(
			new ActionListener() {
				/** {@inheritDoc} */
				@Override
				public void actionPerformed(final ActionEvent ae) {
					getDialog().setVisible(false);
					getDialog().dispose();
				}
			}
		);
		this.cancelButton.addKeyListener(this.dialog);

		// Boton de volver
		this.backButton.setMnemonic('A');
		this.backButton.getAccessibleContext().setAccessibleDescription(
 			SimpleAfirmaMessages.getString("DigitalEnvelopePresentation.8") //$NON-NLS-1$
		);
		this.backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				getDialog().remove(panelCentral);
				getDialog().remove(DigitalEnvelopeSender.this.panel);
				getDialog().remove(getDialog().getSendersPanel());

				getDialog().getEnvelopeData().setSenderPrivateKeyEntry(DigitalEnvelopeSender.this.getSenderPrivateKeyEntry());
				getDialog().getEnvelopeData().setSenderKeyStoreManager(DigitalEnvelopeSender.this.getSenderKeyStoreManager());
				getDialog().getEnvelopeData().setSenderCertificateAlias(DigitalEnvelopeSender.this.getSenderTextField().getText());

				getDialog().setRecipientsPanel(
					new DigitalEnvelopeRecipients(
						getDialog()
					)
				);
				getDialog().add(getDialog().getRecipientsPanel(), BorderLayout.CENTER);
				getDialog().revalidate();
				getDialog().repaint();
			}
		});
		this.backButton.addKeyListener(this.dialog);

		this.panel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		// En Mac OS X el orden de los botones es distinto
		if (Platform.OS.MACOSX.equals(Platform.getOS())) {
			this.panel.add(this.cancelButton);
			this.panel.add(this.backButton);
			this.panel.add(this.nextButton);
		}
		else {
			this.panel.add(this.backButton);
			this.panel.add(this.nextButton);
			this.panel.add(this.cancelButton);
		}

		final JPanel emptyPanel = new JPanel();
		emptyPanel.setBackground(Color.WHITE);

		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 1.0;
        c.weighty = 0.0;
        c.gridx = 0;
        c.gridy = 0;

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(30, 10, 0, 20);
		panelCentral.add(label, c);
		c.gridy++;
		panelCentral.add(labelCombo, c);
		c.gridwidth = 1;
		c.insets = new Insets(5, 10, 0, 11);
		c.weightx = 1.0;
		c.gridy++;
		panelCentral.add(this.senderTextField, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(5, 0, 0, 20);
		c.weightx = 0.0;
		c.gridx = 1;
		c.anchor = GridBagConstraints.LINE_END;
		panelCentral.add(this.addButton, c);
		c.insets = new Insets(20, 10, 20, 20);
		c.weighty = 1.0;
		c.gridy++;
		panelCentral.add(emptyPanel, c);

		this.dialog.getContentPane().add(panelCentral);
		this.dialog.getContentPane().add(this.panel, BorderLayout.PAGE_END);
		this.dialog.revalidate();
        this.dialog.repaint();
        enableButtons(this.senderPrivateKeyEntry != null);
	}

    /** A&ntilde;ade un remitente del origen seleccionado en el desplegable. */
    void setSender() {

    	setCertificateDialogOpenned(true);

    	final AOKeyStoreManager keyStoreManager = SimpleAfirma.getAOKeyStoreManager();
    	if (keyStoreManager == null) {
    		JOptionPane.showMessageDialog(
    				this,
    				SimpleAfirmaMessages.getString("DigitalEnvelopePresentation.11"), //$NON-NLS-1$
    				SimpleAfirmaMessages.getString("DigitalEnvelopePresentation.12"), //$NON-NLS-1$
    				JOptionPane.WARNING_MESSAGE);
    		return;
    	}

        // Solo permitimos usar certificados de firma como remitentes
        final List<CertificateFilter> filtersList = new ArrayList<>();
        filtersList.add(new KeyUsageFilter(KeyUsageFilter.SIGN_CERT_USAGE));

        final AOKeyStoreDialog keyStoreDialog = new AOKeyStoreDialog(
        		keyStoreManager,
    			getDialog(),
    			true,             // Comprobar claves privadas
    			false,            // Mostrar certificados caducados
    			true,             // Comprobar validez temporal del certificado
    			filtersList, 	  // Filtros
    			false             // mandatoryCertificate
		);

    	try {
			keyStoreDialog.show();
			keyStoreManager.setParentComponent(this);
        	this.senderPrivateKeyEntry = keyStoreManager.getKeyEntry(
    			keyStoreDialog.getSelectedAlias()
    		);
        	this.senderKeyStoreManager = keyStoreManager;
    	}
    	catch (final UnrecoverableEntryException e) {
        	LOGGER.warning("Error de contrasena: " + e); //$NON-NLS-1$
            // Control de la excepcion generada al introducir mal la contrasena para el certificado
            AOUIFactory.showMessageDialog(
        		this.dialog,
        		SimpleAfirmaMessages.getString("DigitalEnvelopeSender.22"), //$NON-NLS-1$
        		SimpleAfirmaMessages.getString("DigitalEnvelopeSender.25"),  //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );            return;
        }
        catch (final AOCancelledOperationException e) {
        	LOGGER.info("Operacion cancelada por el usuario: " + e); //$NON-NLS-1$
        	return;
        }
    	catch (final AOCertificatesNotFoundException e) {
        	LOGGER.warning("No se han encontrado certificados validos en el almacen: " + e); //$NON-NLS-1$
        	// Control de la excepcion generada al introducir mal la contrasena para el certificado
            AOUIFactory.showMessageDialog(
        		this.dialog,
        		SimpleAfirmaMessages.getString("DigitalEnvelopeSender.37"), //$NON-NLS-1$
        		SimpleAfirmaMessages.getString("DigitalEnvelopeSender.25"),  //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
        	return;
        }
    	catch (final Exception e) {
        	LOGGER.severe("Error recuperando la clave privada: " + e); //$NON-NLS-1$
        	// Control de la excepcion generada al introducir mal la contrasena para el certificado
            AOUIFactory.showMessageDialog(
        		this.dialog,
        		SimpleAfirmaMessages.getString("DigitalEnvelopeSender.38"), //$NON-NLS-1$
        		SimpleAfirmaMessages.getString("DigitalEnvelopeSender.25"),  //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
        	return;
        }

    	setCertificateDialogOpenned(false);

    	// Mostramos el alias del certificado
    	showSenderCertAlias();

        enableButtons(this.senderPrivateKeyEntry != null);
    }

    void enableButtons(final boolean enable) {
    	this.nextButton.setEnabled(enable);
    	this.nextButton.setFocusable(enable);
    	if (enable) {
    		this.nextButton.requestFocusInWindow();
    	}
    	else {
    		this.addButton.requestFocusInWindow();
    	}
    }
}
