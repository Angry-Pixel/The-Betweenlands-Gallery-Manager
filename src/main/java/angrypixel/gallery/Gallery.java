package angrypixel.gallery;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public final class Gallery {
	public static final Gson GSON = new Gson();
	public static final Type STRING_ARRAY_TYPE = new TypeToken<String[]>() {
	}.getType();

	public static final Gallery INSTANCE = new Gallery();

	private List<EntryData> entries = new ArrayList<>();

	private Gallery() {

	}

	public boolean replaceEntry(EntryData oldEntry, EntryData newEntry) {
		int index = this.entries.indexOf(oldEntry);
		if(index >= 0) {
			this.entries.remove(index);
			this.entries.add(index, newEntry);
			return true;
		}
		return false;
	}

	public boolean removeEntry(EntryData entry) {
		return this.entries.remove(entry);
	}

	public void addEntry(EntryData entry) {
		this.entries.add(entry);
	}

	public List<EntryData> getEntries() {
		return this.entries;
	}

	public void clear() {
		this.entries.clear();
	}

	public void downloadGallery(File folder, String baseUrl) {
		try {
			this.clear();

			System.out.println("Downloading gallery");

			URL url = new URL(baseUrl + "index.json");
			HttpURLConnection request = null;
			try {
				request = Gallery.this.createHttpConnection(url, null);
				request.setRequestProperty("Content-Type", "application/json; charset=utf-8");
				request.connect();

				if(request.getResponseCode() == HttpURLConnection.HTTP_OK) {
					JsonParser parser = new JsonParser();
					final JsonElement jsonElement = parser.parse(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8));
					this.loadGallery(folder, jsonElement, null, null);
				}
			} finally {
				if(request != null) {
					request.disconnect();
				}
			}
		} catch(Exception ex) {
			System.out.println("Failed downloading gallery data");
			ex.printStackTrace();
		}
	}

	public void loadGallery(File folder, File galleryDir) {
		try {
			this.clear();

			System.out.println("Loading gallery");

			try(InputStream is = new FileInputStream(new File(galleryDir, "index.json"))) {
				JsonParser parser = new JsonParser();
				final JsonElement jsonElement = parser.parse(new InputStreamReader(is, StandardCharsets.UTF_8));
				this.loadGallery(folder, jsonElement, null, galleryDir);
			}

		} catch(Exception ex) {
			System.out.println("Failed loading gallery data");
			ex.printStackTrace();
		}
	}

	private HttpURLConnection createHttpConnection(URL url, Proxy proxy) throws IOException {
		HttpURLConnection request = null;
		if(proxy != null) {
			request = (HttpURLConnection) url.openConnection(proxy);
		} else {
			request = (HttpURLConnection) url.openConnection();
		}
		request.setDoInput(true);
		request.setDoOutput(false);
		return request;
	}

	private void loadGallery(File folder, JsonElement json, Proxy proxy, File galleryDir) {
		this.entries = this.parseIndex(folder, json);

		for(EntryData entry : this.entries) {
			try {
				if(this.loadPicture(folder, entry, proxy, galleryDir)) {
					String localSha256 = entry.getLocalSha256();

					if(localSha256 != null && !entry.getSha256().equals(localSha256)) {
						System.out.println("Local/Downloaded gallery picture '" + entry.getUrl() + "' SHA256 hash does not match (Expected: " + entry.getSha256() + " Got: " + localSha256 + ")! Please report this to the mod authors.");
					}

					if(entry.getPictureFile() != null) {
						try(FileInputStream fio = new FileInputStream(entry.getPictureFile())) {
							BufferedImage image = readBufferedImage(fio);
							if(image != null && image.getWidth() > 0 && image.getHeight() > 0) {
								entry.setImage(image);
							} else {
								System.out.println("Failed loading gallery picture '" + entry.getUrl() + "' from file " + entry.getPictureFile());
							}
						}
					} else {
						System.out.println("Failed loading gallery picture '" + entry.getUrl() + "' from file");
					}
				} else {
					System.out.println("Failed downloading gallery picture '" + entry.getUrl() + "'");
				}
			} catch(Exception ex) {
				System.out.println("Failed downloading gallery picture '" + entry.getUrl() + "'");
				ex.printStackTrace();
			}
		}
	}

	private File getPictureFile(File folder, String url) {
		return new File(folder, url.substring(url.lastIndexOf('/') + 1));
	}

	private boolean loadPicture(File folder, EntryData entry, Proxy proxy, File galleryDir) throws IOException {
		if(galleryDir != null) {
			File picFile = this.getPictureFile(new File(galleryDir, "gallery/"), entry.getUrl());
			if(picFile.exists()) {
				Files.copy(picFile.toPath(), this.getPictureFile(folder, entry.getUrl()).toPath(), StandardCopyOption.REPLACE_EXISTING);
				return true;
			} else {
				System.out.println("Could not find gallery picture '" + entry.getSha256() + "'/'" + entry.getUrl() + "'/'" + picFile + "'");
			}
		}

		System.out.println("Downloading gallery picture '" + entry.getSha256() + "'/'" + entry.getUrl() + "'");

		URL url = new URL(entry.getUrl());
		HttpURLConnection request = null;
		try {
			request = Gallery.this.createHttpConnection(url, proxy);
			request.connect();

			if(request.getResponseCode() == HttpURLConnection.HTTP_OK) {
				Files.copy(request.getInputStream(), this.getPictureFile(folder, entry.getUrl()).toPath(), StandardCopyOption.REPLACE_EXISTING);
				return true;
			}
		} finally {
			if(request != null) {
				request.disconnect();
			}
		}

		return false;
	}

	private List<EntryData> parseIndex(File folder, JsonElement json) {
		List<EntryData> entries = new ArrayList<>();

		if(json.isJsonArray()) {
			JsonArray jsonArr = json.getAsJsonArray();
			for(int i = 0; i < jsonArr.size(); i++) {
				try {
					JsonObject element = jsonArr.get(i).getAsJsonObject();
					if(element.has("versions")) {
						String[] versions = GSON.fromJson(element.get("versions"), STRING_ARRAY_TYPE);

						JsonArray index = element.get("index").getAsJsonArray();
						for(int j = 0; j < index.size(); j++) {
							try {
								JsonObject entryJson = index.get(j).getAsJsonObject();
								for(String version : versions) {
									EntryData entry = this.parseEntry(folder, entryJson, version);
									entries.add(entry);
								}
							} catch(Exception ex) {
								System.out.println("Failed parsing gallery index entry: " + j);
								ex.printStackTrace();
							}
						}
					} else {
						System.out.println("Index is missing versions attribute");
					}
				} catch(Exception ex) {
					System.out.println("Failed parsing gallery version entry: " + i);
					ex.printStackTrace();
				}
			}
		}

		return entries;
	}

	private EntryData parseEntry(File folder, JsonObject json, String version) {
		String sha256 = getString(json, "sha256");
		String url = getString(json, "url");
		String title = getString(json, "title");
		String author = getString(json, "author");
		String description = json.has("description") ? getString(json, "description") : null;
		String sourceUrl = json.has("source_url") ? getString(json, "source_url") : null;
		return new EntryData(sha256, url, title, author, description, sourceUrl, this.getPictureFile(folder, url), version);
	}

	public String writeIndex(String url) {
		Map<String, List<EntryData>> versionedEntries = new HashMap<>();

		for(EntryData entry : this.entries) {
			if(entry.validate() == null) {
				List<EntryData> ventries = versionedEntries.get(entry.getVersion());
				if(ventries == null) {
					versionedEntries.put(entry.getVersion(), ventries = new ArrayList<>());
				}
				ventries.add(entry);
			}
		}

		JsonArray galleries = new JsonArray();

		for(Entry<String, List<EntryData>> pair : versionedEntries.entrySet()) {
			String version = pair.getKey();
			List<EntryData> entries = pair.getValue();

			JsonObject gallery = new JsonObject();

			JsonArray galleryVersions = new JsonArray();
			galleryVersions.add(version);

			gallery.add("versions", galleryVersions);

			JsonArray index = new JsonArray();

			for(EntryData entry : entries) {
				JsonObject json = new JsonObject();
				json.add("sha256", new JsonPrimitive(entry.getLocalSha256()));
				json.add("url", new JsonPrimitive(url + entry.getPictureFile().getName()));
				json.add("title", new JsonPrimitive(entry.getTitle()));
				json.add("author", new JsonPrimitive(entry.getAuthor()));
				if(entry.getDescription() != null && entry.getDescription().length() > 0) {
					json.add("description", new JsonPrimitive(entry.getDescription()));
				}
				if(entry.getSourceUrl() != null && entry.getSourceUrl().length() > 0) {
					json.add("source_url", new JsonPrimitive(entry.getSourceUrl()));
				}
				index.add(json);
			}

			gallery.add("index", index);

			galleries.add(gallery);
		}

		return galleries.size() == 0 ? "" : new GsonBuilder().setPrettyPrinting().create().toJson(galleries);
	}

	private static String getString(JsonElement json, String memberName) {
		if(json.isJsonPrimitive()) {
			return json.getAsString();
		} else {
			throw new JsonSyntaxException("Expected " + memberName + " to be a string, was " + json.toString());
		}
	}

	private static String getString(JsonObject json, String memberName) {
		if(json.has(memberName)) {
			return getString(json.get(memberName), memberName);
		} else {
			throw new JsonSyntaxException("Missing " + memberName + ", expected to find a string");
		}
	}

	public static BufferedImage readBufferedImage(InputStream imageStream) throws IOException {
		BufferedImage bufferedimage;

		try {
			bufferedimage = ImageIO.read(imageStream);
		} finally {
			imageStream.close();
		}

		return bufferedimage;
	}
}
