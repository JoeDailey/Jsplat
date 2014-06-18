Jsplat
====

JavaScript Loose and Adjustable Translating
====

Throw JSON at the wall.
Another Java implementation of JSON for JavaScript programmers.

Here is the sample JSON we will used for examples.
```javascript
{
    "glossary": {
        "title": "example glossary",
		"GlossDiv": {
            "title": "S",
			"GlossList": {
                "GlossEntry": {
                    "ID": "SGML",
					"SortAs": "SGML",
					"GlossTerm": "Standard Generalized Markup Language",
					"Acronym": "SGML",
					"Abbrev": "ISO 8879:1986",
					"GlossDef": {
                        "para": "A meta-markup language, used to create markup languages such as DocBook.",
						"GlossSeeAlso": ["GML", "XML"]
                    },
					"GlossSee": "markup"
                }
            }
        }
    }
}
```

Creating a JSO (JavaScript Object)
```javascript
JSO myJSO = new JSO(someJSONstring);
```

traversing the JSO
```javascript
myJSO.j("myProperty").j("myNextProperty").g()
```


