# nubo-test-tree

This repository contains a test software for Android Tree Client.

This project is part of [NUBOMEDIA](http://www.nubomedia.eu).

The source code is available at [https://github.com/nubomedia-vtt/kurento-room-client-android].

Documentation
--------------------
Documentation is available in [Github]. The more detailed Developers Guide and Installation Guide are available at [http://kurento-room-client-android.readthedocs.org/en/latest/](http://kurento-room-client-android.readthedocs.org/en/latest/)

Repository structure
--------------------
This repository consists of an Android Studio library project with gradle build scripts. 

Usage
--------
You can import this project to your own Android Studio project via Maven (jCenter or Maven Central) by adding the following line to module's `build.gradle` file:
```
compile 'fi.vtt.nubomedia:kurento-tree-client-android:version-number'
```

If you want to build the project from source, you need to import the third-party libraries via Maven by adding the following lines to
the module's `build.gradle` file
```
compile 'fi.vtt.nubomedia:utilities-android:version-number'
compile 'fi.vtt.nubomedia:jsonrpc-ws-android:version-number'
compile 'fi.vtt.nubomedia:webrtcpeer-android:version-number'
```

Licensing
---------
[BSD](https://github.com/nubomedia-vtt/kurento-room-client-android/blob/master/LICENSE)

***Contribution policy***

You can contribute to this project through bug-reports, bug-fixes, new code or new documentation. For contributing to the project, drop a post to the mailing list providing information about your contribution and its value. In your contributions, you must comply with the following guidelines

•	You must specify the specific contents of your contribution either through a detailed bug description, through a pull-request or through a patch.

•	You must specify the licensing restrictions of the code you contribute.

•	For newly created code to be incorporated in the code-base, you must accept the code copyright, so that its open source nature is guaranteed.

•	You must justify appropriately the need and value of your contribution. There is no obligations in relation to accepting contributions from third parties.

Support
-------
Support is provided through the [NUBOMEDIA VTT Public Mailing List]

[Github]: https://github.com/nubomedia/kurento-tree-client-android
[NUBOMEDIA VTT Public Mailing List]: https://groups.google.com/forum/#!forum/nubomedia-vtt