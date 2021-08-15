# ElementHistoryDialog

Compare changes between 2 different versions(in history) for any node, way, relation in OpenStreetMap

#### Highlights:
  * Different version based on user edits
  * Changes in Description, Imagery Used, Source for the data (Changeset)
  * Changes in the tags between 2 versions
  * Changes in node coordinates between 2 versions
  * Changes in way nodes between 2 elements
  * Changes in relation members between 2 elements
  
 #### Usage:
 The library takes 2 parameters
  * The id of the element 
    * This is a Long representing the OSM id of the current selection
       
  * The type of the element 
    * node
    * way
    * relation 

Example Usage - 

 * Add the project using jitpack
  
  ```
   allprojects {
    repositories {
      jcenter()
      maven { url "https://jitpack.io" }
    }
   }
   
  ```
   
 * Add the dependency
  ```
  dependencies {
	        implementation 'com.github.zedlabs:ElementHistoryDialog:1.0.0'
	}
 
 ```

 * The dialog extends the android ```DialogFragment```, so it can be instantiated similar to any other ```DialogFragment```
 ```java  
 
  // from an activity
  // with element id = 1 and element type = "node"
  
  ElementHistoryDialog ehd = ElementHistoryDialog.create(1, "node");
  ehd.show(getSupportFragmentManager(), "sample");
 
 ```
 
 
#### Screenshots:

  <img src="https://github.com/zedlabs/ElementHistoryDialog/blob/master/assets/B.png" width="200" height="400">    <img src="https://github.com/zedlabs/ElementHistoryDialog/blob/master/assets/C.png" width="200" height="400"> <img src="https://github.com/zedlabs/ElementHistoryDialog/blob/master/assets/A.png" width="200" height="400"> <img src="https://github.com/zedlabs/ElementHistoryDialog/blob/master/assets/D.png" width="200" height="400"> 
