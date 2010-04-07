package net.innig.macker.event;

/**
 * @author Paul Cantrell
 */
public class ListenerException extends Exception {

	private static final long serialVersionUID = 203201336865917546L;
	
	private final MackerEventListener listener;

	public ListenerException(final MackerEventListener listener, final String message) {
		super(createMessage(listener, message));
		this.listener = listener;
	}

	public ListenerException(final MackerEventListener listener, final String message, final Throwable cause) {
		super(createMessage(listener, message), cause);
		this.listener = listener;
	}

	public MackerEventListener getListener() {
		return this.listener;
	}

	private static String createMessage(final MackerEventListener listener, final String message) {
		return "Aborted by " + listener + ": " + message;
	}
}
