package es.gob.afirma.standalone.ui.envelopes;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import es.gob.afirma.core.misc.Platform;
import es.gob.afirma.core.ui.AOUIFactory;
import es.gob.afirma.standalone.AutoFirmaUtil;
import es.gob.afirma.standalone.SimpleAfirmaMessages;
import es.gob.afirma.standalone.ui.hash.CheckHashDialog;

/** Di&aacute;logo para seleccionar un certificado del directorio del Ministerio de Defensa.
 * @author Mariano Mart&iacute;nez. */
public final class DefenseDirectoryDialog extends JDialog implements KeyListener{

	private static final long serialVersionUID = 2772370402712649569L;
	static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$
	private static final int PREFERRED_WIDTH = 650;
	private static final int PREFERRED_HEIGHT = 400;
	private static final int PREFERRED_SCROLLPANE_WIDTH = 550;
	private static final int PREFERRED_SCROLLPANE_HEIGHT = 150;
	private static final String[] COLUMN_NAMES = {
		SimpleAfirmaMessages.getString("DefenseDirectoryDialog.14"), //$NON-NLS-1$
		SimpleAfirmaMessages.getString("DefenseDirectoryDialog.15"), //$NON-NLS-1$
		SimpleAfirmaMessages.getString("DefenseDirectoryDialog.16") //$NON-NLS-1$
	};

	private X509Certificate certificate;
	void setCertificate(final X509Certificate cert) {
		this.certificate = cert;
	}

	private final FakeLDAPMDEFManager lm = new FakeLDAPMDEFManager();
	FakeLDAPMDEFManager getLDAPMDEFManager() {
		return this.lm;
	}

	private final JTextField searchedUser = new JTextField();
	String getSearchedUser() {
		return this.searchedUser.getText();
	}

	private final JButton searchUserButton = new JButton(SimpleAfirmaMessages.getString("DefenseDirectoryDialog.5")); //$NON-NLS-1$
	private final JButton acceptButton = new JButton(SimpleAfirmaMessages.getString("DefenseDirectoryDialog.6")); //$NON-NLS-1$
	private final JButton cancelButton = new JButton(SimpleAfirmaMessages.getString("DefenseDirectoryDialog.7")); //$NON-NLS-1$

	JButton getAccpetButton() {
		return this.acceptButton;
	}
	void setAcceptButtonEnabled(final boolean enable) {
		this.acceptButton.setEnabled(enable);
	}

	void setSearchButtonEnabled(final boolean enable) {
		this.searchUserButton.setEnabled(enable);
	}

	/** Crea el di&aacute;logo y lo hace visible.
	 * @param parent Frame padre del di&aacute;logo.
	 * @return Certificado seleccionado. */
	public static X509Certificate startDefenseDirectoryDialog(final Frame parent) {
		final DefenseDirectoryDialog ode = new DefenseDirectoryDialog(parent);
		ode.setSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
		ode.setResizable(false);
		ode.setLocationRelativeTo(parent);
		ode.setVisible(true);
		return ode.certificate;
	}

	/** Crea el panel de apertura de un sobre digital.
	 * @param parent Componente padre del di&aacute;logo. */
	public DefenseDirectoryDialog(final Frame parent) {
		super(parent);
		setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
		createUI();
	}

	void createUI() {

		setTitle(SimpleAfirmaMessages.getString("DefenseDirectoryDialog.0")); //$NON-NLS-1$

		setIconImage(AutoFirmaUtil.getDefaultDialogsIcon());

		getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("DefenseDirectoryDialog.1") //$NON-NLS-1$
		);

		this.searchedUser.addKeyListener(this);

