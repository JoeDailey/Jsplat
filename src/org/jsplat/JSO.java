package org.jsplat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
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
	
	private static final Map<Character, Character> enclosers = new HashMap<Character, Character>(){
	private static final long serialVersionUID = 1244391900210794625L;
	{
		put('{', '}');
		put('[', ']');
		put('\"', '\"');
	}};

	private JSO root;
	private JSO parent;
	private Object data;
	private int type;
	private String name;
	

	public JSO(String json) throws malformedJSON{
		this("", TYPE_OBJECT, null, null, clean(json));
	}
	public JSO(String name, int TYPE, JSO root, JSO parent, String json) throws malformedJSON{
		this.root = root;
		if(root == null);
			this.root = this;
		this.type = TYPE;
		this.parent = parent;
		
		
		switch(TYPE){
			case TYPE_OBJECT:
				this.data = newOfObject(root, this, json);
				this.name = name;
			break;
			case TYPE_ARRAY:
				this.data = newOfArray(root, this, json);
				this.name = name;
			break;
			case TYPE_STRING:
				this.data = json.substring(1, json.length()-1);
				this.name = name;
			break;
			case TYPE_NUMBER:
				this.data = Double.parseDouble(json.substring(1, json.length()));
				this.name = name;
			break;
			case TYPE_BOOLEAN:
				if( json.toLowerCase().equals("true") )
					data = true;
				else
					data = false;
				this.name = name;
			break;
			case TYPE_NULL:
				data = null;
				this.name = name;
			break;
			default:
				this.data = json;
				this.name = name;
			break;
		}
	}
	

	private static String[] splitTop(String json){
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
		s.close();
		return json.replace("\t", "").replace("\n", "");
	}
	private static HashMap<String, JSO> newOfObject(JSO root, JSO parent, String json) throws malformedJSON {
		HashMap<String, JSO> meObj = new HashMap<String, JSO>();
		
		for(String prop : splitTop(json)){
			String[] part = prop.split(":", 2);
			if(part.length!=2) throw new malformedJSON("tag and property separated by a :", prop);
			if(part[0].charAt(0) == '"') part[0] = part[0].substring(1, part[0].length());
			if(part[0].charAt(part[0].length()-1) == '"') part[0] = part[0].substring(0, part[0].length()-1);

			switch(part[1].charAt(0)){
				case '\"':	//is String
					meObj.put(part[0], new JSO(part[0], TYPE_STRING, root, parent, part[1]));
				break;
				case '{':	//is object
					meObj.put(part[0], new JSO(part[0], TYPE_OBJECT, root, parent, part[1]));
				break;
				case '[':	//is array	
					meObj.put(part[0], new JSO(part[0], TYPE_ARRAY, root, parent, part[1]));
				break;
				default:
					if( part[1].equals("undefined") || part[1].equals("UNDEFINED") )//is undefined
						meObj.put(part[0], new JSO(part[0], TYPE_UNDEFINED, root, parent, part[1]));
					else if( part[1].equals("null") || part[1].equals("NULL") )	//is null
						meObj.put(part[0], new JSO(part[0], TYPE_NULL, root, parent, null));
					else if( part[1].equals("true") || part[1].equals("TRUE")
							||
							part[1].equals("false") || part[1].equals("FALSE") )//is bool
						meObj.put(part[0], new JSO(part[0], TYPE_BOOLEAN, root, parent, part[1]));
					else if( part[1].contains("function") )	//is function
						meObj.put(part[0], new JSO(part[0], TYPE_FUNCTION, root, parent, part[1]));
					else{
						try{  
						    Double.parseDouble(part[1]);
						    meObj.put(part[0], new JSO(part[0], TYPE_NUMBER, root, parent, part[1]));
						}catch(Exception e){
							throw new malformedJSON("type of STRING, NUMBER, NULL, TRUE, FALSE, OBJECT, ARRAY, or UNDEFINED", part[1]);  
						}
					}
				break;
			}
		}
		
		return meObj;
	}
	private static HashMap<String, JSO> newOfArray(JSO root, JSO parent, String json) throws malformedJSON {
		HashMap<String, JSO> meArr = new HashMap<String, JSO>();
		
		int propCount = 0;
		
		for(String prop : splitTop(json)){
			switch(prop.charAt(0)){
				case '\"':	//is String
					meArr.put(""+propCount, new JSO(""+propCount, TYPE_STRING, root, parent, prop));
				break;
				case '{':	//is object
					meArr.put(""+propCount, new JSO(""+propCount, TYPE_OBJECT, root, parent, prop));
				break;
				case '[':	//is array	
					meArr.put(""+propCount, new JSO(""+propCount, TYPE_ARRAY, root, parent, prop));
				break;
				default:
					if( prop.equals("undefined") || prop.equals("UNDEFINED") )//is undefined
						meArr.put(""+propCount, new JSO(""+propCount, TYPE_UNDEFINED, root, parent, prop));
					else if( prop.equals("null") || prop.equals("NULL") )	//is null
						meArr.put(""+propCount, new JSO(""+propCount, TYPE_NULL, root, parent, prop));
					else if( prop.equals("true") || prop.equals("TRUE")
							||
							prop.equals("false") || prop.equals("FALSE") )//is bool
						meArr.put(""+propCount, new JSO(""+propCount, TYPE_BOOLEAN, root, parent, prop));
					else if( prop.contains("function") )	//is function
						meArr.put(""+propCount, new JSO(""+propCount, TYPE_FUNCTION, root, parent, prop));
					else{
						try{  
						    Double.parseDouble(prop);
						    meArr.put(""+propCount, new JSO(""+propCount, TYPE_NUMBER, root, parent, prop));
						}catch(Exception e){
							throw new malformedJSON("type of STRING, NUMBER, NULL, TRUE, FALSE, OBJECT, ARRAY, or UNDEFINED", prop);  
						}
					}
				break;
			}
			propCount++;
		}
		return meArr;
	}
	
	public Object get() {
		if(!(this.type == TYPE_ARRAY) || !(this.type == TYPE_BOOLEAN))
			return this.data;
		return this;
	}
	public Object get(String node) throws NodeNotFound{
		return traverse(node).get();
	}
	public Object get(int node) throws NodeNotFound{
		return traverse("" + node).get();
	}

	@SuppressWarnings("unchecked")
	public boolean set(String node, String Value) throws NodeNotFound{
		JSO toSet = this.traverse(node);
		String[] nodeArr = node.split("\\.");
		String finode = nodeArr[nodeArr.length-1];
		HashMap<String, JSO> parentData = (HashMap<String, JSO>) toSet.parent().data;
		
		if(Value == null){
			try {
				parentData.put(finode, new JSO(toSet.name, TYPE_NULL, toSet.root, toSet.parent, null));
				return true;
			} catch (malformedJSON e) {
				e.printStackTrace();
				return false;
			}
		}
		if(Value.charAt(0) == '{'){
			try {
				parentData.put(finode, new JSO(toSet.name, TYPE_OBJECT, toSet.root, toSet.parent, Value));
				return true;
			} catch (malformedJSON e) {
				e.printStackTrace();
				return false;
			}
		}
		if(Value.charAt(0) == '['){
			try {
				parentData.put(finode, new JSO(toSet.name, TYPE_ARRAY, toSet.root, toSet.parent, Value));
				return true;
			} catch (malformedJSON e) {
				e.printStackTrace();
				return false;
			}
		}
		try {
			parentData.put(finode, new JSO(toSet.name, TYPE_STRING, toSet.root, toSet.parent, "\""+Value+"\""));
			return true;
		} catch (malformedJSON e) {
			e.printStackTrace();
			return false;
		}
	}
	@SuppressWarnings("unchecked")
	public boolean set(String node, double Value) throws NodeNotFound{
		JSO toSet = this.traverse(node);
		String[] nodeArr = node.split("\\.");
		String finode = nodeArr[nodeArr.length-1];
		HashMap<String, JSO> parentData = (HashMap<String, JSO>) toSet.parent().data;
		try {
			parentData.put(finode, new JSO(toSet.name, TYPE_NUMBER, toSet.root, toSet.parent, ""+Value));
			return true;
		} catch (malformedJSON e) {
			e.printStackTrace();
			return false;
		}
	}
	@SuppressWarnings("unchecked")
	public boolean set(String node, int Value) throws NodeNotFound{
		JSO toSet = this.traverse(node);
		String[] nodeArr = node.split("\\.");
		String finode = nodeArr[nodeArr.length-1];
		HashMap<String, JSO> parentData = (HashMap<String, JSO>) toSet.parent().data;
		try {
			parentData.put(finode, new JSO(toSet.name, TYPE_NUMBER, toSet.root, toSet.parent, ""+Value));
			return true;
		} catch (malformedJSON e) {
			e.printStackTrace();
			return false;
		}
	}
	@SuppressWarnings("unchecked")
	public boolean set(String node, boolean Value) throws NodeNotFound{
		JSO toSet = this.traverse(node);
		String[] nodeArr = node.split("\\.");
		String finode = nodeArr[nodeArr.length-1];
		HashMap<String, JSO> parentData = (HashMap<String, JSO>) toSet.parent().data;
		try {
			parentData.put(finode, new JSO(toSet.name, TYPE_BOOLEAN, toSet.root, toSet.parent, ""+Value));
			return true;
		} catch (malformedJSON e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean set(String Value){
		if(Value == null){
			this.data = null;
			this.type = TYPE_NULL;
			return true;
		}
		if(Value.charAt(0) == '{'){
			try {
				this.data = newOfObject(this.root,this.parent, Value);
				this.type = TYPE_OBJECT;
				return true;
			} catch (malformedJSON e) {
				e.printStackTrace();
				return false;
			}
		}
		if(Value.charAt(0) == '['){
			try {
				this.data = newOfArray(this.root,this.parent, Value);
				this.type = TYPE_ARRAY;
				return true;
			} catch (malformedJSON e) {
				e.printStackTrace();
				return false;
			}
		}
		this.data = Value;
		this.type = TYPE_STRING;
		return true;

	}
	public boolean set(double Value){
		this.data = Value;
		this.type = TYPE_NUMBER;

		return true;
	}
	public boolean set(int Value){
		this.data = Value;
		this.type = TYPE_NUMBER;
		return true;
	}
	public boolean set(boolean Value){
		this.data = Value;
		this.type = TYPE_BOOLEAN;
		return true;
	}
	
	public JSO traverse(String node) throws NodeNotFound{
		return traverse(this, node);
	}
	@SuppressWarnings("unchecked")
	private JSO traverse(JSO jso, String nodes) throws NodeNotFound {
		String[] route = nodes.split("\\.");

		JSO rtrn = null;
		if(route.length == 1){
			if(!(jso.type == TYPE_ARRAY) && !(jso.type == TYPE_OBJECT))
				rtrn = jso;
			else
				rtrn = ((HashMap<String, JSO>)jso.data).get(route[0]);
			if(rtrn == null)
				throw new NodeNotFound(nodes);
			return rtrn;
		}
		String nextNodes = "";
		for(int i = 1; i<route.length-1; i++){
			nextNodes+= route[i]+".";
		}
		nextNodes+= route[route.length-1];
		
		return traverse(((HashMap<String, JSO>)jso.data).get(route[0]), nextNodes);
	}
	
	public JSO parent(){
		return this.parent;
	}
	
	@SuppressWarnings("unchecked")
	public JSO[] children(){
		if(this.type == TYPE_ARRAY || this.type == TYPE_OBJECT){
			Map<String, JSO> children = (Map<String, JSO>) this.data;
			return (JSO[]) children.values().toArray(new JSO[children.size()]);
		}
		return null;
	}
	
	public JSO root(){
		return this.root;
	}
	
	public JSO find(String nodes) throws NodeNotFound{
		String[] route = nodes.split("\\.");
		JSO found = find(this, route[0]);			
		if(found != null)
			return found.parent().traverse(nodes);
		return null;
			
	}
	private JSO find(JSO jso, String node){
		if(jso.name.equals(node))
			return jso;
		if(jso.type == TYPE_ARRAY || jso.type == TYPE_OBJECT){
			for(JSO child : jso.children()){
				JSO found = find(child, node);
				if(found != null)
					return found;
			}
		}
		return null;
	}

	
	@Override
	public String toString() {
		return toString(0, this);
	}
	private String toString(int levels, JSO object) {
		String out = "";
		if(object.root != object)
			out += "\"" + object.name + "\"" + ":";
		if(object.type == TYPE_ARRAY){
			out += "[";
			for(JSO child : object.children()){
				out += "\n" + tabs(levels+1);
				out += child.toString(levels+1, child);
				out += ",";
			}
			out = out.substring(0, out.length()-1);
			out += "\n" + tabs(levels) + "]";
			return out;
		}
		if(object.type == TYPE_OBJECT){
			out += "{";
			for(JSO child : object.children()){
				out += "\n" + tabs(levels+1);
				out += "\"" + child.name + "\"" + ":";
				out += child.toString(levels+1, child);
				out += ",";
			}
			out = out.substring(0, out.length()-1);
			out += "\n" + tabs(levels) + "}";			
			return out;
		}
		return out+"\""+object.data.toString()+"\"";
	}
	
	public String stringify(){
		return this.toString().replace("\t", "").replace("\n", "");
	}
	
	private String tabs(int levels){
		String tabs = "";
		for(int i = 0; i < levels; i++)
			tabs+="\t";
		return tabs;
	}
	
	
	
	public Object g(String node) throws NodeNotFound{return get(node);};
	public Object g(int node) throws NodeNotFound{return get(""+node);};
	public Object g() throws NodeNotFound{return get("");};
	public boolean s(String node, String value) throws NodeNotFound{return set(node, value);};
	public boolean s(String value) throws NodeNotFound{return set("", value);};
	public JSO o(String node) throws NodeNotFound{return traverse(node);}
	public JSO p(){return parent();}
	public JSO[] c(){return children();}
	public JSO r(){return root();}
	public JSO f(String nodes) throws NodeNotFound{return find(nodes);}
}

class malformedJSON extends Exception{
	private static final long serialVersionUID = -1976415057769292607L;
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
class NodeNotFound extends Exception{
	private static final long serialVersionUID = 7091727938827727023L;
	private final String node;
	public NodeNotFound(String node) {
		this.node = node;
	}
	@Override
	public String getMessage() {
		return node + " not found";
	}
}