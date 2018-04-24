package application;

import java.io.File;
import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;

public class TextMessage extends Message implements Serializable {
	private static final long serialVersionUID = -3616781498844645993L;
	private final String message;

	public TextMessage(String msg, String from) {
		super(from);
		this.message = msg;
	}

	public String getMessage() {
		return this.message;
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
		Element content = new Element("p").append(message).appendTo(wrapper);

		return wrapper;
	}

	@Override
	public String toString() {
		String jsonString = null;
		try {
			jsonString = new JSONObject().put("type", "TextMessage").put("message", message).put("sender", getSender())
					.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonString;
	}

}