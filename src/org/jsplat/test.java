package org.jsplat;

public class test {

	public static void main(String[] args) {
//		String json = "{\n\"glossary\": \"title\",\"asdf\": \",\"\n}";
//		String json = "{\n\"glossary\": {},\"asdf\": \",\"\n}";
		String json = "{   \"glossary\":{\"title\":\"example glossary\",\"GlossDiv\":{\"title\":\"S\",\"GlossList\":{\"GlossEntry\":{\"ID\":\"SGML\",\"SortAs\":\"SGML\",\"GlossTerm\":\"Standard Generalized Markup Language\",\"Acronym\":\"SGML\",\"Abbrev\":\"ISO 8879:1986\",\"GlossDef\":{\"para\":\"A meta-markup language, used to create markup languages such as DocBook.\",\"GlossSeeAlso\":[\"GML\",\"XML\"]},\"GlossSee\":\"markup\"}}}}}";
//		String jsonBlacked = {"glossary":"title"};
//		String json = "\"asdfasdf\",\"asdfasdf\",\"asdfasdf\"";
		
		String[] split = JSO.splitTop(json);
		for(String s: split)
			System.out.println(s);
		
		
		
		JSO JSON_OBJECT = null;
		try {
			JSON_OBJECT = new JSO(json);
		} catch (malformedJSON e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
//		JSON_OBJECT.get("glossary.title");
		
	}

}
