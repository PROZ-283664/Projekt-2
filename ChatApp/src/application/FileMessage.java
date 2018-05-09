package application;

import java.io.File;
import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;

public class FileMessage extends Message implements Serializable {
	private static final long serialVersionUID = -8405923816747510263L;
	private final String fileName;
	private final long fileSize;

	public FileMessage(String fileName, long fileSize, String from, Integer hash) {
		super(from, hash);
		this.fileName = fileName;
		this.fileSize = fileSize;
	}

	public FileMessage(String fileName, Integer fileSize, String from) {
		this(fileName, fileSize, from, fileName.hashCode());
	}

	public String getFileName() {
		return fileName;
	}

	public long getFileSize() {
		return fileSize;
	}

	public Element toHTML(Boolean isSender) {
		String msgClass = isSender ? "sent" : "replies";
		Element wrapper = new Element("li").attr("class", msgClass).attr("id", getUID().toString());
		Element image = new Element("img").attr("class", "avatar").attr("src",
				new File("res/harveyspecter.png").toURI().toString());
		if (!isSender) {
			image.attr("src", new File("res/mikeross.png").toURI().toString());
			new Element("span").attr("class", "author").append(getSender()).appendTo(wrapper);
		}
		image.appendTo(wrapper);
		Element attachment = new Element("img").attr("class", "attachment").attr("src",
				new File("res/attach.png").toURI().toString());

		Element message_div = new Element("div").attr("class", "message").attr("style", "opacity: 0.4")
				.appendTo(wrapper);
		new Element("div").attr("class", "processing").appendTo(message_div);
		new Element("div").attr("class", "content").append(fileName).prependChild(attachment).appendTo(message_div);

		return wrapper;
	}

	@Override
	public String toString() {
		String jsonString = null;
		try {
			JSONObject json = new JSONObject().put("type", "FileMessage").put("fileName", fileName).put("size",
					fileSize);
			JSONObject superJson = new JSONObject(super.toString());
			for (String key : JSONObject.getNames(superJson)) {
				json.put(key, superJson.get(key));
			}
			jsonString = json.toString();

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonString;
	}
}
