# react-native-denso-sp1

## Getting started

`$ npm install react-native-denso-sp1 --save`

### Mostly automatic installation

`$ react-native link react-native-denso-sp1`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-denso-sp1` and add `DensoSp1.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libDensoSp1.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import com.densosp1.DensoSp1Package;` to the imports at the top of the file
  - Add `new DensoSp1Package()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-denso-sp1'
  	project(':react-native-denso-sp1').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-denso-sp1/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-denso-sp1')
  	```


## Usage
```javascript
import DensoSp1 from 'react-native-denso-sp1';

// TODO: What to do with the module?
DensoSp1;
```