		final DefaultTableModel tableModel = new DefaultTableModel(COLUMN_NAMES, 0){
			private static final long serialVersionUID = -3072498531421378780L;

			@Override
		    public boolean isCellEditable(final int i, final int i1) {
		        return false;
		    }
		};
		final JTable table = new JTable(tableModel);
		table.setFillsViewportHeight(true);
		table.setAutoCreateRowSorter(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(
			lse -> {
				if (!lse.getValueIsAdjusting()) {
					setAcceptButtonEnabled(true);
				}
			}
		);
		table.getColumnModel().getColumn(0).setPreferredWidth(250);
		table.getColumnModel().getColumn(1).setPreferredWidth(250);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		table.addMouseListener(
			new MouseAdapter() {
			    @Override
				public void mousePressed(final MouseEvent me) {
			        if (me.getClickCount() == 2) {
			        	addSelectedItem(table, tableModel);
			        }
			    }
			}
		);

		final JScrollPane scrollPane = new JScrollPane(
			table,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
		);
		scrollPane.setPreferredSize(new Dimension(PREFERRED_SCROLLPANE_WIDTH, PREFERRED_SCROLLPANE_HEIGHT));
		scrollPane.addKeyListener(this);

		// Icono de la ventana
		setIconImage(AutoFirmaUtil.getDefaultDialogsIcon());

		final JLabel infoLabel = new JLabel(
			SimpleAfirmaMessages.getString("DefenseDirectoryDialog.2") //$NON-NLS-1$
		);

		final JLabel userLabel = new JLabel(
			SimpleAfirmaMessages.getString("DefenseDirectoryDialog.3") //$NON-NLS-1$
		);
		userLabel.setLabelFor(this.searchedUser);

		final JLabel usersListLabel = new JLabel(
			SimpleAfirmaMessages.getString("DefenseDirectoryDialog.4") //$NON-NLS-1$
		);
		usersListLabel.setLabelFor(scrollPane);

		this.searchUserButton.setIcon(
			new ImageIcon(
				Toolkit.getDefaultToolkit().getImage(
					CheckHashDialog.class.getResource("/resources/lupa_20x20.png") //$NON-NLS-1$
				)
			)
		);
		this.searchUserButton.setMnemonic('B');
		this.searchUserButton.getAccessibleContext().setAccessibleDescription(
 			SimpleAfirmaMessages.getString("DefenseDirectoryDialog.8") //$NON-NLS-1$
		);
		this.searchUserButton.addActionListener(
			ae -> {
				if (getSearchedUser() != null && !getSearchedUser().trim().isEmpty()) {
					final Users[] users = getLDAPMDEFManager().getUsers(getSearchedUser());
					tableModel.setRowCount(0);
					for (final Users user : users) {
						final Object[] row = { user.getCn(), user.getEmail(), user.getUid() };
						tableModel.addRow(row);
			        }
				}
			}
		);
		this.searchedUser.getDocument().addDocumentListener(
			new DocumentListener() {
				private void checkField() {
					if (getSearchedUser() != null && !getSearchedUser().trim().isEmpty()) {
						setSearchButtonEnabled(true);
					}
					else {
						setSearchButtonEnabled(false);
					}
				}

				@Override
				public void insertUpdate(final DocumentEvent e) {
					checkField();
				}

				@Override
				public void removeUpdate(final DocumentEvent e) {
					checkField();
				}

				@Override
				public void changedUpdate(final DocumentEvent e) {
					checkField();
				}
			}
		);
		this.searchUserButton.setEnabled(false);
		this.searchUserButton.addKeyListener(this);

		this.acceptButton.setMnemonic('A');
		this.acceptButton.getAccessibleContext().setAccessibleDescription(
 			SimpleAfirmaMessages.getString("DefenseDirectoryDialog.9") //$NON-NLS-1$
		);
		this.acceptButton.addActionListener(
			ae -> addSelectedItem(table, tableModel)
		);
		this.acceptButton.setEnabled(false);
		this.acceptButton.addKeyListener(this);

		this.cancelButton.setMnemonic('C');
		this.cancelButton.getAccessibleContext().setAccessibleDescription(
 			SimpleAfirmaMessages.getString("DefenseDirectoryDialog.10") //$NON-NLS-1$
		);
		this.cancelButton.addActionListener(
			ae -> {
				setCertificate(null);
				setVisible(false);
				dispose();
			}
		);
		this.cancelButton.addKeyListener(this);
		this.cancelButton.addKeyListener(this);

		final JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		// En Mac OS X el orden de los botones es distinto
		if (Platform.OS.MACOSX.equals(Platform.getOS())) {
			panel.add(this.cancelButton);
			panel.add(this.acceptButton);
		}
		else {
			panel.add(this.acceptButton);
			panel.add(this.cancelButton);
		}

		setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 25, 0, 20);
        c.weightx = 1.0;
        c.gridwidth = 2;
        add(infoLabel, c);
        c.insets = new Insets(20, 25, 0, 20);
        c.gridwidth = 1;
        c.gridy++;
        c.gridy++;
        add(userLabel, c);
        c.gridy++;
        c.insets = new Insets(5, 25, 0, 11);
        add(this.searchedUser, c);
        c.weightx = 0.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = new Insets(5, 25, 0, 20);
        add(this.searchUserButton, c);
        c.gridy++;
        c.insets = new Insets(20, 25, 0, 20);
        add(usersListLabel, c);
        c.gridy++;
        c.insets = new Insets(5, 20, 0, 20);
        add(scrollPane, c);
        c.gridy++;
        c.insets = new Insets(20, 20, 0, 20);
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.PAGE_END;
        add(panel, c);

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
			setCertificate(null);
			this.setVisible(false);
			dispose();
		}
	}

	void addSelectedItem(final JTable table, final TableModel tableModel) {
		if (table.getSelectedRow() != -1) {
			try {
				setCertificate(
					getLDAPMDEFManager().getCertificate(
						(String) tableModel.getValueAt(table.convertRowIndexToView(table.getSelectedRow()), 2)
					)
				);
			}
			catch (final Exception e) {
				LOGGER.log(
					Level.SEVERE,
					"No se pudo recuperar el certificado del usuario: " + e, //$NON-NLS-1$
					e
				);
		        AOUIFactory.showErrorMessage(
					this,
					SimpleAfirmaMessages.getString("DefenseDirectoryDialog.13"), //$NON-NLS-1$
					SimpleAfirmaMessages.getString("DefenseDirectoryDialog.12"), //$NON-NLS-1$
					JOptionPane.ERROR_MESSAGE
				);
			}
			setVisible(false);
			dispose();
		}
		else {
			LOGGER.severe("No se ha seleccionado usuario"); //$NON-NLS-1$
	        AOUIFactory.showErrorMessage(
				this,
				SimpleAfirmaMessages.getString("DefenseDirectoryDialog.11"), //$NON-NLS-1$
				SimpleAfirmaMessages.getString("DefenseDirectoryDialog.12"), //$NON-NLS-1$
				JOptionPane.ERROR_MESSAGE
			);
		}
	}
}
