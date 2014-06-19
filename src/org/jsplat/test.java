package org.jsplat;

public class test {

	public static void main(String[] args) {
		String json = "{\n\"glossary\": \"title\",\"asdf\": \",\"\n}";
//		String jsonBlacked = {"glossary":"title"};
		
		String[] split = JSO.splitTop(json);
		for(String s: split)
			System.out.println(s);
		
		
		
//		JSO JSON_OBJECT = null;
//		try {
//			JSON_OBJECT = new JSO(json);
//		} catch (malformedJSON e) {
//			System.out.println(e.getMessage());
//			e.printStackTrace();
//		}
//		System.out.println("wat");
	}

}
