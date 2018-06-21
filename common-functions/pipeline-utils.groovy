// pipeline-utils functions

def createConfigMap(name, project, pathToFiles) {
   echo "Creating configmap ${name} in project ${project} using files under path: ${pathToFiles}"
   openshift.withProject( project ) {
	// create temporal configmap and replace current one
	def configMap = openshift.create( "${name}-tmp", "configmap", "--from-file", pathToFiles ).object(exportable:true)
	configMap.metadata.name = name
	openshift.apply(configMap)  	
	openshift.selector( "configmap/${name}-tmp" ).delete()	
   }
}

return this
