package es.gob.afirma.standalone.ui.envelopes;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.misc.Platform;
import es.gob.afirma.standalone.AutoFirmaUtil;
import es.gob.afirma.standalone.LookAndFeelManager;
import es.gob.afirma.standalone.SimpleAfirmaMessages;
import es.gob.afirma.standalone.ui.CertValidationUi;
import es.gob.afirma.standalone.ui.CertificateUtils;

/**
 * Di&aacute;logo que muestra el resultado de la extracci&oacute;n de datos de
 * un sobre electr&oacute;nico.
 */
public class OpenDigitalEnvelopeInfoDialog extends JDialog implements KeyListener, ActionListener {

	/** Serial Id. */
	private static final long serialVersionUID = -5096799004296782600L;

	static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	private static final int PREFERRED_WIDTH = 600;
	private static final int PREFERRED_HEIGHT = 280;

	private X509Certificate signingCert = null;

	private final JButton viewCertButton = new JButton(SimpleAfirmaMessages.getString("OpenDigitalEnvelopeInfoDialog.0")); //$NON-NLS-1$

	private final JButton validateButton = new JButton(SimpleAfirmaMessages.getString("OpenDigitalEnvelopeInfoDialog.1")); //$NON-NLS-1$
	private final JButton closeButton = new JButton(SimpleAfirmaMessages.getString("OpenDigitalEnvelopeInfoDialog.2")); //$NON-NLS-1$

	/** Crea el panel con el resultado de la apertura del sobre digital.
	 * @param parent Componente padre del di&aacute;logo.
	 **/
	public OpenDigitalEnvelopeInfoDialog(final Window parent) {
		super(parent);

		setSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
		setResizable(false);
		setLocationRelativeTo(parent);

		createUI();
	}

	/** Crea el panel con el resultado de la apertura del sobre digital.
	 * @param parent Componente padre del di&aacute;logo.
	 * @param cert Certificado de firma del sobre electr&oacute;nico.
	 **/
	public OpenDigitalEnvelopeInfoDialog(final Window parent, final X509Certificate cert) {
		super(parent);

		setSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
		setResizable(false);
		setLocationRelativeTo(parent);

		this.signingCert = cert;

		createUI();
	}

	private void createUI() {

		setTitle(SimpleAfirmaMessages.getString("OpenDigitalEnvelopeInfoDialog.3")); //$NON-NLS-1$

		getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("OpenDigitalEnvelopeInfoDialog.4") //$NON-NLS-1$
		);

		if (!LookAndFeelManager.HIGH_CONTRAST) {
            setBackground(LookAndFeelManager.WINDOW_COLOR);
        }

		// Icono de la ventana
		setIconImage(AutoFirmaUtil.getDefaultDialogsIcon());

		// Panel con el resultado de la operacion
		final UnwrapResultPanel infoPanel =
				new UnwrapResultPanel(WrapValidity.VALID, this);
		infoPanel.setBorder(BorderFactory.createLoweredSoftBevelBorder());

		// Panel con el CN del certificado
		JPanel certInfoPanel = null;
		if (this.signingCert != null) {
			certInfoPanel = createCertInfoPanel(this.signingCert);
		}

		// Boton Validar certificados
		this.validateButton.setMnemonic('A');
		this.validateButton.getAccessibleContext().setAccessibleDescription(
 			SimpleAfirmaMessages.getString("OpenDigitalEnvelopeInfoDialog.5") //$NON-NLS-1$
		);
		this.validateButton.addActionListener(this);
		this.validateButton.addKeyListener(this);

		// Boton Cerrar
		this.closeButton.setMnemonic('c');
		this.closeButton.getAccessibleContext().setAccessibleDescription(
 			SimpleAfirmaMessages.getString("OpenDigitalEnvelopeInfoDialog.6") //$NON-NLS-1$
		);
		this.closeButton.addActionListener(this);
		this.closeButton.addKeyListener(this);

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		if (Platform.getOS().equals(Platform.OS.MACOSX)) {
			buttonPanel.add(this.closeButton);
			buttonPanel.add(this.validateButton);
		}
		else {
			buttonPanel.add(this.validateButton);
			buttonPanel.add(this.closeButton);
		}

		setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 0;
		c.weightx = 1.0;
		c.insets = new Insets(5, 11, 0, 11);
        add(infoPanel, c);
        if (certInfoPanel != null) {
        	c.gridy++;
        	add(certInfoPanel, c);
		}
        c.gridy++;
		c.insets = new Insets(11, 11, 0, 11);
        c.anchor = GridBagConstraints.PAGE_END;
        add(buttonPanel, c);
	}

	/**
	 * Crea un panel que muestra el CN de un certificado y un bot&oacute;n para validarlo.
	 * @param cert Certificado que se desea validar.
	 * @return Panel con los componentes gr&aacute;ficos.
	 */
	private JPanel createCertInfoPanel(final X509Certificate cert) {

		final JPanel certInfoPanel = new JPanel(new GridBagLayout());

		final JTextField certCnTextField = new JTextField();
		certCnTextField.setEditable(false);
		certCnTextField.setText(AOUtil.getCN(cert));

		final JLabel certCnLabel = new JLabel(SimpleAfirmaMessages.getString("OpenDigitalEnvelopeInfoDialog.7")); //$NON-NLS-1$
		certCnLabel.setLabelFor(certCnTextField);

		this.viewCertButton.addActionListener(this);
		this.viewCertButton.addKeyListener(this);

		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1.0;
		certInfoPanel.add(certCnLabel, c);
		c.gridy++;
		certInfoPanel.add(certCnTextField, c);
		c.gridx++;
		c.weightx = 0;
		c.insets = new Insets(0,  11, 0, 0);
		certInfoPanel.add(this.viewCertButton, c);

		return certInfoPanel;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {

		// Pulsacion del boton validar
		if (e.getSource() == this.validateButton) {
			validateCert(this.signingCert);
		}
		// Pulsacion del boton cerrar
		else if (e.getSource() == this.closeButton) {
			this.setVisible(false);
			dispose();
		}
		// Pulsacion del boton ver certificado
		else if (e.getSource() == this.viewCertButton) {
			CertificateUtils.openCertificate(this.signingCert, this);
		}

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

	/**
	 * Ejecuta la validaci&oacute;n de un certificado y muestra el resultado al usuario en
	 * un di&aacute;logo modal.
	 */
	private void validateCert(final X509Certificate cert) {
		CertValidationUi.validateCert(cert, this, this, AutoFirmaUtil.getDefaultDialogsIcon());
	}
}