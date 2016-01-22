## Download&install

### Get Java

RED is compatible with any Oracle Java newer than 1.7 . Java official website: [www.java.com](https://www.java.com)

### Get Eclipse

Currently RED is tested with Eclipse Mars ( v4.5.x ) release thus we currently recommend Mars to be used. Eclipse Luna is supported also. We recommend to download respective OS version of Eclipse IDE for Java Developers: [https://www.eclipse.org/downloads/](https://www.eclipse.org/downloads/)

### Get RED

RED feature can be downloaded from [github.com/nokia/RED](https://github.com/nokia/RED) RED feature is a file with zip extension.

### Get Python & RobotFramework

In order to use full spectrum of RED features, Python & RobotFramework should be installed with respectful dependencies (currently Python 2.7.x is supported by RobotFramework). For convenience, we recommend using PIP.

Python official site: [www.python.org](http://www.python.org)

RobotFramework official site: [robotframework.org](http://robotframework.org/)

### Update existing RED installation
We recommend to not to do direct update of newer version, instead perform unistall old RED and install new RED after Eclipse restart.
Uninstall:
Open Help -> Installation Details, select old RED feature and perform unistall

### Install RED into clean Eclipse

Start Eclipse, accept default Workspace folder placement
Open _Help -> Install New Software_
Click _Add -> Archive_ and select RED zip feature (eg. org.robotframework.ide.eclipse.feature_1.0.0.201507130728.zip )
Tick all boxes in Robot Framework IDE Category and apply Next
Accept all prompts and user licences, proceed with installation process
Restart Eclipse as prompted
Verify RED installation by opening _Help -> Installation Details_ to check if RED is visible on installed features list
