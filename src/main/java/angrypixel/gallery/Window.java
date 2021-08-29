package angrypixel.gallery;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.apache.commons.io.FileUtils;

public class Window extends JFrame {
	private static final long serialVersionUID = 1L;

	public JPanel contentPane;
	public JPanel mainPanel;
	private JMenuItem mntmDownload;
	private JMenuItem mntmExport;
	private JMenuItem mntmNewEntry;

	private Manager manager;
	private JLabel lblStatus;
	private JPanel panel;
	private JMenuItem mntmImport;
	private JMenu mnSettings;
	private JMenuItem mntmChangeUrl;
	private JMenuItem mntmChangeMaxSize;
	public JLabel lblMaxSize;
	public JLabel lblUrl;
	private JPanel panel_1;
	public JLabel lblVersion;
	private JMenuItem mntmChangeVersion;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					try {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					} catch(Exception ex) {
					}

					Window frame = new Window();
					frame.setVisible(true);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Window() {
		setMinimumSize(new Dimension(497, 262));

		setTitle("Betweenlands Gallery Manager");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 509, 234);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mntmNewEntry = new JMenuItem("New Entry");
		mnFile.add(mntmNewEntry);

		mntmNewEntry.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(() -> {
					EntryData entry = new EntryData("", "", "", "", "", "", null, Window.this.manager.getVersion());
					Gallery.INSTANCE.addEntry(entry);
					Window.this.manager.addEntryPanel(new EntryPanel(Window.this.manager, entry));
				});
			}
		});

		mntmDownload = new JMenuItem("Download");
		mnFile.add(mntmDownload);

		mntmDownload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(() -> {
					mnFile.setEnabled(false);

					Window.this.manager.removeAllPanels();
					Gallery.INSTANCE.clear();

					Thread downloaderThread = new Thread() {
						@Override
						public void run() {
							try {
								FileUtils.cleanDirectory(Window.this.manager.tempFolder);
							} catch(IOException ex) {
								SwingUtilities.invokeLater(() -> {
									JOptionPane.showMessageDialog(Window.this, "Unable to clear gallery", "Error", JOptionPane.ERROR_MESSAGE);
									ex.printStackTrace();
								});
								return;
							}

							Gallery.INSTANCE.downloadGallery(Window.this.manager.tempFolder, Window.this.manager.getURL());

							SwingUtilities.invokeLater(() -> {
								for(EntryData entry : Gallery.INSTANCE.getEntries()) {
									if(Window.this.manager.getVersion().equals(entry.getVersion())) {
										Window.this.manager.addEntryPanel(new EntryPanel(Window.this.manager, entry));
									}
								}

								Window.this.pack();
							});
						}
					};

					downloaderThread.start();

					Thread statusThread = new Thread() {
						int i = 0;

						@Override
						public void run() {
							while(downloaderThread.isAlive()) {
								try {
									String statusText = "Downloading";
									for(int j = 0; j < 1 + (i % 3); j++) {
										statusText += ".";
									}
									final String finalStatusText = statusText;
									SwingUtilities.invokeLater(() -> {
										lblStatus.setText(finalStatusText);
									});
									i++;
									downloaderThread.join(1000L);
								} catch(InterruptedException e) {
									break;
								}
							}

							SwingUtilities.invokeLater(() -> {
								lblStatus.setText("");
								mnFile.setEnabled(true);
							});
						}
					};

					statusThread.start();
				});
			}
		});

		mntmImport = new JMenuItem("Import");
		mnFile.add(mntmImport);

		mntmImport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(() -> {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Choose gallery folder");
					fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int result = fileChooser.showOpenDialog(Window.this);
					if(result != JFileChooser.APPROVE_OPTION) {
						return;
					}

					File dir = fileChooser.getSelectedFile();

					if(dir.exists()) {
						if(!new File(dir, "index.json").exists()) {
							JOptionPane.showMessageDialog(Window.this, "Unable to find index.json", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}

						Window.this.manager.removeAllPanels();
						Gallery.INSTANCE.clear();

						try {
							FileUtils.cleanDirectory(Window.this.manager.tempFolder);
						} catch(IOException ex) {
							SwingUtilities.invokeLater(() -> {
								JOptionPane.showMessageDialog(Window.this, "Unable to clear gallery", "Error", JOptionPane.ERROR_MESSAGE);
								ex.printStackTrace();
							});
							return;
						}

						Gallery.INSTANCE.loadGallery(Window.this.manager.tempFolder, dir);

						for(EntryData entry : Gallery.INSTANCE.getEntries()) {
							if(Window.this.manager.getVersion().equals(entry.getVersion())) {
								Window.this.manager.addEntryPanel(new EntryPanel(Window.this.manager, entry));
							}
						}

						Window.this.pack();
					}
				});
			}
		});

		mntmExport = new JMenuItem("Export");
		mnFile.add(mntmExport);

		mnSettings = new JMenu("Settings");
		menuBar.add(mnSettings);

		mntmChangeVersion = new JMenuItem("Change version");
		mnSettings.add(mntmChangeVersion);

		mntmChangeVersion.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(() -> {
					String version = JOptionPane.showInputDialog(Window.this, "Enter the version", Window.this.manager.getVersion());
					if(version != null) {
						Window.this.manager.setVersion(version);
					}
				});
			}
		});

		mntmChangeUrl = new JMenuItem("Change URL");
		mnSettings.add(mntmChangeUrl);

		mntmChangeUrl.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(() -> {
					String url = JOptionPane.showInputDialog(Window.this, "Enter the URL", Window.this.manager.getURL());
					if(url != null) {
						Window.this.manager.setURL(url);
					}
				});
			}
		});

		mntmChangeMaxSize = new JMenuItem("Change max. picture size");
		mnSettings.add(mntmChangeMaxSize);

		mntmChangeMaxSize.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(() -> {
					String input = JOptionPane.showInputDialog(Window.this, "Enter the max. picture size", Window.this.manager.getMaxImageSize());
					if(input != null) {
						try {
							Window.this.manager.setMaxImageSize(Integer.parseInt(input));
						} catch(NumberFormatException ex) {
							JOptionPane.showMessageDialog(Window.this, "Invalid number", "Error", JOptionPane.WARNING_MESSAGE);
						}
					}
				});
			}
		});

		panel = new JPanel();
		menuBar.add(panel);
		panel.setLayout(null);

		lblStatus = new JLabel("");
		lblStatus.setBounds(10, 3, 371, 14);
		panel.add(lblStatus);
		lblStatus.setHorizontalAlignment(SwingConstants.LEFT);

		mntmExport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogTitle("Choose gallery folder");
				fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int result = fileChooser.showOpenDialog(Window.this);
				if(result != JFileChooser.APPROVE_OPTION) {
					return;
				}

				File dir = fileChooser.getSelectedFile();

				if(dir.exists()) {
					boolean valid = true;

					for(int i = 0; i < Gallery.INSTANCE.getEntries().size(); i++) {
						EntryData entry = Gallery.INSTANCE.getEntries().get(i);

						String title = entry.getTitle();
						if(title == null || title.length() == 0) {
							title = "#" + (i + 1);
						}

						String validation = entry.validate();
						if(validation != null) {
							switch(validation) {
							case ("no_file"):
								JOptionPane.showMessageDialog(Window.this, "Unable to find picture of entry '" + title + "'", "Error", JOptionPane.ERROR_MESSAGE);
								break;
							case ("no_sha"):
								JOptionPane.showMessageDialog(Window.this, "Unable to compute hash of entry '" + title + "'", "Error", JOptionPane.ERROR_MESSAGE);
								break;
							case ("no_version"):
								JOptionPane.showMessageDialog(Window.this, "Entry '" + title + "' does not have a version", "Error", JOptionPane.ERROR_MESSAGE);
								break;
							case ("no_title"):
								JOptionPane.showMessageDialog(Window.this, "Entry '" + title + "' does not have a title", "Error", JOptionPane.ERROR_MESSAGE);
								break;
							case ("no_author"):
								JOptionPane.showMessageDialog(Window.this, "Entry '" + title + "' does not have an author", "Error", JOptionPane.ERROR_MESSAGE);
								break;
							default:
								JOptionPane.showMessageDialog(Window.this, "Entry '" + title + "' is invalid", "Error", JOptionPane.ERROR_MESSAGE);
								break;
							}

							valid = false;
							break;
						}
					}

					if(valid) {
						File index = new File(dir, "index.json");

						if(index.exists()) {
							int dialogResult = JOptionPane.showConfirmDialog(Window.this, "Index already exists, replace?", "Replace?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
							if(dialogResult == JOptionPane.YES_OPTION) {
								try {
									Files.delete(index.toPath());
								} catch(IOException ex) {
									JOptionPane.showMessageDialog(Window.this, "Unable to delete index", "Error", JOptionPane.ERROR_MESSAGE);
									ex.printStackTrace();
									return;
								}

								if(index.exists()) {
									JOptionPane.showMessageDialog(Window.this, "Unable to delete index", "Error", JOptionPane.ERROR_MESSAGE);
									return;
								}
							} else {
								return;
							}
						}

						File gallery = new File(dir, "gallery");

						if(gallery.exists()) {
							int dialogResult = JOptionPane.showConfirmDialog(Window.this, "Gallery already exists, replace?", "Replace?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
							if(dialogResult == JOptionPane.YES_OPTION) {
								try {
									FileUtils.deleteDirectory(gallery);
								} catch(IOException ex) {
									JOptionPane.showMessageDialog(Window.this, "Unable to delete gallery", "Error", JOptionPane.ERROR_MESSAGE);
									ex.printStackTrace();
									return;
								}

								if(gallery.exists()) {
									JOptionPane.showMessageDialog(Window.this, "Unable to delete gallery", "Error", JOptionPane.ERROR_MESSAGE);
									return;
								}
							} else {
								return;
							}
						}

						gallery.mkdir();

						String indexStr = Gallery.INSTANCE.writeIndex(Window.this.manager.getURL() + "gallery/");

						try {
							FileUtils.writeStringToFile(index, indexStr, StandardCharsets.UTF_8, false);

							for(EntryData entry : Gallery.INSTANCE.getEntries()) {
								File picFile = entry.getPictureFile();
								FileUtils.copyFile(picFile, new File(gallery, picFile.getName()));
							}
						} catch(IOException ex) {
							JOptionPane.showMessageDialog(Window.this, "Unable to export files", "Error", JOptionPane.ERROR_MESSAGE);
							ex.printStackTrace();
							return;
						}

						JOptionPane.showMessageDialog(Window.this, "Export complete!", "", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
		});

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 5));
		setContentPane(contentPane);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getVerticalScrollBar().setUnitIncrement(50);
		contentPane.add(scrollPane, BorderLayout.CENTER);

		mainPanel = new JPanel();
		scrollPane.setViewportView(mainPanel);
		GridBagLayout gbl_mainPanel = new GridBagLayout();
		gbl_mainPanel.columnWidths = new int[] { 0 };
		gbl_mainPanel.rowHeights = new int[] { 0 };
		gbl_mainPanel.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_mainPanel.rowWeights = new double[] { Double.MIN_VALUE };
		mainPanel.setLayout(gbl_mainPanel);

		panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.SOUTH);
		panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_1.setLayout(new GridLayout(0, 3, 0, 0));

		lblVersion = new JLabel("Version:");
		panel_1.add(lblVersion);

		lblMaxSize = new JLabel("Max. picture size:");
		panel_1.add(lblMaxSize);

		lblUrl = new JLabel("URL:");
		panel_1.add(lblUrl);

		this.manager = new Manager(this);

		this.pack();
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension dim = super.getPreferredSize();
		return new Dimension((int) Math.min(dim.getWidth(), 1200), (int) Math.min(dim.getHeight(), 1000));
	}
}