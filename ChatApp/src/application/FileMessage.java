package application;

import java.io.File;
import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;

public class FileMessage extends Message implements Serializable {
	private static final long serialVersionUID = -8405923816747510263L;
	private final String fileName;

	public FileMessage(String fileName, String from, Integer hash) {
		super(from, hash);
		this.fileName = fileName;
	}

	public FileMessage(String fileName, String from) {
		this(fileName, from, fileName.hashCode());
	}

	public String getFileName() {
		return fileName;
	}

	public Element toHTML(Boolean isSender) {
		String msgClass = isSender ? "sent" : "replies";
		Element wrapper = new Element("li").attr("class", msgClass);
		Element image = new Element("img").attr("class", "avatar").attr("src",
				new File("res/harveyspecter.png").toURI().toString());
		if (!isSender) {
			image.attr("src", new File("res/mikeross.png").toURI().toString());
			Element author = new Element("span").attr("class", "author").append(getSender()).appendTo(wrapper);
		}
		image.appendTo(wrapper);
		Element attachment = new Element("img").attr("class", "attachment").attr("src",
				new File("res/attach.png").toURI().toString());
		Element content = new Element("p").append(fileName).prependChild(attachment).appendTo(wrapper);

		return wrapper;
	}

	@Override
	public String toString() {
		String jsonString = null;
		try {
			JSONObject json = new JSONObject().put("type", "FileMessage").put("fileName", fileName);
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
