---
layout: default
---

Internationalization for IRIDA
===============================

Adding internationalizations for IRIDA is an easy but tedious task, and a very helpful way to contribute to the IRIDA project.  Internationalization files can be found under the `/src/main/resources/i18n` path in the IRIDA source code.  To add your language, all of the messages under `/src/main/resources/i18n/messages.properties` should be translated to your language, then saved to a new file with the language code.  For example: English = `messages_en.properties`, French = `messages_fr.properties`.  See a full list of language codes at <https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes>.  Any messages which are not translated will fall back to the `messages.properties` file.

After translating the messages file, your language must be enabled in IRIDA's web configuration.  See the [web configuration guide](../../../administrator/web#web-configuration) for more.

## Within HTML Templates

[Thymeleaf](https://www.thymeleaf.org) template engine is responsible for templating internationalized strings directly within HTML templates.  Please read the [Thymeleaf docs](https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html). 

## Within JavaScript External Files

 Because we use [Webpack](../webpack) to compile and minify JavaScript assets, Thymeleaf cannot be used to internationalize (or any other templating) since the syntax in not proper JavaScript.
 
 Instead, we have created a webpack plugin called `i18nPropertiesWebpackPlugin` to handle client side internationalization.  This works by going through all the webpack entries and looking for all function calls `i18n("term.to.translate")`, the argument to this method is the the string key in the messages file.
 
 ***NOTE:** Do not import the `i18n` method into the JavaScript file, webpack handles this dynamically*.
 
 **In `messages_en.properties`**:
 
 ```
feature_name_title=Interesting Modal
 ```
 
 **In JavaScript file that needs the translation**
 ```js
 i18n("feature_name_title");
 ```
 
 For each entry, webpack will gather the key from the JavaScript file, along with the translation from the messages file and create a new JavaScript file `[entry_name].[locale].js`.  This should be added to the HTML template above the script tag for the webpack bundle for that entry.  This file exposed a JSON object called `translations` to the `window` object which is consumed by the `i18n.js` loaded through the application.
 
 **Example**
 ```js
<script th:src="@{/dist/i18n/app.__${#locale.language}__.js}"></script>
```

Here, Thymeleaf will replace `__${#locale.language}__` with the required locale for the user (currently set in the `configuration.properties` file).