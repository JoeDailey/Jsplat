package org.jsplat;

public class test {

	public static void main(String[] args) throws NodeNotFound {
		String json = "{   \"glossary\":{\"title\":\"example glossary\",\"GlossDiv\":{\"title\":\"S\",\"GlossList\":{\"GlossEntry\":{\"ID\":\"SGML\",\"SortAs\":\"SGML\",\"GlossTerm\":\"Standard Generalized Markup Language\",\"Acronym\":\"SGML\",\"Abbrev\":\"ISO 8879:1986\",\"GlossDef\":{\"para\":\"A meta-markup language, used to create markup languages such as DocBook.\",\"GlossSeeAlso\":[\"GML\",\"XML\"]},\"GlossSee\":\"markup\"}}}}}";

		
		
		JSO JSON_OBJECT = null;
		try {
			JSON_OBJECT = new JSO(json);
		} catch (malformedJSON e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		System.out.println(JSON_OBJECT.get("glossary.title"));
		System.out.println(JSON_OBJECT.o("glossary.title").get());
		System.out.println(JSON_OBJECT.o("glossary").o("title").get());
		System.out.println(""+JSON_OBJECT.o("glossary").s("title", "notitle"));
		System.out.println(JSON_OBJECT.o("glossary"));
		JSO[] arr = JSON_OBJECT.o("glossary").children();
		for(JSO c : arr){
			System.out.println(c);	
		}
		System.out.println(JSON_OBJECT.find("GlossDiv").find("GlossDiv"));
//		System.out.println(""+JSON_OBJECT.o("glossary").s("GlossDiv", "noList"));
		
		System.out.println(JSON_OBJECT.stringify());
		
		 
	}

}
