# Cyber Reliant DataDefender Android demo

This application demonstrates usage of the CRCDataDefender Android library.
Please refer to the [build.gradle](DataDefenderDemo/build.gradle) in this project for a full example.

## Key concepts

### Using CRCDataDefender in your application build.gradle dependencies
:file_folder: application build.gradle
```gradle
implementation 'com.cyberreliant:CRCDataDefender:4.0.6.6'
```

### Adding CRC package repo
The releases of CRCDataDefender are in a private Maven repository. You have to add the repo and set up authentication.

:file_folder: application build.gradle
```gradle
repositories {
    maven {
        url 'https://pkgs.dev.azure.com/cyberreliantcorp/libcrcprotect/_packaging/crcdatadefender/maven/v1'
        name 'crcdatadefender'
        authentication {
            basic(BasicAuthentication)
        }
    }
}
```

### Enabling access to CRC package repo
Build will not work until the settings.xml file has been edited to include authorization to the Cyber Reliant release server.
Access to the Cyber Reliant release server is granted upon request.

:file_folder: $USER_HOME/.m2/settings.xml
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
