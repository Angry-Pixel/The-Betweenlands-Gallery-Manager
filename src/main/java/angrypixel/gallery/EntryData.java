package angrypixel.gallery;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.codec.digest.DigestUtils;

public class EntryData {
	private String sha256, url, title, author;
	private String description;
	private String localSha256;
	private String sourceUrl;
	private File pictureFile;
	private BufferedImage image;
	private int width = 1, height = 1;
	private String version;

	public EntryData(String sha256, String url, String title, String author, String description, String sourceUrl, File pictureFile, String version) {
		this.sha256 = sha256.toLowerCase();
		this.url = url;
		this.title = title;
		this.author = author;
		this.description = description;
		this.sourceUrl = sourceUrl;
		this.pictureFile = pictureFile;
		this.version = version;
	}

	public String getVersion() {
		return this.version;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
		if(image != null) {
			this.width = image.getWidth();
			this.height = image.getHeight();
		} else {
			this.width = this.height = 1;
		}
	}

	public BufferedImage getImage() {
		return this.image;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public File getPictureFile() {
		return this.pictureFile;
	}

	private void computeLocalPictureSha256() {
		this.localSha256 = null;

		if(this.pictureFile != null && this.pictureFile.exists()) {
			try(FileInputStream fio = new FileInputStream(this.pictureFile)) {
				this.localSha256 = DigestUtils.sha256Hex(fio).toLowerCase();
			} catch(Exception ex) {
				System.out.println(String.format("Failed computing SHA256 hash of gallery picture: %s", this.pictureFile.toString()));
				ex.printStackTrace();
			}
		}
	}

	public String getLocalSha256() {
		if(this.localSha256 == null) {
			this.computeLocalPictureSha256();
		}
		return this.localSha256;
	}

	public String getSha256() {
		return this.sha256;
	}

	public String getUrl() {
		return this.url;
	}

	public String getTitle() {
		return this.title;
	}

	public String getAuthor() {
		return this.author;
	}

	public String getDescription() {
		return this.description;
	}

	public String getSourceUrl() {
		return this.sourceUrl;
	}

	public String validate() {
		if(this.getPictureFile() == null || !this.getPictureFile().exists()) {
			return "no_file";
		}
		if(this.getLocalSha256() == null) {
			return "no_sha";
		}
		if(this.getVersion() == null || this.getVersion().length() == 0) {
			return "no_version";
		}
		if(this.getTitle() == null || this.getTitle().length() == 0) {
			return "no_title";
		}
		if(this.getAuthor() == null || this.getAuthor().length() == 0) {
			return "no_author";
		}
		return null;
	}
}
