package angrypixel.gallery;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;

public class EntryPanel extends JPanel {
	private static final long serialVersionUID = 1669990040079384585L;

	private final Manager manager;
	public JTextField txtTitle;
	public JTextField txtAuthor;
	public JTextField txtSourceUrl;
	public JPanel picturePanel;
	private JPanel picturePanel_1;
	public JTextArea txtrDescription;

	public EntryData entry;
	private JPopupMenu popupMenu;

	public EntryPanel(Manager manager, EntryData entry) {
		this.manager = manager;

		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		this.entry = entry;

		setBackground(Color.LIGHT_GRAY);

		JLabel lblTitle = new JLabel("Title:");

		txtTitle = new JTextField();
		txtTitle.setColumns(10);
		if(entry.getTitle() != null)
			txtTitle.setText(entry.getTitle());
		addEntryUpdater(txtTitle, Property.TITLE);

		JLabel lblAuthor = new JLabel("Author:");

		txtAuthor = new JTextField();
		txtAuthor.setColumns(10);
		if(entry.getAuthor() != null)
			txtAuthor.setText(entry.getAuthor());
		addEntryUpdater(txtAuthor, Property.AUTHOR);

		JLabel lblSourceUrl = new JLabel("Source URL:");

		txtSourceUrl = new JTextField();
		txtSourceUrl.setColumns(10);
		if(entry.getSourceUrl() != null)
			txtSourceUrl.setText(entry.getSourceUrl());
		addEntryUpdater(txtSourceUrl, Property.SOURCE_URL);

		JLabel lblDescription = new JLabel("Description:");

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		picturePanel = new JPanel();

		picturePanel_1 = new ImagePanel(entry.getImage());
		addEntryUpdater(picturePanel_1);
		picturePanel_1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		popupMenu = new JPopupMenu();
		addPopup(this, popupMenu);

		JButton btnDelete = new JButton("Delete");
		popupMenu.add(btnDelete);

		btnDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int dialogResult = JOptionPane.showConfirmDialog(EntryPanel.this, "Are you sure you want to delete the entry '" + EntryPanel.this.entry.getTitle() + "'?", "Delete?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if(dialogResult == JOptionPane.YES_OPTION) {
					if(Gallery.INSTANCE.removeEntry(EntryPanel.this.entry)) {
						SwingUtilities.invokeLater(() -> {
							EntryPanel.this.manager.removeEntryPanel(EntryPanel.this);
						});
					}
				}
			}
		});

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addContainerGap().addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblSourceUrl, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE).addComponent(lblTitle, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(lblAuthor, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)).addComponent(lblDescription, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE)).addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(txtAuthor, GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE).addComponent(txtSourceUrl, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE).addComponent(scrollPane, 204, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(txtTitle, GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)).addGap(9).addComponent(picturePanel_1, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE).addGap(11)));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addGroup(groupLayout.createSequentialGroup().addContainerGap().addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addComponent(picturePanel_1, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE).addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(txtTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(lblTitle)).addGap(8).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(txtAuthor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(lblAuthor)).addGap(8).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(txtSourceUrl, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(lblSourceUrl)).addGap(8).addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE).addComponent(lblDescription)))).addContainerGap()));

		txtrDescription = new JTextArea();
		scrollPane.setViewportView(txtrDescription);
		setLayout(groupLayout);

		txtrDescription.getDocument().addDocumentListener(new DocumentListener() {
			private void resizeScrollPane() {
				scrollPane.setSize((int) txtrDescription.getPreferredSize().getWidth(), (int) txtrDescription.getPreferredSize().getHeight());
				EntryPanel.this.revalidate();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				this.resizeScrollPane();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				this.resizeScrollPane();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				this.resizeScrollPane();
			}
		});

		if(entry.getDescription() != null)
			txtrDescription.setText(entry.getDescription());
		addEntryUpdater(txtrDescription, Property.DESCRIPTION);

	}

	enum Property {
		TITLE, AUTHOR, DESCRIPTION, SOURCE_URL, VERSION
	}

	private void setEntry(EntryData newEntry) {
		if(this.entry != null) {
			if(!Gallery.INSTANCE.replaceEntry(this.entry, newEntry)) {
				Gallery.INSTANCE.removeEntry(this.entry);
				Gallery.INSTANCE.addEntry(newEntry);
			}
		} else {
			Gallery.INSTANCE.addEntry(newEntry);
		}
		this.entry = newEntry;
	}

	private void addEntryUpdater(JPanel picturePanel) {
		picturePanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent event) {
				if(event.getButton() == MouseEvent.BUTTON1) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Choose picture");
					fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
					fileChooser.setAcceptAllFileFilterUsed(false);
					fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG", "png"));
					int result = fileChooser.showOpenDialog(EntryPanel.this);
					if(result == JFileChooser.APPROVE_OPTION) {
						EntryPanel.this.updateImageFromFile(picturePanel, fileChooser.getSelectedFile());
					}
				}
			}
		});

		picturePanel.setDropTarget(new DropTarget() {
			private static final long serialVersionUID = 3979966801839969333L;

			@Override
			public synchronized void drop(DropTargetDropEvent evt) {
				try {
					evt.acceptDrop(DnDConstants.ACTION_COPY);
					@SuppressWarnings("unchecked")
					List<File> files = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
					if(!files.isEmpty()) {
						EntryPanel.this.updateImageFromFile(picturePanel, files.get(0));
					}
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	private void updateImageFromFile(JPanel picturePanel, File selectedFile) {
		if(!Pattern.compile("^[a-zA-Z0-9_]*\\.png$").matcher(selectedFile.getName()).find()) {
			JOptionPane.showMessageDialog(EntryPanel.this, "Invalid file name", "Error", JOptionPane.WARNING_MESSAGE);
		} else if(selectedFile.exists()) {
			File targetFile = new File(EntryPanel.this.manager.tempFolder, selectedFile.getName());
			if(!targetFile.exists() || targetFile.equals(EntryPanel.this.entry.getPictureFile())) {
				try(FileInputStream fio = new FileInputStream(selectedFile)) {
					BufferedImage image = Gallery.readBufferedImage(fio);
					if(image != null && image.getWidth() > 0 && image.getHeight() > 0) {
						//Delete existing
						if(EntryPanel.this.entry.getPictureFile() != null) {
							try {
								Files.delete(EntryPanel.this.entry.getPictureFile().toPath());
							} catch(IOException ex) {
								JOptionPane.showMessageDialog(EntryPanel.this, "Failed deleting old picture", "Error", JOptionPane.ERROR_MESSAGE);
								ex.printStackTrace();
								return;
							}
						}

						//Resize image
						if(image.getWidth() > EntryPanel.this.manager.getMaxImageSize() || image.getHeight() > EntryPanel.this.manager.getMaxImageSize()) {
							int maxDim = Math.max(image.getWidth(), image.getHeight());
							float relWidth = (1.0F - (maxDim - image.getWidth()) / (float) maxDim);
							float relHeight = (1.0F - (maxDim - image.getHeight()) / (float) maxDim);
							int drawnWidth = (int) Math.floor(Math.min(EntryPanel.this.manager.getMaxImageSize(), image.getWidth()) * relWidth);
							int drawnHeight = (int) Math.floor(Math.min(EntryPanel.this.manager.getMaxImageSize(), image.getHeight()) * relHeight);
							BufferedImage resized = new BufferedImage(drawnWidth, drawnHeight, BufferedImage.TYPE_INT_ARGB);
							Graphics2D g = resized.createGraphics();
							g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
							g.drawImage(image, 0, 0, drawnWidth, drawnHeight, 0, 0, image.getWidth(), image.getHeight(), null);
							g.dispose();
							image = resized;
						}

						try {
							ImageIO.write(image, "png", targetFile);
						} catch(IOException ex) {
							JOptionPane.showMessageDialog(EntryPanel.this, "Unable to save picture to gallery", "Error", JOptionPane.ERROR_MESSAGE);
							ex.printStackTrace();
						}

						EntryData e = EntryPanel.this.entry;
						EntryPanel.this.setEntry(new EntryData(e.getSha256(), e.getUrl(), e.getTitle(), e.getAuthor(), e.getDescription(), e.getSourceUrl(), targetFile, e.getVersion()));
						EntryPanel.this.entry.setImage(image);

						if(picturePanel instanceof ImagePanel) {
							((ImagePanel) picturePanel).setImage(image);
						}
					}
				} catch(IOException ex) {
					System.out.println("Unable to open selected picture");
					ex.printStackTrace();
				}
			} else {
				JOptionPane.showMessageDialog(EntryPanel.this, "A picture with the same name already exists in the gallery", "Error", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	private void addEntryUpdater(JTextComponent textComponent, Property property) {
		textComponent.getDocument().addDocumentListener(new DocumentListener() {
			private void updateEntry() {
				EntryData e = EntryPanel.this.entry;
				EntryData newEntry = null;
				switch(property) {
				case TITLE:
					newEntry = new EntryData(e.getSha256(), e.getUrl(), textComponent.getText(), e.getAuthor(), e.getDescription(), e.getSourceUrl(), e.getPictureFile(), e.getVersion());
					break;
				case AUTHOR:
					newEntry = new EntryData(e.getSha256(), e.getUrl(), e.getTitle(), textComponent.getText(), e.getDescription(), e.getSourceUrl(), e.getPictureFile(), e.getVersion());
					break;
				case DESCRIPTION:
					newEntry = new EntryData(e.getSha256(), e.getUrl(), e.getTitle(), e.getAuthor(), textComponent.getText(), e.getSourceUrl(), e.getPictureFile(), e.getVersion());
					break;
				case SOURCE_URL:
					newEntry = new EntryData(e.getSha256(), e.getUrl(), e.getTitle(), e.getAuthor(), e.getDescription(), textComponent.getText(), e.getPictureFile(), e.getVersion());
					break;
				case VERSION:
					newEntry = new EntryData(e.getSha256(), e.getUrl(), e.getTitle(), e.getAuthor(), e.getDescription(), e.getSourceUrl(), e.getPictureFile(), textComponent.getText());
					break;
				}
				if(entry != null) {
					EntryPanel.this.setEntry(newEntry);
				}
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateEntry();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateEntry();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateEntry();
			}
		});
	}

	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if(e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}
