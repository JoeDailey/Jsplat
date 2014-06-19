package org.jsplat;

public class test {

	public static void main(String[] args) {
		String json = "{\n\"glossary\": \"title\"\n}";
//		String jsonBlacked = {"glossary":"title"};
		try {
			new JSO(json);
		} catch (malformedJSON e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

}
