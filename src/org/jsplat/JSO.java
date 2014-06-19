package org.jsplat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

public class JSO {
	public static final int TYPE_OBJECT = 0;
	public static final int TYPE_ARRAY = 1;
	public static final int TYPE_NUMBER = 2;
	public static final int TYPE_BOOLEAN = 3;
	public static final int TYPE_FUNCTION = 4;
//	public static final int TYPE_REFERENCE = 5;
	public static final int TYPE_STRING = 6;
	public static final int TYPE_UNDEFINED = 7;
	public static final int TYPE_NULL = 8;
	
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
		this(TYPE_OBJECT, null, json);
		this.root = this;
	}
	@SuppressWarnings("unchecked")
	public JSO(int TYPE, JSO root, String json) throws malformedJSON{
		this.root = root;
		this.type = TYPE_OBJECT;
		
		switch(TYPE){
			case TYPE_OBJECT:
				this.data = new HashMap<String, JSO>();
				
				Map<String, JSO> me = (Map<String, JSO>)this.data;
				
				for(String prop : splitTop(json)){
					String[] part = prop.split(":", 2);
					if(part.length!=2) throw new malformedJSON("tag and property separated by a :", prop);
					if(part[0].charAt(0) == '"') part[0] = part[0].substring(1, part[0].length());
					if(part[0].charAt(part[0].length()-1) == '"') part[0] = part[0].substring(0, part[0].length()-1);

					switch(part[1].charAt(0)){
						case '\"':	//is String
							me.put(part[0], new JSO(TYPE_STRING, root, part[1]));
						break;
						case '{':	//is object
							me.put(part[0], new JSO(TYPE_OBJECT, root, part[1]));
						break;
						case '[':	//is array	
							me.put(part[0], new JSO(TYPE_ARRAY, root, part[1]));
						break;
						default:
							if( part[1].equals("undefined") || part[1].equals("UNDEFINED") )//is undefined
								me.put(part[0], new JSO(TYPE_UNDEFINED, root, part[1]));
							else if( part[1].equals("null") || part[1].equals("NULL") )	//is null
								me.put(part[0], new JSO(TYPE_NULL, root, part[1]));
							else if( part[1].equals("true") || part[1].equals("TRUE")
									||
									part[1].equals("false") || part[1].equals("FALSE") )//is bool
								me.put(part[0], new JSO(TYPE_BOOLEAN, root, part[1]));
							else if( part[1].contains("function") )	//is function
								me.put(part[0], new JSO(TYPE_FUNCTION, root, part[1]));
							else{
								try{  
								    Double.parseDouble(part[1]);
								    me.put(part[0], new JSO(TYPE_NUMBER, root, part[1]));
								}catch(Exception e){
									throw new malformedJSON("type of STRING, NUMBER, NULL, TRUE, FALSE, OBJECT, ARRAY, or UNDEFINED", part[1]);  
								}
							}
						break;
					}
				}
			break;
			case TYPE_ARRAY:
				this.data = new HashMap<String, JSO>();
				
				Map<String, JSO> meArr = (Map<String, JSO>)this.data;
				
				int propCount = 0;
				
				for(String prop : splitTop(json)){
					switch(prop.charAt(0)){
						case '\"':	//is String
							meArr.put(""+propCount, new JSO(TYPE_STRING, root, prop));
						break;
						case '{':	//is object
							meArr.put(""+propCount, new JSO(TYPE_OBJECT, root, prop));
						break;
						case '[':	//is array	
							meArr.put(""+propCount, new JSO(TYPE_ARRAY, root, prop));
						break;
						default:
							if( prop.equals("undefined") || prop.equals("UNDEFINED") )//is undefined
								meArr.put(""+propCount, new JSO(TYPE_UNDEFINED, root, prop));
							else if( prop.equals("null") || prop.equals("NULL") )	//is null
								meArr.put(""+propCount, new JSO(TYPE_NULL, root, prop));
							else if( prop.equals("true") || prop.equals("TRUE")
									||
									prop.equals("false") || prop.equals("FALSE") )//is bool
								meArr.put(""+propCount, new JSO(TYPE_BOOLEAN, root, prop));
							else if( prop.contains("function") )	//is function
								meArr.put(""+propCount, new JSO(TYPE_FUNCTION, root, prop));
							else{
								try{  
								    Double.parseDouble(prop);
								    meArr.put(""+propCount, new JSO(TYPE_NUMBER, root, prop));
								}catch(Exception e){
									throw new malformedJSON("type of STRING, NUMBER, NULL, TRUE, FALSE, OBJECT, ARRAY, or UNDEFINED", prop);  
								}
							}
						break;
					}
				propCount++;
				}
			break;
			case TYPE_STRING:
				this.data = json.substring(1, json.length()-1);
			break;
			case TYPE_NUMBER:
				this.data = Double.parseDouble(json.substring(1, json.length()));
			break;
			case TYPE_BOOLEAN:
				if( json.toLowerCase().equals("true") )
					data = true;
				else
					data = false;
			break;
			default:
				this.data = json;
			break;
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

		for(int i = 0; i < json.length(); i++){
			if(i+1 == json.length()){
				if(scope.empty() || json.charAt(i) == scope.peek()){
					props.add(json);
					break;
				}
			}
			// if the char is an encloser and the top of the stack is not a quote the push
			if( JSO.enclosers.containsKey(json.charAt(i)) && !(!scope.empty() && scope.peek()=='"') )
				//push the counter encloser
				scope.push( JSO.enclosers.get(json.charAt(i)) );
			
			//if the char is a counter encloser and equals the top of the stack
			else if( JSO.enclosers.containsValue(json.charAt(i)) && scope.peek() == json.charAt(i) )
				scope.pop();
			
			//if the char is a prop separator
			else if( json.charAt(i) == ',' )
				//and the scope is empty (at top level)
				if(scope.empty()){
					//add the prop to the list and shorten json/reset count for pre-increment 
					props.add(json.substring(0, i));
					json = json.substring(i+1, json.length());
					i = -1;
				}
				
		}
		
		return props.toArray(new String[0]);
	}
	private static String clean(String json){
		Scanner s = new Scanner(json);
		json = "";
		while(s.hasNext()){
			json += s.findInLine("[^\"\\s]+|\"(\\\\.|[^\\\\\"])*\"");
		}
		return json.replace("\t", "").replace("\n", "");
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
	
	
	
	
	
//	public Object get(String node){
//		int index = 0;
//		String[] route = node.split("\\.");
//		if(index route.length )
//		return null;
//	}
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
	private final String expected;
	private final String instead;
	malformedJSON(String expected, String instead){
		this.expected = expected;
		this.instead = instead;
	}
	malformedJSON(String expected, char instead){
		this.expected = expected;
		this.instead = instead + "";
	}
	@Override
	public String getMessage() {
		return "Expected " + expected + ", instead found " + instead;
	}
	
}