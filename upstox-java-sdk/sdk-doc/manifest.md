# Android Manifest

The `AndroidManifest.xml` file at `src/main/AndroidManifest.xml` is a minimal manifest file required because this project is structured to be compatible with Android build systems (Gradle/Maven for Android), although it is primarily a Java SDK.

## Contents
```xml
<manifest package="com.upstox" xmlns:android="http://schemas.android.com/apk/res/android">
    <application />
</manifest>
```

- **Package**: `com.upstox`
- **Application**: Empty application tag.

This file serves as a placeholder to define the package namespace for Android-based tools.
