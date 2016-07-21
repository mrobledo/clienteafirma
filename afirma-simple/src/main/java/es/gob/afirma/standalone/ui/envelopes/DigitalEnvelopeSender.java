package es.gob.afirma.standalone.ui.envelopes;

import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_KEYSTORE_CYPH_ONLY_CERTS;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.UnrecoverableEntryException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import es.gob.afirma.core.AOCancelledOperationException;
import es.gob.afirma.core.misc.Platform;
import es.gob.afirma.core.ui.AOUIFactory;
import es.gob.afirma.keystores.AOKeyStore;
import es.gob.afirma.keystores.AOKeyStoreDialog;
import es.gob.afirma.keystores.AOKeyStoreManager;
import es.gob.afirma.keystores.AOKeyStoreManagerFactory;
import es.gob.afirma.keystores.KeyStoreConfiguration;
import es.gob.afirma.keystores.filters.CertificateFilter;
import es.gob.afirma.keystores.filters.CipherCertificateFilter;
import es.gob.afirma.standalone.SimpleAfirmaMessages;
import es.gob.afirma.standalone.ui.preferences.PreferencesManager;

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

	private final JComboBox<KeyStoreConfiguration> comboBoxRecipients = new JComboBox<>();

	private final JButton addButton = new JButton(SimpleAfirmaMessages.getString("DigitalEnvelopeSender.17")); //$NON-NLS-1$
	private final JButton removeButton = new JButton(SimpleAfirmaMessages.getString("DigitalEnvelopeSender.19")); //$NON-NLS-1$
	private final JButton nextButton = new JButton(SimpleAfirmaMessages.getString("DigitalEnvelopePresentation.3")); //$NON-NLS-1$
	private final JButton cancelButton = new JButton(SimpleAfirmaMessages.getString("DigitalEnvelopePresentation.4")); //$NON-NLS-1$
	private final JButton backButton = new JButton(SimpleAfirmaMessages.getString("DigitalEnvelopePresentation.5") ); //$NON-NLS-1$

	JButton getBackButton() {
		return this.backButton;
	}

	private final List<CertificateDestiny> certificateList = new ArrayList<>();
	private final JList<String> senderList = new JList<>();
	JList<String> getSendersList() {
		return this.senderList;
	}

	private final JScrollPane scrollPane = new JScrollPane(
		this.senderList,
		ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
	);

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

	/** Crea el panel de remitentes del asistente.
	 * @param parent Di&aacute;logo del asistente de ensobrado. */
	public DigitalEnvelopeSender(final DigitalEnvelopePresentation parent) {
		this.dialog = parent;
		createUI();
	}

	void createUI() {

		getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("DigitalEnvelopeSender.32") //$NON-NLS-1$
		);

		this.senderList.setModel(new DefaultListModel<String>());

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
        labelCombo.setLabelFor(this.comboBoxRecipients);
        this.comboBoxRecipients.setModel(new DefaultComboBoxModel<>(EnvelopesUtils.getKeyStoresToSign()));
 		this.comboBoxRecipients.setSelectedItem(
 			SimpleAfirmaMessages.getString("DigitalEnvelopeSender.13") //$NON-NLS-1$
 		);
 		this.comboBoxRecipients.addKeyListener(this.dialog);

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
					addSender();
				}
			}
		);
		this.addButton.addKeyListener(this.dialog);

		// Area de texto con el remitente
		final JLabel labelText = new JLabel(SimpleAfirmaMessages.getString("DigitalEnvelopeSender.18")); //$NON-NLS-1$
		labelText.setLabelFor(this.scrollPane);
		this.scrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
        this.scrollPane.setFont(new java.awt.Font ("Century Schoolbook L", 0, 13)); //$NON-NLS-1$
        this.scrollPane.addKeyListener(this.dialog);
        this.senderList.setFocusable(false);

        // Boton de eliminar remitente
		this.removeButton.setMnemonic('E');
		this.removeButton.getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("DigitalEnvelopeSender.21") //$NON-NLS-1$
		);
		this.removeButton.addActionListener(
			new ActionListener() {
				/** {@inheritDoc} */
				@Override
				public void actionPerformed(final ActionEvent ae) {
					removeSender();
				}
			}
		);
		this.removeButton.addKeyListener(this.dialog);

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
 		 				getDialog().add(new DigitalEnvelopeEnd(getDialog()));
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
				getDialog().setRecipientsPanel(
					new DigitalEnvelopeRecipients(
						getDialog()
					)
				);
				getDialog().add(getDialog().getRecipientsPanel());
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
		c.gridwidth = 2;
		c.insets = new Insets(5, 10, 0, 20);
		c.weightx = 0.0;
		c.gridy++;
		c.fill = GridBagConstraints.NONE;
		panelCentral.add(this.comboBoxRecipients, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.LINE_END;
		panelCentral.add(this.addButton, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(20, 10, 0, 20);
		c.gridx = 0;
		c.weightx = 1.0;
		c.gridy++;
		panelCentral.add(labelText, c);
		c.insets = new Insets(5, 10, 0, 20);
		c.ipady = 120;
		c.gridy++;
		panelCentral.add(this.scrollPane, c);
		c.insets = new Insets(20, 10, 20, 20);
		c.ipady = 0;
		c.weightx = 0.0;
		c.gridx = 1;
		c.gridy++;
		c.fill = GridBagConstraints.NONE;
		panelCentral.add(this.removeButton, c);
		c.weighty = 1.0;
		c.gridy++;
		panelCentral.add(emptyPanel, c);

		this.dialog.getContentPane().add(panelCentral);
		this.dialog.getContentPane().add(this.panel, BorderLayout.PAGE_END);
		this.dialog.revalidate();
        this.dialog.repaint();
        enableButtons(this.senderList.getModel().getSize() > 0);
	}

    /** A&ntilde;ade un remitente del origen seleccionado en el desplegable. */
    void addSender() {
    	final DefaultListModel<String> modelList = (DefaultListModel<String>) getSendersList().getModel();
    	String[] filter;
    	final AOKeyStoreManager keyStoreManager;
        final KeyStoreConfiguration kc = (KeyStoreConfiguration) this.comboBoxRecipients.getSelectedItem();
        try {
            final AOKeyStore ao = kc.getType();
            String lib = null;
            if (ao == AOKeyStore.PKCS12 || ao == AOKeyStore.SINGLE) {
                if (ao == AOKeyStore.PKCS12) {
                    filter = new String[] {"p12", "pfx"};  //$NON-NLS-1$//$NON-NLS-2$
                }
                else {
                    filter = new String[] { "cer", "p7b", "p7s"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
                final File keystorePath = EnvelopesUtils.addFileSelected(filter, this.comboBoxRecipients, getDialog());
                if (keystorePath == null) {
                    throw new AOCancelledOperationException();
                }
                lib = keystorePath.getAbsolutePath();
            }
            else if (ao == AOKeyStore.PKCS11) {
                filter = new String[] {"dll", "so"};  //$NON-NLS-1$//$NON-NLS-2$
                final File keystorePath = EnvelopesUtils.addFileSelected(filter, this.comboBoxRecipients, getDialog());
                if (keystorePath == null) {
                    throw new AOCancelledOperationException();
                }
                lib = keystorePath.getAbsolutePath();
            }
            keyStoreManager = AOKeyStoreManagerFactory.getAOKeyStoreManager(
        		ao,
        		lib,
        		"default", //$NON-NLS-1$
        		ao.getStorePasswordCallback(getDialog()),
        		getDialog()
            );
        }
        catch (final AOCancelledOperationException e) {
            LOGGER.info("Operacion cancelada por el usuario: " + e); //$NON-NLS-1$
            return;
        }
        catch (final IOException e) {
        	AOUIFactory.showErrorMessage(
				this.dialog,
				SimpleAfirmaMessages.getString("DigitalEnvelopeRecipients.23"), //$NON-NLS-1$
				SimpleAfirmaMessages.getString("DigitalEnvelopeRecipients.20"), //$NON-NLS-1$
				JOptionPane.ERROR_MESSAGE
			);
			LOGGER.severe("Error abriendo el certificado: " + e);//$NON-NLS-1$
			return;
	    }
	    catch (final Exception e) {
	        LOGGER.severe("No se ha podido abrir el almacen de certificados: " + e); //$NON-NLS-1$
	        AOUIFactory.showErrorMessage(
				this.dialog,
				SimpleAfirmaMessages.getString("DigitalEnvelopeRecipients.19"), //$NON-NLS-1$
				SimpleAfirmaMessages.getString("DigitalEnvelopeRecipients.20"), //$NON-NLS-1$
				JOptionPane.ERROR_MESSAGE
			);
	        return;
	    }

        List<CertificateFilter> filtersList = null;
        if (PreferencesManager.getBoolean(PREFERENCE_KEYSTORE_CYPH_ONLY_CERTS, false)) {
        	 filtersList = new ArrayList<>();
             filtersList.add(new CipherCertificateFilter());
        }

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
        	LOGGER.severe("Error de constasena: " + e); //$NON-NLS-1$
            // Control de la excepcion generada al introducir mal la contrasena para el certificado
            AOUIFactory.showMessageDialog(
        		this.dialog,
        		SimpleAfirmaMessages.getString("DigitalEnvelopeSender.22"), //$NON-NLS-1$
        		SimpleAfirmaMessages.getString("DigitalEnvelopeSender.23"),  //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        catch (final AOCancelledOperationException e) {
        	LOGGER.info("Operacion cancelada por el usuario: " + e); //$NON-NLS-1$
        	return;
        }
    	catch (final Exception e1) {
        	LOGGER.info("Error recuperando la clave privada: " + e1); //$NON-NLS-1$
        	return;
        }

	    final CertificateDestiny certDest = new CertificateDestiny(keyStoreDialog.getSelectedAlias(), this.senderPrivateKeyEntry.getCertificate());

	    // Comprobamos que el certificado es correcto
	    if (certDest.getAlias() != null && !certDest.equals("")) { //$NON-NLS-1$
	        boolean copiar = true;
	        for (int i = 0; i < modelList.getSize(); i++) {
	            if (certDest.getAlias().equals(modelList.getElementAt(i))) {
	                copiar = false;
	            }
	        }
	        if (copiar) {
	        	modelList.addElement(certDest.getAlias());
	            this.certificateList.add(certDest);
	        }
	        else {
	        	 LOGGER.severe("Ya existe ese usuario"); //$NON-NLS-1$
	 	        AOUIFactory.showMessageDialog(
     				this.dialog,
     				SimpleAfirmaMessages.getString("DigitalEnvelopeRecipients.25"), //$NON-NLS-1$
     				SimpleAfirmaMessages.getString("DigitalEnvelopeRecipients.20"), //$NON-NLS-1$
     				JOptionPane.ERROR_MESSAGE
     			);
	 	        return;
	        }
	    }
        enableButtons(true);
    }


	/** Elimina el remitente de la lista.*/
    void removeSender() {
    	final DefaultListModel<String> listaModel = (DefaultListModel<String>) getSendersList().getModel();
        for (int i = 0; i < this.certificateList.size(); i++) {
            if (this.certificateList.get(i).getAlias().equals(this.senderList.getSelectedValue())) {
                this.certificateList.remove(this.certificateList.get(i));
                listaModel.remove(this.senderList.getSelectedIndex());
                break;
            }
        }

        if (listaModel.isEmpty()) {
        	enableButtons(false);
        }

        // Borramos las posibles claves del certificado
        this.senderPrivateKeyEntry = null;
    }

	void enableButtons(final boolean enable) {
		if (getDialog().getEnvelopeData().getEnvelopeType() != EnvelopesTypeResources.SIMPLE) {
			this.nextButton.setEnabled(enable);
		    this.nextButton.setFocusable(enable);
		    this.removeButton.setEnabled(enable);
		    this.removeButton.setFocusable(enable);
		    this.addButton.setEnabled(!enable);
		    this.addButton.setFocusable(!enable);
		    if (enable) {
	        	this.nextButton.requestFocusInWindow();
	        }
	        else {
	        	this.addButton.requestFocusInWindow();
	        }
		}
		else {
			this.removeButton.setEnabled(enable);
		    this.removeButton.setFocusable(enable);
		    this.addButton.setEnabled(!enable);
		    this.addButton.setFocusable(!enable);
		    this.nextButton.requestFocusInWindow();
		}
	}
}
