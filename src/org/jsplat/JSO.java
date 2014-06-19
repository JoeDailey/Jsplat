package org.jsplat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class JSO {
	public static final int TYPE_OBJECT = 0;
	public static final int TYPE_ARRAY = 1;
	public static final int TYPE_DOUBLE = 2;
	public static final int TYPE_BOOLEAN = 3;
	public static final int TYPE_FUNCTION = 4;
	public static final int TYPE_REFERENCE = 5;
	public static final int TYPE_STRING = 6;
	
	private static final Map<Character, Character> enclosers = new HashMap<Character, Character>(){{
		put('{', '}');
		put('[', ']');
		put('\"', '\"');
	}};

	private JSO root;
	private JSO parent;
	private Object data;
	private int type;
	
	
	private boolean can_next = true;
	
	
	private JSO(JSO root, JSO parent){
		this.root = root;
		this.parent = parent;
	}
	
	
	@SuppressWarnings("unchecked")
	public JSO(String json) throws malformedJSON{
		root = this;
		json = json.replace(" ", "").replace("\t", "").replace("\n", "");

		if(json.charAt(0) != '{') throw new malformedJSON(0, "{", json.charAt(0));
		if(json.charAt(json.length()-1) != '}') throw new malformedJSON(json.length()-1, "}", json.charAt(json.length()-1));
		
		json = json.substring(1, json.length()-1);
		
		String[] split = json.split(",");
		for(String s : split){
			System.out.println(s);
		}
		type = TYPE_OBJECT;
		data = new HashMap<String, JSO>();
		Map<String, JSO> thisObject = (Map<String, JSO>)data;
		
		for(String line : split){
			System.out.println(line);
			
		}
		
		for(int i = 0; i < json.length(); i++){
			switch(json.charAt(i)){
				case '"':

					if(!can_next) throw new malformedJSON(i, ",", json.charAt(i));
					i++;//past open quote
					String tag = json.substring(i, json.indexOf('"', i));
					i += tag.length() + 1;//past tag and close quote

					if(json.charAt(i)!=':') throw new malformedJSON(i, ":", json.charAt(i));
					i++;//past colon
					JSO newJSO = new JSO(root, parent);
					i = newJSO(newJSO, root, this, json, i);
					
					
					thisObject.put(tag, newJSO);
	
				break;
				case ',':
					can_next = true;
					i++;
				break;

				case '}':
					return;
			}
		}
	}
	
	public static String[] splitTop(String json){
		List<String> props = new ArrayList<String>();
		Stack<Character> scope = new Stack<Character>();
		json = clean(json);
		if(
			(json.charAt(0) == '{' && json.charAt(json.length()-1) == '}')
			||
			(json.charAt(0) == '[' && json.charAt(json.length()-1) == ']')
		) json = json.substring(1, json.length()-1);
		int quotes = 0;
		for(int i = 0; i < json.length(); i++){
			if(i+1 == json.length()){
				if(scope.empty() || json.charAt(i) == scope.peek()){
					props.add(json);
					break;
				}
			}
			if(json.charAt(i) == '\"')
				quotes++;
			// if the char is an encloser and the top of the stack is not a quote the push
			if( JSO.enclosers.containsKey(json.charAt(i)) && !(!scope.empty() && scope.peek()=='"') )
				scope.push( JSO.enclosers.get(json.charAt(i)) );
			
			else if( JSO.enclosers.containsValue(json.charAt(i)) && scope.peek() == json.charAt(i) )
				scope.pop();
				
			else if( json.charAt(i) == ',' )
				if(scope.empty()){
					props.add(json.substring(0, i));
					json = json.substring(i+1, json.length());
					i = -1;
				}
				
		}
		
		return props.toArray(new String[0]);
	}
	private static String clean(String json){
		return json.replace(" ", "").replace("\t", "").replace("\n", "");
	}
	
	private int newJSO(JSO newJSO, JSO root, JSO parent, String json, int i){
		for(;i < json.length(); i++){
			switch(json.charAt(i)){
				case '{':
					newJSO.type = TYPE_OBJECT;
					i++;
					newJSO.data = new HashMap<String, JSO>();
				break;
				case '"':
					newJSO.type = TYPE_STRING;
					i++;
					newJSO.data = json.substring(i, json.indexOf('"', i));
					i+=((String)newJSO.data).length() + 1;//past close quote
				return i;
			}
		}
		return i;
//		if(json.charAt(0) == '{'){
//			type = TYPE_OBJECT;
//			data = new HashMap<String, JSO>();
//		}
//		Map<String, JSO> newJSOObject = (Map<String, JSO>)data;
		
	}
	
	
	
	
	
//	public Object get(String node);
//	public  void set(String node, String Value);
//	public JSO traverse(String node);
//	public JSO parent();
//	public JSO[] children();
//	public JSO root();
	

	
	
	
//	public Object g(String node){return get(node);};
//	public Object g(){return get("");};
//	public void s(String node, String value){set(node, value);};
//	public void s(String value){set("", value);};
//	public JSO j(String node){return traverse(node);}
//	public JSO p(){return parent();}
//	public JSO[] c(){return children();}
//	public JSO r(){return root();}
}

@SuppressWarnings("serial")
class malformedJSON extends Exception{
	private final int ln;
	private final String expected;
	private final char instead;
	malformedJSON(int ln, String expected, char instead){
		this.ln = ln;
		this.expected = expected;
		this.instead = instead;
	}
	@Override
	public String getMessage() {
		return "Expected \"" + expected + "\", instead found \"" + instead + "\"" + " at index " + ln;
	}
	
}