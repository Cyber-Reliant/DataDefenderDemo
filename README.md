## $USER_HOME/.m2/settings.xml example
Build will not work until the settings.xml file has been edited to include authorization to the Cyber Reliant release server.
Access to the Cyber Reliant release server is granted upon request.
```xml

<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                            https://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>

        <server>
            <id>crcdatadefender</id>
            <username>cyberreliantcorp</username>
            <password>[AskCRCForAccess]</password>
        </server>

    </servers>
</settings>
```
