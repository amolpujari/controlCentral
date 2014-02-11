package controlCenter;

class Query {
	public String name;

	public String text;

	public String purpose;

	public String accessor;

	public String recipient;

	public String specificRecipient;

	public Integer isolation;

	public String begin;

	public String end;

	public String result = " "
			+ ResourceManager.getResource("lable.no.records");

	public String verdict;

	public String isolationText = "";

	public String toString() {
		// TODO Auto-generated method stub
		return name;
	}

	public Query() {

	}

	protected Object clone() throws CloneNotSupportedException {
		Query clone = new Query();

		clone.name = this.name;
		clone.text = this.text;
		clone.purpose = this.purpose;
		clone.accessor = this.accessor;
		clone.recipient = this.recipient;
		clone.specificRecipient = this.specificRecipient;
		clone.isolation = this.isolation;
		clone.isolationText = this.isolationText;
		clone.begin = this.begin;
		clone.end = this.end;
		clone.result = this.result;
		clone.verdict = this.verdict;

		return clone;
	}

}
