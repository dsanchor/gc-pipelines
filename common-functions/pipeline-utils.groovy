// pipeline-utils functions

def createConfigMap(name, pathToFiles) {
   echo "Creating configmap ${name} using files under path: ${pathToFiles}"
   // create temporal configmap and replace current one
   def configMap = openshift.create( "configmap", "${name}-tmp", "--from-file", pathToFiles ).object(exportable:true)
   configMap.metadata.name = name
   openshift.apply(configMap)  	
   openshift.selector( "configmap/${name}-tmp" ).delete()	
}

return this
