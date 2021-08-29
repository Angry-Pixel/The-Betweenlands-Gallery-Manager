package angrypixel.gallery;

import java.awt.GridBagConstraints;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;

public class Manager {
	private final Window window;

	public File tempFolder;

	private int maxImageSize;

	private String url;

	private String version;

	public Manager(Window window) {
		this.window = window;

		this.setVersion("1.0.0");
		this.setMaxImageSize(512);
		this.setURL("https://raw.githubusercontent.com/Angry-Pixel/The-Betweenlands/online_picture_gallery/");

		try {
			this.tempFolder = Files.createTempDirectory("betweenlands_gallery_manager").toFile();
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					FileUtils.deleteDirectory(Manager.this.tempFolder);
				} catch(IOException e) {
					e.printStackTrace();
				}
			}));
		} catch(IOException e) {
			System.out.println("Unable to create temporary folder");
			e.printStackTrace();
		}
	}

	public void setVersion(String version) {
		if(!version.equals(this.version)) {
			this.version = version;

			SwingUtilities.invokeLater(() -> {
				this.window.lblVersion.setText(String.format("Version: %s", version));

				this.removeAllPanels();

				for(EntryData entry : Gallery.INSTANCE.getEntries()) {
					if(this.version.equals(entry.getVersion())) {
						this.addEntryPanel(new EntryPanel(this, entry));
					}
				}
			});
		}
	}

	public void setMaxImageSize(int size) {
		if(this.maxImageSize != size) {
			this.maxImageSize = size;
			SwingUtilities.invokeLater(() -> this.window.lblMaxSize.setText(String.format("Max. picture size: %d", size)));
		}
	}

	public void setURL(String url) {
		if(!url.equals(this.url)) {
			this.url = url;
			SwingUtilities.invokeLater(() -> {
				this.window.lblUrl.setText(String.format("URL: %s", url));
				this.window.lblUrl.setToolTipText(url);
			});
		}
	}

	public String getVersion() {
		return this.version;
	}

	public int getMaxImageSize() {
		return this.maxImageSize;
	}

	public String getURL() {
		return this.url;
	}

	public void removeAllPanels() {
		this.window.mainPanel.removeAll();
		this.window.mainPanel.revalidate();
		this.window.mainPanel.repaint();
	}

	public void removeEntryPanel(EntryPanel panel) {
		this.window.mainPanel.remove(panel);
		this.window.mainPanel.revalidate();
		this.window.mainPanel.repaint();
	}

	public void addEntryPanel(EntryPanel panel) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx = 1;
		gbc.weighty = 1;
		this.window.mainPanel.add(panel, gbc, 0);
		this.window.mainPanel.revalidate();
		this.window.mainPanel.repaint();
	}
}
