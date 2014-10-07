Jsplat
====

JSON Parsing and Loosely Adjustable Traversing
====

Throw JSON at the wall!

Another Java implementation of a JSON data structure but this time for JavaScript programmers.

Those familiar with JSON index accessing will recognize this pattern.

Most object methods have shortened named versions.

```xml
1 param = traverse to that property and do (selector)
2 param = set selected value as 2nd param
```
Here is the sample JSON we will used for examples.
```javascript
myObj = {
    "glossary": {
        "title": "example glossary",
		"list":[ 10, 12, 32 ]
    }
}
```
Creating a JSO (JavaScript Object)
```java
JSO myJSO = new JSO(myObj);
```


Getting values from JSON
==
All of the following lines will get the same value at title.
```java
String title = myJSO.traverse("glossary").traverse("title").get();
String title = myJSO.j("glossary").j("title").g();
String title = myJSO.j("glossary").g("title");
String title = myJSO.g("glossary.title");
String title = myJSO.j("glossary.title").g();
```


Setting values into JSON
==
These will set `myObj.glossary.title` = `"asdf"`
```java
myJSO.traverse("glossary").traverse("title").set("asdf");
myJSO.j("glossary").j("title").s("asdf");
myJSO.j("glossary").s("title", "asdf");
myJSO.s("glossary.title", "asdf");
myJSO.j("glossary.title").s("asdf");
```

A note on arrays
==
All variations still apply
```java
double myNum = myJSO.j("glossary.list").g(0);
double myNum = myJSO.j("glossary.list").g("0");
```

The root
==
```java
JSO myNewJSO = myJSO.j("glossary");
JSO myRoot = myNewJSON.root();
JSO myRoot = myNewJSON.r();
//myRoot equals myJSO
```

The parent
==
```java
JSO myParentJSO = myJSO.j("glossary").parent();
JSO myParentJSO = myJSO.j("glossary").p();
//myParentJSO equals myJSO
```

The children
==
```java
JSO[] children = myJSO.j("glossary").children();
JSO[] children = myJSO.j("glossary").c();
//children = [JSO, JSO]
```

The type
==
There are more types available. Reference is not completed.
```java
int type = myJSO.type();
//type == JSO.TYPE_OBJECT
int type = myJSO.j("list").type();
//type == JSO.TYPE_ARRAY
int type = myJSO.j("glossary.title").type();
//type == JSO.TYPE_STRING
```
All Types:
TYPE_OBJECT;
TYPE_ARRAY;
TYPE_NUMBER;
TYPE_BOOLEAN;
TYPE_FUNCTION;
TYPE_STRING;
TYPE_UNDEFINED;
TYPE_NULL;

The find
==
```java
JSO found = myJSO.find("title");
JSO found = myJSO.find("glossary.title");
JSO found = myJSO.f("glossary.title");
```

The stringify
==
no spaces, no tabs, no line-breaks
```java
String JSON = myJSO.stringify();
```

The toString
==
spaces, tabs, line-breaks for readability
```java
String prettyJSON = myJSO.toString();
```
