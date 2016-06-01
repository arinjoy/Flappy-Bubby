package arinjoy.biswas.com.dailyselfie;

public class SelfieRecord {
	private int Id;
	private String filePath;
	private String name;

	public SelfieRecord() {}

	public int getId() {
		return Id;
	}
	public void setId(int ID) {
		this.Id = ID;
	}
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	@Override
	public String toString() {
		return "SelfieRecord{" +
				"filePath='" + filePath + '\'' +
				", name ='" + name +
				'}';
	}
}
