package controlCenter;

class ApplicationUsage implements Comparable {
	public String name = "";

	public String purpose = "";

	public String accessor = "";

	public String recipient = "";

	private boolean verbose;

	public ApplicationUsage(String name, String purpose, String accessor,
			String recipient, boolean verbose) {
		this(name, purpose, accessor, recipient);
		this.verbose = verbose;
	}

	public ApplicationUsage(String name, String purpose, String accessor,
			String recipient) {
		this.name = name;
		this.purpose = purpose;
		this.accessor = accessor;
		this.recipient = recipient;
		this.verbose = false;
	}

	public ApplicationUsage() {
	}

	public int compareTo(Object object) {
		final ApplicationUsage other = (ApplicationUsage) object;

		int result;

		result = name.compareTo(other.name);
		if (result != 0)
			return result;

		result = purpose.compareTo(other.purpose);
		if (result != 0)
			return result;

		result = accessor.compareTo(other.accessor);
		if (result != 0)
			return result;

		result = recipient.compareTo(other.recipient);
		if (result != 0)
			return result;

		return result;
	}

	// Needed when we want to remove an object from a Vector:
	// vector.remove(applicationUsage);
	public boolean equals(Object object) {
		return (this.compareTo(object) == 0);
	}

	protected ApplicationUsage clone(boolean verbose) {
		final ApplicationUsage object = new ApplicationUsage(name, purpose,
				accessor, recipient, verbose);
		return object;
	}

	public String toString() {
		return verbose ? toStringVerbose() : name;
	}

	public String toStringVerbose() {
		return "[" + name + ", " + purpose + ", " + accessor + ", " + recipient
				+ "]";
	}

	public String toStringDetails() {
		String text = "";
		text += "Application:\t" + name + "\n";
		text += "Purpose:\t" + purpose + "\n";
		text += "Accessor:\t" + accessor + "\n";
		text += "Recipient:\t" + recipient;
		return text;
	}

	public boolean isEmpty() {
		return name.equals("") || purpose.equals("") || accessor.equals("")
				|| recipient.equals("");
	}
}
